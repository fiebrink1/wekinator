/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.gui.NewProjectSettingsFrame;
import wekimini.gui.OSCInputStatusFrame;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class MainGUI extends javax.swing.JFrame {
    //private GUIAddNewInput addNewInputGUI;
    private boolean isDisplayingReceiverWindow = false;
    private boolean isDisplayingAddInput = false;
    private OSCInputStatusFrame oscInputStatusFrame = null;
    private Wekinator w;
    
   /* public void displayEditInput(String name) {
        //Only show 1 of these at once
        if (! isDisplayingAddInput) {
            isDisplayingAddInput = true;
            GUIAddEditInput g = new GUIAddEditInput(w, name);
            g.setAlwaysOnTop(true);
            g.setVisible(true);
            g.addWindowListener(new WindowListener() {

                @Override
                public void windowOpened(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                        isDisplayingAddInput = false;
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
            });
        }
    }
    
    public void displayAddInput() {
        //Only show 1 of these at once
        if (! isDisplayingAddInput) {
            isDisplayingAddInput = true;
            GUIAddEditInput g = new GUIAddEditInput(w);
            g.setAlwaysOnTop(true);
            g.setVisible(true);
            g.addWindowListener(new WindowListener() {

                @Override
                public void windowOpened(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                        isDisplayingAddInput = false;
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
            });
        }
    }

        public void displayAddOutput() {
        //Only show 1 of these at once
        if (! isDisplayingAddOutput) {
            isDisplayingAddOutput = true;
            GUIAddEditOutputGroup g = new GUIAddEditOutputGroup(w);
            g.setAlwaysOnTop(true);
            g.setVisible(true);
            g.addWindowListener(new WindowListener() {

                @Override
                public void windowOpened(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                        isDisplayingAddOutput = false;
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
            });
        }
    }
    
    */
    
    /**
     * Creates new form MainGUI
     */
    public MainGUI(Wekinator w) {
        initComponents();
        this.w = w;
        setGUIForWekinator();
        
    }

    private void setGUIForWekinator() {
        this.setTitle(w.getProjectName());
        w.addPropertyChangeListener(this::wekinatorPropertyChanged);
      //  w.getStatusUpdateCenter().addPropertyChangeListener(this::statusUpdated);
    }
    
   /* private void statusUpdated(PropertyChangeEvent evt) {
        StatusUpdateCenter.StatusUpdate u = (StatusUpdateCenter.StatusUpdate)evt.getNewValue();
        
    }*/
    
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

        jPanel1 = new javax.swing.JPanel();
        learningPanel1 = new wekimini.gui.LearningPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        menuItemSave = new javax.swing.JMenuItem();
        menuItemSaveAs = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("New project");
        setMaximumSize(new java.awt.Dimension(817, 2147483647));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(learningPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(learningPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
        );

        menuFile.setMnemonic('F');
        menuFile.setText("File");

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
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText("Outputs");
        jMenu2.add(jMenuItem2);

        jMenuItem3.setText("Logging console");
        jMenuItem3.setEnabled(false);
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Actions");
        jMenu3.setEnabled(false);
        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        try {
            //TODO: Check this isn't same wekinator as mine! (don't load from my same place, or from something already open...)
            Wekinator w = WekinatorSaver.loadWekinatorFromFile(f.getAbsolutePath());
            w.getMainGUI().setVisible(true);
        } catch (Exception ex) {
            
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        showOSCReceiverWindow();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void showOSCReceiverWindow() {        
        if (oscInputStatusFrame == null) {
            oscInputStatusFrame = new OSCInputStatusFrame(w);
            oscInputStatusFrame.setVisible(true);
            
            Util.callOnClosed(oscInputStatusFrame, (Callable) () -> {
                System.out.println("WOAH HERE");
                oscInputStatusFrame = null;
                return null;
            }); 
        } else {
            oscInputStatusFrame.toFront();
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
                    new MainGUI(w).setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }); 
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private wekimini.gui.LearningPanel learningPanel1;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemSave;
    private javax.swing.JMenuItem menuItemSaveAs;
    // End of variables declaration//GEN-END:variables

    void displayEditOutput(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void showExamplesViewer() {
       //String s = w.getDataManager().toString();
       //System.out.println(s);
       w.getDataManager().showViewer();
    }

    public void initializeInputsAndOutputs() {
        Path[] paths = w.getLearningManager().getPaths().toArray(new Path[0]);
        String[] modelNames = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            modelNames[i] = paths[i].getCurrentModelName();
        }
        learningPanel1.setup(w, paths, modelNames);
    }
}