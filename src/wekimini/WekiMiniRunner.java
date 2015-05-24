/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import wekimini.gui.InitInputOutputFrame;

/**
 *
 * @author rebecca
 */
public final class WekiMiniRunner {
    private static final Logger logger = Logger.getLogger(WekiMiniRunner.class.getName());
   // private static final List<Wekinator> runningWekinators = new LinkedList<>();
    private static WekiMiniRunner ref = null; //Singleton
    private HashMap<Wekinator, Closeable> wekinatorCurrentMainFrames = new HashMap<>();

    public static WekiMiniRunner getInstance() {
        if (ref == null) {
            ref = new WekiMiniRunner();
        }
        return ref;
    }
    
    public WekiMiniRunner() {
        registerForMacOSXEvents();
    }
    
    //TODO: remove unnecessary argument
    public void transferControl(Wekinator w, Closeable oldC, Closeable newC) {
        newC.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            handleWindowClosing(e);
                        } 
                });
        wekinatorCurrentMainFrames.put(w, newC);
    }
    
    public static void main(String[] args) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WekiMiniRunner.getInstance().runNewProject();
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
                    Wekinator w = new Wekinator();
                    InitInputOutputFrame f = new InitInputOutputFrame(w);
                    f.setVisible(true);
                    wekinatorCurrentMainFrames.put(w, f);
                    
                    f.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            handleWindowClosing(e);
                        } 
                });
                    
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
                                System.out.println("Wek closed, but not the last one");
                            }
                            wekinatorCurrentMainFrames.remove((Wekinator)e.getSource());
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
                                System.out.println("Close window event for known wekinator");
                            } else {
                                System.out.println("Close window event for unknown wekinator");
                            }
                            if (wekinatorCurrentMainFrames.size() == 0) {
                                quitWithoutPrompt();
                            }
    }
    
    private void handleClosingLast() {
        System.out.println("Closing the last one now");
    }
    
    public void runFromFile(String fileLocation) throws Exception {
        Wekinator w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        w.getMainGUI().setVisible(true);
        w.getMainGUI().showOSCReceiverWindow();
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
                System.err.println("Error while loading the OSXAdapter:");
                logger.log(Level.WARNING, "Error while loading OSXAdapter: {0}", e.getMessage());
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

    public boolean quit() {
        return quitNicely();
    }
    
    // General quit handler; fed to the OSXAdapter as the method to call when a system quit event occurs
    // A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
    public boolean quitNicely() {
        int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            quitWithoutPrompt();
        }
        return (option == JOptionPane.YES_OPTION);
    }
    
    public void quitWithoutPrompt() {
        //This is where we save logs, shutdown any OSC if needed, etc.
        
        System.exit(0);
    }
    
    public interface Closeable {
        public void setCloseable(boolean b);
        public Wekinator getWekinator();
        public void addWindowListener(WindowListener wl);
    }
    
    
}


