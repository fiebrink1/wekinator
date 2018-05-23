/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study1analysis;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import wekimini.learning.SVMModelBuilder;

/**
 *
 * @author louismccallum
 */
public class AccuracyExperiment {
    
    private Wekinator w;
    private final int NUM_FEATURE_SETS = 6;
    private final String STUDY_DIR = "featurnator_study_1";
    private final String PROJECT_NAME = "Study1.wekproj";
    private final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_logs";
    private final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_analysis";
    private Participant participant;
    private int featuresPtr;
    private Iterator it;
    private ArrayList<Participant> participants;
    private boolean testSet = true;
    private final String[] blackList = new String[] {"Esben_Pilot", "Francisco_Pilot", "Sam_Pilot"};
    
    public static void main(String[] args)
    {
        AccuracyExperiment e = new AccuracyExperiment();
        e.runTests();
    }
    
    private void runTests()
    {
        HashMap<String, String> projects = getProjectLocations();
        it = projects.entrySet().iterator();
        participants = new ArrayList();
        if(it.hasNext())
        {
            runForNextParticipant();
        }
    }
    
    private void logParticipant()
    {
        System.out.println(participant.participantID);
        System.out.println(participant.timeTakenForwards);
        System.out.println(participant.timeTakenBackwards);
        System.out.println(Arrays.toString(participant.testSetResults));
        System.out.println(Arrays.toString(participant.trainingSetResults));
        participants.add(participant);
    }
    
    private void logAll()
    {
        ObjectMapper json = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        String path = RESULTS_DIR + File.separator + "accuracyExperiment_" + dateFormat.format(date) + ".json";
        try{
            json.writeValue(new FileOutputStream(path), participants);
        }
        catch(Exception e)
        {
            System.out.println("ERROR: writing file");
        }
        System.exit(0);
    }
    
    private void reset()
    {
        featuresPtr = 0;
        testSet = true;
        participant = new Participant(NUM_FEATURE_SETS);
    }
    
    private boolean isBlackListed(String pID)
    {
        for(String blackListed : blackList)
        {
            if(pID.equals(blackListed))
            {
                return true;
            }
        }
        return false;
    }
    
    private void runForNextParticipant()
    {
        reset();
                
        if(it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
       
            while(isBlackListed((String)pair.getKey()))
            {
                System.out.println("Skipping " + (String)pair.getKey() + "(blacklisted)");
                if(!it.hasNext())
                {
                    logAll();
                    return;
                }
                pair = (Map.Entry)it.next();
            }

            System.out.println(pair.getKey() + " = " + pair.getValue());
            String location = (String) pair.getValue();
            String pID = (String) pair.getKey();
            participant.participantID = pID;

            try {
                w = WekinatorSaver.loadWekinatorFromFile(location);
            } catch (Exception ex) {
                Logger.getLogger(AccuracyExperiment.class.getName()).log(Level.SEVERE, null, ex);
            }
            //w.getSupervisedLearningManager().setModelBuilderForPath(new SVMModelBuilder(), 0);
            participant.numExamples = w.getDataManager().getTrainingDataForOutput(0).numInstances();
            participant.userFeatures = w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames();
            participant.allFeatures = w.getDataManager().featureManager.getFeatureGroups().get(0).getNames();
            participant.rawFeatures = new String[]{"AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ"};
            participant.meanFeatures = new String[]{"MeanAccX", "MeanAccY", "MeanAccZ", "MeanGyroX", "MeanGyroY", "MeanGyroZ"};
            /*
            
            //Select features with backwards select, log time taken
            participant.timeTakenBackwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.WRAPPER_BACKWARDS);
            //participant.timeTakenBackwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN,10);
            participant.backwardsFeatures = w.getDataManager().selectedFeatureNames[0];

            participant.timeTakenForwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.WRAPPER_FORWARDS);
            //participant.timeTakenForwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN,10);
            participant.forwardsFeatures = w.getDataManager().selectedFeatureNames[0];

            
            int mean = (participant.forwardsFeatures.length + participant.backwardsFeatures.length) / 2;

            */
            
            int mean = participant.userFeatures.length;

            //Select features with info gain, log time taken 
            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN, mean);
            participant.infoGainFeatures = w.getDataManager().selectedFeatureNames[0];

            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.RANDOM, mean);
            participant.randomFeatures = w.getDataManager().selectedFeatureNames[0];

            //Get test set accuracy with user selected features (these should be automatically loaded?)
            setFeatures(featuresForPtr(featuresPtr));
            evaluate();
            it.remove(); 
        }
    }
    
    private String[] featuresForPtr(int ptr)
    {
        switch(ptr)
        {
            case 0 : return participant.userFeatures; 
            case 1 : return participant.allFeatures;
            case 2 : return participant.infoGainFeatures;
            case 3 : return participant.randomFeatures;
            case 4 : return participant.rawFeatures;
            case 5 : return participant.meanFeatures;
            case 6 : return participant.backwardsFeatures;
            case 7 : return participant.forwardsFeatures;
            default: return participant.allFeatures;
        }
    }
            
    private void setFeatures(String[] ft)
    {
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeAll();
        for(String f:ft)
        {
            f = f.replaceAll(":0", "");
            w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(f);
        }
    }
    
    private void evaluate()
    {
        
        ModelEvaluator evaluator = new ModelEvaluator(w, new ModelEvaluator.EvaluationResultsReceiver() {

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
        ModelEvaluationFrame.EvaluationMode eval = testSet ? ModelEvaluationFrame.EvaluationMode.TESTING_SET : ModelEvaluationFrame.EvaluationMode.CROSS_VALIDATION;
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
        if(testSet)
        {
            participant.testSetResults[featuresPtr] = Double.parseDouble((results[0].replaceAll("%", "")));
            testSet = false;
            evaluate();
        }
        else
        {
            participant.trainingSetResults[featuresPtr] = Double.parseDouble((results[0].replaceAll("%", "")));
            testSet = true;
            featuresPtr++;
            
            if(featuresPtr < NUM_FEATURE_SETS)
            {
                setFeatures(featuresForPtr(featuresPtr));
                evaluate();
            }
            else if(it.hasNext())
            {
                logParticipant();
                runForNextParticipant();
            }
            else
            {
                logParticipant();
                logAll();
            }
        }
    }
    
    private HashMap<String, String> getProjectLocations()
    {
        HashMap<String, String> projects = new HashMap();
        File folder = new File(ROOT_DIR);
        File[] listOfFiles = folder.listFiles();
        for(File file : listOfFiles)
        {
            if(file.isDirectory())
            {
                String pID = file.getName();
                File studyFolder = new File(file.getAbsolutePath() + File.separator + STUDY_DIR);
                File[] listOfStudyFiles = studyFolder.listFiles();
                for(File studyFile : listOfStudyFiles)
                {
                    if(studyFile.getName().contains("ProjectFiles"))
                    {
                        String projectFile = studyFile.getAbsolutePath() + File.separator + PROJECT_NAME;
                        projects.put(pID, projectFile);
                        break;
                    } 
                }
            }
        }
        return projects;
    }
}
