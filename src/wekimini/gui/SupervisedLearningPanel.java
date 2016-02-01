package wekimini.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import wekimini.SupervisedLearningManager;
import wekimini.Path;
import wekimini.StatusUpdateCenter;
import wekimini.TrainingRunner;
import wekimini.Wekinator;
import wekimini.kadenze.KadenzeLogging;
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
public class SupervisedLearningPanel extends javax.swing.JPanel {

    private Wekinator w;
    private final ImageIcon onIcon = new ImageIcon(getClass().getResource("/wekimini/icons/green3.png")); // NOI18N
    private final ImageIcon offIcon = new ImageIcon(getClass().getResource("/wekimini/icons/yellow2.png")); // NOI18N
    private final ImageIcon problemIcon = new ImageIcon(getClass().getResource("/wekimini/icons/redx1.png")); // NOI18N
    private final ImageIcon problemIcon2 = new ImageIcon(getClass().getResource("/wekimini/icons/red1.png")); // NOI18N

    
    
    int lastRoundAdvertised = 0;

    /**
     * Creates new form TestLearningPanel1
     */
    public SupervisedLearningPanel() {
        initComponents();
    }

    public void setup(Wekinator w, Path[] ps, String[] modelNames) {
        this.w = w;

        simpleLearningSet1.setup(w, ps, modelNames);
       // w.getSupervisedLearningManager().addPropertyChangeListener(this::learningManagerPropertyChanged);
        w.getSupervisedLearningManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                learningManagerPropertyChanged(evt);
            }
        });
        

        w.getTrainingRunner().addCancelledListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                learningCancelled();
            }
        });

        w.getTrainingRunner().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == TrainingRunner.PROP_TRAININGPROGRESS) {
                    trainerUpdated((TrainingRunner.TrainingStatus) evt.getNewValue());
                }

            }
        });

       // w.getStatusUpdateCenter().addPropertyChangeListener(this::statusUpdated);
        w.getStatusUpdateCenter().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                statusUpdated(evt);
            }
        });
        
        if (w.getStatusUpdateCenter().getLastUpdate() == null) {
            setStatus("Ready to go! Press \"Start Recording\" above to record some examples.");
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
        setButtonsForLearningState();
        updateDeleteLastRoundButton();
        updateRecordingButton();
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
        int lastRound = w.getSupervisedLearningManager().getRecordingRound();
        int numLastRound = w.getDataManager().getNumExamplesInRound(lastRound);
        //Look for most recent round with >0 examples recorded.
        while (lastRound > 0 && numLastRound == 0) {
            lastRound--;
            numLastRound = w.getDataManager().getNumExamplesInRound(lastRound);
        }
        if (numLastRound > 0) {
            lastRoundAdvertised = lastRound;
            buttonDeleteLastRecording.setText("Delete last recording (#"
                    + lastRoundAdvertised + ")");
            buttonDeleteLastRecording.setEnabled(true);
        } else {
            buttonDeleteLastRecording.setText("Delete last recording");
            buttonDeleteLastRecording.setEnabled(false);
        }
    }

    private void updateForRecordingState(SupervisedLearningManager.RecordingState rs) {
        updateRecordingButton();
        updateButtonStates();
        if (rs == SupervisedLearningManager.RecordingState.NOT_RECORDING) {
            updateDeleteLastRoundButton();
        }
    }

    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == SupervisedLearningManager.PROP_RECORDINGROUND) {
            updateDeleteLastRoundButton();
        }else if (evt.getPropertyName() == SupervisedLearningManager.PROP_RECORDINGSTATE) {
            updateForRecordingState((SupervisedLearningManager.RecordingState) evt.getNewValue());
        } else if (evt.getPropertyName() == SupervisedLearningManager.PROP_LEARNINGSTATE) {
            setButtonsForLearningState();
            updateStatusForLearningState();
            // System.out.println("Learning state updated: " + w.getSupervisedLearningManager().getLearningState());
        } else if (evt.getPropertyName() == SupervisedLearningManager.PROP_RUNNINGSTATE) {
            updateRunButtonAndText();
        } else if (evt.getPropertyName() == SupervisedLearningManager.PROP_NUMEXAMPLESTHISROUND) {
            //TODO: Update somewhere else
           // TODO THis is really sloww.getStatusUpdateCenter().update(this, "New examples recorded");
            //setStatus();
        } else if (evt.getPropertyName() == SupervisedLearningManager.PROP_ABLE_TO_RECORD) {
            setButtonsForLearningState();
        } else if (evt.getPropertyName() == SupervisedLearningManager.PROP_ABLE_TO_RUN) {
            setButtonsForLearningState();
        }
