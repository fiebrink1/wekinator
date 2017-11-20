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
public class FeatureLibrary 
{
    Map<String, List<Integer>> added = new HashMap<>();
    
    public void addFeatureForKey(FeatureGroup fg, String key)
    {
        if(added.containsKey(key))
        {
            return;
        }
        if(key.equals("PassThroughFirst"))
        {   
            added.put(key,addPassThrough(fg));
        }
        if(key.equals("JustAccelerometer"))
        {   
            added.put(key,addJustAccelerometer(fg));
        }
        if(key.equals("MaxFFT"))
        {   
            added.put(key,addMaxFFT(fg));
        }
        if(key.equals("MinFFT"))
        {   
            added.put(key,addMinFFT(fg));
        }
    }
    
    public void removeFeatureForKey(FeatureGroup fg, String key)
    {
        for(Integer id:added.get(key))
        {
            fg.removeModifier(id);
        }
        fg.removeOrphanedModifiers();
    }
    
    private List<Integer> addPassThrough(FeatureGroup fg)
    {
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredInput(0);
        int id1 = fg.addModifier(modifier);
        ArrayList<Integer> ids = new ArrayList();
        ids.add(id1);
        return ids;
    }
    
    private List<Integer> addJustAccelerometer(FeatureGroup fg)
    {
        PassThroughSingle x = new PassThroughSingle("x",0,0);
        x.addRequiredInput(0);
        int id1 = fg.addModifier(x);
        PassThroughSingle y = new PassThroughSingle("x",1,0);
        y.addRequiredInput(0);
        int id2 = fg.addModifier(y);
        PassThroughSingle z = new PassThroughSingle("z",2,0);
        z.addRequiredInput(0);
        int id3 = fg.addModifier(z);
        ArrayList<Integer> ids = new ArrayList();
        ids.add(id1);
        ids.add(id2);
        ids.add(id3);
        return ids;
    }
    
    private List<Integer> addMaxFFT(FeatureGroup fg)
    {
        int [] bins = {1,2,3,4,5};
        FFTModifier fft = new FFTModifier("fft",0,64,bins);
        fft.addToOutput= false;
        fft.addRequiredInput(0);
        int fftID = fg.addModifier(fft);
        
        ModifiedInput windowMax = new WindowedOperation("input-1",new MaxWindowOperation(),0,10,0);
        windowMax.addRequiredInput(fftID);
        int maxID = fg.addModifier(windowMax);
        
        ArrayList<Integer> ids = new ArrayList();
        ids.add(maxID);
        ids.add(fftID);
        return ids;
    }
    
    private List<Integer> addMinFFT(FeatureGroup fg)
    {
        int [] bins = {1,2,3,4,5};
        FFTModifier fft = new FFTModifier("fft",0,64,bins);
        fft.addToOutput= false;
        fft.addRequiredInput(0);
        int fftID = fg.addModifier(fft);
        
        ModifiedInput windowMin = new WindowedOperation("input-1",new MinWindowOperation(),0,10,0);
        windowMin.addRequiredInput(fftID);
        int minID = fg.addModifier(windowMin);
        
        ArrayList<Integer> ids = new ArrayList();
        ids.add(minID);
        ids.add(fftID);
        return ids;
    }
}
