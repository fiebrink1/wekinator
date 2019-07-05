/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import wekimini.modifiers.BufferedInput;
import wekimini.modifiers.Feature;
import wekimini.modifiers.FeatureCollection;
import wekimini.modifiers.FeatureSingleModifierOutput;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.MultipleInputWindowedOperation;
import wekimini.modifiers.PassThroughSingle;
import wekimini.modifiers.WindowedOperation;

/**
 *
 * @author louismccallum
 */
public class FeatureManagerTest {
    
    FeatureManager fm;
    
    @Before
    public void setUp()
    {
        fm = new FeatureManager();
        String[] names = {"1","2","3"};
        fm.addOutputs(1, names);
        assertEquals(1, fm.getFeatureGroups().size());
        fm.passThroughInputToOutput(false, 0);
        fm.getFeatureGroups().get(0).removeAll();
    }
    
    @Test
    public void testAddModifier()
    {
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredModifierID(0);
        fm.addModifierToOutput(modifier, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getNumModifiers());
    }
    
    @Test
    public void testAddTwoIdenticalModifiers()
    {
        //Exactly the same, skip second
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredModifierID(0);
        int id1 = fm.addModifierToOutput(modifier, 0);
        PassThroughSingle modifier2 = new PassThroughSingle("2",0,0);
        modifier2.addRequiredModifierID(0);
        int id2 = fm.addModifierToOutput(modifier2, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getNumModifiers());
        assertTrue(id1 == id2);
       
        //Differ on pass through, should add
        modifier2.addToOutput = false;
        fm.addModifierToOutput(modifier2, 0);
        assertEquals(3, fm.getFeatureGroups().get(0).getNumModifiers());
        
        //Differ on input index, should add
        PassThroughSingle modifier3 = new PassThroughSingle("3",1,0);
        modifier3.addRequiredModifierID(0);
        fm.addModifierToOutput(modifier3, 0);
        assertEquals(4, fm.getFeatureGroups().get(0).getNumModifiers());
    }
    
    @Test
    public void testRemove()
    {
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredModifierID(0);
        int id1 = fm.addModifierToOutput(modifier, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getNumModifiers());
        
        //Can't remove passthrough
        fm.removeModifierFromOutput(0, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getNumModifiers());
        
        //Can remove added
        fm.removeModifierFromOutput(id1,0);
        assertEquals(1, fm.getFeatureGroups().get(0).getNumModifiers());
    }
    
    @Test 
    public void testRemoveChained()
    {
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredModifierID(0);
        int id1 = fm.addModifierToOutput(modifier, 0);
        
        PassThroughSingle modifier2 = new PassThroughSingle("1",1,0);
        modifier2.addRequiredModifierID(id1);
        int id2 = fm.addModifierToOutput(modifier2, 0);
        
        assertEquals(3, fm.getFeatureGroups().get(0).getNumModifiers());

        fm.removeModifierFromOutput(id1, 0);
        assertEquals(3, fm.getFeatureGroups().get(0).getNumModifiers());
        
        fm.removeModifierFromOutput(id2, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getNumModifiers());
        
        fm.removeModifierFromOutput(id1, 0);
        assertEquals(1, fm.getFeatureGroups().get(0).getNumModifiers());
    }
    
    @Test
    public void testRemoveOrphaned()
    {
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredModifierID(0);
        int id1 = fm.addModifierToOutput(modifier, 0);
        
        PassThroughSingle modifier2 = new PassThroughSingle("1",1,0);
        modifier2.addRequiredModifierID(Integer.MAX_VALUE);
        int id2 = fm.addModifierToOutput(modifier2, 0);
        
        assertEquals(3, fm.getFeatureGroups().get(0).getNumModifiers());
        
        fm.getFeatureGroups().get(0).getModifiers().removeOrphanedModifiers();
        assertEquals(2, fm.getFeatureGroups().get(0).getNumModifiers());   
    }
    
