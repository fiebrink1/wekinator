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
import java.util.Set;
import wekimini.FeatureGroup;
import wekimini.modifiers.WindowedOperation.Operation;
/**
 *
 * @author louismccallum
 */

class FFTFeature extends FeatureSingleModifierOutput
{
    
    int totalBins;
    int[] bins;
    int index;
    
    public FFTFeature(String name, int index, int totalBins, int[] selectedBins) {
        super(name);
        this.bins = selectedBins;
        this.totalBins = totalBins;
        this.index = index;
    }

    @Override
    public void addFeature(FeatureGroup fg)
    {
        FFTModifier fft = new FFTModifier("fft", index, totalBins, bins);
        fft.addToOutput= false;
        fft.addRequiredModifierID(0);
        int fftID = fg.addModifier(fft);
        
        ids.add(fftID);
        
        setOutputModifierID(fftID);
    }
}

class MaxFFT extends FeatureSingleModifierOutput
{
    
    int totalBins;
    int[] bins;
    int index;
    
    public MaxFFT(String name, int index, int totalBins, int[] selectedBins) {
        super(name);
        this.bins = selectedBins;
        this.totalBins = totalBins;
        this.index = index;
    }

    @Override
    public void addFeature(FeatureGroup fg)
    {
        FFTModifier fft = new FFTModifier("fft", index, totalBins, bins);
        fft.addToOutput= false;
        fft.addRequiredModifierID(0);
        int fftID = fg.addModifier(fft);
        
        ModifiedInput max = new MaxInputs("max",0);
        max.addRequiredModifierID(fftID);
        int maxID = fg.addModifier(max);
        
        ids.add(maxID);
        ids.add(fftID);
        
        setOutputModifierID(maxID);
    }
}

class MinFFT extends FeatureSingleModifierOutput
{
    int totalBins;
    int[] bins;
    int index;
    
    public MinFFT(String name, int index, int totalBins, int[] selectedBins) {
        super(name);
        this.bins = selectedBins;
        this.totalBins = totalBins;
        this.index = index;
    }

    @Override
    public void addFeature(FeatureGroup fg)
    {
        FFTModifier fft = new FFTModifier("fft", index, totalBins, bins);
        fft.addToOutput= false;
        fft.addRequiredModifierID(0);
        int fftID = fg.addModifier(fft);
        
        ModifiedInput min = new MinInputs("max",0);
        min.addRequiredModifierID(fftID);
        int minID = fg.addModifier(min);
        
        ids.add(minID);
        ids.add(fftID);
        
        setOutputModifierID(minID);
        
    }
}

class WindowedFeature extends FeatureSingleModifierOutput
{
    
    ModifiedInput window;
    
    public WindowedFeature(String name, Operation op, int inputIndex, int windowSize) {
        super(name);
        this.window = new WindowedOperation("input-1",op,inputIndex,windowSize,0);
        window.addRequiredModifierID(0);
    }
    
    @Override
    public void addFeature(FeatureGroup fg)
    {
        int id1 = fg.addModifier(window);
        ids.add(id1);
        setOutputModifierID(id1);
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
    public void addFeature(FeatureGroup fg)
    {
        for(int index:indexes)
        {
            PassThroughSingle modifier = new PassThroughSingle(Integer.toString(index),index,0);
            modifier.addRequiredModifierID(0);
            int id1 = fg.addModifier(modifier);
            ids.add(id1);
        }
        setOutputModifierIDs(ids.toArray(new Integer[ids.size()]));
    }
}

class PassThroughAll extends FeatureSingleModifierOutput
{    
    public PassThroughAll(String name) {
        super(name);
    }

    @Override
    public void addFeature(FeatureGroup fg)
    {
        String[] names = ((PassThroughVector)fg.getModifier(0)).names;
        PassThroughVector modifier = new PassThroughVector(names, 0);
        modifier.addToOutput = true;
        modifier.addRequiredModifierID(0);
        int id1 = fg.addModifier(modifier);
        ids.add(id1);
        setOutputModifierID(id1);
    }
}

class BufferFeature extends FeatureSingleModifierOutput
{  
    
