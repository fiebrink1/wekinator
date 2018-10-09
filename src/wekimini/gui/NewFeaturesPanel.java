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
import wekimini.Wekinator;
import wekimini.WekinatorSupervisedLearningController;
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

    public NewFeaturesPanel() {
        initComponents();
        availableFiltersTable.setTableHeader(null);
    }

    private void setUpPlots() 
    {
        featureSetPlotPanel.setDimensions(500, 200);
        availableFiltersTable.setModel(new FiltersTableModel(w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).getTags()));
        availableFiltersTable.setDefaultRenderer(String.class, new FiltersTableRenderer());
        MouseListener tableMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = availableFiltersTable.rowAtPoint(e.getPoint());
                int col = availableFiltersTable.columnAtPoint(e.getPoint());
                String tag = (String)availableFiltersTable.getModel().getValueAt(row, col);
                if(selectedFilters.contains(tag))
                {
                    selectedFilters.remove(tag);
                }
                else
                {
                    selectedFilters.add(tag);
                }
                updateFilters();
            }
        };
        availableFiltersTable.addMouseListener(tableMouseListener);
        
        MouseListener plotMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Feature f = featureSetPlotPanel.getNearest(e.getX(), e.getY());
                if(f != null)
                {
                    delegate.newFeatureSelected(f);
                }
            }
        };
        featureSetPlotPanel.addMouseListener(plotMouseListener);
    }
    
    public void update(Wekinator w, int output)
    {
        this.w = w;
        this.outputIndex = output;
        setUpPlots();
        List<Feature> features = w.getDataManager().featureManager.getAllFeaturesGroup().getLibrary();
        Feature[] f = new Feature[features.size()];
        f = features.toArray(f);
        selected = f;
        updateFeaturePlot();
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
            w.getDataManager().featureListUpdated();
            w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).addFeatureForKey(ft.name);
            delegate.featureListUpdated();
            updateFeaturePlot();
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
    
    private Feature[] removeAddedFeatures(Feature[] results)
    {
        Feature[] added = w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).getCurrentFeatures();
        ArrayList<Feature> filteredList = new ArrayList<>();
        for(Feature toShow:results)
        {
            boolean remove = false;
            for(Feature match:added)
            {
                if(match.equals(toShow))
                {
                    remove = true;
                    break;
                }
            }
            if(!remove)
            {
                filteredList.add(toShow);
            }
        }
        Feature[] newResults = new Feature[filteredList.size()];
        return filteredList.toArray(newResults);
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
        SwingWorker worker = new SwingWorker<HashMap<String, Integer> ,Void>()
        {  
            HashMap<String, Integer> rankingSet;

            @Override
            public HashMap<String, Integer>  doInBackground()
            {
                featureSetPlotPanel.showLoading();
                rankingSet = w.getDataManager().getInfoGainRankings(outputIndex);
                return rankingSet;
            }
            
            @Override
            public void done()
            {
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
                    item.ranking = rankingSet.get(inSet.name+":0:0");
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
                        item.ranking = rankingSet.get(result.name+":0:0");
                        items.add(item);
                    }
                }
                FeatureSetPlotItem[] f = new FeatureSetPlotItem[items.size()];
                f = items.toArray(f);
                featureSetPlotPanel.hideLoading();
                featureSetPlotPanel.update(f);
            }
        };
        worker.execute();
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
            w.getDataManager().featureListUpdated();
            delegate.featureListUpdated();
            updateFeaturePlot();
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
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
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
        selectAllButton = new javax.swing.JButton();
        clearSelectButton = new javax.swing.JButton();
        infoFilterSlider = new javax.swing.JSlider();
        javax.swing.JButton selectAllAboveButton = new javax.swing.JButton();
        selectAllBelowButton = new javax.swing.JButton();
        featureSetPlotPanel = new wekimini.gui.FeatureSetPlotPanel();

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

        jLabel1.setText("Select Filters");

        addSelectedButton.setText("Add Selected Features");
        addSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedButtonActionPerformed(evt);
            }
        });

        removeSelectedButton.setText("Remove Selected Features");
        removeSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedButtonActionPerformed(evt);
            }
        });

        selectAllButton.setText("Select All Features");
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        clearSelectButton.setText("Clear Selection");
        clearSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelectButtonActionPerformed(evt);
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

        selectAllAboveButton.setText("<html>Select All Above Threshold</html>");
        selectAllAboveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllAboveButtonActionPerformed(evt);
            }
        });

        selectAllBelowButton.setText("<html>Select All Below Threshold</html>");
        selectAllBelowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllBelowButtonActionPerformed(evt);
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
            .addGap(0, 239, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(selectAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectAllAboveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(addSelectedButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(removeSelectedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(selectAllBelowButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearSelectButton))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
                        .addComponent(infoFilterSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoFilterSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addSelectedButton)
                    .addComponent(removeSelectedButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearSelectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(selectAllAboveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(selectAllBelowButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        // TODO add your handling code here:
        selected = w.getDataManager().featureManager.getAllFeaturesGroup().getCurrentFeatures();
        updateFeaturePlot();
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void clearSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelectButtonActionPerformed
        // TODO add your handling code here:
        selected = new Feature[0];
        updateFeaturePlot();
    }//GEN-LAST:event_clearSelectButtonActionPerformed

    private void infoFilterSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_infoFilterSliderPropertyChange
        // TODO add your handling code here:
        
    
        
    }//GEN-LAST:event_infoFilterSliderPropertyChange

    private void selectAllAboveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllAboveButtonActionPerformed
        // TODO add your handling code here:
        selected = w.getDataManager().getInfoGainRankings(outputIndex, threshold, true);
        updateFeaturePlot();
    }//GEN-LAST:event_selectAllAboveButtonActionPerformed

    private void selectAllBelowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllBelowButtonActionPerformed
        // TODO add your handling code here:
        selected = w.getDataManager().getInfoGainRankings(outputIndex, threshold, false);
        updateFeaturePlot();
    }//GEN-LAST:event_selectAllBelowButtonActionPerformed

    private void infoFilterSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_infoFilterSliderStateChanged
        // TODO add your handling code here:
        threshold = 1.0f - ((double)infoFilterSlider.getValue() / 100.0f);
        System.out.println("threshold:"+threshold + " value:" + infoFilterSlider.getValue());
    }//GEN-LAST:event_infoFilterSliderStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSelectedButton;
    private javax.swing.JTable availableFiltersTable;
    private javax.swing.JButton clearSelectButton;
    private wekimini.gui.FeatureSetPlotPanel featureSetPlotPanel;
    private javax.swing.JSlider infoFilterSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JButton removeSelectedButton;
    private javax.swing.JButton selectAllBelowButton;
    private javax.swing.JButton selectAllButton;
    private wekimini.gui.TestSetFrame testSetFrame1;
    // End of variables declaration//GEN-END:variables
}
