/*
 * Executes basic control over learning actions
 */
package wekimini;

import wekimini.osc.OSCController;


/**
 *
 * @author rebecca
 */
public class WekinatorController {
    private final Wekinator w;
    private final OSCController oscController;
    
    public WekinatorController(Wekinator w) {
        this.w = w;
        oscController = new OSCController(w);
    }
    
    public boolean isOscControlEnabled() {
        return oscController.getOscControlEnabled();
    }
    
    public void setOscControlEnabled(boolean enabled) {
        oscController.setOscControlEnabled(enabled);
    }

    //REQUIRES that it is legal to move to record state at this time
    public void startRecord() {
        if (w.getLearningManager().getRunningState() == LearningManager.RunningState.RUNNING) {
            stopRun();
        }
        w.getLearningManager().startRecording();
        w.getStatusUpdateCenter().update(this, "Recording - waiting for inputs to arrive");
    }
    
    public void stopRecord() {
        w.getLearningManager().stopRecording();
        // setStatus("Examples recorded. Press \"Train\" to build models from data.");
        w.getStatusUpdateCenter().update(this, 
                w.getLearningManager().getNumExamplesThisRound() + " new examples recorded");

    }

    public boolean isTraining() {
        return w.getLearningManager().getLearningState() == LearningManager.LearningState.TRAINING;
    }
    
    public boolean isRunning() {
        return (w.getLearningManager().getRunningState() == LearningManager.RunningState.RUNNING);
    }
    
    public boolean isRecording() {
        return (w.getLearningManager().getRecordingState() == LearningManager.RecordingState.RECORDING);
    }
    
    public void train() {
        if (w.getLearningManager().getLearningState() != LearningManager.LearningState.TRAINING) {
            w.getLearningManager().buildAll();
            w.getStatusUpdateCenter().update(this, "Training");
        }
    }

    public void cancelTrain() {
        w.getLearningManager().cancelTraining();
        w.getStatusUpdateCenter().update(this, "Cancelling training");
    }

    public void startRun() {
        if (w.getLearningManager().getRecordingState() == LearningManager.RecordingState.RECORDING) {
            w.getLearningManager().stopRecording();
        }
        if (w.getLearningManager().getRunningState() == LearningManager.RunningState.NOT_RUNNING) {
           w.getLearningManager().setRunningState(LearningManager.RunningState.RUNNING);
           w.getStatusUpdateCenter().update(this, "Running - waiting for inputs to arrive");
        }
    }

    public void stopRun() {
        w.getLearningManager().setRunningState(LearningManager.RunningState.NOT_RUNNING);
        w.getStatusUpdateCenter().update(this, "Running stopped");
    }

    public void deleteAllExamples() {
        w.getLearningManager().deleteAllExamples();
    }

    public void setModelRecordEnabled(int modelNum, boolean enableRecord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setModelRunEnabled(int modelNum, boolean enableRecord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean canRecord() {
        return w.getLearningManager().isAbleToRecord();
    }

    public boolean canTrain() {
        LearningManager.LearningState ls = w.getLearningManager().getLearningState();
        return (ls == LearningManager.LearningState.DONE_TRAINING ||
                ls == LearningManager.LearningState.READY_TO_TRAIN);
    }
    
    public boolean canRun() {
        return w.getLearningManager().isAbleToRun();
    }  
}
