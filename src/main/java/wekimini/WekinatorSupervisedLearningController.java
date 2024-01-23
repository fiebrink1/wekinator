/*
 * Executes basic control over learning actions
 */
package wekimini;

import java.util.List;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.kadenze.KadenzeUtils;

/**
 *
 * @author rebecca
 */
public class WekinatorSupervisedLearningController {
    private final SupervisedLearningManager m;
    private final Wekinator w;
    private long start =0;

    
    public WekinatorSupervisedLearningController(SupervisedLearningManager m, Wekinator w) {
        this.m = m;
        this.w = w;
    }

    //REQUIRES that it is legal to move to record state at this time
    public void startRecord() {
        if (m.getRunningState() == SupervisedLearningManager.RunningState.RUNNING) {
            stopRun();
        }
        m.startRecording();
        w.getStatusUpdateCenter().update(this, "Recording...");
    }
    
    public void stopRecord() {
        m.stopRecording();
        // setStatus("Examples recorded. Press \"Train\" to build models from data.");
        w.getStatusUpdateCenter().update(this, 
        m.getNumExamplesThisRound() + " new examples recorded");

    }

    public boolean isTraining() {
        return m.getLearningState() == SupervisedLearningManager.LearningState.TRAINING;
    }
    
    public boolean isRunning() {
        return (m.getRunningState() == SupervisedLearningManager.RunningState.RUNNING);
    }
    
    public boolean isRecording() {
        return (m.getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING);
    }
    
    public void train() {
        if (m.getLearningState() != SupervisedLearningManager.LearningState.TRAINING) {
            m.buildAll();
            w.getStatusUpdateCenter().update(this, "Training");
        }
    }

    public void cancelTrain() {
        m.cancelTraining();
        w.getStatusUpdateCenter().update(this, "Cancelling training");
    }

    public void startRun() {
        if (m.getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING) {
            m.stopRecording();
        }
        if (m.getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING) {
           m.setRunningState(SupervisedLearningManager.RunningState.RUNNING);
           KadenzeLogging.getLogger().logStartSupervisedRun(w);
           if (WekiMiniRunner.isKadenze()) {
               startRunTimer();
           }
           w.getStatusUpdateCenter().update(this, "Running...");
        }
    }

    public void stopRun() {
        m.setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.RUN_STOP);
        if (WekiMiniRunner.isKadenze()) {
            String s = "Running stopped (Run time = " + getRunTime() + "s)";
            w.getStatusUpdateCenter().update(this, s);
        } else {
            w.getStatusUpdateCenter().update(this, "Running stopped");
        }
    }
    
    public void startRunTimer() {
        start = System.currentTimeMillis();
    }

    public String getRunTime() {
        long now = System.currentTimeMillis();
        double runTime = (now - start) / 1000.0;
        return KadenzeUtils.formatDouble(runTime);
    }
  

    /*public void deleteAllExamples() {
        m.deleteAllExamples();
    } */

    //Requires modelNum be a valid output (starting from 1)
    public void setModelRecordEnabled(int modelNum, boolean enableRecord) {
        try {
            List<Path> ps = m.getPaths();
            Path p = ps.get(modelNum - 1);
            p.setRecordEnabled(enableRecord);
            if (enableRecord) {
                w.getStatusUpdateCenter().update(this, "Enabled model recording");
            } else {
                w.getStatusUpdateCenter().update(this, "Disabled model recording");
            }

        } catch (Exception ex) {
            w.getStatusUpdateCenter().warn(this, "Unable to change record state for output " + modelNum);
        }
    }

    //Requires modelNum be a valid output (starting from 1)
    public void setModelRunEnabled(int modelNum, boolean enableRun) {
        try {
            List<Path> ps = m.getPaths();
            Path p = ps.get(modelNum - 1);
            p.setRunEnabled(enableRun);
            if (enableRun) {
                w.getStatusUpdateCenter().update(this, "Enabled model running");
            } else {
                w.getStatusUpdateCenter().update(this, "Disabled model running");
            }
        } catch (Exception ex) {
            w.getStatusUpdateCenter().warn(this, "Unable to change record state for output " + modelNum);
        }
    }

    public boolean canRecord() {
        return m.isAbleToRecord();
    }

    public boolean canTrain() {
        SupervisedLearningManager.LearningState ls = m.getLearningState();
        return (ls == SupervisedLearningManager.LearningState.DONE_TRAINING ||
                ls == SupervisedLearningManager.LearningState.READY_TO_TRAIN);
    }
    
    public boolean canRun() {
        return m.isAbleToRun();
    }  
}
