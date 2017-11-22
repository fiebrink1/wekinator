/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.SecondOrderDifference;

/**
 *
 * @author louismccallum
 */
public class SecondOrderDifferenceTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        SecondOrderDifference sod = new SecondOrderDifference("SOD",0,0);
        sod.addRequiredInput(0);
        int idSOD = w.getDataManager().featureManager.addModifierToOutput(sod, 0);
        SecondOrderDifference sod2 = new SecondOrderDifference("SOD",1,0);
        sod2.addRequiredInput(0);
        int idSOD2 = w.getDataManager().featureManager.addModifierToOutput(sod2, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        if(instanceIndex > 1)
        {
            assertEquals(0.0, inputs[1], 0.0);
            assertEquals(0.0, inputs[0], 0.0);
        }

    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,3);
    }
}
