/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author rebecca
 */
public class KadenzeUtils {

    private static final Logger logger = Logger.getLogger(KadenzeUtils.class.getName());

    //Modified from http://stackoverflow.com/questions/1844688/read-all-files-in-a-folder
    public static void listFilesForFolder(final File folder, List<File> files) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, files);
            } else {
                files.add(fileEntry);
            }
        }
    }

    //From http://www.avajava.com/tutorials/lessons/how-can-i-create-a-zip-file-from-a-set-of-files.html
    public static void addToZipFile(File directoryToZip, String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

        //System.out.println("Writing '" + fileName + "' to zip file");
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        //ZipEntry zipEntry = new ZipEntry(fileName);
        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                file.getCanonicalPath().length());
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    //Adapted from http://www.avajava.com/tutorials/lessons/how-do-i-unzip-the-contents-of-a-zip-file.html
    public static void unzip(File intoFolder, ZipFile zipFile) throws FileNotFoundException, IOException {
        Enumeration<?> enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();

            String name = zipEntry.getName();
            String nameFixed = name.replaceAll("\\\\", "/");
            //long size = zipEntry.getSize();
            //long compressedSize = zipEntry.getCompressedSize();
            ///System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n",
            //        name, size, compressedSize);

            File file = new File(intoFolder, nameFixed);
            if (nameFixed.endsWith("/")) {
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();

        }
        zipFile.close();
    }

    public static long getLineTimestamp(String line) throws IOException {
        int index = line.indexOf(",");
        if (index != -1) {
            try {
                Long l = Long.parseLong(line.substring(0, index));
                return l;
            } catch (NumberFormatException ex) {
                throw new IOException("Improperly formatted log file", ex);
            }
        } else {
            throw new IOException("Improperly formatted log file");
        }
    }

    public static String getLineType(String line) {
        //All lines are formatted timestamp,wekID,lineType
        String[] parts = line.split(",", 4);
        /*if (parts.length < 3) {
         throw new IOException("Improperly formatted log file");
         } else {
         return parts[2];
         } */ //Let's be more forgiving, e.g. if log has quit mid-line
        if (parts.length >= 3) {
            return parts[2];
        } else {
            logger.log(Level.WARNING, "Line is not properly formatted: {0}", line);
            return "NONE";
        }
    }

    //Counts starting from 1
    public static String getNthField(int n, String line) throws IOException {
        //All lines are formatted timestamp,wekID,lineType
        String[] parts = line.split(",", n + 1);
        if (parts.length < n) {
            throw new IOException("Improperly formatted log file");
        } else {
            return parts[n - 1];
        }
    }

    public static String getLineAfterRegex(String line, String pattern) throws IOException {
        int i = line.indexOf(pattern);
        if (i == -1) {
            throw new IOException("Improperly formatted log file");
        }
        int numChars = pattern.length();
        return line.substring(i + numChars);

    }

    public static void main(String[] args) {
        String s1 = "abcdefg1";
        String s2 = "1";
        try {
            System.out.println(getLineAfterRegex(s1, s2));
        } catch (Exception ex) {
            Logger.getLogger(KadenzeUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static String getModelStringFromLine(String line) throws IOException {
        String type = KadenzeUtils.getLineType(line);
        if (type.equals("MODEL_BUILDER_UPDATED")) {
            String s1 = KadenzeUtils.getLineAfterRegex(line, "MODEL_BUILDER_UPDATED,");
            return s1.substring(s1.indexOf(",") + 1);
        } else if (type.equals("MODEL_EDITED_NEW")) {
            //Called when model edited using GUI
            String s1 = KadenzeUtils.getLineAfterRegex(line, "MODEL_EDITED_NEW,");
            return s1.substring(s1.indexOf(",") + 1);
        } else {
            logger.log(Level.WARNING, "Not a valid model line");
            return "";
        }

    }

    static boolean isModelStringClassifier(String modelString) {
        //     int
        //       String modelName = modelString.substring(0, modelString.indexOf(","));
        return modelString.startsWith("KNN") || modelString.startsWith("J48") || modelString.startsWith("SVM")
                || modelString.startsWith("DECISIONSTUMP") || modelString.startsWith("ADABOOST")
                || modelString.startsWith("NAIVEBAYES");
    }

    static boolean isModelStringRegression(String modelString) {
        return modelString.startsWith("LINPOLREG") || modelString.startsWith("NEURALNET"); //TODO TEST THIS
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    public static String formatDouble(double d) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return formatter.format(d);
    }

    static double[] getInputsFromRunLine(String line, int numInputs) {
        double[] vals = new double[numInputs];
        String[] fields = line.split(",");
        for (int i = 0; i < numInputs; i++) {
            try {
                vals[i] = Double.parseDouble(fields[i + 5]);
            } catch (NumberFormatException ex) {
                vals[i] = 0;
            }
        }
        return vals;
    }

    static String prettifyJSON(String toJSONString) {
        return toJSONString.replaceAll("\\{", "\n\\{\n").replaceAll(",", ",\n").replaceAll("\\}", "\n\\}");
    }

    static String formatStringList(ArrayList<String> s) {
        if (s.size() == 0) {
            return "";
        } else if (s.size() == 1) {
            return s.get(0);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.size() - 1; i++) {
            sb.append(s.get(i)).append(", ");
        }
        sb.append(s.get(s.size() - 1));
        return sb.toString();
    }

    /** Returns the last log timestamp present, or ?? if an error occurs. */
    public static String getLastLogTimestamp(String dir, String fileName) {
        File f = new File(dir, fileName);
        if (!f.exists()) {
            logger.log(Level.WARNING, "Cannot read log for file {0} in directory + {1}", new Object[]{fileName, dir});
            return "??";
        }
        File f2 = new File(f, fileName + ".txt");
        if (!f2.exists()) {
            logger.log(Level.WARNING, "Cannot read log for file {0} in directory + {1}", new Object[]{fileName + ".txt", f});
            return "??";
        }
        
        FileReader r = null;
        boolean isRunning = false;
        BufferedReader b = null;

        try {
            try {
                r = new FileReader(f2);
            } catch (FileNotFoundException ex) {
                logger.log(Level.WARNING, "Cannot read log for file {0} in directory + {1}", new Object[]{fileName, dir});
                return "??";
            }
            b = new BufferedReader(r);
            String line;
            String lastLine = "";
            while ((line = b.readLine()) != null) {
                lastLine = line;
            }
            long ts = getLineTimestamp(lastLine);
            Date d = new Date(ts);
            DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
            return df.format(d);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return "??";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return "??";
        }

    }

    public static String getAssignmentSummaryString(String dir, int whichAssignment) {
        if (whichAssignment == 2) {
            String[] parts = {"Part 1A", "Part 1B", "Part 1C", "Part 1D", "Part 2", "Part 3A", "Part 3B"};
            String[] files = {"assignment2_1A", "assignment2_1B", "assignment2_1C", "assignment2_1D", "assignment2_2", "assignment2_3A", "assignment2_3B"};
            return getAssignmentSummaryString(dir, parts, files);
        } else if (whichAssignment == 3) {            
            String[] parts = {"Part 1A", "Part 1B", "Part 1C", "Part 2", "Part 3A", "Part 3B"};
            String[] files = {"assignment3_1A", "assignment3_1B", "assignment3_1C", "assignment3_2", "assignment3_3A", "assignment3_3B"};
            return getAssignmentSummaryString(dir, parts, files);
        } else if (whichAssignment == 4) {
            String[] parts = {"Part 1A", "Part 1B", "Part 1C", "Part 2"};
            String[] files = {"assignment4_1A", "assignment4_1B", "assignment4_1C", "assignment4_2"};
            return getAssignmentSummaryString(dir, parts, files);
        } else if (whichAssignment == 6) {
            String[] parts = {"Part 1", "Part 2"};
            String[] files = {"assignment6_1", "assignment6_2"};
            return getAssignmentSummaryString(dir, parts, files);
        } else {
            return "Cannot produce summary for this assignment";
        }
         
    }
    
    private static String getAssignmentSummaryString(String dir, String[] parts, String[] files) {
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 0; i < parts.length; i++) {
            sb.append(getPartSummaryString(parts[i], files[i], dir)).append("<br>");
        }
        sb.append("</html>");
        return sb.toString();
    }
    
    private static String getPartSummaryString(String partName, String fileName, String dir) {
        if (logExists(fileName, dir)) {
            return partName + ": Last modified " + KadenzeUtils.getLastLogTimestamp(dir, fileName);
        } else {
            return partName + ": Missing";
        }
    }
    
    
    private static boolean logExists(String partName, String parentDir) {
        String part1adir = parentDir + File.separator + partName + File.separator + partName + ".txt";
        File f = new File(part1adir);
        return f.exists() && f.canRead();
    }

}
