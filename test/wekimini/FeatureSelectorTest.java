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
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.core.converters.ArffSaver;
import wekimini.DataManager.AutoSelect;
import wekimini.featureanalysis.FeatureSelector;
import wekimini.featureanalysis.RandomSelector;
import wekimini.learning.NeuralNetModelBuilder;
import wekimini.modifiers.PassThroughVector;
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
        
        FastVector classes = new FastVector(numClasses);
        classes.addElement("0"); 
        for (int val = 0; val < numClasses; val++) {
            classes.addElement((new Integer(val + 1)).toString());
        }
        ff.addElement(new Attribute("output", classes));
        Instances newInst = new Instances("testSet", ff, numInstances);

        Random rand = new Random();
        for(double i = 0; i < numInstances; i++)
        {
            double [] vals = new double[numAtt + 1];
            double outputClass = Math.floor((i / (double)numInstances) * numClasses) + 1;
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
            method = w.getDataManager().getClass().getDeclaredMethod("updateFeatureInstances", int.class, boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(w.getDataManager(), 0, false, true);
            Instances firstInstances = w.getDataManager().getAllFeaturesInstances(0, false);
            int attributes = firstInstances.numAttributes();
            int allFeaturesOutputSize = w.getDataManager().featureManager.getAllFeaturesGroup().getModifiers().getOutputDimensionality();
            assertEquals(allFeaturesOutputSize, attributes - 1, 0);
            //assertEquals(w.getDataManager().featureManager.getAllFeaturesGroup().valueMap)
            //method.invoke(w.getDataManager());
        } catch (Exception e) {
            
        }
    }
    
    @Ignore
    @Test
    public void testWrapperKnn() throws IOException
    {
        Instances data = getTestSet(2000, 5, 5, 25, 0.5);
        WrapperSelector wrapperSelector = new WrapperSelector(true);
        IBk knn = new IBk();
        knn.setKNN(1);
        wrapperSelector.classifier = knn;
        int[] indexes = wrapperSelector.getAttributeIndicesForInstances(data);
        System.out.println("Selected : " + Arrays.toString(indexes));
    } 
    
   @Ignore
    @Test
    public void testWrapperPerceptron() throws IOException
    {
        Instances data = getTestSet(50, 4, 4, 10, 0.5);
        WrapperSelector wrapperSelector = new WrapperSelector(true);
        NeuralNetModelBuilder builder = new NeuralNetModelBuilder();
        MultilayerPerceptron classifier = (MultilayerPerceptron)builder.getClassifier();
        wrapperSelector.classifier = classifier;
        int[] indexes = wrapperSelector.getAttributeIndicesForInstances(data);
        System.out.println("Selected : " + Arrays.toString(indexes));
    } 
    
    @Ignore
    @Test
    public void testWrapperSelection() throws InterruptedException
    {
        w.getSupervisedLearningManager().setLearningState(SupervisedLearningManager.LearningState.READY_TO_TRAIN);
        w.getSupervisedLearningManager().stopRunning();
        w.getSupervisedLearningManager().buildAll();
        Thread.sleep(50);
        List<Instances> featureInstances = w.getDataManager().getFeatureInstances(false);
        WrapperSelector wrapperSelector = new WrapperSelector(true);
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
            method = w.getDataManager().getClass().getDeclaredMethod("updateFeatureInstances", int.class, boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(w.getDataManager(), 0, false, true);
        } catch (Exception ex) {
            Logger.getLogger(FeatureSelectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        Instances allFeatures = w.getDataManager().getAllFeaturesInstances(0, false);
        int[] indexes = sel.getAttributeIndicesForInstances(allFeatures);
        assertEquals(0.2,(double)indexes.length/(double)allFeatures.numAttributes(),0.01);
        System.out.println("done");
    }
    
    
    @Test
    public void testRandomSelection() throws InterruptedException
    {
        RandomSelector sel = new RandomSelector();
        sel.useThreshold = true;
        sel.threshold = 0.2;
        Method method;
        try {
            method = w.getDataManager().getClass().getDeclaredMethod("updateFeatureInstances", int.class, boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(w.getDataManager(), 0, false, true);
        } catch (Exception ex) {
            Logger.getLogger(FeatureSelectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        Instances allFeatures = w.getDataManager().getAllFeaturesInstances(0, false);
        int[] indexes = sel.getAttributeIndicesForInstances(allFeatures);
        assertEquals(0.2,(double)indexes.length/(double)allFeatures.numAttributes(),0.01);
        System.out.println("done");
    }
    
    
    @Test
    public void testDownSample()
    {
        int classes = 10;
        double examples = 200;
        double proportion = 0.2;
        Instances data = getTestSet((int)examples, classes, 4, 10, 0.5);
        Instances downSample = FeatureSelector.downSample(data, proportion);
        int[] classCtr = new int[classes + 1];
        assertEquals(examples*proportion, downSample.numInstances(), 0.0);
        for(int i = 0; i < downSample.numInstances(); i++)
        {
            Instance in = downSample.instance(i);
            int classVal =(int)in.classValue(); 
            classCtr[classVal]++; 
        }
        
        for(int i = 1; i < classCtr.length; i++)
        {
            assertEquals((examples*proportion)/classes, classCtr[i], 0);
        }
    }
    
    @Test
    public void testSequentialDownSample()
    {
        int classes = 10;
        double examples = 200;
        double proportion = 0.2;
        Instances data = getTestSet((int)examples, classes, 4, 10, 0.5);
        Instances downSample = FeatureSelector.sequentialDownSample(data, proportion);
        int[] classCtr = new int[classes + 1];
        assertEquals(examples*proportion, downSample.numInstances(), 0.0);
        for(int i = 0; i < downSample.numInstances(); i++)
        {
            Instance in = downSample.instance(i);
            int classVal =(int)in.classValue(); 
            classCtr[classVal]++; 
        }
        
        for(int i = 1; i < classCtr.length; i++)
        {
            assertEquals((examples*proportion)/classes, classCtr[i], 0);
        }
    }
}
