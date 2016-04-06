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
        } else if (p == 5 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT5_PART1A;
        } else if (p == 5 && p2 == 2) {
            return KadenzeAssignmentType.ASSIGNMENT5_PART1B;
        } else if (p == 5 && p2 == 3) {
            return KadenzeAssignmentType.ASSIGNMENT5_PART1C;
        } else if (p == 5 && p2 == 4) {
            return KadenzeAssignmentType.ASSIGNMENT5_PART2;
        } else if (p == 5 && p2 == 5) {
            return KadenzeAssignmentType.ASSIGNMENT5_PART3A;
        } else if (p == 5 && p2 == 6) {
            return KadenzeAssignmentType.ASSIGNMENT5_PART3B;
        } else if (p == 7 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT7_PART1A;
        } else if (p == 7 && p2 == 2) {
            return KadenzeAssignmentType.ASSIGNMENT7_PART1B;
        } else if (p == 7 && p2 == 3) {
            return KadenzeAssignmentType.ASSIGNMENT7_PART1C;
        } else if (p == 7 && p2 == 4) {
            return KadenzeAssignmentType.ASSIGNMENT7_PART2;
        } else if (p == 11 && p2 == 1) {
            return KadenzeAssignmentType.ASSIGNMENT11_PART1;
        } else if (p == 11 && p2 == 2) {
            return KadenzeAssignmentType.ASSIGNMENT11_PART2;
        } else if (p == 12) {
            return KadenzeAssignmentType.ASSIGNMENT12;
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
        ASSIGNMENT5_PART1A,
        ASSIGNMENT5_PART1B,
        ASSIGNMENT5_PART1C,
        ASSIGNMENT5_PART2,
        ASSIGNMENT5_PART3A,
        ASSIGNMENT5_PART3B,
        ASSIGNMENT7_PART1A,
        ASSIGNMENT7_PART1B,
        ASSIGNMENT7_PART1C,
        ASSIGNMENT7_PART2,
        ASSIGNMENT11_PART1,
        ASSIGNMENT11_PART2,
        ASSIGNMENT12
    }

    public static int getAssignmentNumber(KadenzeAssignmentType t) {
        if (t == KadenzeAssignmentType.NONE) {
            return -1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return 1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1A) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1B) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1C) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1D) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3A) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3B) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1A) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1B) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1C) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART2) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3A) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3B) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1A) {
            return 7;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1B) {
            return 7;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1C) {
            return 7;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART2) {
            return 7;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART1) {
            return 11;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART2) {
            return 11;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT12) {
            return 12;
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
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1A) {
            return 1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1B) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1C) {
            return 3;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1D) {
            return 4;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3A) {
            return 6;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3B) {
            return 7;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1A) {
            return 1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1B) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1C) {
            return 3;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART2) {
            return 4;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3A) {
            return 5;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3B) {
            return 6;
        }  else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1A) {
            return 1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1B) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1C) {
            return 3;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART2) {
            return 4;
        }else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART1) {
            return 1;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART2) {
            return 2;
        } else if (t == KadenzeAssignmentType.ASSIGNMENT12) {
            return 1;
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
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1A) {
            return "assignment2_1A.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1B) {
            return "assignment2_1B.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1C) {
            return "assignment2_1C.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1D) {
            return "assignment2_1D.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return "assignment2_2.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3A) {
            return "assignment2_3A.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3B) {
            return "assignment2_3B.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1A) {
            return "assignment5_1A.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1B) {
            return "assignment5_1B.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1C) {
            return "assignment5_1C.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART2) {
            return "assignment5_2.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3A) {
            return "assignment5_3A.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3B) {
            return "assignment5_3B.txt";
        }  else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1A) {
            return "assignment7_1A.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1B) {
            return "assignment7_1B.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1C) {
            return "assignment7_1C.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART2) {
            return "assignment7_2.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART1) {
            return "assignment11_1.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART2) {
            return "assignment11_2.txt";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT12) {
            return "assignment12_1.txt";
        } else {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "tmp.txt";
        }
    }

    public static String getReadableName(KadenzeAssignmentType t) {
        if (t == KadenzeAssignmentType.NONE) {
            return "No assignment";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return "Assignment 1, Part 1";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1A) {
            return "Assignment 2, Part 1A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1B) {
            return "Assignment 2, Part 1B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1C) {
            return "Assignment 2, Part 1C";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1D) {
            return "Assignment 2, Part 1D";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return "Assignment 2, Part 2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3A) {
            return "Assignment 2, Part 3A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3B) {
            return "Assignment 2, Part 3B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1A) {
            return "Assignment 5, Part 1A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1B) {
            return "Assignment 5, Part 1B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1C) {
            return "Assignment 5, Part 1C";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART2) {
            return "Assignment 5, Part 2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3A) {
            return "Assignment 5, Part 3A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3B) {
            return "Assignment 5, Part 3B";
        }  else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1A) {
            return "Assignment 7, Part 1A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1B) {
            return "Assignment 7, Part 1B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1C) {
            return "Assignment 7, Part 1C";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART2) {
            return "Assignment 7, Part 2";
        }  else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART1) {
            return "Assignment 11, Part 1";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART2) {
            return "Assignment 11, Part 2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT12) {
            return "Assignment 12";
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
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1A) {
            return "assignment2" + File.separator + "assignment2_1A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1B) {
            return "assignment2" + File.separator + "assignment2_1B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1C) {
            return "assignment2" + File.separator + "assignment2_1C";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1D) {
            return "assignment2" + File.separator + "assignment2_1D";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return "assignment2" + File.separator + "assignment2_2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3A) {
            return "assignment2" + File.separator + "assignment2_3A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3B) {
            return "assignment2" + File.separator + "assignment2_3B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1A) {
            return "assignment5" + File.separator + "assignment5_1A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1B) {
            return "assignment5" + File.separator + "assignment5_1B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1C) {
            return "assignment5" + File.separator + "assignment5_1C";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART2) {
            return "assignment5" + File.separator + "assignment5_2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3A) {
            return "assignment5" + File.separator + "assignment5_3A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3B) {
            return "assignment5" + File.separator + "assignment5_3B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1A) {
            return "assignment7" + File.separator + "assignment7_1A";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1B) {
            return "assignment7" + File.separator + "assignment7_1B";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1C) {
            return "assignment7" + File.separator + "assignment7_1C";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART2) {
            return "assignment7" + File.separator + "assignment7_2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART1) {
            return "assignment11" + File.separator + "assignment11_1";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART2) {
            return "assignment11" + File.separator + "assignment11_2";
        } else if (t == KadenzeAssignmentType.ASSIGNMENT12) {
            return "assignment12" + File.separator + "assignment12";
        } else {
            System.out.println("ERROR NO ASSIGNMENT NUMBER FOUND");
            return "";
        }
    }

    public static KadenzeLogger getLoggerForAssignmentType(KadenzeAssignmentType t) throws Exception {
        if (t == KadenzeAssignmentType.NONE) {
            return new NoLogger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT1) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1A) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1B) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1C) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART1D) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART2) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3A) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT2_PART3B) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1A) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1B) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART1C) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART2) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3A) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT5_PART3B) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1A) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1B) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART1C) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT7_PART2) {
            return new Assignment12Logger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART1) {
            return new AssignmentFinalLogger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT11_PART2) {
            return new AssignmentFinalLogger();
        } else if (t == KadenzeAssignmentType.ASSIGNMENT12) {
            return new AssignmentFinalLogger();
        } else {
            throw new Exception("No logger for this assignment!");
        }
    }
}
