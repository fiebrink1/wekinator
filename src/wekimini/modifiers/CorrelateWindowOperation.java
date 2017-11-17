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

public class CorrelateWindowOperation implements MultipleInputOperation {
    @Override
    public double doOperation(double[][] vals, int startPtr) {
        
        //We dont need to use the startPtr because everything just gets summed.
        
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;
        double n = vals[0].length;
        
        for(int i = 0; i < n; i++)
        {
            sumX += vals[0][i];
            sumY += vals[1][i];
            sumXY += (vals[0][i] * vals[1][i]);
            sumX2 += (vals[0][i] * vals[0][i]);
            sumY2 += (vals[1][i] * vals[1][i]);
        }
        double numerator = ((n * sumXY) - (sumX * sumY));
        double denomenator = (Math.sqrt(((n * sumX2) - Math.pow(sumX,2.0)) * ((n * sumY2) - Math.pow(sumY, 2))));
        
        //Avoid NaN
        double r =  denomenator > 0.0 ? numerator / denomenator : 0.0;
        
        return r;
    }

    @Override
    public String shortName() {
        return "Cor";
    }
}
