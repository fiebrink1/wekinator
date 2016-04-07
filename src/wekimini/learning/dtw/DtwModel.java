/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning.dtw;

import com.dtw.FastDTW;
import com.thoughtworks.xstream.XStream;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.DtwLearningManager;
import wekimini.Wekinator;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import static wekimini.learning.dtw.DtwModel.RecordingState.NOT_RECORDING;
import static wekimini.learning.dtw.DtwModel.RunningState.NOT_RUNNING;
import wekimini.learning.dtw.DtwSettings.RunningType;
import wekimini.learning.Model;
import wekimini.osc.OSCDtwOutput;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;
import wekimini.util.Util;

/*
 * Working notes:
 (OSC can be sent following ways:
 - Continuously, for every input (show closest class or 0 if none)
 - Only when match changes
 - both 
 OSC can also be sent
 - 1 message per DTW model
 - 1 message with results of all models
 FOR NOW, just make this [/wek/outputs]/className, and just send when it changes
 (Could also send along with continuous messages w/ status)

 Alternative temporal model: just classify previous vector, based on start/stop signals for run
 */
/**
 *
 * @author rebecca
 */
public class DtwModel implements Model {

    public static final String PROP_RUNNING_TYPE = "runningType";
    public static final String PROP_CURRENT_MATCH = "currentMatch";
    public static final String PROP_MAX_DISTANCE = "maxDistance";
    private final transient List<DtwUpdateListener1> updateListeners = new LinkedList<>();
    private final transient List<DtwUpdateListener1> normalizedUpdateListeners = new LinkedList<>();
    private transient int currentMatch = -1;
    private final boolean[] isGestureActive;
    private final DtwData data;
    private int numGestures = 0;
    private final transient double[] closestDistances;
    private final transient double[] normalizedDistances;
    private transient int minSizeInExamples = 0;
    private transient int maxSizeInExamples = 0;
    private transient int minSizeInDownsampledExamples = 0;
    private transient int maxSizeInDownsampledExamples = 0;
    private DtwSettings settings;
    private double maxDistance = 0;
    private transient final Wekinator w;
    private String modelName;
    private String myID;
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private static final Logger logger = Logger.getLogger(DtwModel.class.getName());
    private int maxSliderValue;
    public static final String PROP_MAXSLIDERVALUE = "maxSliderValue";
    private double matchThreshold = 5;
    public static final String PROP_MATCHTHRESHOLD = "matchThreshold";
    private RecordingState recordingState = NOT_RECORDING;
    private RunningState runningState = NOT_RUNNING;

    private final int[] versionNumbers;
    public static final String PROP_VERSIONNUMBERS = "versionNumbers";
   // private final String[] gestureNames;
    private final OSCDtwOutput myOutput;
    private DistanceFunction distanceFunction = DistanceFunctionFactory.EUCLIDEAN_DIST_FN;

    private boolean[] selectedInputs;

    public static final String PROP_SELECTED_INPUTS = "selectedInputs";
    private boolean wasRunningBeforeRecord = false;

    /**
     * Get the value of selectedInputs
     *
     * @return the value of selectedInputs
     */
    public boolean[] getSelectedInputs() {
        return selectedInputs;
    }

    /**
     * Set the value of selectedInputs
     *
     * @param selectedInputs new value of selectedInputs
     */
    public void setSelectedInputs(boolean[] selectedInputs) {
        boolean[] oldSelectedInputs = this.selectedInputs;
        this.selectedInputs = selectedInputs;
        distanceFunction = new EuclideanDistanceWithInputSelection(selectedInputs);
        updateMaxDistance();
        updateID();
        for (int i = 0; i < numGestures; i++) {
            updateVersionNumber(i);
        }
        propertyChangeSupport.firePropertyChange(PROP_SELECTED_INPUTS, oldSelectedInputs, selectedInputs);
    }

    private void setSelectedInputsWithoutVersionOrIDIncrement(boolean[] selectedInputs) {
        boolean[] oldSelectedInputs = this.selectedInputs;
        this.selectedInputs = selectedInputs;
        distanceFunction = new EuclideanDistanceWithInputSelection(selectedInputs);
        updateMaxDistance();
        propertyChangeSupport.firePropertyChange(PROP_SELECTED_INPUTS, oldSelectedInputs, selectedInputs);
    }
    
