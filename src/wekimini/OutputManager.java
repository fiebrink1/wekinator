/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Date;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;
import wekimini.osc.OSCOutputGroup;
import wekimini.util.WeakListenerSupport;
import wekimini.osc.OSCReceiver;

/**
 *
 * @author rebecca
 */
public class OutputManager {
    private static final Logger logger = Logger.getLogger(OutputManager.class.getName());
    private OSCOutputGroup outputGroup = null;

    private final Wekinator w;
    private final WeakListenerSupport wls = new WeakListenerSupport();

    //Listeners for outputs received via OSC
    private final List<OutputManager.OutputValueListener> valueReceivedListeners;

    //Listeners for output group vectors computed internally
    private final List<OutputManager.OutputValueListener> valueComputedListeners;
    
    //Listeners for individual output edits (e.g. change # classes)
    private final List<OutputTypeEditListener> outputTypeEditListeners;

    //Listeners for single outputs computed internally
  //  private final List<OutputManager.SingleOutputValueListener> singleValueComputedListeners;

    private final PropertyChangeListener oscReceiverListener;

    //Listeners for my events
    private final EventListenerList listeners;

    private double[] currentValues = new double[0];

    /*public void setComputedOutputs(double[] values) {
        if (values != null && values.length == currentValues.length) {
            System.arraycopy(values, 0, currentValues, 0, values.length);
            try {
                w.getOSCSender().sendMessage(values);
            } catch (IOException ex) {
                System.out.println("ERROR: Couldn't send OSC values out!");
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            String msg = "Error: Output value array is null or wrong length";
            throw new IllegalArgumentException(msg);
        }

        //TODO need to notify listeners
    } */

