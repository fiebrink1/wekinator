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
public class RawInput implements ModifiedInputSingle, UsesOnlyOriginalInputs {
 
    private final String name;
    private final int index;
    private transient double value = 0;
    
    public RawInput(String originalName, int index, int increment) {
        if (increment == 1) {
            this.name = originalName;
        } else {
            this.name = originalName + "(" + increment + ")";
        }
        this.index = index;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void updateForInputs(double[] inputs) {
        value = inputs[index];
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public double getValue() {
        return value;
    }

}
