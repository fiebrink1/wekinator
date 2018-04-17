/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.MaxInputs;
import wekimini.modifiers.MinInputs;
import wekimini.modifiers.ModifiedInput;

/**
 *
 * @author louismccallum
 */
public class MinMaxInputTest extends ModifierTest 
{
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput max = new MaxInputs("max",0);
        max.addRequiredModifierID(0);
        ModifiedInput min = new MinInputs("min",0);
        min.addRequiredModifierID(0);
        int idMax = w.getDataManager().featureManager.addModifierToOutput(max, 0);
        int idMin = w.getDataManager().featureManager.addModifierToOutput(min, 0);
        addPassThroughForOutput(1);
        addPassThroughForOutput(2);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        assertEquals(instanceIndex % 10 == 9 ? 0.9 : 0.1, inputs[1], 0.0);
        assertEquals(instanceIndex + 1, inputs[0], 0.0);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,3);
    }
    
}
