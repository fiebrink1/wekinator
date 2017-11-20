/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import weka.core.Instance;
import weka.core.Instances;
import wekimini.modifiers.ThreeDimensionalMagnitude;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.MultipleInputWindowedOperation;
import wekimini.modifiers.PassThroughSingle;

/**
 *
 * @author louismccallum
 */
public class MagnitudeTest extends ModifierTest {
    
    
    @Override
    public String getTestSetPath()
    {
       return "/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/PythagoreanQuadrupleTestSet/PythagoreanQuadrupleTestSet/PythagoreanQuadrupleTestSet.wekproj";
    }
    
    @Override
    public int getMainWindowSize()
    {
        return 2;
    }
    
    @Override
    public void setUpFilters(int windowSize)
    {        
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        
        // 2*1, 2*2, 2*3...2*n
        ModifiedInput raw1 = new PassThroughSingle("raw-1",0,0);
        raw1.addToOutput = false;
        raw1.addRequiredInput(0);
        int rawID1 = w.getDataManager().featureManager.addModifierToOutput(raw1, 0);
        
        // 10*1, 10*2, 10*3...10*n
        ModifiedInput raw2 = new PassThroughSingle("raw-2",1,0);
        raw2.addToOutput = false;
        raw2.addRequiredInput(0);
        int rawID2 = w.getDataManager().featureManager.addModifierToOutput(raw2, 0);
        
        // 11*1, 11*2, 11*3...11*n
        ModifiedInput raw3 = new PassThroughSingle("raw-3",2,0);
        raw3.addToOutput = false;
        raw3.addRequiredInput(0);
        int rawID3 = w.getDataManager().featureManager.addModifierToOutput(raw3, 0);
        
        ModifiedInput mag = new MultipleInputWindowedOperation("input-1",new ThreeDimensionalMagnitude(),windowSize,0);
        mag.addRequiredInput(rawID1);
        mag.addRequiredInput(rawID2);
        mag.addRequiredInput(rawID3);
        int id1 = w.getDataManager().featureManager.addModifierToOutput(mag, 0);
    }
    
    @Override
    public void testInputs(int instanceIndex, int windowSize, double[] inputs)
    {
        if(instanceIndex > windowSize)
        {
            //Pythagorean Quadruple is 2,10,11,15
            assertEquals(15.0,inputs[0],0.0);
        }
        
    }
    
    @Override
    public void testNumAttributes(int numAttributes, int windowSize)
    {
        assertEquals(numAttributes,2,0.0);
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
            double[] oscInputs = {instanceIndex * 2, instanceIndex * 10, instanceIndex * 11};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            testNumAttributes(numAttributes, windowSize);
            testInputs(instanceIndex,windowSize,inputs);
        }  
    }
}
