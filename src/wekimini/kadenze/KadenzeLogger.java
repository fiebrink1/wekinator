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
import wekimini.kadenze.KadenzeLogging.KadenzeAssignment;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class KadenzeLogger {
    private final int version = 1;
    private final String dateString = "2016/01/17";
    File log = null;
    FileOutputStream fos = null;
    OutputStreamWriter osw = null;
    PrintWriter pw = null;
    BufferedWriter bw = null;
    String parentDir = "";
    String currentAssignmentDir = "";
    int modelSetID = 1;
    KadenzeAssignment currentAssignment = KadenzeAssignment.ASSIGNMENT1;
    int assignmentNum = 1;
    
   // public void beginLog(String assignmentDir, String assignmentSuffix) throws IOException {
    public void beginLog(String parentDir, KadenzeAssignment a) throws IOException {
       currentAssignment = a;
       assignmentNum = getAssignmentNumber(a);
       
       this.parentDir = parentDir;
       currentAssignmentDir = getAssignmentDirectory(parentDir, a);
       File dir = new File(currentAssignmentDir);
       dir.mkdirs();
       File f = getAssignmentFilename(dir, a);
       doAssignmentSetup(a);

//        String loc = assignmentDir + "assignment" + assignmentSuffix + ".txt";
       // System.out.println("Trying to write to " + loc);
              // fw = new FileWriter(loc, true); //do append if it exists
        //fos = new FileOutputStream
        fos = new FileOutputStream(f, true);
        osw = new OutputStreamWriter(fos, "UTF-8");
        bw = new BufferedWriter(osw);
        pw = new PrintWriter(bw);
        
        logVersionNumberAndDate();
        assignmentStarted(a);
    }
    
    private int getAssignmentNumber(KadenzeAssignment a) {
        switch (a) {
            case ASSIGNMENT1:
                return 1;
            case ASSIGNMENT2_PART1: 
            case ASSIGNMENT2_PART2:
                return 2;
            default:
                return 0;    
        } 
    }
    
    //Can just use same ID list for all assignments
    private void doAssignmentSetup(KadenzeAssignment a) {
        //if (a == KadenzeAssignment.ASSIGNMENT1) {
            GlobalSettings gs = GlobalSettings.getInstance();
            int lastModelSetID = gs.getIntValue("modelSetID", 0);
            modelSetID = lastModelSetID+1;
       // }
    }
    
    private File getAssignmentFilename(File dir, KadenzeAssignment a) {
        String suffix;
      
        switch (a) {
            case ASSIGNMENT1:
                suffix = "1";
                break;
            case ASSIGNMENT2_PART1: 
                suffix = "2Part1";
                break;
            case ASSIGNMENT2_PART2:
                suffix = "2Part2";
                break;
            default:
                suffix = "";     
        } 
        String myAssignmentFile = dir + File.separator + "assignment" + suffix + ".txt";
        return new File(myAssignmentFile);
    }
    
    private String getAssignmentDirectory(String parent, KadenzeAssignment a) {
        String assignmentDir;
        switch (a) {
            case ASSIGNMENT1:
                assignmentDir = "assignment1";
                break;
            case ASSIGNMENT2_PART1: 
                assignmentDir = "assignment2";
                break;
            case ASSIGNMENT2_PART2:
                assignmentDir = "assignment2";
                break;
            default:
                assignmentDir = "tmp";
        } 
        return parent + File.separator + assignmentDir + File.separator;
    }
    
    public void flush() {
        try {
            pw.flush(); //TODO: Need to flush other writers as well?
            bw.flush();
            osw.flush();
            fos.flush();
        } catch (IOException ex) {
            Logger.getLogger(KadenzeLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void closeLog() {
        logClose();
        pw.flush();
        pw.close();
    }

    public void sameAssignmentRequested(KadenzeLogging.KadenzeAssignment a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //TODO: make sure we don't run into trouble writing if e.g. we're running while we switch assignment!
    //Easy way: Make sure we stop running wekinator before allowing this menu item change.
    //TODO !!!
    public void switchToAssignment(String parentDir, KadenzeAssignment ka) throws IOException {
        closeLog();
        beginLog(parentDir, ka);
    }
    
    public static enum KEvent {
        TRAIN_START, //X
        TRAIN_CANCEL, //X
        RUN_START,
        RUN_STOP, //X
        SUPERVISED_DELETE_ALL_EXAMPLES, //X
        DTW_DELETE_ALL_EXAMPLES,
        DTW_DELETE_LAST_EXAMPLE,
        DTW_RE_ADD_LAST_EXAMPLE,
        RANDOMIZE,//X
        SUPERVISED_DATA_VIEWED,//X
        DTW_DATA_VIEWED,
        SUPERVISED_MODEL_DISPLAYED_IN_CONSOLE
    }
    
    public void supervisedLearningRecordStarted(Wekinator w) {
        pw.println(ts()+"," + w.getID() + ",SUPERVISED_RECORD_START," + (w.getSupervisedLearningManager().getRecordingRound()+1));
    }
    
    public void supervisedLearningRecordStopped(Wekinator w) {
        pw.println(ts()+"," + w.getID() + ",SUPERVISED_RECORD_STOP," + w.getSupervisedLearningManager().getRecordingRound());
    }
    
    public void logEvent(Wekinator w, KEvent ke) {
        pw.println(ts()+"," + w.getID() + "," + ke);
    }
    
    public void newProjectStarted(Wekinator w) {
        //Possibly human-readable algorithms list here (esp if CV used, can partition by run)
        
    }
    
    public void projectSaved(Wekinator w, String projectName) {
        
    }
    
    public void projectLoaded(Wekinator w, String projectName) {
        
    }
    
    public void examplesDeletedForModel(Wekinator w, int modelNum) {
        
    }
    
    public void dtwGestureAdded(Wekinator w, int gestureNum) {
        
    }
    
    public void learningAlgorithmChanged(Wekinator w, int modelNum) {
        //Need info about what exactly changed:
        //Algorihtm, parameters, etc.
        
    }
    
    public void crossValidationComputed(Wekinator w, int modelNum, int numFolds, double val) {
        //Probably also need to output all model info & data here so we know what is being tested
        //(E.g., model params may have been changed since last rn, training data may have changed)
    }
    
    public void trainingAccuracyComputed(Wekinator w, int modelNum, double val) {
        //Same as CVal: Need to output all info we'd usually output for a run session
    }
    
    public void selectedFeatures(Wekinator w, boolean[] matrix) {
        
    }
    
    public void supervisedTrainFinished(Wekinator w) throws IOException {
        //Make sure we're getting info about feature selection too
        //Need: Serialized models (1 per timestamped file is fine), with training data for each model
        //Probably want good way of storing binary serialization data into XML as well
        //Want info about which models are currently run-enabled.
        List<Path> paths = w.getSupervisedLearningManager().getPaths();
        pw.println(ts()+"," + w.getID() + ",TRAIN_FINISHED,NUM_MODELS=" + paths.size()+",MODEL_SET="+modelSetID);
        String baseName = "model_" + modelSetID + "_";
        int i = 0;
        for (Path p : paths) {
            String f = currentAssignmentDir + baseName + i + ".xml";
            pw.println(ts()+"," + w.getID() + ",MODEL_NUM="+i+","+baseName + i + ".xml");
            p.writeToFile(f);
            i++;
        }
        GlobalSettings.getInstance().setIntValue("modelSetID", modelSetID);
        modelSetID++; 
    }
    
    //TODO: Make sure model Set ID is set for 
    /*public void supervisedRunStart(Wekinator w) throws IOException {

    } */
    
    //Call each time a new supervised datapoint is added
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
    
    public void dtwRunStart(Wekinator w) {
        //Need current model info (e.g. downsampling behavior): Save DTW Model to XML file (This also contains raw trainng examples)
        //Also need info about feature selection etc.
    }
    
    public void dtwRunData(Wekinator w, double[] inputs, double[] outputs, int recognizedGesture) {
        //outputs are current match info (1 per gesture class), recognizedGesture is 0 if no match
        
    }
    
    public void dtwThresholdChanged(Wekinator w) {
        
    }
    
    public void supervisedOutputEdited(Wekinator w, int whichOutput, OSCOutput oldOutput) {
        
    }
    
    
    
    private Long ts() {
        return (new Date()).getTime();
    }
    
    private void logClose() {
        pw.println(ts()+",0,STOPLOG");
    }
    
    public void logVersionNumberAndDate() {
        pw.println(ts()+",0,STARTLOG,VERSION="+version+",LOGGERDATE="+dateString);
    }
    
    private void assignmentStarted(KadenzeLogging.KadenzeAssignment ka) {
        pw.println(ts()+",0,ASSIGNMENT_STARTED," + ka);
    }
    
    public void deleteLastRecordingRound(Wekinator w, int deletedRound) {
        pw.println(ts()+"," + w.getID() + ",DELETE_LAST_ROUND," + deletedRound);

    }

    public void reAddLastRecordingRound(Wekinator w, int num) {
        pw.println(ts()+"," + w.getID() + ",RE-ADD_LAST_ROUND," + num);
    }
    
    public String createZip() throws FileNotFoundException, IOException {
        String zipName = parentDir + File.separator + "assignment" + assignmentNum + ".zip";
        FileOutputStream fos = new FileOutputStream(zipName);
	ZipOutputStream zos = new ZipOutputStream(fos);
        File assignDir = new File(currentAssignmentDir);
        List<File> fileList = new LinkedList<File>();
        KadenzeUtils.listFilesForFolder(assignDir, fileList);
        File p = new File(parentDir);
        for (File f : fileList) {
            KadenzeUtils.addToZipFile(p, f.getCanonicalPath(), zos);
        }
        zos.close();
	fos.close();
        return zipName;
    }

}
