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
public interface ModifiedInputVector extends ModifiedInput {
    public String[] getNames(); 
    public double[] getValues();
}
