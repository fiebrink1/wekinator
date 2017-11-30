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
import wekimini.modifiers.FeatureLibrary;
/**
 *
 * @author louismccallum
 */
public class FeatureLibraryTest {
    FeatureManager fm;
    FeatureGroup fg;
    
    @Before
    public void setUp()
    {
        fm = new FeatureManager();
        String[] names = {"1","2","3"};
        fm.addOutputs(1, names);
        assertEquals(1, fm.getFeatureGroups().size());
        fm.passThroughInputToOutput(false, 0);
        fg = fm.getFeatureGroups().get(0);
    }
    
    @Test
    public void testSingle()
    {
        fg.addFeatureForKey("PassThroughAll");
        assertEquals(2, fg.getModifiers().size());
        fg.removeFeatureForKey("PassThroughAll");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testMultipleModifiers()
    {
        fg.addFeatureForKey("AllAcc");
        assertEquals(4, fg.getModifiers().size());
        fg.removeFeatureForKey("AllAcc");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testChained()
    {
        fg.addFeatureForKey("MaxFFTAccX");
        assertEquals(3, fg.getModifiers().size());
        fg.removeFeatureForKey("MaxFFTAccX");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testShared()
    {
        fg.addFeatureForKey("MaxFFTAccX");
        assertEquals(3, fg.getModifiers().size());
        fg.addFeatureForKey("MinFFTAccX");
        assertEquals(4, fg.getModifiers().size());
        fg.removeFeatureForKey("MaxFFTAccX");
        assertEquals(3, fg.getModifiers().size());
        fg.removeFeatureForKey("MinFFTAccX");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testMultipleFeatures()
    {
        fg.addFeatureForKey("MaxFFTAccX");
        assertEquals(3, fg.getModifiers().size());
        fg.addFeatureForKey("AllAcc");
        assertEquals(6, fg.getModifiers().size());
        fg.removeFeatureForKey("MaxFFTAccX");
        assertEquals(4, fg.getModifiers().size());
        fg.removeFeatureForKey("AllAcc");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testRemoveNotAddedFeature()
    {
       fg.addFeatureForKey("MaxFFTAccX");
       assertEquals(3, fg.getModifiers().size()); 
       fg.removeFeatureForKey("AllAcc");
       assertEquals(3, fg.getModifiers().size()); 
    }
    
    @Test 
    public void testGetConnections()
    {
       fg.addFeatureForKey("MaxFFTAccX");
       assertEquals(3, fg.getModifiers().size()); 
       boolean[] connections = fg.getConnections();
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
        fg.addFeatureForKey("MaxFFTAccX");
        assertEquals(3, fg.getModifiers().size()); 
        
        boolean[] onOff = new boolean[fg.getFeatureNames().length];
        onOff[0] = true;
        onOff[1] = true;
        fg.setSelectedFeatures(onOff);
        
        boolean[] connections = fg.getConnections();
        int ptr = 0;
        for(boolean isEnabled:connections)
        {
           assertEquals(ptr < 2, isEnabled);
           ptr++;
        }
        assertEquals(5, fg.getModifiers().size());
        
    }
    
    @Test
    public void testChangeWindowSize()
    {
        Field field;
        try {
            field = fg.getClass().getDeclaredField("featureLibrary");
            field.setAccessible(true);
            FeatureLibrary library = (FeatureLibrary)field.get(fg);
            fg.addFeatureForKey("BufferAccX");
            assertEquals(2, fg.getModifiers().size()); 
            assertEquals(10, fg.getOutputDimensionality());
            int ptr = 0;
            boolean [] connections = library.getConnections();
            for(boolean onOff:connections)
            {
                assertEquals(ptr == 27, onOff);
                ptr++;
            }
            
            fg.setFeatureWindowSize(20);
            assertEquals(2, fg.getModifiers().size()); 
            assertEquals(20, fg.getOutputDimensionality());
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
