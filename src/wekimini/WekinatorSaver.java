/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.osc.OSCInputGroup;
import wekimini.osc.OSCOutputGroup;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class WekinatorSaver {
    
    private final static String currentAppend = File.separator + "current";
  //  private final static String dataAppend = File.separator + currentAppend + File.separator + "data";
    private final static String modelsAppend = File.separator + currentAppend + File.separator + "models";
    private final static String stashAppend = File.separator + "saved";
    private final static String inputFilename = File.separator + "inputConfig.xml";
    private final static String outputFilename = File.separator + "outputConfig.xml";
    private final static String dataFilename = File.separator + currentAppend + File.separator + "currentData.arff";

    private static final Logger logger = Logger.getLogger(WekinatorSaver.class.getName());
    
    public static void createNewProject(String name, File projectDir, Wekinator w) throws IOException {
        createProjectFiles(projectDir);
        saveWekinatorFile( name,  projectDir,  w);
        if (w != null) {
            saveInputs(projectDir, w);
            saveOutputs(projectDir, w);
            saveData(projectDir, w);
            saveModels(projectDir, w);  
        }
    }
    
    private static void saveData(File projectDir, Wekinator w) throws IOException {
        File f = new File(projectDir + dataFilename);
        w.getDataManager().writeInstancesToArff(f);
    }
    
    private static void saveInputs(File projectDir, Wekinator w) throws IOException {
        OSCInputGroup g = w.getInputManager().getOSCInputGroup();
        if (g != null) {
            g.writeToFile(projectDir + inputFilename); 
        } else {
            try {
                Util.writeBlankFile(projectDir + inputFilename);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not write blank file to file{0}{1}", new Object[]{projectDir, inputFilename});
            }
        }
    }
    
    private static void saveOutputs(File projectDir, Wekinator w) throws IOException {
        OSCOutputGroup g = w.getOutputManager().getOutputGroup();
        if (g != null) {
            g.writeToFile(projectDir + outputFilename); 
        } else {
            try {
                Util.writeBlankFile(projectDir + inputFilename);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not write blank file to file{0}{1}", new Object[]{projectDir, inputFilename});
            }
        }
    }
   
    
    private static void saveModels(File projectDir, Wekinator w) {
      //  throw new UnsupportedOperationException("Not implemented yet");
    }
    
    private static void saveWekinatorFile(String name, File projectDir, Wekinator w) {
        if (w == null) {
            //Just save name somewhere?
        } else {
            //What to save?
            //OSC receive port
        }
    }
    
    private static void createProjectFiles(File f) throws SecurityException {
        f.mkdirs();
        //String inputsName = f.getAbsolutePath() + File.separator + "inputs";
        //new File(inputsName).mkdirs();
        //String outputsName = f.getAbsolutePath() + File.separator + "outputs";
        //new File(outputsName).mkdirs();
        String currentLearners = f.getAbsolutePath() + currentAppend;
        new File(currentLearners).mkdirs();
      //  String data = f.getAbsolutePath() + dataAppend;
      //  new File(data).mkdirs();
        String models = f.getAbsolutePath() +  modelsAppend;
        new File(models).mkdirs();
        String stash = f.getAbsolutePath() + stashAppend;
        new File(stash).mkdirs();
        //String logs = f.getAbsolutePath() + File.separator + "logs";
        //new File(logs).mkdirs();
    }

    static void saveExistingProject(Wekinator aThis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
