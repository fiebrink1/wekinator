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
        fg = fm.getFeatureGroups().get(0);
    }
    
    @Test
    public void testSingle()
    {
        fg.featureLibrary.addFeatureForKey(fg, "PassThroughFirst");
        assertEquals(2, fg.getModifiers().size());
        fg.featureLibrary.removeFeatureForKey(fg, "PassThroughFirst");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testMultipleModifiers()
    {
        fg.featureLibrary.addFeatureForKey(fg, "JustAccelerometer");
        assertEquals(4, fg.getModifiers().size());
        fg.featureLibrary.removeFeatureForKey(fg, "JustAccelerometer");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testChained()
    {
        fg.featureLibrary.addFeatureForKey(fg, "MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.featureLibrary.removeFeatureForKey(fg, "MaxFFT");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testShared()
    {
        fg.featureLibrary.addFeatureForKey(fg, "MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.featureLibrary.addFeatureForKey(fg, "MinFFT");
        assertEquals(4, fg.getModifiers().size());
        fg.featureLibrary.removeFeatureForKey(fg, "MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.featureLibrary.removeFeatureForKey(fg, "MinFFT");
        assertEquals(1, fg.getModifiers().size());
    }
    
    @Test
    public void testMultipleFeatures()
    {
        fg.featureLibrary.addFeatureForKey(fg, "MaxFFT");
        assertEquals(3, fg.getModifiers().size());
        fg.featureLibrary.addFeatureForKey(fg, "JustAccelerometer");
        assertEquals(6, fg.getModifiers().size());
        fg.featureLibrary.removeFeatureForKey(fg, "MaxFFT");
        assertEquals(4, fg.getModifiers().size());
        fg.featureLibrary.removeFeatureForKey(fg, "JustAccelerometer");
        assertEquals(1, fg.getModifiers().size());
    }
}
