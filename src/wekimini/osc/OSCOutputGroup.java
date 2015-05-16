/*
 * A group of inputs, sent as a single OSC message
 */
package wekimini.osc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCOutputGroup {
    private final String oscMessage; //Does make sense to save this with OSC output group
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
    
    public String[] getOutputNames() {
        String s[] = new String[outputs.size()];
        for (int i = 0; i < outputs.size(); i++) {
            s[i] = outputs.get(i).getName();
        }
        return s;
    }

    public String getOscMessage() {
        return oscMessage;
    }
    
    
    
    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public OSCOutputGroup(List<OSCOutput> outputs, String oscMessage) 
    {
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must be a non-null list with at least one element");
        }
        this.outputs = new LinkedList<>(outputs);
        values = new float[outputs.size()];
        this.oscMessage = oscMessage; //will copy value
    }
    
    public OSCOutput getOutput(int which) {
        return outputs.get(which);
    }

    @Override
    public String toString() {
        return Util.toXMLString(this, "OSCOutputGroup", OSCOutputGroup.class);
    }

    public void writeToFile(String filename) throws IOException {
       Util.writeToXMLFile(this, "OSCOutputGroup", OSCOutputGroup.class, filename);
    }
    
    public static OSCOutputGroup readFromFile(String filename) throws Exception {
        OSCOutputGroup g = (OSCOutputGroup) Util.readFromXMLFile("OSCOutputGroup", OSCOutputGroup.class, filename);
        return g;
    }

    
    public static void main(String[] args) {
       
    }
    
}
