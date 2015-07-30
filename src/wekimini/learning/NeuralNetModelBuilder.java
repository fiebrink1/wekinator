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
import wekimini.LoggingManager;
import wekimini.WekaModelBuilderHelper;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class NeuralNetModelBuilder implements LearningModelBuilder {
    private transient Instances trainingData = null;
    private transient Classifier classifier = null;
    
    public enum LayerContents {NUM_FEATURES, NUMBER};
    
    private int numHiddenLayers = 1;
    private LayerContents[] layerContents = {LayerContents.NUM_FEATURES};
    private int[] layerNumbers = {0};
    private static final Logger logger = Logger.getLogger(NeuralNetModelBuilder.class.getName());
        
    
    public NeuralNetModelBuilder() {
        classifier = new MultilayerPerceptron();
        setHiddenLayers(1, new LayerContents[] {LayerContents.NUM_FEATURES}, null);
        //((MultilayerPerceptron)classifier).setHiddenLayers("i");
    }

    public int getNumHiddenLayers() {
        return numHiddenLayers;
    }

    public LayerContents[] getLayerContents() {
        return layerContents;
    }

    public int[] getLayerNumbers() {
        return layerNumbers;
    }
    
    public final void setHiddenLayers(int numHidden, LayerContents[] layerContents, int[] layerNumbers) {
        if (numHidden == 0) {
            numHiddenLayers = 0;
            this.layerContents = new LayerContents[0];
            this.layerNumbers = new int[0];
            return;
        }
        if (layerContents == null || layerContents.length != numHidden) {
            throw new IllegalArgumentException("Size of layerContents must be equal to numHidden");
        }

        int[] ln = new int[numHiddenLayers];
        //Check legal numbers before committing to any changes:
        for (int i = 0; i < numHiddenLayers; i++) {
            if (this.layerContents[i] == LayerContents.NUMBER) {
                if (layerNumbers == null || i > (layerNumbers.length-1)) {
                    throw new IllegalArgumentException("If layer i is of type NUMBER, then number must be supplied in layerNumbers[i]");
                }
                ln[i] = layerNumbers[i];
            }
        }
        //If we got here, then no errors.
        this.numHiddenLayers = numHidden;
        this.layerContents = new LayerContents[numHidden];
        System.arraycopy(layerContents, 0, this.layerContents, 0, numHidden);

        this.layerNumbers = new int[numHidden];
        System.arraycopy(ln, 0, this.layerNumbers, 0, numHidden);
        
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
        if (layerContents[i] == LayerContents.NUMBER) {
            return Integer.toString(layerNumbers[i]);
        } else { //LayerContents.NUM_FEATURES
            return "i";
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
       MultilayerPerceptron m = (MultilayerPerceptron)WekaModelBuilderHelper.build(classifier, trainingData);
       return new NeuralNetworkModel(name, m);
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCNumericOutput);
    }
    
    public NeuralNetModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof NeuralNetModelBuilder) {
            NeuralNetModelBuilder mb = new NeuralNetModelBuilder();
            NeuralNetModelBuilder old = ((NeuralNetModelBuilder)b);
            mb.setHiddenLayers(old.getNumHiddenLayers(), old.getLayerContents(), old.getLayerNumbers());
            return new NeuralNetModelBuilder();
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
