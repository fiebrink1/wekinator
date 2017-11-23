/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import wekimini.FeatureGroup;
import wekimini.modifiers.WindowedOperation.Operation;
/**
 *
 * @author louismccallum
 */

class MaxFFT extends FeatureSingleModifierOutput
{
    
    int totalBins;
    int[] bins;
    
    public MaxFFT(String name, int totalBins, int[] selectedBins) {
        super(name);
        this.bins = selectedBins;
        this.totalBins = totalBins;
    }

    @Override
    public List<Integer> addFeature(FeatureGroup fg)
    {
        FFTModifier fft = new FFTModifier("fft",0,totalBins,bins);
        fft.addToOutput= false;
        fft.addRequiredInput(0);
        int fftID = fg.addModifier(fft);
        
        ModifiedInput max = new MaxInputs("max",0,0);
        max.addRequiredInput(fftID);
        int maxID = fg.addModifier(max);
        
        ArrayList<Integer> ids = new ArrayList();
        ids.add(maxID);
        ids.add(fftID);
        
        setOutputModifierID(maxID);
        
        return ids;
    }
}

class MinFFT extends FeatureSingleModifierOutput
{
    public MinFFT(String name) {
        super(name);
    }

    @Override
    public List<Integer> addFeature(FeatureGroup fg)
    {
        int [] bins = {1,2,3,4,5};
        FFTModifier fft = new FFTModifier("fft",0,64,bins);
        fft.addToOutput= false;
        fft.addRequiredInput(0);
        int fftID = fg.addModifier(fft);
        
        ModifiedInput min = new MinInputs("min",0,0);
        min.addRequiredInput(fftID);
        int minID = fg.addModifier(min);
        
        ArrayList<Integer> ids = new ArrayList();
        ids.add(minID);
        ids.add(fftID);
        
        setOutputModifierID(minID);
        
        return ids;
    }
}

class WindowedFeature extends FeatureSingleModifierOutput
{
    
    ModifiedInput window;
    
    public WindowedFeature(String name, Operation op, int inputIndex, int windowSize) {
        super(name);
        this.window = new WindowedOperation("input-1",op,inputIndex,windowSize,0);
        window.addRequiredInput(inputIndex);
    }
    
    @Override
    public List<Integer> addFeature(FeatureGroup fg)
    {
        ArrayList<Integer> ids = new ArrayList();
        int id1 = fg.addModifier(window);
        ids.add(id1);
        setOutputModifierID(id1);
        return ids;
    }
}

class PassThrough extends FeatureMultipleModifierOutput
{
    int[] indexes;
    
    public PassThrough(String name, int[] indexes) {
        super(name);
        this.indexes = indexes;
    }

    @Override
    public List<Integer> addFeature(FeatureGroup fg)
    {
        ArrayList<Integer> ids = new ArrayList();
        for(int index:indexes)
        {
            PassThroughSingle modifier = new PassThroughSingle(Integer.toString(index),index,0);
            modifier.addRequiredInput(0);
            int id1 = fg.addModifier(modifier);
            ids.add(id1);
        }
        setOutputModifierIDs(ids.toArray(new Integer[ids.size()]));
        return ids;
    }
}

class PassThroughAll extends FeatureSingleModifierOutput
{    
    public PassThroughAll(String name) {
        super(name);
    }

    @Override
    public List<Integer> addFeature(FeatureGroup fg)
    {
        ArrayList<Integer> ids = new ArrayList();
        String[] names = ((PassThroughVector)fg.getModifier(0)).names;
        PassThroughVector modifier = new PassThroughVector(names, 0);
        modifier.addToOutput = true;
        modifier.addRequiredInput(0);
        int id1 = fg.addModifier(modifier);
        ids.add(id1);
        setOutputModifierID(id1);
        return ids;
    }
}

public final class FeatureLibrary 
{
    private final Map<String, List<Integer>> added = new HashMap<>();
    private final List<Feature> library = new ArrayList();
    private String[] names;
    private FeatureGroup fg;
    
    private FeatureLibrary(){};
    
    public FeatureLibrary(FeatureGroup fg)
    {
        library.add(new PassThroughAll("PassThroughAll"));
        int[] i2 = {0,1,2};
        library.add(new PassThrough("AllAcc",i2));
        library.add(new MaxFFT("MaxFFT",64,new int[]{0,8,16,24,36,48,60}));
        library.add(new MinFFT("MinFFT"));
        int[] i3 = {3,4,5};
        library.add(new PassThrough("AllGyro",i3));
        library.add(new WindowedFeature("MeanAccX",new AverageWindowOperation(),0,10));
        library.add(new WindowedFeature("MeanAccY",new AverageWindowOperation(),1,10));
        library.add(new WindowedFeature("MeanAccZ",new AverageWindowOperation(),2,10));
        library.add(new WindowedFeature("MeanGyroX",new AverageWindowOperation(),3,10));
        library.add(new WindowedFeature("MeanGyroY",new AverageWindowOperation(),4,10));
        library.add(new WindowedFeature("MeanGyroZ",new AverageWindowOperation(),5,10));
        library.add(new WindowedFeature("StdDevAccX",new StdDevWindowOperation(),0,10));
        library.add(new WindowedFeature("StdDevAccY",new StdDevWindowOperation(),1,10));
        library.add(new WindowedFeature("StdDevAccZ",new StdDevWindowOperation(),2,10));
        library.add(new WindowedFeature("StdDevGyroX",new StdDevWindowOperation(),3,10));
        library.add(new WindowedFeature("StdDevGyroY",new StdDevWindowOperation(),4,10));
        library.add(new WindowedFeature("StdDevGyroZ",new StdDevWindowOperation(),5,10));
        names = new String[library.size()];
        int ptr = 0;
        for(Feature feature:library)
        {
            names[ptr] = feature.name;
            ptr++;
        }
        this.fg = fg;
    }
    
    public List<Feature> getLibrary()
    {
        return library;
    }
    
    public String[] getNames()
    {
        return names;
    }
    
    
    public boolean[] getConnections()
    {
        boolean[] connections = new boolean[library.size()];
        int ptr = 0;
        for(Feature feature : library)
        {
            connections[ptr] = added.containsKey(feature.name); 
            ptr++;
        }
        return connections;
    }
            
    public void addFeatureForKey(String key)
    {
        if(added.containsKey(key))
        {
            return;
        }
        
        for(Feature feature:library)
        {
            if(key.equals(feature.name))
            {
                added.put(key,feature.addFeature(fg));
                break;
            }
        }
    }
    
    public void removeFeatureForKey(String key)
    {
        if(!added.containsKey(key))
        {
            return;
        }
        
        for(Integer id:added.get(key))
        {
            fg.removeModifier(id);
        }
        fg.removeOrphanedModifiers();
        
        added.remove(key);
    }
    
    public void clearAdded()
    {
        added.clear();
    }
    
    public String getFeatureNameForModifierID(int id)
    {
        for(Feature feature:library)
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
}
