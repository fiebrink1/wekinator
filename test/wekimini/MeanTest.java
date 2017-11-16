/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.AverageWindowOperation;
import wekimini.modifiers.EnergyWindowOperation;
import wekimini.modifiers.WindowedOperation;
import wekimini.modifiers.ModifiedInput;

/**
 *
 * @author louismccallum
 */
public class MeanTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        ModifiedInput window = new WindowedOperation("input-1",new AverageWindowOperation(),0,windowSize,0);
        window.addRequiredInput(0);
        int id = w.getDataManager().featureManager.addModifierToOutput(window, 0);
        ModifiedInput window2 = new WindowedOperation("input-2",new EnergyWindowOperation(),0,windowSize,0);
        window2.addRequiredInput(0);
        int id2 = w.getDataManager().featureManager.addModifierToOutput(window2, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        double sum = 0;
        double sqSum = 0;
        int countBack = (instanceIndex + 1) < windowSize ? (instanceIndex + 1) : windowSize;
        for(int i = 0; i < countBack; i++)
        {
            sum = sum + ((instanceIndex+1) - i);
            sqSum = sqSum + Math.pow(((instanceIndex+1) - i),2);
        }
        assertEquals(sum/(double)windowSize,inputs[0],0.0);
        assertEquals(sqSum/(double)windowSize,inputs[1],0.0);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,3,0.0);
    }
}
