/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import wekimini.LearningModelBuilder;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class NeuralNetModelBuilder implements RegressionModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;

    @Override
    public Classifier getClassifier() {
        return classifier;
    }
    
    public enum HiddenLayerType {NUM_FEATURES, NUMBER};
    
    private int numHiddenLayers = 1;
    private HiddenLayerType hiddenLayerType = HiddenLayerType.NUM_FEATURES;
    private int numNodesPerHiddenLayer = 1;
    
    private static final Logger logger = Logger.getLogger(NeuralNetModelBuilder.class.getName());
    
    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NEURALNET,NUM_HID=").append(numHiddenLayers);
        sb.append(",HID_TYPE=").append(hiddenLayerType);
        if (hiddenLayerType == HiddenLayerType.NUMBER) {
            sb.append(",NUMPERHID=").append(numNodesPerHiddenLayer);
        }
        return sb.toString();
    }
    
    public NeuralNetModelBuilder() {
        classifier = new MultilayerPerceptron();
        setHiddenLayers(1, HiddenLayerType.NUM_FEATURES, 0);
        //((MultilayerPerceptron)classifier).setHiddenLayers("i");
    }

    public int getNumHiddenLayers() {
        return numHiddenLayers;
    }

    public HiddenLayerType getHiddenLayerType() {
        return hiddenLayerType;
    }

    public int getNumNodesPerHiddenLayer() {
        return numNodesPerHiddenLayer;
    }
    
    public final void setHiddenLayers(int numHiddenLayers, HiddenLayerType hiddenLayerType, int numNodesPerHiddenLayer) {
        if (numHiddenLayers < 0) {
            throw new IllegalArgumentException("Must have 0 or more hidden layers");
        }
        if (numHiddenLayers == 0) {
            this.numHiddenLayers = 0;
            this.hiddenLayerType = HiddenLayerType.NUMBER;
            this.numNodesPerHiddenLayer = 0;
            ((MultilayerPerceptron)classifier).setHiddenLayers("0");
            return;
        }
        
        if (hiddenLayerType == HiddenLayerType.NUMBER && numNodesPerHiddenLayer < 1) {
            throw new IllegalArgumentException("Must have 1 or more nodes per hidden layer");
        }
        
        this.numHiddenLayers = numHiddenLayers;
        this.hiddenLayerType = hiddenLayerType;
        this.numNodesPerHiddenLayer = numNodesPerHiddenLayer;

        String s = createLayerString();
        logger.log(Level.INFO, "Creating neural network builder with string {0}", s);
        ((MultilayerPerceptron)classifier).setHiddenLayers(s);
    }
    
    
    
    private String createLayerString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numHiddenLayers-1; i++) {
            sb.append(getStringForLayer(i));
            sb.append(",");
        }
        sb.append(getStringForLayer(numHiddenLayers-1));
        return sb.toString();
    }
    
    private String getStringForLayer(int i) {
        if (hiddenLayerType == HiddenLayerType.NUMBER) {
            return Integer.toString(numNodesPerHiddenLayer);
        } else { //LayerContents.NUM_FEATURES
            return "i";
        }
    }
    
    @Override
    public void setTrainingExamples(Instances examples) {
        trainingData = examples;
    }

    @Override
    public NeuralNetworkModel build(String name) throws Exception {
       if (trainingData == null) {
           throw new IllegalStateException("Must set training examples (to not null) before building model");
       }
       MultilayerPerceptron m = (MultilayerPerceptron)WekaModelBuilderHelper.build(classifier, trainingData);
       return new NeuralNetworkModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCNumericOutput);
    }
    
    @Override
    public NeuralNetModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof NeuralNetModelBuilder) {
            NeuralNetModelBuilder mb = new NeuralNetModelBuilder();
            NeuralNetModelBuilder old = ((NeuralNetModelBuilder)b);
            mb.setHiddenLayers(old.getNumHiddenLayers(), old.getHiddenLayerType(), old.getNumNodesPerHiddenLayer());
            return mb;
        }
        return null;
    }

    @Override
    public String getPrettyName() {
        return "Neural Network";
    }

    @Override
    public NeuralNetEditorPanel getEditorPanel() {
        return new NeuralNetEditorPanel(this);
    }
}
