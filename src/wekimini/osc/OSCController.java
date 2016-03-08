/*
 * TODO: Testing
 * Test that doesn't break even at early stages of wekinator (before project initialized)
 */
package wekimini.osc;

import com.illposed.osc.OSCMessage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import wekimini.LearningManager;
import wekimini.LearningManager.LearningType;
import wekimini.Wekinator;
import wekimini.WekinatorController;
import wekimini.WekinatorDtwLearningController;
import wekimini.WekinatorSupervisedLearningController;

/**
 *
 * @author rebecca
 */
public class OSCController {
    //TODO: Clean this up, only adding supervised listeners if in supervised state
    
    private final Wekinator w;
    private final OSCControlReceiver controlReceiver;
    private boolean oscControlEnabled = true;
    private WekinatorSupervisedLearningController supervisedController;
    private WekinatorDtwLearningController dtwController;
    private WekinatorController wekController;
    private boolean isSupervised = false;

    public OSCController(Wekinator w) {
        this.w = w;
        controlReceiver = new OSCControlReceiver(w, this);
        wekController = w.getWekinatorController();
        if (w.getLearningManager().getLearningType() == LearningManager.LearningType.SUPERVISED_LEARNING) {
            isSupervised = true;
            supervisedController = w.getLearningManager().getSupervisedLearningManager().getSupervisedLearningController();
        } else {
            isSupervised = false;
            w.getLearningManager().addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(LearningManager.PROP_LEARNINGTYPE)) {
                        learningTypeChnaged((LearningManager.LearningType) evt.getNewValue());
                    }
                }
            });
        }

    }

    private void learningTypeChnaged(LearningManager.LearningType learningType) {
        if (learningType == LearningType.SUPERVISED_LEARNING) {
            isSupervised = true;
            supervisedController = w.getLearningManager().getSupervisedLearningManager().getSupervisedLearningController();
        } else {
            isSupervised = false;
            dtwController = w.getLearningManager().getDtwLearningManager().getDtwLearningController();
                    
        }
    }

    public boolean getOscControlEnabled() {
        return oscControlEnabled;
    }

    protected boolean checkEnabled() {
        if (getOscControlEnabled()) {
            return true;
        } else {
            w.getStatusUpdateCenter().warn(this, "OSC control message received, but ignoring it because OSC control is not enabled");
            return false;
        }
    }

    public void setOscControlEnabled(boolean enable) {
        oscControlEnabled = enable;
    }

    public void startRecord() {
        if (checkEnabled() && isSupervised) {
            if (supervisedController.canRecord()) {
               supervisedController.startRecord();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC record command but cannot record in this state");
            }
        }
    }

    public void stopRecord() {
        if (checkEnabled() && isSupervised) {
            if (supervisedController.isRecording()) {
                supervisedController.stopRecord();
            }
        }
    }

    public void train() {
        if (checkEnabled() && isSupervised) {
            if (supervisedController.canTrain()) {
                supervisedController.train();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC train command but cannot train in this state");
            }
        }
    }

    public void cancelTrain() {
        if (checkEnabled() && isSupervised) {
            if (supervisedController.isTraining()) {
                supervisedController.cancelTrain();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC cancel train command but Wekinator is not currently training");
            }
        }
    }

    public void startRun() {
        //TODO: put start/stop run in wekinator controller
        if (checkEnabled() && isSupervised) {
            if (supervisedController.canRun()) {
                supervisedController.startRun();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC run command but cannot run in this state");
            }
        } else if (checkEnabled() && !isSupervised) {
            if (dtwController.canRun()) {
                dtwController.startRun();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC run command but cannot run in this state");
            }
        } 
    }

    public void stopRun() {
        if (checkEnabled() && isSupervised) {
            if (supervisedController.isRunning()) {
                supervisedController.stopRun();
            }
        } else if (checkEnabled() && !isSupervised) {
            if (dtwController.isRunning()) {
                dtwController.stopRun();
            }
        }
    }

    public void deleteAllExamples() {
        if (checkEnabled()) {
            w.getWekinatorController().deleteAllExamples();
        }
    }

    //modelNum between 1 and numOutputs
    public void setModelRecordEnabled(int modelNum, boolean enableRecord) {
        if (checkEnabled() && isSupervised) {
            supervisedController.setModelRecordEnabled(modelNum, enableRecord);
        }
    }

    //modelNum between 1 and numOutputs
    public void setModelRunEnabled(int modelNum, boolean enableRun) {
        if (checkEnabled() && isSupervised) {
            supervisedController.setModelRunEnabled(modelNum, enableRun);
        }
    }

    //Requires legal length of String array (same as num inputs)
    public void setInputNames(String[] inputNames) {
        w.getWekinatorController().setInputNames(inputNames);
    }

    //Requires legal length of String array (same as num outputs)
    public void setOutputNames(String[] outputNames) {
        w.getWekinatorController().setOutputNames(outputNames);
    }

    //Requires legal length of boolean array (same as num inputs); outputNum between 1 and numOutputs
    //Also requires inputs and outputs to already be set up
    public void setInputSelectionForOutput(int[] whichInputs, int outputNum) {
        w.getWekinatorController().setInputsForOutput(whichInputs, outputNum);
    }

    void startDtwRecord(int whichGesture) {
        dtwController.startRecord(whichGesture);
        
    }

    void stopDtwRecord() {
        dtwController.stopRecord();
    }

    void deleteExamplesForOutput(Integer i) {
        if (checkEnabled()) {
            w.getWekinatorController().deleteExamplesForOutput(i);
        }
    }

}
