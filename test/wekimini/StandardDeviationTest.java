/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.StdDevWindowOperation;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class StandardDeviationTest extends ModifierTest
{
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput window = new WindowedOperation("input-1",new StdDevWindowOperation(),0,windowSize,0);
        window.addRequiredInput(0);
        int id = w.getDataManager().featureManager.addModifierToOutput(window, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        int countBack = (instanceIndex + 1) < windowSize ? (instanceIndex + 1) : windowSize;
        double [] window = new double[windowSize];
        for(int i = 0; i < countBack; i++)
        {
            window[i] = ((instanceIndex+1) - i);
        }
        double sum = 0;
        for (int i = 0; i < window.length; i++) {
            sum += window[i];
        }
        double avg = sum / window.length;

        double ssd = 0;
        for (int i = 0; i < window.length; i++) {
            ssd += Math.pow(window[i] - avg, 2);
        }
        double stdDev = Math.sqrt(ssd / window.length);
        assertEquals(stdDev,inputs[0],0.00001);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,2,0.0);
    }
    
}
