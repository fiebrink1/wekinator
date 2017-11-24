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
import wekimini.modifiers.PassThroughSingle;

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
    }
    
    @Test
    public void testAddModifier()
    {
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredModifierID(0);
        fm.addModifierToOutput(modifier, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getModifiers().size());
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
        assertEquals(2, fm.getFeatureGroups().get(0).getModifiers().size());
        assertTrue(id1 == id2);
       
        //Differ on pass through, should add
        modifier2.addToOutput = false;
        fm.addModifierToOutput(modifier2, 0);
        assertEquals(3, fm.getFeatureGroups().get(0).getModifiers().size());
        
        //Differ on input index, should add
        PassThroughSingle modifier3 = new PassThroughSingle("3",1,0);
        modifier3.addRequiredModifierID(0);
        fm.addModifierToOutput(modifier3, 0);
        assertEquals(4, fm.getFeatureGroups().get(0).getModifiers().size());
    }
    
    @Test
    public void testRemove()
    {
        PassThroughSingle modifier = new PassThroughSingle("1",0,0);
        modifier.addRequiredModifierID(0);
        int id1 = fm.addModifierToOutput(modifier, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getModifiers().size());
        
        //Can't remove passthrough
        fm.removeModifierFromOutput(0, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getModifiers().size());
        
        //Can remove added
        fm.removeModifierFromOutput(id1,0);
        assertEquals(1, fm.getFeatureGroups().get(0).getModifiers().size());
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
        
        assertEquals(3, fm.getFeatureGroups().get(0).getModifiers().size());

        fm.removeModifierFromOutput(id1, 0);
        assertEquals(3, fm.getFeatureGroups().get(0).getModifiers().size());
        
        fm.removeModifierFromOutput(id2, 0);
        assertEquals(2, fm.getFeatureGroups().get(0).getModifiers().size());
        
        fm.removeModifierFromOutput(id1, 0);
        assertEquals(1, fm.getFeatureGroups().get(0).getModifiers().size());
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
        
        assertEquals(3, fm.getFeatureGroups().get(0).getModifiers().size());
        
        fm.getFeatureGroups().get(0).removeOrphanedModifiers();
        assertEquals(2, fm.getFeatureGroups().get(0).getModifiers().size());   
    }
    
}
