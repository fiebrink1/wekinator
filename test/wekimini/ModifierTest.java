/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author louismccallum
 */
@Ignore public class ModifierTest {
            
    public Wekinator w;
            
    @Before
    public void setUp() {
        String fileLocation = getTestSetPath();
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } catch (Exception e)
        {
            
        }
    }
    
    public String getTestSetPath()
    {
       return "/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj";
    }

    public void setUpFilters(int windowSize)
    {
        
    }
    
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        
    }
    
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        
    }
    
    public void testForTraining(int windowSize)
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getSupervisedLearningManager().buildAll();
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances();
        for(int outputIndex = 0; outputIndex < featureInstances.size(); outputIndex++)
        {
            Instances instances = featureInstances.get(outputIndex);
            for (int instanceIndex = 0; instanceIndex < instances.numInstances(); instanceIndex++)
            {
                double[] inputs = instances.instance(instanceIndex).toDoubleArray();
                if(outputIndex == 0)
                {
                    testInputs(instanceIndex,windowSize,inputs);
                }
                else
                {
                    //Check the features on the outputs/paths that dont have any new modifiers
                    assertEquals(instanceIndex + 1, inputs[0], 0.0);
                    assertEquals(1.0, inputs[1], 0.0);
                    if(instanceIndex % 10 == 9)
                    {
                        assertEquals(0.9, inputs[2], 0.0);
                    }
                    else   
                    {
                        assertEquals(0.1, inputs[2], 0.0);
                    }
                    assertEquals(outputIndex + 1, inputs[3], 0.0);
                }
            }
        }
    }
    
    public void testForRunning(int windowSize) throws InterruptedException
    {
        testForTraining(windowSize);
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        for(int instanceIndex = 0; instanceIndex < 50; instanceIndex++)
        {
            double[] oscInputs = {instanceIndex + 1, 1.0, instanceIndex % 9 == 0 ? 0.1 : 0.9};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            testNumAttributes(numAttributes, windowSize);
            testInputs(instanceIndex,windowSize,inputs);
        }  
    }
    
    @Test
    public void testTraining()
    {
        setUpFilters(8);
        testForTraining(8);
    }
    
    
    @Test
    public void testTrainingChangingWindowSize()
    {
        setUpFilters(10);
        testForTraining(10);
        setUpFilters(5);
        testForTraining(5);
    }
    
    
    @Test
    public void testRunning() throws InterruptedException
    {
        setUpFilters(10);
        testForRunning(10);
    }
    
    @Test
    public void testRunningThenTraining() throws InterruptedException
    {
        setUpFilters(10);
        testForRunning(10);
        setUpFilters(5);
        testForTraining(5);
    }

   
}
