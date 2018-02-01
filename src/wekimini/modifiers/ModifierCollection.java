/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
/**
 *
 * @author louismccallum
 * 
 * This has all the modifiers for a single path/output
 */
public class ModifierCollection {
    
    private List<ModifiedInput> modifiers;
    private int dimensionality;
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private transient double[] currentValues;
    private int currentID = 0;
    public String[] valueMap;
    
    public ModifierCollection(List<ModifiedInput> modifiers) 
    {
        this.modifiers = new LinkedList<>(modifiers);
        refreshState();
    }
    
    public ModifierCollection(ModifierCollection groupFromFile) {
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
        return modifier.inputID;
    }
    
    private int nextID()
    {
        currentID++;
        return currentID;
    }
    
    //Remove modifiers that do not output themselves or input to another modifier
    public void removeDeadEnds()
    {
        ArrayList<ModifiedInput> toRemove = new ArrayList();
        for(ModifiedInput modifier:modifiers)
        {
            Boolean doesOutput = modifier.addToOutput;
            Boolean atLeastOneChild = false;
            if(!doesOutput)
            {
                INNER:
                for(ModifiedInput existing:modifiers)
                {
                    for(Integer input:existing.getRequiredInputs())
                    {
                        if(modifier.inputID == input)
                        {
                            atLeastOneChild = true;
                            break INNER;
                        }
                    }
                }
            }
            if(!atLeastOneChild && !doesOutput && modifier.inputID > 0)
            {
                toRemove.add(modifier);
            }
        }
        for(ModifiedInput remove:toRemove)
        {
            removeModifier(remove.inputID);
        }
        if(toRemove.size() > 0)
        {
            refreshState();
        }
    }
    
    //Remove modifiers whose required input does not exist anymore
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
            removeModifier(remove.inputID);
        }
        if(toRemove.size() > 0)
        {
            refreshState();
        }

    }
    
    protected void removeAllModifiers()
    {
        int [] ids = new int[modifiers.size()];
        int ptr = 0;
        for(ModifiedInput modifier: modifiers)
        {
            ids[ptr] = modifier.inputID;
            ptr++;
        }
        for(int id:ids)
        {
            removeModifier(id);
        }
        removeOrphanedModifiers();
        removeDeadEnds();
    }
    
    public void removeModifier(int id)
    {
        if(id > 0)
        {
            Boolean canRemove = true;
            INNER:
            for(ModifiedInput existing:modifiers)
            {
                for(Integer input : existing.getRequiredInputs())
                {
                    if(input == id)
                    {
                        canRemove = false;
                        break INNER;
                    }
                }
            }
            if(canRemove)
            {
                int index = indexForID(id);
                if(index > 0)
                {
                    modifiers.remove(index);
                    refreshState();
                }
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
        return -1;
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
    
    //Outputs
    
    public int getOutputDimensionality() {
        return dimensionality;
    }
    
    //Calculate outputs
    
    private void computeValuesForNewInputs(double[] newInputs, HashMap<String, Feature> features) {
        
        int outputIndex = 0;
        int completedIndex = 0;
        valueMap = new String[currentValues.length];
        ArrayList<ModifiedInput> completedModifiers = new ArrayList();

        for (ModifiedInput modifier : modifiers) 
        {
            modifier.prepareForNewInputs();
        }
        
        //Get the raw inputs first
        ModifiedInput completedModifier = modifiers.get(0);
        completedModifier.updateForInputs(newInputs);
        completedModifiers.add(modifiers.get(0));
        if(completedModifier.addToOutput)
        {
            System.arraycopy(((ModifiedInputVector)completedModifier).getValues(), 0, currentValues, outputIndex, completedModifier.getSize());
            outputIndex += completedModifier.getSize();
        }

        while(completedIndex < completedModifiers.size())
        {
            completedModifier = completedModifiers.get(completedIndex);
            for (ModifiedInput toComplete : modifiers) 
            {
                if(!toComplete.hasAllInputs()) 
                {
                    toComplete.updateRequiredInputs(completedModifier);
                    if(toComplete.hasAllInputs())
                    {
                        //System.out.println("collating inputs for " + toComplete.inputID);
                        toComplete.collateInputsFromModifiers(modifiers);
                        completedModifiers.add(toComplete);
                        if(toComplete.addToOutput)
                        {
                            //SAVE THE INDEX OF THE VALUE ADDED AND A REFERNCE TO ITS SOURCE (THE MODIFIER)
                            String featureName = getFeatureNameForModifierID(toComplete.inputID, features);
                            if (toComplete instanceof ModifiedInputSingle) 
                            {
                                currentValues[outputIndex] = ((ModifiedInputSingle)toComplete).getValue();
                                valueMap[outputIndex] = featureName;
                            } 
                            else 
                            {
                                System.arraycopy(((ModifiedInputVector)toComplete).getValues(), 0, currentValues, outputIndex, toComplete.getSize());
                                for(int i = 0; i < toComplete.getSize(); i++)
                                {
                                    valueMap[outputIndex + i] = featureName + ":" + i;
                                }
                            }
                            outputIndex += toComplete.getSize();
                        }
                    }
                }
            }
            completedIndex++;
        }
    }

    public double[] computeAndGetValuesForNewInputs(double[] newInputs, HashMap<String, Feature> features) {
        computeValuesForNewInputs(newInputs, features);
        return currentValues;
    }
    
    public String getFeatureNameForModifierID(int id, HashMap<String, Feature> features)
    {
        for(Feature feature:features.values())
        {
            if(feature instanceof FeatureSingleModifierOutput)
            {
                if(id == ((FeatureSingleModifierOutput) feature).getOutputModifierID())
                {
                    return ((FeatureSingleModifierOutput) feature).name;
                }
            }
            else
            {
                int ptr = 0;
                for(int matchID:((FeatureMultipleModifierOutput) feature).getOutputModifierIDs())
                {
                    if(id == matchID)
                    {
                        return ((FeatureMultipleModifierOutput) feature).name + ":" + Integer.toString(ptr);
                    }
                    ptr++;
                }
            }
        }
        return "not found";
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
