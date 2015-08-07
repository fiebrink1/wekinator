/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import com.dtw.TimeWarpInfo;
import com.timeseries.TimeSeries;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import weka.core.Instance;
import wekimini.Wekinator;
import static wekimini.learning.DtwModel.RecordingState.NOT_RECORDING;
import static wekimini.learning.DtwModel.RunningState.NOT_RUNNING;
import static wekimini.learning.DtwModel.RunningType.LABEL_CONTINUOUSLY;
import wekimini.osc.OSCDtwOutput;
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

    private final transient List<DtwUpdateListener> updateListeners = new LinkedList<>();

    private double matchThreshold = 0.3;
    private int minAllowedGestureLength = 5;
   // private int maxAllowedGestureLength = 10;

    private transient int currentMatch = -1;
    private final boolean[] isGestureActive;
    private final OSCDtwOutput myOutput;
    private final DtwData data;
    private transient TimeSeries currentTs;
    private int numGestures = 0;
    private final transient double[] closestDistances;
    private transient int minSizeInExamples = 0;
    private transient int maxSizeInExamples = 0;

    public static final String PROP_CURRENT_MATCH = "currentMatch";

    public static enum RecordingState {

        RECORDING, NOT_RECORDING
    };

    public static enum RunningState {

        RUNNING, NOT_RUNNING
    };

    public static enum RunningType {

        LABEL_CONTINUOUSLY, LABEL_ONCE_PER_RECORD
    }

    private RecordingState recordingState = NOT_RECORDING;
    private RunningState runningState = NOT_RUNNING;

    private double maxDistance = 0;
    public static final String PROP_MAX_DISTANCE = "maxDistance";

    public transient final Wekinator w;

    private RunningType runningType = LABEL_CONTINUOUSLY;

    public static final String PROP_RUNNING_TYPE = "runningType";

    private int hopSizeForContinuousSearch = 1;

    /**
     * Get the value of hopSizeForContinuousSearch
     *
     * @return the value of hopSizeForContinuousSearch
     */
    public int getHopSizeForContinuousSearch() {
        return hopSizeForContinuousSearch;
    }

    /**
     * Set the value of hopSizeForContinuousSearch
     *
     * @param hopSizeForContinuousSearch new value of hopSizeForContinuousSearch
     */
    public void setHopSizeForContinuousSearch(int hopSizeForContinuousSearch) {
        this.hopSizeForContinuousSearch = hopSizeForContinuousSearch;
    }

    /**
     * Get the value of runningType
     *
     * @return the value of runningType
     */
    public RunningType getRunningType() {
        return runningType;
    }

    /**
     * Set the value of runningType
     *
     * @param runningType new value of runningType
     */
    public void setRunningType(RunningType runningType) {
        boolean isRunning = (getRunningState() == RunningState.RUNNING);
        if (isRunning) {
            stopRunning();
        }
        RunningType oldRunningType = this.runningType;
        this.runningType = runningType;
        propertyChangeSupport.firePropertyChange(PROP_RUNNING_TYPE, oldRunningType, runningType);
        if (isRunning) {
            startRunning();
        }

    }

    public DtwModel(OSCDtwOutput o, Wekinator w) {
        this.myOutput = o;
        this.w = w;
        this.numGestures = o.getNumGestures();
        closestDistances = new double[numGestures];
        isGestureActive = new boolean[numGestures];
        for (int i = 0; i < isGestureActive.length; i++) {
            isGestureActive[i] = true;
            closestDistances[i] = Double.MAX_VALUE;
        }
        data = new DtwData(numGestures, w);
        data.addDataListener(new DtwData.DtwDataListener() {

            @Override
            public void numExamplesChanged(int whichClass, int currentNumExamples) {
                updateMaxDistance();
            }
        });
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

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

    private int matchWidth = 5;

    /**
     * Get the value of matchWidth
     *
     * @return the value of matchWidth
     */
    public int getMatchWidth() {
        return matchWidth;
    }

    /**
     * Set the value of matchWidth
     *
     * @param matchWidth new value of matchWidth
     */
    public void setMatchWidth(int matchWidth) {
        this.matchWidth = matchWidth;
    }

    /**
     * Get the value of minAllowedGestureLength
     *
     * @return the value of minAllowedGestureLength
     */
    public int getMinAllowedGestureLength() {
        return minAllowedGestureLength;
    }

    /**
     * Set the value of minAllowedGestureLength
     *
     * @param minAllowedGestureLength new value of minAllowedGestureLength
     */
    public void setMinAllowedGestureLength(int minAllowedGestureLength) {
        this.minAllowedGestureLength = minAllowedGestureLength;
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
        this.matchThreshold = matchThreshold;
    }

    @Override
    public String getPrettyName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModelDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double computeOutput(Instance inputs) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            setRecordingState(RecordingState.RECORDING);
            data.startRecording(currentClass);
        }
    }

    public void stopRecording() {
        if (recordingState == RecordingState.RECORDING) {
            setRecordingState(RecordingState.NOT_RECORDING);
            data.stopRecording();
        }
    }

    public void startRunning() {
        if (runningState != RunningState.RUNNING) {
            data.startRunning();
            if (runningType == RunningType.LABEL_CONTINUOUSLY) {
                updateExampleSizeStats();
            } else {
                data.setMaxLengthToRetainDuringRun(Integer.MAX_VALUE); //hang onto everything
            }
            setRunningState(RunningState.RUNNING);
        }
    }

    public void stopRunning() {
        if (runningState == RunningState.RUNNING) {
            if (runningType == RunningType.LABEL_ONCE_PER_RECORD) {
                classifyLast();
            }
            setRunningState(RunningState.NOT_RUNNING);
        }
    }

    public void addRunningVector(double[] inputs) {
        data.addRunningVector(inputs);
        if (runningType == LABEL_CONTINUOUSLY) {
            classifyContinuous();
        }
    }

    public void addDtwUpdateListener(DtwUpdateListener listener) {
        if (listener != null) {
            updateListeners.add(listener);
        }
    }

    public void removeDtwUpdateListener(DtwUpdateListener listener) {
        if (listener != null) {
            updateListeners.remove(listener);
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
                    TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts, currentTs, matchWidth);
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
                        TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts1.getTimeSeries(), ts2.getTimeSeries(), matchWidth);
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
                    TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts1, ts2, matchWidth);
                    if (info.getDistance() > maxDist) {
                        maxDist = info.getDistance();
                    }
                }
            }
            maxDist = maxDist * 10; // just in case
        }
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

        int min = minAllowedGestureLength;
        if (min > minSizeInExamples) {
            min = minSizeInExamples;
        }

        List<TimeSeries> l = data.getCandidateSeriesFromCurrentRun(min, maxSizeInExamples, hopSizeForContinuousSearch);

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
                        TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(ts, candidate, matchWidth); //used to be 5 instead of matchWidth
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
    }

    private void notifyUpdateListeners(double[] closest) {
        for (DtwUpdateListener l : updateListeners) {
            l.dtwUpdateReceived(closest);
        }
    }

    public interface DtwUpdateListener {
        public void dtwUpdateReceived(double[] currentDistances);
    }

}
