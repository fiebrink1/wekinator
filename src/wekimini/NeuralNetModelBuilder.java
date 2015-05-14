/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class NeuralNetModelBuilder implements LearningModelBuilder {
    Instances trainingData = null;
    Classifier classifier = null;
    
    
    public NeuralNetModelBuilder() {
        classifier = new MultilayerPerceptron();
        ((MultilayerPerceptron)classifier).setHiddenLayers("i");
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
       MultilayerPerceptron m = (MultilayerPerceptron)WekaModelBuilderHelper.build(classifier, trainingData);
       return new NeuralNetworKModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return true;
    }
    
}
