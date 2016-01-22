/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import wekimini.DtwLearningManager;
import wekimini.LearningManager;
import wekimini.Path;
import wekimini.WekiMiniRunner.Closeable;
import wekimini.gui.path.PathEditorFrame;
import wekimini.util.Util;
import wekimini.WekiMiniRunner;
import wekimini.Wekinator;
import wekimini.WekinatorFileData;
import wekimini.dtw.gui.DtwEditorFrame;
import wekimini.dtw.gui.DtwLearningPanel;
import wekimini.kadenze.KadenzeAssignment;
import wekimini.kadenze.KadenzeAssignment.KadenzeAssignmentType;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.learning.dtw.DtwModel;

/**
 *
 * @author rebecca
 */
public class MainGUI extends javax.swing.JFrame implements Closeable {

    private OSCInputStatusFrame oscInputStatusFrame = null;
    private InputMonitor inputMonitorFrame = null;
    private OutputViewerTable outputTableWindow = null;
    private ModelEvaluationFrame modelEvaluationFrame = null;
    private InputOutputConnectionsEditor inputOutputConnectionsWindow = null;
    private final Wekinator w;
    private boolean closeable = true; //flaseif this is the last window open
    
    private JMenuItem[] kadenzeMenuItems = new JMenuItem[0];
    
    /**
     * Creates new form MainGUI
     */
    public MainGUI(Wekinator w, LearningManager.LearningType type) {
        initComponents();
        if (type == LearningManager.LearningType.INITIALIZATION) {
            throw new IllegalStateException("GUI can only be created for Wekinator whose learning type is known");
        }
        
        this.w = w;
        setGUIForWekinator(type);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
                int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to close this project?", "Close project?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, WekiMiniRunner.getIcon());

