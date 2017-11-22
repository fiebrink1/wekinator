/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import wekimini.featureanalysis.WrapperSelector;
import wekimini.learning.SupervisedLearningModel;
import weka.classifiers.Classifier;

/**
 *
 * @author louismccallum
 */
public class FeatureSelectorTest {
    
    public Wekinator w;
            
    @Before
    public void setUp() {
        String fileLocation = getTestSetPath();
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } catch (Exception e)
        {
            
        }
    }
    
    public String getTestSetPath()
    {
       return "/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj";
    }
    
    @Test
    public void testWrapperSelection() throws InterruptedException
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(50);
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances();
        WrapperSelector wrapperSelector = new WrapperSelector();
        int ptr = 0;
        for(Instances data:featureInstances)
        {
            Path path = w.getSupervisedLearningManager().getPaths().get(ptr);
            SupervisedLearningModel model = (SupervisedLearningModel)path.getModel();
            Classifier c = model.getClassifier();
            wrapperSelector.classifier = c;
            int[] indexes = wrapperSelector.getFeaturesForInstances(data);
            System.out.println("completed model check:" + ptr);
            ptr++;
        }

        System.out.println("done");
    }
    
}
