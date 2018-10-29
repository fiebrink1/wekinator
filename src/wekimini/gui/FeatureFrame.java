/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;
import javax.swing.Timer;
import wekimini.SupervisedLearningManager;
import wekimini.Wekinator;
import wekimini.kadenze.FeaturnatorLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class FeatureFrame extends JFrame {
    
    private Wekinator w;
    private int selectedRow = -1;
    private int outputIndex = 0;
    private Feature selectedFeature;
    private Timer sliderTimer;
    boolean wasRunning;
    boolean wasPlotting;
    boolean resetState = true;


    public FeatureFrame() {
        initComponents();
    }
    
    public FeatureFrame(Wekinator w) {
        initComponents();
        this.w = w;
        FeatureEditorDelegate delegate = new FeatureEditorDelegate() {
            //FeatureEditorDelegate methods
    
            @Override
            public void newFeatureSelected(Feature ft)
            {
                updateSelectedFeature(ft);
                deselectRows(false);
            }

            @Override
            public void featureListUpdated()
            {
                System.out.println("FeatureEditorDelegate featureListUpdated()");
                w.getDataManager().featureListUpdated();
                newFeaturesPanel.featureListUpdated();
                evaluateFeaturesPanel.featuresListUpdated();
                //featureDetailPanel.showNoFeature();
            }

            @Override
            public void featureLibraryUpdated(boolean sizeDidChange)
            {
                featureLibraryUpdate();
            }

            @Override
            public void windowSliderChanged(double newVal)
            {
                debounceSliderAction(newVal);
            }

            @Override
            public void blockInteraction(boolean doBlock)
            {
                if(doBlock)
                {
                    blockAll();
                }
                else
                {
                    unblockAll();
                }
            }
    
        };
        featureDetailPanel.delegate = delegate;
        newFeaturesPanel.delegate = delegate;
        evaluateFeaturesPanel.delegate = delegate;
        
        newFeaturesPanel.update(w, 0);
        featureDetailPanel.update(w);
        evaluateFeaturesPanel.update(w, 0);
        
        selectedFeature = w.getDataManager().featureManager.getAllFeaturesGroup().getFeatureForKey("AccX");
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                w.getSupervisedLearningManager().isPlotting = false;
                evaluateFeaturesPanel.onClose();
                newFeaturesPanel.onClose();
            }
        }); 
    }
    
    private void blockAll()
    {
        prepareForLibraryUpdate();
        featureDetailPanel.blockInteraction(true, true);
        newFeaturesPanel.blockInteraction(true, true);
    }
    
    private void unblockAll()
    {
        resetFollowingLibraryUpdate();
        featureDetailPanel.blockInteraction(false, true);
        newFeaturesPanel.blockInteraction(false, true);
    }
    
    private void featureLibraryUpdate()
    {
        w.getDataManager().setInfoGainRankingsDirty();
        w.getDataManager().featureListUpdated();
        newFeaturesPanel.featureListUpdated();
        evaluateFeaturesPanel.featuresListUpdated();
    }
    
    class ResizeListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
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

        newFeaturesPanel = new wekimini.gui.NewFeaturesPanel();
        featureDetailPanel = new wekimini.gui.FeatureDetailPanel();
        evaluateFeaturesPanel = new wekimini.gui.EvaluateFeaturesPanel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        newFeaturesPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Evaluate");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(featureDetailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newFeaturesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 711, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(evaluateFeaturesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(evaluateFeaturesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(newFeaturesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 535, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(featureDetailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void debounceSliderAction(double newVal)
    {
        blockAll();
        if(sliderTimer != null)
        {
            if(sliderTimer.isRunning())
            {
                sliderTimer.stop();
                sliderTimer = null;
            }
        }

        sliderTimer = new Timer(500, (ActionEvent arg0) -> {
            if(!evaluateFeaturesPanel.updatingMDS && !newFeaturesPanel.updatingRankings)
            {
                sliderTimer.stop();
                updateWindowSize(newVal);
            }
        });
        sliderTimer.setRepeats(true); 
        sliderTimer.start(); 
    }
    
    private void updateWindowSize(double newVal)
    {
        prepareForLibraryUpdate();
        int ws = (int)(5 + (newVal * 60));
        //TODO::WINDOW AND BUFFER SIZE ARE THE SAME HERE
        w.getDataManager().featureManager.setFeatureWindowSize(ws, ws);
        resetFollowingLibraryUpdate();
        featureLibraryUpdate();
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
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new FeatureFrame().setVisible(true);
        });
    }
    
    private void updateSelectedFeature(Feature ft)
    {
        selectedFeature = w.getDataManager().featureManager.getAllFeaturesGroup().getFeatureForKey(ft.name);
        if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
        {
            ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeaturePreviewed(w, selectedFeature);
        }
        PlotRowModel model = new PlotRowModel(100);
        w.getSupervisedLearningManager().isPlotting = true;
        model.isStreaming = true;
        model.feature = selectedFeature;
        featureDetailPanel.setModel(model);
    }
    
    
    public void selectRow(int row)
    {
        selectedRow = row;
    }
    
    public void deselectRows(boolean deselectPlot)
    {
        selectedRow = -1;
        if(deselectPlot)
        {
            featureDetailPanel.showNoFeature();
        }
    }

    private void prepareForLibraryUpdate()
    {
        System.out.println("----prepareForLibraryUpdate resetting " + resetState);
        if(resetState)
        {
            wasRunning = w.getSupervisedLearningManager().getRunningState() != SupervisedLearningManager.RunningState.NOT_RUNNING;
            wasPlotting = w.getSupervisedLearningManager().isPlotting;
            evaluateFeaturesPanel.cancelWorkers();
            if(wasRunning)
            {
                w.getSupervisedLearningManager().stopRunning();
            }
            if(wasPlotting)
            {
                System.out.println("setting is plotting to false");
                w.getSupervisedLearningManager().isPlotting = false;
            }
            resetState = false;
        }
    }
    
    private void resetFollowingLibraryUpdate()
    {
        System.out.println("----resetFollowingLibraryUpdate");
        if(wasRunning)
        {
            w.getSupervisedLearningManager().startRunning();
        }
        w.getSupervisedLearningManager().isPlotting = wasPlotting;
        resetState = true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private wekimini.gui.EvaluateFeaturesPanel evaluateFeaturesPanel;
    private wekimini.gui.FeatureDetailPanel featureDetailPanel;
    private javax.swing.JLabel jLabel2;
    private wekimini.gui.NewFeaturesPanel newFeaturesPanel;
    // End of variables declaration//GEN-END:variables
}
