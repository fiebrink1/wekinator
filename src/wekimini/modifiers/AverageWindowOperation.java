/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import wekimini.modifiers.WindowedOperation.Operation;

/**
 *
 * @author rebecca
 */
public class AverageWindowOperation implements Operation {

    @Override
    public double doOperation(double[] vals, int startPtr) {
        double sum = 0;
        for (int i = 0; i < vals.length; i++) {
            sum += vals[i];
        }
        return sum / vals.length;
    }

    @Override
    public String shortName() {
        return "Avg";
    }

}
