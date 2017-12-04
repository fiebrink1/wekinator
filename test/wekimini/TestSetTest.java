/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instance;
import weka.core.Instances;
import wekimini.modifiers.BufferedInput;
import wekimini.modifiers.ModifiedInput;

/**
 *
 * @author louismccallum
 */

public class TestSetTest {
    
    public Wekinator w;
    
    @Before
    public void setUp() {
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } 
        catch (Exception e)
        {
            
        }
    }
    
    @Test
    public void testRecordingData()
    {
        w.getSupervisedLearningManager().setRecordingState(SupervisedLearningManager.RecordingState.RECORDING_TEST);
        double[] inputs = {1,2,3};
        w.getSupervisedLearningManager().updateInputs(inputs);
        Instances testSet = w.getDataManager().getTestInstances();
        assertEquals(1, testSet.numInstances(), 0);
        //3 Metadata + 3 inputs + 3 outputs
        assertEquals(3 + 3 + 3, testSet.numAttributes(), 0);
        Instance in = testSet.firstInstance();
        assertEquals(1, in.value(3), 0);
        assertEquals(2, in.value(4), 0);
        assertEquals(3, in.value(5), 0);
    }
    
    @Test
    public void testModifyingTestData()
    {
        w.getSupervisedLearningManager().setRecordingState(SupervisedLearningManager.RecordingState.RECORDING_TEST);
        for(int i = 0; i < 100; i++)
        {
            double[] inputs = {i + 1, 2, 3};
            w.getSupervisedLearningManager().updateInputs(inputs);
        }
        Instances testSet = w.getDataManager().getTestingDataForOutput(0);
        assertEquals(100, testSet.numInstances(), 0);
        Instance in = testSet.firstInstance();
        assertEquals(1, in.value(0), 0);
       
        int windowSize = 10;
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput buffer = new BufferedInput("input-1", 0, windowSize, 0);
        buffer.addRequiredModifierID(0);
        int id = w.getDataManager().featureManager.addModifierToOutput(buffer, 0);
        
        testSet = w.getDataManager().getTestingDataForOutput(0);
        assertEquals(100, testSet.numInstances(), 0);
        for(int instanceIndex = 0; instanceIndex < 100; instanceIndex++)
        {
            double[] inputs = testSet.instance(instanceIndex).toDoubleArray();
            for(int k = 0; k < windowSize; k++)
            {
                if((k + instanceIndex) < windowSize - 1)
                {
                    assertEquals(0.0, inputs[k], 0.0);
                }
                else
                {
                   assertEquals( k + (instanceIndex - (windowSize - 2)), inputs[k], 0.0); 
                }  
            }
        }
    }
    
}
