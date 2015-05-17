/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class KNNModelBuilder implements LearningModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    private static final int defaultNumNeighbors = 10;
    
    public KNNModelBuilder() {
        classifier = new IBk();
        ((IBk)classifier).setKNN(defaultNumNeighbors);
    }
    
    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public Model build(String name) throws Exception {
       if (trainingData == null) {
           throw new IllegalStateException("Must set training examples (to not null) before building model");
       }
       IBk m = (IBk)WekaModelBuilderHelper.build(classifier, trainingData);
       return new KNNModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return true;
    }
    
    public KNNModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof KNNModelBuilder) {
            return new KNNModelBuilder();
        }
        return null;
    }
}
