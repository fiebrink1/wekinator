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
public class FirstOrderDifference extends ModifiedInputSingleOutput {
    
    private transient double x_n1 = 0;
    private transient double x_n = 0;
    
    public FirstOrderDifference(String originalName, int index, int increment) {
        if (increment == 1) {
            this.name = originalName;
        } else {
            this.name = originalName + "(" + increment + ")";
        }
        this.inputIndex = index;
    }
    
    @Override
    public void reset()
    {
        x_n1 = 0;
        x_n = 0;
    }
    
    @Override
    public void updateForInputs(double[] inputs) {
        x_n1 = x_n;
        x_n = inputs[inputIndex];
        value = x_n - x_n1;
    }
    
}
