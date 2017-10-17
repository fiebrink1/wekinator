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
/**
 *
 * @author louismccallum
 */
public class FeatureManager 
{
    
    private ArrayList<ArrayList<ModifiedInput>> modifiers;
    private ArrayList<Boolean> dirtyFlags;
    
    FeatureManager()
    {
        modifiers = new ArrayList<ArrayList<ModifiedInput>>();
        dirtyFlags = new ArrayList();
    }
    
    protected boolean isDirty(int index)
    {
        return dirtyFlags.get(index);
    }
    
    protected void setDirty(int index)
    {
       dirtyFlags.set(index, true);
    }
    
    protected void didRecalculateFeatures(int index)
    {
        dirtyFlags.set(index, false);
    }
    
    protected Instances getNewInstances(int index)
    {
        int length = numModifiedInputs(index);
        FastVector ff = new FastVector(length);
        for(int i = 0; i < length; i++)
        {
            ff.addElement(new Attribute("feature" + i));
        }
        return new Instances("features" + index, ff, 100);
    }
    
    protected double[] modifyInputsForOutput(double[] newInputs, int index)
    {        
         //Compute output values that
        int currentIndex = 0;
        double[] newValues = new double[numModifiedInputs(index)];
        List<ModifiedInput> m = modifiers.get(index);
        //First do computations with no dependencies other than current inputs
        for (ModifiedInput modifier : m) {
            if (modifier instanceof UsesOnlyOriginalInputs) {
                ((UsesOnlyOriginalInputs)modifier).updateForInputs(newInputs);
                if (modifier instanceof ModifiedInputSingle) {
                    newValues[currentIndex] = ((ModifiedInputSingle)modifier).getValue();
                } else {
                    System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, newValues, currentIndex, modifier.getSize());
                }
            } 
            currentIndex += modifier.getSize();
        }
        
        //Do the rest of the computations now
        for (ModifiedInput modifier : m) {
            currentIndex = 0;
            if (modifier instanceof UsesInputsAndOutputs) {
                ((UsesOnlyOriginalInputs)modifier).updateForInputs(newInputs);
                if (modifier instanceof ModifiedInputSingle) {
                    newValues[currentIndex] = ((ModifiedInputSingle)modifier).getValue();
                } else {
                    System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, newValues, currentIndex, modifier.getSize());
                }
            } 
            currentIndex += modifier.getSize();
        }
        return newValues;
    }
    
    protected int numModifiedInputs(int index)
    {
        List<ModifiedInput> m = modifiers.get(index);
        int sum = 0;
        for(int i = 0; i < m.size(); i++)
        {
            sum += m.get(i).getSize();
        }
        return sum;
    }
}
