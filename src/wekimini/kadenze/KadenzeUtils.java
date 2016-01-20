/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author rebecca
 */
public class KadenzeUtils {

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
            //long size = zipEntry.getSize();
            //long compressedSize = zipEntry.getCompressedSize();
            ///System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n",
            //        name, size, compressedSize);

            File file = new File(intoFolder, name);
            if (name.endsWith(File.separator)) {
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
    
    public static long getLineTimestamp(String line) throws Exception {
        int index = line.indexOf(",");
        if (index != -1) {
            try {
                Long l = Long.parseLong(line.substring(0, index));
                return l;
            } catch (NumberFormatException ex) {
                throw new Exception("Improperly formatted log file");
            }
        } else {
            throw new Exception("Improperly formatted log file");
        }
    }
    
    public static String getLineType(String line) throws Exception {
        //All lines are formatted timestamp,wekID,lineType
        String[] parts = line.split(",",4);
        if (parts.length < 3) {
            throw new Exception("Improperly formatted log file");
        } else {
            return parts[2];
        }
    }
    
        
    public static String getNthField(int n, String line) throws Exception {
        //All lines are formatted timestamp,wekID,lineType
        String[] parts = line.split(",",n+1);
        if (parts.length < n) {
            throw new Exception("Improperly formatted log file");
        } else {
            return parts[n-1];
        }
    }
    
}
