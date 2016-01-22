/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class NaiveBayesModelBuilder implements ClassificationModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    
    @Override
    public String toLogString() {
        return "NAIVEBAYES";
    }
    
    public NaiveBayesModelBuilder() {
        classifier = new NaiveBayes();
    }
    
    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public NaiveBayesModel build(String name) throws Exception {
       if (trainingData == null) {
           throw new IllegalStateException("Must set training examples (to not null) before building model");
       }
       NaiveBayes m = (NaiveBayes)WekaModelBuilderHelper.build(classifier, trainingData);
       return new NaiveBayesModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCClassificationOutput);
    }
    
    @Override
    public NaiveBayesModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof NaiveBayesModelBuilder) {
            return new NaiveBayesModelBuilder();
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Naive Bayes";
    }

    @Override
    public NaiveBayesEditorPanel getEditorPanel() {
        return new NaiveBayesEditorPanel(this);
    }

    @Override
    public Classifier getClassifier() {
        return classifier;
    }
}
