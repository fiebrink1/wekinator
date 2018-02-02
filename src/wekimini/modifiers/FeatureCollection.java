/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import wekimini.modifiers.Feature.InputDiagram;
import wekimini.modifiers.WindowedOperation.Operation;
/**
 *
 * @author louismccallum
 */

public final class FeatureCollection 
{
    private final HashMap<String, Feature> added = new HashMap<>();
    private final List<Feature> library = new ArrayList();
    private String[] names;
    private ModifierCollection modifiers;
    private boolean dirtyFlag = true;
    private boolean testSetDirtyFlag = true;
    private String[] tags;
    static final int ACCX = 0;
    static final int ACCY = 1;
    static final int ACCZ = 2;
    static final int GYROX = 3;
    static final int GYROY = 4;
    static final int GYROZ = 5;
       
    private FeatureCollection(){}
    
    public FeatureCollection(String[] inputNames)
    {
        initLibrary(10, 10);
        modifiers = new ModifierCollection(inputNames);
        addFeatureForKey("PassThroughAll");
    }
    
    public void initLibrary(int windowSize, int bufferSize)
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
        library.add(new BufferFeature("BufferAccX", ACCX, bufferSize));
        library.add(new BufferFeature("BufferAccY", ACCY, bufferSize));
        library.add(new BufferFeature("BufferAccZ", ACCZ, bufferSize));
        library.add(new BufferFeature("BufferGyroX", GYROX, bufferSize));
        library.add(new BufferFeature("BufferGyroY", GYROY, bufferSize));
        library.add(new BufferFeature("BufferGyroZ", GYROZ, bufferSize));
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
        
        HashSet<String> keys = new HashSet(added.keySet());
        for(String key:keys)
        {
            removeFeatureForKey(key);
        }

        for(String key:keys)
        {
            addFeatureForKey(key);
        }
        
        updateTags();
        
    }
    
    public ModifierCollection getModifiers()
    {
        return modifiers;
    }
    
    public List<Feature> getLibrary()
    {
        return library;
    }
    
    public String[] getNames()
    {
        return names;
    }
    
    public Feature getFeatureForKey(String key) throws NoSuchElementException
    {
        for(Feature f:library)
        {
            if(f.name.toLowerCase().equals(key.toLowerCase()))
            {
                return f;
            }
        }
        throw new NoSuchElementException();
    }
    
    public Feature[] getFeaturesForKeyword(String search)
    {
        ArrayList<Feature> features = new ArrayList();
        for(Feature f:library)
        {
            if(!features.contains(f))
            {
                if(f.name.toLowerCase().contains(search.toLowerCase()))
                {
                    features.add(f);
                }
                else
                {
                    for(String matchTag:f.tags)
                    {
                        if(matchTag.toLowerCase().contains(search.toLowerCase()))
                        {
                            features.add(f);
                            break;
                        }
                    }
                }
            }
        }
        Feature[] f = new Feature[features.size()];
        f = features.toArray(f);
        return f;
    }
    
    public Feature[] getFeaturesForTags(String[] searchTags)
    {
        ArrayList<Feature> features = new ArrayList();
        for(Feature f:library)
        {
            if(!features.contains(f))
            {
                int matches = 0;
                for(String searchTag:searchTags)
                {
                    boolean matched = false;
                    for(String matchTag:f.tags)
                    {
                        if(matchTag.toLowerCase().contains(searchTag.toLowerCase()))
                        {
                            matches++;
                        }
                    }

                }
                if(matches == searchTags.length)
                {
                    features.add(f);
                }
            }
        }
        Feature[] f = new Feature[features.size()];
        f = features.toArray(f);
        return f;
    }
    
    public String[] getTags()
    {
        return tags;
    }
    
    public void updateTags()
    {
        ArrayList<String> newTags = new ArrayList();
        for(Feature f:library)
        {
            for(String tag:f.tags)
            {
                if(!newTags.contains(tag))
                {
                    newTags.add(tag);
                }
            }
        }
        tags = new String[newTags.size()];
        tags = newTags.toArray(tags);
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
    
    public double[] computeAndGetValuesForNewInputs(double[] newInputs)
    {
        return modifiers.computeAndGetValuesForNewInputs(newInputs, added);
    }
    
    public void resetAllModifiers()
    {
        for(ModifiedInput m:modifiers.getModifiers())
        {
            m.reset();
        }
    }
    
    public void removeAll()
    {
        modifiers.removeAllModifiers();
        clearAdded();
    }
    
    public int getNumModifiers()
    {
        return modifiers.getNumModifiers();
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
                feature.addFeature(modifiers);
                added.put(key,feature);
                break;
            }
        }
        setDirty(true);
        setDirty(false);
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
            modifiers.removeModifier(id);
        }
        modifiers.removeOrphanedModifiers();
        modifiers.removeDeadEnds();
        
        added.remove(key);
        setDirty(true);
        setDirty(false);
    }
    
    public void clearAdded()
    {
        added.clear();
        setDirty(true);
        setDirty(false);
    }
   
    public void setSelectedFeatures(boolean[] onOff)
    {
        int ptr = 0;
        for(Feature feature:getLibrary())
        {
            if(onOff[ptr])
            {
                addFeatureForKey(feature.name);
            }
            else
            {
                 removeFeatureForKey(feature.name);
            }
            ptr++;
        }
    }
    
    //Dirty State
    
    public boolean isDirty(boolean testSet)
    {
        return testSet ? testSetDirtyFlag : dirtyFlag;
    }
    
    public void setDirty(boolean testSet)
    {
       if(testSet)
       {
           testSetDirtyFlag = true;
       }
       else
       {
           dirtyFlag = true;
       }
       
       for(ModifiedInput modifier:modifiers.getModifiers())
       {
           modifier.reset();
       }
    }
    
    public void didRecalculateFeatures(boolean testSet)
    {
        if(testSet)
       {
           testSetDirtyFlag = false;
       }
       else
       {
           dirtyFlag = false;
       }
    }
    
    public void setFeatureWindowSize(int windowSize, int bufferSize)
    {
        initLibrary(windowSize, bufferSize);
        setDirty(true);
        setDirty(false);
    }
}

