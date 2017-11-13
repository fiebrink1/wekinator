/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author louismccallum
 */
@Ignore public class ModifierTest {
            
    public Wekinator w;
            
   @Before
    public void setUp() {
        String fileLocation = ("/Users/louismccallum/Documents/Goldsmiths/Wekinator_Projects/WekinatorTestSet/WekinatorTestSet/WekinatorTestSet.wekproj");
        try{
            w = WekinatorSaver.loadWekinatorFromFile(fileLocation);
        } catch (Exception e)
        {
            
        }
    }
    public void testForTraining(int windowSize)
    {
        
    }
    
    public void testForRunning(int windowSize) throws InterruptedException
    {
        
    }
    
    @Test
    public void testTraining()
    {
        testForTraining(10);
    }
    
    @Test
    public void testTrainingChangingWindowSize()
    {
        testForTraining(10);
        testForTraining(5);
    }
    
    @Test
    public void testRunning() throws InterruptedException
    {
        testForRunning(10);
    }
    
    @Test
    public void testRunningThenTraining() throws InterruptedException
    {
        testForRunning(10);
        testForTraining(10);
    }
    
}
