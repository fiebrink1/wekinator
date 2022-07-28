/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.StringWriter;
import java.io.PrintWriter;

import wekimini.gui.MainGUI;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

    private static final String versionString = "v2.1.1.2_19Jan2022";
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

    /**
     * Load it and start running, handle old project
     *
     * @param oldWekinator The already-running instance of Wekinator, or null.
     * @param filename Path to the .wekproj file to load, relative or absolute.
     * @param options Option specifying what to do with any currently-running project.
     */
    public void runNewProjectAutomatically(Wekinator oldWekinator, String filename, NewProjectOptions options) throws IOException {
        File f = new File(filename);
        int newestID = nextID;
        Wekinator w = WekiMiniRunner.getInstance().runFromFile(f.getAbsolutePath(), /*showOSCWindow=*/false);

        if (options == NewProjectOptions.STOPCURRENTLISTENING ||
                options == NewProjectOptions.CLOSECURRENT) {
            if (oldWekinator != null) {
                oldWekinator.getOSCReceiver().stopListening();
            }
        }
        
        //Start OSC listening for newest one
        w.getOSCReceiver().startListening();
        
        //Start running newest one
        if (w.getLearningManager().getLearningType() == LearningManager.LearningType.SUPERVISED_LEARNING) {
            WekinatorSupervisedLearningController supervisedController = w.getLearningManager().getSupervisedLearningManager().getSupervisedLearningController();
            if (supervisedController.canRun()) {
                supervisedController.startRun();
            } else {
                w.getStatusUpdateCenter().warn(this, "Tried to run automatically but cannot in this state.");
            }
        } else {
            WekinatorDtwLearningController dtwController = w.getLearningManager().getDtwLearningManager().getDtwLearningController();
            if (dtwController.canRun()) {
                dtwController.startRun();
            } else {
                w.getStatusUpdateCenter().warn(this, "Tried to run automatically but cannot in this state.");
            }
        } 
        w.getMainGUI().setPerformanceMode(true);
        
        if (options == NewProjectOptions.CLOSECURRENT) {
            if (oldWekinator != null) {
                oldWekinator.getMainGUI().dispose();
                oldWekinator.close(); //Do I do this or something else?
            }
            
        }
    }
    
    public enum NewProjectOptions {CLOSECURRENT, STOPCURRENTLISTENING, KEEPCURRENTRUNNING};
    
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

    private static CommandLine parseArgs(final String[] args) {
        Options options = new Options();
        Option project = Option.builder("p")
            .longOpt("project")
            .hasArg(true)
            .desc("project file to load in performance mode")
            .build();
        options.addOption(project);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("wekinator", options);
            System.exit(1);
        }
        return cmd;
    }

    public static void main(final String[] args) {
        final CommandLine cmd = parseArgs(args);

        /* Create and display the form */
        //WekiMiniRunner.isKadenze = (args.length != 0);
        WekiMiniRunner.isKadenze = false; //KADENZE SET
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             UIManager.put("Slider.paintValue", false);
             //UIManager.put("Slider.thumbHeight", 5);

        } catch (Exception ex) {
            Logger.getLogger(WekiMiniRunner.class.getName()).log(Level.WARNING, null, ex);
        }
        
        aboutBox.setKadenze(isKadenze);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (WekiMiniRunner.isKadenze) {
                     new KadenzePromptFrame().setVisible(true);
                     return;
                }
                KadenzeLogging.noLogging();
                if (!cmd.hasOption("p")) {
                    WekiMiniRunner.getInstance().runNewProject();
                    return;
                }
                final String projectPath = cmd.getOptionValue("project");
                logger.log(Level.INFO, "Opening project from " + projectPath);
                try {
                    WekiMiniRunner.getInstance()
                        .runNewProjectAutomatically(/*oldWekinator=*/null,
                                                    projectPath,
                                                    NewProjectOptions.CLOSECURRENT);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    System.out.println(e);
                    System.out.println(sw.toString());
                    logger.log(Level.SEVERE, "Error opening project");
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

    /**
     * @param fileLocation Path to the .wekproj file to load.
     */
    public Wekinator runFromFile(String fileLocation, boolean showOSCWindow) throws IOException {
        Wekinator w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        MainGUI mg = w.getMainGUI();
        mg.setVisible(true);
        if (showOSCWindow) {
            mg.showOSCReceiverWindow();
        }
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
        return w;
    }

    public void registerForMacOSXEvents() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Wekinator");
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
