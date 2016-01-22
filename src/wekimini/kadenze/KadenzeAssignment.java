/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

/**
 *
 * @author rebecca
 */
public class KadenzeAssignment {
    
    public enum KadenzeAssignmentType {
        NONE,
        ASSIGNMENT1,
        ASSIGNMENT2_PART1,
        ASSIGNMENT2_PART2
    }
    
    public static int getAssignmentNumber(KadenzeAssignmentType t) {
        if (t == KadenzeAssignmentType.NONE) {
            return -1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return 1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return 2;
        } else {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return -1;
        }
    }

    public static int getAssignmentSubPart(KadenzeAssignmentType t) {
        if (t == KadenzeAssignmentType.NONE) {
            return -1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return -1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1) {
            return 1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return 2;
        } else {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return -1;
        }
    }
    
    public static String getAssignmentLogfilename(KadenzeAssignmentType t) {
        if (t == KadenzeAssignmentType.NONE) {
            return "tmp.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return "assignment1.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1) {
            return "assignment2.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return "assignment3.txt";
        } else {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "tmp.txt";
        }
    }

    public static String getReadableName(KadenzeAssignmentType t) {
        if (t == KadenzeAssignmentType.NONE) {
            return "No assignment";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return "Assignment 1";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1) {
            return "Assignment 2, Part 1";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return "Assignment 2, Part 2";
        } else {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "";
        }
    }

    public static String getLogDirectory(KadenzeAssignmentType t) {
        if (t == KadenzeAssignmentType.NONE) {
            return "";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return "assignment1";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1) {
            return "assignment2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return "assignment2";
        } else {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "";
        }
    }

    public static KadenzeLogger getLoggerForAssignmentType(KadenzeAssignmentType assignmentType) throws Exception {
        
        if (assignmentType == KadenzeAssignmentType.NONE) {
            return new NoLogger();
        } else if (assignmentType == KadenzeAssignmentType.ASSIGNMENT1) {
            return new Assignment12Logger();
        } else if (assignmentType == KadenzeAssignmentType.ASSIGNMENT2_PART1) {
            return new Assignment12Logger();
        } else {
            throw new Exception("No logger for this assignment!");
        }
    } 
}
