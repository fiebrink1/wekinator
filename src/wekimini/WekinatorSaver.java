/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import wekimini.Path.PathAndDataLoader;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.learning.dtw.DtwModel;
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
    private final static String dataFilename = File.separator + currentAppend + File.separator + "currentData"; //No extension: Will not be arff for DTW

    private static final Logger logger = Logger.getLogger(WekinatorSaver.class.getName());

    public static Wekinator loadWekinatorFromFile(String wekFilename) throws Exception {
        File wekFile = new File(wekFilename);
        String projectDir = wekFile.getParentFile().getAbsolutePath();

        WekinatorFileData wfd = WekinatorFileData.readFromFile(wekFilename);
        OSCInputGroup ig = loadInputs(projectDir);
        OSCOutputGroup og = loadOutputs(projectDir);
        
        Wekinator w;
        List<Path> paths = null;
        boolean isSupervised = true;
        try {
           paths  = loadPaths(projectDir, og.getNumOutputs());
        } catch (Exception ex) {
            isSupervised = false;
        }
        if (isSupervised) {
           Instances data = loadDataFromArff(projectDir);
           w = instantiateSupervisedWekinator(wfd, ig, og, data, paths, projectDir);
        } else {
            //Temporal modeling
            w = instantiateTemporalWekinator(wfd, ig, og, projectDir);
        } 
        return w;
    }

    public static void createNewProject(String name, File projectDir, Wekinator w) throws IOException {
        createProjectFiles(projectDir);
        saveWekinatorFile(name, projectDir, w);
        if (w != null) {
            saveInputs(projectDir, w);
            saveOutputs(projectDir, w);
            saveData(projectDir, w);
            saveModels(projectDir, w);
        }
    }

    private static void saveData(File projectDir, Wekinator w) throws IOException {
        w.getLearningManager().writeDataToFile(projectDir, dataFilename);
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
                Util.writeBlankFile(projectDir + outputFilename);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not write blank file to file{0}{1}", new Object[]{projectDir, outputFilename});
            }
        }
    }

    private static void saveModels(File projectDir, Wekinator w) throws IOException {
        String directory = projectDir + modelsAppend;
        w.getLearningManager().saveModels(directory, w);
        
        /* List<Path> paths = w.getSupervisedLearningManager().getPaths();
        String location = projectDir + modelsAppend + File.separator;
        for (int i = 0; i < paths.size(); i++) {
            String filename = location + "model" + i + ".xml";
            paths.get(i).writeToFile(filename);
        } */
    }

    private static void saveWekinatorFile(String name, File projectDir, Wekinator w) throws IOException {
        WekinatorFileData wfd;
        if (w == null) {
            wfd = new WekinatorFileData(name);
        } else {
            wfd = new WekinatorFileData(w);
        }
        String filename = projectDir + File.separator + name + "." + WekinatorFileData.FILENAME_EXTENSION;
        wfd.writeToFile(filename);
        KadenzeLogging.getLogger().projectSaved(w, wfd.getProjectName());
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
        String models = f.getAbsolutePath() + modelsAppend;
        new File(models).mkdirs();
        String stash = f.getAbsolutePath() + stashAppend;
        new File(stash).mkdirs();
        //String logs = f.getAbsolutePath() + File.separator + "logs";
        //new File(logs).mkdirs();
    }

    static void saveExistingProject(Wekinator w) throws IOException {
        String name = w.getProjectName();
        File projectDir = new File(w.getProjectLocation());
        saveWekinatorFile(name, projectDir, w);

        saveInputs(projectDir, w);
        saveOutputs(projectDir, w);
        saveData(projectDir, w);
        saveModels(projectDir, w);

    }

    //TODO: take care of this within object static load functions, not here
    private static OSCInputGroup loadInputs(String projectDir) throws IOException {
        OSCInputGroup loaded = OSCInputGroup.readFromFile(projectDir + inputFilename);
        return new OSCInputGroup(loaded);
    }

    private static OSCOutputGroup loadOutputs(String projectDir) throws IOException {
        OSCOutputGroup loaded = OSCOutputGroup.readFromFile(projectDir + outputFilename);
        return new OSCOutputGroup(loaded);
    }

    private static Instances loadDataFromArff(String projectDir) throws IOException {
        ArffLoader al = new ArffLoader();
        al.setFile(new File(projectDir + dataFilename + ".arff"));
        return al.getDataSet();
    }

    private static List<Path> loadPaths(String projectDir, int howMany) throws Exception {
        String pathsDirectory = projectDir + modelsAppend + File.separator;
        List<Path> paths = new ArrayList<>(howMany);
        for (int i = 0; i < howMany; i++) {
            String filename = pathsDirectory + "model" + i + ".xml"; //TODO: need better solution here.
            PathAndDataLoader.tryLoadFromFile(filename);
            Path ptemp = PathAndDataLoader.getLoadedPath();
            //Don't do anything with loaded data, since we're loading all paths for a project here.
            PathAndDataLoader.discardLoaded();
           // Path ptemp = Path.readFromFile(filename); //TODO: take care of this within Path instead
            paths.add(ptemp); //still need to initialise with wekinator, etc. later
        }
        return paths;
    }

    private static Wekinator instantiateTemporalWekinator(WekinatorFileData wfd, OSCInputGroup ig, OSCOutputGroup og, String projectDir) throws IOException {
        Wekinator w = new Wekinator(WekiMiniRunner.generateNextID());
        KadenzeLogging.getLogger().loadedFromFile(w, wfd.getProjectName());
        w.setProjectLocation(projectDir);
        w.setHasSaveLocation(true);
        wfd.applySettings(w);
        w.getInputManager().setOSCInputGroup(ig);
        w.getOutputManager().setOSCOutputGroup(og);
        w.getLearningManager().setDtw(og);//TEST THIS NOW!
        w.getDtwLearningManager().initializeFromExisting(projectDir + modelsAppend + File.separator);
        w.getOSCSender().setHostnameAndPort(InetAddress.getByName(og.getHostname()), og.getOutputPort());
        w.getStatusUpdateCenter().update(null, "Successfully loaded Wekinator project from file.");
        return w;
    
        
    }
    
    //TODO: XXX WON"T WORK ANYMORE: NEED TO SET LEARNING TYPE HERE!
    private static Wekinator instantiateSupervisedWekinator(WekinatorFileData wfd, OSCInputGroup ig, OSCOutputGroup og, Instances data, List<Path> tempPaths, String projectDir) throws IOException {
        Wekinator w = new Wekinator(WekiMiniRunner.generateNextID());
        KadenzeLogging.getLogger().loadedFromFile(w, wfd.getProjectName());
        w.setProjectLocation(projectDir);
        w.setHasSaveLocation(true);
        wfd.applySettings(w);
        w.getInputManager().setOSCInputGroup(ig);
        w.getOutputManager().setOSCOutputGroup(og);
        //w.getLearningManager().setSupervisedLearning(); //TEST THIS NOW!
        List<Path> paths = new LinkedList<>();
        for (Path t : tempPaths) {
            Path p = new Path(t, w);
            paths.add(p);
        }
        
        w.getOSCSender().setHostnameAndPort(InetAddress.getByName(og.getHostname()), og.getOutputPort());
        w.getLearningManager().setSupervisedLearningWithExisting(data, paths);
        // the above calls w.getDataManager().initialize(...) with data
        w.getStatusUpdateCenter().update(null, "Successfully loaded Wekinator project from file.");
        return w;
    }
}