class FeatureMetadata
{
    static InputDiagram diagramForInput(int index)
    {
        switch(index)
        {
            case FeatureCollection.ACCX: return InputDiagram.ACCX;
            case FeatureCollection.ACCY: return InputDiagram.ACCY;
            case FeatureCollection.ACCZ: return InputDiagram.ACCZ;
            case FeatureCollection.GYROX: return InputDiagram.GYROX;
            case FeatureCollection.GYROY: return InputDiagram.GYROY;
            case FeatureCollection.GYROZ: return InputDiagram.GYROZ;
        }
        return InputDiagram.UNKNOWN;
    }
    
    static String[] tagsForInput(int index)
    {
        switch(index)
        {
            case FeatureCollection.ACCX: return new String[]{"AccelerometerX", "Accelerometer"};
            case FeatureCollection.ACCY: return new String[]{"AccelerometerY", "Accelerometer"};
            case FeatureCollection.ACCZ: return new String[]{"AccelerometerZ", "Accelerometer"};
            case FeatureCollection.GYROX: return new String[]{"GyroscopeX", "Gyroscope"};
            case FeatureCollection.GYROY: return new String[]{"GyroscopeY", "Gyroscope"};
            case FeatureCollection.GYROZ: return new String[]{"GyroscopeZ", "Gyroscope"};
        }
        return new String[0];
    }
    
    static String tagForOperation(Operation op)
    {
        if(op.getClass().equals(AverageWindowOperation.class))
        {
            return "Mean";
        }
        else if(op.getClass().equals(StdDevWindowOperation.class))
        {
            return "Standard Deviation";
        }
        else if(op.getClass().equals(EnergyWindowOperation.class))
        {
            return "Energy";
        }
        else if(op.getClass().equals(MaxWindowOperation.class))
        {
            return "Max";
        }
        else if(op.getClass().equals(MinWindowOperation.class))
        {
            return "Min";
        }
        return "";
    }
}

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
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("FFT");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
        
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        FFTModifier fft = new FFTModifier("fft", index, totalBins, bins);
        fft.addToOutput= true;
        fft.addRequiredModifierID(0);
        int fftID = addModifier(mc,fft);
        
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
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("FFT");
        tags.add("Max");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        FFTModifier fft = new FFTModifier("fft", index, totalBins, bins);
        fft.addToOutput= false;
        fft.addRequiredModifierID(0);
        int fftID = addModifier(mc,fft);
        
        ModifiedInput max = new MaxInputs("max",0);
        max.addRequiredModifierID(fftID);
        int maxID = addModifier(mc,max);
        
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
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("FFT");
        tags.add("Min");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        FFTModifier fft = new FFTModifier("fft", index, totalBins, bins);
        fft.addToOutput= false;
        fft.addRequiredModifierID(0);
        int fftID = addModifier(mc,fft);
        
        ModifiedInput min = new MinInputs("min",0);
        min.addRequiredModifierID(fftID);
        int minID = addModifier(mc,min);
        
        setOutputModifierID(minID);  
    }
}

