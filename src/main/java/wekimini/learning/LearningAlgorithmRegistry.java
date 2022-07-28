/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import wekimini.LearningModelBuilder;

/**
 *
 * @author rebecca
 */
public class LearningAlgorithmRegistry {
    public static LearningModelBuilder[] getClassificationModelBuilders() {
        LearningModelBuilder[] mbs = new LearningModelBuilder[6];
        mbs[0] = new KNNModelBuilder();
        mbs[1] = new AdaboostModelBuilder();
        mbs[2] = new J48ModelBuilder();
        mbs[3] = new SVMModelBuilder();
        mbs[4] = new NaiveBayesModelBuilder();
        mbs[5] = new DecisionStumpModelBuilder();
        return mbs;
    }
    
    public static LearningModelBuilder[] getNumericModelBuilders() {
       LearningModelBuilder[] mbs = new LearningModelBuilder[2];
       mbs[0] = new NeuralNetModelBuilder();
       mbs[1] = new LinearRegressionModelBuilder();
       return mbs;         
    } 
    
}
