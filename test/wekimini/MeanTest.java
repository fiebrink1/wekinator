/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.AverageWindowOperation;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class MeanTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeModifierFromOutput(0, 0);
        w.getDataManager().featureManager.addModifierToOutput(new WindowedOperation("input-1",new AverageWindowOperation(),0,windowSize,0), 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        double sum = 0;
        int countBack = (instanceIndex + 1) < windowSize ? (instanceIndex + 1) : windowSize;
        for(int i = 0; i < countBack; i++)
        {
            sum = sum + ((instanceIndex+1) - i); 
        }
        assertEquals(sum/(double)windowSize,inputs[0],0.0);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(2,numAttributes,0.0);
    }
}
