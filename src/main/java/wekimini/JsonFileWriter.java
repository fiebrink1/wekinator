/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONStringer;
import weka.core.Instances;
import wekimini.learning.KNNModelBuilder;
import wekimini.learning.Model;
import wekimini.learning.NeuralNetModelBuilder;
import wekimini.learning.NeuralNetModelBuilder.HiddenLayerType;
import wekimini.learning.NeuralNetworkModel;
import wekimini.osc.OSCOutput;

/**
 *
 * @author mzed
 */
public class JsonFileWriter {

    private final JSONStringer s;

    public JsonFileWriter() {
        this.s = new JSONStringer();
    }

    private static final Logger logger = Logger.getLogger(JsonFileWriter.class.getName());

    public void setup(String[] allInputNames) {
        try {
            setupJSON(allInputNames);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write json head {0}", ex.getMessage());
        }
    }

    public void writePath(
            int whichPath,
            int numExamples,
            List<String> inputNames,
            OSCOutput output,
            LearningModelBuilder modelBuilder,
            Model model,
            Instances insts) throws IOException {
        if (modelBuilder instanceof NeuralNetModelBuilder) {
            try {
                writeNnModel(whichPath, inputNames, modelBuilder, model, numExamples, insts);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not write to NN model to JSON file {0}", ex.getMessage());
            }
        } else if (modelBuilder instanceof KNNModelBuilder) {
            try {
                writeKnnModel(whichPath, numExamples, inputNames, output, modelBuilder, insts);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not write kNN model to JSON file {0}", ex.getMessage());
            }
        } else {
            logger.log(Level.INFO, "Cannot write C++ for this kind of model.");
        }
    }

    public void write(String fileName) throws IOException {
        try {
            closeAndWrite(fileName);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write json foot {0}", ex.getMessage());
        }
    }

    private void writeNnModel(
            int whichPath,
            List<String> inputNames,
            LearningModelBuilder modelBuilder,
            Model model,
            int numExamples,
            Instances insts) throws IOException {
        int numInputs = inputNames.size();
        int numHiddenNodes = 1;
        int numHiddenLayers = 1;

        NeuralNetModelBuilder nmb = (NeuralNetModelBuilder) modelBuilder;
        HiddenLayerType htp = nmb.getHiddenLayerType();
        if (htp == HiddenLayerType.NUM_FEATURES) {
            numHiddenNodes = numInputs;
        } else {
            numHiddenNodes = nmb.getNumNodesPerHiddenLayer();
        }
        numHiddenLayers = nmb.getNumHiddenLayers();
        NeuralNetworkModel nnm = (NeuralNetworkModel) model;
        String modelDescription = nnm.getModelDescription();
        String modelDescriptionLines[] = modelDescription.split("\\r?\\n");

        //Start JSON Object
        s.object();
        s.key("modelType");
        s.value("Neural Network");
        s.key("numInputs");
        s.value(numInputs);
        s.key("inputNames");
        s.value(inputNames);
        s.key("numHiddenLayers");
        s.value(numHiddenLayers);
        s.key("numHiddenNodes");
        s.value(numHiddenNodes);
        s.key("numOutputs");
        s.value(1);
        
        //for normalization
        double[] inMaxes = new double[numInputs];
        double[] inMins = new double[numInputs];
        double outMax = Double.NEGATIVE_INFINITY;
        double outMin = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numInputs; ++i) {
            inMaxes[i] = Double.NEGATIVE_INFINITY;
            inMins[i] = Double.POSITIVE_INFINITY;
        }
        for (int i = 0; i < numExamples; ++i) {
            String[] splitInstance = insts.instance(i).toString().split(",");
            for (int j = 0; j < numInputs; ++j) {
                double inValue = Double.valueOf(splitInstance[j + 3]);
                if (inMaxes[j] < inValue) {
                    inMaxes[j] = inValue;
                }
                if (inMins[j] > inValue) {
                    inMins[j] = inValue;
                }

            }
            double outValue = Double.valueOf(splitInstance[numInputs + 3]);
            if (outMax < outValue) {
                outMax = outValue;
            }
            if (outMin > outValue) {
                outMin = outValue;
            }
        }
        double[] inRanges = new double[numInputs];
        double[] inBases = new double[numInputs];
        for (int i = 0; i < numInputs; ++i) {
            inRanges[i] = (inMaxes[i] - inMins[i]) * 0.5;
            inBases[i] = (inMaxes[i] + inMins[i]) * 0.5;
        }
        s.key("inBases");
        s.value(inBases);
        s.key("inRanges");
        s.value(inRanges);
        s.key("outRange");
        s.value((outMax - outMin) * 0.5);
        s.key("outBase");
        s.value((outMax + outMin) * 0.5);
             
