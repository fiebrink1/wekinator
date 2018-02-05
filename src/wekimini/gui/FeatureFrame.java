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
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import wekimini.Wekinator;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class FeatureFrame extends javax.swing.JFrame implements FeatureEditorDelegate {
    
    private Wekinator w;
    /**
     * Creates new form FeatureFrame
     */
    public FeatureFrame() {
        initComponents();
    }
    
    public FeatureFrame(Wekinator w) {
        initComponents();
        this.w = w;
        newFeaturesPanel.update(w);
        newFeaturesPanel.delegate = this;
        currentFeaturesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        currentFeaturesTable.setDefaultRenderer(Feature.class, new CurrentFeaturesTableRenderer());
        currentFeaturesTable.setTableHeader(null);
        //currentFeaturesTable.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        updateCurrentFeaturesTable();
        MouseListener featuresMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = currentFeaturesTable.rowAtPoint(e.getPoint());
                int column = currentFeaturesTable.columnAtPoint(e.getPoint());
                System.out.println("row:" + row + " column:" + column);
                Feature ft = (Feature)currentFeaturesTable.getModel().getValueAt(row, 0);
                switch(column)
                {
                    case 0: newFeatureSelected(ft); break;
                    case 1: removeFeature(ft); break;
                }
            }
        };
        currentFeaturesTable.addMouseListener(featuresMouseListener);
    }
    
    private void removeFeature(Feature ft)
    {
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeFeatureForKey(ft.name);
        newFeaturesPanel.featureListUpdated();
        updateCurrentFeaturesTable();
    }
    
    private void resizeColumns() {
        int tW = currentFeaturesTable.getWidth();
        TableColumn column;
        float[] columnWidthPercentage = {0.75f, 0.15f};
        TableColumnModel jTableColumnModel = currentFeaturesTable.getColumnModel();
        int cantCols = jTableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++) {
            column = jTableColumnModel.getColumn(i);
            int pWidth = Math.round(columnWidthPercentage[i] * tW);
            column.setPreferredWidth(pWidth);
        }
    }
    
    public void updateCurrentFeaturesTable()
    {
        Feature[] ft = w.getDataManager().featureManager.getFeatureGroups().get(0).getCurrentFeatures();
        currentFeaturesTable.setModel(new CurrentFeaturesTableModel(ft));
        currentFeaturesTable.setRowSelectionAllowed(false);
        currentFeaturesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resizeColumns();
    }
    
    class CurrentFeaturesTableModel extends AbstractTableModel
    {
        private Feature[] f;
        
        public CurrentFeaturesTableModel(Feature[] f)
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
            return "Remove";
        }
        
        @Override 
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
    }
    
    public class CurrentFeaturesTableRenderer extends JLabel implements TableCellRenderer 
    {
        CurrentFeaturesTableRenderer()
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
                CurrentFeaturesTableModel model = (CurrentFeaturesTableModel)table.getModel();
                setText(f.name);
            }
            else
            {
               setText("Remove"); 
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

        newFeaturesPanel = new wekimini.gui.NewFeaturesPanel();
        jPanel1 = new javax.swing.JPanel();
        currentFeaturesLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        currentFeaturesTable = new javax.swing.JTable();
        featureDetailPanel = new wekimini.gui.FeatureDetailPanel();
        evaluateFeaturesPanel1 = new wekimini.gui.EvaluateFeaturesPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        currentFeaturesLabel.setText("Current Features");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(currentFeaturesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(currentFeaturesLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        currentFeaturesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        currentFeaturesTable.setPreferredSize(new java.awt.Dimension(140, 430));
        currentFeaturesTable.setRowSelectionAllowed(false);
        currentFeaturesTable.setShowHorizontalLines(false);
        currentFeaturesTable.setShowVerticalLines(false);
        jScrollPane1.setViewportView(currentFeaturesTable);

        javax.swing.GroupLayout featureDetailPanelLayout = new javax.swing.GroupLayout(featureDetailPanel);
        featureDetailPanel.setLayout(featureDetailPanelLayout);
        featureDetailPanelLayout.setHorizontalGroup(
            featureDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        featureDetailPanelLayout.setVerticalGroup(
            featureDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout evaluateFeaturesPanel1Layout = new javax.swing.GroupLayout(evaluateFeaturesPanel1);
        evaluateFeaturesPanel1.setLayout(evaluateFeaturesPanel1Layout);
        evaluateFeaturesPanel1Layout.setHorizontalGroup(
            evaluateFeaturesPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );
        evaluateFeaturesPanel1Layout.setVerticalGroup(
            evaluateFeaturesPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newFeaturesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(evaluateFeaturesPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(featureDetailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(newFeaturesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(evaluateFeaturesPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(featureDetailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FeatureFrame().setVisible(true);
            }
        });
    }
    
    public void newFeatureSelected(Feature ft)
    {
        featureDetailPanel.setFeature(ft);
    }
    
    public void featureListUpdated()
    {
        updateCurrentFeaturesTable();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel currentFeaturesLabel;
    private javax.swing.JTable currentFeaturesTable;
    private wekimini.gui.EvaluateFeaturesPanel evaluateFeaturesPanel1;
    private wekimini.gui.FeatureDetailPanel featureDetailPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private wekimini.gui.NewFeaturesPanel newFeaturesPanel;
    // End of variables declaration//GEN-END:variables
}
