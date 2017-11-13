/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import static org.junit.Assert.assertEquals;
import weka.core.Instance;
import weka.core.Instances;
import wekimini.modifiers.AverageWindowOperation;
import wekimini.modifiers.BufferedInput;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class BufferTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeModifierFromOutput(0, 0);
        w.getDataManager().featureManager.addModifierToOutput(new BufferedInput("input-1",0,windowSize,0), 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        int numAttributes = inputs.length;
        assertEquals(windowSize + 1,numAttributes);
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
        assertEquals(windowSize + 1,numAttributes,0.0);
    }
}
