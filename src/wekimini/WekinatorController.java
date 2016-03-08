/*
 * Executes basic control over learning actions
 */
package wekimini;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.LearningManager.LearningType;
import wekimini.osc.OSCController;

/**
 *
 * @author rebecca
 */
public class WekinatorController {
    private final Wekinator w;
    private final OSCController oscController;
    private final List<NamesListener> inputNamesListeners;
    private final List<NamesListener> outputNamesListeners;
    private static final Logger logger = Logger.getLogger(WekinatorController.class.getName());
    public WekinatorController(Wekinator w) {
        this.w = w;
        oscController = new OSCController(w);
        inputNamesListeners = new LinkedList<>();
        outputNamesListeners = new LinkedList<>();
    }
    
    public boolean isOscControlEnabled() {
        return oscController.getOscControlEnabled();
    }
    
    public void setOscControlEnabled(boolean enabled) {
        oscController.setOscControlEnabled(enabled);
    }


    /*public boolean isTraining() {
        return w.getSupervisedLearningManager().getLearningState() == SupervisedLearningManager.LearningState.TRAINING;
    }
    
    public boolean isRunning() {
        return (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.RUNNING);
    }
    
    public boolean isRecording() {
        return (w.getSupervisedLearningManager().getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING);
    }
    
    public void train() {
        if (w.getSupervisedLearningManager().getLearningState() != SupervisedLearningManager.LearningState.TRAINING) {
            w.getSupervisedLearningManager().buildAll();
            w.getStatusUpdateCenter().update(this, "Training");
        }
    }

    public void cancelTrain() {
        w.getSupervisedLearningManager().cancelTraining();
        w.getStatusUpdateCenter().update(this, "Cancelling training");
    }

    public void startRun() {
        if (w.getSupervisedLearningManager().getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING) {
            w.getSupervisedLearningManager().stopRecording();
        }
        if (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING) {
           w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
           w.getStatusUpdateCenter().update(this, "Running - waiting for inputs to arrive");
        }
    }

    public void stopRun() {
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getStatusUpdateCenter().update(this, "Running stopped");
    }*/

    public void deleteAllExamples() {
        w.getLearningManager().deleteAllExamples();
    }

    //Requires modelNum be a valid output (starting from 1)
   /* public void setModelRecordEnabled(int modelNum, boolean enableRecord) {
        try {
            List<Path> ps = w.getSupervisedLearningManager().getPaths();
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
            List<Path> ps = w.getSupervisedLearningManager().getPaths();
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
        return w.getSupervisedLearningManager().isAbleToRecord();
    }

    public boolean canTrain() {
        SupervisedLearningManager.LearningState ls = w.getSupervisedLearningManager().getLearningState();
        return (ls == SupervisedLearningManager.LearningState.DONE_TRAINING ||
                ls == SupervisedLearningManager.LearningState.READY_TO_TRAIN);
    }
    
    public boolean canRun() {
        return w.getSupervisedLearningManager().isAbleToRun();
    }  */

    //Requires either no input group set up yet, or length matches # inputs
    //Listeners can register in case wekinator isn't set up yet (e.g. initial project config screen)
    public void setInputNames(String[] inputNames) {
        if (w.getInputManager().hasValidInputs()) {
            //w.getInputManager().getOSCInputGroup().setInputNames(inputNames);
            //TODO: Fix this, with attention to how Path, SupervisedLearningManager use input names.
            w.getStatusUpdateCenter().warn(this, "Input names cannot be changed after project is created (will be implemented in later version)");
        } else {
            notifyNewInputNames(inputNames);
        }
    }
    
    //Requires either no output group set up yet, or length matches # outputs
    //Listeners can register in case wekinator isn't set up yet (e.g. initial project config screen)
    public void setOutputNames(String[] outputNames) {
        if (w.getOutputManager().hasValidOutputGroup()) {
           // w.getOutputManager().getOutputGroup().setOutputNames(outputNames);
           w.getStatusUpdateCenter().warn(this, "Output names cannot be changed after project is created (will be implemented in later version)");
        } else {
            notifyNewOutputNames(outputNames);
        }
    }
    
    //Requires whichInputs and outputNum are valid
    //WhichInputs and outputNum are indexed from 1 here
    public void setInputsForOutput(int[] whichInputs, int outputNum) {
        boolean[][] currentConnections = w.getLearningManager().getConnectionMatrix();
        boolean[][] newConnections = new boolean[currentConnections.length][currentConnections[0].length];
        //currentConnections[i][j] is true if input i is connected to output j
        for (int i = 0; i < currentConnections.length; i++) {
            System.arraycopy(currentConnections[i], 0, newConnections[i], 0, currentConnections[i].length);
            newConnections[i][outputNum-1] = false;
        }
        for (int i = 0; i < whichInputs.length; i++) {
            newConnections[whichInputs[i]-1][outputNum-1] = true;
        }
        w.getLearningManager().updateInputOutputConnections(newConnections);
    }

    
    public void addInputNamesListener(NamesListener l) {
        inputNamesListeners.add(l);
    }

    public boolean removeInputNamesListener(NamesListener l) {
        return inputNamesListeners.remove(l);
    }
    
    public void addOutputNamesListener(NamesListener l) {
        outputNamesListeners.add(l);
    }

    public boolean removeOutputNamesListener(NamesListener l) {
        return outputNamesListeners.remove(l);
    }
    
    /* public void addConnectionsListener(InputOutputConnectionListener l) {
        inputOutputConnectionsListeners.add(l);
    }

    public boolean removeConnectionsListener(InputOutputConnectionListener l) {
        return inputOutputConnectionsListeners.remove(l);
    } */
    
    private void notifyNewInputNames(String[] newNames) {
        for (NamesListener nl: inputNamesListeners) {
            nl.newNamesReceived(newNames);
        }
    }
    
    private void notifyNewOutputNames(String[] newNames) {
        for (NamesListener nl: outputNamesListeners) {
            nl.newNamesReceived(newNames);
        }
    }
    
   /* private void notifyNewInputOutputConnections(boolean[][] connections) {
        for (InputOutputConnectionListener l: inputOutputConnectionsListeners) {
            l.newConnectionMatrix(connections);
        }
    } */

    //Delete examples for model i, starting with i = 0
    public void deleteExamplesForOutput(Integer i) {
        if (w.getLearningManager().getLearningType() == LearningType.SUPERVISED_LEARNING) {
           if (i >= 1 && i <= w.getSupervisedLearningManager().getPaths().size()) {
                w.getSupervisedLearningManager().deleteExamplesForPath(w.getSupervisedLearningManager().getPaths().get(i-1));
           } else {
               logger.log(Level.WARNING, "Invalid output index {0}: index needs to be between 1 and number of outputs", i);
           }
        } else {
            if (i >= 1 && i <= w.getDtwLearningManager().getModel().getNumGestures()) {
                w.getDtwLearningManager().getModel().getData().deleteExamplesForGesture(i-1);
            } else {
                logger.log(Level.WARNING, "Invalid output index {0}: index needs to be between 1 and number of DTW gestures", i);
            }
        }
    }
    
    
    public interface NamesListener {
        public void newNamesReceived(String[] names);
    }
    

    
}
