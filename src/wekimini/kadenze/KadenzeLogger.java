/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.FileNotFoundException;
import java.io.IOException;
import wekimini.LearningModelBuilder;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.kadenze.KadenzeAssignment.KadenzeAssignmentType;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public interface KadenzeLogger {

    public String getCurrentLoggingDirectory();

    public void logModelBuilderUpdated(Wekinator w, LearningModelBuilder mb, int i);

    public void logPathUpdated(Wekinator w, int which, OSCOutput oldOutput, OSCOutput newOutput, LearningModelBuilder oldModelBuilder, LearningModelBuilder newModelBuilder, String[] selectedInputs, String[] selectedInputNames);

    public static enum KEvent {
        TRAIN_START, //X
        TRAIN_CANCEL, //X
        //RUN_START, //X
        RUN_STOP, //X
        SUPERVISED_DELETE_ALL_EXAMPLES, //X
        DTW_DELETE_ALL_EXAMPLES,
        DTW_DELETE_LAST_EXAMPLE,
        DTW_RE_ADD_LAST_EXAMPLE,
        RANDOMIZE,//X
        SUPERVISED_DATA_VIEWED,//X
        DTW_DATA_VIEWED,
        PROJECT_CLOSED //Not called when AppleQ, but whatev
    }
    
    void logStartRun(Wekinator w);
    
    // public void beginLog(String assignmentDir, String assignmentSuffix) throws IOException {
    void beginLog(String parentDir, KadenzeAssignmentType a) throws IOException;

    void closeLog();

    String createZip() throws FileNotFoundException, IOException;

    void crossValidationComputed(Wekinator w, int modelNum, int numFolds, String val);

    void deleteLastRecordingRound(Wekinator w, int deletedRound);

    void dtwGestureAdded(Wekinator w, int gestureNum);

    void dtwRunData(Wekinator w, double[] inputs, double[] outputs, int recognizedGesture);

    void dtwRunStart(Wekinator w);

    void dtwThresholdChanged(Wekinator w);

    void examplesDeletedForModel(Wekinator w, int modelNum);

    void flush();

    void logEvent(Wekinator w, KEvent ke);

    void logVersionNumberAndDate();

    void newProjectStarted(Wekinator w);

    void projectSaved(Wekinator w, String projectName);

    void reAddLastRecordingRound(Wekinator w, int num);

    void sameAssignmentRequested(KadenzeAssignmentType a);

    void selectedFeatures(Wekinator w, boolean[][] oldConnections, boolean[][] newConnections);
    
    void supervisedLearningRecordStarted(Wekinator w);

    void supervisedLearningRecordStopped(Wekinator w);

    void supervisedOutputEdited(Wekinator w, int whichOutput, OSCOutput oldOutput);

    //TODO: Make sure model Set ID is set for
    /*public void supervisedRunStart(Wekinator w) throws IOException {
    } */
    //Call each time a new supervised datapoint is added
    void supervisedRunData(Wekinator w, double[] inputs, boolean[] computeMask, double[] outputs);

    void supervisedTrainFinished(Wekinator w) throws IOException;

    void trainingAccuracyComputed(Wekinator w, int modelNum, String val);
    
    void loadedFromFile(Wekinator w, String projectName);
    
    void logModelPrintedToConsole(Wekinator w, Path p);
    
}
