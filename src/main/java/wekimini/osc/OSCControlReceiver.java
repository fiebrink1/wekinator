/*
 * Handles incoming OSC control messages
 */
package wekimini.osc;

import com.illposed.osc.OSCMessageListener;
import com.illposed.osc.OSCMessageEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import wekimini.WekiMiniRunner;
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
    private final String startDtwRecordMessage = "/wekinator/control/startDtwRecording";
    private final String stopDtwRecordMessage = "/wekinator/control/stopDtwRecording";

    private final String trainMessage = "/wekinator/control/train";
    private final String cancelTrainingMessage = "/wekinator/control/cancelTrain";
    private final String startRunningMessage = "/wekinator/control/startRunning";
    private final String stopRunningMessage = "/wekinator/control/stopRunning";
    private final String deleteAllExamplesMessage = "/wekinator/control/deleteAllExamples";
    private final String deleteExamplesForOutputMessage = "/wekinator/control/deleteExamplesForOutput";

    private final String enableModelRecordMessage = "/wekinator/control/enableModelRecording"; //List of model #s to enable (indexed from 1) 
    private final String disableModelRecordMessage = "/wekinator/control/disableModelRecording"; //List of model #s to disable (indexed from 1) 
    private final String enableModelRunMessage = "/wekinator/control/enableModelRunning"; //List of model #s to enable (indexed from 1) 
    private final String disableModelRunMessage = "/wekinator/control/disableModelRunning"; //List of model #s to disable (indexed from 1) 

    // private final String setModelRecordEnabledMessage = "/wekinator/control/setModelRecordEnabled"; //1st argument model # (starting from 1), 2nd argument record boolean (0/1))
    //  private final String setModelRunEnabledMessage = "/wekinator/control/setModelRunEnabled"; //1st argument model # (starting from 1), 2nd argument run boolean (0/1))
    private final String setInputNamesMessage = "/wekinator/control/setInputNames";
    private final String setOutputNamesMessage = "/wekinator/control/setOutputNames";
    private final String setInputSelectionForOutputMessage = "/wekinator/control/selectInputsForOutput";

    private final String loadModelFromFileMessage = "/wekinator/control/loadModelFromFile"; //int for model # (starting from 1), 2nd argument a filename, 3rd argument (optional) is WITHDATA or WITHOUTDATA
    private final String saveModelToFileMessage = "/wekinator/control/saveModelToFile"; //int for model # (starting from 1), 2nd argument a filename.

    private final String runNewProjectMessage = "/wekinator/control/runNewProject"; //First argument filename, second argument (optional) CLOSECURRENT/STOPCURRENTLISTENING/KEEPCURRENTRUNNING

    private final String enablePerformanceModeMessage = "/wekinator/control/enablePerformanceMode";
    private final String disablePerformanceModeMessage = "/wekinator/control/disablePerformanceMode";

    
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
        OSCMessageListener startRecordListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.startRecord();
            }
        };
        w.getOSCReceiver().addOSCMessageListener(startRecordMessage, startRecordListener);

        OSCMessageListener stopRecordListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.stopRecord();
            }
        };
        w.getOSCReceiver().addOSCMessageListener(stopRecordMessage, stopRecordListener);

        OSCMessageListener startDtwRecordListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                List<Object> o = oscm.getMessage().getArguments();
                if (o != null && o.size() > 0 && o.get(0) instanceof Integer) {
                    controller.startDtwRecord((Integer) o.get(0));
                } else {
                    String msg = "Error: Expected message " + startDtwRecordMessage + " to be followed by 1 integer argument";
                    w.getStatusUpdateCenter().warn(this, msg);
                }
            }
        };
        w.getOSCReceiver().addOSCMessageListener(startDtwRecordMessage, startDtwRecordListener);

        OSCMessageListener stopDtwRecordListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.stopDtwRecord();
            }
        };
        w.getOSCReceiver().addOSCMessageListener(stopDtwRecordMessage, stopDtwRecordListener);

        OSCMessageListener startRunListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.startRun();
            }
        };
        w.getOSCReceiver().addOSCMessageListener(startRunningMessage, startRunListener);

        OSCMessageListener stopRunListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.stopRun();
            }
        };
        w.getOSCReceiver().addOSCMessageListener(stopRunningMessage, stopRunListener);

        OSCMessageListener trainListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.train();
            }
        };
        w.getOSCReceiver().addOSCMessageListener(trainMessage, trainListener);

        OSCMessageListener cancelTrainListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.cancelTrain();
            }
        };
        w.getOSCReceiver().addOSCMessageListener(cancelTrainingMessage, cancelTrainListener);

        OSCMessageListener deleteAllExamplesListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                controller.deleteAllExamples();
            }
        };

        OSCMessageListener deleteAllExamplesForOutputListener = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                List<Object> o = oscm.getMessage().getArguments();
                if (o != null && o.size() > 0 && o.get(0) instanceof Integer) {
                    controller.deleteExamplesForOutput((Integer) o.get(0));
                } else {
                    String msg = "Error: Expected message " + deleteExamplesForOutputMessage + " to be followed by 1 integer argument";
                    w.getStatusUpdateCenter().warn(this, msg);
                }
            }
        };

        w.getOSCReceiver().addOSCMessageListener(deleteAllExamplesMessage, deleteAllExamplesListener);
        w.getOSCReceiver().addOSCMessageListener(deleteExamplesForOutputMessage, deleteAllExamplesForOutputListener);

        w.getOSCReceiver().addOSCMessageListener(enableModelRecordMessage, createModelChangeListener(true, true));
        w.getOSCReceiver().addOSCMessageListener(disableModelRecordMessage, createModelChangeListener(true, false));
        w.getOSCReceiver().addOSCMessageListener(enableModelRunMessage, createModelChangeListener(false, true));
        w.getOSCReceiver().addOSCMessageListener(disableModelRunMessage, createModelChangeListener(false, false));

        w.getOSCReceiver().addOSCMessageListener(setInputNamesMessage, createInputNamesListener());
        w.getOSCReceiver().addOSCMessageListener(setOutputNamesMessage, createOutputNamesListener());
        w.getOSCReceiver().addOSCMessageListener(setInputSelectionForOutputMessage, createInputSelectionListener());
        w.getOSCReceiver().addOSCMessageListener(loadModelFromFileMessage, createModelLoadListener());
        w.getOSCReceiver().addOSCMessageListener(saveModelToFileMessage, createModelSaveListener());
        w.getOSCReceiver().addOSCMessageListener(runNewProjectMessage, runNewProjectListener());
        w.getOSCReceiver().addOSCMessageListener(enablePerformanceModeMessage, enablePerformanceModeListener());
        w.getOSCReceiver().addOSCMessageListener(disablePerformanceModeMessage, disablePerformanceModeListener());

    }

    private OSCMessageListener createModelChangeListener(final boolean isRecord, final boolean isEnable) {
        OSCMessageListener l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }

                List<Object> o = oscm.getMessage().getArguments();
                if (o.size() == 0) {
                    w.getStatusUpdateCenter().warn(this,
                            "OSC message " + enableModelRecordMessage
                            + " requires a list of model numbers");
                    return;
                }
                int[] modelNumbers = new int[o.size()];
                for (int i = 0; i < o.size(); i++) {
                    if (o.get(i) instanceof Number) {
                        int whichModel = ((Number) o.get(i)).intValue();
                        if (whichModel < 1 || whichModel > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                            w.getStatusUpdateCenter().warn(this,
                                    "Model numbers must be between 1 and # models for OSC message " + enableModelRecordMessage);
                            return;
                        }
                        modelNumbers[i] = whichModel;

                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "Non-numeric model number given for OSC message " + enableModelRecordMessage);
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

    private OSCMessageListener createInputNamesListener() {
        OSCMessageListener l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }

                List<Object> o = oscm.getMessage().getArguments();
                try {
                    if (w.getInputManager().hasValidInputs() && o.size() != w.getInputManager().getNumInputs()) {
                        w.getStatusUpdateCenter().warn(this,
                                "Received wrong number of input names "
                                + "(received " + o.size() + ", expected " + w.getInputManager().getNumInputs() + ")");
                        return;
                    }

                    String[] names = new String[o.size()];
                    for (int i = 0; i < o.size(); i++) {
                        if (!(o.get(i) instanceof String)) {
                            w.getStatusUpdateCenter().warn(this,
                                    "Received non-string argument(s) for OSC message " + oscm.getMessage());
                            return;
                        }
                        names[i] = (String) o.get(i);
                    }
                    if (Util.checkAllUnique(names)) {
                        controller.setInputNames(names);
                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "Input names not unique (received via OSC message " + setInputNamesMessage
                                + ")");
                    }
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    private OSCMessageListener createOutputNamesListener() {
        OSCMessageListener l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }

                List<Object> o = oscm.getMessage().getArguments();
                try {
                    if (w.getOutputManager().hasValidOutputGroup() && o.size() != w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().warn(this,
                                "Received wrong number of output names "
                                + "(received " + o.size() + ", expected " + w.getOutputManager().getOutputGroup().getNumOutputs() + ")");
                        return;
                    }

                    String[] names = new String[o.size()];
                    for (int i = 0; i < o.size(); i++) {
                        if (!(o.get(i) instanceof String)) {
                            w.getStatusUpdateCenter().warn(this,
                                    "Received non-string argument(s) for OSC message " + oscm.getMessage());
                            return;
                        }
                        names[i] = (String) o.get(i);
                    }
                    if (Util.checkAllUnique(names)) {
                        controller.setOutputNames(names);
                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "Output names not unique (received via OSC message " + setOutputNamesMessage
                                + ")");
                    }
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    //TODO Make use of this if it comes in early in project setup!
    private OSCMessageListener createInputSelectionListener() {
        OSCMessageListener l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }

                List<Object> o = oscm.getMessage().getArguments();
                try {
                    //Check if we can reasonably do something with this:
                    if (!w.getInputManager().hasValidInputs() || !w.getOutputManager().hasValidOutputGroup()) {
                        w.getStatusUpdateCenter().warn(this,
                                "Cannot create connections between inputs and outputs: these are not yet set up");
                        return;
                    }

                    if (o.size() < 2) {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + setInputSelectionForOutputMessage + " requires output ID followed by at least one selected input ID");
                        return;
                    }

                    int[] vals = unpackToInts(o, o.size(), setInputSelectionForOutputMessage);

                    int outputNum = vals[0];
                    if (outputNum < 1 || outputNum > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + setInputSelectionForOutputMessage
                                + " requires first argument to be output ID (between 1 and "
                                + w.getOutputManager().getOutputGroup().getNumOutputs() + ")");
                        return;
                    }

                    int[] whichInputs = new int[vals.length - 1];
                    for (int i = 0; i < vals.length - 1; i++) {
                        int nextInput = vals[i + 1];
                        if (nextInput < 1 || nextInput > w.getInputManager().getNumInputs()) {
                            w.getStatusUpdateCenter().warn(this,
                                    "OSC message " + setInputSelectionForOutputMessage
                                    + " requires list of input IDs to be selected, with each ID between 1 and # inputs ("
                                    + w.getInputManager().getNumInputs() + ")");
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

    private OSCMessageListener createModelLoadListener() {
        OSCMessageListener l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }

                List<Object> o = oscm.getMessage().getArguments();
                try {
                    //w.getOutputManager().hasValidOutputGroup()) {

                    if (o.size() < 2) {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + loadModelFromFileMessage
                                + " requires model ID followed by filename");
                        return;
                    }

                    int modelNum = 0;
                    if (o.get(0) instanceof Number) {
                        modelNum = ((Number) o.get(0)).intValue();
                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + loadModelFromFileMessage
                                + " requires model ID (as integer) followed by filename");
                        return;
                    }

                    if (!w.getOutputManager().hasValidOutputGroup()) {
                        w.getStatusUpdateCenter().warn(this,
                                "Wekinator outputs are not set up; cannot yet "
                                + " apply OSC message " + loadModelFromFileMessage);
                        return;
                    }
                    if (modelNum < 1 || modelNum > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + loadModelFromFileMessage
                                + " requires model ID to be between 1 and number of models");
                        return;
                    }

                    String filename;
                    if (o.get(1) instanceof String) {
                        filename = (String) o.get(1);
                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + loadModelFromFileMessage
                                + " requires second argument to be filename (as string)");
                        return;
                    }

                    boolean importData = false;
                    if (o.size() >= 3) {
                        if (o.get(2) instanceof String) {
                            String dataString = (String) o.get(2);
                            if (dataString.equals("WITHDATA")) {
                                importData = true;
                            } else if (!dataString.equals("WITHOUTDATA")) {
                                w.getStatusUpdateCenter().warn(this,
                                        "OSC message " + loadModelFromFileMessage
                                        + " requires third argument to be WITHDATA or WITHOUTDATA");
                                //Don't return, we'll proceed anyway.
                            }
                        } else {
                            w.getStatusUpdateCenter().warn(this,
                                    "OSC message " + loadModelFromFileMessage
                                    + " requires third argument to be WITHDATA or WITHOUTDATA");
                            //Don't return, we'll proceed anyway.
                        }
                    }

                    controller.loadModelFromFilename(modelNum, filename, importData);
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    private OSCMessageListener createModelSaveListener() {
        OSCMessageListener l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }

                List<Object> o = oscm.getMessage().getArguments();
                try {
                    //w.getOutputManager().hasValidOutputGroup()) {

                    if (o.size() < 2) {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + saveModelToFileMessage
                                + " requires model ID followed by filename");
                        return;
                    }

                    int modelNum = 0;
                    if (o.get(0) instanceof Number) {
                        modelNum = ((Number) o.get(0)).intValue();
                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + saveModelToFileMessage
                                + " requires model ID (as integer) followed by filename");
                        return;
                    }

                    if (!w.getOutputManager().hasValidOutputGroup()) {
                        w.getStatusUpdateCenter().warn(this,
                                "Wekinator outputs are not set up; cannot yet "
                                + " apply OSC message " + saveModelToFileMessage);
                        return;
                    }
                    if (modelNum < 1 || modelNum > w.getOutputManager().getOutputGroup().getNumOutputs()) {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + saveModelToFileMessage
                                + " requires model ID to be between 1 and number of models");
                        return;
                    }

                    String filename;
                    if (o.get(1) instanceof String) {
                        filename = (String) o.get(1);
                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + saveModelToFileMessage
                                + " requires second argument to be filename (as string)");
                        return;
                    }

                    controller.saveModelToFilename(modelNum, filename);
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    private OSCMessageListener runNewProjectListener() {
        OSCMessageListener l;
        l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }

                List<Object> o = oscm.getMessage().getArguments();
                try {
                    //w.getOutputManager().hasValidOutputGroup()) {

                    if (o.size() < 1) {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + runNewProjectMessage
                                + " requires a filename");
                        return;
                    }

                    String filename;
                    if (o.get(0) instanceof String) {
                        filename = (String) o.get(0);
                    } else {
                        w.getStatusUpdateCenter().warn(this,
                                "OSC message " + runNewProjectMessage
                                + " requires first argument to be filename (as string)");
                        return;
                    }

                    WekiMiniRunner.NewProjectOptions options = WekiMiniRunner.NewProjectOptions.CLOSECURRENT;

                    if (o.size() > 1) {
                        if (o.get(1) instanceof String) {
                            String s = (String) o.get(1);
                            if (s.equals("CLOSECURRENT")) {
                                options = WekiMiniRunner.NewProjectOptions.CLOSECURRENT;
                            } else if (s.equals("STOPCURRENTLISTENING")) {
                                options = WekiMiniRunner.NewProjectOptions.STOPCURRENTLISTENING;
                            } else if (s.equals("KEEPCURRENTRUNNING")) {
                                options = WekiMiniRunner.NewProjectOptions.KEEPCURRENTRUNNING;
                            } else {
                                w.getStatusUpdateCenter().warn(this,
                                        "OSC message " + runNewProjectMessage
                                        + " requires second argument to be CLOSECURRENT, STOPCURRENTLISTENING, or KEEPCURRENTRUNNING");
                                return;
                            }
                        } else {
                            w.getStatusUpdateCenter().warn(this,
                                    "OSC message " + runNewProjectMessage
                                    + " requires second argument to be CLOSECURRENT, STOPCURRENTLISTENING, or KEEPCURRENTRUNNING");
                            return;
                        }

                    }

                    controller.runNewProject(filename, options);
                } catch (IllegalArgumentException ex) {
                }
            }

        };
        return l;
    }

    private OSCMessageListener enablePerformanceModeListener() {
        OSCMessageListener l;
        l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }
                controller.enablePerformanceMode(true);
            }
        };
        return l;
    }

    private OSCMessageListener disablePerformanceModeListener() {
        OSCMessageListener l;
        l = new OSCMessageListener() {
            @Override
            public void acceptMessage(OSCMessageEvent oscm) {
                if (!controller.checkEnabled()) {
                    return;
                }
                controller.enablePerformanceMode(false);
            }
        };
        return l;
    }

    
    private int[] unpackToInts(List<Object> o, int n, String msg) {
        if (o.size() != n) {
            w.getStatusUpdateCenter().warn(this, "Received wrong number of arguments for OSC message " + msg
                    + " (Expected " + n + ")");
            throw new IllegalArgumentException();
        }
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            if (o.get(i) instanceof Number) {
                a[i] = ((Number) o.get(i)).intValue();
            } else {
                w.getStatusUpdateCenter().warn(this, "Received non-numeric argument(s) for OSC message " + msg);
            }
        }
        return a;
    }

    private String[] unpackToStrings(Object[] o, int n, String msg) {
        if (o.length != n) {
            w.getStatusUpdateCenter().warn(this, "Received wrong number of arguments for OSC message " + msg
                    + " (Expected " + n + ")");
            throw new IllegalArgumentException();
        }
        String[] a = new String[n];
        for (int i = 0; i < n; i++) {
            if (o[i] instanceof String) {
                a[i] = (String) o[i];
            } else {
                w.getStatusUpdateCenter().warn(this, "Received non-string argument(s) for OSC message " + msg);
            }
        }
        return a;
    }

}
