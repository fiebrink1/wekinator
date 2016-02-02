/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import weka.core.SelectedTag;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class LinearRegressionModelBuilder implements RegressionModelBuilder {

    private transient Instances trainingData = null;
    private transient Classifier classifier = null;

    public enum FeatureSelectionType {

        NONE, M5, GREEDY
    };

    private FeatureSelectionType featureSelectionType;
    private static final Logger logger = Logger.getLogger(LinearRegressionModelBuilder.class.getName());
    private int exponent = 1;
    private boolean removeColinear = false;

    
    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LINPOLREG,EXPONENT=").append(exponent);
        sb.append(",FEAT=").append(featureSelectionType);
        sb.append(",REMOVE=").append(removeColinear);
        return sb.toString();
    }
    
    public int getExponent() {
        return exponent;
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
    }

    public LinearRegressionModelBuilder() {
        classifier = new LinearRegression();
        featureSelectionType = FeatureSelectionType.NONE;
        ((LinearRegression) classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
        ((LinearRegression) classifier).setEliminateColinearAttributes(removeColinear);
    }

    public FeatureSelectionType getFeatureSelectionType() {
        return featureSelectionType;
    }

    public boolean isRemoveColinear() {
        return removeColinear;
    }

    public void setRemoveColinear(boolean removeColinear) {
        this.removeColinear = removeColinear;
        ((LinearRegression) classifier).setEliminateColinearAttributes(removeColinear);
    }

    public void setFeatureSelectionType(FeatureSelectionType newType) {
        featureSelectionType = newType;
        switch (newType) {
            case NONE:
                ((LinearRegression) classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
                break;
            case M5:
                ((LinearRegression) classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_M5, LinearRegression.TAGS_SELECTION));
                break;
            case GREEDY:
            default:
                ((LinearRegression) classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_GREEDY, LinearRegression.TAGS_SELECTION));
        }
    }

    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public LinearRegressionModel build(String name) throws Exception {
        if (trainingData == null) {
            throw new IllegalStateException("Must set training examples (to not null) before building model");
        }
        LinearRegression m;
        LinearRegressionAttributeTransformer t = null;
        if (exponent == 1) {
            m = (LinearRegression) WekaModelBuilderHelper.build(classifier, trainingData);
        } else {
            t = new LinearRegressionAttributeTransformer(trainingData.numAttributes() - 1, exponent);
            Instances i = t.transformedData(trainingData);
            System.out.println("New instances:\n" + i.toString());
            m = (LinearRegression) WekaModelBuilderHelper.build(classifier, i);
        }
        //Remove: for testing:
        logger.log(Level.WARNING, m.toString());
        return new LinearRegressionModel(name, m, t);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCNumericOutput);
    }

    @Override
    public LinearRegressionModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof LinearRegressionModelBuilder) {
            LinearRegressionModelBuilder mb = new LinearRegressionModelBuilder();
            mb.setRemoveColinear(((LinearRegressionModelBuilder) b).isRemoveColinear());
            mb.setExponent(((LinearRegressionModelBuilder) b).getExponent());
            mb.setFeatureSelectionType(((LinearRegressionModelBuilder) b).getFeatureSelectionType());
            return mb;
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Linear/Polynomial Regression";
    }

    @Override
    public LinearRegressionEditorPanel getEditorPanel() {
        return new LinearRegressionEditorPanel(this);
    }

    @Override
    public Classifier getClassifier() {
        return classifier;
    }
}
