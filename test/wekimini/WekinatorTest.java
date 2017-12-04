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
import wekimini.modifiers.PassThroughVector;
import wekimini.modifiers.PassThroughSingle;
import wekimini.modifiers.ModifiedInput;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author louismccallum
 */
public class WekinatorTest {
    
    public Wekinator w;
    
    /*
        Load test file. This dataset has three inputs with 100 rows. 
        0 = > 1,2,3,4....100, 
        1 => 1,1,1,1,1....1, 
        2 => 0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.9,0.1,0.1.....etc
        And three outputs, each classifiers with classes 1,2 and 3 respectively
    */
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
        w.getSupervisedLearningManager().buildAll();
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances(false);
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
    public void testMultipleModifiersForOneInput()
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        ModifiedInput modifier1 = new PassThroughSingle("input-1a",0,0);
        modifier1.addRequiredModifierID(0);
        int id1 = w.getDataManager().featureManager.addModifierToOutput(modifier1, 0);
        ModifiedInput modifier2 = new PassThroughSingle("input-1b",0,0);
        modifier2.addRequiredModifierID(id1);
        int id2 = w.getDataManager().featureManager.addModifierToOutput(modifier2, 0);
        ModifiedInput modifier3 = new PassThroughSingle("input-1c",0,0);
        modifier3.addRequiredModifierID(id2);
        int id3 = w.getDataManager().featureManager.addModifierToOutput(modifier3, 0);
        w.getSupervisedLearningManager().buildAll();
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances(false);
        for(int i = 0; i < featureInstances.size(); i++)
        {
            Instances instances = featureInstances.get(i);
            for (int j = 0; j < instances.numInstances(); j++)
            {
                double[] inputs = instances.instance(j).toDoubleArray();
                if(i == 0)
                {
                    assertEquals(j + 1, inputs[0], 0.0);
                    assertEquals(j + 1, inputs[1], 0.0);
                    assertEquals(j + 1, inputs[2], 0.0);
                    assertEquals(1, inputs[3], 0.0);
                }
                else
                {
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
        testInputsPassThroughForTraining();
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        double[] inputs = {1,1,1};
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        w.getSupervisedLearningManager().updateInputs(inputs);
    }
    
    @Test
    public void testRunningWithInputsPassedThroughCheckValues() throws InterruptedException
    {
        testInputsPassThroughForTraining();
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
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

    
    @After
    public void tearDown() {
    }  
}
