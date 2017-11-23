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
public class FeatureSingleModifierOutput extends Feature {
    
    int outputModifierID;

    public FeatureSingleModifierOutput(String name) {
        super(name);
    }
    
    public int getOutputModifierID()
    {
        return outputModifierID;
    }
    
    public void setOutputModifierID(int modifierID)
    {
        this.outputModifierID = modifierID;
    }
}
