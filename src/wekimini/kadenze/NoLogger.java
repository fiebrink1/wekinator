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
import wekimini.learning.dtw.DtwModel;
import wekimini.learning.dtw.DtwSettings;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class NoLogger implements KadenzeLogger {

    @Override
    public void closeLog() {
    }

    @Override
    public String createZip() throws FileNotFoundException, IOException {
        return "NO ZIP FILE:NO LOGGING DONE";
    }

    @Override
    public void crossValidationComputed(Wekinator w, int modelNum, int numFolds, String val) {
    }

    @Override
    public void deleteLastRecordingRound(Wekinator w, int deletedRound) {
    }

    @Override
    public void dtwGestureAdded(Wekinator w, int gestureNum) {
    }

    @Override
    public void dtwRunData(Wekinator w, double[] inputs, double[] outputs, int recognizedGesture) {
    }
    @Override
    public void examplesDeletedForModel(Wekinator w, int modelNum) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void logEvent(Wekinator w, KEvent ke) {
    }

    @Override
    public void logVersionNumberAndDate() {
    }

    @Override
    public void newProjectStarted(Wekinator w) {
    } 

    @Override
    public void projectSaved(Wekinator w, String projectName) {
    }

    @Override
    public void reAddLastRecordingRound(Wekinator w, int num) {
    }
    
    @Override
    public void supervisedLearningRecordStarted(Wekinator w) {
    }

    @Override
    public void supervisedLearningRecordStopped(Wekinator w) {
    }

    @Override
    public void supervisedOutputEdited(Wekinator w, int whichOutput, OSCOutput oldOutput) {
    }

    @Override
    public void supervisedRunData(Wekinator w, double[] inputs, boolean[] computeMask, double[] outputs) {
    }

    @Override
    public void supervisedTrainFinished(Wekinator w) throws IOException {
    }

    @Override
    public void trainingAccuracyComputed(Wekinator w, int modelNum, String val) {
    }

    @Override
    public void beginLog(String parentDir, KadenzeAssignment.KadenzeAssignmentType a) throws IOException {
    }

    @Override
    public void sameAssignmentRequested(KadenzeAssignment.KadenzeAssignmentType a) {
    }

    @Override
    public String getCurrentLoggingDirectory() {
        return "ERROR: NO DIRECTORY";
    }

    @Override
    public void logModelBuilderUpdated(Wekinator w, LearningModelBuilder mb, int i) {
    }

    @Override
    public void logPathUpdated(Wekinator w, int which, OSCOutput oldOutput, OSCOutput newOutput, LearningModelBuilder oldModelBuilder, LearningModelBuilder newModelBuilder, String[] selectedInputs, String[] selectedInputNames) {

    }

    @Override
    public void loadedFromFile(Wekinator w, String projectName) {
    }

    @Override
    public void selectedFeatures(Wekinator w, boolean[][] oldConnections, boolean[][] newConnections) {

    }

    @Override
    public void logSupervisedModelPrintedToConsole(Wekinator w, Path p) {

    }

    @Override
    public void logStartSupervisedRun(Wekinator w) {
    }

    @Override
    public String getZipDirectoryNameForAssignment() {
        return "";
    }

    @Override
    public void logInputInformation(Wekinator w, String inputString, int difficulty, String difficultyString) {
    }

    @Override
    public void logWrittenQuestion(Wekinator w, String idString, String textString) {
    }

    @Override
    public void logStartDtwRun(Wekinator w) {
    }

    @Override
    public void dtwDeleteAllExamplesForGesture(Wekinator w, int gestureNum) {
    }

    @Override
    public void dtwDeleteMostRecentExampleForGesture(Wekinator w, int gestureNum) {
    }

    @Override
    public void dtwThresholdChanged(Wekinator w, double oldMatchThreshold, double matchThreshold) {
    }

    @Override
    public void dtwClassifiedLast(Wekinator w, TimeSeries currentTimeSeries, double[] closestDistances, int closestClass) {
    }

    @Override
    public void logDtwModelUpdated(Wekinator w, DtwModel m, DtwSettings oldSettings, DtwSettings newDtwSettings, boolean[] selectedInputs, boolean[] inputSelection) {
    }
}
