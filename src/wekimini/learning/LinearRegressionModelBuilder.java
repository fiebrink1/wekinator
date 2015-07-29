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
public class LinearRegressionModelBuilder implements LearningModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    public enum FeatureSelectionType {NONE, M5, GREEDY};
    private transient FeatureSelectionType featureSelectionType;
    private static final Logger logger = Logger.getLogger(LinearRegressionModelBuilder.class.getName());
    
    public LinearRegressionModelBuilder() {
        classifier = new LinearRegression();
        //((LinearRegression)classifier).setHiddenLayers("i");
        //TODO: Set up default parameters here! Any others? Ridge parameter?
        
        featureSelectionType = FeatureSelectionType.NONE;
        ((LinearRegression)classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
    }
    
    public FeatureSelectionType getFeatureSelectionType() {
        return featureSelectionType;
    }
    
    public void setFeatureSelectionType(FeatureSelectionType newType) {
        featureSelectionType = newType;
        switch(newType) {
            case NONE:
                ((LinearRegression)classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
                break;
            case M5:
                ((LinearRegression)classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_M5, LinearRegression.TAGS_SELECTION));
                break;
            case GREEDY:
            default:
                ((LinearRegression)classifier).setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_GREEDY, LinearRegression.TAGS_SELECTION));
        }
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
       LinearRegression m = (LinearRegression)WekaModelBuilderHelper.build(classifier, trainingData);
       //Remove: for testing:
       logger.log(Level.WARNING, m.toString());
       return new LinearRegressionModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCNumericOutput);
    }
    
    @Override
    public LinearRegressionModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof LinearRegressionModelBuilder) {
            LinearRegressionModelBuilder mb = new LinearRegressionModelBuilder();
            mb.setFeatureSelectionType(((LinearRegressionModelBuilder)b).getFeatureSelectionType());
            return mb;
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Linear Regression";
    }

    @Override
    public LinearRegressionEditorPanel getEditorPanel() {
        return new LinearRegressionEditorPanel(this);
    }
}