                if (option == JOptionPane.YES_OPTION) {
                    finishUp();
                }
                
            }
        });
    }

    private void finishUp() {        
        KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.PROJECT_CLOSED);
        KadenzeLogging.getLogger().flush(); //Imperfect: Log won't be closed if this is last GUI and we haven't submitted yet...
        w.close();
        this.dispose();
    }

    private void setGUIForWekinator(LearningManager.LearningType type) {
        this.setTitle(w.getProjectName());
        menuItemSave.setEnabled(w.hasSaveLocation());
        w.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                wekinatorPropertyChanged(evt);
            }
        });
        
        if (type == LearningManager.LearningType.SUPERVISED_LEARNING) {
            initializeForSupervisedLearning();
        } else if (type == LearningManager.LearningType.TEMPORAL_MODELING) {
            initializeForTemporalModeling();
        }
        
        initKadenzeMenu();
        
    }

    private void initKadenzeMenu() {
        menuKadenze.setVisible(WekiMiniRunner.isKadenze());
        if (! WekiMiniRunner.isKadenze()) {
            return;
        }
        
        //Add sub-menus here
        addKadenzeMenus();
        updateKadenzeMenus();
        
        //Add listeners
        KadenzeLogging.addListener(new KadenzeLogging.KadenzeListener() {

            //This is called when part changes as well
            @Override
            public void assignmentChanged(KadenzeAssignmentType ka) {
                  updateKadenzeMenus();
            }

            @Override
            public void assignmentStarted(KadenzeAssignmentType ka) {
            }

            @Override
            public void assignmentStopped() {
                updateKadenzeMenus();
            }
        });        
    }

    private void addKadenzeMenus() {
        KadenzeAssignmentType ka = KadenzeLogging.getCurrentAssignmentType();
        if (ka == KadenzeAssignmentType.ASSIGNMENT1) {
            //Don't need any sub-menus
            kadenzeMenuItems = new JMenuItem[2];
            JMenuItem k1 = new JMenuItem("Doing Assignment 1, Part 1A");
            k1.setEnabled(false);
            menuKadenze.add(k1);
            kadenzeMenuItems[0] = k1;
            
            JMenuItem k2 = new JMenuItem("Create Kadenze Assignment 1 submission");
            k2.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    createAssignmentSubmission();
                }
            });
            menuKadenze.add(k2);
            kadenzeMenuItems[1] = k2;
        } else {
            System.out.println("NOT IMPLEMENTED YET");
        }
    }
    
    //Called when part changed or assignment stopped
    private void updateKadenzeMenus() {
        KadenzeAssignmentType ka = KadenzeLogging.getCurrentAssignmentType();
        if (ka == KadenzeAssignmentType.ASSIGNMENT1) {
            if (KadenzeLogging.isCurrentlyLogging()) {
                kadenzeMenuItems[1].setEnabled(true);
            } else {
                kadenzeMenuItems[1].setEnabled(false);
            }
        } else {
            System.out.println("NOT IMPLEMENTED YET");
        }
    }
    
    private void createAssignmentSubmission() {
        try {
            String zipped = KadenzeLogging.createZipForAssignment();
            Util.showPrettyInfoPane(this, "Your assignment is done! Please submit file " + zipped, "Success!");
        } catch (Exception ex) {
            String dir = KadenzeLogging.getLogger().getCurrentLoggingDirectory();
            Util.showPrettyErrorPane(this, "Could not zip file. Please zip your " + dir + " directory manually.");
        }
    }
    
    private void wekinatorPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == Wekinator.PROP_PROJECT_NAME) {
            this.setTitle(w.getProjectName());
        } else if (evt.getPropertyName() == Wekinator.PROP_HAS_SAVE_LOCATION) {
            menuItemSave.setEnabled(w.hasSaveLocation());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        learningPanel1 = new wekimini.gui.SupervisedLearningPanel();
        panelParent = new javax.swing.JPanel();
        dtwLearningPanel1 = new wekimini.dtw.gui.DtwLearningPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        menuItemSave = new javax.swing.JMenuItem();
        menuItemSaveAs = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        menuItemEvaluation = new javax.swing.JMenuItem();
        menuPerformanceCheck = new javax.swing.JCheckBoxMenuItem();
        menuConsole = new javax.swing.JMenuItem();
        menuActions = new javax.swing.JMenu();
        checkEnableOSCControl = new javax.swing.JCheckBoxMenuItem();
        menuKadenze = new javax.swing.JMenu();
        menuTemp = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("New project");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        panelParent.setLayout(new javax.swing.BoxLayout(panelParent, javax.swing.BoxLayout.LINE_AXIS));
        panelParent.add(dtwLearningPanel1);

        menuFile.setMnemonic('F');
        menuFile.setText("File");

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.META_MASK));
        jMenuItem6.setText("New project");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem6);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        jMenuItem4.setText("Open project...");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem4);

        menuItemSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_MASK));
        menuItemSave.setText("Save");
        menuItemSave.setToolTipText("");
        menuItemSave.setEnabled(false);
        menuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuItemSave);

        menuItemSaveAs.setText("Save project as...");
        menuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(menuItemSaveAs);

        jMenuBar1.add(menuFile);

        jMenu2.setText("View");

        jMenuItem5.setText("OSC receiver status");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuItem1.setText("Inputs");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText("Outputs");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem7.setText("Input/output connection editor");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem7);

        menuItemEvaluation.setText("Model evaluation");
        menuItemEvaluation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemEvaluationActionPerformed(evt);
            }
        });
        jMenu2.add(menuItemEvaluation);

        menuPerformanceCheck.setText("Performance mode view");
        menuPerformanceCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPerformanceCheckActionPerformed(evt);
            }
        });
        jMenu2.add(menuPerformanceCheck);

        menuConsole.setText("Console");
        menuConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuConsoleActionPerformed(evt);
            }
        });
        jMenu2.add(menuConsole);

        jMenuBar1.add(jMenu2);

        menuActions.setText("Actions");

        checkEnableOSCControl.setSelected(true);
        checkEnableOSCControl.setText("Enable OSC control of GUI");
        checkEnableOSCControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkEnableOSCControlActionPerformed(evt);
            }
        });
        menuActions.add(checkEnableOSCControl);

        jMenuBar1.add(menuActions);

        menuKadenze.setText("Kadenze");
        jMenuBar1.add(menuKadenze);

        menuTemp.setText("Temp");

        jMenuItem3.setText("Flush logs");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        menuTemp.add(jMenuItem3);

        jMenuBar1.add(menuTemp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveAsActionPerformed
        new NewProjectSettingsFrame(w).setVisible(true);
    }//GEN-LAST:event_menuItemSaveAsActionPerformed

    private void menuItemSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveActionPerformed
        w.save();
    }//GEN-LAST:event_menuItemSaveActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        String homeDir = System.getProperty("user.home");
        File f = Util.findLoadFile(WekinatorFileData.FILENAME_EXTENSION, "Wekinator file", homeDir, this);
        if (f != null) {
            try {
                //TODO: Check this isn't same wekinator as mine! (don't load from my same place, or from something already open...)
                WekiMiniRunner.getInstance().runFromFile(f.getAbsolutePath());
            } catch (Exception ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        showOSCReceiverWindow();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        showInputMonitorWindow();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        showOutputTable();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed

        WekiMiniRunner.getInstance().runNewProject();
        //TODO: this or main?

    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        showInputOutputConnectionWindow();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void menuPerformanceCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuPerformanceCheckActionPerformed
        learningPanel1.setPerfomanceMode(menuPerformanceCheck.isSelected());
        if (menuPerformanceCheck.isSelected()) {
            this.setSize(225, 225);
            setMaximumSize(new Dimension(255, 255));
        } else {
            this.setSize(getPreferredSize());
            this.setMaximumSize(new Dimension(817, 2147483647));
        }
        //pack();
        //repaint();
    }//GEN-LAST:event_menuPerformanceCheckActionPerformed

    private void checkEnableOSCControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkEnableOSCControlActionPerformed
        w.getWekinatorController().setOscControlEnabled(checkEnableOSCControl.isSelected());
    }//GEN-LAST:event_checkEnableOSCControlActionPerformed

    private void menuConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuConsoleActionPerformed
        w.showConsole();
    }//GEN-LAST:event_menuConsoleActionPerformed

    private void menuItemEvaluationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemEvaluationActionPerformed
        showEvaluationWindow();
    }//GEN-LAST:event_menuItemEvaluationActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        KadenzeLogging.getLogger().flush();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void showEvaluationWindow() {
       if (modelEvaluationFrame == null) {
            modelEvaluationFrame = new ModelEvaluationFrame(w.getOutputManager().getOutputGroup().getOutputNames(), w);;
            modelEvaluationFrame.setVisible(true);

            
            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    modelEvaluationFrame = null;
                }
            };    
            Util.callOnClosed(modelEvaluationFrame, callMe);
        } else {
            modelEvaluationFrame.toFront();
        }
    }
    
    
    public void showOutputTable() {
        if (w.getLearningManager().getLearningType() == LearningManager.LearningType.TEMPORAL_MODELING) {
            Util.showPrettyErrorPane(this, "Not yet implemented for DTW outputs");
            return;
        }
        if (outputTableWindow == null) {
            outputTableWindow = new OutputViewerTable(w);
            outputTableWindow.setVisible(true);

           /* Util.callOnClosed(outputTableWindow, (Callable) () -> {
                outputTableWindow = null;
                return null;
            }); */
            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    outputTableWindow = null;
                }
            };
            Util.callOnClosed(outputTableWindow, callMe);
            
        } else {
            outputTableWindow.toFront();
        }
    }

    public void showOSCReceiverWindow() {
        if (oscInputStatusFrame == null) {
            oscInputStatusFrame = new OSCInputStatusFrame(w);
            oscInputStatusFrame.setVisible(true);

            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    oscInputStatusFrame = null;
                }
            };
            Util.callOnClosed(oscInputStatusFrame, callMe);
        } else {
            oscInputStatusFrame.toFront();
        }
    }

    private void showInputMonitorWindow() {
        if (inputMonitorFrame == null) {
            inputMonitorFrame = new InputMonitor(w);
            inputMonitorFrame.setVisible(true);

            
            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    inputMonitorFrame = null;
                }
            };    
            Util.callOnClosed(inputMonitorFrame, callMe);
        } else {
            inputMonitorFrame.toFront();
        }
    }

    private void showInputOutputConnectionWindow() {
        if (inputOutputConnectionsWindow == null) {
            inputOutputConnectionsWindow = new InputOutputConnectionsEditor(w);
            inputOutputConnectionsWindow.setVisible(true);

            //Problem: Won't call on button-triggered dispose...
            
            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    inputOutputConnectionsWindow = null;
                }
            }; 
            
            Util.callOnClosed(inputOutputConnectionsWindow, callMe);
        } else {
            inputOutputConnectionsWindow.toFront();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
         javax.swing.UIManager.setLookAndFeel(info.getClassName());
         break;
         }
         }
         } catch (ClassNotFoundException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         */

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                try {
                    Wekinator w = Wekinator.TestingWekinator();
                    new MainGUI(w, LearningManager.LearningType.SUPERVISED_LEARNING).setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem checkEnableOSCControl;
    private wekimini.dtw.gui.DtwLearningPanel dtwLearningPanel1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private wekimini.gui.SupervisedLearningPanel learningPanel1;
    private javax.swing.JMenu menuActions;
    private javax.swing.JMenuItem menuConsole;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemEvaluation;
    private javax.swing.JMenuItem menuItemSave;
    private javax.swing.JMenuItem menuItemSaveAs;
    private javax.swing.JMenu menuKadenze;
    private javax.swing.JCheckBoxMenuItem menuPerformanceCheck;
    private javax.swing.JMenu menuTemp;
    private javax.swing.JPanel panelParent;
    // End of variables declaration//GEN-END:variables

    void displayEditOutput(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void showExamplesViewer() {
        //String s = w.getDataManager().toString();
        //System.out.println(s);
        w.getDataManager().showViewer();
    }

    private void initializeForSupervisedLearning() {
        learningPanel1 = new SupervisedLearningPanel();
        Path[] paths = w.getSupervisedLearningManager().getPaths().toArray(new Path[0]);
        String[] modelNames = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            modelNames[i] = paths[i].getCurrentModelName();
        }
        learningPanel1.setup(w, paths, modelNames);
        panelParent.removeAll();
        panelParent.add(learningPanel1);
    }
    
    private void initializeForTemporalModeling() {
        panelParent.removeAll();
        dtwLearningPanel1 = new DtwLearningPanel(w);
        panelParent.add(dtwLearningPanel1);
        menuItemEvaluation.setEnabled(false);
        //dtwLearningPanel1.setup(w);
        revalidate();
        repaint();
    }

    public void showPathEditor(Path p) {
        PathEditorFrame f = PathEditorFrame.getEditorForPath(p, w.getInputManager().getInputNames(), w);
        f.setVisible(true);
        f.toFront();
    }

    @Override
    public void setCloseable(boolean b) {
        this.closeable = b;
    }

    @Override
    public Wekinator getWekinator() {
        return w;
    }

    public void showDtwData(int gestureNum) {
        //XXX
        System.out.println("XXXXXXXXXXXXXXX\n\n");

        w.getDtwLearningManager().getModel().dumpToConsole();
        w.getDtwLearningManager().getModel().getData().dumpExamplesForGesture(gestureNum);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void showDtwExamplesViewer() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //XXX
        System.out.println("XXXXXXXXXXXXXXX\n\n");
        w.getDtwLearningManager().getModel().dumpToConsole();
        w.getDtwLearningManager().getModel().getData().dumpAllExamples();
    }

    public void showDtwEditor(DtwModel model) {
        DtwEditorFrame f = DtwEditorFrame.getEditorForModel(model, w.getInputManager().getInputNames(), w);
        f.setVisible(true);
        f.toFront();
    }
}
