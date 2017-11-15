/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import wekimini.modifiers.AverageWindowOperation;
import wekimini.modifiers.BufferedInput;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class ChainingTest extends ModifierTest {
    
    int meanWindowSize = 5;
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        ModifiedInput window = new WindowedOperation("input-1a",new AverageWindowOperation(),0,meanWindowSize,0);
        window.addRequiredInput(0);
        window.addToOutput = false;
        int windowID = w.getDataManager().featureManager.addModifierToOutput(window, 0);
        ModifiedInput buffer = new BufferedInput("input-1b",0,windowSize,0);
        buffer.addRequiredInput(windowID);
        w.getDataManager().featureManager.addModifierToOutput(buffer, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        
        int numAttributes = inputs.length;
        assertEquals(windowSize + 1,numAttributes);
        System.out.println("");
        System.out.print("input[" + instanceIndex + "] = ");
        for(int k = 0; k < windowSize; k++)
        {
            System.out.print(inputs[k] + ",");
           
            if((k + instanceIndex) < windowSize - 1)
            {
                assertEquals(0.0, inputs[k], 0.0);
            }
            else
            {
                if(instanceIndex > windowSize+1)
                {
                    assertEquals( k + (instanceIndex - windowSize), inputs[k], 0.0); 
                }
            } 
            
        }        
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(windowSize + 1,numAttributes,0.0);
    }
    
}
