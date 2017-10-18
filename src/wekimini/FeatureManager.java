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
    
    protected ArrayList<FeatureGroup> modifiers;
    
    FeatureManager()
    {
        modifiers = new ArrayList<FeatureGroup>();
    }
    
    protected boolean isDirty(int index)
    {
        return modifiers.get(index).isDirty();
    }
    
    protected void setDirty(int index)
    {
       modifiers.get(index).setDirty();
    }
    
    protected void didRecalculateFeatures(int index)
    {
        modifiers.get(index).didRecalculateFeatures();
    }
    
    protected void addOutputs(int numOutputs, String[] inputNames)
    {
        for(int i = 0; i < numOutputs; i++)
        {
            ArrayList<ModifiedInput> defaultModifiers = new ArrayList();
            defaultModifiers.add(new RawInputs(inputNames, 0));
            modifiers.add(new FeatureGroup(defaultModifiers));
        }
    }
    
    protected void setAllOutputsDirty()
    {
        for(FeatureGroup modifier:modifiers)
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
        return new Instances("features" + index, ff, 100);
    }
    
    protected double[] modifyInputsForOutput(double[] newInputs, int index)
    {        
        return modifiers.get(index).computeAndGetValuesForNewInputs(newInputs);
    }
    
    protected int numModifiedInputs(int index)
    {
        List<ModifiedInput> m = modifiers.get(index).getOutputs();
        int sum = 0;
        for(int i = 0; i < m.size(); i++)
        {
            sum += m.get(i).getSize();
        }
        return sum;
    }
}
