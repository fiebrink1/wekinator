/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instance;
import weka.core.Instances;
import wekimini.modifiers.AverageWindowOperation;
import wekimini.modifiers.EnergyWindowOperation;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class NormaliseIntegrationTest {
    
    public Wekinator w;
            
    @Before
    public void setUp() {
        String fileLocation = getTestSetPath();
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            w.getDataManager().doNormalise = true;
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
        w.getDataManager().doNormalise = true;
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(true, 0);
    }
    
    public int getMainWindowSize()
    {
        return 10;
    }
    
    public void testInputs(int instanceIndex, int windowSize, double[] inputs, double outputClass)
    {
        assertEquals(instanceIndex / 99.0, inputs[0], 0.0);
        assertEquals(0.0, inputs[1], 0.0);
        assertEquals(instanceIndex % 10 == 9 ? 1.0 : 0.0, inputs[2], 0.0);
        assertEquals(outputClass, inputs[3], 0.0);
    }
    
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(4, numAttributes, 0.0);
    }
    
    public void testForTraining(int windowSize)
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getSupervisedLearningManager().buildAll();
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances(false);
        for(int outputIndex = 0; outputIndex < featureInstances.size(); outputIndex++)
        {
            Instances instances = featureInstances.get(outputIndex);
            for (int instanceIndex = 0; instanceIndex < instances.numInstances(); instanceIndex++)
            {
                double[] inputs = instances.instance(instanceIndex).toDoubleArray();
                testInputs(instanceIndex,windowSize,inputs,outputIndex + 1);
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
            double[] oscInputs = {instanceIndex + 1, 1.0, instanceIndex % 10 == 9 ? 0.9 : 0.1};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            testNumAttributes(numAttributes, windowSize);
            testInputs(instanceIndex,windowSize,inputs, 0.0);
        }  
    }
    
    @Test
    public void testTraining()
    {
        setUpFilters(getMainWindowSize());
        testForTraining(getMainWindowSize());
    }
    
    
    @Test
    public void testTrainingChangingWindowSize()
    {
        setUpFilters(getMainWindowSize());
        testForTraining(getMainWindowSize());
        setUpFilters(getMainWindowSize()*2);
        testForTraining(getMainWindowSize()*2);
    }
    
    
    @Test
    public void testRunning() throws InterruptedException
    {
        setUpFilters(getMainWindowSize());
        testForRunning(getMainWindowSize());
    }
    
    @Test
    public void testRunningThenTraining() throws InterruptedException
    {
        setUpFilters(getMainWindowSize());
        testForRunning(getMainWindowSize());
        setUpFilters(getMainWindowSize()*2);
        testForTraining(getMainWindowSize()*2);
    }
}
