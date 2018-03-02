/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

/**
 *
 * @author louismccallum
 */
public class NormaliseVector extends ModifiedInputVector {
    
    public double[] min;
    public double[] max;
    
    public NormaliseVector(String[] originalNames, int increment) {
        if (increment == 1) {
            this.names = originalNames;
        } else {
            this.names = new String[originalNames.length];
            for(int i = 0; i <originalNames.length; i++)
            {
                this.names[i] = originalNames[i] + "(" + increment + ")";
            }
        }
        values = new double[originalNames.length];
    }
   

    @Override
    public void updateForInputs(double[] inputs) 
    {
        for(int i = 0; i <  values.length; i++)
        {
            double delta = max[i] - min[i];
            values[i] = (inputs[i] - min[i]) / delta;
        }
    }
    
}
