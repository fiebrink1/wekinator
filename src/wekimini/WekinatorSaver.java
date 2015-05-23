/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
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

    public static Wekinator loadWekinatorFromFile(String wekFilename) throws Exception {
        File wekFile = new File(wekFilename);
        String projectDir = wekFile.getParentFile().getAbsolutePath();

        WekinatorFileData wfd = WekinatorFileData.readFromFile(wekFilename);
        OSCInputGroup ig = loadInputs(projectDir);
        OSCOutputGroup og = loadOutputs(projectDir);
        Instances data = loadDataFromArff(projectDir);
        List<Path> paths = loadPaths(projectDir, og.getNumOutputs());
        Wekinator w = instantiateWekinator(wfd, ig, og, data, paths, projectDir);
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

    private static void saveModels(File projectDir, Wekinator w) throws IOException {
        List<Path> paths = w.getLearningManager().getPaths();
        String location = projectDir + modelsAppend + File.separator;
        for (int i = 0; i < paths.size(); i++) {
            String filename = location + "model" + i + ".xml";
            paths.get(i).writeToFile(filename);
        }
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
    private static OSCInputGroup loadInputs(String projectDir) throws Exception {
        OSCInputGroup loaded = OSCInputGroup.readFromFile(projectDir + inputFilename);
        return new OSCInputGroup(loaded);
    }

    private static OSCOutputGroup loadOutputs(String projectDir) throws Exception {
        OSCOutputGroup loaded = OSCOutputGroup.readFromFile(projectDir + outputFilename);
        return new OSCOutputGroup(loaded);
    }

    private static Instances loadDataFromArff(String projectDir) throws IOException {
        ArffLoader al = new ArffLoader();
        al.setFile(new File(projectDir + dataFilename));
        return al.getDataSet();
    }

    private static List<Path> loadPaths(String projectDir, int howMany) throws Exception {
        String pathsDirectory = projectDir + modelsAppend + File.separator;
        List<Path> paths = new ArrayList<>(howMany);
        for (int i = 0; i < howMany; i++) {
            String filename = pathsDirectory + "model" + i + ".xml";
            Path ptemp = Path.readFromFile(filename); //TODO: take care of this within Path instead
            paths.add(ptemp); //still need to initialise with wekinator, etc. later
        }
        return paths;
    }

    private static Wekinator instantiateWekinator(WekinatorFileData wfd, OSCInputGroup ig, OSCOutputGroup og, Instances data, List<Path> tempPaths, String projectDir) throws IOException {
        Wekinator w = new Wekinator();
        w.setProjectLocation(projectDir);
        w.setHasSaveLocation(true);
        wfd.applySettings(w);
        w.getInputManager().setOSCInputGroup(ig);
        w.getOutputManager().setOSCOutputGroup(og);
        List<Path> paths = new LinkedList<>();
        for (Path t : tempPaths) {
            Path p = new Path(t, w);
            paths.add(p);
        }
        w.getLearningManager().initializeInputsAndOutputsWithExisting(data, paths);
        // the above calls w.getDataManager().initialize(...) with data
        w.getMainGUI().initializeInputsAndOutputs();
        w.getStatusUpdateCenter().update(null, "Successfully loaded Wekinator project from file.");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return w;
    }
}
