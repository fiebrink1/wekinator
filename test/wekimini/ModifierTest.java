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
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } catch (Exception e)
        {
            
        }
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
        setUpFilters(windowSize);
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
                    //CHECK PASS THROUGH FEAUTRES
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
        testForTraining(10);
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        for(int instanceIndex = 0; instanceIndex < 20; instanceIndex++)
        {
            double[] oscInputs = {instanceIndex + 1, instanceIndex + 2, (instanceIndex + 1) * (instanceIndex + 1)};
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
        testForTraining(10);
    }
    
    @Test
    public void testTrainingChangingWindowSize()
    {
        testForTraining(10);
        testForTraining(5);
    }
    
    @Test
    public void testRunning() throws InterruptedException
    {
        testForRunning(10);
    }
    
    @Test
    public void testRunningThenTraining() throws InterruptedException
    {
        testForRunning(10);
        testForTraining(10);
    }
    
}
