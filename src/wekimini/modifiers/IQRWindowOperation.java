/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.Arrays;

/**
 *
 * @author louismccallum
 */
public class IQRWindowOperation implements WindowedOperation.Operation
{
    @Override
    public double doOperation(double[] vals, int startPtr) {
        double half1 = Math.ceil((double)vals.length/2.0);
        double q1;
        double q3;
        double[] sorted = vals.clone();
        Arrays.sort(sorted);
        if(half1 % 2 == 0)
        {
            double hiIndex = Math.floor(half1/2.0);
            double loIndex = hiIndex - 1;
            q1 = (sorted[(int)loIndex] + sorted[(int)hiIndex])/2.0;
        }
        else
        {
            double index = (int)Math.floor(half1/2);
            q1 = sorted[(int)index];
        }
        double half2 = sorted.length - half1;
        double offset = half1 - 1;
        if(half2 % 2 == 0)
        {
            double loIndex = offset + Math.floor(half2/2.0);
            double hiIndex = loIndex + 1;
            q3 = (sorted[(int)loIndex] + sorted[(int)hiIndex])/2.0;
        }
        else
        {
            double index = offset + Math.ceil(half2/2);
            q3 = sorted[(int)index];
        }
        return q3-q1;
    }

    @Override
    public String shortName() {
        return "IQR";
    }
}
