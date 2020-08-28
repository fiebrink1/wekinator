/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.studyanalysis;

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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import wekimini.featureanalysis.BestInfoSelector;
import wekimini.learning.SVMModelBuilder;

/**
 *
 * @author louismccallum
 */
public class AccuracyExperiment {
    
    private Wekinator w;
    private final String STUDY_DIR = "featurnator_study_1";
    private final String PROJECT_NAME = "Study1.wekproj";
    //private final String ROOT_DIR = "../../studyData/Study1_logs";
    private final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_logs";
    private final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_analysis";
    private Participant participant;
    private Iterator featureIterator;
    private Iterator participantIterator;
    private ArrayList<Participant> participants;
    private boolean testSet = true;
    //"P1","P2","P3","P4","P5","P6","P7","P8","P9","P10","P11","P12","P13","P15","P16","P17",
    private final String[] blackList = new String[] {"Esben_Pilot", "Francisco_Pilot", "Sam_Pilot", "1"};
    private Map.Entry currentFeatures;
    private double evalStartTime = 0; 
    
    public static void main(String[] args)
    {
        AccuracyExperiment e = new AccuracyExperiment();
        e.runTests();
    }
    
    private void runTests()
    {
        HashMap<String, String> projects = getProjectLocations();
        participantIterator = projects.entrySet().iterator();
        participants = new ArrayList();
        if(participantIterator.hasNext())
        {
            runForNextParticipant();
        }
    }
    
    private void logParticipant()
    {
        System.out.println(participant.participantID);
        System.out.println(participant.timeTakenForwards);
        System.out.println(participant.timeTakenBackwards);
        System.out.println(participant.testSetResults);
        System.out.println(participant.trainingSetResults);
        participants.add(participant);
        ObjectMapper json = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        String path = RESULTS_DIR + File.separator + "accuracyExperiment_temp_" + dateFormat.format(date) + ".json";
        try{
            json.writeValue(new FileOutputStream(path), participants);
        }
        catch(Exception e)
        {
            System.out.println("ERROR: writing file");
        }
        exportAllFeatures();
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
        testSet = true;
        participant = new Participant();
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
                
        if(participantIterator.hasNext())
        {
            Map.Entry pair = (Map.Entry)participantIterator.next();
       
            while(isBlackListed((String)pair.getKey()))
            {
                System.out.println("Skipping " + (String)pair.getKey() + "(blacklisted)");
                if(!participantIterator.hasNext())
                {
                    logAll();
                    return;
                }
                pair = (Map.Entry)participantIterator.next();
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
//            participant.features.put("user",w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames());
//            participant.features.put("all",(w.getDataManager().featureManager.getFeatureGroups().get(0).getNames()));
//            
//            int mean = 165;
//            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN, mean);
//            String[] ranked = w.getDataManager().selectedFeatureNames[0];
//            mean = 5;
//            for(int i = 0; i < 9; i++)
//            {
//                String[] split = new String[mean];
//                System.arraycopy(ranked, 0, split, 0, mean);
//                participant.features.put("info"+i,split);
//                mean +=20;
//            }
//            
//            participant.features.put("raw",new String[]{"AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ"});

//            System.out.println("starting forwards search");
//            participant.timeTakenForwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.WRAPPER_FORWARDS);
//            System.out.println("completed forwards search in " + participant.timeTakenForwards);
//            participant.features.put("forwards", w.getDataManager().selectedFeatureNames[0]);
            double now = System.currentTimeMillis();
            w.getDataManager().setFeaturesForBestInfo(0, false,  new BestInfoSelector.BestInfoResultsReceiver() {
                @Override
                public void finished(int[] features)
                {
                   participant.features.put("best", (w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames()));
                   featureIterator = participant.features.entrySet().iterator();
                   participant.timeTakenBest = System.currentTimeMillis() - now;
                   setNextFeatures();
                   evaluate();
                   participantIterator.remove();
                }
            });
            
            featureIterator = participant.features.entrySet().iterator();
            
            setNextFeatures();
            evaluate();
            participantIterator.remove(); 
        }
    }
            
    private void setNextFeatures()
    {
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeAll();
        currentFeatures = (Map.Entry)featureIterator.next();
        System.out.println("setting features for " + currentFeatures.getKey());
        for(String f:(String[])currentFeatures.getValue())
        {
            f = f.replaceAll(":0", "");
            w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(f);
        }
    }
    
    private void exportAllFeatures()
    {
        Instances dataSet = w.getDataManager().getAllFeaturesInstances(0,false);
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataSet);
        try {
            saver.setFile(new File(RESULTS_DIR + "/" + participant.participantID +"_allFeatures_data.arff"));
            saver.writeBatch();
        } catch (IOException ex) {
            Logger.getLogger(AccuracyExperiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void evaluate()
    {
        evalStartTime = System.currentTimeMillis();
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
            System.out.println("Done test set");
            double timeTaken = System.currentTimeMillis() - evalStartTime;
            participant.testSetTimes.put((String)currentFeatures.getKey(), timeTaken);
            participant.testSetResults.put((String)currentFeatures.getKey(), Double.parseDouble((results[0].replaceAll("%", ""))));
            testSet = false;
            evaluate();
        }
        else
        {
            System.out.println("Done training set");
            double timeTaken = System.currentTimeMillis() - evalStartTime;
            participant.trainingSetTimes.put((String)currentFeatures.getKey(), timeTaken);
            participant.trainingSetResults.put((String)currentFeatures.getKey(), Double.parseDouble((results[0].replaceAll("%", ""))));
            testSet = true;
            
            if(featureIterator.hasNext())
            {
                setNextFeatures();
                evaluate();
            }
            else if(participantIterator.hasNext())
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
        System.out.println(ROOT_DIR);
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
