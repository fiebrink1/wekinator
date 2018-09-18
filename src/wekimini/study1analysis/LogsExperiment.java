/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study1analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author louismccallum
 */
public class LogsExperiment {
    
    private final String STUDY_DIR = "featurnator_study_1";
    private final String PROJECT_NAME = "featurnator_study_1_notest.txt";
    private final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_logs";
    private final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_analysis";
    private Participant participant;
    private Iterator participantIterator;
    private final String[] blackList = new String[] {"Esben_Pilot", "Francisco_Pilot", "Sam_Pilot", "1", "P1", "P10"};
    private ArrayList<Participant> participants = new ArrayList();
    
    public static void main(String[] args)
    {
        LogsExperiment e = new LogsExperiment();
        e.runTests();
    }
    
    private void runTests()
    {
        HashMap<String, String> projects = getProjectLocations();
        participantIterator = projects.entrySet().iterator();
        if(participantIterator.hasNext())
        {
            runForNextParticipant();
        }
    }
    
    private void reset()
    {
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
    
    private void logParticipant()
    {
       participants.add(participant);
    }
    
    private void logAll()
    {
        System.out.println("LOGGING ALL");
        ObjectMapper json = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        String path = RESULTS_DIR + File.separator + "logExperiment" + dateFormat.format(date) + ".json";
        try{
            json.writeValue(new FileOutputStream(path), participants);
        }
        catch(Exception e)
        {
            System.out.println("ERROR: writing file");
        }
        System.exit(0);
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
            ArrayList<String> lines = new ArrayList();
            participant.participantID = pID;
            try 
            {
                Files.lines(Paths.get(location)).forEach(line->lines.add(line));
            } 
            catch (Exception e)
            {
                
            }
            long cumSumRunning = 0;
            long cumSumRecording = 0;
            long prevStart = 0;
            int cvCtr = 0;
            int runCtr = 0;
            int newFeatureRunCtr = 0;
            int newDataRunCtr = 0;
            boolean running = false;
            boolean recording = false;
            boolean didChangeFeatures = false;
            boolean didChangeData = false;
            ArrayList<String> exploredFeatures = new ArrayList();
            
            for(String line : lines)
            {
                String split[] = line.split(",");
                if(split[2].equals("START_RUN"))
                {
                    prevStart = Long.parseUnsignedLong(split[0]);
                    running = true;
                }
                if(running && split[2].equals("RUN_STOP"))
                {
                    long endTime = Long.parseUnsignedLong(split[0]);
                    cumSumRunning += (endTime - prevStart);
                    runCtr++;
                    running = false;
                    newFeatureRunCtr += didChangeFeatures ? 1 : 0;
                    newDataRunCtr += didChangeData ? 1 : 0;
                    didChangeFeatures = false;
                    didChangeData = false;
                }
                if(split[2].equals("SUPERVISED_RECORD_START"))
                {
                    prevStart = Long.parseUnsignedLong(split[0]);
                    recording = true;
                    didChangeData = true;
                }
                if(recording && split[2].equals("SUPERVISED_RECORD_STOP"))
                {
                    long endTime = Long.parseUnsignedLong(split[0]);
                    cumSumRecording += (endTime - prevStart);
                    recording = false;
                }
                if(line.contains("CROSS_VALIDATATION"))
                {
                    cvCtr++;
                }
                if(split[2].equals("FEATURE_REMOVED") || split[2].equals("FEATURE_ADDED"))
                {
                    didChangeFeatures = true;
                    if(split[2].equals("FEATURE_ADDED"))
                    {
                        if(split[3].length() > 3)
                        {
                            String featuresAdded = split[3].substring(1, split[3].length()-2);
                            String[] sepFt = featuresAdded.split(",");
                            for(String ft : sepFt)
                            {
                                if(!exploredFeatures.contains(ft))
                                {
                                    exploredFeatures.add(ft);
                                }
                            } 
                        } 
                    }
                }
            }
            System.out.println(cumSumRunning);
            System.out.println(cumSumRecording);
            System.out.println(cvCtr);
            String[] f = new String[exploredFeatures.size()];
            f = exploredFeatures.toArray(f);
            participant.features.put("explored", f);
            participant.timeSpentRunning = cumSumRunning;
            participant.timeSpentRecording = cumSumRecording;
            participant.cvCount = cvCtr;
            participant.runCount = runCtr;
            participant.newDataRunCount = newDataRunCtr;
            participant.newFeatureRunCount = newFeatureRunCtr;
            participantIterator.remove(); 
            logParticipant();
            runForNextParticipant();
        }
        else
        {
            logAll();
            return;
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
                    if(studyFile.getName().contains(PROJECT_NAME))
                    {
                        String projectFile = studyFile.getAbsolutePath();
                        projects.put(pID, projectFile);
                        break;
                    } 
                }
            }
        }
        return projects;
    }
}
