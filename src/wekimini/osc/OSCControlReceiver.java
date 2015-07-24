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
    private final String setModelRecordEnabledMessage = "/wekinator/control/setModelRecordEnabled"; //1st argument model # (starting from 1), 2nd argument record boolean (0/1))
    private final String setModelRunEnabledMessage = "/wekinator/control/setModelRunEnabled"; //1st argument model # (starting from 1), 2nd argument run boolean (0/1))
    private final String setInputNamesMessage = "/wekinator/control/setInputNames";
    private final String setOutputNamesMessage = "/wekinator/control/setOutputNames";
    private final String setInputSelectionForOutputMessage = "/wekinator/control/setInputSelectionForOutput";

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
        w.getOSCReceiver().addOSCListener(setModelRecordEnabledMessage, createModelRecordEnableListener());
        w.getOSCReceiver().addOSCListener(setModelRunEnabledMessage, createModelRunEnableListener());
        w.getOSCReceiver().addOSCListener(setInputNamesMessage, createInputNamesListener());
        w.getOSCReceiver().addOSCListener(setOutputNamesMessage, createOutputNamesListener());
        w.getOSCReceiver().addOSCListener(setInputSelectionForOutputMessage, createInputSelectionListener());
    }
    
    private OSCListener createModelRecordEnableListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                Object[] o = oscm.getArguments();
                try {
                    int[] args = unpackToInts(o, 2, setModelRecordEnabledMessage);
                    if (args[0] < 1 || args[0] > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().update(this, "Received illegal model number for OSC message "
                                + setModelRecordEnabledMessage
                                + ": Must be between 1 and " + w.getOutputManager().getOutputGroup().getNumOutputs(),
                                Level.WARNING);
                        return;
                    }
                    if (args[1] < 0 || args[1] > 1) {
                        w.getStatusUpdateCenter().update(this, "Received illegal argument for OSC message "
                                + setModelRecordEnabledMessage
                                + ": second argument must be 1 (to enable) or 0 (to disable)",
                                Level.WARNING);
                        return;
                    }
                    //ALL OK here
                    controller.setModelRecordEnabled(args[0], args[1] == 1);

                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;  
    }

    private OSCListener createModelRunEnableListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                Object[] o = oscm.getArguments();
                try {
                    int[] args = unpackToInts(o, 2, setModelRunEnabledMessage);
                    if (args[0] < 1 || args[0] > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().update(this, "Received illegal model number for OSC message "
                                + setModelRunEnabledMessage
                                + ": Must be between 1 and " + w.getOutputManager().getOutputGroup().getNumOutputs(),
                                Level.WARNING);
                        return;
                    }
                    if (args[1] < 0 || args[1] > 1) {
                        w.getStatusUpdateCenter().update(this, "Received illegal argument for OSC message "
                                + setModelRunEnabledMessage
                                + ": second argument must be 1 (to enable) or 0 (to disable)",
                                Level.WARNING);
                        return;
                    }
                    //ALL OK here
                    controller.setModelRunEnabled(args[0], args[1] == 1);

                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;  
    }

    private OSCListener createInputNamesListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                Object[] o = oscm.getArguments();
                try {
                    String[] names = unpackToStrings(o, w.getInputManager().getNumInputs(), setInputNamesMessage);
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
                Object[] o = oscm.getArguments();
                try {
                    String[] names = unpackToStrings(o, w.getOutputManager().getOutputGroup().getNumOutputs(), setOutputNamesMessage);
                    if (Util.checkAllUnique(names)) {
                        controller.setInputNames(names);
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

    private OSCListener createInputSelectionListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                Object[] o = oscm.getArguments();
                try {
                    int[] vals = unpackToInts(o, w.getInputManager().getNumInputs() + 1, setInputSelectionForOutputMessage);
                    boolean[] inputSelection = new boolean[vals.length - 1];
                    for (int i = 0; i < vals.length - 1; i++) {
                        if (vals[i] != 0 && vals[i] != 1) {
                            w.getStatusUpdateCenter().update(this,
                                    "Error: OSC message " + setInputSelectionForOutputMessage + 
                                            " requires one 0/1 per input (boolean) followed by output number (numbered starting from 1",
                                    Level.WARNING);
                            return;
                        }
                        inputSelection[i] = vals[i] == 1;
                    }
                    int outputNum = vals[vals.length - 1];
                    if (outputNum < 1 || outputNum > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().update(this, 
                                "Error: OSC message " + setInputSelectionForOutputMessage + 
                                        " requires last argument to be output number (between 1 and " + 
                                        w.getOutputManager().getOutputGroup().getNumOutputs() + ")",
                                Level.WARNING);
                        return;
                    }
                    controller.setInputSelectionForOutput(inputSelection, outputNum);
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
               a[i] = ((Number)o[i]).intValue();
            } else {
                w.getStatusUpdateCenter().update(this, "Received non-numeric argument(s) for OSC message " + msg, Level.WARNING);
            }
        }
        return a;
    }

    private String[] unpackToStrings(Object[] o, int n, String msg) {
        if (o.length != n) {
            w.getStatusUpdateCenter().update(this, "Received wrong number of arguments for OSC message " + msg   
            + " (Expected " + n + ")" , Level.WARNING);
            throw new IllegalArgumentException();
        }
        String[] a = new String[n];
        for (int i = 0; i < n; i++) {
            if (o[i] instanceof String) {
               a[i] = (String)o[i];
            } else {
                w.getStatusUpdateCenter().update(this, "Received non-string argument(s) for OSC message " + msg, Level.WARNING);
            }
        }
        return a;
    }

}
