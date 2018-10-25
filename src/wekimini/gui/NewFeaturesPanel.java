/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import org.jdesktop.swingworker.SwingWorker;
import wekimini.SupervisedLearningManager;
import wekimini.SupervisedLearningManager.RecordingState;
import wekimini.Wekinator;
import wekimini.WekinatorSupervisedLearningController;
import wekimini.featureanalysis.BestInfoSelector;
import wekimini.kadenze.FeaturnatorLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class NewFeaturesPanel extends javax.swing.JPanel {
    
    private Wekinator w;
    protected Feature[] selected = new Feature[0];
    public FeatureEditorDelegate delegate;
    private int outputIndex = 0;
    private double threshold = 0.5;
    public Boolean updatingRankings = false;
    private PropertyChangeListener learningStateListener;
    private FeatureSelectMenuPanel mainMenuPanel;
    private FilterFeaturesPanel filterPanel;
    private static final int TOP_LAYER = 1;
    private static final int BOTTOM_LAYER = 0;
    
    
    public NewFeaturesPanel() {
        initComponents();
    }

    private void setUpPlots() 
    {
        featureSetPlotPanel.setDimensions(664 - 10, 200);
        
        FeatureMenuDelegate menuDelegate = new FeatureMenuDelegate() {
            @Override
            public void addFeaturePressed() {
                menuLayeredPane.setLayer(filterPanel, TOP_LAYER);
                filterPanel.setIsAdding(true);
                menuLayeredPane.setLayer(mainMenuPanel, BOTTOM_LAYER);
            }

            @Override
            public void autoSelectPressed() {
                autoSelect();
            }

            @Override
            public void removeFeaturePressed() {
                menuLayeredPane.setLayer(filterPanel, TOP_LAYER);
                menuLayeredPane.setLayer(mainMenuPanel, BOTTOM_LAYER);
                filterPanel.setIsAdding(false);
            }

            @Override
            public void backPressed() {
                menuLayeredPane.setLayer(filterPanel, BOTTOM_LAYER);
                menuLayeredPane.setLayer(mainMenuPanel, TOP_LAYER);
            }

            @Override
            public void filtersUpdated() {
                updateFilters();
            }
            
            @Override
            public void selectAllFeatures() {
                selectAll();
            }

            @Override
            public void updateFeatures() {
                handleSetChange(!filterPanel.getIsAdding());
            }
            
        };
        
        mainMenuPanel = new FeatureSelectMenuPanel();
        mainMenuPanel.delegate = menuDelegate;
        mainMenuPanel.setPreferredSize(menuLayeredPane.getPreferredSize());
        mainMenuPanel.setBounds(0, 0, menuLayeredPane.getWidth(), menuLayeredPane.getHeight());
        
        filterPanel = new FilterFeaturesPanel();
        filterPanel.delegate = menuDelegate;
        filterPanel.setPreferredSize(menuLayeredPane.getPreferredSize());
        filterPanel.setBounds(0, 0, menuLayeredPane.getWidth(), menuLayeredPane.getHeight());
        
        menuLayeredPane.add(mainMenuPanel, 1, 0);
        menuLayeredPane.add(filterPanel, 0, 0);
        
        MouseListener plotMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                FeatureSetPlotItem f = featureSetPlotPanel.getNearest(e.getX(), e.getY());
                if(f != null)
                {
                    delegate.newFeatureSelected(f.feature);
                    featureSetPlotPanel.selectedFeature = f;
                }
            }
        };
        featureSetPlotPanel.addMouseListener(plotMouseListener);
        
        learningStateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                learningManagerPropertyChanged(evt);
            }
        };
        w.getSupervisedLearningManager().addPropertyChangeListener(learningStateListener);
    }
    
    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        
        switch (evt.getPropertyName()) {
            case SupervisedLearningManager.PROP_RECORDINGROUND:
                break;
            case SupervisedLearningManager.PROP_RECORDINGSTATE:
                if(w.getSupervisedLearningManager().getRecordingState() == RecordingState.NOT_RECORDING)
                {
                    System.out.println("recording stopped, updating plot");
                    updateFeaturePlot();
                }
                break;
            case SupervisedLearningManager.PROP_LEARNINGSTATE:
                break;
            case SupervisedLearningManager.PROP_RUNNINGSTATE:
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
    
    public void update(Wekinator w, int output)
    {
        this.w = w;
        this.outputIndex = output;
        setUpPlots();
        updateFeaturePlot();
    }
    
    public void onClose()
    {
        w.getSupervisedLearningManager().removePropertyChangeListener(learningStateListener);
    }

    public void newFeatureSelected(Feature ft)
    {
        delegate.newFeatureSelected(ft);
    }
    
    public void addFeature(Feature ft)
    {
        if(w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING)
        {
            if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
            {
                ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeatureAdded(w);
            }
            w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).addFeatureForKey(ft.name);
            delegate.featureListUpdated();
        }
        else
        {
            Object[] options = {"Stop Running","OK"};
            int n = JOptionPane.showOptionDialog(null,
                "Cannot edit features whilst Running",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     
                options,  
                options[0]); 
            if(n ==0)
            {
                new WekinatorSupervisedLearningController(w.getSupervisedLearningManager(),w).stopRun();
            }
        }
    }
    
    public void blockInteraction(boolean block, boolean fromDelegate)
    {
        if(block)
        {
            featureSetPlotPanel.showLoading();
        }
        else
        {
            featureSetPlotPanel.hideLoading();
        }
        if(!fromDelegate)
        {
            delegate.blockInteraction(block);
        }
    }
    
    public void featureListUpdated()
    {
        updateFeaturePlot();
    }
    
    public void updateFilters()
    {
        SwingWorker worker = new SwingWorker<Feature[] ,Void>()
        {   
            Feature[] f;
            String[] sf;

            @Override
            public Feature[]  doInBackground()
            {
                ArrayList<String> selectedFilters = filterPanel.getSelectedFilters();
                sf = new String[selectedFilters.size()];
                sf = selectedFilters.toArray(sf);
                if(sf.length > 0)
                {
                    f = w.getDataManager().featureManager.getAllFeaturesGroup().getFeaturesForTags(sf, false);
                }
                else
                {
                    f = new Feature[0];
                }
                return f;
            }

            @Override
            public void done()
            {
                if(sf.length > 0)
                {
                    if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
                    {
                        ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeatureTagSearch(w, sf, f);
                    }
                }
                selected = f;
                updateFeaturePlot();
            }
        };
        worker.execute();
    }
    
    private void updateFeaturePlot()
    {
        ArrayList<FeatureSetPlotItem> items = new ArrayList();
        Feature[] currentSet = w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).getCurrentFeatures();
        System.out.println("update feature plot :" + updatingRankings);
        SwingWorker worker = new SwingWorker<HashMap<String, Integer> ,Void>()
        {  
            HashMap<String, Integer> rankingSet;

            @Override
            public HashMap<String, Integer>  doInBackground()
            {
                System.out.println("update feature plot worker begin");
                rankingSet = w.getDataManager().getInfoGainRankings(outputIndex);
                return rankingSet;
            }
            
            @Override
            public void done()
            {
                System.out.println("thread done isCancelled? " + isCancelled());
                for(Feature inSet:currentSet)
                {
                    Boolean matched = false;
                    for(Feature result:selected)
                    {
                        if(result.name.equals(inSet.name))
                        {
                            matched = true;
                            break;
                        }
                    }
                    FeatureSetPlotItem item = new FeatureSetPlotItem();
                    item.feature = inSet;
                    item.isInSet = true;
                    item.isSelected = matched;
                    try {
                        item.ranking = rankingSet.get(inSet.name+":0:0");
                    } 
                    catch(NullPointerException e)
                    {
                        System.out.println("cannot find name (currentSet) " + inSet.name);
                    }
                    items.add(item);
                }

                for(Feature result:selected)
                {
                    Boolean matched = false;
                    for(FeatureSetPlotItem item:items)
                    {
                        try {
                        if(result.name.equals(item.feature.name))
                        {
                            matched = true;
                            break;
                        }
                        } catch(NullPointerException e)
                        {
                            System.out.println(e);
                        }
                    }
                    if(!matched)
                    {
                        FeatureSetPlotItem item = new FeatureSetPlotItem();
                        item.feature = result;
                        item.isInSet = false;
                        item.isSelected = true;
                        try {
                            item.ranking = rankingSet.get(result.name+":0:0");
                        } 
                        catch(NullPointerException e)
                        {
                            System.out.println("cannot find name (selected) " + result.name);
                        }
                        items.add(item);
                    }
                }
                FeatureSetPlotItem[] f = new FeatureSetPlotItem[items.size()];
                f = items.toArray(f);
                featureSetPlotPanel.update(f, w.getDataManager().featureManager.getFeatureNames().length);
                updatingRankings = false;
                delegate.blockInteraction(false);
            }
        };
        if(!updatingRankings)
        {
            updatingRankings = true;
            try {
                delegate.blockInteraction(true);
                worker.execute();
            } 
            catch (Exception e)
            {
                delegate.blockInteraction(false);
                System.out.println("EXCEPTION " + e);
            }
            
        }
        else
        {
            System.out.println("BLOCKED from updating rankings");
        }
    }
    
    private void selectThresh(boolean above)
    {
        delegate.blockInteraction(true);
        selected = w.getDataManager().getInfoGainRankings(outputIndex, threshold, above);
        delegate.blockInteraction(false);
        updateFeaturePlot();
    }
    
    private void selectAll()
    {
        delegate.blockInteraction(true);
        selected = w.getDataManager().featureManager.getAllFeaturesGroup().getCurrentFeatures();
        updateFeaturePlot();
    }
    
    private void updateThreshold(double features)
    {
        double max = w.getDataManager().featureManager.getFeatureNames().length;
        threshold = features / max;
        int sliderVal = (int)((1.0d - threshold) * 100.0d);
        infoFilterSlider.setValue(sliderVal);
    }
    
    private void autoSelect()
    {
        if(w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING)
        {
            delegate.blockInteraction(true);
            w.getDataManager().setFeaturesForBestInfo(outputIndex, false,  new BestInfoSelector.BestInfoResultsReceiver() {
                @Override
                public void finished(int[] features)
                {
                   updateThreshold(features.length);
                   updateFeaturePlot();
                }
            });
        }
        else
        {
            Object[] options = {"Stop Running","OK"};
            int n = JOptionPane.showOptionDialog(null,
                "Cannot edit features whilst Running",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     
                options,  
                options[0]); 
            if(n ==0)
            {
                new WekinatorSupervisedLearningController(w.getSupervisedLearningManager(),w).stopRun();
            }
        }
    }
    
    private void handleSetChange(Boolean remove)
    {
        if(w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING)
        {
            for(Feature f : selected)
            {
                if(remove)
                {
                    w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).removeFeatureForKey(f.name);
                    if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
                    {
                        ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeatureRemoved(w);
                    }
                    
                }
                else
                {
                    w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).addFeatureForKey(f.name);
                    if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
                    {
                        ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeatureAdded(w);
                    }
                }
            }
            selected = new Feature[0];
            //availableFiltersTable.repaint();
            delegate.featureListUpdated();
        }
        else
        {
            Object[] options = {"Stop Running","OK"};
            int n = JOptionPane.showOptionDialog(null,
                "Cannot edit features whilst Running",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     
                options,  
                options[0]); 
            if(n ==0)
            {
                new WekinatorSupervisedLearningController(w.getSupervisedLearningManager(),w).stopRun();
            }
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

        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        testSetFrame1 = new wekimini.gui.TestSetFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        infoFilterSlider = new javax.swing.JSlider();
        featureSetPlotPanel = new wekimini.gui.FeatureSetPlotPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        menuLayeredPane = new javax.swing.JLayeredPane();

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable2);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        infoFilterSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                infoFilterSliderStateChanged(evt);
            }
        });
        infoFilterSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                infoFilterSliderPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout featureSetPlotPanelLayout = new javax.swing.GroupLayout(featureSetPlotPanel);
        featureSetPlotPanel.setLayout(featureSetPlotPanelLayout);
        featureSetPlotPanelLayout.setHorizontalGroup(
            featureSetPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        featureSetPlotPanelLayout.setVerticalGroup(
            featureSetPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel2.setText("Low Info");

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel3.setText("High Info");

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Your  Features");

        javax.swing.GroupLayout menuLayeredPaneLayout = new javax.swing.GroupLayout(menuLayeredPane);
        menuLayeredPane.setLayout(menuLayeredPaneLayout);
        menuLayeredPaneLayout.setHorizontalGroup(
            menuLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 681, Short.MAX_VALUE)
        );
        menuLayeredPaneLayout.setVerticalGroup(
            menuLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 283, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3))
                    .addComponent(infoFilterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(menuLayeredPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menuLayeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoFilterSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void infoFilterSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_infoFilterSliderPropertyChange
        // TODO add your handling code here:
        
    
        
    }//GEN-LAST:event_infoFilterSliderPropertyChange

    private void infoFilterSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_infoFilterSliderStateChanged
        // TODO add your handling code here:
        threshold = 1.0f - ((double)infoFilterSlider.getValue() / 100.0f);
        featureSetPlotPanel.updateThreshold(threshold);
    }//GEN-LAST:event_infoFilterSliderStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private wekimini.gui.FeatureSetPlotPanel featureSetPlotPanel;
    private javax.swing.JSlider infoFilterSlider;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JLayeredPane menuLayeredPane;
    private wekimini.gui.TestSetFrame testSetFrame1;
    // End of variables declaration//GEN-END:variables
}
