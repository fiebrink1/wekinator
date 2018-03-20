/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import javax.swing.table.AbstractTableModel;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class FeatureTableModel extends AbstractTableModel {
       
    private Feature[] f;
    public int selectedRow = 0;

    public FeatureTableModel(Feature[] f)
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
        return f[rowIndex];
    }

    @Override 
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
