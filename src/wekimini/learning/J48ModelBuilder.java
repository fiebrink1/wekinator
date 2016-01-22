/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.awt.Component;
import weka.classifiers.Classifier;
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
public class J48ModelBuilder implements ClassificationModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    
    public J48ModelBuilder() {
        classifier = new J48();
    }
    
    @Override
    public String toLogString() {
        return "J48";
    }
    
    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public J48Model build(String name) throws Exception {
       if (trainingData == null) {
           throw new IllegalStateException("Must set training examples (to not null) before building model");
       }
       J48 m = (J48)WekaModelBuilderHelper.build(classifier, trainingData);
       return new J48Model(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCClassificationOutput);
    }
    
    @Override
    public J48ModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof J48ModelBuilder) {
            return new J48ModelBuilder();
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Decision Tree (J48)";
    }

    @Override
    public J48EditorPanel getEditorPanel() {
        return new J48EditorPanel(this);
    }

    @Override
    public Classifier getClassifier() {
        return classifier;
    }
}
