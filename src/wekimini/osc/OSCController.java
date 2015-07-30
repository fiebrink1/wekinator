/*
 * TODO: Testing
 * Test that doesn't break even at early stages of wekinator (before project initialized)
 */
package wekimini.osc;

import wekimini.Wekinator;

/**
 *
 * @author rebecca
 */
public class OSCController {
    private final Wekinator w;
    private final OSCControlReceiver controlReceiver;
    private boolean oscControlEnabled = true;

    public OSCController(Wekinator w) {
       this.w = w; 
       controlReceiver = new OSCControlReceiver(w, this);
    }
    
    public boolean getOscControlEnabled() {
        return oscControlEnabled;
    }
    
    protected boolean checkEnabled() {
        if (getOscControlEnabled())  {
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
        if (checkEnabled()) {
            if (w.getWekinatorController().canRecord()) {
                w.getWekinatorController().startRecord();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC record command but cannot record in this state");
            }
        }
    }
    
    public void stopRecord() {
        if (checkEnabled()) {
            if (w.getWekinatorController().isRecording()) {
                w.getWekinatorController().stopRecord();
            }
        }
    }
    
    public void train() {
        if (checkEnabled()) {
            if (w.getWekinatorController().canTrain()) {
                w.getWekinatorController().train();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC train command but cannot train in this state");
            }
        }
    }
    
    public void cancelTrain() {
        if (checkEnabled()) {
            if (w.getWekinatorController().isTraining()) {
                w.getWekinatorController().cancelTrain();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC cancel train command but Wekinator is not currently training");
            }
        }
    }
    
    public void startRun() {
        if (checkEnabled()) {
            if (w.getWekinatorController().canRun()) {
                w.getWekinatorController().startRun();
            } else {
                w.getStatusUpdateCenter().warn(this, "Recieved OSC run command but cannot run in this state");
            }
        }
    }
    
    public void stopRun() {
        if (checkEnabled()) {
            if (w.getWekinatorController().isRunning()) {
                w.getWekinatorController().stopRun();
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
        if (checkEnabled()) {
            w.getWekinatorController().setModelRecordEnabled(modelNum, enableRecord);
        }
    }
    
    //modelNum between 1 and numOutputs
    public void setModelRunEnabled(int modelNum, boolean enableRun) {
        if (checkEnabled()) {
            w.getWekinatorController().setModelRunEnabled(modelNum, enableRun);
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


}
