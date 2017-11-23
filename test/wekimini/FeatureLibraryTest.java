/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
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
        fg.addFeatureForKey("MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.removeFeatureForKey("MaxFFT");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testShared()
    {
        fg.addFeatureForKey("MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.addFeatureForKey("MinFFT");
        assertEquals(4, fg.getModifiers().size());
        fg.removeFeatureForKey("MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.removeFeatureForKey("MinFFT");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testMultipleFeatures()
    {
        fg.addFeatureForKey("MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.addFeatureForKey("AllAcc");
        assertEquals(6, fg.getModifiers().size());
        fg.removeFeatureForKey("MaxFFT");
        assertEquals(4, fg.getModifiers().size());
        fg.removeFeatureForKey("AllAcc");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testRemoveNotAddedFeature()
    {
       fg.addFeatureForKey("MaxFFT");
       assertEquals(3, fg.getModifiers().size()); 
       fg.removeFeatureForKey("AllAcc");
       assertEquals(3, fg.getModifiers().size()); 
    }
    
    @Test 
    public void testGetConnections()
    {
       fg.addFeatureForKey("MaxFFT");
       assertEquals(3, fg.getModifiers().size()); 
       boolean[] connections = fg.getConnections();
       int ptr = 0;
       for(boolean isEnabled:connections)
       {
           assertEquals(ptr == 2, isEnabled);
           ptr++;
       }
    }
    
    @Test
    public void testSetConnections()
    {
        fg.addFeatureForKey("MaxFFT");
        assertEquals(3, fg.getModifiers().size()); 
        
        boolean[] onOff = {true,true,false,false};
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
}
