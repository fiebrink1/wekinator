/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import com.thoughtworks.xstream.XStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import wekimini.learning.Model;
import wekimini.learning.SupervisedLearningModel;

/**
 *
 * @author rebecca
 */
public class PathAndDataLoader {
    private boolean isLoaded = false;
    private Path loadedPath = null;
    private Instances loadedInstances = null;
    private final Logger logger = Logger.getLogger(PathAndDataLoader.class.getName());

    public PathAndDataLoader() {
        
    }
    
    public void tryLoadFromFile(String filename) throws Exception {
        //Danger: Will not have any transient fields initialised!
        Path p = null;
        FileInputStream instream = null;
        ObjectInputStream objin = null;
        Object o = null;
        boolean err = false;
        Exception myEx = new Exception();
        try {
            instream = new FileInputStream(filename);
            objin = new ObjectInputStream(instream);
            // o = objin.readObject();
            String xml = (String) objin.readObject();
            XStream xstream = new XStream();
            xstream.alias("Path", Path.class);
            p = (Path) xstream.fromXML(xml);
            //Model builder contains transients and will not be initialised properly by xstream,
            //so initialise it
            p.initialiseModelBuilder();
            
            String modelClassName = (String) objin.readObject();
            Model m = null;
            if (!modelClassName.equals("null")) {
                Class c = Class.forName(modelClassName);
                m = ModelLoader.loadModel(c, objin);
            }
            p.setModel((SupervisedLearningModel) m);

            try {
                String instancesString = (String) objin.readObject();
                if (!instancesString.equals("null")) {
                    ArffLoader al = new ArffLoader();
                    al.setSource(objin);
                    loadedInstances = al.getDataSet();
                } else {
                    loadedInstances = null;
                }
            } catch (Exception ex) {
                //Could not load instances: not necessarily a problem
                logger.log(Level.WARNING, "No instances found in path file; not loading them");
                loadedInstances = null;
            }
        } catch (Exception ex) {
            myEx = ex;
            err = true;
            
            logger.log(Level.WARNING, "Error encountered in reading from file: {0}", ex.getMessage());
        } finally {
            try {
                if (objin != null) {
                    objin.close();
                }
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Encountered error closing file objects");
            }
        }
        if (err) {
            throw myEx;
        }
        loadedPath = p;
        isLoaded = true;
    }

    public Path getLoadedPath() {
        if (isLoaded) {
            return loadedPath;
        } else {
            logger.log(Level.WARNING, "Attempt to load path but isLoaded is false");
            return null;
        }
    }

    //TODO: getSelectedInputs() isn't ordered yet
    public String[] getOrderedInputNamesForLoadedPath() {
        if (isLoaded) {
            return loadedPath.getSelectedInputs();
        } else {
            logger.log(Level.WARNING, "Attempt to load input names but isLoaded is false");
            return null;
        }
    }

    //Returns null if no instances could be loaded
    public Instances getLoadedInstances() {
        if (isLoaded) {
            return loadedInstances;
            //If no instances, fail gracefully?
        } else {
            logger.log(Level.WARNING, "Attempt to load instances but isLoaded is false");
            return null;
        }
    }

    public void discardLoaded() {
        loadedPath = null;
        loadedInstances = null;
        isLoaded = false;
    }
    
}
