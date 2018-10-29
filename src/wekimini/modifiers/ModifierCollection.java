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
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
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
    private double[] currentValues;
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
        for (ModifiedInput output : modifiers) {
            if(output.addToOutput)
            {
                s += output.getSize();
                if(output.getSize() > 1)
                {
                    System.out.println("Multiple out:" + s);
                }
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
    
    public ArrayList<Integer> indexesForName(String name)
    {
        ArrayList<Integer> indexes = new ArrayList();
        int i = 0;
        for(String match:outputNames)
        {
            String[] split = match.split(":");
            if(split.length == 0)
            {
                System.out.println("----------NO COLONS IN NAME");
            }
            else if(split[0].equals(name))
            {
                indexes.add(i);
            }
            i++;
        }
        return indexes;
    }
    
    public List<ModifiedInput> getModifiers() {
        return modifiers;
    }
    
    public int getNumModifiers() {
        return modifiers.size();
    }

    public ModifiedInput getModifierForID(int ID) {
        return modifiers.get(indexForID(ID));
    }
    
    //Outputs
    
    public int getOutputDimensionality() {
        return dimensionality;
    }
    
    //Calculate outputs
    
    private void computeValuesForNewInputs(double[] newInputs, HashMap<String, Feature> features, boolean updateNames) {
        
        int modifierOutputIndex = 0;
        int completedIndex = 0;
        double[] newVals = new double[currentValues.length];
        for(int i = 0; i < newVals.length; i++)
        {
            newVals[i] = 0;
        }
        String[] names = new String[dimensionality];
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
            System.arraycopy(((ModifiedInputVectorOutput)completedModifier).getValues(), 0, newVals, modifierOutputIndex, completedModifier.getSize());
            modifierOutputIndex += completedModifier.getSize();
        }

        while(completedIndex < completedModifiers.size())
        {
            completedModifier = completedModifiers.get(completedIndex);
            for (ModifiedInput toComplete : modifiers) {
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
                            String featureName = "";
                            if(updateNames)
                            {
                                //SAVE THE INDEX OF THE VALUE ADDED AND A REFERNCE TO ITS SOURCE (THE MODIFIER)
                                featureName = getFeatureNameForModifierID(toComplete.inputID, features);
                            }
                            
                            
                            if (toComplete instanceof ModifiedInputSingleOutput) 
                            {
                                newVals[modifierOutputIndex] = ((ModifiedInputSingleOutput)toComplete).getValue();
                                if(updateNames)
                                {
                                    String name = featureName + ":0";
                                    //System.out.println("added feature name " + name + " - " + toComplete.inputID);
                                    names[modifierOutputIndex] = name;
                                }
                            }
                            else 
                            {
                                System.arraycopy(((ModifiedInputVectorOutput)toComplete).getValues(), 0, newVals, modifierOutputIndex, toComplete.getSize());
                                for(int i = 0; i < toComplete.getSize(); i++)
                                {
                                    if(updateNames)
                                    {
                                        String name = featureName + ":" + i;
                                        //System.out.println("added feature name " + name + " - " + toComplete.inputID);
                                        names[modifierOutputIndex] = name;
                                    }
                                }
                            }
                            modifierOutputIndex += toComplete.getSize();
                        }
                    }
                }
            }
            completedIndex++;
        }
        currentValues = newVals;
        if(updateNames)
        {
            outputNames = names;
        }
    }

    public double[] computeAndGetValuesForNewInputs(double[] newInputs, HashMap<String, Feature> features, boolean updateNames) {
        try {
            computeValuesForNewInputs(newInputs, features, updateNames);
        } 
        catch (ConcurrentModificationException ex)
        {
            System.out.println("-------EXCEPTION---------- Caught concurrent mod exception");
        }
        return currentValues;
    }
    
    //EACH FEATURE CAN HAVE MULTIPLE MODIFIERS THAT OUTPUT, EACH OUTPUTTING MODIFIER CAN OUTPUT A VECTOR
    //THE NAMING CONVENTION IS {FEATURE_NAME}:{MODIFIER_INDEX}:{OUTPUT_INDEX}
    private String getFeatureNameForModifierID(int id, HashMap<String, Feature> features)
    {
        for(Feature feature:features.values())
        {
            if(feature instanceof FeatureSingleModifierOutput)
            {
                if(id == ((FeatureSingleModifierOutput) feature).getOutputModifierID())
                {
                    return ((FeatureSingleModifierOutput) feature).name + ":0";
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
