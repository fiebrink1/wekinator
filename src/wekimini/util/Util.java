/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
    
    public static int showPrettyOptionPane(Component caller, String msg, String title) {
        Object[] options = { "OK", "Cancel" };
        return JOptionPane.showOptionDialog(caller,
                "<html><body><p style='width: 200px;'>" + msg + "</p></body></html>",
                title,
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
    }
    
    public static int showPrettyYesNoPane(Component caller, String msg, String title) {
        Object[] options = { "Yes", "No" };
        return JOptionPane.showOptionDialog(caller,
                "<html><body><p style='width: 200px;'>" + msg + "</p></body></html>",
                title,
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
    }
    
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    public static void showPrettyErrorPane(Component caller, String msg) {
        JOptionPane.showMessageDialog(caller,
                "<html><body><p style='width: 200px;'>" + msg + "</p></body></html>",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showPrettyInfoPane(Component caller, String msg, String title) {
        Class c = Util.class;
        ImageIcon icon = new ImageIcon(c.getResource("/wekimini/icons/wekimini_small.png"));

        JOptionPane.showMessageDialog(caller,
                "<html><body><p style='width: 200px;'>" + msg + "</p></body></html>",
                title,
                JOptionPane.INFORMATION_MESSAGE, 
                icon);
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

    public static void writeBlankFile(String filename) throws IOException {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
    }

    public static void writeToXMLFile(Object o, String id, Class c, String filename) throws FileNotFoundException, IOException {
        FileOutputStream fos = null;
        try {
            XStream xstream = new XStream();
            xstream.alias(id, c);
            //String xml = xstream.toXML(this);
            //System.out.println(xml);
            fos = new FileOutputStream(filename);
            fos.write("<?xml version=\"1.0\"?>\n".getBytes("UTF-8")); //write XML header, as XStream doesn't do that for us
            if (o != null) {
                xstream.toXML(o, fos);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex2) {
                //Don't care
            }
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex2) {
                //Don't care
            }
            throw ex;
        }
    }

    public static Object readFromXMLFile(String id, Class c, String filename) throws IOException {
        try {
            XStream xstream = new XStream();
            xstream.alias(id, c);
            return xstream.fromXML(new File(filename));
        } catch (XStreamException ex) {
            throw new IOException(ex);
        }
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
        if (textField.getText().trim().contains(" ")) {
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
        String[] s = new String[b - a + 1];
        for (int i = 0; i <= (b - a); i++) {
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

    public static boolean isInteger(double d) {
        return d == Math.floor(d);
    }

    public static File findLoadFile(String ext, String description, String defDir, Component c) {
        /* File defaultFile = null;
         if (defFile != null)
         defaultFile = new File(defFile); */
        // String lastLoc = WekinatorInstance.getWekinatorInstance().getSettings().getLastKeyValue(ext);
        File defaultFile = null;
        File defaultDir = null;
        if (defDir != null) {
            defaultDir = new File(defDir);
        }

        FileChooserWithExtension fc = new FileChooserWithExtension(
                ext,
                description,
                defaultFile,
                defaultDir,
                false);

        File file = null;
        int returnVal = fc.showOpenDialog(c);
        if (returnVal == FileChooserWithExtension.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        return file;
    }

    
    public interface CallableOnClosed {
        public void callMe();
    }
    
    public static void callOnClosed(JFrame f, final CallableOnClosed func) {
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    func.callMe();
                } catch (Exception ex) {
                    Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        /*f.addWindowListener(new WindowListener() {

         @Override
         public void windowOpened(WindowEvent e) {
         }

         @Override
         public void windowClosing(WindowEvent e) {
         }

         @Override
         public void windowClosed(WindowEvent e) {
         try {
         func.call();
         } catch (Exception ex) {
         Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
         }
         }

         @Override
         public void windowIconified(WindowEvent e) {
         }

         @Override
         public void windowDeiconified(WindowEvent e) {
         }

         @Override
         public void windowActivated(WindowEvent e) {
         }

         @Override
         public void windowDeactivated(WindowEvent e) {
         }
         }); */
    }
    
    public static String prettyDecimalFormat(double d, int numPlaces) {
        StringBuilder sb = new StringBuilder("#.");
        for (int i = 0; i < numPlaces; i++) {
            sb.append("#");
        }
        DecimalFormat dFormat = new DecimalFormat(sb.toString());
        return dFormat.format(d);
    }
    
    public static boolean isMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("mac os x");
    }
    
    public static boolean isWindows() {
      return System.getProperty("os.name").startsWith("Windows");   
    }
    
    /*public static String getVersion() {
       // String vFile = getClass().getResource("/wekimini/"
        System.getPrope
    } */
    
   /* public static void writePropertyTest() throws FileNotFoundException, IOException {
        //InputStream pinput = getClass().getResourceAsStream("/wekimini/util/properties.txt");
        Util u = new Util();
        URL p = u.getClass().getResource("/wekimini/util/properties.txt");
        File f = new File(p.getFile());
        FileOutputStream out = new FileOutputStream(f);
        Properties defaultProps = new Properties();
        defaultProps.put("TestProp1", "TestValue");
        defaultProps.store(out, "Comments");
    }/*
   /* 
     public static void readPropertyTest() throws FileNotFoundException, IOException  {
        //InputStream pinput = getClass().getResourceAsStream("/wekimini/util/properties.txt");
        Util u = new Util();
        URL p = u.getClass().getResource("/wekimini/util/properties.txt");
        File f = new File(p.getFile());
         
       Properties defaultProps = new Properties();
       FileInputStream in = new FileInputStream(f);
       defaultProps.load(in);
       in.close();
        
         System.out.println("Propreties are:");
         System.out.println(defaultProps.toString());
         
         /*Util u = new Util();
        URL p = u.getClass().getResource("/wekimini/util/properties.txt");
        File f = new File(p.getFile());
        FileOutputStream out = new FileOutputStream(f);
        Properties defaultProps = new Properties();
        defaultProps.put("TestProp1", "TestValue");
        defaultProps.store(out, "Comments"); */
    //} */
        
}
