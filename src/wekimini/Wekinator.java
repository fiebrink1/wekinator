/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import wekimini.osc.OSCSender;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import wekimini.osc.OSCReceiver;

/**
 *
 * @author rebecca
 */
public class Wekinator {
    private final Settings settings;
    private File projectLocation;
    private final OSCReceiver oscReceiver;
    private final OSCSender oscSender;
    private final InputManager inputManager;
    private final OutputManager outputManager;
    private final MainGUI mainGUI;
   // private final Scheduler scheduler;
   // private final CommunicationManager communicationManager;
  /*  private final LearningManager learningManager;
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
    
    public InputManager getInputManager() {
        return inputManager;
    }
    
    public OutputManager getOutputManager() {
        return outputManager;
    }
    
    public MainGUI getMainGUI() {
        return mainGUI;
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
        return new Wekinator(Wekinator.getDefaultNextProjectName(), new File(Wekinator.getDefaultDirectory()));
    }
    
    public Wekinator(String projectName, File projectDir) throws IOException, SecurityException {
        registerForMacOSXEvents();
        projectLocation = projectDir;
       // createProjectFiles(projectLocation);
        settings = new Settings(projectName);
        settings.writeToFile(projectLocation);
        oscReceiver = new OSCReceiver();
        oscSender = new OSCSender();
        oscSender.setDefaultHostAndPort();
        mainGUI = new MainGUI(this);
        
        inputManager = new InputManager(this);
        outputManager = new OutputManager(this);
        //scheduler = new Scheduler(this);
      //  communicationManager = new CommunicationManager();
    }
    
    private void createProjectFiles(File f) throws SecurityException {
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
    }
    
    public void registerForMacOSXEvents() {
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
                System.err.println("Error while loading the OSXAdapter:");
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
        System.out.println("HI WEKINATOR");
        //prefs.setLocation((int) this.getLocation().getX() + 22, (int) this.getLocation().getY() + 22);
        //prefs.setVisible(true);
    }

    // General quit handler; fed to the OSXAdapter as the method to call when a system quit event occurs
    // A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
    public boolean quit() {
       // int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
       // if (option == JOptionPane.YES_OPTION) {
       //     exit();
       // }
       // return (option == JOptionPane.YES_OPTION);
        return true;
    }

}
