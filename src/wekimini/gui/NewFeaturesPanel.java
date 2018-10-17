/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
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
    private ArrayList<String> selectedFilters = new ArrayList();
    protected Feature[] selected = new Feature[0];
    public FeatureEditorDelegate delegate;
    private int outputIndex = 0;
    private double threshold = 0.5;
    public Boolean updatingRankings = false;
    private PropertyChangeListener learningStateListener;
    private String highlightedTag = "";

    public NewFeaturesPanel() {
        initComponents();
        availableFiltersTable.setTableHeader(null);
    }

    private void setUpPlots() 
    {
        featureSetPlotPanel.setDimensions(596, 200);
        availableFiltersTable.setRowHeight(140/4);
        
        String[] combined = new String[] {"All", "Above Thresh", "Below Thresh", "None", "Accelerometer", "Gyroscope",
                "AccelerometerX", "AccelerometerY", "AccelerometerZ", "GyroscopeX", "GyroscopeY", "GyroscopeZ", 
                "Raw", "Mean", "1st Order Diff", "FFT", "Max", "Min",
                "Energy", "IQR", "Correlation", "Standard Deviation", "Magnitude"
        };
        availableFiltersTable.setModel(new FiltersTableModel(combined));
        availableFiltersTable.setDefaultRenderer(String.class, new FiltersTableRenderer());
        
        MouseMotionListener tableMouseMotionListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = availableFiltersTable.rowAtPoint(e.getPoint());
                int col = availableFiltersTable.columnAtPoint(e.getPoint());
                highlightedTag = (String)availableFiltersTable.getModel().getValueAt(row, col);
                //System.out.println("hover on " + highlightedTag);
                availableFiltersTable.repaint();
            }
        };
        availableFiltersTable.addMouseMotionListener(tableMouseMotionListener);
        
        MouseListener tableMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = availableFiltersTable.rowAtPoint(e.getPoint());
                int col = availableFiltersTable.columnAtPoint(e.getPoint());
                String tag = (String)availableFiltersTable.getModel().getValueAt(row, col);
                if(tag.equals("All"))
                {
                    selectAll();
                }
                else if(tag.equals("Above Thresh"))
                {
                    selectThresh(true);
                }
                else if(tag.equals("Below Thresh"))
                {
                    selectThresh(false);
                }
                else if(tag.equals("None"))
                {
                    clearSelection();
                }
                else if(selectedFilters.contains(tag))
                {
                    selectedFilters.remove(tag);
                    updateFilters();
                }
                else
                {
                    selectedFilters.add(tag);
                    updateFilters();
                }
            }
        };
        availableFiltersTable.addMouseListener(tableMouseListener);
        
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
        addSelectedButton.setEnabled(!block);
        bestInfoButton.setEnabled(!block);
        removeSelectedButton.setEnabled(!block);
//        selectAllBelowButton.setEnabled(!block);
//        selectAllButton.setEnabled(!block);
//        clearSelectionButton.setEnabled(!block);
//        selectAllAboveButton.setEnabled(!block);
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
        availableFiltersTable.repaint();
        SwingWorker worker = new SwingWorker<Feature[] ,Void>()
        {   
            Feature[] f;
            String[] sf;

            @Override
            public Feature[]  doInBackground()
            {
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
    
    public void refreshResultsTable()
    {
        selected = w.getDataManager().featureManager.getAllFeaturesGroup().getFeaturesForFeatures(selected);
        updateFeaturePlot();
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
                featureSetPlotPanel.update(f);
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
    
    private void clearSelection()
    {
        selected = new Feature[0];
        selectedFilters.clear();
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
                   clearSelection();
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
            selectedFilters.clear();
            availableFiltersTable.repaint();
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
    
    class FiltersTableModel extends AbstractTableModel
    {
        private String[] tags;
        
        public FiltersTableModel(String[] tags)
        {
            this.tags = tags;
        }

        @Override
        public int getRowCount() {
            return (int)Math.ceil((double)tags.length/(double)getColumnCount());
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getValueAt(int rowIndex, int columnIndex) {
            int r = rowIndex * getColumnCount();
            int c = columnIndex % getColumnCount();
            int index = c + r;
            return index < tags.length ? tags[index] : "";
        }
        
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
    }
    
    public class FiltersTableRenderer extends JLabel implements TableCellRenderer 
    {
        FiltersTableRenderer()
        {
            setOpaque(true);
        }

        @Override
        public JLabel getTableCellRendererComponent(
                        JTable table, Object value,
                        boolean isSelected, boolean hasFocus,
                        int row, int column) {
            String tag = (String)value;
            FiltersTableModel model = (FiltersTableModel)table.getModel();
            Color c = FeatureSetPlotPanel.colorForTag(tag, false);
            setBackground(selectedFilters.contains(tag) ? Color.DARK_GRAY : c);
            setForeground(selectedFilters.contains(tag) ? c : Color.DARK_GRAY);
            setBorder(BorderFactory.createEmptyBorder(4, 3, 4, 1));
            if(tag.equals(highlightedTag))
            {
                setBorder(BorderFactory.createLineBorder(Color.black));
            }
            setText(tag);
            return this;
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
        jScrollPane2 = new javax.swing.JScrollPane();
        availableFiltersTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        addSelectedButton = new javax.swing.JButton();
        removeSelectedButton = new javax.swing.JButton();
        infoFilterSlider = new javax.swing.JSlider();
        featureSetPlotPanel = new wekimini.gui.FeatureSetPlotPanel();
        bestInfoButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

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

        availableFiltersTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(availableFiltersTable);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Use These Filters to Highlight Features");

        addSelectedButton.setText("Use Highlighted Features");
        addSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedButtonActionPerformed(evt);
            }
        });

        removeSelectedButton.setText("Remove Highlighted Features");
        removeSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedButtonActionPerformed(evt);
            }
        });

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

        bestInfoButton.setText("Auto Select");
        bestInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bestInfoButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel2.setText("Low Info");

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel3.setText("High Info");

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Your  Features");

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Then Update Your Chosen Features");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3))
                            .addComponent(infoFilterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(addSelectedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSelectedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bestInfoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoFilterSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addSelectedButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeSelectedButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bestInfoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedButtonActionPerformed
        // TODO add your handling code here:
        handleSetChange(true);
        
    }//GEN-LAST:event_removeSelectedButtonActionPerformed

    private void addSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectedButtonActionPerformed
        // TODO add your handling code here:
        handleSetChange(false);
    }//GEN-LAST:event_addSelectedButtonActionPerformed

    private void infoFilterSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_infoFilterSliderPropertyChange
        // TODO add your handling code here:
        
    
        
    }//GEN-LAST:event_infoFilterSliderPropertyChange

    private void infoFilterSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_infoFilterSliderStateChanged
        // TODO add your handling code here:
        threshold = 1.0f - ((double)infoFilterSlider.getValue() / 100.0f);
        featureSetPlotPanel.updateThreshold(threshold);
    }//GEN-LAST:event_infoFilterSliderStateChanged

    private void bestInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bestInfoButtonActionPerformed
        // TODO add your handling code here:
        autoSelect();
    }//GEN-LAST:event_bestInfoButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSelectedButton;
    private javax.swing.JTable availableFiltersTable;
    private javax.swing.JButton bestInfoButton;
    private wekimini.gui.FeatureSetPlotPanel featureSetPlotPanel;
    private javax.swing.JSlider infoFilterSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JButton removeSelectedButton;
    private wekimini.gui.TestSetFrame testSetFrame1;
    // End of variables declaration//GEN-END:variables
}
