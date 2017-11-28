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
public class MinInputs extends ModifiedInputSingle {
    
    private double[] values;
    
    public MinInputs(String originalName, int increment) {
        if (increment == 1) {
            this.name = originalName;
        } else {
            this.name = originalName + "(" + increment + ")";
        }
    }
    
    @Override
    public void updateForInputs(double[] inputs) {
        values = inputs;
        dirty = true;
    }
    
    @Override public double getValue()
    {
        if(dirty)
        {
            double min = Double.POSITIVE_INFINITY;
            for(double val:values)
            {
                if(val < min)
                {
                    min = val;
                }
            }
            value = min;
            dirty = false;
        }
        return value;
    }
    
}