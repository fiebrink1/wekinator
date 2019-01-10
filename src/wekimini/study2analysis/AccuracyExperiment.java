/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study2analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import wekimini.DataManager;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.WekinatorSaver;
import wekimini.gui.ModelEvaluationFrame;
import wekimini.learning.ModelEvaluator;
import wekimini.study1analysis.Participant;

/**
 *
 * @author louismccallum
 */
public class AccuracyExperiment {
    
    private Wekinator w;
    private static final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study_2_logs/projects";
    private final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study_2_analysis";
    private final String PROJECT_NAME = "Week6.wekproj";     
    private final String TEST_SET_PATH = "current" + File.separator + "testData.arff";     
    private Participant participant;
    private Iterator featureIterator;
    private Iterator participantIterator;
    private ArrayList<Participant> participants;
    private boolean testSet = true;
    //P3 + P6 have no test data 
    private final String[] blackList = new String[] {"P3", "P6"};
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
            participant.numExamples = w.getDataManager().getTrainingDataForOutput(0).numInstances();
            participant.features.put("user",w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames());
            participant.features.put("all",(w.getDataManager().featureManager.getFeatureGroups().get(0).getNames()));
            participant.features.put("raw",new String[]{"AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ"});
            
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
                File[] projectDirs = file.listFiles();
                for(File projectDir : projectDirs)
                {
                    if(projectDir.getName().contains("Week6"))
                    {
                        String projectFile = projectDir.getAbsolutePath() + File.separator + PROJECT_NAME;
                        String testSetFile = projectDir.getAbsolutePath() + File.separator + TEST_SET_PATH;
                        File f = new File(testSetFile);
                        if(f.exists() && !f.isDirectory()) { 
                            projects.put(pID, projectFile);
                        }
                    }
                }
                
            }
        }
        return projects;
    } 
}
