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
public class FeatureMultipleModifierOutput extends Feature 
{
    Integer[] outputModifierIDs;

    public FeatureMultipleModifierOutput(String name, boolean doNormalise) {
        super(name, doNormalise);
    }
    
    public Integer[] getOutputModifierIDs()
    {
        return outputModifierIDs;
    }
    
    public void setOutputModifierIDs(Integer[] modifierIDs)
    {
        this.outputModifierIDs = modifierIDs;
    }
}
