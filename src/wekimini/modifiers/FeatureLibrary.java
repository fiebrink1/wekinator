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

/**
 *
 * @author louismccallum
 */

class MaxFFT extends Feature
{

    public MaxFFT(String name) {
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
        
        ModifiedInput max = new MaxInputs("max",0,0);
        max.addRequiredInput(fftID);
        int maxID = fg.addModifier(max);
        
        ArrayList<Integer> ids = new ArrayList();
        ids.add(maxID);
        ids.add(fftID);
        return ids;
    }
}

class MinFFT extends Feature
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
        return ids;
    }
}

class PassThrough extends Feature
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
        int[] i1 = {0};
        library.add(new PassThrough("PassThroughFirst",i1));
        int[] i2 = {0,1,2};
        library.add(new PassThrough("JustAccelerometer",i2));
        library.add(new MaxFFT("MaxFFT"));
        library.add(new MinFFT("MinFFT"));
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
}
