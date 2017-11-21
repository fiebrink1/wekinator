/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import static org.junit.Assert.assertEquals;
import weka.core.Instance;
import weka.core.Instances;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.MultipleInputWindowedOperation;
import wekimini.modifiers.PassThroughSingle;
import wekimini.modifiers.CorrelateWindowOperation;

/**
 *
 * @author louismccallum
 */
public class CorrelateTest extends ModifierTest {
    
    @Override
    public String getTestSetPath()
    {
       return "/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/CorrelationTestSet/CorrelationTestSet/WekinatorProject/WekinatorProject.wekproj";
    }
    
    @Override
    public void setUpFilters(int windowSize)
    {
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        
        //Sequential 0->100
        ModifiedInput raw1 = new PassThroughSingle("raw-1",0,0);
        raw1.addToOutput = false;
        raw1.addRequiredInput(0);
        int rawID1 = w.getDataManager().featureManager.addModifierToOutput(raw1, 0);
        
        //Sequential 0->100
        ModifiedInput raw2 = new PassThroughSingle("raw-2",1,0);
        raw2.addToOutput = false;
        raw2.addRequiredInput(0);
        int rawID2 = w.getDataManager().featureManager.addModifierToOutput(raw2, 0);
        
        //Sequential 100->0
        ModifiedInput raw3 = new PassThroughSingle("raw-3",2,0);
        raw3.addToOutput = false;
        raw3.addRequiredInput(0);
        int rawID3 = w.getDataManager().featureManager.addModifierToOutput(raw3, 0);
        
        ModifiedInput correlatePos = new MultipleInputWindowedOperation("input-1",new CorrelateWindowOperation(),windowSize,0);
        correlatePos.addRequiredInput(rawID1);
        correlatePos.addRequiredInput(rawID2);
        int id1 = w.getDataManager().featureManager.addModifierToOutput(correlatePos, 0);

        ModifiedInput correlateNeg = new MultipleInputWindowedOperation("input-2",new CorrelateWindowOperation(),windowSize,0);
        correlateNeg.addRequiredInput(rawID1);
        correlateNeg.addRequiredInput(rawID3);
        int id2 = w.getDataManager().featureManager.addModifierToOutput(correlateNeg, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        System.out.println(inputs[0]);
        //[0..windowSize] has erroneous 0 values 
        if(instanceIndex > windowSize)
        {
            //Two signals exactly positively correlated
            assertEquals(1.0,inputs[0],0.0);
            //Two signals exactly negatively correlated
            assertEquals(-1.0,inputs[1],0.0);
        }
        
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,3,0.0);
    }
    
    @Override
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
            }
        }
    }
    
    @Override
    public void testForRunning(int windowSize) throws InterruptedException
    {
        testForTraining(windowSize);
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        for(int instanceIndex = 0; instanceIndex < 101; instanceIndex++)
        {
            double[] oscInputs = {instanceIndex, instanceIndex, 100 - instanceIndex};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            testNumAttributes(numAttributes, windowSize);
            testInputs(instanceIndex,windowSize,inputs);
        }  
    }
    
}
