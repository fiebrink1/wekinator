/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
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
}
