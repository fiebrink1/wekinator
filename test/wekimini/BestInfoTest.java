/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import weka.core.Instance;
import weka.core.Instances;
import wekimini.featureanalysis.BestInfoSelector;
import wekimini.featureanalysis.InfoGainSelector;

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
        int[] attibutes = {0, 10, 20, 30, 40, 50};
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeAll();
        int ptr = 0;
        for(int attributeIndex:attibutes)
        {
            String name = w.getDataManager().featureManager.getAllFeatures().getModifiers().nameForIndex(attributeIndex);
            String[] split = name.split(":");
            w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(split[0]);
        }
        Instances user = w.getDataManager().getFeatureInstances(false).get(0);
        Instances filtered = w.getDataManager().getFeaturesInstancesFromIndices(attibutes, 0, false);
        for(int i = 0; i < user.numInstances(); i++)
        {
            Instance userIn = user.instance(i);
            Instance filterIn = filtered.instance(i);
            for(int a = 0; a < userIn.numAttributes(); a++)
            {
                System.out.println(userIn.value(a) + ","  + filterIn.value(a));
                assertEquals(userIn.value(a), filterIn.value(a), 0.0);
            }
        }
    }  
    
    @Test
    public void testMatchingAllFeatures()
    {
        w.getDataManager().featureManager.getFeatureGroups().get(0).removeAll();
        int ptr = 0;
        String[] names = w.getDataManager().featureManager.getFeatureNames();
        for(String name:names)
        {
            w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey(name);
        }
        Instances user = w.getDataManager().getFeatureInstances(false).get(0);
        Instances filtered = w.getDataManager().getAllFeaturesInstances(0, false);
        for(int i = 0; i < user.numInstances(); i++)
        {
            Instance userIn = user.instance(i);
            Instance filterIn = filtered.instance(i);
            for(int a = 0; a < userIn.numAttributes(); a++)
            {
                System.out.println(userIn.value(a) + ","  + filterIn.value(a));
                assertEquals(userIn.value(a), filterIn.value(a), 0.0);
            }
        }
    }  

}
