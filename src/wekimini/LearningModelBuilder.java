/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import weka.core.Instances;

/**
 *
 * @author rebecca
 */
public interface LearningModelBuilder extends ModelBuilder {
    public static enum TrainingState {NOT_TRAINED, TRAINING, TRAINED};
    
    public void setTrainingExamples(Instances examples);
    
    
}
