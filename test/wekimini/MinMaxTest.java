/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import wekimini.modifiers.MaxWindowOperation;
import wekimini.modifiers.MinWindowOperation;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class MinMaxTest extends ModifierTest {
 
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.addModifierToOutput(new WindowedOperation("input-1",new MaxWindowOperation(),0,windowSize,0), 0);
        w.getDataManager().featureManager.addModifierToOutput(new WindowedOperation("input-1",new MinWindowOperation(),0,windowSize,0), 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        double min = (instanceIndex - windowSize) >= -1 ? (instanceIndex + 1) - (windowSize - 1) : 0.0;
        assertEquals(min, inputs[1], 0.0);
        assertEquals(instanceIndex + 1, inputs[0], 0.0);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(3,numAttributes,0.0);
    }
    
}