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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import wekimini.osc.OSCInputGroup;
import wekimini.util.WeakListenerSupport;
import wekimini.osc.OSCReceiver;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class InputManager {

    private OSCInputGroup inputGroup = null;
    private final Wekinator w;
    private final WeakListenerSupport wls = new WeakListenerSupport();
    private final PropertyChangeListener oscReceiverListener;
    private final List<InputListener> inputValueListeners;
    private final EventListenerList inputGroupChangeListeners = new EventListenerList();
    public static final String PROP_INPUTGROUP = "inputGroup";
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private double[] currentValues = new double[0];
    private static final Logger logger = Logger.getLogger(InputManager.class.getName());

    public double[] getInputValues() {
        if (inputGroup == null) {
            return new double[0];
        } else {
            return currentValues;
        }
    }

    public int getNumInputs() {
        if (inputGroup == null) {
            return 0;
        } else {
            return inputGroup.getNumInputs();
        }
    }

    public String[] getInputNames() {
        if (inputGroup == null) {
            return new String[0];
        } else {
            return inputGroup.getInputNames();
        }
    }

    public boolean hasValidInputs() {
        return (inputGroup != null);
    }

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

    public InputManager(Wekinator w) {
        //Make sure Wekinator initialises this after OSCReceiver 
        this.w = w;
        oscReceiverListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscReceiverPropertyChanged(evt);
            }
        };
        //oscReceiverListener = this::oscReceiverPropertyChanged;
        w.getOSCReceiver().addPropertyChangeListener(wls.propertyChange(oscReceiverListener));
        inputValueListeners = new LinkedList<>();
        //For testing:
        /*OSCInputGroup g;
         String[] names1 = {"a"};
         g = new OSCInputGroup("group1", "/m1", 1, names1);
         addOSCInputGroup(g, true); */
    }

    private void oscReceiverPropertyChanged(PropertyChangeEvent e) {
        if (e.getPropertyName() == OSCReceiver.PROP_CONNECTIONSTATE) {
            if (e.getNewValue() == OSCReceiver.ConnectionState.CONNECTED) {
                addOSCInputListener();
            }
        }
    }

    public void setOSCInputGroup(OSCInputGroup newG) {
        //inputs.add(g);
        OSCInputGroup oldGroup = inputGroup;
        inputGroup = newG;

        if (oldGroup == null || !oldGroup.getOscMessage().equals(newG.getOscMessage())) {
            //Update OSC istener for new(?) message
            addOSCInputListener();
        }
        if (inputGroup != null) {
            currentValues = new double[inputGroup.getNumInputs()];
        }
        propertyChangeSupport.firePropertyChange(PROP_INPUTGROUP, oldGroup, inputGroup);
    }

    public OSCInputGroup getOSCInputGroup() {
        return inputGroup;
    }

    private void addOSCInputListener() {
        if (inputGroup != null) {
            addOSCInputListener(inputGroup);
        }
    }

    //Listener for input group name
    //(all inputs in a group will update simultaneously)
    public void addInputValueListener(InputListener l) {
        inputValueListeners.add(l);
    }

    public boolean removeInputValueListener(InputListener l) {
        return inputValueListeners.remove(l);
    }

    private void addOSCInputListener(final OSCInputGroup g) {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                messageArrived(g.getOscMessage(), oscm);
            }
        };

        OSCListener groupl = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                bundleArrived(g.getOscMessage(), oscm);
            }

        };

        w.getOSCReceiver().addOSCListener(g.getOscMessage(), l);
        w.getOSCReceiver().addOSCListener("/wek/inputs/bundle", groupl);
    }

    private void messageArrived(String messageName, OSCMessage m) {
        //TODO: CHeck if enabled before doing anything
        //System.out.println("Received " + name);
        if (inputGroup != null && messageName.equals(inputGroup.getOscMessage())) {
            List<Object> o = m.getArguments();
            double d[] = new double[o.size()];
            for (int i = 0; i < o.size(); i++) {
                if (o.get(i) instanceof Float) {
                    d[i] = ((Float) o.get(i));
                } else {
                    Logger.getLogger(InputManager.class.getName()).log(Level.WARNING, "Received feature is not a float");
                }
            }
            if (d.length == currentValues.length) {
                notifyListeners(d);
                System.arraycopy(d, 0, currentValues, 0, d.length);
            } else {
                String msg = "Mismatch in input length: "
                        + "Expected " + currentValues.length + ", received " + o.size();
                w.getStatusUpdateCenter().warn(this, msg);
                notifyListenersOfError();
            }
        }
        //Not sure if we need to store this array within this class, too
    }

    private void bundleArrived(String messageName, OSCMessage m) {
        //TODO

        //TODO: CHeck if enabled before doing anything
        //System.out.println("Received " + name);
        if (inputGroup != null) { try {
            //&& messageName.equals(makeBundleMessage(inputGroup.getOscMessage()))) {
            List<Object> o = m.getArguments();
            if (o == null || o.size() < 1 || !(o.get(0) instanceof String)) {
                String msg = "Unexpected bundle message; require 1 string containing filename";
                w.getStatusUpdateCenter().warn(this, msg);
                notifyListenersOfError();
                return;
            }
            String filename = (String) o.get(0);
            BufferedReader br;
            String line = "";
            String cvsSplitBy = ",";
            LinkedList<List<Double>> data = new LinkedList<>();
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                String[] thisLine = line.split(cvsSplitBy);
                ArrayList<Double> thisData = new ArrayList<>(thisLine.length);
                for (String thisLine1 : thisLine) {
                    thisData.add(Double.parseDouble(thisLine1));
                }
                data.add(thisData);
            }
            notifyBundleListeners(data);
            for (int i = 0; i < currentValues.length; i++) {
                currentValues[i] = data.getLast().get(i);
            }
            
            
            /* int numDatapoints = 0;
            if (o.get(0) instanceof Integer) {
            numDatapoints = (Integer) o.get(0);
            } else {
            String msg = "Unexpected bundle message; require at least 1 datapoint";
            w.getStatusUpdateCenter().warn(this, msg);
            notifyListenersOfError();
            return;
            }
            if (o.size() != (numDatapoints * currentValues.length + 1)) {
            String msg = "Unexpected bundle length: Expected " + numDatapoints + " points";
            w.getStatusUpdateCenter().warn(this, msg);
            notifyListenersOfError();
            return;
            }
            if (numDatapoints > 0) {
            //o.remove(0);
            notifyBundleListeners(o);
            //update currentValues
            for (int i = 0; i < currentValues.length; i++) {
            currentValues[i] = (Float)o.get(o.size()-currentValues.length + i);
            }
            }  */
            /* int dataIndex = 1;
            for (int i = 0; i < numDatapoints; i++) {
            double[] d = new double[currentValues.length];
            for (int j = 0; j < currentValues.length; j++) {
            if (o.get(dataIndex) instanceof Float) {
            d[j] = ((Float) o.get(dataIndex++));
            } else {
            String msg = "Received feature is not a float";
            w.getStatusUpdateCenter().warn(this, msg);
            notifyListenersOfError();
            return;
            }
            }
            notifyListeners(d);
            System.arraycopy(d, 0, currentValues, 0, d.length);
            } */
            } catch (FileNotFoundException ex) {
                logger.log(Level.WARNING, "File not found");
            } catch (IOException ex) {
                Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void notifyListeners(double[] data) {
        for (InputListener l : inputValueListeners) {
            l.update(data);
        }
    }

    private void notifyListenersOfError() {
        for (InputListener l : inputValueListeners) {
            l.notifyInputError();
        }
    }

    private String makeBundleMessage(String oscMessage) {
        return new StringBuilder(oscMessage).append("/bundle").toString();
    }

    private void notifyBundleListeners(List<List<Double>> inputs) {
        for (InputListener l : inputValueListeners) {
            l.updateBundle(inputs);
        }
    }

    public interface InputListener extends EventListener {

        public void update(double[] vals);

        public void updateBundle(List<List<Double>> inputs);

        public void notifyInputError();
    }

    /*public interface InputGroupChangeListener extends EventListener {

     void inputGroupChange(InputGroupChangeEvent evt);
     }*/

    /*public class InputGroupChangeEvent {

     private final String name;
     private final InputGroupChangeType changeType;

     public InputGroupChangeEvent(final String name, final InputGroupChangeType changeType) {
     this.name = name;
     this.changeType = changeType;
     }

     public String getName() {
     return name;
     }

     public InputGroupChangeType getChangeType() {
     return changeType;
     } 
     } */
}
