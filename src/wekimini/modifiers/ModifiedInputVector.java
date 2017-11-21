/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

/**
 *
 * @author rebecca
 */
public class ModifiedInputVector extends ModifiedInput {
    
    public String[] names;
    protected transient double[] values;
    
    @Override
    public void reset()
    {
        values = new double[getSize()];
    }
    
    @Override
    public int getSize()
    {
        return values.length;
    }
    
    public String[] getNames()
    {
        return names;
    }
    
    public double[] getValues()
    {
        return values;
    }
}
