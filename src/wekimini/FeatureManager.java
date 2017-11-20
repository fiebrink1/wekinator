/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import java.util.List;
import java.util.ArrayList;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Attribute;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.PassThroughVector;
/**
 *
 * @author louismccallum
 */
public class FeatureManager 
{
    //There is one feature group for each path/output
    protected ArrayList<FeatureGroup> featureGroups;
    
    FeatureManager()
    {
        featureGroups = new ArrayList<>();
    }
    
    public ArrayList<FeatureGroup> getFeatureGroups()
    {
        return featureGroups;
    }
    
    protected boolean isDirty(int output)
    {
        return featureGroups.get(output).isDirty();
    }
    
    protected void setDirty(int output)
    {
       featureGroups.get(output).setDirty();
    }
    
    protected void didRecalculateFeatures(int output)
    {
        featureGroups.get(output).didRecalculateFeatures();
    }
    
    protected void addOutputs(int numOutputs, String[] inputNames)
    {
        for(int i = 0; i < numOutputs; i++)
        {
            ArrayList<ModifiedInput> defaultModifiers = new ArrayList();
            ModifiedInput rawInput = new PassThroughVector(inputNames, 0);
            rawInput.inputID = 0;
            defaultModifiers.add(rawInput);
            featureGroups.add(new FeatureGroup(defaultModifiers));
        }
    }
    
    protected void setAllOutputsDirty()
    {
        for(FeatureGroup modifier:featureGroups)
        {
            modifier.setDirty();
        }
    }
    
    protected Instances getNewInstances(int output)
    {
        int length = numModifiedInputs(output);
        FastVector ff = new FastVector(length);
        for(int i = 0; i < length; i++)
        {
            ff.addElement(new Attribute("feature" + i));
        }
        
        ff.addElement(new Attribute("output"));
        return new Instances("features" + output, ff, 100);
    }
    
    protected double[] modifyInputsForOutput(double[] newInputs, int output)
    {        
        return featureGroups.get(output).computeAndGetValuesForNewInputs(newInputs);
    }
    
    protected void resetAllModifiers()
    {
        for(FeatureGroup f:featureGroups)
        {
            for(ModifiedInput m:f.getModifiers())
            {
                m.reset();
            }
        }
    }
    
    protected int numModifiedInputs(int output)
    {
        return featureGroups.get(output).getOutputDimensionality();
    }
    
    protected int addModifierToOutput(ModifiedInput modifier, int output)
    {
        return featureGroups.get(output).addModifier(modifier);
    }
    
    protected void passThroughInputToOutput(boolean passThrough, int output)
    {
        featureGroups.get(output).getModifiers().get(0).addToOutput = passThrough;
    }
    
    protected void removeAllModifiersFromOutput(int output)
    {
        int toRemove = featureGroups.get(output).getNumModifiers();
        for(int i = toRemove-1; i > 0; i--)
        {
            removeModifierFromOutput(i, output);
        }
    }
    
    protected void removeModifierFromOutput(int modifierID, int output)
    {
        try {
            featureGroups.get(output).removeModifier(modifierID);      
        } 
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Error trying to remove modifier, index out of bounds");
        }
    }
}
