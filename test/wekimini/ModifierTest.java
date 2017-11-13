/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import org.junit.Ignore;

/**
 *
 * @author louismccallum
 */
@Ignore public interface ModifierTest {
            
    public void testForTraining(int windowSize);
    public void testForRunning(int windowSize) throws InterruptedException;
    public void testTraining();
    public void testTrainingChangingWindowSize();
    public void testRunning() throws InterruptedException;
    public void testRunningThenTraining() throws InterruptedException;
    
}
