/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import wekimini.modifiers.MedianWindowOperation;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class MedianTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        ModifiedInput window = new WindowedOperation("input-1",new MedianWindowOperation(),0,windowSize,0);
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
        Arrays.sort(window);
        assertEquals(window[(int)Math.floor(window.length/2.0)],inputs[0],0.0);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(2,numAttributes,0.0);
    }
}
