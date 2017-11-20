/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;
import wekimini.modifiers.MultipleInputWindowedOperation.MultipleInputOperation;
/**
 *
 * @author louismccallum
 */
public class SumInputsWindowOperation implements MultipleInputOperation {
    
    @Override
    public double doOperation(double[][] vals, int startPtr) {
        double sum = 0;
        for (double[] signal : vals) {
            for(int j = 0; j < signal.length; j++)
            {
                sum += signal[j];
            }
        }
        return sum;
    }

    @Override
    public String shortName() {
        return "SumIn";
    }
}
