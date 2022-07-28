/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import com.timeseries.TimeSeries;
import java.beans.PropertyChangeEvent; //test
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;
import weka.core.Instances;
import wekimini.GlobalSettings;
import static wekimini.InputManager.PROP_INPUTGROUP;
import wekimini.LearningManager;
import wekimini.LearningModelBuilder;
import wekimini.OutputManager;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.kadenze.KadenzeAssignment.KadenzeAssignmentType;
import wekimini.learning.ModelBuilder;
import wekimini.learning.dtw.DtwModel;
import wekimini.learning.dtw.DtwSettings;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCInputGroup;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
public class AssignmentFinalLogger implements KadenzeLogger {

    private static final int version = 2;
    private static final String dateString = "2016/04/02";
    private FileOutputStream fos = null;
    private OutputStreamWriter osw = null;
    private PrintWriter pw = null;
    private BufferedWriter bw = null;
    private String parentDir = ""; //Place in which this assignment directory lives
    private String currentAssignmentDir = ""; //Directory for this assignment
    private int modelSetID = 1; //For choosing model xml file names
    private KadenzeAssignmentType currentAssignmentType;
    private static final Logger logger = Logger.getLogger(AssignmentFinalLogger.class.getName());
    private boolean hasLoggedModelsInThisAssignment = false;

    @Override
    public void beginLog(String parentDir, KadenzeAssignment.KadenzeAssignmentType a) throws IOException {
        this.parentDir = parentDir;
        currentAssignmentType = a;
        currentAssignmentDir = getAssignmentDirectory(parentDir, a);
        File dir = new File(currentAssignmentDir);
        dir.mkdirs();
        File f = getAssignmentFilename(dir, a);
        doAssignmentSetup(a);

        fos = new FileOutputStream(f, true);
        osw = new OutputStreamWriter(fos, "UTF-8");
        bw = new BufferedWriter(osw);
        pw = new PrintWriter(bw);

        logVersionNumberAndDate();
        assignmentStarted(a);
        hasLoggedModelsInThisAssignment = false;
    }

    private void doAssignmentSetup(KadenzeAssignmentType a) {
        GlobalSettings gs = GlobalSettings.getInstance();
        int lastModelSetID = gs.getIntValue("modelSetID", 0);
        modelSetID = lastModelSetID + 1;
    }

    private File getAssignmentFilename(File dir, KadenzeAssignmentType a) {
        String myFile = dir + File.separator + KadenzeAssignment.getAssignmentLogfilename(a);
        return new File(myFile);
    }

    private String getAssignmentDirectory(String parent, KadenzeAssignmentType a) {
        return parent + File.separator + KadenzeAssignment.getLogDirectory(a) + File.separator;
    }

