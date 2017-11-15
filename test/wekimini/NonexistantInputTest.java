/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.RawInput;
import wekimini.modifiers.RawInputs;

/**
 *
 * @author louismccallum
 */
public class NonexistantInputTest extends ModifierTest {
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        ModifiedInput passThrough = new RawInput("1",0,0);
        passThrough.addRequiredInput(0);
        passThrough.addToOutput = true;
        int id1 = w.getDataManager().featureManager.addModifierToOutput(passThrough, 0);
        String[] names = {"1","2","3"};
        ModifiedInput passThrough2 = new RawInputs(names,0);
        //THIS IS A NONEXISTANT INPUT
        passThrough2.addRequiredInput(4);
        passThrough2.addToOutput = true;
        int id2 = w.getDataManager().featureManager.addModifierToOutput(passThrough2, 0);
    }
    
    /*
    inputs[0] is the pass through of the sequential input, the next three should be the raw inputs 
    But are 0 because the input is incorrectly set (to 4, should be 0)
    */
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        assertEquals(instanceIndex + 1, inputs[0], 0.0);
        assertEquals(0.0, inputs[1], 0.0);
        assertEquals(0.0, inputs[2], 0.0);
        assertEquals(0.0, inputs[3], 0.0);
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(1 + 3 + 1, numAttributes, 0.0);
    }
    
}
