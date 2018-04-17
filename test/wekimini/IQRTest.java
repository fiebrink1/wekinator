/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.IQRWindowOperation;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class IQRTest extends ModifierTest 
{
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput window = new WindowedOperation("input-1",new IQRWindowOperation(),0,windowSize,0);
        window.addRequiredModifierID(0);
        int id = w.getDataManager().featureManager.addModifierToOutput(window, 0);
        addPassThroughForOutput(1);
        addPassThroughForOutput(2);
    }
    
    @Override
    public int getMainWindowSize()
    {
        return 12;
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        System.out.println(instanceIndex + ":" + inputs[0]);
        if(windowSize == getMainWindowSize() && instanceIndex > getMainWindowSize())
        {
            assertEquals((double)getMainWindowSize()/2.0,inputs[0],0);
        } 
        else if(windowSize == getMainWindowSize()*2 && instanceIndex > getMainWindowSize()*2)
        {
            assertEquals(getMainWindowSize(),inputs[0],0);
        }
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,2.0,0.0);
    }
}
