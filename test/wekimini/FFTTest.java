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
import wekimini.modifiers.FFTModifier;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.PassThroughVector;

/**
 *
 * @author louismccallum
 */
public class FFTTest {
    
    public Wekinator w;
    int bins[] = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};
        
    @Before
    public void setUp() {
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            w.getDataManager().doNormalise = false;
        } catch (Exception e)
        {
            
        }
    }
    
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput fft1 = new FFTModifier("input-1",0,50,bins);
        fft1.addRequiredModifierID(0);
        int id = w.getDataManager().featureManager.addModifierToOutput(fft1, 0);
        addPassThroughForOutput(1);
        addPassThroughForOutput(2);
    }
    
    public void addPassThroughForOutput(int output)
    {
        PassThroughVector passThrough = new PassThroughVector(new String[] {"input1","input2","input3"}, 1);
        passThrough.addRequiredModifierID(0);
        w.getDataManager().featureManager.addModifierToOutput(passThrough, output);
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
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances(false);
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
        testForTraining(windowSize);
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        double sampleRate = 50;
        double totalTime = 10;
        double sineFreq = 5;
        for(double instanceIndex = 0; instanceIndex < sampleRate * totalTime; instanceIndex++)
        {
            double input = Math.sin( ( (2.0 * Math.PI * sineFreq) / sampleRate) * instanceIndex);
            double input2 = Math.sin( ( (2.0 * Math.PI * sineFreq * 2.0) / sampleRate) * instanceIndex);
            double input3 = Math.sin( ( (2.0 * Math.PI * sineFreq * 10.0) / sampleRate) * instanceIndex);
            double[] oscInputs = {input, input2, input3};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            assertEquals(numAttributes,bins.length + 1,0.0);
            testInputs((int)instanceIndex,windowSize,inputs);
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
