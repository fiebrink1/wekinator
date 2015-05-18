/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekimini;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author rebecca
 */
public class DataTableModel extends AbstractTableModel {

    DataManager m;
    private String[] columnNames;
    int numMetaData, numInputs, numOutputs;
    private static final Logger logger = Logger.getLogger(DataTableModel.class.getName());
    private final ChangeListener tableChangeListener;
    
    
    public DataTableModel(DataManager m) {
        this.m = m;
        this.numInputs = m.getNumInputs();
        this.numOutputs = m.getNumOutputs();
        this.numMetaData = 3; //for now, ID, time, & training round
        setColNames();
        
        tableChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fireTableDataChanged(); //TODO for efficiency, update this...
            }
        };
        
        //This listener needs to be deleted on dispose
       m.addChangeListener(tableChangeListener);
    }

    public void prepareToDie() {
        m.removeChangeListener(tableChangeListener);
    }
    
    private void setColNames() {
        columnNames = new String[numMetaData + numInputs + numOutputs];
        columnNames[0] = "ID";
        columnNames[1] = "Time";
        columnNames[2] = "Recording round";

        for (int i = 0; i < numInputs; i++) {
            columnNames[i + numMetaData] = m.getInputName(i);
        }
        for (int i = 0; i < numOutputs; i++) {
            columnNames[numMetaData + numInputs + i] = m.getOutputName(i);
        }
    }

    @Override
    public int getColumnCount() {
       // return columnNames.length;
        return numMetaData + numInputs + numOutputs;
    }

    @Override
    public int getRowCount() {
        return m.getNumExamples();
    }

    public void addRow(int recordingRound) {

        double[] inputs = new double[numInputs];
        double[] outputs = new double[numOutputs];
        boolean[] mask = new boolean[numOutputs];
        m.addToTraining(inputs, outputs, mask, recordingRound);
        int row = m.getNumExamples()-1;
        fireTableRowsInserted(row, row);
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= m.getNumExamples()) {
            return null;
        }

        if (col == 0) {
            return m.getID(row);
        } else if (col == 1) {
            return(m.getTimestampAsString(row));
           // return DataManager.dateDoubleToString(m.getTimestamp(row));
        } else if (col == 2) {
            return m.getRecordingRound(row);
        } else if (col < numMetaData + numInputs) {
            return m.getInputValue(row, col - numMetaData);
        } else if (col < numMetaData + numInputs + numOutputs) {
            //Treat as strings to represent missing values!
            Double d = m.getOutputValue(row, col - numMetaData - numInputs);
            if (d.isNaN()) {
                return "X";
            } else {
                return Double.toString(d);
            }
        } else {
            return null;
        }
    }

    @Override
    //TODO does this work for 0-sized table?
    public Class getColumnClass(int c) {
        //System.out.println("Class for " + c + "is " + getValueAt(0, c).getClass());
        //return getValueAt(0, c).getClass();
        if (c == 0) {
            //ID
            return Integer.class;
        } else if (c == 1) {
            //Date
            return String.class;
        } else if (c == 2) {
            //Recording round
            return Integer.class;    
        } else if (c >= 3 && c < (3+ numInputs)) {
            //It's an input (no "X" allowed for missing data)
            return Double.class;
        } else {
            //Output: "X" is allowed
            return String.class;
        }
            
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col >= numMetaData) { //don't allow editing of ID, timestamp, or training round
            return true;
        } else {
            return false;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col < numMetaData) {    
            logger.log(Level.WARNING, "Attempt to set value for column {0}", col);
            return;
        } 
        if (col < numMetaData + numInputs) { //it's an input
            //Assume all double inputs 
            if (value instanceof Double) {
                m.setInputValue(row, col - numMetaData, (Double) value);
                fireTableCellUpdated(row, col);
                return;
            } else {
                logger.log(Level.WARNING, "Non-double input encountered");
                return;
            }
        } else if (col < numMetaData + numInputs + numOutputs) { //it's an output
            //Check if legal? Probably should.
            int outputNum = col - numMetaData - numInputs; //DANGER DANGER TODOTODO
            double d = 0;
            
            /*int stuffWekaNeedsToAdd = 0;
            if (m.isOutputClassifier(outputNum)) {
                stuffWekaNeedsToAdd = -1;
            } */

            if (value instanceof Integer) {
                d = ((Integer) value);
            } else if (value instanceof Double) {
                d = ((Double) value);
            } else if (value instanceof String) {
                String s = (String) value;
                if (s.equals("X") || s.equals("x")) {
                    m.setOutputMissing(row, outputNum);
                    fireTableCellUpdated(row, col);
                    return;
                } else {
                    try {
                        d = Double.parseDouble((String) value);
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "Could not convert string value{0} to output", value);
                        return;
                    }
                }
            }
            
            if (m.isProposedOutputLegal(d, outputNum)) {
                m.setOutputValue(row, outputNum, d);
                fireTableCellUpdated(row, col);
            } else {
                logger.log(Level.WARNING, "Illegal output value attempted: {0} for output {1}", new Object[]{d, outputNum});
            }
        }
    }

    public int getNumInputs() {
        return numInputs;
    }
    
    public void deleteRows(int[] selectedRows) {
        for (int j = selectedRows.length - 1; j >= 0; j--) {
            //Delete the weka representation
            logger.log(Level.INFO, "Deleting row {0}", j);
            m.deleteExample(selectedRows[j]);
            fireTableRowsDeleted(selectedRows[j], selectedRows[j]);
        }

    }

    //Use something like this only if we want OSC coming in to be used to update these output rows.
    /*
    double[] getSelectedOutputs(int row) {
        double f[] = new double[numOutputs];
        for (int i = 0; i < numOutputs; i++) {
            // f[i] = (float) instances[i].instance(row).classValue();
            f[i] = m.getOutputValue(row, i);
            Double d = new Double(f[i]);
            if (d.isNaN()) {
                System.out.println("Error: d NaN here");
            }
        }
        return f;
    } */
}
