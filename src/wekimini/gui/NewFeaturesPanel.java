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
import wekimini.gui.FilterFeaturesPanel.FilterPanelState;
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
    private int showing = 0;
    private int toRemove = 0;
    private int toAdd = 0;
    
    public NewFeaturesPanel() {
        initComponents();
    }

    private void setUpPlots() 
    {
        featureSetPlotPanel.setDimensions(700 - 10, 175);
        
        FeatureFilterDelegate filterDelegate = new FeatureFilterDelegate()
        {
            @Override
            public void backPressed() {
                showMenu();
                updateFilters();
                updateLabels();
            }

            @Override
            public void filtersUpdated() {
                updateFilters();
                updateLabels();
            }
            
            @Override
            public void selectAllFeatures() {
                selectAll();
                updateLabels();
            }
            
            @Override
            public void selectNoFeatures() {
                clearAll();
                updateLabels();
            }

            @Override
            public void updateFeatures() {
                
            }
            
        };
        
        FeatureMenuDelegate menuDelegate = new FeatureMenuDelegate() {
            @Override
            public void addFeaturePressed() {
                showFilters();
                filterPanel.setState(FilterFeaturesPanel.FilterPanelState.ADDING);
                updateLabels();
                updateFeaturePlot();
            }

            @Override
            public void autoSelectPressed() {
                autoSelect();
            }
            
            @Override
            public void selectThresholdPressed(boolean above) {
                selectThresh(above);
            }

            @Override
            public void removeFeaturePressed() {
                showFilters();
                filterPanel.setState(FilterFeaturesPanel.FilterPanelState.REMOVING);
                updateFeaturePlot();
                updateLabels();
            }

            @Override
            public void exploreFeaturePressed()
            {
                showFilters();
                filterPanel.setState(FilterFeaturesPanel.FilterPanelState.EXPLORING);
                updateFeaturePlot();
                updateLabels();
            }

        };
        
        mainMenuPanel = new FeatureSelectMenuPanel();
        mainMenuPanel.delegate = menuDelegate;
        mainMenuPanel.setPreferredSize(menuLayeredPane.getPreferredSize());
        mainMenuPanel.setBounds(0, 0, menuLayeredPane.getWidth(), menuLayeredPane.getHeight());
        
        filterPanel = new FilterFeaturesPanel();
        filterPanel.delegate = filterDelegate;
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
                    selectFeature(f);
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
        updateLabels();
        showMenu();
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
    
    public void showFilters()
    {
        menuLayeredPane.setLayer(filterPanel, TOP_LAYER);
        menuLayeredPane.setLayer(mainMenuPanel, BOTTOM_LAYER);
        infoFilterSlider.setVisible(false);
        featureSetPlotPanel.showThreshold = false;
        addRemoveButton.setVisible(true);
        updateLabels();
    }
    
    public void showMenu()
    {
        filterPanel.clearSelection();
        filterPanel.setState(FilterFeaturesPanel.FilterPanelState.NONE);
        menuLayeredPane.setLayer(filterPanel, BOTTOM_LAYER);
        menuLayeredPane.setLayer(mainMenuPanel, TOP_LAYER);
        infoFilterSlider.setVisible(true);
        featureSetPlotPanel.showThreshold = true;
        addRemoveButton.setVisible(false);
        updateLabels();
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
    
    public void selectFeature(FeatureSetPlotItem f)
    {
        featureSetPlotPanel.selectedFeature = f;
        delegate.newFeatureSelected(f.feature);
    }
    
    private boolean hasTrainingData()
    {
        return w.getDataManager().getNumExamples() > 0;
    }
    
    private void updateLabels()
    {
        int numSelected = showing;
        if(filterPanel.getState() == FilterPanelState.ADDING)
        {
            numSelected = toAdd;
        } 
        else if (filterPanel.getState() == FilterPanelState.REMOVING)
        {
            numSelected = toRemove;
        } 
        
        lowInfoLabel.setVisible(hasTrainingData());
        highInfoLabel.setVisible(hasTrainingData());
        if(!hasTrainingData())
        {
            plotTitleLabel.setText("Features - Record Examples To Get Information Gain Rankings");
        }
        else if(filterPanel.getState() == FilterFeaturesPanel.FilterPanelState.EXPLORING)
        {
            plotTitleLabel.setText("Exploring Features");
        }
        else if(filterPanel.getState() == FilterFeaturesPanel.FilterPanelState.ADDING)
        {
            plotTitleLabel.setText("Adding Features");
        }
        else if(filterPanel.getState() == FilterFeaturesPanel.FilterPanelState.NONE)
        {
            plotTitleLabel.setText("Your Features");
        }
        if(filterPanel.getState() == FilterFeaturesPanel.FilterPanelState.REMOVING)
        {
            plotTitleLabel.setText("Removing Features");
        }
        if(filterPanel.getState() == FilterPanelState.EXPLORING)
        {
            addRemoveButton.setVisible(false);
        }
        
        String desc;
        if(!filterPanel.filtersSelected() && numSelected == 0)
        {
            desc = "No Features Selected";
        }
        else
        {
            desc = "Showing";
            if(filterPanel.getState() == FilterPanelState.ADDING)
            {
                desc = "Add";
            }
            else if (filterPanel.getState() == FilterPanelState.REMOVING)
            {
                desc = "Remove";
            }
            if(selected.length == w.getDataManager().featureManager.getFeatureNames().length)
            {
                desc = desc + " All Features";
            }
            else
            {
                desc = desc + " Features with ";
                if(filterPanel.inputFilterSelected())
                {
                    desc = desc + filterPanel.selectedInputFilter;
                }
                if(filterPanel.operationFilterSelected())
                {
                    for(int i = 0; i < filterPanel.selectedOperationFilters.size(); i++)
                    {
                        String f = filterPanel.selectedOperationFilters.get(i);
                        String delim = i == filterPanel.selectedOperationFilters.size() - 1 ? " and " : ", ";
                        if(filterPanel.inputFilterSelected() || i > 0)
                        {
                            desc = desc + delim;
                        }
                        desc = desc + f;
                    }
                }
                desc = desc + " (" + numSelected + ")";
            }
        }        
        addRemoveButton.setText(desc);
    }
    
    public void blockInteraction(boolean block, boolean fromDelegate)
    {
        filterPanel.blockInteraction(block);
        mainMenuPanel.blockInteraction(block, hasTrainingData());
        addRemoveButton.setEnabled(!block);
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
                    f = w.getDataManager().featureManager.getAllFeatures().getFeaturesForTags(sf, false);
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
                setSelectedFeatures(f);
                updateFeaturePlot();
            }
        };
        worker.execute();
    }
    
    private void setSelectedFeatures(Feature[] f)
    {
        updateLabels();
        selected = f;
    }
    
    private void updateFeaturePlot()
    {
        toRemove = 0;
        toAdd = 0;
        showing = 0;
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
                    if(filterPanel.getState() == FilterPanelState.REMOVING && matched)
                    {
                        item.state = FeatureSetPlotItem.FeaturePlotItemState.REMOVING;
                        toRemove++;
                    }
                    else 
                    {
                        item.state = FeatureSetPlotItem.FeaturePlotItemState.NORMAL;
                    }
                    try {
                        item.ranking = rankingSet.get(inSet.name+":0:0");
                    } 
                    catch(NullPointerException e)
                    {
                        System.out.println("cannot find name (currentSet) " + inSet.name);
                    }
                    if(filterPanel.getState() != FilterPanelState.EXPLORING)
                    {
                        items.add(item);
                    }
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
                        if(filterPanel.getState() == FilterPanelState.ADDING)
                        {
                            item.state = FeatureSetPlotItem.FeaturePlotItemState.ADDING;
                            toAdd++;
                        }
                        else 
                        {
                            item.state = FeatureSetPlotItem.FeaturePlotItemState.NORMAL;
                        }
                        try {
                            item.ranking = rankingSet.get(result.name+":0:0");
                        } 
                        catch(NullPointerException e)
                        {
                            System.out.println("cannot find name (selected) " + result.name);
                        }
                        if(filterPanel.getState() != FilterPanelState.REMOVING)
                        {
                            items.add(item);
                        }
                    }
                }
                showing = items.size();
                updateLabels();
                FeatureSetPlotItem[] f = new FeatureSetPlotItem[items.size()];
                f = items.toArray(f);
                featureSetPlotPanel.update(f, w.getDataManager().featureManager.getFeatureNames().length);
                updatingRankings = false;
                delegate.blockInteraction(false);
                if(items.size() > 0)
                {
                    //selectFeature(items.get(0));
                }
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
        if(w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING)
        {
            delegate.blockInteraction(true);
            w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).removeAll();
            setSelectedFeatures(w.getDataManager().getInfoGainRankings(outputIndex, threshold, above));
            delegate.blockInteraction(false);
            handleSetChange(false);
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
    
    private void selectAll()
    {
        delegate.blockInteraction(true);
        setSelectedFeatures(w.getDataManager().featureManager.getAllFeatures().getCurrentFeatures());
        updateFeaturePlot();
    }
    
    private void clearAll()
    {
        delegate.blockInteraction(true);
        setSelectedFeatures(new Feature[0]);
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
            setSelectedFeatures(new Feature[0]);
            showMenu();
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
        lowInfoLabel = new javax.swing.JLabel();
        highInfoLabel = new javax.swing.JLabel();
        menuLayeredPane = new javax.swing.JLayeredPane();
        addRemoveButton = new javax.swing.JButton();
        plotTitleLabel = new javax.swing.JLabel();

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
            .addGap(0, 177, Short.MAX_VALUE)
        );

        lowInfoLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        lowInfoLabel.setText("Low Info");

        highInfoLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        highInfoLabel.setText("High Info");

        javax.swing.GroupLayout menuLayeredPaneLayout = new javax.swing.GroupLayout(menuLayeredPane);
        menuLayeredPane.setLayout(menuLayeredPaneLayout);
        menuLayeredPaneLayout.setHorizontalGroup(
            menuLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        menuLayeredPaneLayout.setVerticalGroup(
            menuLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        addRemoveButton.setBackground(new java.awt.Color(0, 0, 0));
        addRemoveButton.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        addRemoveButton.setText("Add ");
        addRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRemoveButtonActionPerformed(evt);
            }
        });

        plotTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        plotTitleLabel.setText("Text");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lowInfoLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(highInfoLabel))
                    .addComponent(infoFilterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(menuLayeredPane)
                    .addComponent(plotTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menuLayeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotTitleLabel)
                .addGap(4, 4, 4)
                .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowInfoLabel)
                    .addComponent(highInfoLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoFilterSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void addRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRemoveButtonActionPerformed
        // TODO add your handling code here:
        handleSetChange(filterPanel.getState() == FilterFeaturesPanel.FilterPanelState.REMOVING);
    }//GEN-LAST:event_addRemoveButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRemoveButton;
    private wekimini.gui.FeatureSetPlotPanel featureSetPlotPanel;
    private javax.swing.JLabel highInfoLabel;
    private javax.swing.JSlider infoFilterSlider;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JLabel lowInfoLabel;
    private javax.swing.JLayeredPane menuLayeredPane;
    private javax.swing.JLabel plotTitleLabel;
    private wekimini.gui.TestSetFrame testSetFrame1;
    // End of variables declaration//GEN-END:variables
}
