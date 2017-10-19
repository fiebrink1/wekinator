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

public class RawInputs implements ModifiedInputVector, UsesOnlyOriginalInputs {
    
    private final String[] names;
    private transient double[] values;
    
    public RawInputs(String[] originalNames, int increment) {
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
    public void reset()
    {
        
    }
    
    @Override
    public String[] getNames() {
        return names;
    }

    @Override
    public void updateForInputs(double[] inputs) {
        values = inputs;
    }

    @Override
    public int getSize() {
        return values.length;
    }

    @Override
    public double[] getValues() {
        return values;
    }
    
}
