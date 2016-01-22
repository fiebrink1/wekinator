/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

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
import wekimini.GlobalSettings;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.kadenze.KadenzeAssignment.KadenzeAssignmentType;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class Assignment12Logger implements KadenzeLogger {

    private static final int version = 2;
    private static final String dateString = "2016/01/22";
    private FileOutputStream fos = null;
    private OutputStreamWriter osw = null;
    private PrintWriter pw = null;
    private BufferedWriter bw = null;
    private String parentDir = ""; //Place in which this assignment directory lives
    private String currentAssignmentDir = ""; //Directory for this assignment
    private int modelSetID = 1; //For choosing model xml file names
    private KadenzeAssignmentType currentAssignmentType;

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
            Logger.getLogger(Assignment12Logger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void closeLog() {
        logClose();
        pw.flush();
        pw.close();
    }

    @Override
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
    public void newProjectStarted(Wekinator w) {
        //Possibly human-readable algorithms list here (esp if CV used, can partition by run)

    }

    @Override
    public void projectSaved(Wekinator w, String projectName) {

    }

    @Override
    public void projectLoaded(Wekinator w, String projectName) {

    }

    @Override
    public void examplesDeletedForModel(Wekinator w, int modelNum) {

    }

    @Override
    public void dtwGestureAdded(Wekinator w, int gestureNum) {

    }

    @Override
    public void learningAlgorithmChanged(Wekinator w, int modelNum) {
        //Need info about what exactly changed:
        //Algorihtm, parameters, etc.

    }

    @Override
    public void crossValidationComputed(Wekinator w, int modelNum, int numFolds, double val) {
        //Probably also need to output all model info & data here so we know what is being tested
        //(E.g., model params may have been changed since last rn, training data may have changed)
    }

    @Override
    public void trainingAccuracyComputed(Wekinator w, int modelNum, double val) {
        //Same as CVal: Need to output all info we'd usually output for a run session
    }

    @Override
    public void selectedFeatures(Wekinator w, boolean[] matrix) {

    }

    @Override
    public void supervisedTrainFinished(Wekinator w) throws IOException {
        //Make sure we're getting info about feature selection too
        //Need: Serialized models (1 per timestamped file is fine), with training data for each model
        //Probably want good way of storing binary serialization data into XML as well
        //Want info about which models are currently run-enabled.
        List<Path> paths = w.getSupervisedLearningManager().getPaths();
        pw.println(ts() + "," + w.getID() + ",TRAIN_FINISHED,NUM_MODELS=" + paths.size() + ",MODEL_SET=" + modelSetID);
        String baseName = "model_" + modelSetID + "_";
        int i = 0;
        for (Path p : paths) {
            String f = currentAssignmentDir + baseName + i + ".xml";
            pw.println(ts() + "," + w.getID() + ",MODEL_NUM=" + i + "," + baseName + i + ".xml");
            p.writeToFile(f);
            i++;
        }
        GlobalSettings.getInstance().setIntValue("modelSetID", modelSetID);
        modelSetID++;
    }

    //Call each time a new supervised datapoint is added
    @Override
    public void supervisedRunData(Wekinator w, double[] inputs, boolean[] computeMask, double[] outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(ts());
        sb.append(",").append(w.getID()).append(",RUN,i=").append(inputs.length);
        sb.append(",o=").append(outputs.length).append(',');
        sb.append(doubleArrayToString(inputs));
        sb.append(booleanArrayToString(computeMask)).append(',');
        sb.append(doubleArrayToString(outputs));
        pw.println(sb.toString());
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
    public void dtwRunStart(Wekinator w) {
        //Need current model info (e.g. downsampling behavior): Save DTW Model to XML file (This also contains raw trainng examples)
        //Also need info about feature selection etc.
    }

    @Override
    public void dtwRunData(Wekinator w, double[] inputs, double[] outputs, int recognizedGesture) {
        //outputs are current match info (1 per gesture class), recognizedGesture is 0 if no match

    }

    @Override
    public void dtwThresholdChanged(Wekinator w) {

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
        String zipName = parentDir + File.separator + KadenzeAssignment.getLogDirectory(currentAssignmentType) + ".zip";
        FileOutputStream fos2 = new FileOutputStream(zipName);
        ZipOutputStream zos = new ZipOutputStream(fos2);
        File assignDir = new File(currentAssignmentDir);
        List<File> fileList = new LinkedList<>();
        KadenzeUtils.listFilesForFolder(assignDir, fileList);
        File p = new File(parentDir);
        for (File f : fileList) {
            KadenzeUtils.addToZipFile(p, f.getCanonicalPath(), zos);
        }
        zos.close();
        fos2.close();
        return zipName;
    }

    /* @Override
     public void beginLog(String parentDir, KadenzeAssignment.KadenzeAssignmentType a) throws IOException {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     } */
    @Override
    public void sameAssignmentRequested(KadenzeAssignment.KadenzeAssignmentType a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCurrentLoggingDirectory() {
        return currentAssignmentDir;
    }

}
