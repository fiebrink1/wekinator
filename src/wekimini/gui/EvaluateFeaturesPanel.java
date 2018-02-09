/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import wekimini.OutputManager;
import wekimini.Path;
import wekimini.SupervisedLearningManager;
import wekimini.TrainingRunner;
import wekimini.Wekinator;
import wekimini.WekinatorSupervisedLearningController;
import wekimini.gui.ModelEvaluationFrame.EvaluationMode;
import wekimini.learning.ModelEvaluator;
import wekimini.util.ConfusionParser;

/**
 *
 * @author louismccallum
 */
public class EvaluateFeaturesPanel extends javax.swing.JPanel {

    private ConfusionComponent confusionPanel;
    private Wekinator w;
    private boolean isRunning = false;
    private WekinatorSupervisedLearningController controller;
    private ModelEvaluator e = null;
    private int outputIndex = 0;
    private PropertyChangeListener trainingListener;
    private PropertyChangeListener learningStateListener;
    
    public EvaluateFeaturesPanel() {
        initComponents();
        confusionWrapper.setLayout(new FlowLayout());
        confusionPanel = new ConfusionComponent();
        confusionPanel.setPreferredSize(confusionWrapper.getPreferredSize());
        confusionWrapper.add(confusionPanel);
    }
    
    public void update(Wekinator w, int output)
    {
        this.w = w;
        this.outputIndex = output;
        controller = new WekinatorSupervisedLearningController(w.getSupervisedLearningManager(),w);
        trainingListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == TrainingRunner.PROP_TRAININGPROGRESS) {
                    trainerUpdated((TrainingRunner.TrainingStatus) evt.getNewValue());
                }

            }
        };
        learningStateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                learningManagerPropertyChanged(evt);
            }
        };
        w.getSupervisedLearningManager().addPropertyChangeListener(learningStateListener);
        
        w.getTrainingRunner().addPropertyChangeListener(trainingListener);
        w.getOutputManager().addOutputGroupComputedListener(new OutputManager.OutputValueListener() {

            @Override
            public void update(double[] vals) {
                outputLabel.setText("Output:" + vals[0]);
            }
        });
    }
    
    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case SupervisedLearningManager.PROP_RECORDINGROUND:
                break;
            case SupervisedLearningManager.PROP_RECORDINGSTATE:
                break;
            case SupervisedLearningManager.PROP_LEARNINGSTATE:
                break;
            case SupervisedLearningManager.PROP_RUNNINGSTATE:
                isRunning = w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.RUNNING;
                System.out.println("Callback isRunning:" + isRunning);
                if (isRunning) {
                    trainBtn.setText("Stop running");
                }
                else
                {
                    trainBtn.setText("Train and Run");
                }
                break;
            case SupervisedLearningManager.PROP_NUMEXAMPLESTHISROUND:
                break;
            case SupervisedLearningManager.PROP_ABLE_TO_RECORD:
                break;
            case SupervisedLearningManager.PROP_ABLE_TO_RUN:
                break;
            default:
                break;
        }
    }
    
    public void onClose()
    {
        w.getTrainingRunner().removePropertyChangeListener(trainingListener);
        w.getSupervisedLearningManager().removePropertyChangeListener(learningStateListener);
    }
    
    private void trainerUpdated(TrainingRunner.TrainingStatus newStatus) {
        String s = newStatus.getNumTrained() + " of " + newStatus.getNumToTrain()
                + " models trained, " + newStatus.getNumErrorsEncountered()
                + " errors encountered.";
        boolean completed =  newStatus.getNumToTrain() == newStatus.getNumTrained();
        if(completed)
        {
            controller.startRun();
        }
        if (newStatus.isWasCancelled()) 
        {
            s += " Training cancelled.";
        }
        w.getStatusUpdateCenter().update(this, s);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        trainBtn = new javax.swing.JButton();
        outputLabel = new javax.swing.JLabel();
        reevaluateBtn = new javax.swing.JButton();
        accuracyLabel = new javax.swing.JLabel();
        confusionWrapper = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        trainBtn.setBackground(new java.awt.Color(255, 51, 0));
        trainBtn.setText("Train and Run");
        trainBtn.setContentAreaFilled(false);
        trainBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainBtnActionPerformed(evt);
            }
        });

        outputLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        outputLabel.setText("Output:0");

        reevaluateBtn.setBackground(new java.awt.Color(255, 0, 204));
        reevaluateBtn.setText("Re-evaluate");
        reevaluateBtn.setContentAreaFilled(false);
        reevaluateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reevaluateBtnActionPerformed(evt);
            }
        });

        accuracyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        accuracyLabel.setText("Accuracy:");

        confusionWrapper.setBackground(new java.awt.Color(255, 255, 255));
        confusionWrapper.setPreferredSize(new java.awt.Dimension(211, 211));
        confusionWrapper.setRequestFocusEnabled(false);

        javax.swing.GroupLayout confusionWrapperLayout = new javax.swing.GroupLayout(confusionWrapper);
        confusionWrapper.setLayout(confusionWrapperLayout);
        confusionWrapperLayout.setHorizontalGroup(
            confusionWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        confusionWrapperLayout.setVerticalGroup(
            confusionWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 176, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reevaluateBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(trainBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                    .addComponent(accuracyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(confusionWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(trainBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reevaluateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(accuracyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(confusionWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cvPropertyChanged(PropertyChangeEvent evt) 
    {
        System.out.println("CV property changed");
    }

    private void cvModelFinished(int modelNum, String results, String confusion) 
    {
        System.out.println("Model " + modelNum + ": " + results);
        int[][] arr = ConfusionParser.parseMatrix(confusion);
        confusionPanel.setModel(arr);
        accuracyLabel.setText(results);
    }

    private void cvCancelled() 
    {
        System.out.println("CV Cancelled!!!");
    }

    private void cvFinished(String[] results) 
    {
        System.out.println("CV Finished");
    }
    
    private void reevaluateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reevaluateBtnActionPerformed
        // TODO add your handling code here:
        e = new ModelEvaluator(w, new ModelEvaluator.EvaluationResultsReceiver() {

            @Override
            public void finishedModel(int modelNum, String results, String confusion) {
                cvModelFinished(modelNum, results, confusion);
            }

            @Override
            public void finished(String[] results) {
                cvFinished(results);
            }

            @Override
            public void cancelled() {
                cvCancelled();
            }
        });
        EvaluationMode eval = ModelEvaluationFrame.EvaluationMode.CROSS_VALIDATION;
        Path p = w.getSupervisedLearningManager().getPaths().get(outputIndex);
        LinkedList<Path> paths = new LinkedList<>();
        paths.add(p);
        e.evaluateAll(paths, eval, 10, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                cvPropertyChanged(evt);
            }

        });
    }//GEN-LAST:event_reevaluateBtnActionPerformed

    private void trainBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainBtnActionPerformed
        isRunning = w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.RUNNING;
        if(!isRunning)
        {
            if (controller.canTrain()) {
                controller.train();
            }
        }
        else
        {
            controller.stopRun();
        }
        
        System.out.println("Button isRunning:" + isRunning);
    }//GEN-LAST:event_trainBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel accuracyLabel;
    private javax.swing.JPanel confusionWrapper;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JButton reevaluateBtn;
    private javax.swing.JButton trainBtn;
    // End of variables declaration//GEN-END:variables
}
