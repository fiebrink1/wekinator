/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.BufferedInput;
import wekimini.modifiers.ModifiedInput;

/**
 *
 * @author louismccallum
 */
public class BufferTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput buffer = new BufferedInput("input-1",0,windowSize,0);
        buffer.addRequiredModifierID(0);
        int id = w.getDataManager().featureManager.addModifierToOutput(buffer, 0);
        addPassThroughForOutput(1);
        addPassThroughForOutput(2);
        
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        int numAttributes = inputs.length;
        assertEquals(windowSize + 1, numAttributes);
        for(int k = 0; k < windowSize; k++)
        {
            if((k + instanceIndex) < windowSize - 1)
            {
                assertEquals(0.0, inputs[k], 0.0);
            }
            else
            {
               assertEquals( k + (instanceIndex - (windowSize - 2)), inputs[k], 0.0); 
            }  
        }
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,windowSize + 1,0.0);
    }
}
