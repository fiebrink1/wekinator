/*
 * Handles incoming OSC control messages
 */
package wekimini.osc;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.logging.Level;
import wekimini.Wekinator;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCControlReceiver {

    private final Wekinator w;
    private final OSCController controller;
    private final PropertyChangeListener oscReceiverListener;
    private final String startRecordMessage = "/wekinator/control/startRecording";
    private final String stopRecordMessage = "/wekinator/control/stopRecording";
    private final String trainMessage = "/wekinator/control/train";
    private final String cancelTrainingMessage = "/wekinator/control/cancelTrain";
    private final String startRunningMessage = "/wekinator/control/startRunning";
    private final String stopRunningMessage = "/wekinator/control/stopRunning";
    private final String deleteAllExamplesMessage = "/wekinator/control/deleteAllExamples";
    private final String enableModelRecordMessage = "/wekinator/control/enableModelRecording"; //List of model #s to enable (indexed from 1) 
    private final String disableModelRecordMessage = "/wekinator/control/disableModelRecording"; //List of model #s to disable (indexed from 1) 
    private final String enableModelRunMessage = "/wekinator/control/enableModelRunning"; //List of model #s to enable (indexed from 1) 
    private final String disableModelRunMessage = "/wekinator/control/disableModelRunning"; //List of model #s to disable (indexed from 1) 

   // private final String setModelRecordEnabledMessage = "/wekinator/control/setModelRecordEnabled"; //1st argument model # (starting from 1), 2nd argument record boolean (0/1))
    //  private final String setModelRunEnabledMessage = "/wekinator/control/setModelRunEnabled"; //1st argument model # (starting from 1), 2nd argument run boolean (0/1))
    private final String setInputNamesMessage = "/wekinator/control/setInputNames";
    private final String setOutputNamesMessage = "/wekinator/control/setOutputNames";
    private final String setInputSelectionForOutputMessage = "/wekinator/control/selectInputsForOutput";

    public OSCControlReceiver(Wekinator w, OSCController controller) {
        this.w = w;
        this.controller = controller;

        oscReceiverListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscReceiverPropertyChanged(evt);
            }
        };
        //oscReceiverListener = this::oscReceiverPropertyChanged;
        w.getOSCReceiver().addPropertyChangeListener(oscReceiverListener);
    }

    private void oscReceiverPropertyChanged(PropertyChangeEvent e) {
        if (e.getPropertyName() == OSCReceiver.PROP_CONNECTIONSTATE) {
            if (e.getNewValue() == OSCReceiver.ConnectionState.CONNECTED) {
                addOSCControlListeners();
            }
        }
    }

    private void addOSCControlListeners() {
        OSCListener startRecordListener = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                controller.startRecord();
            }
        };
        w.getOSCReceiver().addOSCListener(startRecordMessage, startRecordListener);

        OSCListener stopRecordListener = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                controller.stopRecord();
            }
        };
        w.getOSCReceiver().addOSCListener(stopRecordMessage, stopRecordListener);

        OSCListener startRunListener = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                controller.startRun();
            }
        };
        w.getOSCReceiver().addOSCListener(startRunningMessage, startRunListener);

        OSCListener stopRunListener = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                controller.stopRun();
            }
        };
        w.getOSCReceiver().addOSCListener(stopRunningMessage, stopRunListener);

        OSCListener trainListener = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                controller.train();
            }
        };
        w.getOSCReceiver().addOSCListener(trainMessage, trainListener);

        OSCListener cancelTrainListener = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                controller.cancelTrain();
            }
        };
        w.getOSCReceiver().addOSCListener(cancelTrainingMessage, cancelTrainListener);

        OSCListener deleteAllExamplesListener = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                controller.deleteAllExamples();
            }
        };
        w.getOSCReceiver().addOSCListener(deleteAllExamplesMessage, deleteAllExamplesListener);
        w.getOSCReceiver().addOSCListener(enableModelRecordMessage, createModelChangeListener(true, true));
        w.getOSCReceiver().addOSCListener(disableModelRecordMessage, createModelChangeListener(true, false));
        w.getOSCReceiver().addOSCListener(enableModelRunMessage, createModelChangeListener(false, true));
        w.getOSCReceiver().addOSCListener(disableModelRunMessage, createModelChangeListener(false, false));

        w.getOSCReceiver().addOSCListener(setInputNamesMessage, createInputNamesListener());
        w.getOSCReceiver().addOSCListener(setOutputNamesMessage, createOutputNamesListener());
        w.getOSCReceiver().addOSCListener(setInputSelectionForOutputMessage, createInputSelectionListener());
    }

    private OSCListener createModelChangeListener(final boolean isRecord, final boolean isEnable) {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }
                
                Object[] o = oscm.getArguments();
                if (o.length == 0) {
                    w.getStatusUpdateCenter().update(this,
                            "Error: OSC message " + enableModelRecordMessage
                            + " requires a list of model numbers", Level.WARNING);
                    return;
                }
                int[] modelNumbers = new int[o.length];
                for (int i = 0; i < o.length; i++) {
                    if (o[i] instanceof Number) {
                        int whichModel = ((Number) o[i]).intValue();
                        if (whichModel < 1 || whichModel > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                            w.getStatusUpdateCenter().update(this,
                                    "Error: Model numbers must be between 1 and # models for OSC message " + enableModelRecordMessage, Level.WARNING);
                            return;
                        }
                        modelNumbers[i] = whichModel;

                    } else {
                        w.getStatusUpdateCenter().update(this,
                                "Error: Non-numeric model number given for OSC message " + enableModelRecordMessage, Level.WARNING);
                        return;
                    }
                }
                for (int i = 0; i < modelNumbers.length; i++) {
                    if (isRecord) {
                        controller.setModelRecordEnabled(modelNumbers[i], isEnable);
                    } else {
                        controller.setModelRunEnabled(modelNumbers[i], isEnable);
                    }
                }
            }
        };
        return l;
    }

    private OSCListener createInputNamesListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }
                
                Object[] o = oscm.getArguments();
                try {
                    if (w.getInputManager().hasValidInputs() && o.length != w.getInputManager().getNumInputs()) {
                        w.getStatusUpdateCenter().update(this,
                                "Error: Received wrong number of input names "
                                + "(received " + o.length + ", expected " + w.getInputManager().getNumInputs() + ")",
                                Level.WARNING);
                        return;
                    }

                    String[] names = new String[o.length];
                    for (int i = 0; i < o.length; i++) {
                        if (!(o[i] instanceof String)) {
                            w.getStatusUpdateCenter().update(this,
                                    "Error: Received non-string argument(s) for OSC message " + oscm, Level.WARNING);
                            return;
                        }
                        names[i] = (String) o[i];
                    }
                    if (Util.checkAllUnique(names)) {
                        controller.setInputNames(names);
                    } else {
                        w.getStatusUpdateCenter().update(this,
                                "Error: Input names not unique (received via OSC message " + setInputNamesMessage
                                + ")", Level.WARNING);
                    }
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    private OSCListener createOutputNamesListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }
                
                Object[] o = oscm.getArguments();
                try {
                    if (w.getOutputManager().hasValidOutputGroup() && o.length != w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().update(this,
                                "Error: Received wrong number of output names "
                                + "(received " + o.length + ", expected " + w.getOutputManager().getOutputGroup().getNumOutputs() + ")",
                                Level.WARNING);
                        return;
                    }

                    String[] names = new String[o.length];
                    for (int i = 0; i < o.length; i++) {
                        if (!(o[i] instanceof String)) {
                            w.getStatusUpdateCenter().update(this,
                                    "Error: Received non-string argument(s) for OSC message " + oscm, Level.WARNING);
                            return;
                        }
                        names[i] = (String) o[i];
                    }
                    if (Util.checkAllUnique(names)) {
                        controller.setOutputNames(names);
                    } else {
                        w.getStatusUpdateCenter().update(this,
                                "Error: Output names not unique (received via OSC message " + setOutputNamesMessage
                                + ")", Level.WARNING);
                    }
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    //TODO Make use of this if it comes in early in project setup!
    private OSCListener createInputSelectionListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }
                
                Object[] o = oscm.getArguments();
                try {
                    //Check if we can reasonably do something with this:
                    if (!w.getInputManager().hasValidInputs() || !w.getOutputManager().hasValidOutputGroup()) {
                       w.getStatusUpdateCenter().update(this,
                                "Error: Cannot create connections between inputs and outputs: these are not yet set up",
                                Level.WARNING);
                        return; 
                    } 
                    
                    if (o.length < 2) {
                        w.getStatusUpdateCenter().update(this,
                                "Error: OSC message " + setInputSelectionForOutputMessage + " requires output ID followed by at least one selected input ID",
                                Level.WARNING);
                        return; 
                    }
                    
                    int[] vals = unpackToInts(o, o.length, setInputSelectionForOutputMessage);
                    
                    int outputNum = vals[0];
                    if (outputNum < 1 || outputNum > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().update(this,
                                "Error: OSC message " + setInputSelectionForOutputMessage
                                + " requires first argument to be output ID (between 1 and "
                                + w.getOutputManager().getOutputGroup().getNumOutputs() + ")",
                                Level.WARNING);
                        return;
                    }

                    int[] whichInputs = new int[vals.length - 1];
                    for (int i = 0; i < vals.length - 1; i++) {
                        int nextInput = vals[i + 1];
                        if (nextInput < 1 || nextInput > w.getInputManager().getNumInputs()) {
                            w.getStatusUpdateCenter().update(this,
                                    "Error: OSC message " + setInputSelectionForOutputMessage
                                    + " requires list of input IDs to be selected, with each ID between 1 and # inputs ("
                                    + w.getInputManager().getNumInputs() + ")",
                                    Level.WARNING);
                            return;
                        }
                        whichInputs[i] = nextInput;
                    }
                    controller.setInputSelectionForOutput(whichInputs, outputNum);
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    private int[] unpackToInts(Object[] o, int n, String msg) {
        if (o.length != n) {
            w.getStatusUpdateCenter().update(this, "Received wrong number of arguments for OSC message " + msg
                    + " (Expected " + n + ")", Level.WARNING);
            throw new IllegalArgumentException();
        }
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            if (o[i] instanceof Number) {
                a[i] = ((Number) o[i]).intValue();
            } else {
                w.getStatusUpdateCenter().update(this, "Received non-numeric argument(s) for OSC message " + msg, Level.WARNING);
            }
        }
        return a;
    }

    private String[] unpackToStrings(Object[] o, int n, String msg) {
        if (o.length != n) {
            w.getStatusUpdateCenter().update(this, "Received wrong number of arguments for OSC message " + msg
                    + " (Expected " + n + ")", Level.WARNING);
            throw new IllegalArgumentException();
        }
        String[] a = new String[n];
        for (int i = 0; i < n; i++) {
            if (o[i] instanceof String) {
                a[i] = (String) o[i];
            } else {
                w.getStatusUpdateCenter().update(this, "Received non-string argument(s) for OSC message " + msg, Level.WARNING);
            }
        }
        return a;
    }

}
