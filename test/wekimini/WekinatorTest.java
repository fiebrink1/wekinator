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
        int numModifiers = w.getDataManager().featureManager.modifiers.size();
        int expResult = 3;
        int result = numModifiers;
        assertEquals(expResult, result);
    }
    
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
                assertEquals(inputs[0], j + 1, 0.0);
                assertEquals(inputs[1], 1.0, 0.0);
                if(j % 10 == 9)
                {
                    assertEquals(inputs[2], 0.9, 0.0);
                }
                else   
                {
                    assertEquals(inputs[2], 0.1, 0.0);
                }
                System.out.println(inputs[3] + "=" + i);
                assertEquals(inputs[3], i + 1, 0.0);
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
    public void testRunningWithInputsPassedThrough() throws InterruptedException
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.setAllOutputsDirty();
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(2000);
        double[] inputs = {1,1,1};
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        w.getSupervisedLearningManager().updateInputs(inputs);
    }
    
    @After
    public void tearDown() {
    }  
}
