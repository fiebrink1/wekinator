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
public class EnergyWindowOperation implements Operation 
{
    @Override
    public double doOperation(double[] vals, int startPtr) {
        double sum = 0;
        for (int i = 0; i < vals.length; i++) {
            sum += Math.pow(vals[i], 2);
        }
        return sum / vals.length;
    }

    @Override
    public String shortName() {
        return "En";
    }
    
}
