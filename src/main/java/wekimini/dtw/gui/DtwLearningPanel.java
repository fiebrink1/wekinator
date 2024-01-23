package wekimini.dtw.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import wekimini.DtwLearningManager;
import wekimini.SupervisedLearningManager;
import wekimini.Path;
import wekimini.StatusUpdateCenter;
import wekimini.TrainingRunner;
import wekimini.Wekinator;
import wekimini.learning.dtw.DtwData;
import wekimini.osc.OSCMonitor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fiebrink
 */
public class DtwLearningPanel extends javax.swing.JPanel {

    private Wekinator w;
    private final ImageIcon onIcon = new ImageIcon(getClass().getResource("/wekimini/icons/green3.png")); // NOI18N
    private final ImageIcon offIcon = new ImageIcon(getClass().getResource("/wekimini/icons/yellow2.png")); // NOI18N
    private final ImageIcon problemIcon = new ImageIcon(getClass().getResource("/wekimini/icons/redx1.png")); // NOI18N
    private final ImageIcon problemIcon2 = new ImageIcon(getClass().getResource("/wekimini/icons/red1.png")); // NOI18N

    int lastRoundAdvertised = 0;

    /**
     * Creates new form TestLearningPanel1
     */
    public DtwLearningPanel() {
        initComponents();
    }
    
    public DtwLearningPanel(Wekinator w) {
        initComponents();
        setup(w);
    }

