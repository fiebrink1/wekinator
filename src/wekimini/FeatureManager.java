/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import java.io.IOException;
import java.util.ArrayList;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Attribute;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.FeatureCollection;
import wekimini.util.Util;
/**
 *
 * @author louismccallum
 */
public class FeatureManager 
{
    //There is one feature group for each path/output
    protected ArrayList<FeatureCollection> featureCollections;
    private FeatureCollection allFeatures;
    private int windowSize = 15;
    private int bufferSize = 15;
    protected String[] inputNames;
    
    public FeatureManager()
    {
        featureCollections = new ArrayList<>();
    }
    
    public FeatureManager(FeatureManagerData dataFromFile)
    {
        featureCollections = new ArrayList<>();
        addOutputs(dataFromFile.numOutputs,dataFromFile.inputNames);
        windowSize = dataFromFile.windowSize;
        bufferSize = dataFromFile.bufferSize;
        for(int i = 0; i < dataFromFile.added.size(); i++)
        {
            FeatureCollection fc = featureCollections.get(i);
            fc.removeAll();
            ArrayList<String> keys = dataFromFile.added.get(i);
            for(int j = 0; j < keys.size(); j++)
            {
                fc.addFeatureForKey(keys.get(j));
            }
        }
    }
    
    public ArrayList<FeatureCollection> getFeatureGroups()
    {
        return featureCollections;
    }
    
    public String[] getFeatureNames()
    {
        return featureCollections.get(0).getNames();
    }
    
    public void writeToFile(String fileName) throws IOException
    {
        FeatureManagerData data = new FeatureManagerData(this);
        Util.writeToXMLFile(data, "FeatureManagerData", FeatureManagerData.class, fileName);
    }
    
    public static FeatureManagerData readFromFile(String filename) throws IOException {
      FeatureManagerData fmd = (FeatureManagerData) Util.readFromXMLFile("FeatureManagerData", FeatureManagerData.class, filename);
      return fmd;
    }
    
    
        
    protected boolean isDirty(int output)
    {
        return featureCollections.get(output).isDirty(false);
    }
    
    protected void setDirty(int output)
    {
       featureCollections.get(output).setDirty(false);
    }
    
    protected void didRecalculateFeatures(int output)
    {
        featureCollections.get(output).didRecalculateFeatures(false);
    }
    
    protected boolean isTestSetDirty(int output)
    {
        return featureCollections.get(output).isDirty(true);
    }
    
    protected void setTestSetDirty(int output)
    {
       featureCollections.get(output).setDirty(true);
    }
    
    protected void didRecalculateTestSetFeatures(int output)
    {
        featureCollections.get(output).didRecalculateFeatures(true);
    }
    
    protected void addOutputs(int numOutputs, String[] in)
    {
        this.inputNames = in;
        allFeatures = new FeatureCollection(inputNames);
        
        for(String feature:allFeatures.getNames())
        {
            allFeatures.addFeatureForKey(feature);
        }
        
        if(inputNames.length == 6)
        {
            allFeatures.computeAndGetValuesForNewInputs(new double[inputNames.length]);
        }
        
        for(int i = 0; i < numOutputs; i++)
        {   
            featureCollections.add(new FeatureCollection(inputNames));
        }
    }
    
    protected void setAllOutputsDirty()
    {
        int ptr = 0;
        for(FeatureCollection mc:featureCollections)
        {
            mc.setDirty(false);
            mc.setDirty(true);
            ptr++;
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
        return featureCollections.get(output).computeAndGetValuesForNewInputs(newInputs);
    }
    
    protected void resetAllModifiers()
    {
        for(FeatureCollection f:featureCollections)
        {
            f.resetAllModifiers();
        }
    }
    
    protected int numModifiedInputs(int output)
    {
        return featureCollections.get(output).getModifiers().getOutputDimensionality();
    }
    
    public int addModifierToOutput(ModifiedInput modifier, int output)
    {
        setTestSetDirty(output);
        setDirty(output);
        return featureCollections.get(output).getModifiers().addModifier(modifier);
    }
    
    protected void passThroughInputToOutput(boolean passThrough, int output)
    {
        setTestSetDirty(output);
        setDirty(output);
        if(passThrough)
        {
            featureCollections.get(output).addFeatureForKey("PassThroughAll"); 
        }
        else
        {
            featureCollections.get(output).removeFeatureForKey("PassThroughAll");
        }
    }
    
    protected void removeAllModifiersFromOutput(int output)
    {
        setTestSetDirty(output);
        setDirty(output);
        featureCollections.get(output).removeAll();
    }
    
    public void removeModifierFromOutput(int modifierID, int output)
    {
        setTestSetDirty(output);
        setDirty(output);
        try {
            featureCollections.get(output).getModifiers().removeModifier(modifierID);      
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
    
    public int getFeatureBufferSize()
    {
        return bufferSize;
    }

    
    public void setFeatureWindowSize(int wSize, int bSize)
    {
        windowSize = wSize;
        bufferSize = bSize;
        int output = 0;
        allFeatures.setFeatureWindowSize(windowSize, bufferSize);
        for(FeatureCollection mc:featureCollections)
        {
            setTestSetDirty(output);
            output++;
            mc.setFeatureWindowSize(windowSize, bufferSize);
        }
    }
    
    //All Features
    protected synchronized double[] modifyInputsForAllFeatures(double[] newInputs)
    {        
        return allFeatures.computeAndGetValuesForNewInputs(newInputs);
    }
    
    protected void resetAllFeaturesModifiers()
    {
        allFeatures.resetAllModifiers();
    }
    
    protected Instances getAllFeaturesNewInstances()
    {
        int length = allFeatures.getModifiers().getOutputDimensionality();
        return getNewInstancesOfLength(length, 0);
    }
    
    protected Instances getAllFeaturesNewInstances(int numClasses)
    {
        int length = allFeatures.getModifiers().getOutputDimensionality();
        return getNewInstancesOfLength(length, numClasses);
    }
    
    protected boolean isAllFeaturesDirty(boolean testSet)
    {
        return allFeatures.isDirty(testSet);
    }
    
    protected void didRecalculateAllFeatures(boolean testSet)
    {
        allFeatures.didRecalculateFeatures(testSet);
    }
    
    protected void setAllFeaturesToDirty(boolean testSet)
    {
        allFeatures.setDirty(testSet);
    }
    
    public FeatureCollection getAllFeaturesGroup()
    {
        return allFeatures;
    }
}
