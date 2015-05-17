/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
public class OutputTableModel extends AbstractTableModel {

    OSCOutputGroup o;
    private String[] columnNames;
    private final int numRows;
    private static final Logger logger = Logger.getLogger(OutputTableModel.class.getName());
    private final List<Path> paths; //TODO: problem if these change while this window is open

    public OutputTableModel(OSCOutputGroup o, List<Path> paths) {
        this.o = o;
        this.numRows = o.getNumOutputs();
        setColNames();
        this.paths = paths;
    }

    private void setColNames() {
        columnNames = new String[7];
        columnNames[0] = "#";
        columnNames[1] = "Name";
        columnNames[2] = "Type";
        columnNames[3] = "Min";
        columnNames[4] = "Max";
        columnNames[5] = "Limit type";
        columnNames[6] = "Current algorithm";
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public int getRowCount() {
        return numRows;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        OSCOutput output = o.getOutputs().get(row);
        if (col == 0) {
            return (row + 1);
        } else if (col == 1) {
            return output.getName();
        } else if (col == 2) {
            if (output instanceof OSCClassificationOutput) {
                return "Classification";
            } else if (output instanceof OSCNumericOutput) {
                if (((OSCNumericOutput) output).getOutputType() == OSCNumericOutput.NumericOutputType.INTEGER) {
                    return "Numeric - Integer";
                } else {
                    return "Numeric - Real";
                }
            } else {
                return "Unknown";
            }
        } else if (col == 3) {
            if (output instanceof OSCClassificationOutput) {
                return 1;
            } else {
                return ((OSCNumericOutput) output).getMin();
            }
        } else if (col == 4) {
            if (output instanceof OSCClassificationOutput) {
                return ((OSCClassificationOutput) output).getNumClasses();
            } else {
                return ((OSCNumericOutput) output).getMax();
            }
        } else if (col == 5) {
            if (output instanceof OSCClassificationOutput) {
                return "";
            } else {
                OSCNumericOutput.LimitType l = ((OSCNumericOutput) output).getLimitType();
                if (l == OSCNumericOutput.LimitType.HARD) {
                    return "Hard";
                } else {
                    return "Soft";
                }
            }
        } else if (col == 6) {
            if (paths != null && paths.size() > row) {
                if (paths.get(row).getModelBuilder() != null) {
                    return paths.get(row).getModelBuilder().getPrettyName();
                }
            }
            return "None";
        } else {
            return null;
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return String.class;
    }
}