class WindowedFeature extends FeatureSingleModifierOutput
{
    
    ModifiedInput window;
    
    public WindowedFeature(String name, Operation op, int index, int windowSize) {
        super(name);
        this.window = new WindowedOperation("input-1",op,index,windowSize,0);
        window.addRequiredModifierID(0);
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add(FeatureMetadata.tagForOperation(op));
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
    }
    
    @Override
    public void addFeature(ModifierCollection mc)
    {
        int id1 = addModifier(mc, window);
        setOutputModifierID(id1);
    }
}

class PassThrough extends FeatureMultipleModifierOutput
{
    int[] indexes;
    
    public PassThrough(String name, int[] inputs) {
        super(name);
        this.indexes = inputs;
        this.diagram = InputDiagram.MULTIPLE;
        tags.add("Raw");
        for(int input:inputs)
        {
            String[] inputTags = FeatureMetadata.tagsForInput(input);
            for(String tag:inputTags)
            {
                if(!tags.contains(tag))
                {
                    tags.add(tag);
                }
            }
        }
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        for(int index:indexes)
        {
            PassThroughSingle modifier = new PassThroughSingle(Integer.toString(index),index,0);
            modifier.addRequiredModifierID(0);
            int id1 = addModifier(mc, modifier);
        }
        setOutputModifierIDs(ids.toArray(new Integer[ids.size()]));
    }
}

class PassThroughAll extends FeatureSingleModifierOutput
{    
    public PassThroughAll(String name) {
        super(name);
        this.diagram = InputDiagram.MULTIPLE;
        tags.add("Gyroscope");
        tags.add("GyroscopeX");
        tags.add("GyroscopeY");
        tags.add("GyroscopeZ");
        tags.add("Accelerometer");
        tags.add("AccelerometerX");
        tags.add("AccelerometerY");
        tags.add("AccelerometerZ");
        tags.add("Raw");
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        String[] names = ((PassThroughVector)mc.getModifier(0)).names;
        PassThroughVector modifier = new PassThroughVector(names, 0);
        modifier.addToOutput = true;
        modifier.addRequiredModifierID(0);
        int id1 = addModifier(mc, modifier);
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
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("Buffer");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        BufferedInput modifier = new BufferedInput(name, index, windowSize, 0);
        modifier.addToOutput = true;
        modifier.addRequiredModifierID(0);
        int id1 = addModifier(mc, modifier);
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
        this.diagram = InputDiagram.MULTIPLE;
        tags.add("Magnitude");
        tags.add("1st Order Diff");
        for(int input:inputs)
        {
            String[] inputTags = FeatureMetadata.tagsForInput(input);
            for(String tag:inputTags)
            {
                if(!tags.contains(tag))
                {
                    tags.add(tag);
                }
            }
        }
    }
    
    @Override
    public void addFeature(ModifierCollection mc)
    {
        
        FirstOrderDifference fod1 = new FirstOrderDifference("FOD-1",inputs[0],0);
        fod1.addRequiredModifierID(0);
        fod1.addToOutput = false;
        int fod1ID = addModifier(mc, fod1);
                    
        FirstOrderDifference fod2 = new FirstOrderDifference("FOD-2",inputs[1],0);
        fod2.addRequiredModifierID(0);
        fod2.addToOutput = false;
        int fod2ID = addModifier(mc, fod2);
         
        FirstOrderDifference fod3 = new FirstOrderDifference("FOD-3",inputs[2],0);
        fod3.addRequiredModifierID(0);
        fod3.addToOutput = false;
        int fod3ID = addModifier(mc, fod3);
        
        ModifiedInput mag = new MultipleInputWindowedOperation("input-1",new ThreeDimensionalMagnitude(),windowSize,0);
        mag.addRequiredModifierID(fod1ID);
        mag.addRequiredModifierID(fod2ID);
        mag.addRequiredModifierID(fod3ID);
        int id1 = addModifier(mc, mag);
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
        this.diagram = InputDiagram.MULTIPLE;
        tags.add("Magnitude");
        for(int input:inputs)
        {
            String[] inputTags = FeatureMetadata.tagsForInput(input);
            for(String tag:inputTags)
            {
                if(!tags.contains(tag))
                {
                    tags.add(tag);
                }
            }
        }
    }
    
