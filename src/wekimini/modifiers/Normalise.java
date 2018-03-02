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
public class Normalise extends ModifiedInputSingle {
    
    public double min = 0;
    public double max = 0;
    
    public Normalise(String originalName, int index, int increment) {
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
        double input = inputs[inputIndex];
        double delta = max - min;
        value = (input - min) / delta;
    }
    
}
