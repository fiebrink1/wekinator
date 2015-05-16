/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.IOException;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class WekinatorFileData {
    private int oscReceivePort = 6448;
    private String projectName = "";
    public static final String FILENAME_EXTENSION = "wekproj";

    public int getOscReceivePort() {
        return oscReceivePort;
    }

    public void setOscReceivePort(int oscReceivePort) {
        this.oscReceivePort = oscReceivePort;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
        
    public WekinatorFileData(Wekinator w) {
        this.oscReceivePort = w.getOSCReceiver().getReceivePort();
        this.projectName = w.getProjectName();
    }
    
    public WekinatorFileData(String projectName) {
        this.projectName = projectName;
    }
    
    @Override
    public String toString() {
        return Util.toXMLString(this, "WekinatorSaveData", WekinatorFileData.class);
    }
    
    public static WekinatorFileData readFromFile(String filename) throws Exception {
        WekinatorFileData w = (WekinatorFileData) Util.readFromXMLFile("WekinatorFileData", WekinatorFileData.class, filename);
        return w;
        /*InputStream in = new FileInputStream(filename);
        XStream xstream = new XStream();
        xstream.alias("WekinatorFileData", WekinatorFileData.class);
        in.close();
        return (WekinatorFileData) xstream.fromXML(in);  */
    }
    
    public void writeToFile(String filename) throws IOException {
        Util.writeToXMLFile(this, "WekinatorFileData", WekinatorFileData.class, filename);

        /*FileOutputStream fos = null;
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
    
    
}
