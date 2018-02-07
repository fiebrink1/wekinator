/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author louismccallum
 *
 */

public class ConfusionComponent extends javax.swing.JPanel
{
    private final JTable table;
    private final JScrollPane scroll;

    ConfusionComponent()
    {
        table = new JTable();
        table.setDefaultRenderer(Integer.class, new ConfusionTableRenderer());
        setLayout(new GridLayout(1, 1));
        scroll = new JScrollPane(table);
        setPreferredSize(new Dimension(100,100));
        add(scroll);
    }

    public void setModel(int[][] matrix)
    {
        ConfusionTableModel model = new ConfusionTableModel();
        model.data = matrix;
        table.setModel(model);
        table.validate();
        scroll.setViewportView(table);
        scroll.validate();
    }

    public class ConfusionTableRenderer extends JLabel implements TableCellRenderer 
    {

        ConfusionTableRenderer()
        {
            setOpaque(true);
        }

        @Override
        public JLabel getTableCellRendererComponent(
                        JTable table, Object value,
                        boolean isSelected, boolean hasFocus,
                        int row, int column) {
            Integer val = (Integer)value;
            ConfusionTableModel model = (ConfusionTableModel)table.getModel();

            if(row > 0 && column > 0)
            {
                float error = model.error(row, column);
                if(row == column)
                {
                    setBackground(Color.getHSBColor(102.0f/255.0f, error, 240.0f/255.0f));
                }
                else
                {
                    setBackground(Color.getHSBColor(7.0f/255.0f, error, 240.0f/255.0f));
                }
            }
            else
            {
                setBackground(new Color(1.0f, 1.0f, 1.0f));
            }
            if(column == 0)
            {
                setText(((Integer)row).toString());
            }
            else
            {
                setText(val.toString());
            }
            return this;
        }
    }

    public class ConfusionTableModel extends AbstractTableModel
    {
        int[][] data;    

        public float error(int row, int column)
        {
            int sum = 0;
            for(int i = 0; i < getColumnCount(); i++)
            {
                sum += data[row][i];
            }

            return (float)data[row][column]/(float)sum;
        }

        @Override
        public String getColumnName(int column)
        {
            return ((Integer)column).toString();
        }

        @Override
        public int getColumnCount() {
            return data.length;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public Integer getValueAt(int row, int col) {
            return data[row][col];
        }

        public void setValueAt(int value, int row, int col) {
            data[row][col] = value;
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;        
        }
    }
}
