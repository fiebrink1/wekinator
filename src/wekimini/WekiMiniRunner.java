/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private static final List<Wekinator> runningWekinators = new LinkedList<>();
    private static WekiMiniRunner ref = null; //Singleton
    private HashMap<Wekinator, Closeable> wekinatorCurrentMainFrames = new HashMap<>();

    
    private boolean isInitialState = true;
   // private List<Closeable> wekinatorCurrentMainFrames = new LinkedList<>();

    public static WekiMiniRunner getInstance() {
        if (ref == null) {
            ref = new WekiMiniRunner();
        }
        return ref;
    }
    
    public WekiMiniRunner() {
        registerForMacOSXEvents();
    }
    
    public void transferControl(Wekinator w, Closeable oldC, Closeable newC) {
        /*if (wekinatorCurrentMainFrames.containsValue(oldC)) {
            wekinatorCurrentMainFrames.remove(oldC);

        }
        wekinatorCurrentMainFrames.add(newC);
                    if (wekinatorCurrentMainFrames.size() == 1) {
                                    wekinatorCurrentMainFrames.get(0).setCloseable(false);
            }*/
        wekinatorCurrentMainFrames.put(w, newC);
    }
    
    public static void main(String[] args) {
        //WelcomeScreen
                 /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
       /* try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WekiMiniRunner.getInstance().runNewProject();
            }
        });  
    }
    
    public int numRunningProjects() {
        return runningWekinators.size();
    }
    
    public void runNewProject() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                try {
                    Wekinator w = new Wekinator();
                    InitInputOutputFrame f = new InitInputOutputFrame(w);
                    f.setVisible(true);
                    f.addWindowListener(new WindowAdapter() {
                        
                        @Override
                        public void windowClosed(WindowEvent e) {
                            if (wekinatorCurrentMainFrames.containsValue(e.getComponent())) {
                                System.out.println("Found it");
                               // Wekinator wek = ((Closeable)e.getComponent()).getWekinator();
                               // wekinatorCurrentMainFrames.remove(wek);
                                
                               //  wekinatorCurrentMainFrames.remove(e.getComponent());
                              //  if (wekinatorCurrentMainFrames.size() == 1) {
                              //      wekinatorCurrentMainFrames.get(0).setCloseable(false);
                               // }
                            } else {
                                System.out.println("Danger: component not found");
                            }
                            System.out.println("CLOSEEVENT");
                        }
                });
                    if (runningWekinators.size() == 0) {
                        f.setCloseable(false);
                    } else {
                        f.setCloseable(true);
                        for (Closeable c : wekinatorCurrentMainFrames.values()) {
                            c.setCloseable(true);
                        }
                    }
                    
                    runningWekinators.add(w);
                    wekinatorCurrentMainFrames.put(w, f);
                    w.addCloseListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {
                            runningWekinators.remove(w);
                            logger.log(Level.INFO, "Wekinator project closed");
                            if (runningWekinators.size() == 0) {
                                System.out.println("Making last one noncloseable");
                                //Problem: size of this can be different from size of runningWekinators!
                                //wekinator might be closed before we get windowclose event.
                                wekinatorCurrentMainFrames.get(w).setCloseable(false);
                            } else {
                                System.out.println("Not on the last one");
                            }
                        }
                    });
                    
                } catch (IOException | SecurityException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        });
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

    // General quit handler; fed to the OSXAdapter as the method to call when a system quit event occurs
    // A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
    public boolean quit() {
        int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            exit();
        }
        return (option == JOptionPane.YES_OPTION);
    }
    
    private void exit() {
        //This is where we save logs, shutdown any OSC if needed, etc.
        
        System.exit(0);
    }
    
    public interface Closeable {
        public void setCloseable(boolean b);
        
        public Wekinator getWekinator();
    }
    
    
}


