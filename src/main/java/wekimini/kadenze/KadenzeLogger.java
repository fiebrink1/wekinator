/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import com.timeseries.TimeSeries;
import java.io.FileNotFoundException;
import java.io.IOException;
import wekimini.LearningModelBuilder;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.kadenze.KadenzeAssignment.KadenzeAssignmentType;
import wekimini.learning.dtw.DtwModel;
import wekimini.learning.dtw.DtwSettings;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public interface KadenzeLogger {

    public String getCurrentLoggingDirectory();

    public void logModelBuilderUpdated(Wekinator w, LearningModelBuilder mb, int i);

    public void logPathUpdated(Wekinator w, int which, OSCOutput oldOutput, OSCOutput newOutput, LearningModelBuilder oldModelBuilder, LearningModelBuilder newModelBuilder, String[] selectedInputs, String[] selectedInputNames);

    public String getZipDirectoryNameForAssignment();

    public void logStartDtwRun(Wekinator w);

    public void dtwDeleteAllExamplesForGesture(Wekinator w, int gestureNum);

    public void dtwDeleteMostRecentExampleForGesture(Wekinator w, int gestureNum);

    public void dtwThresholdChanged(Wekinator w, double oldMatchThreshold, double matchThreshold);

    public void dtwClassifiedLast(Wekinator w, TimeSeries currentTimeSeries, double[] closestDistances, int closestClass);

    public void logDtwModelUpdated(Wekinator w, DtwModel m, DtwSettings oldSettings, DtwSettings newDtwSettings, boolean[] selectedInputs, boolean[] inputSelection);

    public static enum KEvent {
        TRAIN_START,
        TRAIN_CANCEL,
        RUN_STOP,
        SUPERVISED_DELETE_ALL_EXAMPLES,
        DTW_DELETE_ALL_EXAMPLES,
        DTW_DELETE_LAST_EXAMPLE,
        DTW_RE_ADD_LAST_EXAMPLE, 
        RANDOMIZE,
        SUPERVISED_DATA_VIEWED,
        DTW_DATA_VIEWED, 
        PROJECT_CLOSED, //Not called when AppleQ, but whatev
        DTW_RUN_STOP 
    }
    
    void logStartSupervisedRun(Wekinator w);
    
    // public void beginLog(String assignmentDir, String assignmentSuffix) throws IOException {
    void beginLog(String parentDir, KadenzeAssignmentType a) throws IOException;

    void closeLog();

    String createZip() throws FileNotFoundException, IOException;

    void crossValidationComputed(Wekinator w, int modelNum, int numFolds, String val);

    void deleteLastRecordingRound(Wekinator w, int deletedRound);

    void dtwGestureAdded(Wekinator w, int gestureNum);

    void dtwRunData(Wekinator w, double[] inputs, double[] outputs, int recognizedGesture);

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
    
    void logSupervisedModelPrintedToConsole(Wekinator w, Path p);
    
    public void logInputInformation(Wekinator w, String inputString, int difficulty, String difficultyString);
    
    public void logWrittenQuestion(Wekinator w, String idString, String textString);
    
}
