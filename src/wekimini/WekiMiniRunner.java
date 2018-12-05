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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import wekimini.gui.Study1Prompt;
import wekimini.gui.Study2Prompt;
import wekimini.kadenze.FeaturnatorLogger;
import wekimini.kadenze.KadenzeAssignment;
import wekimini.kadenze.KadenzeLogging;
import wekimini.kadenze.KadenzePromptFrame;
import wekimini.learning.LearningAlgorithmRegistry;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCInputGroup;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public final class WekiMiniRunner {

    private static final String versionString = "v2.1.1.0a_10Jan2017";
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
    private static final boolean IS_STUDY_1 = false;
    private static final boolean IS_STUDY_2 = false;
    
    //Load it and start running, handle old project
    public void runNewProjectAutomatically(Wekinator oldWekinator, String filename, NewProjectOptions options) throws Exception {
        File f = new File(filename);
        int newestID = nextID;
        Wekinator w = WekiMiniRunner.getInstance().runFromFile(f.getAbsolutePath(), false);

        if (options == NewProjectOptions.STOPCURRENTLISTENING ||
                options == NewProjectOptions.CLOSECURRENT) {
            if (oldWekinator != null) {
                oldWekinator.getInputManager().stopListening();
            }
        }
        
        //Start OSC listening for newest one
        w.getInputManager().startListening();
        
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

    public static void main(String[] args) {
        WekiMiniRunner.isKadenze = false; //KADENZE SET
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             UIManager.put("Slider.paintValue", false);

        } catch (Exception ex) {
            Logger.getLogger(WekiMiniRunner.class.getName()).log(Level.WARNING, null, ex);
        }
        
        aboutBox.setKadenze(isKadenze);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if(IS_STUDY_1)
                {
                    new Study1Prompt().setVisible(true);
                }
                else if(IS_STUDY_2)
                {
                    new Study2Prompt().setVisible(true);
                }
                else
                {
                    if (WekiMiniRunner.isKadenze) {
                        new KadenzePromptFrame().setVisible(true);
                    } else {
                        KadenzeLogging.noLogging();
                        WekiMiniRunner.getInstance().runNewProject();
                    }
                }

            }
        });
    }

    public int numRunningProjects() {
        return wekinatorCurrentMainFrames.size();
    }
    
    public Wekinator runStudy2(String currentSaveLocation)
    {
        Wekinator w = null;
        try {
            String dir = currentSaveLocation;
            File f = new File(dir);
            w = new Wekinator(WekiMiniRunner.generateNextID());
            String[] inputs = new String[]{"accX","accY","accZ","gyroX","gyroY","gyroZ","emg1","emg2","emg3","emg4","emg5","emg6","emg7","emg8",};
            int numClasses = inputs.length;
            int numOutputs = 1;
            String name = "Inputs";
            String inputMessage = "/myo1";
            OSCInputGroup inputGroup = new OSCInputGroup(name, inputMessage, numClasses, inputs);
            List<OSCOutput> outputs = new LinkedList<>();
            for (int i = 0; i < 1; i++) {
                OSCClassificationOutput o = new OSCClassificationOutput("output"+i, numClasses, false);
                outputs.add(o);
            }
            String outputMessage = "/wek/outputs";
            OSCOutputGroup outputGroup = new OSCOutputGroup(outputs, outputMessage, "127.0.0.1", 12000);
            
            w.getInputManager().setOSCInputGroup(inputGroup);
            w.getInputManager().startListening();
            w.getOutputManager().setOSCOutputGroup(outputGroup);
            w.getLearningManager().setSupervisedLearning();
            
            LearningModelBuilder mb = LearningAlgorithmRegistry.getClassificationModelBuilders()[0];
            for (int i = 0; i < outputGroup.getNumOutputs(); i++) 
            {
                LearningModelBuilder mbnew = mb.fromTemplate(mb);
                logger.log(Level.INFO, "Setting model builder to" + mbnew.getPrettyName());
                w.getSupervisedLearningManager().setModelBuilderForPath(mbnew, i);
            }
                        
            WekinatorSaver.createNewProject("Week6", f, w);
            w.setHasSaveLocation(true);
            w.setProjectLocation(dir);
            w.setProjectName("Week6");
            
            w.addCloseListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    logger.log(Level.INFO, "Wekinator project closed");
                    if (wekinatorCurrentMainFrames.size() == 1) 
                    {
                        handleClosingLast();
                    } 
                    wekinatorCurrentMainFrames.remove((Wekinator) e.getSource());
                }
            });
            
        } catch(IOException e)
        {
            
        }
        return w;
    }
    
    public Wekinator runStudy1(String userID, String hostName, int port)
    {
        Wekinator w = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date date = new Date();
            String dir = ((FeaturnatorLogger)KadenzeLogging.getLogger()).getUserDir() + "/ProjectFiles_" + userID + "_Study1_" + dateFormat.format(date);
            File f = new File(dir);
            w = new Wekinator(WekiMiniRunner.generateNextID());
            w.getOSCSender().setHostnameAndPort(InetAddress.getByName(hostName), port);
            String[] inputs = new String[]{"accX","accY","accZ","gyroX","gyroY","gyroZ"};
            int numClasses = inputs.length;
            int numOutputs = 1;
            String name = "Inputs";
            String inputMessage = "/wek/inputs";
            OSCInputGroup inputGroup = new OSCInputGroup(name, inputMessage, 6, inputs);
            List<OSCOutput> outputs = new LinkedList<>();
            for (int i = 0; i < 1; i++) {
                OSCClassificationOutput o = new OSCClassificationOutput("output"+i, numClasses, false);
                outputs.add(o);
            }
            String outputMessage = "/wek/outputs";
            OSCOutputGroup outputGroup = new OSCOutputGroup(outputs, outputMessage, hostName, port);
            
            w.getInputManager().setOSCInputGroup(inputGroup);
            w.getOutputManager().setOSCOutputGroup(outputGroup);
            w.getLearningManager().setSupervisedLearning();
            
            LearningModelBuilder mb = LearningAlgorithmRegistry.getClassificationModelBuilders()[0];
            for (int i = 0; i < outputGroup.getNumOutputs(); i++) 
            {
                LearningModelBuilder mbnew = mb.fromTemplate(mb);
                logger.log(Level.INFO, "Setting model builder to" + mbnew.getPrettyName());
                w.getSupervisedLearningManager().setModelBuilderForPath(mbnew, i);
            }
                        
            WekinatorSaver.createNewProject("Study1", f, w);
            w.setHasSaveLocation(true);
            w.setProjectLocation(dir);
            w.setProjectName("Study1");
            
            w.addCloseListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    logger.log(Level.INFO, "Wekinator project closed");
                    if (wekinatorCurrentMainFrames.size() == 1) 
                    {
                        handleClosingLast();
                    } 
                    wekinatorCurrentMainFrames.remove((Wekinator) e.getSource());
                }
            });
            
        } catch(IOException e)
        {
            
        }
        return w;
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

    public Wekinator runFromFile(String fileLocation, boolean showOSCWindow) throws Exception {
        System.out.println("running from file");
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
