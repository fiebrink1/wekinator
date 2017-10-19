/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import wekimini.modifiers.BufferedInput;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author louismccallum
 */
public class WekinatorTest {
    
    Wekinator w;
    
    public WekinatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } catch (Exception e)
        {
            
        }
    }
    
    @Test
    public void testNumInputs()
    {
        System.out.println("Num inputs " + w.getInputManager().getNumInputs());
        int expResult = 3;
        int result = w.getInputManager().getNumInputs();
        assertEquals(expResult,result);
    }
    
    @Test
    public void testOutputsAddedToFeatureManager()
    {
        int numModifiers = w.getDataManager().featureManager.featureGroups.size();
        int expResult = 3;
        int result = numModifiers;
        assertEquals(expResult, result);
    }
    
    @Test
    public void testDefaultModifiersAddedToFeatureManager()
    {
        int numOutputs = w.getDataManager().featureManager.numModifiedInputs(0);
        int expResult = 3;
        int result = numOutputs;
        assertEquals(expResult, result);
        numOutputs = w.getDataManager().featureManager.numModifiedInputs(1);
        result = numOutputs;
        assertEquals(expResult, result);
        numOutputs = w.getDataManager().featureManager.numModifiedInputs(2);
        result = numOutputs;
        assertEquals(expResult, result);
    }
    
    @Test
    public void testInputsPassThroughForTraining()
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.setAllOutputsDirty();
        w.getSupervisedLearningManager().buildAll();
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances();
        for(int i = 0; i < featureInstances.size(); i++)
        {
            Instances instances = featureInstances.get(i);
            for (int j = 0; j < instances.numInstances(); j++)
            {
                double[] inputs = instances.instance(j).toDoubleArray();
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
    
    @Test
    public void testInputsGetClassifiableInstanceWithInputsPassedThrough()
    {
        double[] input = {1,1,1};
        Instance instance = w.getDataManager().getClassifiableInstanceForOutput(input, 0);
        assert(instance.numAttributes() == 4);
        assert(instance.value(0) == 1);
        assert(instance.value(1) == 1);
        assert(instance.value(2) == 1);
    } 
    
    @Test
    public void testRunningWithInputsPassedThroughCompute() throws InterruptedException
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.setAllOutputsDirty();
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        double[] inputs = {1,1,1};
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        w.getSupervisedLearningManager().updateInputs(inputs);
    }
    
        @Test
    public void testRunningWithInputsPassedThroughCheckValues() throws InterruptedException
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.setAllOutputsDirty();
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        boolean[] mask = {true,true,true};
        for(int j = 1; j < 21; j++)
        {
            double[] oscInputs = {j, j + 1, j * j};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            assertEquals(oscInputs.length + 1,numAttributes);
            assertEquals(j, inputs[0], 0.0);
            assertEquals(j + 1, inputs[1], 0.0);
            assertEquals(j * j, inputs[2], 0.0);
        } 
    }
    

    public void testInputsBufferedForTraining(int bufferSize)
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.setAllOutputsDirty();
        w.getDataManager().featureManager.removeModifierFromOutput(0, 0);
        w.getDataManager().featureManager.addModifierToOutput(new BufferedInput("input-1",0,bufferSize,0), 0);
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
                    assertEquals(bufferSize + 1,numAttributes);
                    for(int k = 0; k < bufferSize; k++)
                    {
                        if((k + j) < bufferSize - 1)
                        {
                            assertEquals(0.0, inputs[k], 0.0);
                        }
                        else
                        {
                           assertEquals( k + (j - (bufferSize - 2)), inputs[k], 0.0); 
                        }  
                    }
                    assertEquals(1.0, inputs[bufferSize], 0.0);
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
    
    /*
        Remove pass through and add 10 window buffer to input 1 for path 1
    */
    
    @Test
    public void testSingleBuffer()
    {
        testInputsBufferedForTraining(10);
    }
    
    /*
        Train on 10 buffer, then 5 buffer
    */
    
    @Test
    public void testChangingBufferSize()
    {
        testInputsBufferedForTraining(10);
        testInputsBufferedForTraining(5);
    }
    
    @Test
    public void testRunningWithBuffers() throws InterruptedException
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.setAllOutputsDirty();
        w.getDataManager().featureManager.removeModifierFromOutput(0, 0);
        int bufferSize = 10;
        w.getDataManager().featureManager.addModifierToOutput(new BufferedInput("input-1",0,bufferSize,0), 0);
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        boolean[] mask = {true,true,true};
        for(int j = 1; j < 21; j++)
        {
            double[] oscInputs = {j,j,j};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            assertEquals(bufferSize + 1,numAttributes);
            for(int k = 0; k < bufferSize; k++)
            {
                if((k + (j-1)) < (bufferSize - 1))
                {
                    assertEquals(0.0, inputs[k], 0.0);
                }
                else
                {
                   assertEquals( k + (j - (bufferSize - 1)), inputs[k], 0.0); 
                }  
            }
        }  
    }
    
    @After
    public void tearDown() {
    }  
}