    @Override
    public void flush() {
        try {
            pw.flush(); //TODO: Need to flush other writers as well?
            bw.flush();
            osw.flush();
            fos.flush();
        } catch (IOException ex) {
            Logger.getLogger(AssignmentFinalLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override //CloseLog called when AppleQ hit, whereas project closed called when window X hit
    public void closeLog() {
        logClose();
        pw.flush();
        pw.close();
    }

    @Override //Add record mask TODO
    public void supervisedLearningRecordStarted(Wekinator w) {
        pw.println(ts() + "," + w.getID() + ",SUPERVISED_RECORD_START," + (w.getSupervisedLearningManager().getRecordingRound() + 1));
    }

    @Override
    public void supervisedLearningRecordStopped(Wekinator w) {
        pw.println(ts() + "," + w.getID() + ",SUPERVISED_RECORD_STOP," + w.getSupervisedLearningManager().getRecordingRound());
    }

    @Override
    public void logEvent(Wekinator w, KEvent ke) {
        pw.println(ts() + "," + w.getID() + "," + ke);
    }

    @Override
    public void newProjectStarted(final Wekinator w) {
        pw.println(ts() + "," + w.getID() + ",NEW_PROJECT_STARTED");
        addListeners(w);
    }

    private void addListeners(final Wekinator w) {
        w.getInputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PROP_INPUTGROUP)) {
                    logInputGroupUpdate((OSCInputGroup) evt.getNewValue(), w.getID());
                }
            }
        });

        w.getOutputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OutputManager.PROP_OUTPUTGROUP)) {
                    logOutputGroupUpdate((OSCOutputGroup) evt.getNewValue(), w.getID());
                }
            }
        });

        w.getLearningManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(LearningManager.PROP_LEARNINGTYPE)) {
                    logLearningTypeStarted(w);
                }
            }
        });

        // KadenzeLogging.getLogger().newProjectStarted(this);
    }

    @Override
    public void loadedFromFile(Wekinator w, String projectName) {
        pw.println(ts() + "," + w.getID() + ",PROJECT_LOADED," + projectName);
        addListeners(w);
    }

    private void logLearningTypeStarted(final Wekinator w) {
        if (w.getLearningManager().getLearningType() == LearningManager.LearningType.SUPERVISED_LEARNING) {
            pw.println(ts() + "," + w.getID() + ",SUPERVISED_LEARNING_START");
        } else if (w.getLearningManager().getLearningType() == LearningManager.LearningType.TEMPORAL_MODELING) {
            pw.println(ts() + "," + w.getID() + ",TEMPORAL_MODELING_START");
        } else {
            pw.println(ts() + "," + w.getID() + ",OTHER_MODEL_START");
        }
    }

    private void logInputGroupUpdate(OSCInputGroup newG, int id) {
        StringBuilder sb = new StringBuilder();
        sb.append(ts()).append(',').append(id).append(",INPUT_GROUP_UPDATE");

        try {
            String[] ins = newG.getInputNames();
            for (String in : ins) {
                sb.append(',').append(in);
            }
            pw.println(sb.toString());
        } catch (Exception ex) {
            sb.append("ERROR_ENCOUNTERED");
            pw.println(sb.toString());
        }
    }

    private void logOutputGroupUpdate(OSCOutputGroup newG, int id) {
        StringBuilder sb = new StringBuilder();
        sb.append(ts()).append(',').append(id).append(",OUTPUT_GROUP_UPDATE");
        sb.append(",").append(newG.getNumOutputs());

        for (int i = 0; i < newG.getNumOutputs(); i++) {
            if (newG.getOutput(i) instanceof OSCNumericOutput) {
                sb.append(',').append("N");
            } else if (newG.getOutput(i) instanceof OSCClassificationOutput) {
                sb.append(',').append("C");
            } else {
                sb.append(',').append("D");
            }
        }

        try {
            String[] ins = newG.getOutputNames();
            for (String in : ins) {
                sb.append(',').append(in);
            }
            pw.println(sb.toString());
        } catch (Exception ex) {
            sb.append("ERROR_ENCOUNTERED");
            pw.println(sb.toString());
        }
    }

    //Not used anywhere...
    private void printModelInfo(Wekinator w) {
        StringBuilder sb = new StringBuilder();
        sb.append(ts()).append(",").append(w.getID()).append(",MODEL_INFO_LIST");
        try {
            if (w.getLearningManager().getLearningType() == LearningManager.LearningType.SUPERVISED_LEARNING) {
                sb.append(",SUPERVISED");
                List<Path> paths = w.getSupervisedLearningManager().getPaths();
                for (Path p : paths) {
                    sb.append(',').append(p.getModelBuilder().getPrettyName());
                }
            } else if (w.getLearningManager().getLearningType() == LearningManager.LearningType.TEMPORAL_MODELING) {
                sb.append(",TEMPORAL");
                int num = w.getDtwLearningManager().getModel().getNumGestures();
                for (int i = 0; i < num; i++) {
                    sb.append(',').append(w.getDtwLearningManager().getModel().getGestureName(i));
                }
            } else {
                //Initialization
                sb.append(",INITIALIZATION");
            }
            sb.append('\n');
            pw.print(sb.toString());
        } catch (Exception ex) {
            sb.append("ERROR_ENCOUNTERED\n");
            pw.print(sb.toString());
        }
    }

    @Override
    public void projectSaved(Wekinator w, String projectName) {
        pw.println(ts() + "," + w.getID() + ",PROJECT_SAVED," + projectName);
    }

    @Override //Supervised learning only
    public void examplesDeletedForModel(Wekinator w, int modelNum) {
        pw.println(ts() + "," + w.getID() + ",MODEL_EXAMPLES_DELETED," + modelNum);
    }

    @Override
    //DTWTESTED
    public void dtwGestureAdded(Wekinator w, int gestureNum) {
        pw.println(ts() + "," + w.getID() + ",DTW_GESTURE_ADDED,GESTURE=" + gestureNum);
    }

    @Override //TODO
    public void crossValidationComputed(Wekinator w, int modelNum, int numFolds, String val) {
        //Probably also need to output all model info & data here so we know what is being tested
        //(E.g., model params may have been changed since last rn, training data may have changed)
        ModelBuilder mmb = w.getSupervisedLearningManager().getPaths().get(modelNum).getModelBuilder();
        pw.println(ts() + "," + w.getID() + ",CROSS_VALIDATATION,MODEL_NUM=" + modelNum + ",NUM_FOLDS=" + numFolds + ",VAL=" + val + ",MODEL=" + mmb.toLogString());
    }

    @Override
    public void trainingAccuracyComputed(Wekinator w, int modelNum, String val) {
        ModelBuilder mmb = w.getSupervisedLearningManager().getPaths().get(modelNum).getModelBuilder();
        pw.println(ts() + "," + w.getID() + ",TRAIN_ACCURACY,MODEL_NUM=" + modelNum + ",VAL=" + val + ",MODEL=" + mmb.toLogString());
    }

    @Override
    public void selectedFeatures(Wekinator w, boolean[][] oldConnections, boolean[][] newConnections) {
        int numInputs = oldConnections.length;
        int numOutputs = oldConnections[0].length;
        //NOTE: "FEATURES_SELECTED" was not added until 2 April in AssignmentFinalLogger version. Previously ID would be "NUM_IN"
        pw.println(ts() + "," + w.getID() + ",FEATURES_SELECTED,NUM_IN=" + numInputs + ",NUM_OUT=" + numOutputs
                + ",OLD=" + booleanMatrixToString(oldConnections)
                + ",NEW=" + booleanMatrixToString(newConnections));
    }

    private String booleanMatrixToString(boolean[][] m) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < m.length; i++) {
            sb.append('{');
            for (int j = 0; j < m[i].length; j++) {
                sb.append(m[i][j]).append(',');
            }
            sb.append("},");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void supervisedTrainFinished(Wekinator w) throws IOException {
        //Make sure we're getting info about feature selection too
        //Need: Serialized models (1 per timestamped file is fine), with training data for each model
        //Probably want good way of storing binary serialization data into XML as well
        //Want info about which models are currently run-enabled.
        List<Path> paths = w.getSupervisedLearningManager().getPaths();
        Long t = ts();
        pw.println(t + "," + w.getID() + ",TRAIN_FINISHED,NUM_MODELS=" + paths.size() + ",MODEL_SET=" + modelSetID);
        String baseName = "model_" + modelSetID + "_";
        int i = 0;
        int numInputs = w.getInputManager().getNumInputs();
        int numOutputs = w.getOutputManager().getOutputGroup().getNumOutputs();
        int numMetaData = w.getDataManager().getNumMetaData();
        for (Path p : paths) {
            String f = currentAssignmentDir + baseName + i + ".xml";
            // pw.println(t + "," + w.getID() + ",MODEL_NUM=" + i + "," + baseName + i + ".xml");
            pw.println(t + "," + w.getID() + ",MODEL_NUM=" + i + "," + baseName + i + ".xml," + p.getModelBuilder().toLogString());

            p.writeToFile(f);

            //Write to another file:
            // int numInputs, int numOutputs, int numMetaData, Instances dummyInstances, Filter outputFilter) {
            Instances dummyInstances = w.getDataManager().getDummyInstances();
            String outputFilterString = w.getDataManager().getTrainingFilterString(i);
            LoadableInstanceMaker m;
            try {
                m = new LoadableInstanceMaker(numInputs, numOutputs, numMetaData, dummyInstances, outputFilterString);
                String f2 = currentAssignmentDir + baseName + i + "_m.xml";
                m.writeToFile(f2);
            } catch (Exception ex) {
                Logger.getLogger(AssignmentFinalLogger.class.getName()).log(Level.SEVERE, null, ex);
            }

            i++;
        }

        GlobalSettings.getInstance().setIntValue("modelSetID", modelSetID);
        modelSetID++;
        hasLoggedModelsInThisAssignment = true;
    }

    //Call when these models are runnable but not logged in the current assignment
    //e.g., have been loaded from a saved project.
    private void logFirstSupervisedRunInAssignmentWithNewModels(Wekinator w) throws IOException {
        List<Path> paths = w.getSupervisedLearningManager().getPaths();
        Long t = ts();
        pw.println(t + "," + w.getID() + ",FIRST_ASSIGNMENT_MODEL_LOG,NUM_MODELS=" + paths.size() + ",MODEL_SET=" + modelSetID);
        String baseName = "model_" + modelSetID + "_";
        int i = 0;
        int numInputs = w.getInputManager().getNumInputs();
        int numOutputs = w.getOutputManager().getOutputGroup().getNumOutputs();
        int numMetaData = w.getDataManager().getNumMetaData();
        for (Path p : paths) {
            String f = currentAssignmentDir + baseName + i + ".xml";
            pw.println(t + "," + w.getID() + ",MODEL_NUM=" + i + "," + baseName + i + ".xml," + p.getModelBuilder().toLogString());
            p.writeToFile(f);

            //Write to another file:
            // int numInputs, int numOutputs, int numMetaData, Instances dummyInstances, Filter outputFilter) {
            Instances dummyInstances = w.getDataManager().getDummyInstances();
            String outputFilterString = w.getDataManager().getTrainingFilterString(i);
            LoadableInstanceMaker m;
            try {
                m = new LoadableInstanceMaker(numInputs, numOutputs, numMetaData, dummyInstances, outputFilterString);
                String f2 = currentAssignmentDir + baseName + i + "_m.xml";
                m.writeToFile(f2);
            } catch (Exception ex) {
                Logger.getLogger(AssignmentFinalLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }
        GlobalSettings.getInstance().setIntValue("modelSetID", modelSetID);
        modelSetID++;
        hasLoggedModelsInThisAssignment = true;
    }

    //Call each time a new supervised datapoint is added
    @Override
    public void supervisedRunData(Wekinator w, double[] inputs, boolean[] computeMask, double[] outputs) {

        //ONLY do this for certain assignemnts:
        if (currentAssignmentType == KadenzeAssignmentType.ASSIGNMENT2_PART3A
                || currentAssignmentType == KadenzeAssignmentType.ASSIGNMENT2_PART3B
                || currentAssignmentType == KadenzeAssignmentType.ASSIGNMENT3_PART3A
                || currentAssignmentType == KadenzeAssignmentType.ASSIGNMENT3_PART3B) {

            StringBuilder sb = new StringBuilder();
            sb.append(ts());
            sb.append(",").append(w.getID()).append(",RUN,i=").append(inputs.length);
            sb.append(",o=").append(outputs.length).append(',');
            sb.append(doubleArrayToString(inputs));
            sb.append(booleanArrayToString(computeMask)).append(',');
            sb.append(doubleArrayToString(outputs));
            pw.println(sb.toString());
        }
    }

    private String doubleArrayToString(double[] vals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            sb.append(Double.toString(vals[i])).append(',');
        }
        return sb.toString();
    }

    private String booleanArrayToString(boolean[] vals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            sb.append(vals[i] ? '1' : '0');
        }
        return sb.toString();
    }

    @Override
    //DTW Tested
    public void dtwRunData(Wekinator w, double[] inputs, double[] outputs, int recognizedGesture) {
        if (currentAssignmentType == KadenzeAssignmentType.ASSIGNMENT6_PART1) {
            StringBuilder infoString = new StringBuilder();
            infoString.append("REC_GEST=").append(recognizedGesture);
            infoString.append(",NUM_IN=").append(inputs.length);
            infoString.append(",INPUTS=").append(doubleArrayToBracketedStringList(inputs));
            infoString.append(",NUM_OUT=").append(outputs.length);
            infoString.append(",OUTPUTS=").append(doubleArrayToBracketedStringList(outputs));
            pw.println(ts() + "," + w.getID() + ",DTW_RUN_DATA," + infoString.toString());
        }
    }

    @Override
    //DTW Tested
    public void dtwThresholdChanged(Wekinator w, double oldThreshold, double newThreshold) {
        pw.println(ts() + "," + w.getID() + ",DTW_THRESHOLD_CHANGED,OLD=" + oldThreshold + ",NEW=" + newThreshold);
    }

    @Override
    public void supervisedOutputEdited(Wekinator w, int whichOutput, OSCOutput oldOutput) {

    }

    private Long ts() {
        return (new Date()).getTime();
    }

    private void logClose() {
        pw.println(ts() + ",0,STOPLOG");
    }

    @Override
    public void logVersionNumberAndDate() {
        pw.println(ts() + ",0,STARTLOG,VERSION=" + version + ",LOGGERDATE=" + dateString);
    }

    private void assignmentStarted(KadenzeAssignmentType ka) {
        pw.println(ts() + ",0,ASSIGNMENT_STARTED," + ka);
    }

    @Override
    public void deleteLastRecordingRound(Wekinator w, int deletedRound) {
        pw.println(ts() + "," + w.getID() + ",DELETE_LAST_ROUND," + deletedRound);

    }

    @Override
    public void reAddLastRecordingRound(Wekinator w, int num) {
        pw.println(ts() + "," + w.getID() + ",RE-ADD_LAST_ROUND," + num);
    }

    @Override
    public String createZip() throws FileNotFoundException, IOException {
        //  String zipName = parentDir + File.separator + KadenzeAssignment.getLogDirectory(currentAssignmentType) + ".zip";
        //String zipName = parentDir + File.separator + "assignment" + KadenzeAssignment.getAssignmentNumber(currentAssignmentType) + ".zip";
        String zipName = getZipDirectoryNameForAssignment() + ".zip";

        try (FileOutputStream fos2 = new FileOutputStream(zipName); ZipOutputStream zos = new ZipOutputStream(fos2)) {
            // File assignDir = new File(currentAssignmentDir);
            //String dirToZip = parentDir + File.separator + "assignment" + KadenzeAssignment.getAssignmentNumber(currentAssignmentType);
            String dirToZip = getZipDirectoryNameForAssignment();
            File fileToZip = new File(dirToZip);
            List<File> fileList = new LinkedList<>();
            KadenzeUtils.listFilesForFolder(fileToZip, fileList);
            File p = new File(parentDir);
            for (File f : fileList) {
                KadenzeUtils.addToZipFile(p, f.getCanonicalPath(), zos);
            }
        }
        return zipName;
    }

    @Override
    public void sameAssignmentRequested(KadenzeAssignment.KadenzeAssignmentType a) {
        //TODO: Anything?
    }

    @Override
    public String getCurrentLoggingDirectory() {
        return currentAssignmentDir;
    }

    @Override
    public void logModelBuilderUpdated(Wekinator w, LearningModelBuilder mb, int i) {
        pw.println(ts() + "," + w.getID() + ",MODEL_BUILDER_UPDATED," + i + "," + mb.toLogString());
    }

    //For supervised learning only
    @Override
    public void logPathUpdated(Wekinator w, int which, OSCOutput oldOutput, OSCOutput newOutput, LearningModelBuilder oldModelBuilder, LearningModelBuilder newModelBuilder, String[] oldSelectedInputs, String[] newSelectedInputs) {
        //Which : which path
        Long t = ts();
        int id = w.getID();
        pw.println(t + "," + w.getID() + ",PATH_EDITED," + which);
        if (newOutput != null) {
            logOutputUpdated(t, id, which, oldOutput, newOutput);
        }
        if (newModelBuilder != null) {
            logModelBuilderChange(t, id, which, oldModelBuilder, newModelBuilder);
        }
        //if (oldSelectedInputs.length != newSelectedInputs.length) {
        logFeatureSelectionChangeForModel(t, id, which, oldSelectedInputs, newSelectedInputs);
        //}
    }

    private void logModelBuilderChange(Long t, int id, int which, LearningModelBuilder oldModelBuilder, LearningModelBuilder newModelBuilder) {
        String oldModelString = oldModelBuilder.toLogString();
        String newModelString = newModelBuilder.toLogString();

        if (oldModelString.equals(newModelString)) {
            return;
        }

        pw.println(t + "," + id + ",MODEL_EDITED_OLD," + which + "," + oldModelString);
        pw.println(t + "," + id + ",MODEL_EDITED_NEW," + which + "," + newModelString);
    }

    private void logOutputUpdated(Long t, int id, int which, OSCOutput oldOutput, OSCOutput newOutput) {
        String oldOutputString = oldOutput.toLogString();
        String newOutputString = newOutput.toLogString();

        pw.println(t + "," + id + ",OUTPUT_EDITED_OLD," + which + "," + oldOutputString);
        pw.println(t + "," + id + ",OUTPUT_EDITED_NEW," + which + "," + newOutputString);
    }

    private void logFeatureSelectionChangeForModel(Long t, int id, int which, String[] oldSelectedInputs, String[] newSelectedInputs) {
        StringBuilder oldInputNames = new StringBuilder();
        for (int i = 0; i < oldSelectedInputs.length; i++) {
            oldInputNames.append(oldSelectedInputs[i]).append(',');
        }
        StringBuilder newInputNames = new StringBuilder();
        for (int i = 0; i < newSelectedInputs.length; i++) {
            newInputNames.append(newSelectedInputs[i]).append(',');
        }

        if (oldInputNames.toString().equals(newInputNames.toString())) {
            return;
        }

        pw.println(t + "," + id + ",SELECTED_INPUTS_OLD," + which + "," + oldInputNames.toString());
        pw.println(t + "," + id + ",SELECTED_INPUTS_NEW," + which + "," + newInputNames.toString());

    }

    @Override
    public void logSupervisedModelPrintedToConsole(Wekinator w, Path p) {
        List<Path> paths = w.getSupervisedLearningManager().getPaths();
        int which = paths.indexOf(p);
        pw.println(ts() + "," + w.getID() + ",MODEL_TO_CONSOLE," + which);
    }

    @Override
    public void logStartSupervisedRun(Wekinator w) {
        pw.println(ts() + "," + w.getID() + ",START_RUN");
        if (!hasLoggedModelsInThisAssignment) {
            try {
                logFirstSupervisedRunInAssignmentWithNewModels(w);
            } catch (IOException ex) {
                Logger.getLogger(AssignmentFinalLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String getZipDirectoryNameForAssignment() {
        return parentDir + File.separator + "assignment" + KadenzeAssignment.getAssignmentNumber(currentAssignmentType);
    }

    //Caution: this will be called in whatever sub-part of the assignment is open when it is submitted. May have multiple of these, will have to look for latest one in order to make sense of it!
    @Override
    public void logInputInformation(Wekinator w, String inputString, int difficulty, String difficultyString) {
        String modInputString = inputString.replaceAll("\n", " NEWLINE ").replaceAll("\r", " NEWLINE "); //TODO TEST ON WINDOWS
        String modDifficultyString = difficultyString.replaceAll("\n", " NEWLINE ").replaceAll("\r", " NEWLINE "); //TODO TEST ON WINDOWS
        pw.println(ts() + "," + w.getID() + ",INPUT_DESCRIPTION_STRING," + modInputString);
        pw.println(ts() + "," + w.getID() + ",ASSIGNMENT_DIFFICULTY_RATING," + difficulty);
        pw.println(ts() + "," + w.getID() + ",ASSIGNMENT_DIFFICULTY_COMMENTS," + modDifficultyString);
    }

    //Caution: this will be called in whatever sub-part of the assignment is open when it is submitted. May have multiple of these, will have to look for latest one in order to make sense of it!
    @Override
    public void logWrittenQuestion(Wekinator w, String idString, String textString) {
        String modTextString = textString.replaceAll("\n", " NEWLINE ").replaceAll("\r", " NEWLINE "); //TODO TEST ON WINDOWS
        pw.println(ts() + "," + w.getID() + ",WRITTEN_QUESTION," + idString + "," + textString);
    }

    @Override
    public void logStartDtwRun(Wekinator w) {
        pw.println(ts() + "," + w.getID() + ",START_DTW_RUN");
        try {
            logDtwModel(w);
        } catch (IOException ex) {
            Logger.getLogger(AssignmentFinalLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void logDtwModel(Wekinator w) throws IOException {
        Long t = ts();
        pw.println(t + "," + w.getID() + ",LOG_DTW_MODEL,MODEL_SET=" + modelSetID);
        String baseName = "model_" + modelSetID + ".xml";
        String f = currentAssignmentDir + baseName;
        pw.println(t + "," + w.getID() + ",DTW_MODEL_FILE,F=" + baseName + "," + w.getDtwLearningManager().getModel().toLogInfoString());
        w.getDtwLearningManager().getModel().writeToFile(f);
        GlobalSettings.getInstance().setIntValue("modelSetID", modelSetID);
        modelSetID++;
        hasLoggedModelsInThisAssignment = true;
    }

    @Override
    //DTWTESTED
    public void dtwDeleteAllExamplesForGesture(Wekinator w, int gestureNum) {
        pw.println(ts() + "," + w.getID() + ",DTW_DELETE_ALL_EXAMPLES_FOR_GESTURE,GESTURE=" + gestureNum);
    }

    @Override
    //DTWTESTED
    public void dtwDeleteMostRecentExampleForGesture(Wekinator w, int gestureNum) {
        pw.println(ts() + "," + w.getID() + ",DTW_DELETE_MOST_RECENT_EXAMPLE_FOR_GESTURE,GESTURE=" + gestureNum);
    }

    @Override
    //DTWTESTED
    public void dtwClassifiedLast(Wekinator w, TimeSeries currentTimeSeries, double[] closestDistances, int closestClass) {
        StringBuilder distString = new StringBuilder();
        distString.append("DISTS=");
        distString.append(doubleArrayToBracketedStringList(closestDistances));
        pw.println(ts() + "," + w.getID() + ",DTW_CLASSIFIED_LAST,CLOSEST=" + closestClass
                + "," + distString.toString() + "," + timeSeriesToSingleLine(currentTimeSeries));
    }

    //DTWTESTED
    private String timeSeriesToSingleLine(TimeSeries ts) {
        int numSamples = ts.numOfPts();
        int dim = ts.numOfDimensions();
        StringBuilder sb = new StringBuilder();
        sb.append("NUM_SAMPLES=").append(numSamples);
        sb.append(",NUM_DIM=").append(dim);
        sb.append(",DATA=");
        for (int input = 0; input < dim; input++) {
            sb.append("{");
            for (int t = 0; t < ts.numOfPts() - 1; t++) {
                sb.append(ts.getMeasurement(t, input)).append(',');
            }
            if (ts.numOfPts() != 0) {
                sb.append(ts.getMeasurement(ts.numOfPts() - 1, input));
            }
            sb.append("}");
            if (input != dim - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private String doubleArrayToBracketedStringList(double[] a) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < a.length - 1; i++) {
            sb.append(a[i]).append(",");
        }
        sb.append(a[a.length - 1]).append("}");
        return sb.toString();
    }

    private String booleanArrayToBracketedStringList(boolean[] a) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < a.length - 1; i++) {
            sb.append(a[i] ? "1" : "0").append(",");
        }
        sb.append(a[a.length - 1] ? "1" : "0").append("}");
        return sb.toString();
    }

    @Override
    //DTWTESTED
    public void logDtwModelUpdated(Wekinator w, DtwModel m, DtwSettings oldSettings, DtwSettings newDtwSettings, boolean[] lastInputSelection, boolean[] currentInputSelection) {
        StringBuilder sb = new StringBuilder();
        sb.append(ts() + "," + w.getID() + ",DTW_MODEL_UPDATED,");
        sb.append(m.toLogInfoString());
        sb.append(",OLD_SETTINGS={");
        if (oldSettings != null) {
            sb.append(oldSettings.toLogInfoString());
        }
        sb.append("},NEW_SETTINGS={");
        if (newDtwSettings != null) {
            sb.append(newDtwSettings.toLogInfoString());
        }
        sb.append("},OLD_FEATURES=").append(booleanArrayToBracketedStringList(lastInputSelection));
        sb.append(",NEW_FEATURES=").append(booleanArrayToBracketedStringList(currentInputSelection));
        pw.println(sb.toString());
    }

}
