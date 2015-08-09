/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static wekimini.DtwLearningManager.RunningState.NOT_RUNNING;
import wekimini.learning.dtw.DtwData;
import wekimini.learning.dtw.DtwModel;
import wekimini.osc.OSCDtwOutput;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
public class DtwLearningManager implements ConnectsInputsToOutputs {

    private DtwModel model; // In future could make more than 1
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private String[] gestureNames;

    public static final String PROP_GESTURE_NAMES = "gestureNames";

    private int[] versionNumbers;

    public static final String PROP_VERSIONNUMBERS = "versionNumbers";
    private final Wekinator w;

     public DtwLearningManager(Wekinator w, OSCOutputGroup group) {
         this.w = w;
        if (group.getNumOutputs() != 1) {
            throw new IllegalArgumentException("Only one dtw output allowed right now");
        }

        /* for (int i = 0; i < group.getNumOutputs(); i++) {
         OSCDtwOutput out = (OSCDtwOutput)group.getOutput(i);
         int numGestures = out.getNumGestures();
         DtwModel model = new DtwModel(out.getName(), numGestures, w, null);
         models.add(model);
         } */
        OSCDtwOutput out = (OSCDtwOutput) group.getOutput(0);
        int numGestures = out.getNumGestures();
        model = new DtwModel(out.getName(), numGestures, w, this, null);
        String[] newNames = new String[numGestures];
        int[] newVersionNumbers = new int[numGestures];
        for (int i = 0; i < numGestures; i++) {
            newNames[i] = out.getName() + "_" + i;
            newVersionNumbers[i] = 1;
        }
        setGestureNames(newNames);
        setVersionNumbers(newVersionNumbers);

        final DtwData data = model.getData();
        data.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DtwData.PROP_NUMTOTALEXAMPLES)) {
                    if (data.getNumTotalExamples() == 0) {
                        setCanRun(false);
                    } else {
                        setCanRun(true);
                    }
                }
            }
        });
        w.getInputManager().addInputValueListener(new InputManager.InputListener() {

            @Override
            public void update(double[] vals) {
                updateInputs(vals);
            }

            @Override
            public void notifyInputError() {
            }
        });
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
     * Set the value of versionNumbers
     *
     * @param versionNumbers new value of versionNumbers
     */
    public void setVersionNumbers(int[] versionNumbers) {
        int[] oldVersionNumbers = this.versionNumbers;
        this.versionNumbers = versionNumbers;
        propertyChangeSupport.firePropertyChange(PROP_VERSIONNUMBERS, oldVersionNumbers, versionNumbers);
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

    /**
     * Set the value of versionNumbers at specified index.
     *
     * @param index the index of versionNumbers
     * @param versionNumbers new value of versionNumbers at specified index
     */
    public void setVersionNumbers(int index, int versionNumbers) {
        int oldVersionNumbers = this.versionNumbers[index];
        this.versionNumbers[index] = versionNumbers;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_VERSIONNUMBERS, index, oldVersionNumbers, versionNumbers);
    }

    /**
     * Get the value of gestureNames
     *
     * @return the value of gestureNames
     */
    public String[] getGestureNames() {
        return gestureNames;
    }

    /**
     * Set the value of gestureNames
     *
     * @param gestureNames new value of gestureNames
     */
    public void setGestureNames(String[] gestureNames) {
        String[] oldGestureNames = this.gestureNames;
        this.gestureNames = gestureNames;
        propertyChangeSupport.firePropertyChange(PROP_GESTURE_NAMES, oldGestureNames, gestureNames);
    }

    /**
     * Get the value of gestureNames at specified index
     *
     * @param index the index of gestureNames
     * @return the value of gestureNames at specified index
     */
    public String getGestureNames(int index) {
        return this.gestureNames[index];
    }

    /**
     * Set the value of gestureNames at specified index.
     *
     * @param index the index of gestureNames
     * @param gestureNames new value of gestureNames at specified index
     */
    public void setGestureNames(int index, String gestureNames) {
        String oldGestureNames = this.gestureNames[index];
        this.gestureNames[index] = gestureNames;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_GESTURE_NAMES, index, oldGestureNames, gestureNames);
    }

    public DtwModel getModel() {
        return model;
    }

    public String getGestureName(int whichGesture) {
        return gestureNames[whichGesture];
    }

    public void setGestureEnabled(int gestureNum, boolean e) {
        model.setGestureActive(gestureNum, e);
    }

    public boolean getGestureEnabled(int gestureNum) {
        return model.isGestureActive(gestureNum);
    }

    public void deleteExamplesForGesture(int gestureNum) {
        model.deleteExamplesForGesture(gestureNum);
    }

    public int getNumGestures() {
        return model.getNumGestures();
    }

    public int getRecordingRound() {
        //XXX fix this
        return 0;
    }

    public int getNumDeletedTrainingRound() {
        //XXX
        return 0;
    }

    public void reAddDeletedTrainingRound() {
        //XXX
    }

    public int getNumExamplesInRound(int lastRound) {
        //XXX
            return 0;
    }

    public void deleteTrainingRound(int lastRoundAdvertised) {
        ///XXX
        
    }

    public int getNumActiveInputs() {
        //XXX feature selection
        return w.getInputManager().getNumInputs();
    }

    public static enum RunningState {

        RUNNING, NOT_RUNNING
    };
    private boolean canRun = false;

    public static final String PROP_CANRUN = "canRun";


    /*    private RunningState runningState = NOT_RUNNING; */
    private RunningState runningState = NOT_RUNNING;
    public static final String PROP_RUNNING_STATE = "runningState";

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
        RunningState oldRunningState = this.runningState;
        this.runningState = runningState;
        propertyChangeSupport.firePropertyChange(PROP_RUNNING_STATE, oldRunningState, runningState);
    }

    public void startRunning() {
        if (runningState != RunningState.RUNNING) {
            model.startRunning();
            setRunningState(RunningState.RUNNING);
        }
    }

    public void stopRunning() {
        if (runningState == RunningState.RUNNING) {
            model.stopRunning();
            setRunningState(RunningState.NOT_RUNNING);
        }
    }

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

   
    private void updateInputs(double[] vals) {
        if (model.getRecordingState() == DtwModel.RecordingState.RECORDING) {
            model.addTrainingVector(vals);
        } else if (runningState == RunningState.RUNNING) {
            model.addRunningVector(vals);
        }
    }

    //Need functions that GUI can call to start/stop running, update run mask, ...
    @Override
    public void addConnectionsListener(InputOutputConnectionsListener l) {
        //XXX
    }

    @Override
    public boolean[][] getConnectionMatrix() {
        ///XXX
        return new boolean[0][0];
    }

    @Override
    public boolean removeConnectionsListener(InputOutputConnectionsListener l) {
        return true;
        //XXX
    }

    @Override
    public void updateInputOutputConnections(boolean[][] newConnections) {
        //XXX
    }

    void deleteAllExamples() {
        /* for (DtwModel m : models) {
         m.deleteAllExamples();
         } */
        model.deleteAllExamples();
    }

    public DtwData getData() {
        return model.getData();
    }

    /**
     * Get the value of canRun
     *
     * @return the value of canRun
     */
    public boolean canRun() {
        return canRun;
    }

    /**
     * Set the value of canRun
     *
     * @param canRun new value of canRun
     */
    private void setCanRun(boolean canRun) {
        boolean oldCanRun = this.canRun;
        this.canRun = canRun;
        propertyChangeSupport.firePropertyChange(PROP_CANRUN, oldCanRun, canRun);
    }

    boolean isLegalTrainingValue(int whichOutput, float value) {
        if (whichOutput != 0) {
            throw new IllegalArgumentException("Invalid output " + whichOutput);
        }
        return (value < gestureNames.length);
    }

}
