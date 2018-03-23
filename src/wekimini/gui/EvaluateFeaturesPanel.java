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
        confusionWrapper.setVisible(false);
        confusionHoldingImage.setVisible(true);
        confusionHoldingImage.loadImage(getClass().getResource("/wekimini/icons/confusionPlaceholder.png"));
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
        
        trainBtn.setEnabled(w.getDataManager().getNumExamples() > 0);
    }
    
    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        
        trainBtn.setEnabled(w.getDataManager().getNumExamples() > 0);
        
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
                    trainBtn.setForeground(Color.red);
                }
                else
                {
                    trainBtn.setForeground(Color.black);
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
        evaluateBtn = new javax.swing.JButton();
        accuracyLabel = new javax.swing.JLabel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        confusionHoldingImage = new wekimini.gui.ImagePanel();
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

        evaluateBtn.setBackground(new java.awt.Color(255, 0, 204));
        evaluateBtn.setText("Evaluate");
        evaluateBtn.setContentAreaFilled(false);
        evaluateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evaluateBtnActionPerformed(evt);
            }
        });

        accuracyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        accuracyLabel.setText("Accuracy:");

        confusionHoldingImage.setPreferredSize(new java.awt.Dimension(170, 159));

        javax.swing.GroupLayout confusionHoldingImageLayout = new javax.swing.GroupLayout(confusionHoldingImage);
        confusionHoldingImage.setLayout(confusionHoldingImageLayout);
        confusionHoldingImageLayout.setHorizontalGroup(
            confusionHoldingImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 170, Short.MAX_VALUE)
        );
        confusionHoldingImageLayout.setVerticalGroup(
            confusionHoldingImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 168, Short.MAX_VALUE)
        );

        confusionWrapper.setBackground(new java.awt.Color(255, 255, 255));
        confusionWrapper.setPreferredSize(new java.awt.Dimension(211, 211));
        confusionWrapper.setRequestFocusEnabled(false);

        javax.swing.GroupLayout confusionWrapperLayout = new javax.swing.GroupLayout(confusionWrapper);
        confusionWrapper.setLayout(confusionWrapperLayout);
        confusionWrapperLayout.setHorizontalGroup(
            confusionWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 207, Short.MAX_VALUE)
        );
        confusionWrapperLayout.setVerticalGroup(
            confusionWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 164, Short.MAX_VALUE)
        );

        jLayeredPane1.setLayer(confusionHoldingImage, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(confusionWrapper, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(confusionHoldingImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(confusionWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addGap(0, 14, Short.MAX_VALUE)
                .addComponent(confusionHoldingImage, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addGap(12, 12, 12)
                    .addComponent(confusionWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accuracyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(trainBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(evaluateBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(evaluateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(accuracyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        evaluateBtn.setEnabled(true);
        confusionWrapper.setVisible(true);
        confusionHoldingImage.setVisible(false);
    }

    private void cvCancelled() 
    {
        evaluateBtn.setEnabled(true);
        System.out.println("CV Cancelled!!!");
    }

    private void cvFinished(String[] results) 
    {
        evaluateBtn.setEnabled(true);
        System.out.println("CV Finished");
    }
    
    private void evaluateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluateBtnActionPerformed
        // TODO add your handling code here:
        evaluateBtn.setText("Re-evaluate");
        evaluateBtn.setEnabled(false);
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
    }//GEN-LAST:event_evaluateBtnActionPerformed

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
    private wekimini.gui.ImagePanel confusionHoldingImage;
    private javax.swing.JPanel confusionWrapper;
    private javax.swing.JButton evaluateBtn;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JButton trainBtn;
    // End of variables declaration//GEN-END:variables
}
