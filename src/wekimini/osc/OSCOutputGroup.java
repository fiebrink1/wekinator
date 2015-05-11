/*
 * A group of inputs, sent as a single OSC message
 */
package wekimini.osc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCOutputGroup {
   //private final String oscMessage;
   // private final String hostName;
   // private final int outputPort;
    private final List<OSCOutput> outputs;
    private float[] values;
    
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public int getNumOutputs() {
        return outputs.size();
    }
    
    //Requires both arrays are same length
    public void setValues(float[] values) {
        //TODO: check if this is really necessary
        System.arraycopy(values, 0, this.values, 0, values.length);
    }
    
    public void setValue(int index, float value) {
        values[index] = value;
    }
    
    //Danger: values can be modified by caller
    public float[] getValues() {
        return values;
    }
    
    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public OSCOutputGroup(List<OSCOutput> outputs) 
    {
        if (outputs == null || outputs.size() == 0) {
            throw new IllegalArgumentException("outputs must be a non-null list with at least one element");
        }
        this.outputs = new LinkedList<>(outputs);
        values = new float[outputs.size()];
    }
    
    public OSCOutput getOutput(int which) {
        return outputs.get(which);
    }

    @Override
    public String toString() {
        return Util.toXMLString(this, "OSCOutputGroup", OSCOutputGroup.class);
    }

    
    public static void main(String[] args) {
       
    }
    
}
