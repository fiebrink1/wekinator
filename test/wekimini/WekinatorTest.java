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
        for(Instances instances:featureInstances)
        {
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
            }
        }
    }
    
    @After
    public void tearDown() {
    }  
}
