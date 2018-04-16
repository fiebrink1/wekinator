/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study1analysis;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.DataManager;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.WekinatorSaver;
import wekimini.gui.ModelEvaluationFrame;
import wekimini.learning.ModelEvaluator;
import wekimini.modifiers.Feature;
import wekimini.util.ConfusionParser;

/**
 *
 * @author louismccallum
 */
public class AccuracyExperiment {
    
    private Wekinator w;
    private final String ROOT_DIR = "/Users/louismccallum/study1";
    private final String CVS_KEY = "id, userAcc, forwardAcc, backwardsAcc, infoGainAcc, allAcc, randomAcc, forwardsTime, backwardsTime";
    private ModelEvaluator evaluator;
    private String[] results;
    private int featuresPtr;
    private String[][] features;
    
    public static void main(String[] args)
    {
        AccuracyExperiment e = new AccuracyExperiment();
        e.runTests();
    }
    
    private void runTests()
    {
        HashMap<String, String> projects = getProjectLocations();
        Iterator it = projects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            String location = (String) pair.getValue();
            String participantID = (String) pair.getKey();
            try {
                w = WekinatorSaver.loadWekinatorFromFile(location);
            } catch (Exception ex) {
                Logger.getLogger(AccuracyExperiment.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            featuresPtr = 0;
            results = new String[6];
            
            features = new String[6][];
            features[0] = w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames();
            features[1] = w.getDataManager().featureManager.getFeatureGroups().get(0).getNames();
            
            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.WRAPPER_FORWARDS);
            features[2] = w.getDataManager().selectedFeatureNames[0];
            
            //Select features with backwards select, log time taken 
            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.WRAPPER_BACKWARDS);
            features[3] = w.getDataManager().selectedFeatureNames[0];
            
            int mean = (features[2].length + features[3].length) / 2;
            
            //Select features with info gain, log time taken 
            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN, mean);
            features[4] = w.getDataManager().selectedFeatureNames[0];
            
            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.RANDOM, mean);
            features[5] = w.getDataManager().selectedFeatureNames[0];
            
            //Get test set accuracy with user selected features (these should be automatically loaded?)
            setFeatures(features[featuresPtr]);
            evaluate();
            it.remove(); 
        }
        
    }
    
    private void setFeatures(String[] ft)
    {
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeAll();
        for(String f:ft)
        {
            w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(f);
        }
    }
    
    private void evaluate()
    {
        
        evaluator = new ModelEvaluator(w, new ModelEvaluator.EvaluationResultsReceiver() {

            @Override
            public void finishedModel(int modelNum, String results, String confusion) {
                evaluatorModelFinished(modelNum, results, confusion);
            }

            @Override
            public void finished(String[] results) {
                evaluatorFinished(results);
            }

            @Override
            public void cancelled() {
                evaluatorCancelled();
            }
        });
        ModelEvaluationFrame.EvaluationMode eval = ModelEvaluationFrame.EvaluationMode.TESTING_SET;
        Path p = w.getSupervisedLearningManager().getPaths().get(0);
        LinkedList<Path> paths = new LinkedList<>();
        paths.add(p);
        evaluator.evaluateAll(paths, eval, 10, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                evaluatorPropertyChanged(evt);
            }

        });
    }
    
    private void evaluatorPropertyChanged(PropertyChangeEvent evt) {
        
    }

    private void evaluatorModelFinished(int modelNum, String results, String confusion) {


    }

    private void evaluatorCancelled() {

    }

    private void evaluatorFinished(String[] results) 
    {
        this.results[featuresPtr] = results[0];
        featuresPtr++;
        setFeatures(features[featuresPtr]);
        evaluate();
    }
    
    private HashMap<String, String> getProjectLocations()
    {
        HashMap<String, String> projects = new HashMap();
        File folder = new File(ROOT_DIR);
        File[] listOfFiles = folder.listFiles();
        for(File file : listOfFiles)
        {
            String[] split = file.getName().split("_");
            String participantID = split[1];
            File studyFolder = new File(file.getAbsolutePath() + File.pathSeparator + "featurnator_study_1");
            File[] listOfStudyFiles = studyFolder.listFiles();
            for(File studyFile : listOfStudyFiles)
            {
                if(studyFile.getName().contains("ProjectFiles"))
                {
                    String projectFile = studyFile.getAbsolutePath() + File.pathSeparator + "Study1.wekproj";
                    projects.put(participantID, projectFile);
                    break;
                } 
            }
        }
        return projects;
    }
    
}
