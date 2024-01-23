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
    public static Classifier build(Classifier c, Instances data) throws WekaException {
        try {
            // These methods require callers to catch Exception or declare `throws WekaException`,
            // so convert their errors into a named exception instead. This prevents the throws
            // declaration from hiding other exceptions that may occur in the same method.
            c.buildClassifier(data);
            return Classifier.makeCopy(c);
        } catch (Exception e) {
            throw new WekaException(e);
        }
    }
}
