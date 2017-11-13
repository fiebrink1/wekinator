/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;
import wekimini.modifiers.WindowedOperation.Operation;

/**
 *
 * @author louismccallum
 */
public class MinWindowOperation implements Operation
{
    @Override
    public double doOperation(double[] vals, int startPtr) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] < min) {
                min = vals[i];
            }
        }
        return min;
    }

    @Override
    public String shortName() {
        return "Min";
    }
}
