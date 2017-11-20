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
public class AngleWindowOperation implements MultipleInputOperation {
    
    @Override
    public double doOperation(double[][] vals, int startPtr) {
        
        int n = vals[0].length;
        int frontPtr = startPtr + 1;
        if(frontPtr > (n - 1))
        {
            frontPtr = 0;
        }
        double y1 = vals[0][frontPtr];
        double x1 = 0;
        double y2 = vals[0][startPtr];
        double x2 = n-1;
        double y3 = vals[1][frontPtr];
        double x3 = 0;
        double y4 = vals[1][startPtr];
        double x4 = n-1;
        double angle = Math.atan2(x1*y2 - y1*x2, x1*x2 + y1*y2);
        System.out.println(angle);
        
        return angle;
    }

    @Override
    public String shortName() {
        return "Ang";
    }
}
