/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import static wekimini.DtwLearningManager.RunningState.NOT_RUNNING;
import wekimini.learning.DtwData;
import wekimini.learning.DtwModel;
import wekimini.learning.DtwSettings;
import wekimini.osc.OSCDtwOutput;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
public class DtwLearningManager implements ConnectsInputsToOutputs {
    private DtwModel model; // In future could make more than 1
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
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
    
    public DtwLearningManager(Wekinator w, OSCOutputGroup group) {
        if (group.getNumOutputs() != 1) {
            throw new IllegalArgumentException("Only one dtw output allowed right now");
        }
        
       /* for (int i = 0; i < group.getNumOutputs(); i++) {
            OSCDtwOutput out = (OSCDtwOutput)group.getOutput(i);
            int numGestures = out.getNumGestures();
            DtwModel model = new DtwModel(out.getName(), numGestures, w, null);
            models.add(model);
        } */
        OSCDtwOutput out = (OSCDtwOutput)group.getOutput(0);
        int numGestures = out.getNumGestures();
        model = new DtwModel(out.getName(), numGestures, w, null);
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
    }
    
    //Need functions that GUI can call to start/stop running, update run mask, ...
    @Override
    public void addConnectionsListener(InputOutputConnectionsListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean[][] getConnectionMatrix() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeConnectionsListener(InputOutputConnectionsListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateInputOutputConnections(boolean[][] newConnections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
