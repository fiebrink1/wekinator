    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import wekimini.modifiers.Feature.InputDiagram;
import wekimini.modifiers.WindowedOperation.Operation;
/**
 *
 * @author louismccallum
 */

public final class FeatureCollection 
{
    private final LinkedHashMap<String, Feature> added = new LinkedHashMap<>();
    private final List<Feature> library = new ArrayList();
    private String[] names;
    private ModifierCollection modifiers;
    private boolean dirtyFlag = true;
    private boolean testSetDirtyFlag = true;
    private String[] tags;
    protected static final int ACCX = 0;
    protected static final int ACCY = 1;
    protected static final int ACCZ = 2;
    protected static final int GYROX = 3;
    protected static final int GYROY = 4;
    protected static final int GYROZ = 5;
    protected HashMap<String, Integer> inputs = new HashMap();
    protected static final String FFT_DESCRIPTION = "FFT \nUse this feature if you are interested in the periodicity of your motion";
    protected static final String RAW_DESCRIPTION = "Raw \nJust the raw signal";
    protected static final String MEAN_DESCRIPTION = "Mean \nUse this feature to smooth out measurements over the given window e.g. if you are only interested in bigger changes over time";
    protected static final String FOD_DESCRIPTION = "1st Order Diff \nUse this feature if you want to distinguish gestures or rotate at different speeds";
    protected static final String ENERGY_DESCRIPTION = "Energy \nUse this if you are interested in the strength of your signal";
    protected static final String MAX_DESCRIPTION = "Max \nUse this feature if you are interested in the extremes of your motion";
    protected static final String MIN_DESCRIPTION = "Min \nUse this feature if you are interested in the extremes of your motion";
    protected static final String IQR_DESCRIPTION = "Interquartile Range \nUse this feature to measure variability";
    protected static final String STDDEV_DESCRIPTION = "Standard Deviation \nUse this feature if you want to model how much variation there is in the signal";
    protected static final String CORRELATION_DESCRIPTION = "Correlation \nThis is a measure of how similar two signals are";
    protected static final String MAG_DESCRIPTION = "Magnitude \nThis feature tells you the amount of movement over all three axes";
    
    private FeatureCollection(){}
    
    public FeatureCollection(String[] inputNames)
    {
        inputs.put("AccX", ACCX);
        inputs.put("AccY", ACCY);
        inputs.put("AccZ", ACCZ);
        inputs.put("GyroX", GYROX);
        inputs.put("GyroY", GYROY);
        inputs.put("GyroZ", GYROZ);
        initLibrary(10, 10);
        modifiers = new ModifierCollection(inputNames);
    }
    
    public synchronized void initLibrary(int windowSize, int bufferSize)
    {
        
        library.clear();
        
        Iterator it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new PassThrough(pair.getKey(),new int[]{pair.getValue()}));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFeature("Mean" + pair.getKey(),new AverageWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFeature("StdDev" + pair.getKey(),new StdDevWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFeature("Max" + pair.getKey(),new MaxWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFeature("Min" + pair.getKey(),new MinWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFeature("Energy" + pair.getKey(),new EnergyWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFeature("IQR" + pair.getKey(),new IQRWindowOperation(),pair.getValue(),windowSize));
        }

        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new FODRaw("FOD" + pair.getKey(), pair.getValue()));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFOD("MeanFOD" + pair.getKey(),new AverageWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFOD("StdDevFOD" + pair.getKey(),new StdDevWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFOD("MaxFOD" + pair.getKey(),new MaxWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFOD("MinFOD" + pair.getKey(),new MinWindowOperation(),pair.getValue(),windowSize));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new WindowedFOD("IQRFOD" + pair.getKey(),new IQRWindowOperation(),pair.getValue(),windowSize));
        }
        
        int [] bins = new int[]{0,8,16,24,36,48,60};
        for(int i = 0; i < 7; i++)
        {
            it = inputs.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<String, Integer> pair = (Map.Entry)it.next();
                library.add(new FFTSingleBinFeature("FFT" + pair.getKey() + "("+bins[i]+"/128)", pair.getValue(), 128, bins[i]));
                library.add(new FFTFODSingleBinFeature("FFTFOD" + pair.getKey() + "("+bins[i]+"/128)", pair.getValue(), 128, bins[i]));
            }
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new MaxFFT("MaxBinFFT" + pair.getKey(), pair.getValue(), 128));
        }
        
