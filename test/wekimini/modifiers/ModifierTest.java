/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import org.junit.Ignore;
import wekimini.Wekinator;

/**
 *
 * @author louismccallum
 */
@Ignore public interface ModifierTest {
            
    public void testForTraining(int windowSize);
    public void testForRunning(int windowSize) throws InterruptedException;
    public void testSingle();
    public void testChangingWindowSize();
    public void testRunning() throws InterruptedException;
    public void testRunningThenTraining() throws InterruptedException;
    
}
