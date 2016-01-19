/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.osc.OSCInputGroup;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
public class KadenzeLogger {
    private final int version = 1;
    private final String dateString = "2016/01/17";
    File log = null;
    FileOutputStream fos = null;
    //BufferedOutputStream bos = null;
    OutputStreamWriter osw = null;
    PrintWriter pw = null;
    BufferedWriter bw = null;
    //FileWriter fw = null;
    
    //TODO Use unique identifier for each Wekinator project!
    
    //assignmentDir includes terminating file separator
    public void beginLog(String assignmentDir, String assignmentSuffix) throws IOException {
        File f = new File(assignmentDir);
        f.mkdirs();
        String loc = assignmentDir + "assignment" + assignmentSuffix + ".txt";
        System.out.println("Trying to write to " + loc);
        File l = new File(loc);
       // fw = new FileWriter(loc, true); //do append if it exists
        //fos = new FileOutputStream
        fos = new FileOutputStream(l, true);
        osw = new OutputStreamWriter(fos, "UTF-8");
        bw = new BufferedWriter(osw);
        pw = new PrintWriter(bw);
        
        logVersionNumberAndDate();
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

    void logSameAssignmentRequested(KadenzeLogging.KadenzeAssignment a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void switchToLog(String myAssignmentDir, String suffix) {
    //TODO: Need to handle when this chnages part without wekinator being closed.
    //Need to Flush, close current log and move on
    }
    
    public static enum KEvent {
        SUPERVISED_RECORD_START,
        SUPERVISED_RECORD_STOP,
        TRAIN_START,
        TRAIN_STOP,
        TRAIN_CANCEL,
        RUN_START,
        RUN_STOP,
        DELETE_ALL_EXAMPLES,
        DELETE_LAST_ROUND,
        RE_ADD_LAST_ROUND,
        RANDOMIZE,
        DATA_VIEWED,
        MODEL_DISPLAYED_IN_CONSOLE
    }
    
    public void logEvent(KEvent ke) {
        
    }
    
    public void newProjectStarted(OSCInputGroup ing, OSCOutputGroup og) {
        //Also learning algorithms needed
    }
    
    public void projectSaved(String projectName) {
        
    }
    
    public void projectLoaded(String projectName) {
        
    }
    
    public void logExamplesDeletedForModel(int modelNum) {
        
    }
    
    public void logDtwGestureAdd(int gestureNum) {
        
    }
    
    public void changeLearningAlgorithm(int modelNum) {
        //Need info about what exactly changed:
        //Algorihtm, parameters, etc.
        
    }
    
    
    

    
    public void logComputeCVal(int modelNum, int numFolds, double val) {
        //Probably also need to output all model info & data here so we know what is being tested
        //(E.g., model params may have been changed since last rn, training data may have changed)
    }
    
    public void logComputeTrainAccuracy(int modelNum, double val) {
        //Same as CVal: Need to output all info we'd usually output for a run session
    }
    
    public void logSelectedFeatures(boolean[] matrix) {
        
    }
    
    public void logSupervisedRunStart() {
        //Make sure we're getting info about feature selection too
        //Need: Serialized models (1 per timestamped file is fine), with training data for each model
        //Probably want good way of storing binary serialization data into XML as well
    }
    
    //Call each time a new supervised datapoint is added
    public void logSupervisedRunData(double[] inputs, double[] outputs) {
        
    }
    
    public void logDtwRunStart() {
        //Need current model info (e.g. downsampling behavior): Save DTW Model to XML file (This also contains raw trainng examples)
        //Also need info about feature selection etc.
    }
    
    public void logDtwRunData(double[] inputs, double[] outputs, int recognizedGesture) {
        //outputs are current match info (1 per gesture class), recognizedGesture is 0 if no match
        
    }
    
    public void dtwThresholdChanged() {
        
    }
    
    public Long ts() {
        return (new Date()).getTime();
    }
    
    private void logClose() {
        pw.println(ts()+",0,STOPLOG");
    }
    
    public void logVersionNumberAndDate() {
        pw.println(ts()+",0,STARTLOG,VERSION="+version+",LOGGERDATE="+dateString);
    }
}
