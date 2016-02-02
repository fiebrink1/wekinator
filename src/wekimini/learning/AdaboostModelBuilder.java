/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.awt.Component;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.DecisionStump;
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
public class AdaboostModelBuilder implements ClassificationModelBuilder {

    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    private static final int defaultNumRounds = 100;
    private int numRounds = defaultNumRounds;

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ADABOOST,NUM_ROUNDS=").append(numRounds);
        sb.append(",BASELEARN=").append(baseLearnerType);
        return sb.toString();
    }

    public static enum BaseLearner {

        DECISION_TREE, DECISION_STUMP
    };
    private BaseLearner baseLearnerType = BaseLearner.DECISION_TREE;

    public AdaboostModelBuilder() {
        classifier = new AdaBoostM1();
        // ((AdaBoostM1) classifier).setClassifier(new DecisionStump());
        ((AdaBoostM1) classifier).setClassifier(new J48());
        ((AdaBoostM1) classifier).setNumIterations(defaultNumRounds);
    }

    public AdaboostModelBuilder(int numRounds, BaseLearner t) {
        classifier = new AdaBoostM1();
        setNumRounds(numRounds);
        setBaseLearnerType(t);
    }

    public int getNumRounds() {
        return numRounds;
    }

    public BaseLearner getBaseLearnerType() {
        return baseLearnerType;
    }

    public void setNumRounds(int n) {
        numRounds = n;
        ((AdaBoostM1) classifier).setNumIterations(numRounds);
    }

    public void setBaseLearnerType(BaseLearner t) {
        baseLearnerType = t;
        if (t == BaseLearner.DECISION_STUMP) {
            ((AdaBoostM1) classifier).setClassifier(new DecisionStump());
        } else {
            ((AdaBoostM1) classifier).setClassifier(new J48());
        }
    }

    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public AdaboostModel build(String name) throws Exception {
        if (trainingData == null) {
            throw new IllegalStateException("Must set training examples (to not null) before building model");
        }
        AdaBoostM1 m = (AdaBoostM1) WekaModelBuilderHelper.build(classifier, trainingData);
        return new AdaboostModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCClassificationOutput);
    }

    public AdaboostModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof AdaboostModelBuilder) {
            AdaboostModelBuilder a = (AdaboostModelBuilder) b;
            return new AdaboostModelBuilder(a.getNumRounds(), a.getBaseLearnerType());
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "AdaBoost.M1";
    }

    @Override
    public LearningModelBuilderEditorPanel getEditorPanel() {
        return new AdaBoostEditorPanel(this);
    }

    @Override
    public Classifier getClassifier() {
        return classifier;
    }
}
