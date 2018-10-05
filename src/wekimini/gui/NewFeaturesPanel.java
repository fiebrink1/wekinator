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
public class NewFeaturesPanel extends javax.swing.JPanel implements WekiTokenFieldDelegate {
    
    private Wekinator w;
    private ArrayList<String> selectedFilters = new ArrayList();
    protected Feature[] currentResults = new Feature[0];
    public FeatureEditorDelegate delegate;
    private int selectedRow = -1;
    private int outputIndex = 0;
    public boolean searchCurrent = false;

    public NewFeaturesPanel() {
        initComponents();
                                                                     
    }

    private void setUpFeatureSetPlot() 
    {

    }
    
    @Override
    public void onTokenPressed(String token)
    {
        selectedFilters.remove(token);
        updateFilters();
    }
    
    public void update(Wekinator w, int output)
    {
        this.w = w;
        this.outputIndex = output;

        
        List<Feature> features = w.getDataManager().featureManager.getAllFeaturesGroup().getLibrary();
        Feature[] f = new Feature[features.size()];
        f = features.toArray(f);
        updateFeaturePlot(f);
        
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
            updateFeaturePlot(currentResults);
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
        updateFeaturePlot(currentResults);
    }
    
    public void searchForFeature(String searchTerm)
    {
        if(!searchTerm.isEmpty())
        {
            SwingWorker worker = new SwingWorker<Feature[] ,Void>()
            {     
                Feature[] f;
                
                @Override
                public Feature[]  doInBackground()
                {
                    f = w.getDataManager().featureManager.getAllFeaturesGroup().getFeaturesForKeyword(searchTerm, false);
                    return f;
                }

                @Override
                public void done()
                {
                    if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
                    {
                        ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeatureSearch(w, searchTerm, f);
                    }
                    updateFeaturePlot(f);
                }
            };
            worker.execute();
        }
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
                sf = new String[selectedFilters.size()];
                sf = selectedFilters.toArray(sf);
                if(sf.length > 0)
                {
                    if(searchCurrent)
                    {
                        f = w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).getFeaturesForTags(sf, searchCurrent);  
                    }
                    else
                    {
                        f = w.getDataManager().featureManager.getAllFeaturesGroup().getFeaturesForTags(sf, searchCurrent);
                    } 
                }
                else
                {
                    List<Feature> allFeatures = w.getDataManager().featureManager.getAllFeaturesGroup().getLibrary();
                    f = new Feature[allFeatures.size()];
                    f = allFeatures.toArray(f);
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
                updateFeaturePlot(f);
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
        updateFeaturePlot(w.getDataManager().featureManager.getAllFeaturesGroup().getFeaturesForFeatures(currentResults));
    }
    
    private void updateFeaturePlot(Feature[] results)
    {
        ArrayList<FeatureSetPlotItem> items = new ArrayList();
        Feature[] currentSet = w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).getCurrentFeatures();
        for(Feature inSet:currentSet)
        {
            Boolean matched = false;
            for(Feature result:results)
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
            item.ranking = w.getDataManager().infoRankNames[outputIndex].get(inSet.name);
            items.add(item);
        }
        
        for(Feature result:results)
        {
            Boolean matched = false;
            for(FeatureSetPlotItem item:items)
            {
                if(result.name.equals(item.feature.name))
                {
                    matched = true;
                    break;
                }
            }
            if(!matched)
            {
                FeatureSetPlotItem item = new FeatureSetPlotItem();
                item.feature = result;
                item.isInSet = false;
                item.isSelected = false;
                item.ranking = w.getDataManager().infoRankNames[outputIndex].get(result.name);
                items.add(item);
            }
        }
        FeatureSetPlotItem[] f = new FeatureSetPlotItem[items.size()];
        f = items.toArray(f);
        featureSetPlotPanel.update(f);
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
            return 2;
        }

        @Override
        public String getValueAt(int rowIndex, int columnIndex) {
            int r = rowIndex * getColumnCount();
            int c = columnIndex % getColumnCount();
            //System.out.println("rowIndex:"+rowIndex+" columnIndex:" + columnIndex + " r:" + r + " c:" + c + " index:" + (r+c));
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
            setBackground(selectedFilters.contains(tag) ? Color.DARK_GRAY  :Color.WHITE);
            setForeground(selectedFilters.contains(tag) ? Color.WHITE : Color.DARK_GRAY);
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        javax.swing.GroupLayout featureSetPlotPanelLayout = new javax.swing.GroupLayout(featureSetPlotPanel);
        featureSetPlotPanel.setLayout(featureSetPlotPanelLayout);
        featureSetPlotPanelLayout.setHorizontalGroup(
            featureSetPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 575, Short.MAX_VALUE)
        );
        featureSetPlotPanelLayout.setVerticalGroup(
            featureSetPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 257, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(61, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(featureSetPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(154, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private wekimini.gui.FeatureSetPlotPanel featureSetPlotPanel;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable2;
    private wekimini.gui.TestSetFrame testSetFrame1;
    // End of variables declaration//GEN-END:variables
}