//Could do training state for GUI too... Want to have singleton Training Worker so all GUI elements can access update info
    }

    private void updateRunButtonAndText() {
        if (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.RUNNING) {
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

    private void updateStatusForTrainingWorker() {

    }

    private void updateStatusForLearningState() {
        SupervisedLearningManager.LearningState ls = w.getSupervisedLearningManager().getLearningState();
        if (ls == SupervisedLearningManager.LearningState.NOT_READY_TO_TRAIN) {
           // setStatus("Ready to go! Press \"Start Recording\" above to record some examples.");
            w.getStatusUpdateCenter().update(this, "Ready to go! Press \"Start Recording\" above to record some examples.");

        } else if (ls == SupervisedLearningManager.LearningState.TRAINING) {
            //setStatus("Training...");
            w.getStatusUpdateCenter().update(this, "Training...");

        } else if (ls == SupervisedLearningManager.LearningState.DONE_TRAINING) {
            if (w.getTrainingRunner().wasCancelled()) {
                 w.getStatusUpdateCenter().update(this, "Training was cancelled.");
            } else if (w.getTrainingRunner().errorEncountered()) {
                 w.getStatusUpdateCenter().update(this, "Error(s) encountered during training.");
            } else {
                int n = w.getSupervisedLearningManager().numRunnableModels();
                if (n > 0) {
                     w.getStatusUpdateCenter().update(this, "Training completed. Press \"Run\" to run trained models.");
                } else {
                     w.getStatusUpdateCenter().update(this, "No models are ready to run. Record data and/or train.");
                }
            }
        } else if (ls == SupervisedLearningManager.LearningState.READY_TO_TRAIN) {
            w.getStatusUpdateCenter().update(this, "New examples recorded");
           // setStatus("Examples recorded. Press \"Train\" to build models from data.");
        }
    }

    private void setButtonsForLearningState() {
        buttonRun.setEnabled(w.getSupervisedLearningManager().isAbleToRun());
        buttonRecord.setEnabled(w.getSupervisedLearningManager().isAbleToRecord());

        SupervisedLearningManager.LearningState ls = w.getSupervisedLearningManager().getLearningState();
        if (ls == SupervisedLearningManager.LearningState.NOT_READY_TO_TRAIN) {
            buttonTrain.setText("Train");
            buttonTrain.setEnabled(false);
            buttonTrain.setForeground(Color.BLACK);
        } else if (ls == SupervisedLearningManager.LearningState.TRAINING) {
            buttonTrain.setEnabled(true);
            buttonTrain.setText("Cancel training");
            buttonTrain.setForeground(Color.RED);
        } else if (ls == SupervisedLearningManager.LearningState.DONE_TRAINING) {
            buttonTrain.setEnabled(true); //Don't prevent immediate retraining; some model builders may give different models on same data.
            buttonTrain.setText("Train");
            buttonTrain.setForeground(Color.BLACK);
        } else if (ls == SupervisedLearningManager.LearningState.READY_TO_TRAIN) {
            buttonTrain.setEnabled(true);
            buttonTrain.setText("Train");
            buttonTrain.setForeground(Color.BLACK);
        }
    }

    private void updateRecordingButton() {
        if (w.getSupervisedLearningManager().getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING) {
            buttonRecord.setText("Stop Recording");
            buttonRecord.setForeground(Color.red);
        } else {
            buttonRecord.setText("Start Recording");
            buttonRecord.setForeground(Color.black);
        }
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
        buttonRecord = new javax.swing.JButton();
        buttonTrain = new javax.swing.JButton();
        buttonRun = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        buttonDeleteLastRecording = new javax.swing.JButton();
        buttonReAddLastRecording = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        indicatorOscIn = new javax.swing.JLabel();
        indicatorOscOut = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        panelStatus = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        simpleLearningSet1 = new wekimini.gui.SupervisedLearningSetGUI();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        buttonRecord.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        buttonRecord.setText("Start Recording");
        buttonRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRecordActionPerformed(evt);
            }
        });

        buttonTrain.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        buttonTrain.setText("<html>Train</html>");
        buttonTrain.setEnabled(false);
        buttonTrain.setMaximumSize(new java.awt.Dimension(75, 29));
        buttonTrain.setPreferredSize(new java.awt.Dimension(132, 29));
        buttonTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTrainActionPerformed(evt);
            }
        });

        buttonRun.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        buttonRun.setText("Run");
        buttonRun.setEnabled(false);
        buttonRun.setPreferredSize(new java.awt.Dimension(132, 29));
        buttonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRunActionPerformed(evt);
            }
        });

        buttonDeleteLastRecording.setEnabled(false);
        buttonDeleteLastRecording.setLabel("Delete last recording ");
        buttonDeleteLastRecording.setMaximumSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastRecording.setMinimumSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastRecording.setPreferredSize(new java.awt.Dimension(225, 29));
        buttonDeleteLastRecording.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteLastRecordingActionPerformed(evt);
            }
        });

        buttonReAddLastRecording.setEnabled(false);
        buttonReAddLastRecording.setLabel("Re-add last recording");
        buttonReAddLastRecording.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonReAddLastRecordingActionPerformed(evt);
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
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonDeleteLastRecording, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jSeparator2)
                    .addComponent(buttonRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonTrain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(indicatorOscIn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(indicatorOscOut))
                    .addComponent(buttonReAddLastRecording, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
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
                .addGap(0, 0, 0)
                .addComponent(buttonRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonTrain, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonRun, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonDeleteLastRecording, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonReAddLastRecording)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel1.add(jSeparator1);

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
            .addComponent(labelStatus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(simpleLearningSet1, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(panelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(simpleLearningSet1, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonStates() {
        // if ()

        /*if (w.getSupervisedLearningManager().getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING) {
         buttonRecord.setEnabled(true);
         buttonTrain.setEnabled(false);
         buttonRun.setEnabled(false);
         } else if (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.RUNNING) {
         buttonRecord.setEnabled(true);
         buttonTrain.setEnabled(false);
         buttonRun.setEnabled(false);
         } */
    }

    private void buttonRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRecordActionPerformed

       /* if (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.RUNNING) {
           w.getWekinatorController().stopRun();
            // w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        } */

        if (w.getSupervisedLearningManager().getRecordingState() != SupervisedLearningManager.RecordingState.RECORDING) {
           w.getSupervisedLearningManager().getSupervisedLearningController().startRecord();
            /* w.getSupervisedLearningManager().startRecording();
             w.getStatusUpdateCenter().update(this, "Recording - waiting for inputs to arrive"); */
        } else {
            w.getSupervisedLearningManager().getSupervisedLearningController().stopRecord();
           /* w.getSupervisedLearningManager().stopRecording();
           // setStatus("Examples recorded. Press \"Train\" to build models from data.");
            w.getStatusUpdateCenter().update(this, w.getSupervisedLearningManager().getNumExamplesThisRound() + " new examples recorded");*/
        }
    }//GEN-LAST:event_buttonRecordActionPerformed

    private void buttonTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTrainActionPerformed
        if (w.getSupervisedLearningManager().getLearningState() == SupervisedLearningManager.LearningState.TRAINING) {
            w.getSupervisedLearningManager().getSupervisedLearningController().cancelTrain();
            //w.getSupervisedLearningManager().cancelTraining();
        } else {
            w.getSupervisedLearningManager().getSupervisedLearningController().train();     
            //w.getSupervisedLearningManager().buildAll();
        }
    }//GEN-LAST:event_buttonTrainActionPerformed

    private void buttonDeleteLastRecordingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteLastRecordingActionPerformed
        w.getDataManager().deleteTrainingRound(lastRoundAdvertised);
        int numDeleted = w.getDataManager().getNumDeletedTrainingRound();
        KadenzeLogging.getLogger().deleteLastRecordingRound(w, lastRoundAdvertised);
        w.getStatusUpdateCenter().update(this, "Recording set #" + lastRoundAdvertised + " (" + numDeleted + " examples) deleted.");
        if (numDeleted > 0) {
            updateReAddButton(lastRoundAdvertised);
        }
        updateDeleteLastRoundButton();
    }//GEN-LAST:event_buttonDeleteLastRecordingActionPerformed

    private void updateReAddButton(int whichRound) {
        buttonReAddLastRecording.setText("Re-add last recording (#" + whichRound + ")");
        buttonReAddLastRecording.setEnabled(true);
    }

    private void buttonReAddLastRecordingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonReAddLastRecordingActionPerformed
        int num = w.getDataManager().getNumDeletedTrainingRound();
        w.getDataManager().reAddDeletedTrainingRound();
        w.getStatusUpdateCenter().update(this, "Last recording set restored: undeleted " + num + " examples");
        updateDeleteLastRoundButton();
        KadenzeLogging.getLogger().reAddLastRecordingRound(w, lastRoundAdvertised);
        buttonReAddLastRecording.setText("Re-add last recording");
        buttonReAddLastRecording.setEnabled(false);
    }//GEN-LAST:event_buttonReAddLastRecordingActionPerformed

    private void buttonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRunActionPerformed
        /*if (w.getSupervisedLearningManager().getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING) {
            w.getSupervisedLearningManager().stopRecording();
        } */

        if (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING) {
            w.getSupervisedLearningManager().getSupervisedLearningController().startRun();
            //  w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        } else {
            w.getSupervisedLearningManager().getSupervisedLearningController().stopRun();
            //w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        }
    }//GEN-LAST:event_buttonRunActionPerformed

    private void indicatorOscInMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_indicatorOscInMouseClicked
        w.getMainGUI().showOSCReceiverWindow();
    }//GEN-LAST:event_indicatorOscInMouseClicked

    private void indicatorOscOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_indicatorOscOutMouseClicked
        w.getMainGUI().showOutputTable();
    }//GEN-LAST:event_indicatorOscOutMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDeleteLastRecording;
    private javax.swing.JButton buttonReAddLastRecording;
    private javax.swing.JButton buttonRecord;
    private javax.swing.JButton buttonRun;
    private javax.swing.JButton buttonTrain;
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
    private wekimini.gui.SupervisedLearningSetGUI simpleLearningSet1;
    // End of variables declaration//GEN-END:variables

    public void setPerfomanceMode(boolean selected) {
        if (selected) {
            simpleLearningSet1.setVisible(false);
            panelStatus.setVisible(false);
            this.setSize(jPanel2.getPreferredSize());
        } else {
            simpleLearningSet1.setVisible(true);
            panelStatus.setVisible(true);
            this.setSize(getPreferredSize());
        }
    }
}
