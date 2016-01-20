/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import wekimini.GlobalSettings;
import static wekimini.kadenze.KadenzeLogging.KadenzeAssignment.*;

/**
 *
 * @author rebecca
 */
public class KadenzeLogging {
    protected static List<KadenzeListener> listenerList = new LinkedList<>();
    private static KadenzeLogger logger = new KadenzeLogger();
    private static boolean isCurrentlyLogging = false;
    private static KadenzeAssignment currentAssignment = ASSIGNMENT1;
    public static final String PROP_CURRENTASSIGNMENT1 = "currentAssignment1";

    public static boolean isCurrentlyLogging() {
        return isCurrentlyLogging;
    }
    /**
     * Get the value of currentAssignment1
     *
     * @return the value of currentAssignment1
     */
    public static KadenzeAssignment getCurrentAssignment() {
        return currentAssignment;
    }

    /**
     * Set the value of currentAssignment1
     *
     * @param currentAssignment1 new value of currentAssignment1
     */
    private static void setCurrentAssignment(KadenzeAssignment newAssignment) {
        notifyAssignmentChangeListeners(newAssignment);
    }
    
    public static void addListener(KadenzeListener l) {
        listenerList.add(l);
    }

    public static void removeListener(KadenzeListener l) {
        listenerList.remove(l);
    }

    private static void notifyAssignmentChangeListeners(KadenzeAssignment newAssignment) {
        for (KadenzeListener l : listenerList) {
            l.assignmentChanged(newAssignment);
        }
    }
    
    private static void notifyAssignmentStartedListeners(KadenzeAssignment newAssignment) {
        for (KadenzeListener l : listenerList) {
            l.assignmentStarted(newAssignment);
        }
    }
    
    private static void notifyAssignmentStoppedListeners() {
        for (KadenzeListener l : listenerList) {
            l.assignmentStopped();
        }
    }

    public static String submitAssignment() throws IOException {
        //try {
        logger.flush();
        String filename = logger.createZip();
        //} 
        return filename;
    }
    
    public enum KadenzeAssignment {
        ASSIGNMENT1,
        ASSIGNMENT2_PART1,
        ASSIGNMENT2_PART2
    }
    
    public static final KadenzeAssignment[] comboOptions = {ASSIGNMENT1, ASSIGNMENT2_PART1, ASSIGNMENT2_PART2};
    public static final String[] comboStrings = {"Assignment 1", "Assignment 2: Part 1", "Assignment 2: Part 2"};
    
    public static void startLoggingForAssignment(KadenzeAssignment a) throws IOException {
        String dir = GlobalSettings.getInstance().getKadenzeSaveLocation();
        /*String assignmentDir, suffix;
      
        switch (a) {
            case ASSIGNMENT1:
                assignmentDir = "assignment1";
                suffix = "1";
                break;
            case ASSIGNMENT2_PART1: 
                assignmentDir = "assignment2";
                suffix = "2Part1";
                break;
            case ASSIGNMENT2_PART2:
                assignmentDir = "assignment2";
                suffix = "2Part2";
                break;
            default:
                assignmentDir = "tmp";
                suffix = "";     
        } 
        String myAssignmentDir = dir + File.separator + assignmentDir + File.separator; */

        if (isCurrentlyLogging) {
            if (a == currentAssignment) {
                getLogger().sameAssignmentRequested(a);
            } else {
                getLogger().switchToAssignment(dir, a);
                currentAssignment = a;
                notifyAssignmentChangeListeners(currentAssignment);
            }
        } else {
            logger.beginLog(dir, a);
            currentAssignment = a;
            isCurrentlyLogging = true;
            notifyAssignmentStartedListeners(currentAssignment);
        }
    }
    
    public static void noLogging() {
        //ERROR: NOt implemented yet!
        System.out.println("ERROR: NO LOGGING NOT IMPLEMENTED YET");
        isCurrentlyLogging = false;
        notifyAssignmentStoppedListeners();
    }
    
    public static KadenzeLogger getLogger() {
        return logger;
    }
    
    public interface KadenzeListener {
        public void assignmentChanged(KadenzeAssignment ka);
        public void assignmentStarted(KadenzeAssignment ka);
        public void assignmentStopped();
    }
    
}