    public static final String PROP_OUTPUTGROUP = "outputGroup";
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public OutputManager(Wekinator w) {
        //Make sure Wekinator initialises this after OSCReceiver 
        this.w = w;

        //Listeners for outputGroupChange 
        listeners = new EventListenerList();
        valueReceivedListeners = new LinkedList<>();
        valueComputedListeners = new LinkedList<>();
        outputTypeEditListeners = new LinkedList<>();
        //singleValueComputedListeners = new LinkedList<>();

        //Currently have listeners for outputGroupChange (add, remove, modify)
        //TODO: Add listeners for (1) new OSC output values received; (2) new output values computed
        //TODO: We will want to subscribe to Connections that tell us when new output values are available
        //For receiving new output values from Max/etc.
        
        //oscReceiverListener = this::oscReceiverPropertyChanged;
        oscReceiverListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscReceiverPropertyChanged(evt);
            }
        };
        
        w.getOSCReceiver().addPropertyChangeListener(wls.propertyChange(oscReceiverListener));
        addOSCOutputValueListener();
    }

    public void updateOutput(OSCOutput newOutput, OSCOutput oldOutput) {
        //Set current value
        int which = outputGroup.getOutputNumber(oldOutput);
        
        if (which != -1) {
            outputGroup.updateOutput(newOutput, which);
            if (! newOutput.isLegalOutputValue(currentValues[which])) {
                currentValues[which] = newOutput.getDefaultValue();
            }
            notifyOutputEditListeners(newOutput, oldOutput, which);
        } else {
            logger.log(Level.WARNING, "Old output group not found");
        }
    }
    
    private void oscReceiverPropertyChanged(PropertyChangeEvent e) {
        if (e.getPropertyName() == OSCReceiver.PROP_CONNECTIONSTATE) {
            if (e.getNewValue() == OSCReceiver.ConnectionState.CONNECTED) {
                //Re-add listeners for new output values from Max/etc. if OSC is re-connected
                addOSCOutputValueListener();
            }
        }
    }
    
    public boolean hasValidOutputGroup() {
        return outputGroup != null;
    }

    //For now, no possibility to modify an output group: it's a totally new group.
    public void setOSCOutputGroup(OSCOutputGroup newG) throws IllegalArgumentException {
        OSCOutputGroup oldGroup = outputGroup;
        outputGroup = newG;
        currentValues = new double[newG.getNumOutputs()];
        for (int i = 0; i < currentValues.length; i++) {
            currentValues[i] = outputGroup.getOutput(i).getDefaultValue();
        }
        
        propertyChangeSupport.firePropertyChange(PROP_OUTPUTGROUP, oldGroup, outputGroup);
    }

    public OSCOutputGroup getOutputGroup() {
        return outputGroup;
    }
    
    public double[] getCurrentValues() {
        return currentValues;
    }
    
    public void setCurrentValue(int which, double value) {
        if (which >= currentValues.length) {
            throw new IllegalArgumentException("Illegal current value index " + which);
        }
        currentValues[which] = value;
    }

    private void addOSCOutputValueListener() {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                messageArrived(oscm);
            }
        };
        //Groupname as message:
        w.getOSCReceiver().addOSCOutputValueListener(l);
    }

    //Will be called when new output values arrived via OSC
    private void messageArrived(OSCMessage m) {
        //TODO: Probably want to make intelligent decision about whether to
        //use these or not (i.e., if in running mode, ignore these)

        List<Object> o = m.getArguments();
        double d[] = new double[o.size()];
        if (currentValues.length == d.length) {
            for (int i = 0; i < o.size(); i++) {
                if (o.get(i) instanceof Float) {
                    try {
                        if (w.getLearningManager().isLegalTrainingValue(i, (Float)o.get(i))) {
                        //if (w.getSupervisedLearningManager().getPaths().get(i).getOSCOutput().isLegalTrainingValue((Float)o[i])) {
                            d[i] = ((Float) o.get(i));
                        } else {
                            w.getStatusUpdateCenter().warn(this, "Illegal output value " + o.get(i) + " received for output " + i + "; ignoring it");
                            d[i] = currentValues[i];
                        }
                    } catch (Exception ex) {
                        d[i] = currentValues[i];
                    }  
                } else {
                    Logger.getLogger(OutputManager.class.getName()).log(Level.WARNING, "Received output value is not a float");
                }
            }
            
            
            System.arraycopy(d, 0, currentValues, 0, d.length);
            notifyValueReceivedListeners(d);
        } else if (currentValues.length != 0) { //Don't warn if we're not set up yet
            
            String msg = "Output values received but message is wrong length: "
                    + "Expected " + currentValues.length + " values, received "
                    + d.length;
            w.getStatusUpdateCenter().warn(this, msg);
        }
    }

    public void randomizeAllOutputs() {
        double randoms[] = new double[outputGroup.getNumOutputs()];
        //Assumes we'll never need to make a distinction between outputs that are computed vs those that are randomly generated
        for (int i= 0; i < outputGroup.getNumOutputs(); i++) {
            double r= outputGroup.getOutput(i).generateRandomValue();
            randoms[i] = r;
        }
        setNewComputedValues(randoms);
        KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.RANDOMIZE);
    }
    
    //Notifies listeners that we've got a new OSC-received output vector
    private void notifyValueReceivedListeners(double[] data) {
        for (OutputValueListener l : valueReceivedListeners) {
            l.update(data); //May have to include group name/ID here (problematic if it changes later)
        }
    }

    //Only for output values received via OSC (not computed internally)
    public void addOutputValueReceivedListener(OutputManager.OutputValueListener l) {
        valueReceivedListeners.add(l);

    }

    //Only for output values received via OSC (not computed internally)
    public boolean removeOutputValueReceivedListener(OutputManager.OutputValueListener l) {
        return valueReceivedListeners.remove(l);
    }

    //Notifies listeners that we've got an internally-computed output vector
    private void notifyOutputGroupComputedListeners(double[] data) {
        for (OutputValueListener l : valueComputedListeners) {
            l.update(data); //May have to include group name/ID here (problematic if it changes later)
        }
    }

    //Only for output values computed internally
    public void addOutputGroupComputedListener(OutputManager.OutputValueListener l) {
        valueComputedListeners.add(l);
    }

    //Only for output values computed internally
    public boolean removeOutputGroupComputedListener(OutputManager.OutputValueListener l) {
        return valueComputedListeners.remove(l);
    }

    public void sendMessage(String msgName) {
        try {
            w.getOSCSender().sendOutputMessage(msgName);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Couldn't send message");
            logger.log(Level.SEVERE, null, ex);        
        }
    }
    
    //Call this when new values available
    public void setNewComputedValues(double[] values) {
       // System.out.println("IN SETTING NEW COMPUTED");
        if (values == null || values.length != currentValues.length) {
            throw new IllegalArgumentException("values is null or wrong length");
        }
        System.arraycopy(values, 0, currentValues, 0, values.length);
        notifyOutputGroupComputedListeners(values);

        //In future, may want OSC Sender to listen and respond rather than push
        try {
            w.getOSCSender().sendOutputValuesMessage(outputGroup.getOscMessage(), values);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Couldn't send message");
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    //For right now, send message whenever any output is changed in GUI
    //In future, could change this so that only one output acts as trigger, or user has to press button to trigger send.
    public void setNewValueFromGUI(int which, double value) {
      //  System.out.println("IN SETTING VALUE FROM GUI");
        if (which >= currentValues.length) {
            throw new IllegalArgumentException("Attempted to set index of values out of bounds");
        }
        currentValues[which] = value;
        try {
            w.getOSCSender().sendOutputValuesMessage(outputGroup.getOscMessage(), currentValues);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Couldn't send message", ex);
        }  
    }


    public String outputsToString() {
        /* StringBuilder sb = new StringBuilder();
         for (OSCOutputGroup o : outputGroups.values()) {
         sb.append(o.toString());
         }
         return sb.toString();
         */
        if (outputGroup == null) {
            return "none";
        } else {
            return outputGroup.toString();
        }
    }
    
    public void addIndividualOutputEditListener(OutputTypeEditListener l) {
        outputTypeEditListeners.add(l);
    }
    
    public void removeIndividualOutputEditListener(OutputTypeEditListener l) {
        outputTypeEditListeners.remove(l);
    }

    private void notifyOutputEditListeners(OSCOutput newOutput, OSCOutput oldOutput, int index) {
        for (OutputTypeEditListener l : outputTypeEditListeners) {
            l.outputTypeEdited(newOutput, oldOutput, index);
        }
    }

    public boolean containsOutputName(String name) {
        List<OSCOutput> l = outputGroup.getOutputs();
        for (OSCOutput o : l) {
            if (o.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    void setDistribution(int i, double[] dist) {
        try {
            w.getOSCSender().sendOutputValuesMessage("/" + outputGroup.getOutput(i).getName(), dist);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Couldn't send message");
            logger.log(Level.SEVERE, null, ex);
        }
    }

    void setNewComputedBundleValues(List<List<Double>> allOutputs) {
        // System.out.println("IN SETTING NEW COMPUTED");
        for (List<Double> nextOutputs : allOutputs) {
           // double[] values = new double[nextOutputs.size()];
            if (nextOutputs.size() != currentValues.length) {
                throw new IllegalArgumentException("values is null or wrong length");
            }
            for (int i = 0; i < currentValues.length; i++) {
                currentValues[i] = nextOutputs.get(i);
            }
            notifyOutputGroupComputedListeners(currentValues);
        }
        try {
            w.getOSCSender().sendOutputBundleValuesMessage(outputGroup.getOscMessage(), allOutputs);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Couldn't send message");
            logger.log(Level.SEVERE, null, ex);
        }
    }

    void setNewComputedBundleDistribution(int i, double[][] allDistributions) {
        try {
            w.getOSCSender().sendOutputBundleValuesMessage("/" + outputGroup.getOutput(i).getName(), allDistributions);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Couldn't send message");
            logger.log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    // TODO: Not sure if we need this?
    public interface OutputValueListener extends EventListener {

        public void update(double[] vals);
    }


    /*public interface OutputGroupChangeListener extends EventListener {

     void outputGroupChange(OutputGroupChangeEvent evt);
     } */

    /*public class OutputGroupChangeEvent {

     private final String name;
     private final OutputGroupChangeType changeType;

     public OutputGroupChangeEvent(final String name, final OutputGroupChangeType changeType) {
     this.name = name;
     this.changeType = changeType;
     }

     public String getName() {
     return name;
     }

     public OutputGroupChangeType getChangeType() {
     return changeType;
     }
     } */
    
    public interface OutputTypeEditListener {
        public void outputTypeEdited(OSCOutput newOutput, OSCOutput oldOutput, int which);   
    }
}
