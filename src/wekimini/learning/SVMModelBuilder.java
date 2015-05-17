/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class SVMModelBuilder implements LearningModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    
    
    public SVMModelBuilder() {
        classifier = new SMO();
        PolyKernel k = new PolyKernel();
        k.setExponent(2.0);
        ((SMO)classifier).setKernel(k);
    }
    
    private SMO getClassifier() {
        return (SMO)classifier;
    }
    
    public void setLinearKernel() {
        Kernel k = getClassifier().getKernel();
        if (k instanceof PolyKernel && ((PolyKernel)k).getExponent() == 1.0) {
            return; // already got it
        }
        else {
            PolyKernel nk = new PolyKernel();
            nk.setExponent(1.0);
            getClassifier().setKernel(nk);
        }
    }

    public void setPolyKernel(double e, boolean useLowerOrder) {
        Kernel k = getClassifier().getKernel();
        if (k instanceof PolyKernel) {
            ((PolyKernel)k).setExponent(e);
            ((PolyKernel)k).setUseLowerOrder(useLowerOrder);
            return;
        }
        else {
            PolyKernel nk = new PolyKernel();
            nk.setExponent(e);
            nk.setUseLowerOrder(useLowerOrder);
            getClassifier().setKernel(nk);
        }
    }

    public void setRbfKernel(double gamma) {
        Kernel k = getClassifier().getKernel();
        if (k instanceof RBFKernel) {
            ((RBFKernel)k).setGamma(gamma);
            return;
        }
        else {
            RBFKernel nk = new RBFKernel();
            nk.setGamma(gamma);
            getClassifier().setKernel(nk);
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
       SMO m = (SMO)WekaModelBuilderHelper.build(classifier, trainingData);
       return new SVMModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return true;
    }
    
    public SVMModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof SVMModelBuilder) {
            return new SVMModelBuilder();
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Support Vector Machine";
    }
}
