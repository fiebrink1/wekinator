/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import weka.core.Instance;
import wekimini.featureanalysis.WrapperSelector;
import wekimini.featureanalysis.InfoGainSelector;
import wekimini.learning.SupervisedLearningModel;
import weka.classifiers.Classifier;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import weka.core.Attribute;
import weka.core.FastVector;
import java.util.Random;
import weka.classifiers.lazy.IBk;
import weka.core.converters.ArffSaver;
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
       return "/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/Smallest6/WekinatorProject1/WekinatorProject1.wekproj";
    }
    
    public Instances getTestSet(int numInstances, int numClasses, int goodFeatures, int badFeatures, double howGood)
    {
        int numAtt = goodFeatures + badFeatures;
        FastVector ff = new FastVector(numAtt);
        for(int i = 0; i < numAtt; i++)
        {
            ff.addElement(new Attribute("feature" + i));
        }
        
        ff.addElement(new Attribute("output"));
        Instances newInst = new Instances("testSet", ff, numInstances);

        Random rand = new Random();
        for(double i = 0; i < numInstances; i++)
        {
            double [] vals = new double[numAtt + 1];
            double outputClass = Math.floor((i / (double)numInstances) * numClasses);
            for(int j = 0; j < numAtt; j ++)
            {
                double r = rand.nextDouble();
                if(j < goodFeatures)
                {
                    if(rand.nextDouble() < howGood)
                    {
                        r = r / (double)numClasses;
                        vals[j] = ((outputClass+1.0)/(double)numClasses) - r;
                    }
                    else
                    {
                        vals[j] = r;
                    }
                    //System.out.println("val["+j+"] = " + vals[j] + " outputClass = " + outputClass + " r = " + r);
                }
                else
                {
                    vals[j] = r;
                }
                    
            }
            vals[numAtt] = outputClass;
            Instance example = new Instance(1.0, vals);
            newInst.add(example);

        }     
        newInst.setClassIndex(numAtt);
        return newInst;
    }
    
    @Test
    public void testSetGenerator()
    {
        Instances data = getTestSet(50, 2, 2, 10, 1.0);
        assertEquals(13, data.numAttributes(), 0);
        assertEquals(50, data.numInstances(), 0);
    }
    
    @Test 
    public void testUpdateAllFeatures()
    {
        Method method;
        try {
            method = w.getDataManager().getClass().getDeclaredMethod("updateAllFeaturesInstances");
            method.setAccessible(true);
            method.invoke(w.getDataManager());
            int attributes = w.getDataManager().getAllFeaturesInstances().numAttributes();
            int allFeaturesOutputSize = w.getDataManager().featureManager.getAllFeaturesGroup().getOutputDimensionality();
            assertEquals(allFeaturesOutputSize, attributes - 1, 0);
            //assertEquals(w.getDataManager().featureManager.getAllFeaturesGroup().valueMap)
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testAutomaticSelect() throws InterruptedException
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(50);
        w.getDataManager().selectFeaturesAutomatically(true);
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(2000);
        assertEquals(SupervisedLearningManager.LearningState.DONE_TRAINING,w.getSupervisedLearningManager().getLearningState());
        w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
        for(int instanceIndex = 0; instanceIndex < 50; instanceIndex++)
        {
            double[] oscInputs = {instanceIndex + 1, 1.0, instanceIndex % 10 == 9 ? 0.9 : 0.1, 0, 0, 0};
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(oscInputs, 0);
            double [] computed = w.getSupervisedLearningManager().computeValues(oscInputs, new boolean[]{true});
            assertEquals(5.0, instance.numAttributes(),0);
            assertEquals(1.0, computed.length,0);
        } 
    }
    
    @Test
    public void testWrapperKnn() throws IOException
    {
        Instances data = getTestSet(500, 4, 4, 150, 0.5);
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File("./data/test_" + System.currentTimeMillis() + ".arff"));
        saver.writeBatch();
        WrapperSelector wrapperSelector = new WrapperSelector();
        IBk knn = new IBk();
        knn.setKNN(1);
        wrapperSelector.classifier = knn;
        int[] indexes = wrapperSelector.getAttributeIndicesForInstances(data);
        System.out.println("Selected : " + Arrays.toString(indexes));
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
            int[] indexes = wrapperSelector.getAttributeIndicesForInstances(data);
            System.out.println("completed model check:" + ptr);
            ptr++;
        }

        System.out.println("done");
    }
    
    @Test
    public void testInfoGainSelection() throws InterruptedException
    {
        InfoGainSelector sel = new InfoGainSelector();
        Method method;
        try {
            method = w.getDataManager().getClass().getDeclaredMethod("updateAllFeaturesInstances");
            method.setAccessible(true);
            method.invoke(w.getDataManager());
        } catch (Exception ex) {
            Logger.getLogger(FeatureSelectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        Instances allFeatures = w.getDataManager().getAllFeaturesInstances();
        int[] indexes = sel.getAttributeIndicesForInstances(allFeatures);
        System.out.println("done");
    }
    
}
