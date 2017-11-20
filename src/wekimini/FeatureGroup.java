/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.ModifiedInputSingle;
import wekimini.modifiers.ModifiedInputVector;
import wekimini.modifiers.FeatureLibrary;
/**
 *
 * @author louismccallum
 * 
 * This has all the modifiers for a single path/output
 */
public class FeatureGroup {
    
    private List<ModifiedInput> modifiers;
    private int dimensionality;
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private transient double[] currentValues;
    private transient double[] lastInputs;
    private boolean dirtyFlag = true;
    private int currentID = 0;
    protected FeatureLibrary featureLibrary;
    
    public FeatureGroup(List<ModifiedInput> modifiers) 
    {
        this.modifiers = new LinkedList<>(modifiers);
        featureLibrary = new FeatureLibrary();
        refreshState();
    }
    
    public FeatureGroup(FeatureGroup groupFromFile) {
        this.modifiers = new LinkedList<>(groupFromFile.getModifiers());
        this.dimensionality = groupFromFile.dimensionality;
    }
    
    private void refreshState()
    {
        int s = 0;
        boolean d = false;
        for (ModifiedInput output : modifiers) {
            if(output.addToOutput)
            {
                s += output.getSize();
            }
        }
        dimensionality = s;
        currentValues = new double[s];
    }
    
    //Modifiers 
    
    public int addModifier(ModifiedInput modifier)
    {
        Boolean matched = false;
        for(ModifiedInput existingModifier:modifiers)
        {
            if(existingModifier.equals(modifier))
            {
                matched = true;
                modifier = existingModifier;
                break;
            }
        }
        if(!matched)
        {
            modifier.inputID = nextID();
            modifiers.add(modifier);
        }
        refreshState();
        setDirty();
        return modifier.inputID;
    }
    
    private int nextID()
    {
        currentID++;
        return currentID;
    }
    
    public void removeOrphanedModifiers()
    {
        ArrayList<ModifiedInput> toRemove = new ArrayList();
        for(ModifiedInput modifier:modifiers)
        {
            Boolean foundParent = false;
            INNER:
            for(Integer input : modifier.getRequiredInputs())
            {
                for(ModifiedInput existing:modifiers)
                {
                    if(input == existing.inputID)
                    {
                        foundParent = true;
                        break INNER;
                    }
                }
            }
            if(!foundParent && modifier.inputID > 0)
            {
                toRemove.add(modifier);
            }
        }
        for(ModifiedInput remove:toRemove)
        {
            removeModifier(modifiers.indexOf(remove));
        }
        if(toRemove.size() > 0)
        {
            refreshState();
            setDirty();
        }

    }
    
    public void removeModifier(int id)
    {
        if(id > 0)
        {
            Boolean canRemove = true;
            for(ModifiedInput existing:modifiers)
            {
                for(Integer input : existing.getRequiredInputs())
                {
                    if(input == id)
                    {
                        canRemove = false;
                        break;
                    }
                }
            }
            if(canRemove)
            {
                modifiers.remove(indexForID(id));
                refreshState();
                setDirty();
            }
        }
    }
    
    private int indexForID(int id)
    {
        for(int i = 0; i < modifiers.size(); i++)
        {
            if(modifiers.get(i).inputID == id)
            {
                return i;
            }
        }
        return 0;
    }
    
    public List<ModifiedInput> getModifiers() {
        return modifiers;
    }
    
    public int getNumModifiers() {
        return modifiers.size();
    }
    
    public ModifiedInput getModifier(int which) {
        return modifiers.get(which);
    }
    
    protected void passThroughInputToOutput(boolean passThrough)
    {
        modifiers.get(0).addToOutput = passThrough;
        setDirty();
        refreshState();
    }
    
    //Dirty State
    
    protected boolean isDirty()
    {
        return dirtyFlag;
    }
    
    protected void setDirty()
    {
       dirtyFlag = true;
       for(ModifiedInput modifier:modifiers)
       {
           modifier.reset();
       }
    }
    
    protected void didRecalculateFeatures()
    {
        dirtyFlag = false;
    }
    
    //Outputs
    
    public int getOutputDimensionality() {
        return dimensionality;
    }
    
    public int getOutputNumber(FeatureGroup o) {
        for (int i= 0; i < modifiers.size(); i++) {
            if (modifiers.get(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }
    
    //Calculate outputs
    
    private void computeValuesForNewInputs(double[] newInputs) {
        
        int outputIndex = 0;
        int matchingIndex = 0;
        ArrayList<ModifiedInput> completedModifiers = new ArrayList();

        for (ModifiedInput modifier : modifiers) 
        {
            modifier.prepareForNewInputs();
        }
        
        ModifiedInput currentModifier = modifiers.get(0);
        currentModifier.updateForInputs(newInputs);
        completedModifiers.add(modifiers.get(0));
        if(currentModifier.addToOutput)
        {
            System.arraycopy(((ModifiedInputVector)currentModifier).getValues(), 0, currentValues, outputIndex, currentModifier.getSize());
            outputIndex += currentModifier.getSize();
        }

        while(matchingIndex < completedModifiers.size())
        {
            currentModifier = completedModifiers.get(matchingIndex);
            for (ModifiedInput modifier : modifiers) 
            {
                if(!modifier.hasAllInputs()) 
                {
                    modifier.isInputRequired(currentModifier);
                    if(modifier.hasAllInputs())
                    {
                        modifier.collateInputsFromModifiers(modifiers);
                        completedModifiers.add(modifier);
                        if(modifier.addToOutput)
                        {
                            if (modifier instanceof ModifiedInputSingle) 
                            {
                                currentValues[outputIndex] = ((ModifiedInputSingle)modifier).getValue();
                            } 
                            else 
                            {
                                System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, currentValues, outputIndex, modifier.getSize());
                            }
                            outputIndex += modifier.getSize();
                        }
                    }
                }
            }
            matchingIndex++;
        }
    }

    public double[] computeAndGetValuesForNewInputs(double[] newInputs) {
        computeValuesForNewInputs(newInputs);
        lastInputs = newInputs;
        return currentValues;
    }
    
    public double[] getLastInputs() {
        return lastInputs; //is returning null when nothing is computed yet...
    }
    
    public String[] getOutputNames() {
        int currentIndex = 0;
        String[] s = new String[getOutputDimensionality()];
        for (ModifiedInput o : modifiers) {
            if (o instanceof ModifiedInputSingle) {
                s[currentIndex] = ((ModifiedInputSingle)o).getName();
                currentIndex++;
            } else {
                for (int i = 0 ; i < o.getSize(); i++) {
                    s[currentIndex] = ((ModifiedInputVector)o).getNames()[i];
                    currentIndex++;
                }
            }
        }
        return s;
    }  
    
    //Property Change Listeners
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
}
