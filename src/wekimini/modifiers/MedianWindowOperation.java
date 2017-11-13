/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;
import wekimini.modifiers.WindowedOperation.Operation;
import java.util.Arrays;
/**
 *
 * @author louismccallum
 */
public class MedianWindowOperation implements Operation {
    @Override
    public double doOperation(double[] vals, int startPtr) {
        double[] sorted = vals.clone();
        Arrays.sort(sorted);
        return sorted[(int)Math.floor(sorted.length/2.0)];
    }

    @Override
    public String shortName() {
        return "Med";
    }
}
