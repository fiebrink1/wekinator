/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author louismccallum
 */
public class BestInfoTest {
    
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
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet6Inputs/6Inputs/6Inputs.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            w.getDataManager().doNormalise = false;
        } catch (Exception e)
        {
            
        }
    }
    
    @Test
    public void testMatchingAcrossSets()
    {
        int[] attributes = {0, 10, 20, 30, 40, 50};
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeAll();
        for(int attributeIndex:attributes)
        {
            String name = w.getDataManager().featureManager.getAllFeatures().getModifiers().nameForIndex(attributeIndex);
            String[] split = name.split(":");
            w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(split[0]);
        }
        Instances user = w.getDataManager().getFeatureInstances(false).get(0);
        Instances filtered = w.getDataManager().getFeaturesInstancesFromIndices(attributes, 0, false);
        for(int i = 0; i < user.numInstances(); i++)
        {
            Instance userIn = user.instance(i);
            Instance filterIn = filtered.instance(i);
            for(int a = 0; a < userIn.numAttributes(); a++)
            {
                //System.out.println(userIn.value(a) + ","  + filterIn.value(a));
                assertEquals(userIn.value(a), filterIn.value(a), 0.0);
            }
        }
    }  
    
    @Test
    public void testMatchingAllFeatures()
    {
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeAll();
        String[] names = w.getDataManager().featureManager.getFeatureNames();
        //Make sure the raw inputs are added in the correct order
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("AccX");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("AccY");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("AccZ");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("GyroX");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("GyroY");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("GyroZ");
        for(String name:names)
        {
            w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(name);
        }
        Instances user = w.getDataManager().getFeatureInstances(false).get(0);
        Instances filtered = w.getDataManager().getAllFeaturesInstances(0, false);
        for(int a = 0; a < user.numAttributes(); a++)
        {
            for(int i = 0; i < user.numInstances(); i++)
            {
                Instance userIn = user.instance(i);
                Instance filterIn = filtered.instance(i);
                assertEquals(userIn.value(a), filterIn.value(a), 0.0);
            }
        }
    }  
}