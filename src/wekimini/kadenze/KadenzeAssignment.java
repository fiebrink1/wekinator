/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class KadenzeAssignment {

    private static final Logger logger = Logger.getLogger(KadenzeAssignment.class.getName());

    public static KadenzeAssignmentType getAssignment(int p, int p2) {
        if (p == 1 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT1;
        } else if (p == 2 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT2_PART1A;
        } else if (p == 2 && p2 == 2) {
            return KadenzeAssignmentType.ASSIGNMENT2_PART1B;
        } else if (p == 2 && p2 == 3) {
            return KadenzeAssignmentType.ASSIGNMENT2_PART1C;
        } else if (p == 2 && p2 == 4) {
            return KadenzeAssignmentType.ASSIGNMENT2_PART1D;
        } else if (p == 2 && p2 == 5) {
            return KadenzeAssignmentType.ASSIGNMENT2_PART2;
        } else if (p == 2 && p2 == 6) {
            return KadenzeAssignmentType.ASSIGNMENT2_PART3A;
        } else if (p == 2 && p2 == 7) {
            return KadenzeAssignmentType.ASSIGNMENT2_PART3B;
        } else if (p == 3 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT3_PART1A;
        } else if (p == 3 && p2 == 2) {
            return KadenzeAssignmentType.ASSIGNMENT3_PART1B;
        } else if (p == 3 && p2 == 3) {
            return KadenzeAssignmentType.ASSIGNMENT3_PART1C;
        } else if (p == 3 && p2 == 4) {
            return KadenzeAssignmentType.ASSIGNMENT3_PART2;
        } else if (p == 3 && p2 == 5) {
            return KadenzeAssignmentType.ASSIGNMENT3_PART3A;
        } else if (p == 3 && p2 == 6) {
            return KadenzeAssignmentType.ASSIGNMENT3_PART3B;
        } else if (p == 4 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT4_PART1A;
        } else if (p == 4 && p2 == 2) {
            return KadenzeAssignmentType.ASSIGNMENT4_PART1B;
        } else if (p == 4 && p2 == 3) {
            return KadenzeAssignmentType.ASSIGNMENT4_PART1C;
        } else if (p == 4 && p2 == 4) {
            return KadenzeAssignmentType.ASSIGNMENT4_PART2;
        } else if (p == 6 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT6_PART1;
        } else if (p == 6 && p2 == 2) {
            return KadenzeAssignmentType.ASSIGNMENT6_PART2;
        } else if (p == 7) {
            return KadenzeAssignmentType.ASSIGNMENT7;
        } else {
            logger.log(Level.WARNING, "NO ASSIGNMENT FOUND FOR " + p + "," + p2);
            return KadenzeAssignmentType.NONE;
        }
    }

    public enum KadenzeAssignmentType {

        NONE,
        ASSIGNMENT1,
        ASSIGNMENT2_PART1A,
        ASSIGNMENT2_PART1B,
        ASSIGNMENT2_PART1C,
        ASSIGNMENT2_PART1D,
        ASSIGNMENT2_PART2,
        ASSIGNMENT2_PART3A,
        ASSIGNMENT2_PART3B,
        ASSIGNMENT3_PART1A,
        ASSIGNMENT3_PART1B,
        ASSIGNMENT3_PART1C,
        ASSIGNMENT3_PART2,
        ASSIGNMENT3_PART3A,
        ASSIGNMENT3_PART3B,
        ASSIGNMENT4_PART1A,
        ASSIGNMENT4_PART1B,
        ASSIGNMENT4_PART1C,
        ASSIGNMENT4_PART2,
        ASSIGNMENT6_PART1,
        ASSIGNMENT6_PART2,
        ASSIGNMENT7,
        FEATURNATOR_STUDY_1
    }

    public static int getAssignmentNumber(KadenzeAssignmentType t) {
        if (null == t) {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return -1;
        } else switch (t) {
            case NONE:
                return -1;
            case ASSIGNMENT1:
                return 1;
            case ASSIGNMENT2_PART1A:
                return 2;
            case ASSIGNMENT2_PART1B:
                return 2;
            case ASSIGNMENT2_PART1C:
                return 2;
            case ASSIGNMENT2_PART1D:
                return 2;
            case ASSIGNMENT2_PART2:
                return 2;
            case ASSIGNMENT2_PART3A:
                return 2;
            case ASSIGNMENT2_PART3B:
                return 2;
            case ASSIGNMENT3_PART1A:
                return 3;
            case ASSIGNMENT3_PART1B:
                return 3;
            case ASSIGNMENT3_PART1C:
                return 3;
            case ASSIGNMENT3_PART2:
                return 3;
            case ASSIGNMENT3_PART3A:
                return 3;
            case ASSIGNMENT3_PART3B:
                return 3;
            case ASSIGNMENT4_PART1A:
                return 4;
            case ASSIGNMENT4_PART1B:
                return 4;
            case ASSIGNMENT4_PART1C:
                return 4;
            case ASSIGNMENT4_PART2:
                return 4;
            case ASSIGNMENT6_PART1:
                return 6;
            case ASSIGNMENT6_PART2:
                return 6;
            case ASSIGNMENT7:
                return 7;
            case FEATURNATOR_STUDY_1:
                return 8;
            default:
                System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
                return -1;
        }
    }

    public static int getAssignmentSubPart(KadenzeAssignmentType t) {
        if (null == t) {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return -1;
        } else switch (t) {
            case NONE:
                return -1;
            case ASSIGNMENT1:
                return -1;
            case ASSIGNMENT2_PART1A:
                return 1;
            case ASSIGNMENT2_PART1B:
                return 2;
            case ASSIGNMENT2_PART1C:
                return 3;
            case ASSIGNMENT2_PART1D:
                return 4;
            case ASSIGNMENT2_PART2:
                return 5;
            case ASSIGNMENT2_PART3A:
                return 6;
            case ASSIGNMENT2_PART3B:
                return 7;
            case ASSIGNMENT3_PART1A:
                return 1;
            case ASSIGNMENT3_PART1B:
                return 2;
            case ASSIGNMENT3_PART1C:
                return 3;
            case ASSIGNMENT3_PART2:
                return 4;
            case ASSIGNMENT3_PART3A:
                return 5;
            case ASSIGNMENT3_PART3B:
                return 6;
            case ASSIGNMENT4_PART1A:
                return 1;
            case ASSIGNMENT4_PART1B:
                return 2;
            case ASSIGNMENT4_PART1C:
                return 3;
            case ASSIGNMENT4_PART2:
                return 4;
            case ASSIGNMENT6_PART1:
                return 1;
            case ASSIGNMENT6_PART2:
                return 2;
            case ASSIGNMENT7:
                return 1;
            case FEATURNATOR_STUDY_1:
                return 1;
            default:
                System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
                return -1;
        }
    }

    public static String getAssignmentLogfilename(KadenzeAssignmentType t) {
        if (null == t) {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "tmp.txt";
        } else switch (t) {
            case NONE:
                return "tmp.txt";
            case ASSIGNMENT1:
                return "assignment1.txt";
            case ASSIGNMENT2_PART1A:
                return "assignment2_1A.txt";
            case ASSIGNMENT2_PART1B:
                return "assignment2_1B.txt";
            case ASSIGNMENT2_PART1C:
                return "assignment2_1C.txt";
            case ASSIGNMENT2_PART1D:
                return "assignment2_1D.txt";
            case ASSIGNMENT2_PART2:
                return "assignment2_2.txt";
            case ASSIGNMENT2_PART3A:
                return "assignment2_3A.txt";
            case ASSIGNMENT2_PART3B:
                return "assignment2_3B.txt";
            case ASSIGNMENT3_PART1A:
                return "assignment3_1A.txt";
            case ASSIGNMENT3_PART1B:
                return "assignment3_1B.txt";
            case ASSIGNMENT3_PART1C:
                return "assignment3_1C.txt";
            case ASSIGNMENT3_PART2:
                return "assignment3_2.txt";
            case ASSIGNMENT3_PART3A:
                return "assignment3_3A.txt";
            case ASSIGNMENT3_PART3B:
                return "assignment3_3B.txt";
            case ASSIGNMENT4_PART1A:
                return "assignment4_1A.txt";
            case ASSIGNMENT4_PART1B:
                return "assignment4_1B.txt";
            case ASSIGNMENT4_PART1C:
                return "assignment4_1C.txt";
            case ASSIGNMENT4_PART2:
                return "assignment4_2.txt";
            case ASSIGNMENT6_PART1:
                return "assignment6_1.txt";
            case ASSIGNMENT6_PART2:
                return "assignment6_2.txt";
            case ASSIGNMENT7:
                return "assignment7_1.txt";
            case FEATURNATOR_STUDY_1:
                return "featurnator_study_1.txt";
            default:
                System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
                return "tmp.txt";
        }
    }

    public static String getReadableName(KadenzeAssignmentType t) {
        if (null == t) {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "";
        } else switch (t) {
            case NONE:
                return "No assignment";
            case ASSIGNMENT1:
                return "Assignment 1, Part 1";
            case ASSIGNMENT2_PART1A:
                return "Assignment 2, Part 1A";
            case ASSIGNMENT2_PART1B:
                return "Assignment 2, Part 1B";
            case ASSIGNMENT2_PART1C:
                return "Assignment 2, Part 1C";
            case ASSIGNMENT2_PART1D:
                return "Assignment 2, Part 1D";
            case ASSIGNMENT2_PART2:
                return "Assignment 2, Part 2";
            case ASSIGNMENT2_PART3A:
                return "Assignment 2, Part 3A";
            case ASSIGNMENT2_PART3B:
                return "Assignment 2, Part 3B";
            case ASSIGNMENT3_PART1A:
                return "Assignment 3, Part 1A";
            case ASSIGNMENT3_PART1B:
                return "Assignment 3, Part 1B";
            case ASSIGNMENT3_PART1C:
                return "Assignment 3, Part 1C";
            case ASSIGNMENT3_PART2:
                return "Assignment 3, Part 2";
            case ASSIGNMENT3_PART3A:
                return "Assignment 3, Part 3A";
            case ASSIGNMENT3_PART3B:
                return "Assignment 3, Part 3B";
            case ASSIGNMENT4_PART1A:
                return "Assignment 4, Part 1A";
            case ASSIGNMENT4_PART1B:
                return "Assignment 4, Part 1B";
            case ASSIGNMENT4_PART1C:
                return "Assignment 4, Part 1C";
            case ASSIGNMENT4_PART2:
                return "Assignment 4, Part 2";
            case ASSIGNMENT6_PART1:
                return "Assignment 6, Part 1";
            case ASSIGNMENT6_PART2:
                return "Assignment 6, Part 2";
            case ASSIGNMENT7:
                return "Assignment 7";
            case FEATURNATOR_STUDY_1:
                return "Study 1";
            default:
                System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
                return "";
        }
    }

    public static String getLogDirectory(KadenzeAssignmentType t) {
        if (null == t) {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "";
        } else switch (t) {
            case NONE:
                return "";
            case ASSIGNMENT1:
                return "assignment1";
            case ASSIGNMENT2_PART1A:
                return "assignment2" + File.separator + "assignment2_1A";
            case ASSIGNMENT2_PART1B:
                return "assignment2" + File.separator + "assignment2_1B";
            case ASSIGNMENT2_PART1C:
                return "assignment2" + File.separator + "assignment2_1C";
            case ASSIGNMENT2_PART1D:
                return "assignment2" + File.separator + "assignment2_1D";
            case ASSIGNMENT2_PART2:
                return "assignment2" + File.separator + "assignment2_2";
            case ASSIGNMENT2_PART3A:
                return "assignment2" + File.separator + "assignment2_3A";
            case ASSIGNMENT2_PART3B:
                return "assignment2" + File.separator + "assignment2_3B";
            case ASSIGNMENT3_PART1A:
                return "assignment3" + File.separator + "assignment3_1A";
            case ASSIGNMENT3_PART1B:
                return "assignment3" + File.separator + "assignment3_1B";
            case ASSIGNMENT3_PART1C:
                return "assignment3" + File.separator + "assignment3_1C";
            case ASSIGNMENT3_PART2:
                return "assignment3" + File.separator + "assignment3_2";
            case ASSIGNMENT3_PART3A:
                return "assignment3" + File.separator + "assignment3_3A";
            case ASSIGNMENT3_PART3B:
                return "assignment3" + File.separator + "assignment3_3B";
            case ASSIGNMENT4_PART1A:
                return "assignment4" + File.separator + "assignment4_1A";
            case ASSIGNMENT4_PART1B:
                return "assignment4" + File.separator + "assignment4_1B";
            case ASSIGNMENT4_PART1C:
                return "assignment4" + File.separator + "assignment4_1C";
            case ASSIGNMENT4_PART2:
                return "assignment4" + File.separator + "assignment4_2";
            case ASSIGNMENT6_PART1:
                return "assignment6" + File.separator + "assignment6_1";
            case ASSIGNMENT6_PART2:
                return "assignment6" + File.separator + "assignment6_2";
            case ASSIGNMENT7:
                return "assignment7" + File.separator + "assignment7";
            case FEATURNATOR_STUDY_1:
                return "featurnator_study_1";
            default:
                System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
                return "";
        }
    }

    public static KadenzeLogger getLoggerForAssignmentType(KadenzeAssignmentType t) throws Exception {
        if (null == t) {
            throw new Exception("No logger for this assignment!");
        } else switch (t) {
            case NONE:
                return new NoLogger();
            case ASSIGNMENT1:
                return new Assignment12Logger();
            case ASSIGNMENT2_PART1A:
                return new Assignment12Logger();
            case ASSIGNMENT2_PART1B:
                return new Assignment12Logger();
            case ASSIGNMENT2_PART1C:
                return new Assignment12Logger();
            case ASSIGNMENT2_PART1D:
                return new Assignment12Logger();
            case ASSIGNMENT2_PART2:
                return new Assignment12Logger();
            case ASSIGNMENT2_PART3A:
                return new Assignment12Logger();
            case ASSIGNMENT2_PART3B:
                return new Assignment12Logger();
            case ASSIGNMENT3_PART1A:
                return new Assignment12Logger();
            case ASSIGNMENT3_PART1B:
                return new Assignment12Logger();
            case ASSIGNMENT3_PART1C:
                return new Assignment12Logger();
            case ASSIGNMENT3_PART2:
                return new Assignment12Logger();
            case ASSIGNMENT3_PART3A:
                return new Assignment12Logger();
            case ASSIGNMENT3_PART3B:
                return new Assignment12Logger();
            case ASSIGNMENT4_PART1A:
                return new Assignment12Logger();
            case ASSIGNMENT4_PART1B:
                return new Assignment12Logger();
            case ASSIGNMENT4_PART1C:
                return new Assignment12Logger();
            case ASSIGNMENT4_PART2:
                return new Assignment12Logger();
            case ASSIGNMENT6_PART1:
                return new AssignmentFinalLogger();
            case ASSIGNMENT6_PART2:
                return new AssignmentFinalLogger();
            case ASSIGNMENT7:
                return new AssignmentFinalLogger();
            case FEATURNATOR_STUDY_1:
                return new FeaturnatorLogger();
            default:
                throw new Exception("No logger for this assignment!");
        }
    }
}
