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
    public Model build(String name) {
        return new SimpleModel(name);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return true;
    }
    
}
