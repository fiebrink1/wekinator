/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.IIOException;
import wekimini.GlobalSettings;
import wekimini.kadenze.KadenzeAssignment.KadenzeAssignmentType;

/**
 *
 * @author rebecca
 */
public class KadenzeLogging {

    protected static List<KadenzeListener> listenerList = new LinkedList<>();
    private static KadenzeLogger logger = new NoLogger();

    private static boolean isCurrentlyLogging = false;

    private static KadenzeAssignmentType currentAssignmentType = KadenzeAssignmentType.NONE;
    //private static KadenzeAssignment currentAssignment = null;

    public static final String PROP_CURRENT_ASSIGNMENT_TYPE = "currentAssignmentType";

    //public static final KadenzeAssignment[] comboOptions = {ASSIGNMENT1, ASSIGNMENT2_PART1A, ASSIGNMENT2_PART1B};
    //public static final String[] comboStrings = {"Assignment 1", "Assignment 2: Part 1", "Assignment 2: Part 2"};
    public static boolean isCurrentlyLogging() {
        return isCurrentlyLogging;
    }

    /**
     * Get the value of currentAssignment
     *
     * @return the value of currentAssignment
     */
    public static KadenzeAssignmentType getCurrentAssignmentType() {
        return currentAssignmentType;
    }

    /**
     * Set the value of currentAssignment
     *
     * @param currentAssignment new value of currentAssignment
     */
    private static void setCurrentAssignmentType(KadenzeAssignmentType newAssignmentType) {
        notifyAssignmentChangeListeners(newAssignmentType);
    }

    public static void addListener(KadenzeListener l) {
        listenerList.add(l);
    }

    public static void removeListener(KadenzeListener l) {
        listenerList.remove(l);
    }

    private static void notifyAssignmentChangeListeners(KadenzeAssignmentType newAssignmentType) {
        for (KadenzeListener l : listenerList) {
            l.assignmentChanged(newAssignmentType);
        }
    }

    private static void notifyAssignmentStartedListeners(KadenzeAssignmentType newAssignmentType) {
        for (KadenzeListener l : listenerList) {
            l.assignmentStarted(newAssignmentType);
        }
    }

    private static void notifyAssignmentStoppedListeners() {
        for (KadenzeListener l : listenerList) {
            l.assignmentStopped();
        }
    }

    public static String createZipForAssignment() throws IOException {
        if (currentAssignmentType == KadenzeAssignmentType.NONE) {
            throw new IIOException("Error: Not currently logging, cannot submit");
        }
        logger.flush();
        String filename = logger.createZip();
        return filename;
    }

    //TODO: Make sure you can't do this while WEkinator is running!
    public static void startLoggingForAssignment(KadenzeAssignmentType a) throws IOException {
        String dir = GlobalSettings.getInstance().getKadenzeSaveLocation();
        if (isCurrentlyLogging) {
            if (a == currentAssignmentType) {
                getLogger().sameAssignmentRequested(a);
                return;
            } else {
                getLogger().closeLog();
                try {
                    logger = KadenzeAssignment.getLoggerForAssignmentType(currentAssignmentType);
                } catch (Exception ex) {
                    throw new IOException("Could not get logger for assignment " + KadenzeAssignment.getReadableName(a));
                }
                currentAssignmentType = a;
                logger.beginLog(dir, a);
                notifyAssignmentChangeListeners(currentAssignmentType);
            }
        } else {
            try {
                logger = KadenzeAssignment.getLoggerForAssignmentType(a);
            } catch (Exception ex) {
                throw new IOException("Could not get logger for assignment " + KadenzeAssignment.getReadableName(a));
            }
            currentAssignmentType = a;
            logger.beginLog(dir, a);
            isCurrentlyLogging = true;
            notifyAssignmentStartedListeners(currentAssignmentType);
        }
    }

    public static void noLogging() {
        logger = new NoLogger();
        currentAssignmentType = KadenzeAssignmentType.NONE;
        if (isCurrentlyLogging) {
            isCurrentlyLogging = false;
            notifyAssignmentStoppedListeners();
        }
    }

    public static KadenzeLogger getLogger() {
        return logger;
    }

    public interface KadenzeListener {
        public void assignmentChanged(KadenzeAssignmentType ka);
        public void assignmentStarted(KadenzeAssignmentType ka);
        public void assignmentStopped();
    }

}
