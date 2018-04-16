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

/**
 *
 * @author louismccallum
 */
public class AccuracyExperiment {
    
    private Wekinator w;
    private final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_logs";
    private final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_analysis";
    private ModelEvaluator evaluator;
    private Participant participant;
    private int featuresPtr;
    Iterator it;
    private ArrayList<Participant> participants;
    
    
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
        System.out.println(Arrays.toString(participant.results));
        System.out.println(Arrays.toString(participant.features));
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
    }
    
    private void reset()
    {
        featuresPtr = 0;
        participant = new Participant();
        participant.results = new String[6];
        participant.features = new String[6][];
    }
    
    private void runForNextParticipant()
    {
        reset();
                
        Map.Entry pair = (Map.Entry)it.next();
        System.out.println(pair.getKey() + " = " + pair.getValue());
        String location = (String) pair.getValue();
        participant.participantID = (String) pair.getKey();
        try {
            w = WekinatorSaver.loadWekinatorFromFile(location);
        } catch (Exception ex) {
            Logger.getLogger(AccuracyExperiment.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        participant.features[0] = w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames();
        participant.features[1] = w.getDataManager().featureManager.getFeatureGroups().get(0).getNames();

        participant.timeTakenForwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.WRAPPER_FORWARDS);
        //participant.timeTakenForwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN,10);
        participant.features[2] = w.getDataManager().selectedFeatureNames[0];

        //Select features with backwards select, log time taken
        participant.timeTakenBackwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.WRAPPER_BACKWARDS);
        //participant.timeTakenBackwards = w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN,10);
        participant.features[3] = w.getDataManager().selectedFeatureNames[0];

        int mean = (participant.features[2].length + participant.features[3].length) / 2;

        //Select features with info gain, log time taken 
        w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN, mean);
        participant.features[4] = w.getDataManager().selectedFeatureNames[0];

        w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.RANDOM, mean);
        participant.features[5] = w.getDataManager().selectedFeatureNames[0];

        //Get test set accuracy with user selected features (these should be automatically loaded?)
        setFeatures(participant.features[featuresPtr]);
        evaluate();
        it.remove(); 
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
        participant.results[featuresPtr] = results[0];
        featuresPtr++;
        if(featuresPtr < participant.features.length)
        {
            setFeatures(participant.features[featuresPtr]);
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
                File studyFolder = new File(file.getAbsolutePath() + File.separator + "featurnator_study_1");
                File[] listOfStudyFiles = studyFolder.listFiles();
                for(File studyFile : listOfStudyFiles)
                {
                    if(studyFile.getName().contains("ProjectFiles"))
                    {
                        String projectFile = studyFile.getAbsolutePath() + File.separator + "Study1.wekproj";
                        projects.put(pID, projectFile);
                        break;
                    } 
                }
            }
        }
        return projects;
    }
    
}
