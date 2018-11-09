/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import wekimini.modifiers.Feature;


/**
 *
 * @author louismccallum
 */
public class FilterFeaturesPanel extends javax.swing.JPanel {
    
    public FeatureFilterDelegate delegate;
    private String highlightedTag = "";
    public String selectedInputFilter = NO_INPUT;
    public String selectedOperationFilter = NO_INPUT;
    private FilterPanelState state = FilterPanelState.NONE;
    private boolean blocked = false;
    public enum FilterPanelState {
        ADDING, REMOVING, EXPLORING, NONE
    };
    private static final String NO_INPUT = "none";
    private int numFeaturesSelected = 0;

    public FilterFeaturesPanel() {
        initComponents();
        setUpTables();
    }
    
    public void setUpTables()
    {
        MouseListener inputTableMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(!blocked)
                {
                    JTable source = (JTable)e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int col = source.columnAtPoint(e.getPoint());
                    String tag = (String)source.getModel().getValueAt(row, col);
                    if(tag.equals(selectedInputFilter))
                    {
                        selectedInputFilter = NO_INPUT; 
                    }
                    else
                    {
                        selectedInputFilter = tag;
                    }
                    filtersUpdated();
                    source.repaint();
                }
            }
        };
        
        MouseListener operationTableMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(!blocked)
                {
                    JTable source = (JTable)e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int col = source.columnAtPoint(e.getPoint());
                    String tag = (String)source.getModel().getValueAt(row, col);
                    if(tag.equals(selectedOperationFilter))
                    {
                        selectedOperationFilter = NO_INPUT; 
                    }
                    else
                    {
                        selectedOperationFilter = tag;
                    }
                    filtersUpdated();
                    source.repaint();
                }
            }
        };
        
        MouseMotionListener tableMouseMotionListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if(!blocked)
                {
                    JTable source = (JTable)e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int col = source.columnAtPoint(e.getPoint());
                    highlightedTag = (String)source.getModel().getValueAt(row, col);
                    source.repaint();
                }
            }
        };
        
        inputFiltersTable.setTableHeader(null);
        operationFiltersTable.setTableHeader(null);
        
        inputFiltersTable.setRowHeight(140/4);
        operationFiltersTable.setRowHeight(140/4);
        
        String[] inputs = new String[] {"Accelerometer", "Gyroscope",
                "AccelerometerX", "AccelerometerY", "AccelerometerZ", "GyroscopeX", "GyroscopeY", "GyroscopeZ"
        };
        inputFiltersTable.setModel(new FiltersTableModel(inputs));
        inputFiltersTable.setDefaultRenderer(String.class, new FiltersTableRenderer());
        inputFiltersTable.addMouseMotionListener(tableMouseMotionListener);
        inputFiltersTable.addMouseListener(inputTableMouseListener);
        inputFiltersTable.repaint();
        
        String[] operations = {
                "Raw", "Buffer", "Mean", "1st Order Diff", "FFT", "Max", "Min",
                "Energy", "IQR", "Correlation", "Standard Deviation", "Magnitude"
        };
        
        operationFiltersTable.setModel(new FiltersTableModel(operations));
        operationFiltersTable.setDefaultRenderer(String.class, new FiltersTableRenderer());
        operationFiltersTable.addMouseMotionListener(tableMouseMotionListener);
        operationFiltersTable.addMouseListener(operationTableMouseListener);
        operationFiltersTable.repaint();
                
    }
    
    private void filtersUpdated()
    {
        delegate.filtersUpdated();
    }
    
    public boolean inputFilterSelected()
    {
        return !selectedInputFilter.equals(NO_INPUT);
    }
    
    public boolean operationFilterSelected()
    {
        return !selectedOperationFilter.equals(NO_INPUT);
    }
    
    public boolean filtersSelected()
    {
        return operationFilterSelected() || inputFilterSelected();
    }
    
    public void setState(FilterPanelState state)
    {
        this.state = state;
        if(state == FilterPanelState.ADDING)
        {
            titleLabel.setText("Use The Tags to Pick The Features You Want to Use");
        }
        else if (state == FilterPanelState.REMOVING)
        {
            titleLabel.setText("Use The Tags to Pick The Features You Want to Remove");
        }
    }
    
    public FilterPanelState getState()
    {
        return state;
    }
    
    public ArrayList<String> getSelectedFilters()
    {
        ArrayList<String> sf = new ArrayList();
        if(!selectedOperationFilter.equals(NO_INPUT))
        {
            sf.add(selectedOperationFilter);
        }
        if(!selectedInputFilter.equals(NO_INPUT))
        {
            sf.add(selectedInputFilter);
        }
        return sf;
    }
    
    public void blockInteraction(boolean doBlock)
    {
        blocked = doBlock;
        selectAllButton.setEnabled(!doBlock);
        clearSelectionButton.setEnabled(!doBlock);
    }
    
    public void clearSelection()
    {
        selectedOperationFilter = NO_INPUT;
        selectedInputFilter = NO_INPUT;
        inputFiltersTable.repaint();
        operationFiltersTable.repaint();
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
            setFont(new Font("Lucida Grande", Font.BOLD, 11));
            FiltersTableModel model = (FiltersTableModel)table.getModel();
            Color textColor = FeatureSetPlotPanel.colorForTag(tag, 255);
            Color borderColor = textColor;
            Color selectedTextColor = Color.DARK_GRAY;
            if(textColor == null)
            {
                textColor = Color.DARK_GRAY;
                borderColor = new Color(245, 245, 245, 255);
                selectedTextColor = borderColor;
            }
            if(tag.equals(selectedOperationFilter) || tag.equals(selectedInputFilter))
            {
                setBackground(textColor);
                setBorder(BorderFactory.createEmptyBorder(4, 3, 4, 1));
                setForeground(selectedTextColor);
            }
            else
            {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(borderColor, 4));
                setForeground(textColor);
            }
            if(tag.equals(highlightedTag))
            {
                setBorder(BorderFactory.createLineBorder(Color.black));
            }
            setHorizontalAlignment(CENTER);
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

        jScrollPane2 = new javax.swing.JScrollPane();
        inputFiltersTable = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        operationFiltersTable = new javax.swing.JTable();
        selectAllButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        clearSelectionButton = new javax.swing.JButton();
        titleLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(704, 251));

        inputFiltersTable.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        inputFiltersTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(inputFiltersTable);

        jLabel6.setText("Input");

        jLabel7.setText("Operation");

        operationFiltersTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(operationFiltersTable);

        selectAllButton.setBackground(new java.awt.Color(204, 204, 204));
        selectAllButton.setText("All");
        selectAllButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        selectAllButton.setOpaque(true);
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        backButton.setBackground(new java.awt.Color(204, 204, 204));
        backButton.setText("Back");
        backButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backButton.setOpaque(true);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        clearSelectionButton.setBackground(new java.awt.Color(204, 204, 204));
        clearSelectionButton.setText("None");
        clearSelectionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clearSelectionButton.setOpaque(true);
        clearSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelectionButtonActionPerformed(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("Use Filters To Select Features");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 236, Short.MAX_VALUE)
                        .addComponent(selectAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearSelectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(174, 174, 174))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(backButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                            .addComponent(jScrollPane2)
                            .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(backButton)
                    .addComponent(titleLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectAllButton)
                    .addComponent(clearSelectionButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        // TODO add your handling code here:
        clearSelection();
        state = FilterPanelState.NONE;
        delegate.backPressed();
    }//GEN-LAST:event_backButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        // TODO add your handling code here:
        clearSelection();
        delegate.selectAllFeatures();
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void clearSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelectionButtonActionPerformed
        // TODO add your handling code here:
        clearSelection();
        delegate.selectNoFeatures();
    }//GEN-LAST:event_clearSelectionButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton clearSelectionButton;
    private javax.swing.JTable inputFiltersTable;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable operationFiltersTable;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
