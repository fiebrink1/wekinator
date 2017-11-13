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
import wekimini.modifiers.AverageWindowOperation;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class MeanTest extends ModifierTest {
    
    @Override
    public void testForTraining(int windowSize)
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getDataManager().featureManager.removeModifierFromOutput(0, 0);
        w.getDataManager().featureManager.addModifierToOutput(new WindowedOperation("input-1",new AverageWindowOperation(),0,windowSize,0), 0);
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
                    double sum = 0;
                    int countBack = (instanceIndex + 1) < windowSize ? (instanceIndex + 1) : windowSize;
                    for(int i = 0; i < countBack; i++)
                    {
                        sum = sum + ((instanceIndex+1) - i); 
                    }
                    assertEquals(sum/(double)windowSize,inputs[0],0.0);
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
    
    @Override
    public void testForRunning(int windowSize) throws InterruptedException
    {
        testForTraining(10);
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        for(int instanceIndex = 1; instanceIndex < 20; instanceIndex++)
        {
            double[] oscInputs = {instanceIndex, instanceIndex + 1, instanceIndex * instanceIndex};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double[] inputs = instance.toDoubleArray();
            int numAttributes = inputs.length;
            assertEquals(2,numAttributes);
            double sum = 0;
            int countBack = instanceIndex < windowSize ? instanceIndex : windowSize;
            for(int i = 0; i < countBack; i++)
            {
                sum = sum + (instanceIndex - i); 
            }
            assertEquals(sum/(double)windowSize,inputs[0],0.0);
        }  
    }  
}
