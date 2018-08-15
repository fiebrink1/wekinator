/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study1analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.DataManager;
import wekimini.Wekinator;
import wekimini.WekinatorSaver;

/**
 *
 * @author louismccallum
 */
public class UserPicksExperiment {
    
    private Wekinator w;
    private final String STUDY_DIR = "featurnator_study_1";
    private final String PROJECT_NAME = "Study1.wekproj";
    private final String ROOT_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_logs";
    private final String RESULTS_DIR = "/Users/louismccallum/Documents/Goldsmiths/Study1_analysis";
    private Participant participant;
    private Iterator participantIterator;
    private final String[] blackList = new String[] {"Esben_Pilot", "Francisco_Pilot", "Sam_Pilot", "1"};
    private HashMap<String, Integer> ranks = new HashMap();
    
    public static void main(String[] args)
    {
        UserPicksExperiment e = new UserPicksExperiment();
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
       System.out.println(ranks);
    }
    
    private void logAll()
    {
        Iterator it = ranks.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            String ft = (String)pair.getKey();
            Integer cumSum = (Integer)pair.getValue();
            System.out.println(ft + ":"+ cumSum/17);
            ranks.put(ft,cumSum/17);
        }
        System.out.println(ranks);
        ObjectMapper json = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        String path = RESULTS_DIR + File.separator + "rankExperiment" + dateFormat.format(date) + ".json";
        try{
            json.writeValue(new FileOutputStream(path), ranks);
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
            participant.participantID = pID;
            
            try {
                w = WekinatorSaver.loadWekinatorFromFile(location);
            } catch (Exception ex) {
                Logger.getLogger(AccuracyExperiment.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            participantIterator.remove(); 
            logParticipant();
            runForNextParticipant();
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
