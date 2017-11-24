/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.Threshold;
import wekimini.modifiers.ModifiedInput;

/**
 *
 * @author louismccallum
 */
public class TheresholdTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        Threshold thresh = new Threshold("thresh",0,0);
        thresh.threshold = 50.0;
        thresh.addRequiredModifierID(0);
        int idThresh = w.getDataManager().featureManager.addModifierToOutput(thresh, 0);
        Threshold thresh2 = new Threshold("thresh2",1,0);
        thresh2.threshold = 0.5;
        thresh2.addRequiredModifierID(0);
        int idThresh2 = w.getDataManager().featureManager.addModifierToOutput(thresh2, 0);
        Threshold thresh3 = new Threshold("thresh3",2,0);
        thresh3.threshold = 0.5;
        thresh3.addRequiredModifierID(0);
        int idThresh3 = w.getDataManager().featureManager.addModifierToOutput(thresh3, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        assertEquals(1.0, inputs[1], 0.0);
        assertEquals(instanceIndex % 10 == 9 ? 1.0 : 0.0, inputs[2], 0.0);
        assertEquals((instanceIndex + 1) > 50 ? 1.0 :0.0, inputs[0], 0.0);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,4);
    }
    
}
