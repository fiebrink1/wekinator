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
public class Threshold extends ModifiedInputSingleOutput {
    
    public double threshold = 0.5;
    
    public Threshold(String originalName, int index, int increment) {
        if (increment == 1) {
            this.name = originalName;
        } else {
            this.name = originalName + "(" + increment + ")";
        }
        this.inputIndex = index;
    }
    
    @Override
    public void updateForInputs(double[] inputs) 
    {    
        value = inputs[inputIndex] > threshold ? 1.0: 0.0;
    }
    
}