    private int index;
    private int windowSize;
    
    public BufferFeature(String name, int index, int windowSize) {
        super(name);
        this.index = index;
        this.windowSize = windowSize;
    }

    @Override
    public void addFeature(FeatureGroup fg)
    {
        BufferedInput modifier = new BufferedInput(name, index, windowSize, 0);
        modifier.addToOutput = true;
        modifier.addRequiredModifierID(0);
        int id1 = fg.addModifier(modifier);
        ids.add(id1);
        setOutputModifierID(id1);
    }
}

class MagnitudeFODFeature extends FeatureSingleModifierOutput
{
    int[] inputs;
    int windowSize;
    
    public MagnitudeFODFeature(String name, int[] inputs, int windowSize)
    {
        super(name);
        this.inputs = inputs;
        this.windowSize = windowSize;
    }
    
    @Override
    public void addFeature(FeatureGroup fg)
    {
        
        FirstOrderDifference fod1 = new FirstOrderDifference("FOD-1",inputs[0],0);
        fod1.addRequiredModifierID(0);
        int fod1ID = fg.addModifier(fod1);
                    
        FirstOrderDifference fod2 = new FirstOrderDifference("FOD-2",inputs[1],0);
        fod2.addRequiredModifierID(0);
        int fod2ID = fg.addModifier(fod2);
         
        FirstOrderDifference fod3 = new FirstOrderDifference("FOD-3",inputs[2],0);
        fod3.addRequiredModifierID(0);
        int fod3ID = fg.addModifier(fod3);
        
        ModifiedInput raw1 = new PassThroughSingle("raw-1",0,0);
        raw1.addToOutput = false;
        raw1.addRequiredModifierID(fod2ID);
        int rawID1 = fg.addModifier(raw1);
        
        ModifiedInput raw2 = new PassThroughSingle("raw-2",0,0);
        raw2.addToOutput = false;
        raw2.addRequiredModifierID(fod3ID);
        int rawID2 = fg.addModifier(raw2);
        
        ModifiedInput raw3 = new PassThroughSingle("raw-3",0,0);
        raw3.addToOutput = false;
        raw3.addRequiredModifierID(0);
        int rawID3 = fg.addModifier(raw3);
        
        ModifiedInput mag = new MultipleInputWindowedOperation("input-1",new ThreeDimensionalMagnitude(),windowSize,0);
        mag.addRequiredModifierID(rawID1);
        mag.addRequiredModifierID(rawID2);
        mag.addRequiredModifierID(rawID3);
        int id1 = fg.addModifier(mag);
        ids.add(id1);
        setOutputModifierID(id1);
    }
}

class MagnitudeFeature extends FeatureSingleModifierOutput
{
    int[] inputs;
    int windowSize;
    
    public MagnitudeFeature(String name, int[] inputs, int windowSize)
    {
        super(name);
        this.inputs = inputs;
        this.windowSize = windowSize;
    }
    
    @Override
    public void addFeature(FeatureGroup fg)
    {
        
        ModifiedInput raw1 = new PassThroughSingle("raw-1",inputs[0],0);
        raw1.addToOutput = false;
        raw1.addRequiredModifierID(0);
        int rawID1 = fg.addModifier(raw1);
        
        ModifiedInput raw2 = new PassThroughSingle("raw-2",inputs[1],0);
        raw2.addToOutput = false;
        raw2.addRequiredModifierID(0);
        int rawID2 = fg.addModifier(raw2);
        
        ModifiedInput raw3 = new PassThroughSingle("raw-3",inputs[2],0);
        raw3.addToOutput = false;
        raw3.addRequiredModifierID(0);
        int rawID3 = fg.addModifier(raw3);
        
        ModifiedInput mag = new MultipleInputWindowedOperation("input-1",new ThreeDimensionalMagnitude(),windowSize,0);
        mag.addRequiredModifierID(rawID1);
        mag.addRequiredModifierID(rawID2);
        mag.addRequiredModifierID(rawID3);
        int id1 = fg.addModifier(mag);
        ids.add(id1);
        setOutputModifierID(id1);
    }
}

class FODRaw extends FeatureSingleModifierOutput
{
    int index;
    
