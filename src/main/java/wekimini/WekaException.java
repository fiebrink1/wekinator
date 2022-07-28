/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * A custom checked exception to use when a Weka class throws a raw exception.
 *
 * This allows Wekimini code to catch WekaException instead of Exception.
 */
public class WekaException extends Exception {
    public WekaException (String message) {
        super(message);
    }
    public WekaException (String message, Throwable err) {
        super(message, err);
    }
    public WekaException (Throwable err) {
        super(err);
    }
}
