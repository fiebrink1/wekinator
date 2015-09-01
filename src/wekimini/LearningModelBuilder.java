/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import weka.classifiers.Classifier;
import wekimini.learning.ModelBuilder;
import weka.core.Instances;
import wekimini.learning.Model;
import wekimini.learning.SupervisedLearningModel;

/**
 *
 * @author rebecca
 */
public interface LearningModelBuilder extends ModelBuilder {
    public static enum TrainingState {NOT_TRAINED, TRAINING, TRAINED};
    
    public void setTrainingExamples(Instances examples);
    
    @Override
    public SupervisedLearningModel build(String name) throws Exception;

    @Override
    public LearningModelBuilder fromTemplate(ModelBuilder template);
    
    public Classifier getClassifier();

}