    @Override
    public void addFeature(ModifierCollection mc)
    {
        
        ModifiedInput raw1 = new PassThroughSingle("raw-1",inputs[0],0);
        raw1.addToOutput = false;
        raw1.addRequiredModifierID(0);
        int rawID1 = addModifier(mc, raw1);
        
        ModifiedInput raw2 = new PassThroughSingle("raw-2",inputs[1],0);
        raw2.addToOutput = false;
        raw2.addRequiredModifierID(0);
        int rawID2 = addModifier(mc, raw2);
        
        ModifiedInput raw3 = new PassThroughSingle("raw-3",inputs[2],0);
        raw3.addToOutput = false;
        raw3.addRequiredModifierID(0);
        int rawID3 = addModifier(mc, raw3);
        
        ModifiedInput mag = new MultipleInputWindowedOperation("input-1",new ThreeDimensionalMagnitude(),windowSize,0);
        mag.addRequiredModifierID(rawID1);
        mag.addRequiredModifierID(rawID2);
        mag.addRequiredModifierID(rawID3);
        int id1 = addModifier(mc, mag);
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
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("1st Order Diff");
        tags.add("Raw");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
    }
    
    @Override
    public void addFeature(ModifierCollection mc)
    {
        FirstOrderDifference fod = new FirstOrderDifference("FOD",index,0);
        fod.addRequiredModifierID(0);
        int id1 = addModifier(mc, fod);
        setOutputModifierID(id1);
    }
}

class WindowedFOD extends FeatureSingleModifierOutput
{
    Operation op;
    int windowSize;
    int index;
    
    public WindowedFOD(String name, Operation op, int index, int windowSize) {
        super(name);
        this.op = op;
        this.index = index;
        this.windowSize = windowSize;
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("1st Order Diff");
        tags.add(FeatureMetadata.tagForOperation(op));
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
    }
    
    @Override
    public void addFeature(ModifierCollection mc)
    {
        
        FirstOrderDifference fod = new FirstOrderDifference("FOD", index, 0);
        fod.addRequiredModifierID(0);
        fod.addToOutput = false;
        int fodID = addModifier(mc, fod);
        
        ModifiedInput window = new WindowedOperation("input-1",op, 0, windowSize, 0);
        window.addRequiredModifierID(fodID);
        int windowID = addModifier(mc, window);

        setOutputModifierID(windowID);
    }
}

class CorrelateFeature extends FeatureSingleModifierOutput
{
    int windowSize;
    int[] indexes;
    
    public CorrelateFeature(String name, int inputs[], int windowSize) {
        super(name);
        this.indexes = inputs;
        this.windowSize = windowSize;
        this.diagram = InputDiagram.MULTIPLE;
        tags.add("Correlation");
        for(int input:inputs)
        {
            String[] inputTags = FeatureMetadata.tagsForInput(input);
            for(String tag:inputTags)
            {
                if(!tags.contains(tag))
                {
                    tags.add(tag);
                }
            }
        }
    }
    
    @Override
    public void addFeature(ModifierCollection mc)
    {
        
        ModifiedInput raw1 = new PassThroughSingle("raw-1",indexes[0],0);
        raw1.addToOutput = false;
        raw1.addRequiredModifierID(0);
        int rawID1 = addModifier(mc, raw1);
        
        ModifiedInput raw2 = new PassThroughSingle("raw-2",indexes[1],0);
        raw2.addToOutput = false;
        raw2.addRequiredModifierID(0);
        int rawID2 = addModifier(mc, raw2);
       
        ModifiedInput correlate = new MultipleInputWindowedOperation("input-1",new CorrelateWindowOperation(),windowSize,0);
        correlate.addRequiredModifierID(rawID1);
        correlate.addRequiredModifierID(rawID2);
        int correlateID = addModifier(mc, correlate);

        setOutputModifierID(correlateID);
    }
}