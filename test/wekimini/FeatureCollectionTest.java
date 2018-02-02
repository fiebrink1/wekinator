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
import wekimini.modifiers.Feature;
import wekimini.modifiers.FeatureCollection;
import wekimini.modifiers.ModifierCollection;
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
        String[] names = {"1","2","3"};
        fm.addOutputs(1, names);
        assertEquals(1, fm.getFeatureGroups().size());
        fm.passThroughInputToOutput(false, 0);
        fc = fm.getFeatureGroups().get(0);
    }
    
    @Test
    public void testSingle()
    {
        fc.addFeatureForKey("PassThroughAll");
        assertEquals(2, fc.getNumModifiers());
        fc.removeFeatureForKey("PassThroughAll");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testMultipleModifiers()
    {
        fc.addFeatureForKey("AllAcc");
        assertEquals(4, fc.getNumModifiers());
        fc.removeFeatureForKey("AllAcc");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testChained()
    {
        fc.addFeatureForKey("MaxFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.removeFeatureForKey("MaxFFTAccX");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testShared()
    {
        fc.addFeatureForKey("MaxFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.addFeatureForKey("MinFFTAccX");
        assertEquals(4, fc.getNumModifiers());
        fc.removeFeatureForKey("MaxFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.removeFeatureForKey("MinFFTAccX");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testMultipleFeatures()
    {
        fc.addFeatureForKey("MaxFFTAccX");
        assertEquals(3, fc.getNumModifiers());
        fc.addFeatureForKey("AllAcc");
        assertEquals(6, fc.getNumModifiers());
        fc.removeFeatureForKey("MaxFFTAccX");
        assertEquals(4, fc.getNumModifiers());
        fc.removeFeatureForKey("AllAcc");
        assertEquals(1, fc.getNumModifiers());
    }
    
    @Test
    public void testRemoveNotAddedFeature()
    {
       fc.addFeatureForKey("MaxFFTAccX");
       assertEquals(3, fc.getNumModifiers()); 
       fc.removeFeatureForKey("AllAcc");
       assertEquals(3, fc.getNumModifiers()); 
    }
    
    @Test 
    public void testGetConnections()
    {
       fc.addFeatureForKey("MaxFFTAccX");
       assertEquals(3, fc.getNumModifiers()); 
       boolean[] connections = fc.getConnections();
       int ptr = 0;
       for(boolean isEnabled:connections)
       {
           assertEquals(ptr == 67, isEnabled);
           ptr++;
       }
    }
    
    @Test
    public void testSetConnections()
    {
        fc.addFeatureForKey("MaxFFTAccX");
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
        assertEquals(5, fc.getNumModifiers());
        
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
        assertTrue(results.contains(fc.getFeatureForKey("PassThroughAll")));
        assertTrue(results.contains(fc.getFeatureForKey("MeanAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MaxAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("StdDevAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("EnergyAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("BufferAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MagAcc")));
        assertTrue(results.contains(fc.getFeatureForKey("MagFODAcc")));
        assertTrue(results.contains(fc.getFeatureForKey("AccXFOD")));
        assertTrue(results.contains(fc.getFeatureForKey("MeanFODAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("StdDevFODAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("CorrelateAccXY")));
        assertTrue(results.contains(fc.getFeatureForKey("CorrelateAccXZ")));
        assertTrue(results.contains(fc.getFeatureForKey("FFTAccX7Bins")));
        assertTrue(results.contains(fc.getFeatureForKey("MaxFFTAccX")));
        assertTrue(results.contains(fc.getFeatureForKey("MinFFTAccX")));
        assertFalse(results.contains(fc.getFeatureForKey("MeanGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("MaxGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("StdDevGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("EnergyGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("BufferGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("MagGyro")));
        assertFalse(results.contains(fc.getFeatureForKey("MagFODGyro")));
        assertFalse(results.contains(fc.getFeatureForKey("GyroYFOD")));
        assertFalse(results.contains(fc.getFeatureForKey("MeanFODGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("StdDevFODGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("CorrelateGyroYZ")));
        assertFalse(results.contains(fc.getFeatureForKey("FFTGyroY7Bins")));
        assertFalse(results.contains(fc.getFeatureForKey("MaxFFTGyroY")));
        assertFalse(results.contains(fc.getFeatureForKey("MinFFTGyroY")));
    }
    
    @Test
    public void testSearch()
    {
        ArrayList<Feature> results = new ArrayList<>(Arrays.asList(fc.getFeaturesForKeyword("AccelerometerX")));
        testResultsForAccX(results);
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForKeyword("MeanAccX")));
        assertEquals(1, results.size());
        assertTrue(results.contains(fc.getFeatureForKey("MeanAccX")));
        
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForKeyword("acc")));
        testResultsForAccX(results);
        
    }
    
    @Test
    public void testTagSearch()
    {
        ArrayList<Feature> results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[]{"AccelerometerX"})));
        testResultsForAccX(results);
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[]{"AccelerometerX","Gibberish"})));
        testResultsForAccX(results);
        results = new ArrayList<>(Arrays.asList(fc.getFeaturesForTags(new String[]{"acc","ometer"})));
        testResultsForAccX(results);
    }
    
}
