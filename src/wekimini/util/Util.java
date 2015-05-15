/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekimini.util;

import com.thoughtworks.xstream.XStream;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author rebecca
 */
public class Util {



    public static String getCanonicalPath(File f) {
            String s;
        try {
            s = f.getCanonicalPath();
        } catch (IOException ex) {
            s = f.getAbsolutePath();
        }
            return s;
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    public static void showPrettyErrorPane(Component caller, String msg) {
        JOptionPane.showMessageDialog(caller, 
              "<html><body><p style='width: 200px;'>" + msg + "</p></body></html>",
              "Error", 
              JOptionPane.ERROR_MESSAGE);
    }
    
    public static int showPrettyWarningPromptPane(Component caller, String msg) {
        Object[] options = {"OK", "Cancel"};
 
        int response = JOptionPane.showOptionDialog(null, 
                 "<html><body><p syle='width: 200px;'>" + msg + "</p></body></html>",
                 "Warning",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        return response;
    }
    
    public static void writeToXMLFile(Object o, String id, Class c, String filename) {
       FileOutputStream fos = null;
        try {
            XStream xstream = new XStream();
            xstream.alias(id, c);
            //String xml = xstream.toXML(this);
            //System.out.println(xml);
            fos = new FileOutputStream(filename);
            fos.write("<?xml version=\"1.0\"?>\n".getBytes("UTF-8")); //write XML header, as XStream doesn't do that for us
            xstream.toXML(o, fos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Object readFromXMLFile(String id, Class c, String filename) throws Exception {
        XStream xstream = new XStream();
        xstream.alias(id, c);
        return xstream.fromXML(new File(filename));
    } 

    public static String toXMLString(Object o, String id, Class c) {
         XStream xstream = new XStream();
         xstream.alias(id, c);
         return xstream.toXML(o);
    }
    
    public static boolean checkIsPositiveNumber(JTextField textField, String name, Component caller) {
        try {
            int i = Integer.parseInt(textField.getText());
            if (i <= 0) {
                Util.showPrettyErrorPane(caller, name + " must be an integer > 0");
                return false;
            }
        } catch (NumberFormatException ex) {
            Util.showPrettyErrorPane(caller, name + " must be an integer > 0");
            return false;
        }
        return true;
    }
    public static boolean checkNotBlank(JTextField textField, String name, Component caller) {
        if (textField.getText().trim().length() == 0) {
            Util.showPrettyErrorPane(caller, name + " cannot be blank");
            return false;
        }
        return true;
    }
    
    public static boolean checkNoSpace(JTextField textField, String name, Component caller) {
        if (textField.getText().trim().length() == 0) {
            Util.showPrettyErrorPane(caller, name + " cannot contain a space");
            return false;
        }
        return true;
    }
    
    public static boolean checkAllUnique(String[] strings) {
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < strings.length; i++) {
            if (set.contains(strings[i])) {
                return false;
            }
            set.add(strings[i]);
        }
        return true; 
    }
    
//For testing:
    public static void main(String[] args) {
        String s[] = new String[3];
        s[0] = "abc";
        s[1] = "as";
        s[2] = "def";
        System.out.println(checkAllUnique(s));
    }

    //Requires a < b
    //String array starts with a and goes up to (including) b
    public static String[] numbersFromAtoBAsStrings(int a, int b) {
        String[] s = new String[b-a + 1];
        for (int i = 0; i <= (b-a); i++) {
            s[i] = Integer.toString(a + i);
        }
        return s;
    }
    
    public static void logWarning(Object o, String msg) {
         Logger.getLogger(o.getClass().getName()).log(Level.WARNING, msg);
    }

    public static File findSaveFile(String ext, String defaultName, String description, Component c) {
        String homeDir = System.getProperty("user.home");
        File defaultFile = new File(homeDir + File.separator + defaultName + "." + ext);

        FileChooserWithExtension fc = new FileChooserWithExtension(
                ext,
                description,
                defaultFile,
                null,
                true);

        File file = null;
        int returnVal = fc.showSaveDialog(c);
        if (returnVal == FileChooserWithExtension.APPROVE_OPTION) {
            file = fc.getSelectedFile();
           // fc.getCu
        }
        return file;

     }
    
}
