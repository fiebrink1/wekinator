/*
 * Executes basic control over learning actions
 */
package wekimini;

import wekimini.kadenze.KadenzeLogging;

/**
 *
 * @author rebecca
 */
public class WekinatorDtwLearningController {
    private final DtwLearningManager m;
    private final Wekinator w;
    
    public WekinatorDtwLearningController(DtwLearningManager m, Wekinator w) {
        this.m = m;
        this.w = w;
    }

    //REQUIRES that it is legal to move to record state at this time
    public void startRecord(int whichGesture) {
        if (whichGesture <= 0 || whichGesture > m.getModel().getNumGestures()) {
            w.getStatusUpdateCenter().warn(this, "Illegal DTW gesture number " + 
                    whichGesture + ": must be between 1 and " +m.getModel().getNumGestures() );
            return;
        } 
        if (m.getRunningState() == DtwLearningManager.RunningState.RUNNING) {
            stopRun();
        }
        m.startRecording(whichGesture-1);
        w.getStatusUpdateCenter().update(this, "Recording for gesture " + whichGesture + ": waiting for inputs to arrive");
    }
    
    public void stopRecord() {
        m.stopRecording();
        // setStatus("Examples recorded. Press \"Train\" to build models from data.");
        w.getStatusUpdateCenter().update(this, "Recording stopped");

    }

    public boolean isRunning() {
        return (m.getRunningState() == DtwLearningManager.RunningState.RUNNING);
    }

    public void startRun() {
        if (m.getRunningState() == DtwLearningManager.RunningState.NOT_RUNNING) {
            m.startRunning();
           // KadenzeLogging.getLogger().logStartDtwRun(w);
            w.getStatusUpdateCenter().update(this, "Running - waiting for inputs to arrive");
        }
    }

    public void stopRun() {
        m.stopRunning();
        w.getStatusUpdateCenter().update(this, "Running stopped");
    }

    /*public void deleteAllExamples() {
        m.deleteAllExamples();
    } */
    
    public boolean canRun() {
        return m.canRun();
    }  
}
