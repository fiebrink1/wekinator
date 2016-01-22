/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.awt.Component;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class KNNModelBuilder implements ClassificationModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    private static final int defaultNumNeighbors = 1;
    private int numNeighbors = defaultNumNeighbors;
    
    @Override
    public String toLogString() {
        return "KNN,K=" + numNeighbors;
    }
    
    public KNNModelBuilder() {
        classifier = new IBk();
        ((IBk)classifier).setKNN(defaultNumNeighbors);
    }
    
    public KNNModelBuilder(int numNeighbors) {
        this.numNeighbors = numNeighbors;
        classifier = new IBk();
        ((IBk)classifier).setKNN(numNeighbors);
    }
    
    public int getNumNeighbors() {
        return numNeighbors;
    }
    
    public void setNumNeighbors(int n) {
        numNeighbors = n;
        ((IBk)classifier).setKNN(numNeighbors);
    }
            
    
    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public KNNModel build(String name) throws Exception {
       if (trainingData == null) {
           throw new IllegalStateException("Must set training examples (to not null) before building model");
       }
       IBk m = (IBk)WekaModelBuilderHelper.build(classifier, trainingData);
       return new KNNModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCClassificationOutput);
    }
    
    public KNNModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof KNNModelBuilder) {
            return new KNNModelBuilder(((KNNModelBuilder)b).getNumNeighbors());
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "k-Nearest Neighbor";
    }

    @Override
    public KNNEditorPanel getEditorPanel() {
        return new KNNEditorPanel(this);
    }

    @Override
    public Classifier getClassifier() {
        return classifier;
    }
}
