/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import wekimini.learning.SimpleModel;
import wekimini.learning.NeuralNetworkModel;
import wekimini.learning.Model;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.DecisionStump;
import wekimini.learning.AdaboostModel;
import wekimini.learning.DecisionStumpModel;
import wekimini.learning.J48Model;
import wekimini.learning.KNNModel;
import wekimini.learning.LinearRegressionModel;
import wekimini.learning.NaiveBayesModel;
import wekimini.learning.SVMModel;

/**
 *
 * @author rebecca
 */
public class ModelLoader {
    private static final Logger logger = Logger.getLogger(ModelLoader.class.getName());
    
    public static Model loadModel(Class c, ObjectInputStream is) throws IOException, ClassNotFoundException {
        if (c == SimpleModel.class) {
            return SimpleModel.readFromInputStream(is);
        } else if (c == NeuralNetworkModel.class) {
            return NeuralNetworkModel.readFromInputStream(is);  
        } else if (c == AdaboostModel.class) {
            return AdaboostModel.readFromInputStream(is);
        } else if (c == KNNModel.class) {
            return KNNModel.readFromInputStream(is);
        } else if (c == J48Model.class){ 
            return J48Model.readFromInputStream(is);
        } else if (c == SVMModel.class) {
            return SVMModel.readFromInputStream(is);
        } else if (c == LinearRegressionModel.class)  {
            return LinearRegressionModel.readFromInputStream(is);
        } else if (c == DecisionStumpModel.class)  {
            return DecisionStumpModel.readFromInputStream(is);
        } else if (c == NaiveBayesModel.class)  {
            return NaiveBayesModel.readFromInputStream(is);
        } else {
            logger.log(Level.WARNING, "Could not find input reader for class {0}", c);
            return null;
        }
    }
}
