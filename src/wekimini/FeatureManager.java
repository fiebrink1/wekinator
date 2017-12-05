/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import java.util.List;
import java.util.ArrayList;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Attribute;
import wekimini.featureanalysis.WrapperSelector;
import wekimini.learning.SupervisedLearningModel;
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
    private FeatureGroup allFeatures;
    boolean testSetDirty = true;
    private int windowSize = 10;
    
    public FeatureManager()
    {
        featureGroups = new ArrayList<>();

    }
    
    public ArrayList<FeatureGroup> getFeatureGroups()
    {
        return featureGroups;
    }
    
    public String[] getFeatureNames()
    {
        return featureGroups.get(0).getFeatureNames();
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
    
    protected boolean isTestSetDirty(int output)
    {
        return testSetDirty;
    }
    
    protected void setTestSetDirty(int output)
    {
       testSetDirty = true;
    }
    
    protected void didRecalculateTestSetFeatures(int output)
    {
        testSetDirty = false;
    }
    
    protected void addOutputs(int numOutputs, String[] inputNames)
    {
        ArrayList<ModifiedInput> defaultModifiers = new ArrayList();
        ModifiedInput rawInput = new PassThroughVector(inputNames, 0);
        rawInput.inputID = 0;
        rawInput.addToOutput = false;
        defaultModifiers.add(rawInput);
        allFeatures = new FeatureGroup(defaultModifiers);
        for(String feature:allFeatures.getFeatureNames())
        {
            allFeatures.addFeatureForKey(feature);
        }
        
        for(int i = 0; i < numOutputs; i++)
        {
            defaultModifiers = new ArrayList();
            rawInput = new PassThroughVector(inputNames, 0);
            rawInput.inputID = 0;
            rawInput.addToOutput = false;
            defaultModifiers.add(rawInput);
            FeatureGroup fg = new FeatureGroup(defaultModifiers);
            fg.addFeatureForKey("PassThroughAll");
            featureGroups.add(fg);
        }
    }
    
    protected void setAllOutputsDirty()
    {
        for(FeatureGroup modifier:featureGroups)
        {
            modifier.setDirty();
        }
    }
    
    protected Instances getNewInstancesOfLength(int length, int numClasses)
    {
        FastVector ff = new FastVector(length);
        for(int i = 0; i < length; i++)
        {
            ff.addElement(new Attribute("feature" + i));
        }
        
        if(numClasses > 0)
        {
            FastVector classes = new FastVector(numClasses);
            classes.addElement("0"); 
            for (int val = 0; val < numClasses; val++) {
                classes.addElement((new Integer(val + 1)).toString());
            }
            ff.addElement(new Attribute("output", classes));
        }  
        else
        {
            ff.addElement(new Attribute("output"));
        }

        Instances newInst = new Instances("features", ff, 100);
        newInst.setClassIndex(length);
        return newInst;
    }
    
    protected Instances getNewInstances(int output, int numClasses)
    {
        int length = numModifiedInputs(output);
        return getNewInstancesOfLength(length, numClasses);
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
    
    public int addModifierToOutput(ModifiedInput modifier, int output)
    {
        setTestSetDirty(output);
        return featureGroups.get(output).addModifier(modifier);
    }
    
    protected void passThroughInputToOutput(boolean passThrough, int output)
    {
        setTestSetDirty(output);
        if(passThrough)
        {
            featureGroups.get(output).addFeatureForKey("PassThroughAll"); 
        }
        else
        {
            featureGroups.get(output).removeFeatureForKey("PassThroughAll");
        }
    }
    
    protected void removeAllModifiersFromOutput(int output)
    {
        setTestSetDirty(output);
        featureGroups.get(output).removeAllModifiers();
    }
    
    public void removeModifierFromOutput(int modifierID, int output)
    {
        setTestSetDirty(output);
        try {
            featureGroups.get(output).removeModifier(modifierID);      
        } 
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Error trying to remove modifier, index out of bounds");
        }
    }
    
    public int getFeatureWindowSize()
    {
        return windowSize;
    }
    
    public void setFeatureWindowSize(int size)
    {
        windowSize = size;
        int output = 0;
        allFeatures.setFeatureWindowSize(windowSize);
        for(FeatureGroup fg:featureGroups)
        {
            setTestSetDirty(output);
            output++;
            fg.setFeatureWindowSize(windowSize);
        }
    }
    
    //All Features
    protected double[] modifyInputsForAllFeatures(double[] newInputs)
    {        
        return allFeatures.computeAndGetValuesForNewInputs(newInputs);
    }
    
    protected Instances getAllFeaturesNewInstances()
    {
        int length = allFeatures.getOutputDimensionality();
        return getNewInstancesOfLength(length, 0);
    }
    
    protected Instances getAllFeaturesNewInstances(int numClasses)
    {
        int length = allFeatures.getOutputDimensionality();
        return getNewInstancesOfLength(length, numClasses);
    }
    
    protected boolean isAllFeaturesDirty()
    {
        return allFeatures.isDirty();
    }
    
    protected void didRecalculateAllFeatures()
    {
        allFeatures.didRecalculateFeatures();
    }
    
    protected void setAllFeaturesToDirty()
    {
        allFeatures.setDirty();
    }
    
    public FeatureGroup getAllFeaturesGroup()
    {
        return allFeatures;
    }
}