        /////////////////////////////////////////////////
        s.key("nodes");
        s.array();
        // Output Node NB: This assumes one output node
        s.object();
        s.key("name");
        s.value(modelDescriptionLines[0]);
        String threshWeight[] = modelDescriptionLines[2].split("\\s+");
        s.key(threshWeight[1]);
        s.value(threshWeight[2]);
        for (int i = 3; i < numHiddenNodes + 3; ++i) {
            String nodeWeight[] = modelDescriptionLines[i].split("\\s+");
            s.key(nodeWeight[1] + " " + nodeWeight[2]);
            s.value(nodeWeight[3]);
        }
        s.endObject();

        //Nodes between input and first hidden layer
        int hiddenNodeSize = numHiddenNodes + 3;
        int inputNodeSize = numInputs + 3;
        for (int i = 0; i < numHiddenNodes; ++i) {
            int startLine = (i * inputNodeSize) + hiddenNodeSize;
            s.object();
            s.key("name");
            s.value(modelDescriptionLines[startLine]);
            String inputThreshWeight[] = modelDescriptionLines[startLine + 2].split("\\s+");
            s.key(inputThreshWeight[1]);
            s.value(inputThreshWeight[2]);
            for (int j = 3; j < numInputs + 3; ++j) {
                String inputWeight[] = modelDescriptionLines[startLine + j].split("\\s+");
                String inputName = inputWeight[1];
                for (int k = 2; k < (inputWeight.length - 1); ++k) {
                    inputName = inputName + " " + inputWeight[k];
                }
                s.key(inputName);
                s.value(inputWeight[inputWeight.length - 1]); // the last one is always the weight
            }
            s.endObject();
        }

        //Nodes between hidden layers
        if (numHiddenLayers > 1) {
            int hiddenOffset = hiddenNodeSize + (numHiddenNodes * inputNodeSize);
            for (int k = 1; k < numHiddenLayers; ++k) {
                int layerOffset = hiddenOffset + ((k - 1) * (hiddenNodeSize) * numHiddenNodes);
                for (int i = 0; i < numHiddenNodes; ++i) {
                    int nodeOffset = layerOffset + (i * (hiddenNodeSize));
                    s.object();
                    s.key("name");
                    s.value(modelDescriptionLines[nodeOffset]);
                    String hiddenThreshWeight[] = modelDescriptionLines[nodeOffset + 2].split("\\s+");
                    s.key(hiddenThreshWeight[1]);
                    s.value(hiddenThreshWeight[2]);
                    for (int j = 3; j < numHiddenNodes + 3; ++j) {
                        String hiddenWeight[] = modelDescriptionLines[layerOffset + j].split("\\s+");
                        s.key(hiddenWeight[1] + " " + hiddenWeight[2]);
                        s.value(hiddenWeight[3]);
                    }
                    s.endObject();
                }
            }
        }
        s.endArray();
        s.endObject();
    }

    private void writeKnnModel(
            int whichPath,
            int numExamples,
            List<String> inputNames,
            OSCOutput output,
            LearningModelBuilder modelBuilder,
            Instances insts) {
             
        //Get numNeighbours from modelBuilder and numClasses from output
        int numNeighbours = 1;
        int numClasses = 1;
        try {
            KNNModelBuilder kmb = (KNNModelBuilder) modelBuilder;
            numNeighbours = kmb.getNumNeighbors();
            //FIXME: Use above syntax for this method. -MZ
            Method getNumClasses = output.getClass().getMethod("getNumClasses", (Class<?>[]) null);
            numClasses = (int) getNumClasses.invoke(output, (Object[]) null);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not create JSON for knn {0}", ex.getMessage());
        }
        int numInputs = inputNames.size();
        
        //Start JSON Object
        s.object();
        s.key("modelType");
        s.value("kNN classification");
        s.key("numInputs");
        s.value(numInputs);
        s.key("inputNames");
        s.value(inputNames);
        s.key("numClasses");
        s.value(numClasses);
        s.key("k");
        s.value(numNeighbours);
        s.key("numExamples");
        s.value(numExamples);
        //examples
        s.key("examples");
        s.array();
        for (int i = 0; i < numExamples; ++i) {
                String[] splitInstance = insts.instance(i).toString().split(",");
                s.object();
                s.key("class");
                s.value(splitInstance[splitInstance.length - 1]);
                s.key("features");
                s.array();
                for (int j = 3; j < numInputs + 3; ++j) {
                    s.value(splitInstance[j]);
                }
                s.endArray();
                s.endObject();
            }
        s.endArray();
        s.endObject();
    }

    private void setupJSON(String[] allInputNames) {
        s.object();
        
        //Insert metadata into JSON
        s.key("metadata");
        s.object();
        s.key("creator");
        s.value("Wekinator");
        s.key("version");
        s.value("the one you have");
        s.key("inputNames");
        s.value(allInputNames);
        s.endObject();
        
        //Start model set 
        s.key("modelSet");
        s.array();
    }

    private void closeAndWrite(String location) throws IOException {
        s.endArray();
        s.endObject();
        String fileName = location;
        FileWriter fnWrite = new FileWriter(fileName, true);
        try (PrintWriter fPrint = new PrintWriter(fnWrite)) {
            fPrint.printf(s.toString());
        }
    }
}
