/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import wekimini.gui.MainGUI;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import wekimini.gui.About;
import wekimini.gui.InitInputOutputFrame;
import wekimini.gui.Preferences;
import wekimini.kadenze.KadenzeLogging;
import wekimini.kadenze.KadenzePromptFrame;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public final class WekiMiniRunner {

    private static final String versionString = "29 February 2016";
    private static final Logger logger = Logger.getLogger(WekiMiniRunner.class.getName());
    // private static final List<Wekinator> runningWekinators = new LinkedList<>();
    private static WekiMiniRunner ref = null; //Singleton
    private HashMap<Wekinator, Closeable> wekinatorCurrentMainFrames = new HashMap<>();
    private final WindowListener wl;
    private final static About aboutBox = new About();
    private final static Preferences preferencesBox = new Preferences();
    private final ImageIcon myIcon = new ImageIcon(getClass().getResource("/wekimini/icons/wekimini_small.png"));
    private static boolean isKadenze = false;
    private static int nextID = 1;
    
    public static int generateNextID() {
        return nextID++;
    }
    
    public static WekiMiniRunner getInstance() {
        if (ref == null) {
            ref = new WekiMiniRunner();
        }
        return ref;
    }

    public static ImageIcon getIcon() {
        return getInstance().myIcon;
    }

    private void loadProperties() throws FileNotFoundException, IOException {
        //See https://docs.oracle.com/javase/tutorial/essential/environment/properties.html

        // create and load default properties
        Properties defaultProps = new Properties();
        FileInputStream in = new FileInputStream("defaultProperties");
        defaultProps.load(in);
        in.close();

        // create application properties with default
        Properties applicationProps = new Properties(defaultProps);

        // now load properties 
        // from last invocation
        in = new FileInputStream("appProperties");
        applicationProps.load(in);
        in.close();
    }

    public WekiMiniRunner() {
        if (Util.isMac()) {
            registerForMacOSXEvents();
        }
        LoggingManager.setupUniversalLog(versionString);

        /*try {
         //loadProperties();
         Util.writePropertyTest();
         } catch (IOException ex) {
         System.out.println("NOT WORKING");
         Logger.getLogger(WekiMiniRunner.class.getName()).log(Level.SEVERE, null, ex);
         }
        
         try {
         Util.readPropertyTest();
         } catch (IOException ex) {
         System.out.println("READ NOT WORKING");
         Logger.getLogger(WekiMiniRunner.class.getName()).log(Level.SEVERE, null, ex);
         } */
        wl = new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                handleWindowClosing(e);
            }
        };
    }

    //TODO: remove unnecessary argument
    public void transferControl(Wekinator w, Closeable oldC, Closeable newC) {
        oldC.removeWindowListener(wl);
        newC.addWindowListener(wl); //Danger: this won't be called if newC is last GUI open :/
        wekinatorCurrentMainFrames.put(w, newC);
    }

    public static void main(String[] args) {
        /* Create and display the form */
        //WekiMiniRunner.isKadenze = (args.length != 0);
        WekiMiniRunner.isKadenze = true; //KADENZE SET
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             UIManager.put("Slider.paintValue", false);
             //UIManager.put("Slider.thumbHeight", 5);

        } catch (Exception ex) {
            Logger.getLogger(WekiMiniRunner.class.getName()).log(Level.WARNING, null, ex);
        }
        
        aboutBox.setKadenze(isKadenze);
        //args.length == 0
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (WekiMiniRunner.isKadenze) {
                     new KadenzePromptFrame().setVisible(true);
                } else {
                    KadenzeLogging.noLogging();
                    WekiMiniRunner.getInstance().runNewProject();
                }
            }
        });
    }

    public int numRunningProjects() {
        return wekinatorCurrentMainFrames.size();
    }

    public void runNewProject() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                try {
                    Wekinator w = new Wekinator(WekiMiniRunner.generateNextID());
                    KadenzeLogging.getLogger().newProjectStarted(w);
                    InitInputOutputFrame f = new InitInputOutputFrame(w);
                    f.setVisible(true);
                    wekinatorCurrentMainFrames.put(w, f);

                    f.addWindowListener(wl);

                    /* if (runningWekinators.size() == 0) {
                     f.setCloseable(false);
                     } else {
                     f.setCloseable(true);
                     for (Closeable c : wekinatorCurrentMainFrames.values()) {
                     c.setCloseable(true);
                     }
                     } */
                    w.addCloseListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {
                            logger.log(Level.INFO, "Wekinator project closed");
                            if (wekinatorCurrentMainFrames.size() == 1) {
                                //It's our last great hope
                                handleClosingLast();
                            } else {
                                //   System.out.println("Wek closed, but not the last one");
                            }
                            wekinatorCurrentMainFrames.remove((Wekinator) e.getSource());
                        }
                    });

                } catch (IOException | SecurityException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void handleWindowClosing(WindowEvent e) {
        if (wekinatorCurrentMainFrames.containsValue(e.getComponent())) {
            /*     System.out.println("Close window event for known wekinator");
             } else {
             System.out.println("Close window event for unknown wekinator");
             } */
            if (wekinatorCurrentMainFrames.size() == 0) {
                quitWithoutPrompt();
            }
        }
    }

    private void handleClosingLast() {
        System.out.println("Closing the last window now");
        System.exit(0); //Too late to go back
    }

    public void runFromFile(String fileLocation) throws Exception {
        Wekinator w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        MainGUI mg = w.getMainGUI();
        mg.setVisible(true);
        mg.showOSCReceiverWindow();
        wekinatorCurrentMainFrames.put(w, mg);
        mg.addWindowListener(wl);
        w.addCloseListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                logger.log(Level.INFO, "Wekinator project closed");
                if (wekinatorCurrentMainFrames.size() == 1) {
                    //It's our last great hope
                    handleClosingLast();
                } else {
                    //   System.out.println("Wek closed, but not the last one");
                }
                wekinatorCurrentMainFrames.remove((Wekinator) e.getSource());
            }
        });

    }

    public void registerForMacOSXEvents() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Wekinator");
        //TODO: Do we want to use flag for this to protect Windows/Linux?
        //if (MAC_OS_X) {
        try {
            // Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
            // use as delegates for various com.apple.eawt.ApplicationListener methods
            OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
            OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
            OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
            //  OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadImageFile", new Class[] { String.class }));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while loading OSXAdapter: {0}", e.getMessage());
            e.printStackTrace();
        }
        //}
    }

    // General info dialog; fed to the OSXAdapter as the method to call when
    // "About OSXAdapter" is selected from the application menu
    public void about() {
        //aboutBox.setLocation((int) this.getLocation().getX() + 22, (int) this.getLocation().getY() + 22);
        aboutBox.setVisible(true);
    }

    // General preferences dialog; fed to the OSXAdapter as the method to call when
    // "Preferences..." is selected from the application menu
    public void preferences() {
        preferencesBox.setVisible(true);
    }

    public boolean quit() {
        return quitNicely();
    }

    // General quit handler; fed to the OSXAdapter as the method to call when a system quit event occurs
    // A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
    public boolean quitNicely() {
        
        int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit Wekinator?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, myIcon);
        if (option == JOptionPane.YES_OPTION) {
            quitWithoutPrompt();
        }
        return (option == JOptionPane.YES_OPTION);
    }

    public void quitWithoutPrompt() {
        //This is where we save logs, shutdown any OSC if needed, etc.
        //Notice that each Wekinator must do its own shutdown of OSC, logging, etc. separately (this is universal shutdown)
        LoggingManager.closeUniversalLogs();
        KadenzeLogging.getLogger().closeLog();
        
        if (!wekinatorCurrentMainFrames.isEmpty()) {
            Wekinator[] stillOpen = wekinatorCurrentMainFrames.keySet().toArray(new Wekinator[0]);
            for (int i = 0; i < stillOpen.length; i++) {
                stillOpen[i].close();
            }

            //This gives error: multiple threads accessing collection concurrently = bad !
           /* for (Wekinator w : wekinatorCurrentMainFrames.keySet()) {
             w.close();
             }  */
        }
       

        System.exit(0);
    }

    public static boolean isKadenze() {
        return isKadenze;
    }

    public interface Closeable {

        public void setCloseable(boolean b);

        public Wekinator getWekinator();

        public void addWindowListener(WindowListener wl);

        public void removeWindowListener(WindowListener wl);
    }

}
