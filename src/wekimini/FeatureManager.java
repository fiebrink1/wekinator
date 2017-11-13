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
import wekimini.modifiers.ModifiedInputSingle;
import wekimini.modifiers.ModifiedInputVector;
import wekimini.modifiers.UsesOnlyOriginalInputs;
import wekimini.modifiers.UsesInputsAndOutputs;
import wekimini.modifiers.RawInputs;
/**
 *
 * @author louismccallum
 */
public class FeatureManager 
{
    
    protected ArrayList<FeatureGroup> featureGroups;
    
    FeatureManager()
    {
        featureGroups = new ArrayList<FeatureGroup>();
    }
    
    protected boolean isDirty(int index)
    {
        return featureGroups.get(index).isDirty();
    }
    
    protected void setDirty(int index)
    {
       featureGroups.get(index).setDirty();
    }
    
    protected void didRecalculateFeatures(int index)
    {
        featureGroups.get(index).didRecalculateFeatures();
    }
    
    protected void addOutputs(int numOutputs, String[] inputNames)
    {
        for(int i = 0; i < numOutputs; i++)
        {
            ArrayList<ModifiedInput> defaultModifiers = new ArrayList();
            defaultModifiers.add(new RawInputs(inputNames, 0));
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
    
    protected Instances getNewInstances(int index)
    {
        int length = numModifiedInputs(index);
        FastVector ff = new FastVector(length);
        for(int i = 0; i < length; i++)
        {
            ff.addElement(new Attribute("feature" + i));
        }
        
        ff.addElement(new Attribute("output"));
        return new Instances("features" + index, ff, 100);
    }
    
    protected double[] modifyInputsForOutput(double[] newInputs, int index)
    {        
        return featureGroups.get(index).computeAndGetValuesForNewInputs(newInputs);
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
    
    protected int numModifiedInputs(int index)
    {
        List<ModifiedInput> m = featureGroups.get(index).getModifiers();
        int sum = 0;
        for(int i = 0; i < m.size(); i++)
        {
            sum += m.get(i).getSize();
        }
        return sum;
    }
    
    protected void addModifierToOutput(ModifiedInput modifier, int index)
    {
        featureGroups.get(index).addModifier(modifier);
    }
    
    protected void removeAllModifiersFromOutput(int index)
    {
        int toRemove = featureGroups.get(index).getNumModifiers();
        for(int i = 0; i < toRemove; i++)
        {
            removeModifierFromOutput(0, index);
        }
    }
    
    protected void removeModifierFromOutput(int modifierIndex, int index)
    {
        try {
            featureGroups.get(index).removeModifier(modifierIndex);      
        } 
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Error trying to remove modifier, index out of bounds");
        }
    }
}
