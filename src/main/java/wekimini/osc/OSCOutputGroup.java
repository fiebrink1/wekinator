/*
 * A group of inputs, sent as a single OSC message
 */
package wekimini.osc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCOutputGroup {
    private String oscMessage; //When all group values are being sent together, this is default message name
    private String hostName;
    private int outputPort;
    private final List<OSCOutput> outputs;
    private static final Logger logger = Logger.getLogger(OSCOutputGroup.class.getName());
    
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
    
    public String getHostname() {
        return hostName;
    }
    
    public int getOutputPort() {
        return outputPort;
    }

    public void setOscMessage(String oscMessage) {
        this.oscMessage = oscMessage;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setOutputPort(int outputPort) {
        this.outputPort = outputPort;
    }
    
    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public OSCOutputGroup(List<OSCOutput> outputs, String oscMessage, String hostname, int outputPort) 
    {
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must be a non-null list with at least one element");
        }
        this.outputs = new LinkedList<>(outputs);
        //values = new float[outputs.size()];
        this.oscMessage = oscMessage; //will copy value
        this.hostName = hostname;
        this.outputPort = outputPort;
    }
    
    public OSCOutputGroup(OSCOutputGroup groupFromFile) {
        this.outputs = new LinkedList<>(groupFromFile.getOutputs());
        //values = new float[outputs.size()];
        this.oscMessage = groupFromFile.oscMessage; //will copy value
        this.hostName = groupFromFile.hostName;
        this.outputPort = groupFromFile.outputPort;
    }
    
    public int getOutputNumber(OSCOutput o) {
        for (int i= 0; i < outputs.size(); i++) {
            if (outputs.get(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }
    
    //Output changed by gui (e.g. change # of classes)
    public void updateOutput(OSCOutput o, int index) {
        if (index < outputs.size()) {
            outputs.set(index, o); //replace it
        } else {
            logger.log(Level.WARNING, "Illegal output index value: {0}", index);
        }
    }
    
    public List<OSCOutput> getOutputs() {
        return outputs;
    }
    
    public OSCOutput getOutput(int which) {
        return outputs.get(which);
    }

    public String toString() {
        return Util.toXMLString(this, "OSCOutputGroup", OSCOutputGroup.class);
    }

    public void writeToFile(String filename) throws IOException {
       Util.writeToXMLFile(this, "OSCOutputGroup", OSCOutputGroup.class, filename);
    }
    
    public static OSCOutputGroup readFromFile(String filename) throws IOException {
        OSCOutputGroup g = (OSCOutputGroup) Util.readFromXMLFile("OSCOutputGroup", OSCOutputGroup.class, filename);
        return g;
    }

    
    public static void main(String[] args) {
       
    }
    
}
