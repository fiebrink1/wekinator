/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning.dtw;

import com.dtw.TimeWarpInfo;
import com.timeseries.TimeSeries;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instance;
import wekimini.DtwLearningManager;
import wekimini.Wekinator;
import static wekimini.learning.dtw.DtwModel.RecordingState.NOT_RECORDING;
import static wekimini.learning.dtw.DtwModel.RunningState.NOT_RUNNING;
import wekimini.learning.dtw.DtwSettings.RunningType;
import wekimini.learning.Model;
import wekimini.osc.OSCOutput;

/*
 * allow enable/disable of each gesture in set
 properties: min size, max size of matchable gesture
 fires events: when classification results updated (i.e. distance to each), when
 new min classification result received
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

    private final transient List<DtwUpdateListener> updateListeners = new LinkedList<>();
    private final transient List<DtwUpdateListener> normalizedUpdateListeners = new LinkedList<>();
    private transient int currentMatch = -1;
    private final boolean[] isGestureActive;
    private final DtwData data;
    private transient TimeSeries currentTs;
    private int numGestures = 0;
    private final transient double[] closestDistances;
    private final transient double[] normalizedDistances;
    private transient int minSizeInExamples = 0;
    private transient int maxSizeInExamples = 0;
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
        System.out.println("SETTING MATCH THRESHOLD TO " + matchThreshold);
        double oldMatchThreshold = this.matchThreshold;
        this.matchThreshold = matchThreshold;
        propertyChangeSupport.firePropertyChange(PROP_MATCHTHRESHOLD, oldMatchThreshold, matchThreshold);
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

    
    
    public void deleteExamplesForGesture(int gestureNum) {
        data.deleteExamplesForGesture(gestureNum);
    }

    public void deleteMostRecentExample(int gestureNum) {
        data.deleteMostRecentExample(gestureNum);
    }

    public int getNumGestures() {
        return numGestures;
    }
    
    public static enum RecordingState {

        RECORDING, NOT_RECORDING
    };

    public static enum RunningState {

        RUNNING, NOT_RUNNING
    };
    

    public void dumpToConsole() {
        System.out.println("DTW MODEL:");
        System.out.println("Threshold: "+ matchThreshold); 
        settings.dumpToConsole();
    }



    private RecordingState recordingState = NOT_RECORDING;
    private RunningState runningState = NOT_RUNNING;

    public DtwSettings getSettings() {
        return settings;
    }

    public void setSettings(DtwSettings settings) {
        boolean isRunning = (getRunningState() == RunningState.RUNNING);
        if (isRunning) {
            stopRunning();
        }
        RunningType oldRunningType = this.getSettings().getRunningType();
        RunningType newRunningType = settings.getRunningType();
        this.settings = settings;
        updateIdentifier();
        propertyChangeSupport.firePropertyChange(PROP_RUNNING_TYPE, oldRunningType, newRunningType);
        if (isRunning) {
            startRunning();
        }
    }

    //Settings can optionally be null
    public DtwModel(String name, int numGestures, Wekinator w, DtwLearningManager m, DtwSettings settings) {
        this.modelName = name;
        if (settings != null) {
            this.settings = settings;
        } else {
            this.settings = new DtwSettings();
        }

        this.numGestures = numGestures;
        this.w = w;

        closestDistances = new double[numGestures];
        normalizedDistances = new double[numGestures];
        isGestureActive = new boolean[numGestures];
        for (int i = 0; i < isGestureActive.length; i++) {
            isGestureActive[i] = true;
            closestDistances[i] = Double.MAX_VALUE;
            normalizedDistances[i] = 0;
        }
        data = new DtwData(numGestures, m, w);
        data.addDataListener(new DtwData.DtwDataListener() {

            @Override
            public void numExamplesChanged(int whichClass, int currentNumExamples) {
                updateMaxDistance();
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
        updateIdentifier();
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
     * Get the value of currentMatch
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
    public void setCurrentMatch(int currentMatch) {
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

    /**
     * Get the value of maxAllowedGestureLength
     *
     * @return the value of maxAllowedGestureLength
     */
    /*public int getMaxAllowedGestureLength() {
     return maxAllowedGestureLength;
     }*/
    /**
     * Set the value of maxAllowedGestureLength
     *
     * @param maxAllowedGestureLength new value of maxAllowedGestureLength
     */
    /* public void setMaxAllowedGestureLength(int maxAllowedGestureLength) {
     this.maxAllowedGestureLength = maxAllowedGestureLength;
     } */
    public boolean isGestureActive(int i) {
        return isGestureActive[i];
    }

    public void setGestureActive(int i, boolean active) {
        isGestureActive[i] = active;
    }

    @Override
    public String getPrettyName() {
        return modelName;
    }

    @Override
    public String getModelDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double computeOutput(Instance inputs) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void updateIdentifier() {
        Date d = new Date();
        String timestamp = Long.toString(d.getTime());
        myID = this.modelName + "_" + timestamp;
    }

    @Override
    public String getUniqueIdentifier() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeToOutputStream(ObjectOutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addTrainingVector(double[] inputs) {
        data.addTrainingVector(inputs);
    }

    public void startRecording(int currentClass) {
        if (recordingState == RecordingState.NOT_RECORDING) {
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
        updateIdentifier();
    }

    public List<DtwExample> getExamplesForGesture(int which) {
        return data.getExamplesForGesture(which);
    }

    public void deleteExample(int id) {
        data.delete(id);
        updateIdentifier();
    }

    public void deleteAllExamples() {
        data.deleteAll();
        updateIdentifier();
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
        }
    }

    public void addRunningVector(double[] inputs) {
        data.addRunningVector(inputs);
        if (settings.getRunningType() == RunningType.LABEL_CONTINUOUSLY) {
            classifyContinuous();
        }
    }

    public void addDtwUpdateListener(DtwUpdateListener listener) {
        if (listener != null) {
            updateListeners.add(listener);
        }
    }
    
    public void addNormalizedDtwUpdateListener(DtwUpdateListener listener) {
        if (listener != null) {
            normalizedUpdateListeners.add(listener);
        }
    }

    public void removeDtwUpdateListener(DtwUpdateListener listener) {
        if (listener != null) {
            updateListeners.remove(listener);
        }
    }
    
    public void removeNormalizedDtwUpdateListener(DtwUpdateListener listener) {
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

        for (int whichClass = 0; whichClass < numGestures; whichClass++) {
            if (isGestureActive[whichClass]) {
                for (DtwExample ex : data.getExamplesForGesture(whichClass)) {
                    //for (TimeSeries ts : allseries.get(whichClass)) {
                    TimeSeries ts = ex.getTimeSeries();
                    TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts, currentTs, settings.getMatchWidth());
                    if (closestDistances[whichClass] > info.getDistance()) {
                        closestDistances[whichClass] = info.getDistance();
                    }
                    if (info.getDistance() < closestDist) {
                        closestDist = info.getDistance();
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
            setMaxSliderValue((int) (100 * newDist) + 1);
        } else {
            setMaxSliderValue((int) (100 * matchThreshold) + 1);
        }
        System.out.println("Slider max set to " + getMaxSliderValue() * .01);

    }

    protected void updateMaxDistance() {
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
                for (DtwExample ts1 : list1) {
                    for (DtwExample ts2 : list2) {
                        //TODO: Sometimes error here: XXX
                        /* Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: -1
	at com.dtw.SearchWindow.markVisited(SearchWindow.java:288)
	at com.dtw.SearchWindow.expandSearchWindow(SearchWindow.java:197)
	at com.dtw.SearchWindow.expandWindow(SearchWindow.java:132)
                                */
                        TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts1.getTimeSeries(), ts2.getTimeSeries(), settings.getMatchWidth());
                        if (info.getDistance() > maxDist) {
                            maxDist = info.getDistance();
                        }
                    }
                }
            }
        }

        if (numClassesWithExamples == 1) {
            List<DtwExample> list = data.getExamplesForGesture(whichClass);
            for (int i = 0; i < list.size(); i++) {
                TimeSeries ts1 = list.get(i).getTimeSeries();
                for (int j = i + 1; j < list.size(); j++) {
                    TimeSeries ts2 = list.get(j).getTimeSeries();
                    TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts1, ts2, settings.getMatchWidth());
                    if (info.getDistance() > maxDist) {
                        maxDist = info.getDistance();
                    }
                }
            }
            maxDist = maxDist * 10; // just in case
        }
        System.out.println("MAX DIST updated to " + maxDist);
        setMaxDistance(maxDist);
    }

    protected void updateExampleSizeStats() {
        minSizeInExamples = data.getMinSizeInExamples();
        maxSizeInExamples = data.getMaxSizeInExamples();
        data.setMaxLengthToRetainDuringRun(maxSizeInExamples);
    }

    private void classifyContinuous() {
        //Chop to sizes between minSizeInExamples, min(current ts size, maxSizeInExamples)
        // and look for best match.

        int min = settings.getMinAllowedGestureLength();
        if (min > minSizeInExamples) {
            min = minSizeInExamples;
        }

        List<TimeSeries> l = data.getCandidateSeriesFromCurrentRun(min, maxSizeInExamples, settings.getHopSizeForContinuousSearch());

        double closestDist = Double.MAX_VALUE;
        int closestClass = -1;

        for (int i = 0; i < closestDistances.length; i++) {
            closestDistances[i] = Double.MAX_VALUE;
        }

        for (int whichClass = 0; whichClass < numGestures; whichClass++) {
            if (isGestureActive[whichClass]) {
                for (TimeSeries candidate : l) {
                    for (DtwExample ex : data.getExamplesForGesture(whichClass)) {
                        TimeSeries ts = ex.getTimeSeries();
                        //for (TimeSeries ts : allseries.get(whichClass)) {
                        TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts, candidate, settings.getMatchWidth()); //used to be 5 instead of matchWidth
                        if (closestDistances[whichClass] > info.getDistance()) {
                            closestDistances[whichClass] = info.getDistance();
                        }
                        if (info.getDistance() < closestDist) {
                            closestDist = info.getDistance();
                            closestClass = whichClass;
                        }
                    }
                }
            }
        }
        if (closestDist < matchThreshold) {
            setCurrentMatch(closestClass);
        } else {
            setCurrentMatch(0);
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
        for (DtwUpdateListener l : updateListeners) {
            l.dtwUpdateReceived(closest);
        }
    }
    
    private void notifyNormalizedUpdateListeners(double[] closest) {
        for (DtwUpdateListener l : normalizedUpdateListeners) {
            l.dtwUpdateReceived(closest);
        }
    }
    
    public DtwData getData() {
        return data;
    }

    public interface DtwUpdateListener {

        public void dtwUpdateReceived(double[] currentDistances);
    }

}
