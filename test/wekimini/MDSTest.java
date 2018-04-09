/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author louismccallum
 */
public class MDSTest {
        
    public Wekinator w;
    
    public void setUp() {
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            w.getDataManager().doNormalise = true;
        } 
        catch (Exception e)
        {
            
        }
    }
    
    @Test
    public void testReduction()
    {
        setUp();
        w.getDataManager().featureManager.removeAllModifiersFromOutput(0);
        w.getDataManager().featureManager.passThroughInputToOutput(false, 0);
        
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("AccX");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("AccY");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("AccZ");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("MeanAccX");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("MeanAccY");
        w.getDataManager().featureManager.getFeatureGroups().get(0).addFeatureForKey("MeanAccZ");
        
        w.getDataManager().updateMDSInstances(0);
        
        assertEquals(w.getDataManager().getTrainingDataForOutput(0).numInstances(),w.getDataManager().getMDSInstances(0).numInstances(),0);
        assertEquals(3, w.getDataManager().getMDSInstances(0).numAttributes(), 0);
    }
    
}
