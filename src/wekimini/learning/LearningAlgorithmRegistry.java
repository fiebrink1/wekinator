/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

/**
 *
 * @author rebecca
 */
public class LearningAlgorithmRegistry {
    public static ModelBuilder[] getClassificationModelBuilders() {
        ModelBuilder[] mbs = new ModelBuilder[4];
        mbs[0] = new KNNModelBuilder();
        mbs[1] = new AdaboostModelBuilder();
        mbs[2] = new J48ModelBuilder();
        mbs[3] = new SVMModelBuilder();
        return mbs;
    }
    
    public static ModelBuilder[] getNumericModelBuilders() {
       ModelBuilder[] mbs = new ModelBuilder[2];
       mbs[0] = new NeuralNetModelBuilder();
       mbs[1] = new LinearRegressionModelBuilder();
       return mbs;         
    } 
    
}
