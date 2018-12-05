/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study2analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import wekimini.study1analysis.Participant;

/**
 *
 * @author louismccallum
 */
public class LogsExperiment {
    
    private static final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study_2_logs/projects";
    private final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study_2_analysis";
    private final String LOG_DIR = "featurnator_study_2";
    private final String LOG_FILE = "featurnator_study_2.txt";
    private final String PROJECT_NAME = "Week6.wekproj";      
    private Iterator participantIterator;
    private ArrayList<Participant> participants;
    private Participant participant;
    
    public static void main(String[] args)
    {
        LogsExperiment e = new LogsExperiment();
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
    
     private void runForNextParticipant()
    {
        reset();
                
        if(participantIterator.hasNext())
        {
            Map.Entry pair = (Map.Entry)participantIterator.next();
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
            for(String line : lines)
            {
                System.out.println(line);
            }
            participantIterator.remove(); 
            runForNextParticipant();
        }
        

    }
    
    private void reset()
    {
        participant = new Participant();
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
                for(File porjectDir : projectDirs)
                {
                    if(porjectDir.getName().contains("Week6"))
                    {
                        //String projectFile = porjectDir.getAbsolutePath() + File.separator + PROJECT_NAME;
                        String logFile = porjectDir.getAbsolutePath() + File.separator + LOG_DIR + File.separator + LOG_FILE;
                        File f = new File(logFile);
                        System.out.println(logFile);
                        if(f.exists() && !f.isDirectory()) { 
                            projects.put(pID, logFile);
                        }
                    }
                }
                
            }
        }
        return projects;
    } 
}