    @Test
    public void testRemoveDeadEnds()
    {
        PassThroughSingle modifierDeadEnd = new PassThroughSingle("1",0,0);
        modifierDeadEnd.addRequiredModifierID(0);
        modifierDeadEnd.addToOutput = false;
        int id1 = fm.addModifierToOutput(modifierDeadEnd, 0);
        
        PassThroughSingle modifierParent = new PassThroughSingle("1",0,0);
        modifierParent.addRequiredModifierID(0);
        modifierParent.addToOutput = false;
        int id2 = fm.addModifierToOutput(modifierParent, 0);
        
        PassThroughSingle modifierChild = new PassThroughSingle("1",0,0);
        modifierChild.addRequiredModifierID(id2);
        modifierChild.addToOutput = true;
        int id3 = fm.addModifierToOutput(modifierChild, 0);
        
        fm.getFeatureGroups().get(0).getModifiers().removeDeadEnds();
        
        assertEquals(3, fm.getFeatureGroups().get(0).getNumModifiers());
        
    }
    
//    @Test
//    public void testChangingWindowSizeWihAddedFeatures()
//    {
//        FeatureCollection fc = fm.getFeatureGroups().get(0);
//        fc.clearAdded();
//        fc.addFeatureForKey("MeanAccX");
//        FeatureSingleModifierOutput added = (FeatureSingleModifierOutput) fc.getCurrentFeatures()[0];
//        ModifiedInput outputModifier = fm.getAllFeatures().getModifiers().getModifierForID(added.getOutputModifierID());
//        fm.setFeatureWindowSize(20,30);
//        ModifiedInput newOutputModifier = fm.getAllFeatures().getModifiers().getModifierForID(added.getOutputModifierID());
//    }
//    
    @Test
    public void testChangingWindowSize()
    {
        int ws = 20;
        int bs = 30;
        fm.setFeatureWindowSize(ws,bs);
        for(FeatureCollection fc:fm.getFeatureGroups())
        {
            testWindowSizeForFeatureGroup(ws, bs, fc);
        }
        testWindowSizeForFeatureGroup(ws, bs, fm.getAllFeatures());
        
        ws = 5;
        bs = 50;
        fm.setFeatureWindowSize(ws,bs);
        for(FeatureCollection fc:fm.getFeatureGroups())
        {
            testWindowSizeForFeatureGroup(ws, bs, fc);
        }
        testWindowSizeForFeatureGroup(ws, bs, fm.getAllFeatures());
    }
    
    public void testWindowSizeForFeatureGroup(int ws, int bs, FeatureCollection fc)
    {
        for(ModifiedInput modifier:fc.getModifiers().getModifiers())
        {
            if(modifier instanceof WindowedOperation)
            {
               assertEquals(ws,((WindowedOperation)modifier).windowSize);
            }
            else if (modifier instanceof BufferedInput)
            {
                assertEquals(bs,((BufferedInput)modifier).bufferSize);
            }
        }
    }
    
    @Test
    public void testReloadFeatures()
    {
        Wekinator w = null;
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSetFeatures/WekinatorTestSet/WekinatorTestSet.wekproj");
        try {
            
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            
            w.getDataManager().featureManager.featureCollections.get(0).removeAll();
            w.getDataManager().featureManager.featureCollections.get(0).addFeatureForKey("MeanAccX");
            w.getDataManager().featureManager.featureCollections.get(0).addFeatureForKey("MeanAccY");
            
            w.getDataManager().featureManager.featureCollections.get(1).removeAll();
            w.getDataManager().featureManager.featureCollections.get(1).addFeatureForKey("MeanGyroX");
            w.getDataManager().featureManager.featureCollections.get(1).addFeatureForKey("MeanGyroY");
            
            w.getDataManager().featureManager.setFeatureWindowSize(30, 50);
            
            w.save();
            
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
            
            Feature[] f = w.getDataManager().featureManager.featureCollections.get(0).getCurrentFeatures();
            checkFeature(f,"MeanAccX");
            checkFeature(f,"MeanAccY");
            
            f = w.getDataManager().featureManager.featureCollections.get(1).getCurrentFeatures();
            checkFeature(f,"MeanGyroX");
            checkFeature(f,"MeanGyroY");
            
            assertEquals(30, w.getDataManager().featureManager.getFeatureWindowSize());
            assertEquals(50, w.getDataManager().featureManager.getFeatureBufferSize());
        } 
        catch (Exception e)
        {
            
        }
    }
    
    public void checkFeature(Feature[] f, String name)
    {
        boolean found = false;
        for(Feature feature:f)
        {
            if(feature.name.equals(name))
            {
                found = true;
            }
        }
        assertTrue(found);
    }
    
}
