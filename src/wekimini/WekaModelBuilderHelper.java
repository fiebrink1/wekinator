/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 *
 * @author rebecca
 */
public class WekaModelBuilderHelper  {    
    public static Classifier build(Classifier c, Instances data) throws Exception {
        c.buildClassifier(data);
        return Classifier.makeCopy(c); //TODO: May have to do Classifier.makeCopy
    }   
}
