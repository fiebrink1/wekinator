/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeEvent;
import wekimini.gui.MainGUI;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import wekimini.osc.OSCSender;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import wekimini.LearningManager.LearningType;
import wekimini.gui.Console;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.osc.OSCMonitor;
import wekimini.osc.OSCReceiver;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class Wekinator {

    public static final String version = "v2.1.0.4";

//TODO: Can make more efficient by initializing some of these on demand (e.g. gui, OSC Monitor)
   // private final Settings settings;
    //  private File projectLocation;
    private final OSCReceiver oscReceiver;
    private final OSCSender oscSender;
    private final InputManager inputManager;
    private final OutputManager outputManager;
    private MainGUI mainGUI;
    private final int id;
    
    //private final SupervisedLearningManager supervisedLearningManager;
    private final DataManager dataManager;
    private final TrainingRunner trainingRunner;
    private final StatusUpdateCenter statusUpdateCenter;
    protected EventListenerList exitListenerList = new EventListenerList();
    private ChangeEvent changeEvent = null;
    private final OSCMonitor oscMonitor;
    private final LoggingManager loggingManager;
    private final WekinatorController wekinatorController;
    private Console myConsole = null;
    private final LearningManager learningManager;

    //private final OSCController oscController;
    private String projectName = "New Project";

    public static final String PROP_PROJECT_NAME = "projectName";

    private String projectLocation = "";

    public static final String PROP_PROJECT_LOCATION = "projectLocation";

    private boolean hasSaveLocation = false;

    public static final String PROP_HAS_SAVE_LOCATION = "hasSaveLocation";

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private static final Logger logger = Logger.getLogger(Wekinator.class.getName());
    
    public int getID() {
        return id;
    }
    
    public void addCloseListener(ChangeListener l) {
        exitListenerList.add(ChangeListener.class, l);
    }

    public void removeCloseListener(ChangeListener l) {
        exitListenerList.remove(ChangeListener.class, l);
    }

    private void fireCloseEvent() {
        Object[] listeners = exitListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    public void close() {
        KadenzeLogging.getLogger().logEvent(this, KadenzeLogger.KEvent.PROJECT_CLOSED);
        prepareToDie();
        fireCloseEvent();
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

   // private final Scheduler scheduler;
    // private final CommunicationManager communicationManager;
  /*  private final SupervisedLearningManager supervisedLearningManager;
     private final GUIManager wekinatorGUIManager;
     private final WekinatorLogger logger;
    
     //State:
     private final WekinatorSettings settings;
     private final Inputs inputs;
     private final Outputs outputs;
     private final Connections connections; // includes learners */

    /*public Scheduler getScheduler() {
     return scheduler;
     } */
    public StatusUpdateCenter getStatusUpdateCenter() {
        return statusUpdateCenter;
    }

    public TrainingRunner getTrainingRunner() {
        return trainingRunner;
    }

    public SupervisedLearningManager getSupervisedLearningManager() {
        //XXX get rid of all calls to this?
        return learningManager.getSupervisedLearningManager();
    }

    public DtwLearningManager getDtwLearningManager() {
        return learningManager.getDtwLearningManager();
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public OutputManager getOutputManager() {
        return outputManager;
    }

    public MainGUI getMainGUI() {
        return mainGUI;
    }
    
    
    public OSCMonitor getOSCMonitor() {
        return oscMonitor;
    }

    //private final OSCSender oscSender;
    // private final File projectDirectory; 

    /*public Wekinator(File projectDirectory) {
     //TODO: Check directory is valid
        
     this.projectDirectory = projectDirectory;
     this.oscReceiver = new OSCReceiver(); 
     }  */
    public OSCReceiver getOSCReceiver() {
        return oscReceiver;
    }

    public OSCSender getOSCSender() {
        return oscSender;
    }

    public WekinatorController getWekinatorController() {
        return wekinatorController;
    }

    public static String getDefaultDirectory() {
        return System.getProperty("user.home");
    }

    public static String getDefaultNextProjectName() {
        String projectDefault = "WekinatorProject";
        String homeDir = getDefaultDirectory();
        File f1 = new File(homeDir + File.separator + projectDefault);
        int numTries = 0;
        while (f1.exists() && numTries < 1000) {
            numTries++;
            f1 = new File(homeDir + File.separator + projectDefault + numTries);
        }
        if (numTries == 0) {
            return projectDefault;
        } else {
            return (projectDefault + numTries);
        }
    }

    //Use only for testing
    public static Wekinator TestingWekinator() throws IOException {
        return new Wekinator(WekiMiniRunner.generateNextID());
    }

    public LearningManager getLearningManager() {
        return learningManager;
    }

    public LoggingManager getLoggingManager() {
        return loggingManager;
    }

    public Wekinator(int id) throws IOException {
        this.id = id;
        loggingManager = new LoggingManager(this);
        loggingManager.startLoggingToFile();

        statusUpdateCenter = new StatusUpdateCenter(this);

        oscReceiver = new OSCReceiver();
        oscSender = new OSCSender();
        oscSender.setDefaultHostAndPort();
        //mainSupervisedGUI = new MainGUI(this);

        inputManager = new InputManager(this);
        outputManager = new OutputManager(this);
        dataManager = new DataManager(this);
        trainingRunner = new TrainingRunner(this);
        learningManager = new LearningManager(this);
        //supervisedLearningManager = new SupervisedLearningManager(this);
        wekinatorController = new WekinatorController(this);
        oscMonitor = new OSCMonitor(oscReceiver, inputManager, oscSender);
        //oscController = new OSCController(this);

        //scheduler = new Scheduler(this);
        //  communicationManager = new CommunicationManager();
        learningManager.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(LearningManager.PROP_LEARNINGTYPE)) {
                    learningTypeChanged((LearningManager.LearningType) evt.getNewValue());
                }
            }
        });
       // KadenzeLogging.getLogger().newProjectStarted(this);
    }

    private void learningTypeChanged(LearningManager.LearningType learningType) {
        if (learningType == LearningManager.LearningType.SUPERVISED_LEARNING) {
            mainGUI = new MainGUI(this, LearningType.SUPERVISED_LEARNING);
        } else if (learningType == LearningManager.LearningType.TEMPORAL_MODELING) {
            mainGUI = new MainGUI(this, LearningType.TEMPORAL_MODELING);
        }
    }

    /*  private void createProjectFiles(File f) throws SecurityException {
     projectLocation.mkdirs();
     String inputsName = f.getAbsolutePath() + File.separator + "inputs";
     new File(inputsName).mkdirs();
     String outputsName = f.getAbsolutePath() + File.separator + "outputs";
     new File(outputsName).mkdirs();
     String currentLearners = f.getAbsolutePath() + File.separator + "current";
     new File(currentLearners).mkdirs();
     String data = currentLearners + File.separator + "data";
     new File(data).mkdirs();
     String models = currentLearners + File.separator + "models";
     new File(models).mkdirs();
     String stash = f.getAbsolutePath() + File.separator + "stash";
     new File(stash).mkdirs();
     String logs = f.getAbsolutePath() + File.separator + "logs";
     new File(logs).mkdirs();
     } */
    public final void registerForMacOSXEvents() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

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
            logger.log(Level.WARNING, "Error while loading the OSXAdapter:", e);
            e.printStackTrace();
        }
        //}
    }

    // General info dialog; fed to the OSXAdapter as the method to call when
    // "About OSXAdapter" is selected from the application menu

    public void about() {
       // aboutBox.setLocation((int) this.getLocation().getX() + 22, (int) this.getLocation().getY() + 22);
        // aboutBox.setVisible(true);
    }

    // General preferences dialog; fed to the OSXAdapter as the method to call when
    // "Preferences..." is selected from the application menu
    public void preferences() {
        // System.out.println("HI WEKINATOR");
        //TODO: Some preferences!
        //prefs.setLocation((int) this.getLocation().getX() + 22, (int) this.getLocation().getY() + 22);
        //prefs.setVisible(true);
    }

    // General quit handler; fed to the OSXAdapter as the method to call when a system quit event occurs
    // A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
    /*public boolean quit() {
     // int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
     // if (option == JOptionPane.YES_OPTION) {
     //     exit();
     // }
     // return (option == JOptionPane.YES_OPTION);
     return true;
     } */
    /**
     * Get the value of projectName
     *
     * @return the value of projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the value of projectName
     *
     * @param projectName new value of projectName
     */
    public void setProjectName(String projectName) {
        String oldProjectName = this.projectName;
        this.projectName = projectName;
        propertyChangeSupport.firePropertyChange(PROP_PROJECT_NAME, oldProjectName, projectName);
    }

    /**
     * Get the value of projectLocation
     *
     * @return the value of projectLocation
     */
    public String getProjectLocation() {
        return projectLocation;
    }

    /**
     * Set the value of projectLocation i.e., the folder in which the .wekproj
     * file is currently saved
     *
     * @param projectLocation new value of projectLocation
     */
    public void setProjectLocation(String projectLocation) {
        String oldProjectLocation = this.projectLocation;
        this.projectLocation = projectLocation;
        propertyChangeSupport.firePropertyChange(PROP_PROJECT_LOCATION, oldProjectLocation, projectLocation);
    }

    /**
     * Get the value of hasSaveLocation
     *
     * @return the value of hasSaveLocation
     */
    public boolean hasSaveLocation() {
        return hasSaveLocation;
    }

    public void setHasSaveLocation(boolean hasSaveLocation) {
        boolean oldHasSaveLocation = this.hasSaveLocation;
        this.hasSaveLocation = hasSaveLocation;
        propertyChangeSupport.firePropertyChange(PROP_HAS_SAVE_LOCATION, oldHasSaveLocation, hasSaveLocation);
    }

    public void saveAs(String name, File projectDir) throws IOException {
        String oldName = getProjectName();
        String oldLocation = getProjectLocation();
        try {
            setProjectName(name);
            setProjectLocation(projectDir.getAbsolutePath());
            WekinatorSaver.createNewProject(name, projectDir, this);
        } catch (IOException ex) {
            setProjectName(oldName);
            setProjectLocation(oldLocation);
            throw ex;
        }

        setHasSaveLocation(true);
    }

    public void save() {
        try {
            WekinatorSaver.saveExistingProject(this);
            setHasSaveLocation(true);

        } catch (IOException ex) {
            Util.showPrettyWarningPromptPane(null, "Error encountered in saving file: " + ex.getLocalizedMessage());
            logger.log(Level.SEVERE, "Could not save Wekinator file:{0}", ex.getMessage());
        }
    }

    public void prepareToDie() {
        logger.log(Level.INFO, "Preparing to die");
        oscReceiver.stopListening();
        loggingManager.prepareToDie(); //Problem: getting here with X but not with close handler
    }

    public void showConsole() {
        if (myConsole == null) {
            myConsole = new Console(this);
        }
        myConsole.setVisible(true);
        myConsole.toFront();
        Util.callOnClosed(myConsole, new Util.CallableOnClosed() {

            @Override
            public void callMe() {
                myConsole = null;
            }
        });
    }

}
