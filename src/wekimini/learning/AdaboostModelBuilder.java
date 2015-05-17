/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class AdaboostModelBuilder implements LearningModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    private static final int defaultNumRounds = 100;
    private static final boolean isBaseTree = true;
    
    public AdaboostModelBuilder() {
        classifier = new AdaBoostM1();
       // ((AdaBoostM1) classifier).setClassifier(new DecisionStump());
        ((AdaBoostM1)classifier).setClassifier(new J48());
        ((AdaBoostM1) classifier).setNumIterations(defaultNumRounds);
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
       AdaBoostM1 m = (AdaBoostM1)WekaModelBuilderHelper.build(classifier, trainingData);
       return new AdaboostModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCClassificationOutput);
    }
    
    public AdaboostModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof AdaboostModelBuilder) {
            return new AdaboostModelBuilder();
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "AdaBoost.M1";
    }
}
