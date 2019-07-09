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
import wekimini.kadenze.FeaturnatorLogger;
import wekimini.kadenze.KadenzeLogging;
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
    private ArrayList<FeatureCollection> allFeatures;
    private FeatureCollection plotFeatures;
    private int windowSize = 15;
    private int bufferSize = 15;
    protected String[] inputNames;
    
    public FeatureManager()
    {
        featureCollections = new ArrayList<>();
        allFeatures = new ArrayList<>();
    }
    
    public FeatureManager(FeatureManagerData dataFromFile)
    {
        featureCollections = new ArrayList<>();
        allFeatures = new ArrayList<>();
        windowSize = dataFromFile.windowSize;
        bufferSize = dataFromFile.bufferSize;
        addOutputs(dataFromFile.numOutputs,dataFromFile.inputNames);
        
        for(int i = 0; i < dataFromFile.added.size(); i++)
        {
            FeatureCollection fc = featureCollections.get(i);
            fc.removeAll();
            ArrayList<String> keys = dataFromFile.added.get(i);
            for(int j = 0; j < keys.size(); j++)
            {
                System.out.print(","+keys.get(j));
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
        for(int i = 0; i < numOutputs; i++)
        {
            allFeatures.add(new FeatureCollection(inputNames, windowSize, bufferSize));
            for(String feature:allFeatures.get(i).getNames())
            {
                allFeatures.get(i).addFeatureForKey(feature);
            }
            if(inputNames.length == 6)
            {
                allFeatures.get(i).computeAndGetValuesForNewInputs(new double[inputNames.length], true);
            }
            featureCollections.add(new FeatureCollection(inputNames, windowSize, bufferSize));
        }
        
        plotFeatures = new FeatureCollection(inputNames, windowSize, bufferSize);
        if(inputNames.length == 6)
        {
            plotFeatures.computeAndGetValuesForNewInputs(new double[inputNames.length], true);
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
    
    protected Instances getNewMDSInstances(int numClasses)
    {
        return getNewInstancesOfLength(2, numClasses);
    }
    
    protected double[] modifyInputsForOutput(double[] newInputs, int output, boolean updateNames)
    {        
        return featureCollections.get(output).computeAndGetValuesForNewInputs(newInputs, updateNames);
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
        for(int i = 0; i < featureCollections.size(); i++)
        {
            allFeatures.get(i).removeAll();
            allFeatures.get(i).setFeatureWindowSize(windowSize, bufferSize);
            for(String feature:allFeatures.get(i).getNames())
            {
                allFeatures.get(i).addFeatureForKey(feature);
            }
            featureCollections.get(i).setFeatureWindowSize(windowSize, bufferSize);
        }
        plotFeatures.setFeatureWindowSize(windowSize, bufferSize);
    }
    
    //All Features
    protected double[] modifyInputsForAllFeatures(int output, double[] newInputs, boolean updateNames)
    {    
        return allFeatures.get(output).computeAndGetValuesForNewInputs(newInputs, updateNames);
    }
    
    protected void resetAllFeaturesModifiers(int output)
    {
        allFeatures.get(output).resetAllModifiers();
    }
    
    protected Instances getAllFeaturesNewInstances()
    {
        int length = allFeatures.get(0).getModifiers().getOutputDimensionality();
        System.out.println("getting new instances " + length);
        return getNewInstancesOfLength(length, 0);
    }
    
    protected Instances getAllFeaturesNewInstances(int numClasses)
    {
        int length = allFeatures.get(0).getModifiers().getOutputDimensionality();
        return getNewInstancesOfLength(length, numClasses);
    }
    
    protected boolean isAllFeaturesDirty(int output, boolean testSet)
    {
        return allFeatures.get(output).isDirty(testSet);
    }
    
    protected void didRecalculateAllFeatures(int output, boolean testSet)
    {
        allFeatures.get(output).didRecalculateFeatures(testSet);
    }
    
    protected void setAllFeaturesToDirty(int output, boolean testSet)
    {
        allFeatures.get(output).setDirty(testSet);
    }
    
    public FeatureCollection getAllFeatures(int output)
    {
        return allFeatures.get(output);
    }
    
    //Plot Features
    protected double[] modifyInputsForPlotFeatures(double[] newInputs, boolean updateNames)
    {    
        return plotFeatures.computeAndGetValuesForNewInputs(newInputs, updateNames);
    }
    
    protected void resetPlotFeaturesModifiers()
    {
        plotFeatures.resetAllModifiers();
    }
    
    public FeatureCollection getPlotFeatures()
    {
        return plotFeatures;
    }
}
