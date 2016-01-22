/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.awt.Component;
import weka.classifiers.Classifier;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class SimpleModelBuilder implements ClassificationModelBuilder {
    
    @Override
    public String toLogString() {
        return "SIMPLE";
    }
    
    @Override
    public void setTrainingExamples(Instances examples) {
        //Nothing to do
    }

    @Override
    public SimpleModel build(String name) throws Exception {
        //For testing
       // double d = Math.random();
        Thread.sleep(2000); //Not a great example of how other code will function, since sleep() does throw Interrupted exception.
       // if (d < 0.5) {
            return new SimpleModel(name) {

            @Override
            public Classifier getClassifier() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
       // } else {
       //     throw new Exception("Testing");
       // }
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return true;
    }

    @Override
    public LearningModelBuilder fromTemplate(ModelBuilder template) {
        if (template instanceof SimpleModelBuilder) {
            return new SimpleModelBuilder();
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Simple Model (testing only)";
    }

    @Override
    public LearningModelBuilderEditorPanel getEditorPanel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Classifier getClassifier() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