    public FODRaw(String name, int index)
    {
        super(name);
        this.index = index;
    }
    
    @Override
    public void addFeature(FeatureGroup fg)
    {
        FirstOrderDifference fod = new FirstOrderDifference("FOD",index,0);
        fod.addRequiredModifierID(0);
        int id1 = fg.addModifier(fod);
        ids.add(id1);
        setOutputModifierID(id1);
    }
}

class WindowedFOD extends FeatureSingleModifierOutput
{
    Operation op;
    int windowSize;
    int index;
    
    public WindowedFOD(String name, Operation op, int inputIndex, int windowSize) {
        super(name);
        this.op = op;
        this.index = inputIndex;
        this.windowSize = windowSize;
    }
    
    @Override
    public void addFeature(FeatureGroup fg)
    {
        
        FirstOrderDifference fod = new FirstOrderDifference("FOD", index, 0);
        fod.addRequiredModifierID(0);
        fod.addToOutput = false;
        int fodID = fg.addModifier(fod);
        ids.add(fodID);
        
        ModifiedInput window = new WindowedOperation("input-1",op, 0, windowSize, 0);
        window.addRequiredModifierID(fodID);
        int windowID = fg.addModifier(window);
        ids.add(windowID);

        setOutputModifierID(windowID);
    }
}


class CorrelateFeature extends FeatureSingleModifierOutput
{
    int windowSize;
    int[] indexes;
    
    public CorrelateFeature(String name, int indexes[], int windowSize) {
        super(name);
        this.indexes = indexes;
        this.windowSize = windowSize;
    }
    
    @Override
    public void addFeature(FeatureGroup fg)
    {
        
        ModifiedInput raw1 = new PassThroughSingle("raw-1",indexes[0],0);
        raw1.addToOutput = false;
        raw1.addRequiredModifierID(0);
        int rawID1 = fg.addModifier(raw1);
        
        ModifiedInput raw2 = new PassThroughSingle("raw-2",indexes[1],0);
        raw2.addToOutput = false;
        raw2.addRequiredModifierID(0);
        int rawID2 = fg.addModifier(raw2);
        
        ModifiedInput correlate = new MultipleInputWindowedOperation("input-1",new CorrelateWindowOperation(),windowSize,0);
        correlate.addRequiredModifierID(rawID1);
        correlate.addRequiredModifierID(rawID2);
        int correlateID = fg.addModifier(correlate);

        setOutputModifierID(correlateID);
    }
}

public final class FeatureLibrary 
{
    private final HashMap<String, Feature> added = new HashMap<>();
    private final List<Feature> library = new ArrayList();
    private String[] names;
    private FeatureGroup fg;
    private final int ACCX = 0;
    private final int ACCY = 1;
    private final int ACCZ = 2;
    private final int GYROX = 3;
    private final int GYROY = 4;
    private final int GYROZ = 5;
    
    private FeatureLibrary(){};
    
    public FeatureLibrary(FeatureGroup fg)
    {
        this.fg = fg;
        initLibrary(10);
    }
    
