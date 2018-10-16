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
import org.junit.Test;
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
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            w.getDataManager().doNormalise = false;
        } catch (Exception e)
        {
            
        }
    }
    
    
    @Test 
    public void testGetFeaturesByIndices()
    {
        //JUST THE RAW INPUTS
        int[] indices = {0,1,2,3,4,5};
        
        //TRAINING SET, OUTPUT 0
        Instances selected = w.getDataManager().getFeaturesInstancesFromIndices(indices, 0, false);
        assertEquals(indices.length + 1, selected.numAttributes());
        assertEquals(100, selected.numInstances());
        
        //TEST SET, OUTPUT 0
        selected = w.getDataManager().getFeaturesInstancesFromIndices(indices, 0, true);
        assertEquals(indices.length + 1, selected.numAttributes());
        assertEquals(200, selected.numInstances());
    }
    
//    @Test
//    public void testInfoGainSelection() throws InterruptedException
//    {
//        BestInfoSelector sel = new BestInfoSelector(w);
//        sel.outputIndex = 0;
//        Method method;
//        try {
//            method = w.getDataManager().getClass().getDeclaredMethod("updateFeatureInstances", int.class, boolean.class, boolean.class);
//            method.setAccessible(true);
//            method.invoke(w.getDataManager(), 0, false, true);
//        } catch (Exception ex) {
//            Logger.getLogger(FeatureSelectorTest.class.getName()).log(Level.SEVERE, null, ex);
//        } 
//        Instances allFeatures = w.getDataManager().getAllFeaturesInstances(0, false);
//        sel.getAttributeIndicesForInstances(allFeatures, new BestInfoSelector.BestInfoResultsReceiver() {
//             @Override
//             public void finished(int[] features)
//             {
//                 assertEquals(true, features.length > 5);
//                 System.out.println("done");
//             }
//        });
//    } 
}
