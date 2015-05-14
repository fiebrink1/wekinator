/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import weka.core.Instances;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class SimpleModelBuilder implements LearningModelBuilder {
    
    
    @Override
    public void setTrainingExamples(Instances examples) {
        //Nothing to do
    }

    @Override
    public Model build(String name) throws Exception {
        //For testing
       // double d = Math.random();
        Thread.sleep(2000); //Not a great example of how other code will function, since sleep() does throw Interrupted exception.
       // if (d < 0.5) {
            return new SimpleModel(name);
       // } else {
       //     throw new Exception("Testing");
       // }
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return true;
    }
    
}
