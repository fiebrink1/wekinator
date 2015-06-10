/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class PathOutputCorrecter {
    private final boolean isRounding;
    private final boolean hardLimits;
    private final double min;
    private final double max;

    public PathOutputCorrecter(OSCOutput output) {
        if (output instanceof OSCClassificationOutput) {
            isRounding = false;
            hardLimits = false;
            min = 0;
            max = 0;
            return;
        }
        isRounding = ((OSCNumericOutput) output).getOutputType() == OSCNumericOutput.NumericOutputType.INTEGER;
        
        if (((OSCNumericOutput) output).getLimitType() == OSCNumericOutput.LimitType.HARD) {
            hardLimits = true;
            min = ((OSCNumericOutput) output).getMin();
            max = ((OSCNumericOutput) output).getMax();
        } else {
            hardLimits = false;
            min = 0;
            max = 0;
        }
    }

    public static boolean needsCorrecting(OSCOutput output) {
        if (output instanceof OSCClassificationOutput) {
            return false;
        }
        if (((OSCNumericOutput) output).getOutputType() == OSCNumericOutput.NumericOutputType.INTEGER) {
            return true;
        }
        return ((OSCNumericOutput) output).getLimitType() == OSCNumericOutput.LimitType.HARD;
    }

    public double correct(double val) {
        double myVal = val;
        if (hardLimits && val < min) {
            myVal = min;
        }
        if (hardLimits && val > max) {
            myVal = max;
        }
        if (isRounding) {
            myVal = (int) myVal;
        }
        return myVal;
    }
    
}
