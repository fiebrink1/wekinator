/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.jdesktop.swingworker.SwingWorker;
import wekimini.DataManager;
import wekimini.Wekinator;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class NewFeaturesPanel extends javax.swing.JPanel implements WekiTokenFieldDelegate {
    private Wekinator w;
    ArrayList<String> selectedFilters = new ArrayList();
    /**
     * Creates new form NewFeaturesPanel
     */
    
    public NewFeaturesPanel() {
        initComponents();
        selectedFiltersTokenField.setDelegate(this);
        resultsTable.setRowSelectionAllowed(false);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setTableHeader(null);
        availableFiltersTable.setTableHeader(null);
    }

    private void resizeColumns() {
        int tW = resultsTable.getWidth();
        TableColumn column;
        float[] columnWidthPercentage = {0.75f, 0.15f};
        TableColumnModel jTableColumnModel = resultsTable.getColumnModel();
        int cantCols = jTableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++) {
            column = jTableColumnModel.getColumn(i);
            int pWidth = Math.round(columnWidthPercentage[i] * tW);
            column.setPreferredWidth(pWidth);
        }
    }
    
    @Override
    public void onTokenPressed(String token)
    {
        selectedFilters.remove(token);
        updateFilters();
    }
    
    public void update(Wekinator w)
    {
        this.w = w;
        availableFiltersTable.setModel(new FiltersTableModel(w.getDataManager().featureManager.getFeatureGroups().get(0).getTags()));
        availableFiltersTable.setDefaultRenderer(String.class, new FiltersTableRenderer());
        MouseListener tableMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = availableFiltersTable.rowAtPoint(e.getPoint());
                int col = availableFiltersTable.columnAtPoint(e.getPoint());
                String tag = (String)availableFiltersTable.getModel().getValueAt(row, col);
                if(selectedFilters.contains(tag))
                {
                    System.out.println("Deselected " + tag);
                    selectedFilters.remove(tag);
                }
                else
                {
                    System.out.println("Selected " + tag);
                    selectedFilters.add(tag);
                }
                updateFilters();
            }
        };
        availableFiltersTable.addMouseListener(tableMouseListener);
        
        MouseListener resultsMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = resultsTable.rowAtPoint(e.getPoint());
                int column = resultsTable.columnAtPoint(e.getPoint());
                System.out.println("row:" + row + " column:" + column);
                switch(column)
                {
                    case 0: break;
                    case 1: addFeature((Feature)resultsTable.getModel().getValueAt(row, 0)); break;
                }
            }
        };
        resultsTable.addMouseListener(resultsMouseListener);
    }
    
    public void addFeature(Feature ft)
    {
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(ft.name);
    }
    
    public void updateFilters()
    {
        selectedFiltersTokenField.setTokens(selectedFilters);
        availableFiltersTable.repaint();
        SwingWorker worker = new SwingWorker<Feature[] ,Void>()
        {   
            Feature[] f;

            @Override
            public Feature[]  doInBackground()
            {
                String[] sf = new String[selectedFilters.size()];
                sf = selectedFilters.toArray(sf);
                f = w.getDataManager().featureManager.getFeatureGroups().get(0).getFeaturesForTags(sf);
                return f;
            }

            @Override
            public void done()
            {
                updateResultsTable(f);
            }
        };
        worker.execute();
    }
    
    public void updateResultsTable(Feature[] results)
    {
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsTable.setDefaultRenderer(Feature.class, new ResultsTableRenderer());
        resultsTable.setModel(new ResultsTableModel(results));
        resizeColumns();
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
            return (int)Math.ceil(tags.length/3);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getValueAt(int rowIndex, int columnIndex) {
            int r = rowIndex * getColumnCount();
            int c = columnIndex % getColumnCount();
            //System.out.println("rowIndex:"+rowIndex+" columnIndex:" + columnIndex + " r:" + r + " c:" + c + " index:" + (r+c));
            return tags[c + r];
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
            
            setText(tag);
            return this;
        }
    }
    
    class ResultsTableModel extends AbstractTableModel
    {
        private Feature[] f;
        
        public ResultsTableModel(Feature[] f)
        {
            this.f = f;
        }

        @Override
        public int getRowCount() {
            return f.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex == 0)
            {
                return f[rowIndex];
            }
            return "Add";
        }
        
        @Override 
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
//        @Override
//        public Class getColumnClass(int c) {
//            return getValueAt(0, c).getClass();
//        }
    }
    
    public class ResultsTableRenderer extends JLabel implements TableCellRenderer 
    {
        ResultsTableRenderer()
        {
            setOpaque(true);
        }

        @Override
        public JLabel getTableCellRendererComponent(
                        JTable table, Object value,
                        boolean isSelected, boolean hasFocus,
                        int row, int column) {
            setBackground(Color.WHITE);
            if(column == 0)
            {
                Feature f = (Feature)value;
                ResultsTableModel model = (ResultsTableModel)table.getModel();
                setText(f.name);
            }
            else
            {
               setText("Add"); 
            }
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
        searchBar = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        availableFiltersTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        selectedFiltersTokenField = new wekimini.gui.WekiTokenField();

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

        searchBar.setText("Search");
        searchBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBarActionPerformed(evt);
            }
        });
        searchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                searchBarKeyTyped(evt);
            }
        });

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

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(resultsTable);

        jLabel1.setText("Filters");

        jLabel2.setText("Results");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(selectedFiltersTokenField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(searchBar, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedFiltersTokenField, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchBarActionPerformed

    private void searchBarKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchBarKeyTyped
         SwingWorker worker = new SwingWorker<Feature[] ,Void>()
        {   
            Feature[] f;
            
            @Override
            public Feature[]  doInBackground()
            {
                f = w.getDataManager().featureManager.getFeatureGroups().get(0).getFeaturesForKeyword(searchBar.getText());
                return f;

            }
            
            @Override
            public void done()
            {
                updateResultsTable(f);
            }
        };
        worker.execute();
        
        
    }//GEN-LAST:event_searchBarKeyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable availableFiltersTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable resultsTable;
    private javax.swing.JTextField searchBar;
    private wekimini.gui.WekiTokenField selectedFiltersTokenField;
    // End of variables declaration//GEN-END:variables
}
