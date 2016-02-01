/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package wekimini.gui;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import wekimini.SupervisedLearningManager;
import wekimini.OutputManager;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.osc.OSCOutput;

/**
 *
 * @author fiebrink
 */
public class SupervisedLearningSetGUI extends javax.swing.JPanel {
    private Wekinator w;
    private List<Path> paths;
    private List<LearningRow> pathPanels;
    private boolean recordAll = true;
    private boolean runAll = true;
    private final ImageIcon recordIconOn = new ImageIcon(getClass().getResource("/wekimini/icons/record1.png"));
    private final ImageIcon recordIconOff = new ImageIcon(getClass().getResource("/wekimini/icons/norec3.png"));
    private final ImageIcon playIconOn = new ImageIcon(getClass().getResource("/wekimini/icons/play1.png"));
    private final ImageIcon playIconOff = new ImageIcon(getClass().getResource("/wekimini/icons/noplay1.png"));
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture scheduledFuture;
    private static final Logger logger = Logger.getLogger(SupervisedLearningSetGUI.class.getName());
    /**
     * Creates new form LearningSet1
     */
    public SupervisedLearningSetGUI() {
        initComponents();
      //  jButton3.setVisible(false);
    }
    
    public final void setup(Wekinator w, Path[] ps, String[] modelNames) {
        this.w = w;
        paths = new LinkedList<>();
        pathPanels = new LinkedList<>();
        paths.addAll(Arrays.asList(ps));
        addPathsToGUI();
        w.getOutputManager().addOutputGroupComputedListener(new OutputManager.OutputValueListener() {

            @Override
            public void update(double[] vals) {
                outputValuesComputed(vals);
            }
        });
        w.getOutputManager().addOutputValueReceivedListener(new OutputManager.OutputValueListener() {

            @Override
            public void update(double[] vals) {
                outputValuesReceived(vals);
            }
        });
        
       /* w.getOutputManager().addIndividualOutputEditListener(new OutputManager.OutputTypeEditListener() {

            @Override
            public void outputTypeEdited(OSCOutput newOutput, OSCOutput oldOutput, int which) {
                outputTypeChanged(newOutput, oldOutput, which);
            }
        }); */
        
        w.getSupervisedLearningManager().addPathEditedListener(new SupervisedLearningManager.PathOutputTypeEditedListener() {

            @Override
            public void pathOutputTypeEdited(int which, Path newPath, Path oldPath) {
                changePath(which, newPath, oldPath);
            }
        });
        
    
        
        //setupThread(); //Was test for lower GUI update rate, didn't make too much difference
            //Also interfered with user setting of GUI
    }
    
    
    private void changePath(int which, Path newPath, Path oldPath) {
        double[] currentValues = w.getOutputManager().getCurrentValues();
        LearningRow r = new SupervisedLearningRow(w, newPath);
        r.setValue(currentValues[which]);
        
        //Replace in my panel array
        pathPanels.remove(which);
        pathPanels.add(which, r);
        
       // pathsPanel.c
        pathsPanel.remove(2*which+1); //added in sequence (separator, row)
        pathsPanel.add(r.getComponent(), 2*which+1);
        
         /*   pathsPanel.add(sep);
            pathsPanel.add(r.getComponent()); */

        pathsPanel.revalidate();
        scrollPathsPanel.validate();
        repaint();  
    }
    
    public SupervisedLearningSetGUI(Wekinator w, Path[] ps, String[] modelNames) {
        initComponents();
        setup(w, ps, modelNames);
        //jButton3.setVisible(false);

        
    }
    
   /* private void setupThread() {
            scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (LearningRow lr : pathPanels) {
                    lr.updateValueGUI();
                }
            }
        }, 500, 50, TimeUnit.MILLISECONDS);
    } */
    
    //TODO: check if this setValue here is resulting in duplicate call to learning manager value change
    //Assumes that ordering of outputs is never going to change; don't have to look up anything or refer to SupervisedLearningManager.
    private void outputValuesComputed(double[] vals) {
        for (int i = 0; i < vals.length; i++) {
            pathPanels.get(i).setComputedValue(vals[i]);
        }
    }
    
    //Called when new output values received via OSC (not via GUI, not computed)
    private void outputValuesReceived(double[] vals) {
        for (int i = 0; i < vals.length; i++) {
            if (paths.get(i).getOSCOutput().isLegalTrainingValue(vals[i])) {
                pathPanels.get(i).setValueOnlyForDisplay(vals[i]);
            } else {
                logger.log(Level.WARNING, "Received illegal value of {0} for model {1}; ignoring it"
                        , new Object[]{vals[i], i});
            }
        }
    }

    private void addPathsToGUI() {
        pathsPanel.removeAll();
        double[] currentValues = w.getOutputManager().getCurrentValues();
        for (int i = 0; i < paths.size(); i++) {
            LearningRow r = new SupervisedLearningRow(w, paths.get(i));
            r.setValue(currentValues[i]);
            pathPanels.add(r);
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(32767, 5));
            pathsPanel.add(sep);
            pathsPanel.add(r.getComponent());
        }
        
        pathsPanel.revalidate();
        scrollPathsPanel.validate();
        repaint();  
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
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        buttonRandomize = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        buttonDeleteAllExamples = new javax.swing.JButton();
        buttonViewExamples = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        buttonRecord = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        scrollPathsPanel = new javax.swing.JScrollPane();
        pathsPanel = new javax.swing.JPanel();
        supervisedLearningRow6 = new wekimini.gui.SupervisedLearningRow();
        supervisedLearningRow5 = new wekimini.gui.SupervisedLearningRow();
        supervisedLearningRow7 = new wekimini.gui.SupervisedLearningRow();
        supervisedLearningRow8 = new wekimini.gui.SupervisedLearningRow();

