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
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class MinMaxWindowTest extends ModifierTest {
 
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput windowMax = new WindowedOperation("input-1",new MaxWindowOperation(),0,windowSize,0);
        windowMax.addRequiredInput(0);
        ModifiedInput windowMin = new WindowedOperation("input-1",new MinWindowOperation(),0,windowSize,0);
        windowMin.addRequiredInput(0);
        int idMax = w.getDataManager().featureManager.addModifierToOutput(windowMax, 0);
        int idMin = w.getDataManager().featureManager.addModifierToOutput(windowMin, 0);
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
        assertEquals(numAttributes,3,0.0);
    }
    
}