    /**
     * Set the value of selectedInputs at specified index.
     *
     * @param index the index of selectedInputs
     * @param isSelected new value of selectedInputs at specified index
     */
    public void setSelectedInputs(int index, boolean isSelected) {
        boolean oldSelectedInputs = this.selectedInputs[index];
        this.selectedInputs[index] = isSelected;
        distanceFunction = new EuclideanDistanceWithInputSelection(selectedInputs);

        propertyChangeSupport.fireIndexedPropertyChange(PROP_SELECTED_INPUTS, index, oldSelectedInputs, selectedInputs);
    }

    public DtwModel(String name, OSCDtwOutput output, int numGestures, Wekinator w, DtwLearningManager m, DtwSettings settings) {
        this.modelName = name;
        this.settings = settings;
        this.myOutput = output;
        this.numGestures = numGestures;
        this.w = w;

        closestDistances = new double[numGestures];
        normalizedDistances = new double[numGestures];
        isGestureActive = new boolean[numGestures];
        versionNumbers = new int[numGestures];
        //gestureNames = new String[numGestures];
        selectedInputs = new boolean[w.getInputManager().getNumInputs()];

        for (int i = 0; i < numGestures; i++) {
            isGestureActive[i] = true;
            closestDistances[i] = Double.MAX_VALUE;
            normalizedDistances[i] = 0;
            versionNumbers[i] = 1;
            //gestureNames[i] = name + "_" + i;
        }

        for (int i = 0; i < selectedInputs.length; i++) {
            selectedInputs[i] = true;
        }

        data = new DtwData(numGestures, m, w);
        applySettingsToData(settings, data);
        data.addDataListener(new DtwData.DtwDataListener() {

            @Override
            public void numExamplesChanged(int whichClass, int currentNumExamples) {
                updateMaxDistance();
                updateID();
                updateVersionNumber(whichClass);
            }

            @Override
            public void exampleAdded(int whichClass) {
            }

            @Override
            public void exampleDeleted(int whichClass) {
            }

            @Override
            public void allExamplesDeleted() {
            }
        });

        System.out.println("myOutput = " + myOutput);
        System.out.println("Prop is " + OSCDtwOutput.PROP_NUMGESTURES);
        myOutput.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OSCDtwOutput.PROP_NUMGESTURES)) {
                    numGesturesChanged((Integer) evt.getNewValue());
                }
            }
        });

        updateID();
    }

    /**
     * Get the value of matchThreshold
     *
     * @return the value of matchThreshold
     */
    public double getMatchThreshold() {
        return matchThreshold;
    }

    /**
     * Set the value of matchThreshold
     *
     * @param matchThreshold new value of matchThreshold
     */
    public void setMatchThreshold(double matchThreshold) {
        double oldMatchThreshold = this.matchThreshold;
        this.matchThreshold = matchThreshold;
        propertyChangeSupport.firePropertyChange(PROP_MATCHTHRESHOLD, oldMatchThreshold, matchThreshold);
        KadenzeLogging.getLogger().dtwThresholdChanged(w, oldMatchThreshold, matchThreshold);
    }

    /**
     * Get the value of maxSliderValue
     *
     * @return the value of maxSliderValue
     */
    public int getMaxSliderValue() {
        return maxSliderValue;
    }

    /**
     * Set the value of maxSliderValue
     *
     * @param maxSliderValue new value of maxSliderValue
     */
    public void setMaxSliderValue(int maxSliderValue) {
        int oldMaxSliderValue = this.maxSliderValue;
        this.maxSliderValue = maxSliderValue;
        propertyChangeSupport.firePropertyChange(PROP_MAXSLIDERVALUE, oldMaxSliderValue, maxSliderValue);
    }

    public int getNumGestures() {
        return numGestures;
    }

    public boolean isInputSelected(int inputIndex) {
        return selectedInputs[inputIndex];
    }

    public OSCDtwOutput getOSCOutput() {
        return myOutput;
    }

    public double[][] runOnBundle(List<List<Double>> values) {
        //int valNum = 1; //Starts at 1
        double[][] allOutputs = new double[values.size()][];
        int i = 0;
        //for (int i = 0; i < values.size(); i++) {
        for (List<Double> thisValue : values) {
            double[] thisInput = new double[thisValue.size()];
            for (int j = 0 ; j < thisValue.size(); j++) {
                thisInput[j] = thisValue.get(j);
            }
            data.addRunningVector(thisInput);
            double[] thisOutput = getClassificationVector();
            allOutputs[i++] = thisOutput;
        }
        return allOutputs;
    
    }

    public void writeDataToFile(File f) throws FileNotFoundException {
        data.writeDataToFile(f);
    }

    public void writeToFile(String filename) throws IOException {
        Util.writeToXMLFile(this, "DtwModel", DtwModel.class, filename);
        
        /*boolean success = false;
        IOException myEx = new IOException();
        FileOutputStream outstream = null;
        ObjectOutputStream objout = null;
        try {
            outstream = new FileOutputStream(filename);
            objout = new ObjectOutputStream(outstream);
            XStream xstream = new XStream();
            xstream.alias("DtwModel", DtwModel.class);
            String xml = xstream.toXML(this);
            objout.writeObject(xml);
            success = true;
        } catch (IOException ex) {
            success = false;
            myEx = ex;
            logger.log(Level.WARNING, "Could not write to file {0", ex.getMessage());
        } finally {
            try {
                if (objout != null) {
                    objout.close();
                }
                if (outstream != null) {
                    outstream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not close file objects");
            }
        }
        if (!success) {
            throw myEx;
        } */
        
    }

    private boolean isCompatible(DtwModel m) {
        if (m.getNumGestures() != getNumGestures()) {
            return false;
        }
        return m.getSelectedInputs().length == getSelectedInputs().length;
    }
    
    //Do it this way rather than copying model because of listeners & memory
    public void loadFromExisting(DtwModel modelToLoad) {
        //TODO: Turn into warning if we can recover gracefully (e.g. different # gestures, features)
        if (! isCompatible(modelToLoad)) {
            throw new IllegalArgumentException("New DTW model is incompatible with this one");
        }
        //But for now, assume # of gestures and # of features is the same.
        
        System.arraycopy(modelToLoad.isGestureActive, 0, isGestureActive, 0, isGestureActive.length);
        
        data.loadFromExisting(modelToLoad.data);
        setSettings(modelToLoad.settings, false);
        maxDistance = modelToLoad.maxDistance;
        
        //TODO: Use a setter for these for GUI?
        modelName = modelToLoad.modelName;
        myID = modelToLoad.myID;
        for (int i = 0; i < versionNumbers.length; i++) {
            setVersionNumber(i, modelToLoad.versionNumbers[i]);
        }
        //setSelectedInputs(modelToLoad.selectedInputs); //NO: This will update distance and increment version number
        boolean[] oldSelectedInputs = selectedInputs;
        selectedInputs = modelToLoad.selectedInputs;
        distanceFunction = new EuclideanDistanceWithInputSelection(selectedInputs);
        updateMaxDistance();
        propertyChangeSupport.firePropertyChange(PROP_SELECTED_INPUTS, oldSelectedInputs, selectedInputs);
    
        //Update stats:
        updateExampleSizeStats();
        setMaxSliderValue(modelToLoad.maxSliderValue);
        setMatchThreshold(modelToLoad.matchThreshold);
    }

    public String toLogInfoString() {
            //Describe as comma-separated description string
        StringBuilder sb = new StringBuilder();
        sb.append("NUM_IN=").append(w.getInputManager().getNumInputs());
        sb.append(",NUM_GESTURES=").append(numGestures).append(",NUM_EXAMPLES={");
        for (int i = 0; i < numGestures-1; i++) { //gestures go from 0 to num_gestures-1
            sb.append(data.getNumExamplesForGesture(i)).append(',');
        }
        sb.append(data.getNumExamplesForGesture(numGestures-1)).append("}");
        sb.append(",ACTIVE={");
        for (int i = 0; i < numGestures-1; i++) { //gestures go from 0 to num_gestures-1
            sb.append(isGestureActive[i] ? "1," : "0,");
        }
        sb.append(isGestureActive[numGestures-1] ? "1}" : "0}");
        return sb.toString();
    }

    public static enum RecordingState {

        RECORDING, NOT_RECORDING
    };

    public static enum RunningState {

        RUNNING, NOT_RUNNING
    };

    private void updateVersionNumber(int whichGesture) {
        int curr = versionNumbers[whichGesture];
        setVersionNumber(whichGesture, ++curr);
    }

    /**
     * Get the value of versionNumbers
     *
     * @return the value of versionNumbers
     */
    public int[] getVersionNumbers() {
        return versionNumbers;
    }

    /**
     * Get the value of versionNumbers at specified index
     *
     * @param index the index of versionNumbers
     * @return the value of versionNumbers at specified index
     */
    public int getVersionNumber(int index) {
        return this.versionNumbers[index];
    }

    private void setVersionNumber(int index, int versionNumber) {
        int oldVersionNumber = this.versionNumbers[index];
        this.versionNumbers[index] = versionNumber;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_VERSIONNUMBERS, index, oldVersionNumber, versionNumbers);
    }

    public void dumpToConsole() {
        System.out.println("DTW MODEL:");
        System.out.println("Threshold: " + matchThreshold);
        settings.dumpToConsole();
    }

    public DtwSettings getSettings() {
        return settings;
    }

    private static void applySettingsToData(DtwSettings settings, DtwData data) {
        DtwSettings.DownsamplePolicy dp = settings.getDownsamplePolicy();
        if (dp == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
            data.setNoDownsample();
        } else if (dp == DtwSettings.DownsamplePolicy.DOWNSAMPLE_BY_CONSTANT_FACTOR) {
            data.setDownsampleByConstantFactor(settings.getDownsampleFactor());
        } else {
            //Downsample to max example length
            data.setDownsampleToMaxLength(settings.getDownsampleMaxLength());
        }
    }

    private void setSettings(DtwSettings settings, boolean updateID) {
        boolean isRunning = (getRunningState() == RunningState.RUNNING);
        if (isRunning) {
            stopRunning();
        }
        //Apply settings 
        RunningType oldRunningType = this.getSettings().getRunningType();
        RunningType newRunningType = settings.getRunningType();
        applySettingsToData(settings, data);

        this.settings = settings;
        updateID();

        //Notify change
        propertyChangeSupport.firePropertyChange(PROP_RUNNING_TYPE, oldRunningType, newRunningType);

        if (isRunning) {
            startRunning();
        }
    }
    
    public void setSettings(DtwSettings settings) {
        setSettings(settings, true);
    }

    /**
     * Get the value of recordingState
     *
     * @return the value of recordingState
     */
    public RecordingState getRecordingState() {
        return recordingState;
    }

    /**
     * Set the value of recordingState
     *
     * @param recordingState new value of recordingState
     */
    private void setRecordingState(RecordingState recordingState) {
        this.recordingState = recordingState;
    }

    /**
     * Get the value of runningState
     *
     * @return the value of runningState
     */
    public RunningState getRunningState() {
        return runningState;
    }

    /**
     * Set the value of runningState
     *
     * @param runningState new value of runningState
     */
    private void setRunningState(RunningState runningState) {
        this.runningState = runningState;
    }

    /**
     * Get the value of currentMatch Matching gesture, or -1 if nothing above
     * threshold
     *
     * @return the value of currentMatch
     */
    public int getCurrentMatch() {
        return currentMatch;
    }

    /**
     * Set the value of currentMatch
     *
     * @param currentMatch new value of currentMatch
     */
    private void setCurrentMatch(int currentMatch) {
        int oldCurrentMatch = this.currentMatch;
        this.currentMatch = currentMatch;
        propertyChangeSupport.firePropertyChange(PROP_CURRENT_MATCH, oldCurrentMatch, currentMatch);
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public boolean isGestureActive(int i) {
        return isGestureActive[i];
    }

    public void setGestureActive(int i, boolean active) {
        isGestureActive[i] = active;
    }

    @Override
    public String getPrettyName() {
        return "Dynamic time warping";
    }

    @Override
    public String getModelDescription() {
        StringBuilder sb = new StringBuilder("Dynamic time warping with ");
        sb.append(numGestures).append(" gesture categories\n");
        sb.append("Match threshold: ").append(matchThreshold).append("\n\n");
        sb.append("SETTINGS:\n").append(settings.toString()).append("\n\n");
        sb.append("DATA SUMMARY:\n").append(data.getSummaryString()).append("\n");
        return sb.toString();
    }

    private void updateID() {
        Date d = new Date();
        String timestamp = Long.toString(d.getTime());
        myID = this.modelName + "_" + timestamp;
    }

    @Override
    public String getUniqueIdentifier() {
        return myID;
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCDtwOutput);
    }

    @Override
    public void writeToOutputStream(ObjectOutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void startRecording(int currentClass) {
        if (recordingState == RecordingState.NOT_RECORDING) {
            wasRunningBeforeRecord = (runningState == RunningState.RUNNING);
            if (wasRunningBeforeRecord) {
                stopRunning();
            }
            data.startRecording(currentClass);
            setRecordingState(RecordingState.RECORDING);
        } else {
            logger.log(Level.WARNING, "Already recording for gesture " + currentClass);
        }
    }

    public void stopRecording() {
        if (recordingState == RecordingState.RECORDING) {
            setRecordingState(RecordingState.NOT_RECORDING);
            data.stopRecording();
        }
        if (wasRunningBeforeRecord) {
            startRunning();
            wasRunningBeforeRecord = false;
        }
        // updateID();
    }

    public void startRunning() {
        if (runningState != RunningState.RUNNING) {
            data.startRunning();
            if (settings.getRunningType() == RunningType.LABEL_CONTINUOUSLY) {
                updateExampleSizeStats();
            } else {
                data.setMaxLengthToRetainDuringRun(Integer.MAX_VALUE); //hang onto everything
            }
            setRunningState(RunningState.RUNNING);
        }
    }

    public void stopRunning() {
        if (runningState == RunningState.RUNNING) {
            if (settings.getRunningType() == RunningType.LABEL_ONCE_PER_RECORD) {
                classifyLast();
            }
            setRunningState(RunningState.NOT_RUNNING);
            KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.DTW_RUN_STOP);
        }
    }

    public void runOnVector(double[] inputs) {
        boolean wasAdded = data.addRunningVector(inputs);
        if (settings.getRunningType() == RunningType.LABEL_CONTINUOUSLY && wasAdded) {
            classifyContinuous();
            KadenzeLogging.getLogger().dtwRunData(w, inputs, closestDistances, currentMatch);
        }
    }

    public void addDtwUpdateListener(DtwUpdateListener1 listener) {
        if (listener != null) {
            updateListeners.add(listener);
        }
    }

    public void addNormalizedDtwUpdateListener(DtwUpdateListener1 listener) {
        if (listener != null) {
            normalizedUpdateListeners.add(listener);
        }
    }

    public void removeDtwUpdateListener(DtwUpdateListener1 listener) {
        if (listener != null) {
            updateListeners.remove(listener);
        }
    }

    public void removeNormalizedDtwUpdateListener(DtwUpdateListener1 listener) {
        if (listener != null) {
            normalizedUpdateListeners.remove(listener);
        }
    }

    public void classifyLast() {
        double closestDist = Double.MAX_VALUE;
        int closestClass = -1;
        for (int i = 0; i < closestDistances.length; i++) {
            closestDistances[i] = Double.MAX_VALUE;
        }

        TimeSeries currentTs = data.getCurrentTimeSeries();

        for (int whichClass = 0; whichClass < numGestures; whichClass++) {
            if (isGestureActive[whichClass]) {
                for (DtwExample ex : data.getExamplesForGesture(whichClass)) {
                    //for (TimeSeries ts : allseries.get(whichClass)) {
                    TimeSeries ts;
                    if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                        ts = ex.getTimeSeries();
                    } else {
                        ts = ex.getDownsampledTimeSeries();
                    }

                    //TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts, currentTs, settings.getMatchWidth());
                    double dist = FastDTW.getWarpDistBetween(ts, currentTs, settings.getMatchWidth(), distanceFunction);
                    
                    if (closestDistances[whichClass] > dist) {
                        closestDistances[whichClass] = dist;
                    }
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestClass = whichClass;
                    }
                }
            }
        }

        //Doesn't use threshold
        //if (closestDist < matchThreshold) {
        setCurrentMatch(closestClass);
        //} else {
        //    setCurrentMatch(0);
        //}
        notifyUpdateListeners(closestDistances);
        computeAndNotifyNormalizedDistances();
        KadenzeLogging.getLogger().dtwClassifiedLast(w, data.getCurrentTimeSeries(), closestDistances, closestClass);
    }

    /**
     * Get the value of maxDistance
     *
     * @return the value of maxDistance
     */
    public double getMaxDistance() {
        return maxDistance;
    }

    /**
     * Set the value of maxDistance
     *
     * @param maxDistance new value of maxDistance
     */
    public void setMaxDistance(double maxDistance) {
        double oldMaxDistance = this.maxDistance;
        this.maxDistance = maxDistance;
        propertyChangeSupport.firePropertyChange(PROP_MAX_DISTANCE, oldMaxDistance, maxDistance);
        updateMaxSliderValue();
    }

    private void updateMaxSliderValue() {
        double newDist = maxDistance;
        if (newDist > matchThreshold) {
            ///setMaxSliderValue((int) (100 * newDist) + 1); //No longer updates matchThreshold
            setMaxSliderValue((int) (200 * newDist) + 1);
        } else {
            //setMaxSliderValue((int) (100 * matchThreshold) + 1); //No longer updates matchThreshold; this effectively takes current match threshold and makes it slider max which is not so cool
            setMaxSliderValue((int) (500 * matchThreshold) + 1);
        }
       // System.out.println("Slider max set to " + getMaxSliderValue() * .01);
    }

    private void updateMaxDistance() {
        //Update threshold info
        double maxDist = 0.0;

        int numClassesWithExamples = 0;
        int whichClass = -1;

        for (int i = 0; i < numGestures; i++) {
            //For now, include inactive classes here.
            List<DtwExample> list1 = data.getExamplesForGesture(i);
            if (list1.size() > 0) {
                numClassesWithExamples++;
                whichClass = i;
            }

            for (int j = i + 1; j < numGestures; j++) {
                List<DtwExample> list2 = data.getExamplesForGesture(j);
                //Find max distance between list1 and list2
                for (DtwExample ex1 : list1) {
                    for (DtwExample ex2 : list2) {
                        //TODO: Sometimes error here: XXX
                        /* Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: -1
                         at com.dtw.SearchWindow.markVisited(SearchWindow.java:288)
                         at com.dtw.SearchWindow.expandSearchWindow(SearchWindow.java:197)
                         at com.dtw.SearchWindow.expandWindow(SearchWindow.java:132)
                         */
                        //  TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts1.getTimeSeries(), ts2.getTimeSeries(), settings.getMatchWidth());
                        TimeSeries ts1, ts2;
                        if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                            ts1 = ex1.getTimeSeries();
                            ts2 = ex2.getTimeSeries();
                        } else {
                            ts1 = ex1.getDownsampledTimeSeries();
                            ts2 = ex2.getDownsampledTimeSeries();
                        }

                        double dist = FastDTW.getWarpDistBetween(ts1, ts2, settings.getMatchWidth(), distanceFunction);

                        if (dist > maxDist) {
                            maxDist = dist;
                        }
                    }
                }
            }
        }

        if (numClassesWithExamples == 1) {
            List<DtwExample> list = data.getExamplesForGesture(whichClass);
            for (int i = 0; i < list.size(); i++) {
                TimeSeries ts1;
                if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                    ts1 = list.get(i).getTimeSeries();
                } else {
                    ts1 = list.get(i).getDownsampledTimeSeries();
                }
                //TimeSeries ts1 = list.get(i).getTimeSeries();
                for (int j = i + 1; j < list.size(); j++) {
                    //TimeSeries ts2 = list.get(j).getTimeSeries();

                    TimeSeries ts2;
                    if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                        ts2 = list.get(j).getTimeSeries();
                    } else {
                        ts2 = list.get(j).getDownsampledTimeSeries();
                    }

                    // TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts1, ts2, settings.getMatchWidth());
                    double dist = FastDTW.getWarpDistBetween(ts1, ts2, settings.getMatchWidth(), distanceFunction);

                    if (dist > maxDist) {
                        maxDist = dist;
                    }
                }
            }
            maxDist = maxDist * 10; // just in case
        }
       // System.out.println("MAX DIST updated to " + maxDist);
        setMaxDistance(maxDist);
    }

    protected void updateExampleSizeStats() {
        minSizeInExamples = data.getMinSizeInExamples();
        maxSizeInExamples = data.getMaxSizeInExamples();
        minSizeInDownsampledExamples = data.getMinSizeInDownsampledExamples();
        maxSizeInDownsampledExamples = data.getMaxSizeInDownsampledExamples();
        if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
            data.setMaxLengthToRetainDuringRun(maxSizeInExamples);
        } else {
            data.setMaxLengthToRetainDuringRun(maxSizeInDownsampledExamples);
        }
    }

    private double[] getClassificationVector() {
          double[] d = new double[numGestures];
          
        //Chop to sizes between minSizeInExamples, min(current ts size, maxSizeInExamples)
        // and look for best match.

        int min;
        if (settings.getMinimumLengthRestriction() == DtwSettings.MinimumLengthRestriction.SET_FROM_EXAMPLES) {
            if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                min = minSizeInExamples;
            } else {
                min = minSizeInDownsampledExamples;
            }
        } else {
            min = settings.getMinAllowedGestureLength();
            /*if (min > minSizeInExamples) {
             min = minSizeInExamples; //this means 1 very short example can break everything: disallow
             } */
        }
        //System.out.println("Classifying with min=" + min);

        int max;
        if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
            max = maxSizeInExamples;
        } else {
            max = maxSizeInDownsampledExamples;
        }

        List<TimeSeries> l = data.getCandidateSeriesFromCurrentRun(min, max, settings.getHopSizeForContinuousSearch());
       // System.out.println("min/Max:" + min + max);
        
        
        double closestDist = Double.MAX_VALUE;
        int closestClass = -1;

        for (int i = 0; i < closestDistances.length; i++) {
            d[i] = Double.MAX_VALUE;
        }

        for (int whichClass = 0; whichClass < numGestures; whichClass++) {
            if (isGestureActive[whichClass]) {
                for (TimeSeries candidate : l) {
                    for (DtwExample ex : data.getExamplesForGesture(whichClass)) {
                        TimeSeries ts;
                        if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                            ts = ex.getTimeSeries();
                        } else {
                            ts = ex.getDownsampledTimeSeries();
                        }

                        //for (TimeSeries ts : allseries.get(whichClass)) {
                        // TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts, candidate, settings.getMatchWidth()); //used to be 5 instead of matchWidth
                        double dist = FastDTW.getWarpDistBetween(ts, candidate, settings.getMatchWidth(), distanceFunction);

                        if (d[whichClass] > dist) {
                            d[whichClass] = dist;
                        }
                        if (dist < closestDist) {
                            closestDist = dist;
                            closestClass = whichClass;
                        }
                    }
                }
            }
        }
        /*if (closestDist < matchThreshold) {
            setCurrentMatch(closestClass);
        } else {
            setCurrentMatch(-1);
        } */
        //notifyUpdateListeners(closestDistances);
        //computeAndNotifyNormalizedDistances();
        return d;
    }

    
    private void classifyContinuous() {
        //Chop to sizes between minSizeInExamples, min(current ts size, maxSizeInExamples)
        // and look for best match.

        int min;
        if (settings.getMinimumLengthRestriction() == DtwSettings.MinimumLengthRestriction.SET_FROM_EXAMPLES) {
            if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                min = minSizeInExamples;
            } else {
                min = minSizeInDownsampledExamples;
            }
        } else {
            min = settings.getMinAllowedGestureLength();
            /*if (min > minSizeInExamples) {
             min = minSizeInExamples; //this means 1 very short example can break everything: disallow
             } */
        }
        //System.out.println("Classifying with min=" + min);

        int max;
        if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
            max = maxSizeInExamples;
        } else {
            max = maxSizeInDownsampledExamples;
        }

        List<TimeSeries> l = data.getCandidateSeriesFromCurrentRun(min, max, settings.getHopSizeForContinuousSearch());
       // System.out.println("min/Max:" + min + max);
        
        
        double closestDist = Double.MAX_VALUE;
        int closestClass = -1;

        for (int i = 0; i < closestDistances.length; i++) {
            closestDistances[i] = Double.MAX_VALUE;
        }

        for (int whichClass = 0; whichClass < numGestures; whichClass++) {
            if (isGestureActive[whichClass]) {
                for (TimeSeries candidate : l) {
                    for (DtwExample ex : data.getExamplesForGesture(whichClass)) {
                        TimeSeries ts;
                        if (settings.getDownsamplePolicy() == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
                            ts = ex.getTimeSeries();
                        } else {
                            ts = ex.getDownsampledTimeSeries();
                        }

                        //for (TimeSeries ts : allseries.get(whichClass)) {
                        // TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts, candidate, settings.getMatchWidth()); //used to be 5 instead of matchWidth
                        double dist = FastDTW.getWarpDistBetween(ts, candidate, settings.getMatchWidth(), distanceFunction);

                        if (closestDistances[whichClass] > dist) {
                            closestDistances[whichClass] = dist;
                        }
                        if (dist < closestDist) {
                            closestDist = dist;
                            closestClass = whichClass;
                        }
                    }
                }
            }
        }
        if (closestDist < matchThreshold) {
            setCurrentMatch(closestClass);
        } else {
            setCurrentMatch(-1);
        }
        notifyUpdateListeners(closestDistances);
        computeAndNotifyNormalizedDistances();
    }

    private void computeAndNotifyNormalizedDistances() {
        double max = getMaxSliderValue() * .01;
        int min = 0;
        for (int i = 0; i < closestDistances.length; i++) {

            //distance is scale from 0 --> maxSlider*.01
            //mapped to 100 -> 0 (i.e., left is furthest)
            double thisDist = (1 - (closestDistances[i] / max)) * 100;
            if (thisDist < 0) {
                thisDist = 0;
            }

            normalizedDistances[i] = thisDist;
        }
        notifyNormalizedUpdateListeners(normalizedDistances);
    }

    private void notifyUpdateListeners(double[] closest) {
        for (DtwUpdateListener1 l : updateListeners) {
            l.dtwUpdateReceived(closest);
        }
    }

    private void notifyNormalizedUpdateListeners(double[] closest) {
        for (DtwUpdateListener1 l : normalizedUpdateListeners) {
            l.dtwUpdateReceived(closest);
        }
    }

    public DtwData getData() {
        return data;
    }

    public interface DtwUpdateListener1 {

        public void dtwUpdateReceived(double[] currentDistances);
    }

    /**
     * Get the value of gestureNames at specified index
     *
     * @param index the index of gestureNames
     * @return the value of gestureNames at specified index
     */
    public String getGestureName(int index) {
        return myOutput.getGestureNames(index);
    }

    /**
     * Set the value of gestureNames at specified index.
     *
     * @param index the index of gestureNames
     * @param gestureNames new value of gestureNames at specified index
     */
    /*public void setGestureName(int index, String gestureNames) {
        String oldGestureNames = this.gestureNames[index];
        this.gestureNames[index] = gestureNames;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_GESTURE_NAMES, index, oldGestureNames, gestureNames);
    } */

    private void numGesturesChanged(int newNumGestures) {
        boolean wasRunning = false;
        if (runningState == RunningState.RUNNING) {
            wasRunning = true;
            stopRunning();
        }
        if (recordingState == RecordingState.RECORDING) {
            stopRecording();
        }
        numGestures = newNumGestures;
        data.setNumGestures(newNumGestures);
        if (wasRunning) {
            startRunning();
        }
    }
    
    public static DtwModel readFromFile(String filename) throws IOException {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        DtwModel m = (DtwModel) Util.readFromXMLFile("DtwModel", DtwModel.class, filename);
        return m;
    }
    
}
