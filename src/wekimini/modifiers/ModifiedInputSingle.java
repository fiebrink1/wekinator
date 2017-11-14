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
public class ModifiedInputSingle extends ModifiedInput {
    
    protected transient double value = 0;
    protected String name;

    @Override
    public int getSize()
    {
        return 1;
    }
    
    public String getName()
    {
        return name;
    }
    
    public double getValue()
    {
        return value;
    }
}
