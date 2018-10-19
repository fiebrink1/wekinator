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
import wekimini.modifiers.BufferedInput;
import wekimini.modifiers.PassThroughSingle;
import wekimini.modifiers.PassThroughVector;

/**
 *
 * @author louismccallum
 */
@Ignore public class ModifierTest {
            
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
        String fileLocation = getTestSetPath();
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            w.getDataManager().doNormalise = false;
        } catch (Exception e)
        {
            
        }
    }
    
    public String getTestSetPath()
    {
       return "/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj";
    }

    public int getMainWindowSize()
    {
        return 10;
    }
    
    public void setUpFilters(int windowSize)
    {

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
        w.getSupervisedLearningManager().stopRunning();
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
                    //Check the features on the outputs/paths that dont have any new modifiers
                    assertEquals(instanceIndex + 1, inputs[0], 0.0);
                    assertEquals(1.0, inputs[1], 0.0);
                    assertEquals(instanceIndex % 10 == 9 ? 0.9 : 0.1, inputs[2], 0.0);
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
        w.getSupervisedLearningManager().startRunning();
        for(int instanceIndex = 0; instanceIndex < 50; instanceIndex++)
        {
            double[] oscInputs = {instanceIndex + 1, 1.0, instanceIndex % 10 == 9 ? 0.9 : 0.1};
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
