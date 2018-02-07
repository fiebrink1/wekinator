/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class FeatureTableRenderer extends JLabel implements TableCellRenderer {
        
    FeatureTableRenderer()
    {
        setOpaque(true);
    }

    @Override
    public JLabel getTableCellRendererComponent(
        JTable table, 
        Object value,
        boolean isSelected, 
        boolean hasFocus,
        int row, 
        int column) 
    {
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        setBackground(row == ((FeatureTableModel)table.getModel()).selectedRow ? Color.DARK_GRAY:Color.WHITE);
        setForeground(row != ((FeatureTableModel)table.getModel()).selectedRow ? Color.DARK_GRAY:Color.WHITE);
        if(column == 0)
        {
            Feature f = (Feature)value;
            FeatureTableModel model = (FeatureTableModel)table.getModel();
            setText(f.name);
        }
        return this;
    }
}
