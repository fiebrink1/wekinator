/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class Settings {

    private String projectName;
    private boolean isLoggingToFile = false;

    public Settings(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean isIsLoggingToFile() {
        return isLoggingToFile;
    }

    public void setIsLoggingToFile(boolean isLoggingToFile) {
        this.isLoggingToFile = isLoggingToFile;
    }

    public void writeToFile(File file) throws FileNotFoundException, IOException {
        FileOutputStream fos = null;
        FileNotFoundException fnfEx = null;
        IOException ioEx = null;

        try {
            XStream xstream = new XStream();
            xstream.alias("Settings", Settings.class);
            //String xml = xstream.toXML(this);
            //System.out.println(xml);
            fos = new FileOutputStream(file + File.separator + "settings.xml");
            fos.write("<?xml version=\"1.0\"?>\n".getBytes("UTF-8")); //write XML header, as XStream doesn't do that for us
            xstream.toXML(this, fos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            fnfEx = ex;
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            ioEx = ex;

        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                ioEx = ex;
            }
        }
        if (fnfEx != null) {
            throw fnfEx;
        } else if (ioEx != null) {
            throw ioEx;
        }
    }

    public static Settings readFromFile(String filename) throws Exception {
        XStream xstream = new XStream();
        xstream.alias("Settings", Settings.class);
        return (Settings) xstream.fromXML(new File(filename));
    }

}
