/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instance;
import weka.core.Instances;
import wekimini.modifiers.BufferedInput;

/**
 *
 * @author louismccallum
 */
public class BufferTest implements ModifierTest {
    
    public Wekinator w;
    
    @Before
    public void setUp() {
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } catch (Exception e)
        {
            
        }
    }
    
    /*
    // ****BUFFER***** //
    // BASE TESTS
    */
    
    @Override
    public void testForTraining(int windowSize)
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.removeModifierFromOutput(0, 0);
        w.getDataManager().featureManager.addModifierToOutput(new BufferedInput("input-1",0,windowSize,0), 0);
        w.getSupervisedLearningManager().buildAll();
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances();
        for(int i = 0; i < featureInstances.size(); i++)
        {
            Instances instances = featureInstances.get(i);
            for (int j = 0; j < instances.numInstances(); j++)
            {
                double[] inputs = instances.instance(j).toDoubleArray();
                //CHECK BUFFERED FEATURES (Input 1 is incremental 1-100)
                if(i == 0)
                {
                    int numAttributes = inputs.length;
                    assertEquals(windowSize + 1,numAttributes);
                    for(int k = 0; k < windowSize; k++)
                    {
                        if((k + j) < windowSize - 1)
                        {
                            assertEquals(0.0, inputs[k], 0.0);
                        }
                        else
                        {
                           assertEquals( k + (j - (windowSize - 2)), inputs[k], 0.0); 
                        }  
                    }
                    assertEquals(1.0, inputs[windowSize], 0.0);
                }
                else
                {
                    //CHECK PASS THROUGH FEAUTRES
                    assertEquals(j + 1, inputs[0], 0.0);
                    assertEquals(1.0, inputs[1], 0.0);
                    if(j % 10 == 9)
                    {
                        assertEquals(0.9, inputs[2], 0.0);
                    }
                    else   
                    {
                        assertEquals(0.1, inputs[2], 0.0);
                    }
                    assertEquals(i + 1, inputs[3], 0.0);
                }
            }
        }
    }
    
    @Override
    public void testForRunning(int windowSize) throws InterruptedException
    {
        testForTraining(10);
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        for(int j = 1; j < 21; j++)
        {
            double[] oscInputs = {j, j + 1, j * j};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            assertEquals(windowSize + 1,numAttributes);
            for(int k = 0; k < windowSize; k++)
            {
                if((k + (j-1)) < (windowSize - 1))
                {
                    assertEquals(0.0, inputs[k], 0.0);
                }
                else
                {
                   assertEquals( k + (j - (windowSize - 1)), inputs[k], 0.0); 
                }  
            }
        }  
    }
    
    /*
    // ****BUFFER***** //
    // TESTS
    */
    
    @Override
    @Test
    public void testTraining()
    {
        testForTraining(10);
    }
    
    @Override
    @Test
    public void testTrainingChangingWindowSize()
    {
        testForTraining(10);
        testForTraining(5);
    }
    
    @Override
    @Test
    public void testRunning() throws InterruptedException
    {
        testForRunning(10);
    }
    
    @Override
    @Test
    public void testRunningThenTraining() throws InterruptedException
    {
        testForRunning(10);
        testForTraining(10);
    }
    
}