    public void initLibrary(int windowSize)
    {
        
        library.clear();
        
        library.add(new PassThroughAll("PassThroughAll"));
        library.add(new PassThrough("AllAcc",new int[]{ACCX,ACCY,ACCZ}));
        library.add(new PassThrough("AllGyro",new int[]{GYROX,GYROY,GYROZ}));
        library.add(new WindowedFeature("MeanAccX",new AverageWindowOperation(),ACCX,windowSize));
        library.add(new WindowedFeature("MeanAccY",new AverageWindowOperation(),ACCY,windowSize));
        library.add(new WindowedFeature("MeanAccZ",new AverageWindowOperation(),ACCZ,windowSize));
        library.add(new WindowedFeature("MeanGyroX",new AverageWindowOperation(),GYROX,windowSize));
        library.add(new WindowedFeature("MeanGyroY",new AverageWindowOperation(),GYROY,windowSize));
        library.add(new WindowedFeature("MeanGyroZ",new AverageWindowOperation(),GYROZ,windowSize));
        library.add(new WindowedFeature("StdDevAccX",new StdDevWindowOperation(),ACCX,windowSize));
        library.add(new WindowedFeature("StdDevAccY",new StdDevWindowOperation(),ACCY,windowSize));
        library.add(new WindowedFeature("StdDevAccZ",new StdDevWindowOperation(),ACCZ,windowSize));
        library.add(new WindowedFeature("StdDevGyroX",new StdDevWindowOperation(),GYROX,windowSize));
        library.add(new WindowedFeature("StdDevGyroY",new StdDevWindowOperation(),GYROY,windowSize));
        library.add(new WindowedFeature("StdDevGyroZ",new StdDevWindowOperation(),GYROZ,windowSize));
        library.add(new WindowedFeature("MaxAccX",new MaxWindowOperation(),ACCX,windowSize));
        library.add(new WindowedFeature("MaxAccY",new MaxWindowOperation(),ACCY,windowSize));
        library.add(new WindowedFeature("MaxAccZ",new MaxWindowOperation(),ACCZ,windowSize));
        library.add(new WindowedFeature("MaxGyroX",new MaxWindowOperation(),GYROX,windowSize));
        library.add(new WindowedFeature("MaxGyroY",new MaxWindowOperation(),GYROY,windowSize));
        library.add(new WindowedFeature("MaxGyroZ",new MaxWindowOperation(),GYROZ,windowSize));
        library.add(new WindowedFeature("EnergyAccX",new EnergyWindowOperation(),ACCX,windowSize));
        library.add(new WindowedFeature("EnergyAccY",new EnergyWindowOperation(),ACCY,windowSize));
        library.add(new WindowedFeature("EnergyAccZ",new EnergyWindowOperation(),ACCZ,windowSize));
        library.add(new WindowedFeature("EnergyGyroX",new EnergyWindowOperation(),GYROX,windowSize));
        library.add(new WindowedFeature("EnergyGyroY",new EnergyWindowOperation(),GYROY,windowSize));
        library.add(new WindowedFeature("EnergyGyroZ",new EnergyWindowOperation(),GYROZ,windowSize));
        library.add(new BufferFeature("BufferAccX", ACCX, windowSize));
        library.add(new BufferFeature("BufferAccY", ACCY, windowSize));
        library.add(new BufferFeature("BufferAccZ", ACCZ, windowSize));
        library.add(new BufferFeature("BufferGyroX", GYROX, windowSize));
        library.add(new BufferFeature("BufferGyroY", GYROY, windowSize));
        library.add(new BufferFeature("BufferGyroZ", GYROZ, windowSize));
        library.add(new MagnitudeFeature("MagAcc", new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new MagnitudeFeature("MagGyro", new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new MagnitudeFODFeature("MagFODAcc", new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new MagnitudeFODFeature("MagFODGyro", new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new FODRaw("AccXFOD", ACCX));
        library.add(new FODRaw("AccYFOD", ACCY));
        library.add(new FODRaw("AccZFOD", ACCZ));
        library.add(new FODRaw("GyroXFOD", GYROX));
        library.add(new FODRaw("GyroYFOD", GYROY));
        library.add(new FODRaw("GyroZFOD", GYROZ));
        library.add(new WindowedFOD("MeanFODAccX",new AverageWindowOperation(),ACCX,windowSize));
        library.add(new WindowedFOD("MeanFODAccY",new AverageWindowOperation(),ACCY,windowSize));
        library.add(new WindowedFOD("MeanFODAccZ",new AverageWindowOperation(),ACCZ,windowSize));
        library.add(new WindowedFOD("MeanFODGyroX",new AverageWindowOperation(),GYROX,windowSize));
        library.add(new WindowedFOD("MeanFODGyroY",new AverageWindowOperation(),GYROY,windowSize));
        library.add(new WindowedFOD("MeanFODGyroZ",new AverageWindowOperation(),GYROZ,windowSize));
        library.add(new WindowedFOD("StdDevFODAccX",new StdDevWindowOperation(),ACCX,windowSize));
        library.add(new WindowedFOD("StdDevFODAccY",new StdDevWindowOperation(),ACCY,windowSize));
        library.add(new WindowedFOD("StdDevFODAccZ",new StdDevWindowOperation(),ACCZ,windowSize));
        library.add(new WindowedFOD("StdDevFODGyroX",new StdDevWindowOperation(),GYROX,windowSize));
        library.add(new WindowedFOD("StdDevFODGyroY",new StdDevWindowOperation(),GYROY,windowSize));
        library.add(new WindowedFOD("StdDevFODGyroZ",new StdDevWindowOperation(),GYROZ,windowSize));
        library.add(new CorrelateFeature("CorrelateAccXY",new int[]{ACCX,ACCY},windowSize));
        library.add(new CorrelateFeature("CorrelateAccXZ",new int[]{ACCX,ACCZ},windowSize));
        library.add(new CorrelateFeature("CorrelateAccYZ",new int[]{ACCY,ACCZ},windowSize));
        library.add(new CorrelateFeature("CorrelateGyroXY",new int[]{GYROX,GYROY},windowSize));
        library.add(new CorrelateFeature("CorrelateGyroXZ",new int[]{GYROX,GYROZ},windowSize));
        library.add(new CorrelateFeature("CorrelateGyroYZ",new int[]{GYROY,GYROZ},windowSize));
        library.add(new FFTFeature("FFTAccX7Bins", ACCX, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new FFTFeature("FFTAccY7Bins", ACCY, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new FFTFeature("FFTAccZ7Bins", ACCZ, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new FFTFeature("FFTGyroX7Bins", GYROX, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new FFTFeature("FFTGyroY7Bins", GYROY, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new FFTFeature("FFTGyroZ7Bins", GYROZ, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MaxFFT("MaxFFTAccX", ACCX, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MaxFFT("MaxFFTAccY", ACCY, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MaxFFT("MaxFFTAccZ", ACCZ, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MaxFFT("MaxFFTGyroX", GYROX, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MaxFFT("MaxFFTGyroY", GYROY, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MaxFFT("MaxFFTGyroZ", GYROZ, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MinFFT("MinFFTAccX", ACCX, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MinFFT("MinFFTAccY", ACCY, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MinFFT("MinFFTAccZ", ACCZ, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MinFFT("MinFFTGyroX", GYROX, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MinFFT("MinFFTGyroY", GYROY, 128, new int[]{0,8,16,24,36,48,60}));
        library.add(new MinFFT("MinFFTGyroZ", GYROZ, 128, new int[]{0,8,16,24,36,48,60}));
        
        names = new String[library.size()];
        int ptr = 0;
        for(Feature feature:library)
        {
            names[ptr] = feature.name;
            ptr++;
        }
        
        Set<String> keys = added.keySet();
        for(String key:keys)
        {
            removeFeatureForKey(key);
            addFeatureForKey(key);
        }
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
                feature.addFeature(fg);
                added.put(key,feature);
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
        
        Feature f = added.get(key);
        
        for(Integer id:f.ids)
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
        for(Feature feature:added.values())
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
