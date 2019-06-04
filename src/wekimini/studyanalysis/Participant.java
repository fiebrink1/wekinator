/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.studyanalysis;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author louismccallum
 */
public class Participant {
    public double timeTakenForwards;
    public double timeTakenBackwards;
    public int numExamples;
    public String participantID;
    public HashMap<String,Double> testSetResults;
    public HashMap<String,Double> testSetTimes;
    public HashMap<String,Double> trainingSetTimes;
    public HashMap<String,Double> trainingSetResults;
    public HashMap<String, String[]> features;
    public int trainingSetSize;
    public int testSetSize;
    public long timeSpentRunning = 0;
    public long timeSpentRecording = 0;
    public int cvCount = 0;
    public int runCount = 0;
    public int newFeatureRunCount = 0;
    public int newDataRunCount = 0;
    public int autoSelectCount = 0;
    public int thresholdSelectCount = 0;
    public int addFeaturesCount = 0;
    public int addPanelCount = 0;
    public int removePanelCount = 0;
    public Boolean didEvalLargeLast = false;
    public Boolean didEvalRawLast = false;
    
    public Participant()
    {
        features = new HashMap();
        testSetResults = new HashMap();
        testSetTimes = new HashMap();
        trainingSetTimes = new HashMap();
        trainingSetResults = new HashMap();
    }
}
