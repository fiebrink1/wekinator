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
import java.util.List;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import wekimini.featureanalysis.BestInfoSelector;
import wekimini.gui.Study2SubmissionFrame;
import wekimini.learning.SVMModelBuilder;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class Study2AccuracyExperiment {
    
    public Wekinator w;
    public final String PROJECT_NAME = "Week6";
    //public final String ROOT_DIR = "../../studyData/Study1_logs";
    public final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study_2_logs/projects";
    public final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study2_analysis";
    public Participant participant;
    public Iterator featureIterator;
    public Iterator participantIterator;
    public ArrayList<Participant> participants;
    public boolean testSet = true;
    public final String[] blackList = new String[] {"P6", "P16", "P18"};
    public Map.Entry currentFeatures;
    public double evalStartTime = 0; 
    
    public static void main(String[] args)
    {
        Study2AccuracyExperiment e = new Study2AccuracyExperiment();
        e.runTests();
    }
    
    public void runTests()
    {
        HashMap<String, String> projects = getProjectLocations();
        participantIterator = projects.entrySet().iterator();
        participants = new ArrayList();
        if(participantIterator.hasNext())
        {
            runForNextParticipant();
        }
    }
    
    public void logParticipant()
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
    
    public void logAll()
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
    
    public void reset()
    {
        testSet = true;
        participant = new Participant();
    }
    
    public boolean isBlackListed(String pID)
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
    
    public void runForNextParticipant()
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
            System.out.println("MODEL TYPE:" + w.getSupervisedLearningManager().getPaths().get(0).getModelBuilder().getPrettyName());
//            participant.features.put("user",w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames());
//            String largeSet[];
//            List<Feature> library = w.getDataManager().featureManager.getFeatureGroups().get(0).getLibrary();
//            if(library.size() > Study2SubmissionFrame.MAX_LARGE_SET_SIZE)
//            {
//                largeSet = new String[(int)Study2SubmissionFrame.MAX_LARGE_SET_SIZE];
//                double threshold = (double)(Study2SubmissionFrame.MAX_LARGE_SET_SIZE / (double)(library.size()));
//                Feature[] above = w.getDataManager().getInfoGainRankings(0, threshold, true);
//                int ptr = 0;
//                for(Feature f:above)
//                {
//                    largeSet[ptr] = f.name;
//                    ptr++;
//                }
//            }
//            else
//            {
//                largeSet = new String[library.size()];
//                int ptr = 0;
//                for(Feature f:library)
//                {
//                    largeSet[ptr] = f.name;
//                    ptr++;
//                }
//            }
//            participant.features.put("all", largeSet);
//            int mean = 165;
//            w.getDataManager().selectFeaturesAutomatically(DataManager.AutoSelect.INFOGAIN, mean);
//            String[] ranked = w.getDataManager().selectedFeatureNames[0];
//            mean = 5;
//            
//            while(mean < largeSet.length)
//            {
//                String[] split = new String[mean];
//                System.arraycopy(ranked, 0, split, 0, mean);
//                participant.features.put("info"+mean,split);
//                mean +=20;
//            }
            participant.features.put("raw", new String[]{"AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ"});
            featureIterator = participant.features.entrySet().iterator();
            setNextFeatures();
            evaluate();
            participantIterator.remove();
//            w.getDataManager().setFeaturesForBestInfo(0, false,  new BestInfoSelector.BestInfoResultsReceiver() {
//                @Override
//                public void finished(int[] features)
//                {
//                   participant.features.put("best", (w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatureNames()));
//                   featureIterator = participant.features.entrySet().iterator();
//                   setNextFeatures();
//                   evaluate();
//                   participantIterator.remove();
//                }
//            });
        }
    }
            
    public void setNextFeatures()
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
    
    public void exportAllFeatures()
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
    
    public void evaluate()
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
    
    public void evaluatorPropertyChanged(PropertyChangeEvent evt) {
        
    }

    public void evaluatorModelFinished(int modelNum, String results, String confusion) {


    }

    public void evaluatorCancelled() {

    }
    
    public double getPercent(String res)
    {
        try {
            return Double.parseDouble((res.replaceAll("%", "")));
        } catch (NumberFormatException e)
        {
            return -1;
        }
        
    }

    public void evaluatorFinished(String[] results) 
    {
        if(testSet)
        {
            System.out.println("Done test set");
            double timeTaken = System.currentTimeMillis() - evalStartTime;
            participant.testSetTimes.put((String)currentFeatures.getKey(), timeTaken);
            participant.testSetResults.put((String)currentFeatures.getKey(),getPercent(results[0]));
            testSet = false;
            evaluate();
        }
        else
        {
            System.out.println("Done training set");
            double timeTaken = System.currentTimeMillis() - evalStartTime;
            participant.trainingSetTimes.put((String)currentFeatures.getKey(), timeTaken);
            participant.trainingSetResults.put((String)currentFeatures.getKey(), getPercent(results[0]));
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
    
    public HashMap<String, String> getProjectLocations()
    {
        HashMap<String, String> projects = new HashMap();
        File folder = new File(ROOT_DIR);
        File[] listOfFiles = folder.listFiles();
        for(File idFile : listOfFiles)
        {
            if(idFile.isDirectory())
            {
                String pID = idFile.getName();
                for(File projectFile : idFile.listFiles())
                {
                    if(projectFile.isDirectory() && projectFile.getName().contains(PROJECT_NAME))
                    {
                        File[] listOfStudyFiles = projectFile.listFiles();
                        for(File studyFile : listOfStudyFiles)
                        {
                            if(studyFile.getName().contains(PROJECT_NAME))
                            {
                                projects.put(pID, studyFile.getAbsolutePath());
                                break;
                            } 
                        }
                    }
                }
            }
        }
        return projects;
    }
}
