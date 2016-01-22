/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import weka.classifiers.Classifier;
import weka.classifiers.trees.DecisionStump;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class DecisionStumpModelBuilder implements ClassificationModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    
    public DecisionStumpModelBuilder() {
        classifier = new DecisionStump();
    }
    
    @Override
    public String toLogString() {
        return "DECISIONSTUMP";
    }
    
    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public DecisionStumpModel build(String name) throws Exception {
       if (trainingData == null) {
           throw new IllegalStateException("Must set training examples (to not null) before building model");
       }
       DecisionStump m = (DecisionStump)WekaModelBuilderHelper.build(classifier, trainingData);
       return new DecisionStumpModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCClassificationOutput);
    }
    
    @Override
    public DecisionStumpModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof DecisionStumpModelBuilder) {
            return new DecisionStumpModelBuilder();
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Decision Stump (ONLY for educational purposes!)";
    }

    @Override
    public DecisionStumpEditorPanel getEditorPanel() {
        return new DecisionStumpEditorPanel(this);
    }

    @Override
    public Classifier getClassifier() {
        return classifier;
    }
}
