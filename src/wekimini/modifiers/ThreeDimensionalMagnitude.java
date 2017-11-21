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
public class ThreeDimensionalMagnitude implements MultipleInputOperation {
    
    @Override
    public double doOperation(double[][] vals, int startPtr) {
        
        int n = vals[0].length;
        int frontPtr = startPtr + 1;
        if(frontPtr > (n - 1))
        {
            frontPtr = 0;
        }
        double dist1 = vals[0][frontPtr] - vals[0][startPtr];
        double dist2 = vals[1][frontPtr] - vals[1][startPtr];
        double dist3 = vals[2][frontPtr] - vals[2][startPtr];
        return Math.sqrt(Math.pow(dist1,2) + Math.pow(dist2,2) + Math.pow(dist3,2)); 
    }

    @Override
    public String shortName() {
        return "3DMag";
    }
}