        it = inputs.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, Integer> pair = (Map.Entry)it.next();
            library.add(new MinFFT("MinBinFFT" + pair.getKey(), pair.getValue(), 128));
        }
        
        //Less obvious how to make these modular

        library.add(new CorrelateFeature("CorrelateAccXY",new int[]{ACCX,ACCY},windowSize));
        library.add(new CorrelateFeature("CorrelateAccXZ",new int[]{ACCX,ACCZ},windowSize));
        library.add(new CorrelateFeature("CorrelateAccYZ",new int[]{ACCY,ACCZ},windowSize));
        library.add(new CorrelateFeature("CorrelateGyroXY",new int[]{GYROX,GYROY},windowSize));
        library.add(new CorrelateFeature("CorrelateGyroXZ",new int[]{GYROX,GYROZ},windowSize));
        library.add(new CorrelateFeature("CorrelateGyroYZ",new int[]{GYROY,GYROZ},windowSize));
        library.add(new CorrelateFOD("CorrelateFODAccXY",new int[]{ACCX,ACCY},windowSize));
        library.add(new CorrelateFOD("CorrelateFODAccXZ",new int[]{ACCX,ACCZ},windowSize));
        library.add(new CorrelateFOD("CorrelateFODAccYZ",new int[]{ACCY,ACCZ},windowSize));
        library.add(new CorrelateFOD("CorrelateFODGyroXY",new int[]{GYROX,GYROY},windowSize));
        library.add(new CorrelateFOD("CorrelateFODGyroXZ",new int[]{GYROX,GYROZ},windowSize));
        library.add(new CorrelateFOD("CorrelateFODGyroYZ",new int[]{GYROY,GYROZ},windowSize));
        
        library.add(new MagnitudeFeature("MagAcc", new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new WindowedMagnitudeFeature("MeanMagAcc", new AverageWindowOperation(), new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new WindowedMagnitudeFeature("StdDevMagAcc", new StdDevWindowOperation(), new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new WindowedMagnitudeFeature("MaxMagAcc", new MaxWindowOperation(), new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new WindowedMagnitudeFeature("MinMagAcc", new MinWindowOperation(), new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new WindowedMagnitudeFeature("EnergyMagAcc", new EnergyWindowOperation(), new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new WindowedMagnitudeFeature("IQRMagAcc", new IQRWindowOperation(), new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new MagnitudeFeature("MagGyro", new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new WindowedMagnitudeFeature("MeanMagGyro", new AverageWindowOperation(), new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new WindowedMagnitudeFeature("StdDevMagGyro", new StdDevWindowOperation(), new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new WindowedMagnitudeFeature("MaxMagGyro", new MaxWindowOperation(), new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new WindowedMagnitudeFeature("MinvMagGyro", new MinWindowOperation(), new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new WindowedMagnitudeFeature("EnergyMagGyro", new EnergyWindowOperation(), new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new WindowedMagnitudeFeature("IQRMagGyro", new IQRWindowOperation(), new int[]{GYROX,GYROY,GYROZ}, 2));
        library.add(new MagnitudeFODFeature("MagFODAcc", new int[]{ACCX,ACCY,ACCZ}, 2));
        library.add(new MagnitudeFODFeature("MagFODGyro", new int[]{GYROX,GYROY,GYROZ}, 2));
        
        names = new String[library.size()];
        int ptr = 0;
        for(Feature feature:library)
        {
            names[ptr] = feature.name;
            ptr++;
        }
        
        ArrayList<String> keys = new ArrayList(added.keySet());
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
        System.out.println("cant find feature for key:" + key);
        throw new NoSuchElementException();
    }
    
    public Feature[] getFeaturesForFeatures(Feature[] features)
    {
        ArrayList<Feature> toReturn = new ArrayList();
        for(Feature feature:features)
        {
            toReturn.add(getFeatureForKey(feature.name));
        }
        Feature[] f = new Feature[toReturn.size()];
        f = toReturn.toArray(f);
        return f;
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
        if(searchTags.length < 1)
        {
            return new Feature[0];
        }
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
                        if(matchTag.toLowerCase().equals(searchTag.toLowerCase()))
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
    
    public synchronized void updateFeatureIndexes()
    {
        for(Iterator<Feature> iterator = added.values().iterator(); iterator.hasNext();)
        {
             Feature f = iterator.next();
            f.setOutputIndexes(modifiers.indexesForName(f.name));
        }
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
        double[] vals = modifiers.computeAndGetValuesForNewInputs(newInputs, added);
        updateFeatureIndexes();
        return vals;
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
        
        for(Integer id:f.modifierIds)
        {
            modifiers.removeModifier(id);
        }
        modifiers.removeOrphanedModifiers();
        modifiers.removeDeadEnds();
        
        added.remove(key);
        setDirty(true);
        setDirty(false);
    }
    
    public Feature[] getCurrentFeatures()
    {
        Feature[] ft = new Feature[added.values().size()];
        ft = added.values().toArray(ft);
        return ft;
    }
    
    public String[] getCurrentFeatureNames()
    {
        Feature[] ft = getCurrentFeatures();
        String[] names = new String[ft.length];
        int ptr = 0;
        for(Feature f : ft)
        {
            names[ptr] = ft[ptr].name;
            ptr++;
        }
        return names;
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
        else if(op.getClass().equals(IQRWindowOperation.class))
        {
            return "IQR";
        }
        return "";
    }
    
    static String descriptionForOperation(Operation op)
    {
        if(op.getClass().equals(AverageWindowOperation.class))
        {
            return FeatureCollection.MEAN_DESCRIPTION;
        }
        else if(op.getClass().equals(StdDevWindowOperation.class))
        {
            return FeatureCollection.STDDEV_DESCRIPTION;
        }
        else if(op.getClass().equals(EnergyWindowOperation.class))
        {
            return FeatureCollection.ENERGY_DESCRIPTION;
        }
        else if(op.getClass().equals(MaxWindowOperation.class))
        {
            return FeatureCollection.MAX_DESCRIPTION;
        }
        else if(op.getClass().equals(MinWindowOperation.class))
        {
            return FeatureCollection.MIN_DESCRIPTION;
        }
        else if(op.getClass().equals(IQRWindowOperation.class))
        {
            return FeatureCollection.IQR_DESCRIPTION;
        }
        return "";
    }
}

class FFTFODSingleBinFeature extends FeatureSingleModifierOutput
{
    
    int totalBins;
    int bin;
    int index;
    
    public FFTFODSingleBinFeature(String name, int index, int totalBins, int selectedBin) {
        super(name);
        this.bin = selectedBin;
        this.totalBins = totalBins;
        this.index = index;
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("FFT");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
        this.description = FeatureCollection.FFT_DESCRIPTION;
        
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        
        FirstOrderDifference fod1 = new FirstOrderDifference("FOD-1",index,0);
        fod1.addRequiredModifierID(0);
        fod1.addToOutput = false;
        int fod1ID = addModifier(mc, fod1);
        
        int [] allBins = new int[totalBins];
        for(int i = 0; i < totalBins; i++)
        {
            allBins[i] = i;
        }
        
        FFTModifier fft = new FFTModifier("fft", 0, totalBins, allBins);
        fft.addToOutput = false;
        fft.addRequiredModifierID(fod1ID);
        int fftID = addModifier(mc,fft);
        
        PassThroughSingle single = new PassThroughSingle(Integer.toString(bin),bin,0);
        single.addToOutput = true;
        single.addRequiredModifierID(fftID);
        int singleID = addModifier(mc,single);
        
        setOutputModifierID(singleID);
    }
}

class FFTSingleBinFeature extends FeatureSingleModifierOutput
{
    
    int totalBins;
    int bin;
    int index;
    
    public FFTSingleBinFeature(String name, int index, int totalBins, int selectedBin) {
        super(name);
        this.bin = selectedBin;
        this.totalBins = totalBins;
        this.index = index;
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("FFT");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
        this.description = FeatureCollection.FFT_DESCRIPTION;
        
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        int [] allBins = new int[totalBins];
        for(int i = 0; i < totalBins; i++)
        {
            allBins[i] = i;
        }
        FFTModifier fft = new FFTModifier("fft", index, totalBins, allBins);
        fft.addToOutput = false;
        fft.addRequiredModifierID(0);
        int fftID = addModifier(mc,fft);
        
        PassThroughSingle single = new PassThroughSingle(Integer.toString(bin),bin,0);
        single.addToOutput = true;
        single.addRequiredModifierID(fftID);
        int singleID = addModifier(mc,single);
        
        setOutputModifierID(singleID);
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
        this.description = FeatureCollection.FFT_DESCRIPTION;
        
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
    int index;
    
    public MaxFFT(String name, int index, int totalBins) {
        super(name);
        this.totalBins = totalBins;
        this.index = index;
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("FFT");
        tags.add("Max");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
        this.description = FeatureCollection.FFT_DESCRIPTION;
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        int [] allBins = new int[totalBins];
        for(int i = 0; i < totalBins; i++)
        {
            allBins[i] = i;
        }
        FFTModifier fft = new FFTModifier("fft", index, totalBins, allBins);
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
    int index;
    
    public MinFFT(String name, int index, int totalBins) {
        super(name);
        this.totalBins = totalBins;
        this.index = index;
        this.diagram = FeatureMetadata.diagramForInput(index);
        tags.add("FFT");
        tags.add("Min");
        tags.addAll(new ArrayList<>(Arrays.asList(FeatureMetadata.tagsForInput(index))));
        this.description = FeatureCollection.FFT_DESCRIPTION;
    }

    @Override
    public void addFeature(ModifierCollection mc)
    {
        int [] allBins = new int[totalBins];
        for(int i = 0; i < totalBins; i++)
        {
            allBins[i] = i;
        }
        FFTModifier fft = new FFTModifier("fft", index, totalBins, allBins);
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
        this.description = FeatureMetadata.descriptionForOperation(op);
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
        if(inputs.length == 1)
        {
            this.diagram = FeatureMetadata.diagramForInput(inputs[0]);
        }
        tags.add("Raw");
        this.description = FeatureCollection.RAW_DESCRIPTION;
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
        setOutputModifierIDs(modifierIds.toArray(new Integer[modifierIds.size()]));
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
        String[] names = ((PassThroughVector)mc.getModifierForID(0)).names;
        PassThroughVector modifier = new PassThroughVector(names, 0);
        modifier.addToOutput = true;
        modifier.addRequiredModifierID(0);
        int id1 = addModifier(mc, modifier);
        setOutputModifierID(id1);
    }
}

class BufferFeature extends FeatureSingleModifierOutput
{  
    
    private final int index;
    private final int windowSize;
    
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
        this.description = FeatureCollection.MAG_DESCRIPTION;
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
        this.description = FeatureCollection.FOD_DESCRIPTION;
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

class WindowedMagnitudeFeature extends FeatureSingleModifierOutput
{
    int[] inputs;
    int windowSize;
    Operation op;
    
    public WindowedMagnitudeFeature(String name, Operation op, int[] inputs, int windowSize)
    {
        super(name);
        this.op = op;
        this.inputs = inputs;
        this.windowSize = windowSize;
        this.diagram = InputDiagram.MULTIPLE;
        tags.add("Magnitude");
        tags.add(FeatureMetadata.tagForOperation(op));
        this.description = FeatureCollection.FOD_DESCRIPTION;
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
        
        ModifiedInput mag = new MultipleInputWindowedOperation("mag",new ThreeDimensionalMagnitude(),windowSize,0);
        mag.addRequiredModifierID(rawID1);
        mag.addRequiredModifierID(rawID2);
        mag.addRequiredModifierID(rawID3);
        mag.addToOutput = false;
        int magID = addModifier(mc, mag);
        
        ModifiedInput window = new WindowedOperation("window",op, 0, windowSize, 0);
        window.addRequiredModifierID(magID);
        int windowID = addModifier(mc, window);

        setOutputModifierID(windowID);
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
        this.description = FeatureCollection.FOD_DESCRIPTION;
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
        this.description = FeatureCollection.FOD_DESCRIPTION;
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

class CorrelateFOD extends FeatureSingleModifierOutput
{
    int windowSize;
    int[] indexes;
    
    public CorrelateFOD(String name, int inputs[], int windowSize) {
        super(name);
        this.indexes = inputs;
        this.windowSize = windowSize;
        this.diagram = InputDiagram.MULTIPLE;
        tags.add("Correlation");
        tags.add("1st Order Diff");
        this.description = FeatureCollection.CORRELATION_DESCRIPTION;
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
        
        ModifiedInput fod1 = new FirstOrderDifference("fod-1",indexes[0],0);
        fod1.addToOutput = false;
        fod1.addRequiredModifierID(0);
        int fodID1 = addModifier(mc, fod1);
        
        ModifiedInput fod2 = new FirstOrderDifference("fod-2",indexes[1],0);
        fod2.addToOutput = false;
        fod2.addRequiredModifierID(0);
        int fodID2 = addModifier(mc, fod2);
       
        ModifiedInput correlate = new MultipleInputWindowedOperation("input-1",new CorrelateWindowOperation(),windowSize,0);
        correlate.addRequiredModifierID(fodID1);
        correlate.addRequiredModifierID(fodID2);
        int correlateID = addModifier(mc, correlate);

        setOutputModifierID(correlateID);
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
        this.description = FeatureCollection.CORRELATION_DESCRIPTION;
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