        setMaximumSize(new java.awt.Dimension(580, 32767));
        setMinimumSize(new java.awt.Dimension(580, 0));
        setPreferredSize(new java.awt.Dimension(580, 348));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 38, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Values");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        buttonRandomize.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonRandomize.setText("randomize");
        buttonRandomize.setToolTipText("Set outputs to random values");
        buttonRandomize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRandomizeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
            .addComponent(buttonRandomize)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(buttonRandomize, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setText("Examples");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        buttonDeleteAllExamples.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/x2.png"))); // NOI18N
        buttonDeleteAllExamples.setToolTipText("Delete all examples");
        buttonDeleteAllExamples.setPreferredSize(new java.awt.Dimension(34, 34));
        buttonDeleteAllExamples.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteAllExamplesActionPerformed(evt);
            }
        });

        buttonViewExamples.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/mag2.png"))); // NOI18N
        buttonViewExamples.setToolTipText("View examples");
        buttonViewExamples.setMaximumSize(new java.awt.Dimension(30, 34));
        buttonViewExamples.setName(""); // NOI18N
        buttonViewExamples.setPreferredSize(new java.awt.Dimension(34, 34));
        buttonViewExamples.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonViewExamplesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(buttonViewExamples, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(buttonDeleteAllExamples, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 0, 0)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonDeleteAllExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonViewExamples, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jLabel4.setText("Configure");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        buttonRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/record1.png"))); // NOI18N
        buttonRecord.setToolTipText("Toggle example recording for all outputs");
        buttonRecord.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonRecord.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonRecord.setName(""); // NOI18N
        buttonRecord.setPreferredSize(new java.awt.Dimension(34, 34));
        buttonRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRecordActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/play1.png"))); // NOI18N
        jButton5.setToolTipText("Toggle run-time computation for all outputs");
        jButton5.setMaximumSize(new java.awt.Dimension(30, 34));
        jButton5.setName(""); // NOI18N
        jButton5.setPreferredSize(new java.awt.Dimension(34, 34));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel5.setText("Edit  Status");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(buttonRecord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel4)
                .addGap(0, 0, 0)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(buttonRecord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addContainerGap())))
        );

        jLabel1.setText("Models");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));

        scrollPathsPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPathsPanel.setMaximumSize(new java.awt.Dimension(578, 32767));
        scrollPathsPanel.setMinimumSize(new java.awt.Dimension(578, 23));
        scrollPathsPanel.setPreferredSize(new java.awt.Dimension(588, 374));

        pathsPanel.setBackground(new java.awt.Color(255, 255, 255));
        pathsPanel.setLayout(new javax.swing.BoxLayout(pathsPanel, javax.swing.BoxLayout.Y_AXIS));
        pathsPanel.add(supervisedLearningRow6);
        pathsPanel.add(supervisedLearningRow5);
        pathsPanel.add(supervisedLearningRow7);
        pathsPanel.add(supervisedLearningRow8);

        scrollPathsPanel.setViewportView(pathsPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrollPathsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(scrollPathsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonRandomizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRandomizeActionPerformed
        w.getOutputManager().randomizeAllOutputs();
    }//GEN-LAST:event_buttonRandomizeActionPerformed

    private void runClicked() {
        runAll = !runAll;
        updateRunButton();
        for (LearningRow r : pathPanels) {
           r.setRunEnabled(runAll);
       } 
    }
    
    private void updateRunButton() {
        if (runAll) {
            jButton5.setIcon(playIconOn);
        } else {
            jButton5.setIcon(playIconOff);
        }
    }
    
    private void buttonViewExamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonViewExamplesActionPerformed
        w.getMainGUI().showExamplesViewer();
    }//GEN-LAST:event_buttonViewExamplesActionPerformed

    private void buttonDeleteAllExamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteAllExamplesActionPerformed
        //w.getSupervisedLearningManager().deleteAllExamples();
        w.getWekinatorController().deleteAllExamples();
    }//GEN-LAST:event_buttonDeleteAllExamplesActionPerformed

    private void recordClicked() {
        recordAll = !recordAll;
        updateRecordButton();
        for (LearningRow r : pathPanels) {
           r.setRecordEnabled(recordAll);
       }
    }
    
    private void buttonRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRecordActionPerformed
        recordClicked();
    }//GEN-LAST:event_buttonRecordActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        runClicked();
    }//GEN-LAST:event_jButton5ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDeleteAllExamples;
    private javax.swing.JButton buttonRandomize;
    private javax.swing.JButton buttonRecord;
    private javax.swing.JButton buttonViewExamples;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel pathsPanel;
    private javax.swing.JScrollPane scrollPathsPanel;
    private wekimini.gui.SupervisedLearningRow supervisedLearningRow5;
    private wekimini.gui.SupervisedLearningRow supervisedLearningRow6;
    private wekimini.gui.SupervisedLearningRow supervisedLearningRow7;
    private wekimini.gui.SupervisedLearningRow supervisedLearningRow8;
    // End of variables declaration//GEN-END:variables

    private void updateRecordButton() {
        if (recordAll) {
            buttonRecord.setIcon(recordIconOn);
        } else {
            buttonRecord.setIcon(recordIconOff);
        }
    }
}
