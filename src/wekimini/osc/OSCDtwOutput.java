/*
 * Information about an output, not the value of the output itself (that is handled by manager)
 */
package wekimini.osc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;
import wekimini.util.Util;

/**
 * XXX in future: need to know when to send: - whenever new feature received -
 * whenever new output gesture matched
 *
 * - send all as bundle vs send individual messages (one per output), or both
 *
 * @author rebecca
 */
public class OSCDtwOutput implements OSCOutput {

    private String name;
    public static final String PROP_NAME = "name";
    private int numGestures;
    public static final String PROP_NUMGESTURES = "numGestures";
    private String[] gestureNames;
    public static final String PROP_GESTURE_NAMES = "gestureNames";
    private String[] gestureOscMessages;
    public static final String PROP_GESTURE_OSC_MESSAGES = "gestureOscMessages";
    private String outputOscMessage;
    //Removed final for XML de-serialization support
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public String getOutputOscMessage() {
        return outputOscMessage;
    }
    
    public void setOutputOscMessage(String m) {
        outputOscMessage = m;
    }
            


    
    public OSCDtwOutput(String name, int numGestures) {
        this.name = name;
        this.numGestures = numGestures;
        this.gestureNames = new String[numGestures];
        this.gestureOscMessages = new String[numGestures];
        populateNames();
        this.outputOscMessage = name;
        populateOscMessagesFromNames();
    }
    
    /**
     * Get the value of gestureOscMessages
     *
     * @return the value of gestureOscMessages
     */
    public String[] getGestureOscMessages() {
        return gestureOscMessages;
    }

    /**
     * Set the value of gestureOscMessages
     *
     * @param gestureOscMessages new value of gestureOscMessages
     */
    public void setGestureOscMessages(String[] gestureOscMessages) {
        String[] oldGestureOscMessages = this.gestureOscMessages;
        this.gestureOscMessages = gestureOscMessages;
        propertyChangeSupport.firePropertyChange(PROP_GESTURE_OSC_MESSAGES, oldGestureOscMessages, gestureOscMessages);
    }

    /**
     * Get the value of gestureOscMessages at specified index
     *
     * @param index the index of gestureOscMessages
     * @return the value of gestureOscMessages at specified index
     */
    public String getGestureOscMessages(int index) {
        return this.gestureOscMessages[index];
    }

    /**
     * Set the value of gestureOscMessages at specified index.
     *
     * @param index the index of gestureOscMessages
     * @param gestureOscMessages new value of gestureOscMessages at specified
     * index
     */
    public void setGestureOscMessages(int index, String gestureOscMessages) {
        String oldGestureOscMessages = this.gestureOscMessages[index];
        this.gestureOscMessages[index] = gestureOscMessages;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_GESTURE_OSC_MESSAGES, index, oldGestureOscMessages, gestureOscMessages);
    }

    /**
     * Get the value of numGestures
     *
     * @return the value of numGestures
     */
    public int getNumGestures() {
        return numGestures;
    }

    /**
     * Set the value of numGestures
     *
     * @param numGestures new value of numGestures
     */
    public void setNumGestures(int numGestures) {
        int oldNumGestures = this.numGestures;
        this.numGestures = numGestures;
        propertyChangeSupport.firePropertyChange(PROP_NUMGESTURES, oldNumGestures, numGestures);
    }

    /**
     * Get the value of gestureNames
     *
     * @return the value of gestureNames
     */
    public String[] getGestureNames() {
        return gestureNames;
    }

    /**
     * Set the value of gestureNames
     *
     * @param gestureNames new value of gestureNames
     */
    public void setGestureNames(String[] gestureNames) {
        String[] oldGestureNames = this.gestureNames;
        this.gestureNames = gestureNames;
        propertyChangeSupport.firePropertyChange(PROP_GESTURE_NAMES, oldGestureNames, gestureNames);
    }

    /**
     * Get the value of gestureNames at specified index
     *
     * @param index the index of gestureNames
     * @return the value of gestureNames at specified index
     */
    public String getGestureNames(int index) {
        return this.gestureNames[index];
    }

    /**
     * Set the value of gestureNames at specified index.
     *
     * @param index the index of gestureNames
     * @param gestureNames new value of gestureNames at specified index
     */
    public void setGestureNames(int index, String gestureNames) {
        String oldGestureNames = this.gestureNames[index];
        this.gestureNames[index] = gestureNames;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_GESTURE_NAMES, index, oldGestureNames, gestureNames);
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, name);
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

    private void populateOscMessagesFromNames() {
        for (int i = 0; i < numGestures; i++) {
            gestureOscMessages[i] = "/" + gestureNames[i];
        }
    }

    private void populateNames() {
        for (int i = 0; i < gestureNames.length; i++) {
            gestureNames[i] = name + "_" + (i + 1);
        }
    }

    @Override
    public String toString() {
        return Util.toXMLString(this, "OSCDtwOutput", OSCDtwOutput.class);
    }

    @Override
    public double generateRandomValue() {
        Random r = new Random();
        int i = r.nextInt(numGestures+1);
        return i;
    }

    @Override
    public double getDefaultValue() {
        return 1;
    }

    @Override
    public boolean isLegalTrainingValue(double value) {
        return (value > 0 && value <= numGestures); 
    }

    @Override
    public boolean isLegalOutputValue(double value) {
        if (value < 0 || value > numGestures) { //out of range
            return false;
        }
        return Util.isInteger(value); //is it really an int?
    }

    @Override
    public double forceLegalTrainingValue(double value) {
        //return forceLegalOutputValue(value);
        if (value <= 0) {
            return 1;
        } 
        if (value > numGestures) {
            return numGestures;
        }
        return value;
    }

    @Override
    public double forceLegalOutputValue(double value) {
        int which = (int) value;
        if (which < 0) {
            which = 1;
        }
        if (which > numGestures) {
            which = numGestures;
        }
        return which;
    }
    
  
    
    private Object readResolve() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        return this;
  }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DTW,NAME=").append(name);
        sb.append(",NUM_GEST=").append(numGestures);
        sb.append(",GESTURE_NAMES=");
        for (int i = 0; i < numGestures; i++) {
            sb.append(gestureNames[i]).append(',');
        }
        return sb.toString();
    }
    
}
