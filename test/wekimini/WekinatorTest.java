/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import wekimini.gui.MainGUI;
import wekimini.osc.OSCMonitor;
import wekimini.osc.OSCReceiver;
import wekimini.osc.OSCSender;

/**
 *
 * @author louismccallum
 */
public class WekinatorTest {
    
    Wekinator w;
    
    public WekinatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } catch (Exception e)
        {
            
        }
    }
    
    @Test
    public void testNumInputs()
    {
        System.out.println("Num inputs " + w.getInputManager().getNumInputs());
        int expResult = 3;
        int result = w.getInputManager().getNumInputs();
        assertEquals(expResult,result);
    }
    
    @After
    public void tearDown() {
    }  
}
