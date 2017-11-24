/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.FirstOrderDifference;
import wekimini.modifiers.ModifiedInput;

/**
 *
 * @author louismccallum
 */
public class FirstOrderDifferenceTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        FirstOrderDifference fod = new FirstOrderDifference("FOD",0,0);
        fod.addRequiredModifierID(0);
        int idFOD = w.getDataManager().featureManager.addModifierToOutput(fod, 0);
        FirstOrderDifference fod2 = new FirstOrderDifference("FOD",1,0);
        fod2.addRequiredModifierID(0);
        int idFOD2 = w.getDataManager().featureManager.addModifierToOutput(fod2, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        if(instanceIndex > 0)
        {
            assertEquals(0.0, inputs[1], 0.0);
            assertEquals(1.0, inputs[0], 0.0);
        }

    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,3);
    }
    
}