    //FIX LATER
    private void setup(Wekinator w) {
        this.w = w;
        dtwLearningSetGUI1.setup(w);
        // w.getDtwLearningManager().addPropertyChangeListener(this::learningManagerPropertyChanged);
        w.getDtwLearningManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                learningManagerPropertyChanged(evt);
            }
        });

        w.getStatusUpdateCenter().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                statusUpdated(evt);
            }
        });

        if (w.getStatusUpdateCenter().getLastUpdate() == null) {
            setStatus("Ready to go! Hold the '+' button next to a gesture category to record a new example");
        } else {
            setStatus(w.getStatusUpdateCenter().getLastUpdate().toString());
        }

        w.getOSCMonitor().startMonitoring();
        w.getOSCMonitor().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscMonitorChanged(evt);
            }
        });
        
        w.getDtwLearningManager().getData().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DtwData.PROP_NUM_DELETED_AND_CACHED)) {
                    updateReAddButton(((Integer)evt.getNewValue()) > 0);
                }
            }
        });
        
        updateReAddButton(w.getDtwLearningManager().getData().getNumDeletedAndCached() > 0);
        setButtonsForLearningState();
        updateDeleteLastRoundButton();
        setInIcon(w.getOSCMonitor().getReceiveState());
        setOutIcon(w.getOSCMonitor().isSending());
    }

    private void setInIcon(OSCMonitor.OSCReceiveState rstate) {
        if (rstate == OSCMonitor.OSCReceiveState.NOT_CONNECTED) {
            indicatorOscIn.setIcon(problemIcon);
            indicatorOscIn.setToolTipText("OSC receiver is not listening");
        } else if (rstate == OSCMonitor.OSCReceiveState.CONNECTED_NODATA) {
            indicatorOscIn.setIcon(offIcon);
            indicatorOscIn.setToolTipText("Listening, but no data arriving");
        } else if (rstate == OSCMonitor.OSCReceiveState.RECEIVING) {
            indicatorOscIn.setIcon(onIcon);
            indicatorOscIn.setToolTipText("Receiving inputs");
        } else {
            //There's a problem!
            indicatorOscIn.setIcon(problemIcon2);
            indicatorOscIn.setToolTipText("Wrong number of inputs received");
        }
    }

    private void setOutIcon(boolean isSending) {
        if (isSending) {
            indicatorOscOut.setIcon(onIcon);
            indicatorOscOut.setToolTipText("Sending outputs");
        } else {
            indicatorOscOut.setIcon(offIcon);
            indicatorOscOut.setToolTipText("Not sending outputs");
        }
    }

    private void oscMonitorChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == OSCMonitor.PROP_RECEIVE_STATE) {
            setInIcon((OSCMonitor.OSCReceiveState) evt.getNewValue());

        } else if (evt.getPropertyName() == OSCMonitor.PROP_ISSENDING) {
            setOutIcon((Boolean) evt.getNewValue());
        }
    }

    private void statusUpdated(PropertyChangeEvent evt) {
        StatusUpdateCenter.StatusUpdate u = (StatusUpdateCenter.StatusUpdate) evt.getNewValue();
        setStatus(u.toString());
    }

    private void learningCancelled() {
        //setStatus("Training was cancelled.");
        w.getStatusUpdateCenter().update(this, "Training was cancelled");

    }

    private void trainerUpdated(TrainingRunner.TrainingStatus newStatus) {
        String s = newStatus.getNumTrained() + " of " + newStatus.getNumToTrain()
                + " models trained, " + newStatus.getNumErrorsEncountered()
                + " errors encountered.";
        if (newStatus.isWasCancelled()) {
            s += " Training cancelled.";
        }
        //  setStatus(s);
        w.getStatusUpdateCenter().update(this, s);
    }

    private void updateDeleteLastRoundButton() {
        boolean hasExamples = w.getDtwLearningManager().hasExamples();
        buttonDeleteLastExample.setEnabled(hasExamples);
    }

    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DtwLearningManager.PROP_RUNNING_STATE) {
            updateRunButtonAndText();
        } else if (evt.getPropertyName() == DtwLearningManager.PROP_CANRUN) {
            setButtonsForLearningState();
        } else if (evt.getPropertyName() == DtwLearningManager.PROP_HAS_EXAMPLES) {
            updateDeleteLastRoundButton();
        }
   }

    private void updateRunButtonAndText() {
        if (w.getDtwLearningManager().getRunningState() == DtwLearningManager.RunningState.RUNNING) {
            buttonRun.setText("Stop running");
            buttonRun.setForeground(Color.RED);
            //  setStatus("Running.");
            w.getStatusUpdateCenter().update(this, "Running");
        } else {
            buttonRun.setText("Run");
            buttonRun.setForeground(Color.BLACK);
            // setStatus("Stopped running.");
            w.getStatusUpdateCenter().update(this, "Stopped running");
        }
    }

    private void setButtonsForLearningState() {
        buttonRun.setEnabled(w.getDtwLearningManager().canRun());
    }

    private void setStatus(String s) {
        labelStatus.setText(s);
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
        buttonRun = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        buttonDeleteLastExample = new javax.swing.JButton();
        buttonReAddLastExample = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        indicatorOscIn = new javax.swing.JLabel();
        indicatorOscOut = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        dtwLearningSetGUI1 = new wekimini.dtw.gui.DtwLearningSetGUI();
        panelStatus = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        buttonRun.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        buttonRun.setText("Run");
        buttonRun.setEnabled(false);
        buttonRun.setPreferredSize(new java.awt.Dimension(132, 29));
        buttonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRunActionPerformed(evt);
            }
        });

        buttonDeleteLastExample.setText("Delete last example ");
        buttonDeleteLastExample.setEnabled(false);
        buttonDeleteLastExample.setMaximumSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastExample.setMinimumSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastExample.setPreferredSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastExample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteLastExampleActionPerformed(evt);
            }
        });

        buttonReAddLastExample.setText("Re-add last example");
        buttonReAddLastExample.setEnabled(false);
        buttonReAddLastExample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonReAddLastExampleActionPerformed(evt);
            }
        });

        indicatorOscIn.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        indicatorOscIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/green3.png"))); // NOI18N
        indicatorOscIn.setText("OSC In");
        indicatorOscIn.setToolTipText("Receiver is not listening");
        indicatorOscIn.setPreferredSize(new java.awt.Dimension(45, 28));
        indicatorOscIn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                indicatorOscInMouseClicked(evt);
            }
        });

        indicatorOscOut.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        indicatorOscOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/yellow2.png"))); // NOI18N
        indicatorOscOut.setText("OSC Out");
        indicatorOscOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                indicatorOscOutMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonDeleteLastExample, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonReAddLastExample, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(buttonRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(indicatorOscIn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(indicatorOscOut)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(indicatorOscIn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indicatorOscOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(96, 96, 96)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonDeleteLastExample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(buttonReAddLastExample))
        );

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(dtwLearningSetGUI1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(100, Short.MAX_VALUE))
            .addComponent(dtwLearningSetGUI1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        panelStatus.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Status:");

        labelStatus.setText(" ");

        javax.swing.GroupLayout panelStatusLayout = new javax.swing.GroupLayout(panelStatus);
        panelStatus.setLayout(panelStatusLayout);
        panelStatusLayout.setHorizontalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelStatusLayout.setVerticalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelStatus))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelStatus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonDeleteLastExampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteLastExampleActionPerformed
        w.getDtwLearningManager().deleteLastExample();
        w.getStatusUpdateCenter().update(this, "Deleted last example");
       // updateReAddButton(true);
    }//GEN-LAST:event_buttonDeleteLastExampleActionPerformed

    private void updateReAddButton(boolean canReAdd) {
        buttonReAddLastExample.setEnabled(canReAdd);
    }

    private void buttonReAddLastExampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonReAddLastExampleActionPerformed
        w.getDtwLearningManager().reAddLastExample();
        w.getStatusUpdateCenter().update(this, "Last example re-added");
      //  updateReAddButton(false);
    }//GEN-LAST:event_buttonReAddLastExampleActionPerformed

    private void buttonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRunActionPerformed
        if (w.getDtwLearningManager().getRunningState() == DtwLearningManager.RunningState.NOT_RUNNING) {
            w.getDtwLearningManager().startRunning();
            //  w.getDtwLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        } else {
            w.getDtwLearningManager().stopRunning();
            //w.getDtwLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        }
    }//GEN-LAST:event_buttonRunActionPerformed

    private void indicatorOscInMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_indicatorOscInMouseClicked
        w.getMainGUI().showOSCReceiverWindow();
    }//GEN-LAST:event_indicatorOscInMouseClicked

    private void indicatorOscOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_indicatorOscOutMouseClicked
        w.getMainGUI().showOutputTable();
    }//GEN-LAST:event_indicatorOscOutMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDeleteLastExample;
    private javax.swing.JButton buttonReAddLastExample;
    private javax.swing.JButton buttonRun;
    private wekimini.dtw.gui.DtwLearningSetGUI dtwLearningSetGUI1;
    private javax.swing.JLabel indicatorOscIn;
    private javax.swing.JLabel indicatorOscOut;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JPanel panelStatus;
    // End of variables declaration//GEN-END:variables

    public void setPerfomanceMode(boolean selected) {
        if (selected) {
            dtwLearningSetGUI1.setVisible(false);
            panelStatus.setVisible(false);
            this.setSize(jPanel2.getPreferredSize());
        } else {
            dtwLearningSetGUI1.setVisible(true);
            panelStatus.setVisible(true);
            this.setSize(getPreferredSize());
        }
    }
}
