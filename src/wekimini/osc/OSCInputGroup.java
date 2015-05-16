/*
 * A group of inputs, sent as a single OSC message
 */
package wekimini.osc;

import com.thoughtworks.xstream.XStream;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCInputGroup {
    private final String oscMessage;
    private final int numInputs;
    private final String groupName;
    private final String[] inputNames;

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

    public OSCInputGroup(String groupName, String oscMessage, int numInputs, String[] inputNames) {
        if (inputNames == null || inputNames.length != numInputs) {
            throw new IllegalArgumentException("inputNames[] must have size equal to numInputs");
        } 
        if (! Util.checkAllUnique(inputNames)) {
            throw new IllegalArgumentException("Input names must be unique");
        }
        this.oscMessage = oscMessage;
        this.numInputs = numInputs;
        this.groupName = groupName;
        
        this.inputNames = new String[numInputs];
        System.arraycopy(inputNames, 0, this.inputNames, 0, numInputs); 
    }

    public String getOscMessage() {
        return oscMessage;
    }

    public int getNumInputs() {
        return numInputs;
    }

    public String getGroupName() {
        return groupName;
    }

    public String[] getInputNames() {
        return inputNames;
    }

   public void writeToFile(String filename) throws IOException {
      Util.writeToXMLFile(this, "OSCInputGroup", OSCInputGroup.class, filename);

      /* FileOutputStream fos = null;
        try {
            XStream xstream = new XStream();
            xstream.alias("OSCInputGroup", OSCInputGroup.class);
            //String xml = xstream.toXML(this);
            //System.out.println(xml);
            fos = new FileOutputStream(filename);
            fos.write("<?xml version=\"1.0\"?>\n".getBytes("UTF-8")); //write XML header, as XStream doesn't do that for us
            xstream.toXML(this, fos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OSCInputGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OSCInputGroup.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(OSCInputGroup.class.getName()).log(Level.SEVERE, null, ex);
            }
        } */
    }

    public static OSCInputGroup readFromFile(String filename) throws Exception {
      OSCInputGroup g = (OSCInputGroup) Util.readFromXMLFile("OSCInputGroup", OSCInputGroup.class, filename);
      return g;
        
        /*  XStream xstream = new XStream();
        xstream.alias("OSCInputGroup", OSCInputGroup.class);
        try {
            return (OSCInputGroup)xstream.fromXML(new File(filename));
        } catch (Exception ex) {
            System.out.println("exception: " + ex.toString());
            return null; //TODO
        } */
    } 

    @Override
    public String toString() {
         XStream xstream = new XStream();
         xstream.alias("OSCInputGroup", OSCInputGroup.class);
         return xstream.toXML(this);
    }
    
    
    public static void main(String[] args) {
        String inputNames[] = {"123", "456"};
        OSCInputGroup g = new OSCInputGroup("myGroup", "/hi/123", 2, inputNames);
       try {
            g.writeToFile("/Users/rebecca/test1.xml");
            OSCInputGroup g2 = OSCInputGroup.readFromFile("/Users/rebecca/test1.xml");
            System.out.println(g2);      
        } catch (Exception ex) {
            Logger.getLogger(OSCInputGroup.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
}
