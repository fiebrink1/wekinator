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
        oscReceiverListener = this::oscReceiverPropertyChanged;
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
    
    public OSCInputGroup getOSCInputGroup(String name) {
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

    private void addOSCInputListener(OSCInputGroup g) {
        OSCListener l = new OSCListener() {
            @Override
            public void acceptMessage(Date date, OSCMessage oscm) {
                messageArrived(g.getOscMessage(), oscm);
            }
        };
        w.getOSCReceiver().addOSCListener(g.getOscMessage(), l);
    }

    private void messageArrived(String messageName, OSCMessage m) {
        //TODO: CHeck if enabled before doing anything
        //System.out.println("Received " + name);
        if (inputGroup != null && messageName.equals(inputGroup.getOscMessage())) {
            Object[] o = m.getArguments();
                double d[] = new double[o.length];
                for (int i = 0; i < o.length; i++) {
                    if (o[i] instanceof Float) {
                        d[i] = ((Float) o[i]);
                    } else {
                        Logger.getLogger(InputManager.class.getName()).log(Level.WARNING, "Received feature is not a float");
                    }
                }
                if (d.length == currentValues.length) {
                    notifyListeners(d);
                    System.arraycopy(d, 0, currentValues, 0, d.length);
                } else {
                    String msg = "Error: mismatch in input length: "
                            + "Expected " + d.length + ", received " + o.length;
                    System.out.println(msg);
                    Logger.getLogger(InputManager.class.getName()).log(Level.WARNING, "msg");

                }
        }
        //Not sure if we need to store this array within this class, too
    }

    private void notifyListeners(double[] data) {
        for(InputListener l : inputValueListeners) {
            l.update(data);
        }
    }

    public interface InputListener extends EventListener {
        public void update(double[] vals);
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
