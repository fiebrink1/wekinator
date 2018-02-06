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
    private String[] outputNames;
    
    private ModifierCollection() {}
    
    public ModifierCollection(String[] inputNames) 
    {
        ArrayList<ModifiedInput> defaultModifiers = new ArrayList();
        ModifiedInput rawInput = new PassThroughVector(inputNames, 0);
        rawInput.inputID = 0;
        rawInput.addToOutput = false;
        defaultModifiers.add(rawInput);
        this.modifiers = new LinkedList<>(defaultModifiers);
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
        for(ModifiedInput existingModifier:modifiers)
        {
            if(existingModifier.equals(modifier))
            {
                return existingModifier.inputID;
            }
        }
        modifier.inputID = nextID();
        modifiers.add(modifier);
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
    
    public int indexForID(int id)
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
    
    public String[] getOutputNames()
    {
        return outputNames;
    }
    
    public String nameForIndex(int index)
    {
        return outputNames[index];
    }
    
    public int indexForName(String name)
    {
        int i = 0;
        for(String match:outputNames)
        {
            if(match.equals(name))
            {
                return i;
            }
            i++;
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
    
    //Outputs
    
    public int getOutputDimensionality() {
        return dimensionality;
    }
    
    //Calculate outputs
    
    private void computeValuesForNewInputs(double[] newInputs, HashMap<String, Feature> features) {
        
        int outputIndex = 0;
        int completedIndex = 0;
        outputNames = new String[currentValues.length];
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
                                outputNames[outputIndex] = featureName + ":0";
                            } 
                            else 
                            {
                                System.arraycopy(((ModifiedInputVector)toComplete).getValues(), 0, currentValues, outputIndex, toComplete.getSize());
                                for(int i = 0; i < toComplete.getSize(); i++)
                                {
                                    outputNames[outputIndex + i] = featureName + ":" + i;
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
   
    //Property Change Listeners
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
}
