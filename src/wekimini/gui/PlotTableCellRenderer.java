/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.TableCellRenderer;
import weka.core.Instance;
import wekimini.Wekinator;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class PlotTableCellRenderer extends PlotPanel implements TableCellRenderer {

    public PlotTableCellRenderer(int w, int h) {
        super(w, h, 10);
    }
    
    @Override
    public PlotPanel getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) 
    {
        Feature ft = (Feature)value;
        PlottedFeatureTableModel tableModel = (PlottedFeatureTableModel) table.getModel();
        this.updateModel(tableModel.getModel(ft));
        return this;
    }
    
}
