/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.awt.Component;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class SVMModelBuilder implements ClassificationModelBuilder {

    private transient Instances trainingData = null;
    private transient Classifier classifier = null;

    public static enum KernelType {

        LINEAR, RBF, POLYNOMIAL
    };
    private KernelType kernelType;
    private double polyExponent = 2.0;
    private boolean polyUseLowerOrder = false;
    private double rbfGamma = 0.;
    private double complexity = 1.0;
    
    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SVM,KERNEL=").append(kernelType);
        sb.append(",PEXP=").append(polyExponent);
        sb.append(",LOWER=").append(polyUseLowerOrder);
        sb.append(",GAMMA=").append(rbfGamma);
        sb.append(",C=").append(complexity);
        return sb.toString();
    }

    public SVMModelBuilder() {
        classifier = new SMO();
        kernelType = KernelType.POLYNOMIAL;
        PolyKernel k = new PolyKernel();
        k.setExponent(polyExponent);
        ((SMO) classifier).setKernel(k);
    }

    public double getPolyExponent() {
        return polyExponent;
    }

    public KernelType getKernelType() {
        return kernelType;
    }

    public double getRbfGamma() {
        return rbfGamma;
    }

    public void setPolyExponent(double e) {
        this.polyExponent = e;
        PolyKernel k = new PolyKernel();
        k.setExponent(polyExponent);
        k.setUseLowerOrder(polyUseLowerOrder);
        ((SMO) classifier).setKernel(k);
    }

    public boolean getPolyUseLowerOrder() {
        return polyUseLowerOrder;
    }

    public void setPolyUseLowerOrder(boolean u) {
        polyUseLowerOrder = u;
        PolyKernel k = new PolyKernel();
        k.setExponent(polyExponent);
        k.setUseLowerOrder(polyUseLowerOrder);
        ((SMO) classifier).setKernel(k);
    }

    public double getComplexity() {
        return complexity;
    }

    private void updateClassifier() {
        if (kernelType == KernelType.LINEAR) {
            Kernel k = getClassifier().getKernel();
            if (k instanceof PolyKernel && ((PolyKernel) k).getExponent() == 1.0) {
                //do nothing;  already got it
            } else {
                PolyKernel nk = new PolyKernel();
                nk.setExponent(1.0);
                ((SMO) classifier).setKernel(nk);
            }
        } else if (kernelType == KernelType.POLYNOMIAL) {
            Kernel k = getClassifier().getKernel();
            if (k instanceof PolyKernel) {
                ((PolyKernel) k).setExponent(polyExponent);
                ((PolyKernel) k).setUseLowerOrder(polyUseLowerOrder);
                //return;
            } else {
                PolyKernel nk = new PolyKernel();
                nk.setExponent(polyExponent);
                nk.setUseLowerOrder(polyUseLowerOrder);
                getClassifier().setKernel(nk);
            }
        } else { //RBF
            Kernel k = getClassifier().getKernel();
            if (k instanceof RBFKernel) {
                ((RBFKernel) k).setGamma(rbfGamma);
               // return;
            } else {
                RBFKernel nk = new RBFKernel();
                nk.setGamma(rbfGamma);
                getClassifier().setKernel(nk);
            }
        }
        ((SMO) classifier).setC(complexity);
    }

    public void setComplexity(double c) {
        complexity = c;
        updateClassifier();
    }

    @Override
    public SMO getClassifier() {
        return (SMO) classifier;
    }

    public void setLinearKernel() {
        kernelType = KernelType.LINEAR;
        updateClassifier();
    }

    public void setPolyKernel(double e, boolean useLowerOrder) {
        kernelType = KernelType.POLYNOMIAL;
        polyExponent = e;
        polyUseLowerOrder = useLowerOrder;
        updateClassifier();
    }

    public void setRbfKernel(double gamma) {
        kernelType = KernelType.RBF;
        rbfGamma = gamma;
        updateClassifier();
    }

    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public SVMModel build(String name) throws Exception {
        if (trainingData == null) {
            throw new IllegalStateException("Must set training examples (to not null) before building model");
        }
        SMO m = (SMO) WekaModelBuilderHelper.build(classifier, trainingData);
        return new SVMModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCClassificationOutput);
    }

    public SVMModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof SVMModelBuilder) {
            SVMModelBuilder mb = new SVMModelBuilder();
            SVMModelBuilder template = (SVMModelBuilder) b;
            if (template.getKernelType() == KernelType.LINEAR) {
                mb.setLinearKernel();
            } else if (template.getKernelType() == KernelType.POLYNOMIAL) {
                mb.setPolyKernel(template.getPolyExponent(), template.getPolyUseLowerOrder());
            } else {
                mb.setRbfKernel(template.getRbfGamma());
            }
            mb.setComplexity(template.getComplexity());
            return mb;
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Support Vector Machine";
    }

    @Override
    public LearningModelBuilderEditorPanel getEditorPanel() {
        return new SVMEditorPanel(this);
    }
}
