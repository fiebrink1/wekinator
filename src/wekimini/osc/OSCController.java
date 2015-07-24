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
    
    private boolean checkEnabled() {
        if (getOscControlEnabled())  {
            return true;
        } else {
            w.getStatusUpdateCenter().update(this, "OSC control message received, but ignoring it because OSC control is not enabled");
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
                w.getStatusUpdateCenter().update(this, "Recieved OSC record command but cannot record in this state");
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
                w.getStatusUpdateCenter().update(this, "Recieved OSC train command but cannot train in this state");
            }
        }
    }
    
    public void cancelTrain() {
        if (checkEnabled()) {
            if (w.getWekinatorController().isTraining()) {
                w.getWekinatorController().cancelTrain();
            } else {
                w.getStatusUpdateCenter().update(this, "Recieved OSC cancel train command but Wekinator is not currently training");
            }
        }
    }
    
    public void startRun() {
        if (checkEnabled()) {
            if (w.getWekinatorController().canRun()) {
                w.getWekinatorController().startRun();
            } else {
                w.getStatusUpdateCenter().update(this, "Recieved OSC run command but cannot run in this state");
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
    public void setModelRunEnabled(int modelNum, boolean enableRecord) {
        if (checkEnabled()) {
            w.getWekinatorController().setModelRunEnabled(modelNum, enableRecord);
        }
    }
    
    //Requires legal length of String array (same as num inputs)
    public void setInputNames(String[] inputNames) {
        
    }
    
    //Requires legal length of String array (same as num outputs)
    public void setOutputNames(String[] outputNames) {
        
    }
    
    //Requires legal length of boolean array (same as num inputs); outputNum between 1 and numOutputs
    public void setInputSelectionForOutput(boolean[] isInputSelected, int outputNum) {
        
    }


}
