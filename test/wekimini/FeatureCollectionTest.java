/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import wekimini.modifiers.BufferedInput;
import wekimini.modifiers.Feature;
import wekimini.modifiers.FeatureCollection;
import wekimini.modifiers.ModifierCollection;
import wekimini.modifiers.PassThroughSingle;
/**
 *
 * @author louismccallum
 */

public class FeatureCollectionTest {
    FeatureManager fm;
    ModifierCollection modifiers;
    FeatureCollection fc;
    
    @Before
    public void setUp()
    {
        fm = new FeatureManager();
        String[] names = {"1","2","3","4","5","6"};
        fm.addOutputs(1, names);
        assertEquals(1, fm.getFeatureGroups().size());
        fm.passThroughInputToOutput(false, 0);
        fc = fm.getFeatureGroups().get(0);
        fc.removeAll();
    }
    
    @Test
    public void testSingle()
    {
        fc.addFeatureForKey("AccX");
        assertEquals(2, fc.getNumModifiers());
        fc.removeFeatureForKey("AccX");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testMultipleModifiers()
    {
        fc.addFeatureForKey("MeanFODAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.removeFeatureForKey("MeanFODAccX");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testChained()
    {
        fc.addFeatureForKey("MaxBinFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.removeFeatureForKey("MaxBinFFTAccX");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testShared()
    {
        fc.addFeatureForKey("MaxBinFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.addFeatureForKey("MinBinFFTAccX");
        assertEquals(4, fc.getNumModifiers());
        fc.removeFeatureForKey("MaxBinFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.removeFeatureForKey("MinBinFFTAccX");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testMultipleFeatures()
    {
        fc.addFeatureForKey("MaxBinFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.addFeatureForKey("MeanFODAccX");
        assertEquals(5, fc.getNumModifiers());
        fc.removeFeatureForKey("MaxBinFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.removeFeatureForKey("MeanFODAccX");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testRemoveNotAddedFeature()
    {
       fc.addFeatureForKey("MaxBinFFTAccX");
       assertEquals(3, fc.getNumModifiers()); 
       fc.removeFeatureForKey("MeanFODAccX");
       assertEquals(3, fc.getNumModifiers()); 
    }
    
    @Test 
    public void testGetConnections()
    {
       fc.addFeatureForKey("MaxBinFFTAccX");
       assertEquals(3, fc.getNumModifiers()); 
       boolean[] connections = fc.getConnections();
       int ptr = 0;
       for(boolean isEnabled:connections)
       {
           assertEquals(ptr == 164, isEnabled);
           ptr++;
       }
    }
    
    @Test
    public void testSetConnections()
    {
        fc.addFeatureForKey("MaxBinFFTAccX");
        assertEquals(3, fc.getNumModifiers()); 
        
        boolean[] onOff = new boolean[fc.getNames().length];
        onOff[0] = true;
        onOff[1] = true;
        fc.setSelectedFeatures(onOff);
        
        boolean[] connections = fc.getConnections();
        int ptr = 0;
        for(boolean isEnabled:connections)
        {
           assertEquals(ptr < 2, isEnabled);
           ptr++;
        }
        assertEquals(3, fc.getNumModifiers());
        
    }
    
    @Test
    public void testChangeWindowSize()
    {
        Field field;
        try {
            field = fc.getClass().getDeclaredField("featureLibrary");
            field.setAccessible(true);
            FeatureCollection library = (FeatureCollection)field.get(fc);
            fc.addFeatureForKey("BufferAccX");
            assertEquals(2, fc.getNumModifiers()); 
            assertEquals(10, fc.getModifiers().getOutputDimensionality());
            int ptr = 0;
            boolean [] connections = library.getConnections();
            for(boolean onOff:connections)
            {
                assertEquals(ptr == 27, onOff);
                ptr++;
            }
            
            fc.setFeatureWindowSize(10,20);
            assertEquals(2, fc.getNumModifiers()); 
            assertEquals(20, fc.getModifiers().getOutputDimensionality());
            ptr = 0;
            connections = library.getConnections();
            for(boolean onOff:connections)
            {
                assertEquals(ptr == 27, onOff);
                ptr++;
            }
        } catch (Exception e)
        {
            
        }
    }
    
    @Test
    public void testDiagram()
    {
        //THERES SOME WEIRDNESS GOING ON WITH IMPORTING THIS ENUM AND RUNNING TESTS, IGNORING FOR NOW
//        Feature ft = fc.getFeatureForKey("MeanAccX");
//        assertEquals(wekimini.modifiers.InputDiagram.ACCX, ft.diagram);
//        ft = fc.getFeatureForKey("MeanAccY");
//        assertEquals(wekimini.modifiers.InputDiagram.ACCY, ft.diagram);
//        ft = fc.getFeatureForKey("MeanAccZ");
//        assertEquals(wekimini.modifiers.InputDiagram.ACCZ, ft.diagram);
//        ft = fc.getFeatureForKey("MeanGyroX");
//        assertEquals(wekimini.modifiers.InputDiagram.GYROX, ft.diagram);
//        ft = fc.getFeatureForKey("MeanGyroY");
//        assertEquals(wekimini.modifiers.InputDiagram.GYROY, ft.diagram);
//        ft = fc.getFeatureForKey("MeanGyroZ");
//        assertEquals(wekimini.modifiers.InputDiagram.GYROZ, ft.diagram);
    }
    
    public void testResultsForAccX(ArrayList<Feature> results)
    {
        assertTrue(results.contains(fc.getFeatureForKey("AccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MeanAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MaxAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("StdDevAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("EnergyAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MagAcc")));
        assertTrue(results.contains(fc.getFeatureForKey("MagFODAcc")));
        assertTrue(results.contains(fc.getFeatureForKey("FODAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MeanFODAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("StdDevFODAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("CorrelateAccXY")));
        assertTrue(results.contains(fc.getFeatureForKey("CorrelateAccXZ")));
        assertTrue(results.contains(fc.getFeatureForKey("MaxBinFFTAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MinBinFFTAccX")));
        assertFalse(results.contains(fc.getFeatureForKey("MeanGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("MaxGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("StdDevGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("EnergyGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("MagGyro")));
        assertFalse(results.contains(fc.getFeatureForKey("MagFODGyro")));
        assertFalse(results.contains(fc.getFeatureForKey("FODGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("MeanFODGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("StdDevFODGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("CorrelateGyroYZ")));
        assertFalse(results.contains(fc.getFeatureForKey("MaxBinFFTGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("MinBinFFTGyroY")));
    }
    
    @Test
    public void testSearch()
    {
        ArrayList<Feature> results = new ArrayList<>(Arrays.asList(fc.getFeaturesForKeyword("AccelerometerX", false)));
        testResultsForAccX(results);
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForKeyword("MeanAccX", false)));
        assertEquals(1, results.size());
        assertTrue(results.contains(fc.getFeatureForKey("MeanAccX")));
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForKeyword("acc", false)));
        testResultsForAccX(results);
        
    }
    
    @Test
    public void testTagSearch()
    {
        ArrayList<Feature> results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[]{"AccelerometerX"}, false)));
        testResultsForAccX(results);
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[]{"AccelerometerX","NotATag"}, false)));
        assertEquals(results.size(),0);
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[]{"AccelerometerX","Mean"}, false)));
        assertEquals(3, results.size());
        assertTrue(results.contains(fc.getFeatureForKey("MeanAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MeanFODAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MeanMagAcc")));
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[0], false)));
        assertEquals(0, results.size());
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[]{"Accelerometer"}, false)));
        assertTrue(results.size() > 0);
    }
    
    @Test
    public void testBufferDimensionality()
    {
        int winSize = 10;
        int inputs = 6;
        for(int i = 0; i < inputs; i++)
        {
            for(int j = 0; j < winSize; j++)
            {
                BufferedInput bufmodifier = new BufferedInput("test" + i, i, winSize, 0);
                bufmodifier.addToOutput = false;
                bufmodifier.addRequiredModifierID(0);
                int id1 = fc.getModifiers().addModifier(bufmodifier);

                PassThroughSingle modifier = new PassThroughSingle("Buffertest" + i + j, j, 0);
                modifier.addRequiredModifierID(id1);
                modifier.addToOutput = true;
                int id2 = fc.getModifiers().addModifier(modifier);
            }
        }
        assertEquals(60, fc.getModifiers().getOutputDimensionality());
    }
    
    @Test
    public void equalityTest()
    {
        BufferedInput bufmodifier = new BufferedInput("test-1", 0, 10, 0);
        bufmodifier.addToOutput = false;
        bufmodifier.addRequiredModifierID(0);
        int id1 = fc.getModifiers().addModifier(bufmodifier);
        
        PassThroughSingle modifier = new PassThroughSingle("Buffertest1" + 0, 0, 0);
        modifier.addRequiredModifierID(id1);
        modifier.addToOutput = true;
        int id2 = fc.getModifiers().addModifier(modifier);
        
        BufferedInput bufmodifier2 = new BufferedInput("test-2", 1, 10, 0);
        bufmodifier2.addToOutput = false;
        bufmodifier2.addRequiredModifierID(0);
        int id3 = fc.getModifiers().addModifier(bufmodifier2);
        assertTrue(id3 != id1);
        
        PassThroughSingle modifier2 = new PassThroughSingle("Buffertest2" + 0, 0, 0);
        modifier2.addRequiredModifierID(id3);
        modifier2.addToOutput = true;
        int id4 = fc.getModifiers().addModifier(modifier2);
        assertTrue(id2 != id4);
        
        BufferedInput bufmodifier3 = new BufferedInput("test-3", 1, 10, 0);
        bufmodifier3.addToOutput = false;
        bufmodifier3.addRequiredModifierID(0);
        int id5 = fc.getModifiers().addModifier(bufmodifier3);
        assertTrue(id5 != id1);
        assertTrue(id5 == id3);
        
        PassThroughSingle modifier3 = new PassThroughSingle("Buffertest3" + 1, 1, 0);
        modifier3.addRequiredModifierID(id5);
        modifier3.addToOutput = true;
        int id6 = fc.getModifiers().addModifier(modifier3);
        assertTrue(id2 != id6);
        assertTrue(id4 != id6);
    }
    
}
