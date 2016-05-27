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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class CppWriter {

    private static final Logger logger = Logger.getLogger(CppWriter.class.getName());

    public void writeToFiles(
            int whichPath,
            int numExamples,
            List<String> inputNames,
            String[] allInputNames,
            OSCOutput output,
            LearningModelBuilder modelBuilder,
            Instances insts,
            Model model,
            String location
    ) throws IOException {
        try {
            writeBaseModel(location);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write baseModel {0}", ex.getMessage());
        }
        try {
            writeModelSet(location);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write modelSet {0}", ex.getMessage());
        }
        if (modelBuilder instanceof NeuralNetModelBuilder) {
            try {
                writeNNModel(whichPath, numExamples, inputNames, allInputNames, modelBuilder, model, insts, location);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not write to NN model to Cpp file {0}", ex.getMessage());
            }
        } else if (modelBuilder instanceof KNNModelBuilder) {
            try {
                writeKNNModel(whichPath, numExamples, inputNames, allInputNames, output, modelBuilder, insts, location);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not write kNN model to Cpp file {0}", ex.getMessage());
            }
        } else {
            logger.log(Level.INFO, "Cannot write C++ for this kind of model.");
        }
    }
    
    private void writeBaseModel(String location) throws IOException {
        //write header
        String headerName = location + "baseModel.h";
        File h = new File(headerName);
        if (!h.exists()) {
            FileWriter headerWrite = new FileWriter(headerName, true);
            try (PrintWriter headerPrint = new PrintWriter(headerWrite)) {
                headerPrint.printf("#ifndef baseModel_h\n");
                headerPrint.printf("#define baseModel_h\n\n");
                headerPrint.printf("class baseModel {\n");
                headerPrint.printf("public:\n");
                headerPrint.printf("    virtual double processInput(double*) {};\n");
                headerPrint.printf("    virtual ~baseModel() {};\n");
                headerPrint.printf("};\n\n");
                headerPrint.printf("#endif");
            }
        }
    }
    
    private void writeModelSet(String location) throws IOException {
        //write header
        String headerName = location + "modelSet.h";
        File h = new File(headerName);
        if (!h.exists()) {
            FileWriter headerWrite = new FileWriter(headerName, true);
            try (PrintWriter headerPrint = new PrintWriter(headerWrite)) {
                headerPrint.printf("#ifndef modelSet_h\n");
                headerPrint.printf("#define modelSet_h\n\n");
                headerPrint.printf("#include <vector>\n");
                headerPrint.printf("#include \"baseModel.h\"\n\n");
                headerPrint.printf("class modelSet {\n");
                headerPrint.printf("public:\n");
                headerPrint.printf("    modelSet();\n");
                headerPrint.printf("    void addModel(baseModel*);\n");
                headerPrint.printf("    double* passInputToModels(double*);\n\n");
                headerPrint.printf("private:\n");
                headerPrint.printf("    std::vector<baseModel*> myModelSet;\n");
                headerPrint.printf("};\n\n");
                headerPrint.printf("#endif");
            }
        }
        //write cpp
        String cppName = location + "modelSet.cpp";
        File c = new File(cppName);
        if (!c.exists()) {
            FileWriter cppWrite = new FileWriter(cppName, true);
            try (PrintWriter cppPrint = new PrintWriter(cppWrite)) {
                cppPrint.printf("#include <vector>\n");
                cppPrint.printf("#include \"modelSet.h\"\n\n");
                cppPrint.printf("modelSet::modelSet() {\n");
                cppPrint.printf("    std::vector<baseModel*> myModelSet;\n");
                cppPrint.printf("};\n\n");
                cppPrint.printf("void modelSet::addModel(baseModel* modelAddress) {\n");
                cppPrint.printf("    myModelSet.push_back(modelAddress);\n");
                cppPrint.printf("}\n\n");
                cppPrint.printf("double* modelSet::passInputToModels(double* input) {\n");
                cppPrint.printf("    int setSize = myModelSet.size();\n");
                cppPrint.printf("    double* output = new double[setSize];\n");
                cppPrint.printf("    for (int i = 0; i < setSize; i++) {\n");
                cppPrint.printf("        output[i] = myModelSet[i]->processInput(input);\n");
                cppPrint.printf("    }\n");
                cppPrint.printf("    return output;\n");
                cppPrint.printf("}");
            }
        }
    }
        
    private void writeKNNModel(int whichPath,
            int numExamples, 
            List<String> inputNames,
            String[] allInputNames,
            OSCOutput output, 
            LearningModelBuilder modelBuilder, 
            Instances insts, 
            String location) 
            throws IOException {
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
            logger.log(Level.WARNING, "Could not write to Cpp file {0}", ex.getMessage());
        }
        int numInputs = inputNames.size();
        
        //Write header
        String headerName = location + "knnClassification.h";
        File h = new File(headerName);
        if (!h.exists()) {
            FileWriter headerWrite = new FileWriter(headerName, true);
            try (PrintWriter headerPrint = new PrintWriter(headerWrite)) {
                headerPrint.printf("#ifndef knnClassification_h\n");
                headerPrint.printf("#define knnClassification_h\n\n");
                headerPrint.printf("#define NUM_NEIGHBOURS " + numNeighbours + "\n");
                headerPrint.printf("#define NUM_CLASSES " + numClasses + "\n");
                headerPrint.printf("#define NUM_EXAMPLES " + numExamples + "\n");
                headerPrint.printf("#define NUM_INPUTS " + numInputs + "\n\n");
                headerPrint.printf("#include \"baseModel.h\"\n\n");
                headerPrint.printf("struct neighbour {\n");
                headerPrint.printf("	int classNum;\n");
                headerPrint.printf("	double features[NUM_INPUTS];\n");
                headerPrint.printf("};\n\n");
                headerPrint.printf("class knnClassification : public baseModel {\n\n");
                headerPrint.printf("public:\n");
                headerPrint.printf("	knnClassification(int*, neighbour*);\n");
                headerPrint.printf("	~knnClassification();\n\n");
                headerPrint.printf("	double processInput(double*);\n\n");
                headerPrint.printf("private:\n");
                headerPrint.printf("	int numInputs;\n");
                headerPrint.printf("	int* whichInputs;\n");
                headerPrint.printf("	neighbour* neighbours;\n");
                headerPrint.printf("};\n\n");
                headerPrint.printf("#endif\n\n");
            }
        }
        FileWriter headerWrite = new FileWriter(headerName, true);
        try (PrintWriter headerPrint = new PrintWriter(headerWrite)) {
            headerPrint.printf("knnClassification setup_model" + whichPath + "();\n");
        }

        //Write cpp
        String cppName = location + "knnClassification.cpp";
        File c = new File(cppName);
        if (!c.exists()) {
            FileWriter cppWrite = new FileWriter(cppName, true);
            try (PrintWriter cppPrint = new PrintWriter(cppWrite)) {
                cppPrint.printf("#include <math.h>\n");
                cppPrint.printf("#include <utility>\n");
                cppPrint.printf("#include \"knnClassification.h\"\n\n");
                cppPrint.printf("knnClassification::knnClassification(int num_inputs, int* which_inputs, neighbour* _neighbours) {\n");
                cppPrint.printf("	numInputs = num_inputs;\n");
                cppPrint.printf("	whichInputs = which_inputs;\n");
                cppPrint.printf("	neighbours = _neighbours;\n}\n\n");
                cppPrint.printf("knnClassification::~knnClassification() {\n");
                cppPrint.printf("	delete neighbours;\n}\n\n");
                cppPrint.printf("int knnClassification::processInput(double* inputVector) {\n");
                cppPrint.printf("	std::pair<int, double> nearestNeighbours[NUM_NEIGHBOURS];\n");
                cppPrint.printf("	std::pair<int, double> farthestNN = {0, 0.};\n\n");
                cppPrint.printf("	//Find k nearest neighbours\n");
                cppPrint.printf("	for (int i = 0; i < NUM_EXAMPLES; i++) {\n");
                cppPrint.printf("		//find Euclidian distance for this neighbor\n");
                cppPrint.printf("		double euclidianDistance = 0;\n");
                cppPrint.printf("		for(int j = 0; j < NUM_INPUTS ; j++){\n");
                cppPrint.printf("			euclidianDistance = euclidianDistance + pow((inputVector[j] - neighbours[i].features[j]),2);\n");
                cppPrint.printf("		}\n");
                cppPrint.printf("		euclidianDistance = sqrt(euclidianDistance);\n");
                cppPrint.printf("		if (i < NUM_NEIGHBOURS) {\n");
                cppPrint.printf("			//save the first k neighbours\n");
                cppPrint.printf("			nearestNeighbours[i] = {i, euclidianDistance};\n");
                cppPrint.printf("			if (euclidianDistance > farthestNN.second) {\n");
                cppPrint.printf("				farthestNN = {i, euclidianDistance};\n}\n");
                cppPrint.printf("		} else if (euclidianDistance < farthestNN.second) {\n");
                cppPrint.printf("			//replace farthest, if new neighbour is closer\n");
                cppPrint.printf("			nearestNeighbours[farthestNN.first] = {i, euclidianDistance};\n");
                cppPrint.printf("			farthestNN = {i, euclidianDistance};\n}\n}\n");
                cppPrint.printf("	//majority vote on nearest neighbours\n");
                cppPrint.printf("	int numVotesPerClass[NUM_CLASSES] = {};\n");
                cppPrint.printf("	for (int i = 0; i < NUM_NEIGHBOURS; i++){\n");
                cppPrint.printf("		numVotesPerClass[neighbours[nearestNeighbours[i].first].classNum - 1]++;\n");
                cppPrint.printf("	}\n");
                cppPrint.printf("	int foundClass = 0;\n");
                cppPrint.printf("	int mostVotes = 0;\n");
                cppPrint.printf("	for (int i = 0; i < NUM_CLASSES; i++) {\n");
                cppPrint.printf("		if (numVotesPerClass[i] > mostVotes) { //TODO: Handle ties the same way Wekinator does\n");
                cppPrint.printf("			mostVotes = numVotesPerClass[i];\n");
                cppPrint.printf("			foundClass = i + 1;\n");
                cppPrint.printf("		}\n");
                cppPrint.printf("	}\n");
                cppPrint.printf("	return foundClass;\n");
                cppPrint.printf("}\n\n");
                cppPrint.printf("// Setup models ------------------------------------------------------------------------------\n\n");
            }
        }
        FileWriter cppWrite = new FileWriter(cppName, true);
        try (PrintWriter cppPrint = new PrintWriter(cppWrite)) {
            cppPrint.printf("knnClassification setup_model" + whichPath + "() {\n\n");
            cppPrint.printf("    int whichInputs[" + numInputs +"] = {");
            boolean needComma = false;
            for (int i = 0; i < allInputNames.length; i++) {
                if (inputNames.contains(allInputNames[i])){
                    if (needComma) {
                        cppPrint.printf(", ");
                    } else {
                        needComma = true;
                    }
                    cppPrint.printf(String.valueOf(i));    
                }
            }
            cppPrint.printf("	neighbour *neighbours = new(neighbour[" + numExamples + "]);\n");
            for (int i = 0; i < numExamples; i++) {
                String[] splitInstance = insts.instance(i).toString().split(",");
                cppPrint.printf("	neighbours[" + i + "] = {");
                cppPrint.printf(splitInstance[splitInstance.length - 1] + ", {");
                for (int j = 3; j < numInputs + 3; j++) {
                    if (j > 3) {
                        cppPrint.printf(", ");
                    }
                    cppPrint.printf(splitInstance[j]);
                }
                cppPrint.printf("}};\n");
            }
            
            cppPrint.printf("\n	knnClassification knn" + whichPath + "(" + numInputs+ ", whichInputs, neighbours);\n");
            cppPrint.printf("	return knn" + whichPath + ";\n");
            cppPrint.printf("}\n\n");
        }
    }

    private void writeNNModel(
            int whichPath, 
            int numExamples, 
            List<String> inputNames, 
            String[] allInputNames,
            LearningModelBuilder modelBuilder, 
            Model model, 
            Instances insts, 
            String location
    ) throws IOException {
        int numInputs = inputNames.size();
        int numHiddenNodes = 1;
        int numHiddenLayers = 1;
        String modelDescription = "";

        try {
            NeuralNetModelBuilder nmb = (NeuralNetModelBuilder) modelBuilder;
            HiddenLayerType htp = nmb.getHiddenLayerType();
            if (htp == HiddenLayerType.NUM_FEATURES) {
                numHiddenNodes = numInputs;
            } else {
                numHiddenNodes = nmb.getNumNodesPerHiddenLayer();
            }
            numHiddenLayers = nmb.getNumHiddenLayers();
            
            NeuralNetworkModel nnm = (NeuralNetworkModel) model;
            modelDescription = nnm.getModelDescription();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write to Cpp file {0}", ex.getMessage());
        }
           
        if (numHiddenLayers > 1) {
            logger.log(Level.WARNING, "Cannot write Cpp model with {0} layers.", numHiddenLayers);
        }
        
        //Write header
        String headerName = location + "neuralNetwork.h";
        File h = new File(headerName);
        if (!h.exists()) {
            FileWriter headerWrite = new FileWriter(headerName, true);
            try (PrintWriter headerPrint = new PrintWriter(headerWrite)) {
                headerPrint.printf("#ifndef neuralNetwork_h\n");
                headerPrint.printf("#define neuralNetwork_h\n\n");
                headerPrint.printf("#include \"baseModel.h\"\n\n");  
                headerPrint.printf("class neuralNetwork : public baseModel {\n\n");
                headerPrint.printf("public:\n\n");
                headerPrint.printf("	neuralNetwork(int, int*, int, double***, double*, double*, double*, double, double);\n");
                headerPrint.printf("	~neuralNetwork();\n\n");
                headerPrint.printf("	double processInput(double*);\n\n");
                headerPrint.printf("private:\n\n");
                headerPrint.printf("	int numInputs;\n");
                headerPrint.printf("    int* whichInputs;\n\n");
                headerPrint.printf("	int numHiddenNodes;\n\n");
                headerPrint.printf("	double* inputNeurons;\n");
                headerPrint.printf("	double* hiddenNeurons;\n");
                headerPrint.printf("	double*** weights;\n");
                headerPrint.printf("	double* wHiddenOutput;\n\n");
                headerPrint.printf("    double* inRanges;\n");
                headerPrint.printf("    double* inBases;\n");
                headerPrint.printf("    double outRange;\n");
                headerPrint.printf("    double outBase;\n\n");
                headerPrint.printf("	double output;\n\n");
                headerPrint.printf("	inline double activationFunction(double);\n");
                headerPrint.printf("};\n\n");
                headerPrint.printf("#endif\n\n");
            }
        }
        FileWriter headerWrite = new FileWriter(headerName, true);
        try (PrintWriter headerPrint = new PrintWriter(headerWrite)) {
            headerPrint.printf("neuralNetwork setup_model" + whichPath + "();\n");
        }

        //Write cpp
        String cppName = location + "neuralNetwork.cpp";
        File c = new File(cppName);
        if (!c.exists()) {
            FileWriter cppWrite = new FileWriter(cppName, true);
            try (PrintWriter cppPrint = new PrintWriter(cppWrite)) {
                cppPrint.printf("#include <math.h>\n");
                cppPrint.printf("#include <algorithm>\n");
                cppPrint.printf("#include \"neuralNetwork.h\"\n\n");
                cppPrint.printf("neuralNetwork::neuralNetwork(int num_inputs, \n");
                cppPrint.printf("                             int* which_inputs,\n");
                cppPrint.printf("                             int num_hidden_nodes,\n");
                cppPrint.printf("                             double*** _weights,\n");
                cppPrint.printf("                             double* w_hidden_output,\n");
                cppPrint.printf("                             double* in_max,\n");
                cppPrint.printf("                             double* in_min,\n");
                cppPrint.printf("                             double out_max,\n");
                cppPrint.printf("                             double out_min) {\n");
                cppPrint.printf("	numInputs = num_inputs;\n");
                cppPrint.printf("	whichInputs = which_inputs;\n");
                cppPrint.printf("	numHiddenNodes = num_hidden_nodes;\n");
                cppPrint.printf("	//input neurons, including bias\n");
                cppPrint.printf("	inputNeurons = new double[numInputs + 1];\n");
                cppPrint.printf("	for (int i=0; i < numInputs; i++){\n");
                cppPrint.printf("		inputNeurons[i] = 0;\n");
                cppPrint.printf("	}\n");
                cppPrint.printf("	inputNeurons[numInputs] = 1;\n\n");
                cppPrint.printf("	//hidden neurons, including bias\n");
                cppPrint.printf("	hiddenNeurons = new double[numHiddenNodes + 1];\n");
                cppPrint.printf("	for (int i=0; i < numHiddenNodes; i++){\n");
                cppPrint.printf("		hiddenNeurons[i] = 0;\n");
                cppPrint.printf("	}\n");
                cppPrint.printf("	hiddenNeurons[numHiddenNodes] = 1;\n\n");
                cppPrint.printf("	output = 0;\n\n");
                cppPrint.printf("	weights = _weights;\n");
                cppPrint.printf("	wHiddenOutput = w_hidden_output;\n\n");
                cppPrint.printf("	inRanges = new double[numInputs];\n");
                cppPrint.printf("	inBases = new double[numInputs];\n\n");
                cppPrint.printf("	for (int i = 0; i < numInputs; i++) {\n");
                cppPrint.printf("           inRanges[i] = (in_max[i] - in_min[i])/ 2;\n");
                cppPrint.printf("           inBases[i] = (in_max[i] + in_min[i])/ 2;\n");
                cppPrint.printf("	}\n\n");
                cppPrint.printf("       outRange = (out_max - out_min)/ 2;\n");
                cppPrint.printf("       outBase = (out_max + out_min)/ 2;");
                cppPrint.printf("}\n\n");

                cppPrint.printf("neuralNetwork::~neuralNetwork() {\n");
                cppPrint.printf("	delete[] inputNeurons;\n");
                cppPrint.printf("	delete[] hiddenNeurons;\n\n");
                cppPrint.printf("	int maxNodes = std::max(numInputs, numHiddenNodes);\n");
                cppPrint.printf("	for (int i=0; i <= numInputs; i++) {\n");
                cppPrint.printf("		for (int j=0; j <=maxNodes; j++) {\n");
                cppPrint.printf("                   delete[] weights[i][j];\n");
                cppPrint.printf("		}\n");
                cppPrint.printf("		delete[] weights[i];\n");
                cppPrint.printf("	}\n");
                cppPrint.printf("	delete[] weights;\n\n");
                cppPrint.printf("	delete[] wHiddenOutput;\n");
                cppPrint.printf("}\n\n");

                cppPrint.printf("inline double neuralNetwork::activationFunction(double x) {\n");
                cppPrint.printf("	//sigmoid\n");
                cppPrint.printf("       if (x < -45) { //from weka, to combat overflow\n");
                cppPrint.printf("           x = 0;\n");
                cppPrint.printf("       } else if (x > 45) {\n");
                cppPrint.printf("           x = 1;\n");
                cppPrint.printf("       } else {\n");
                cppPrint.printf("           x = 1/(1 + exp(-x));\n");
                cppPrint.printf("       }\n");
                cppPrint.printf("       return x;\n");
                cppPrint.printf("}\n\n");

                cppPrint.printf("double neuralNetwork::processInput(double* inputVector) {\n");
                cppPrint.printf("	double pattern[numInputs];\n");
                cppPrint.printf("	for (int h = 0; h < numInputs; h++) {\n");
                cppPrint.printf("		pattern[h] = inputVector[whichInputs[h]];\n");
                cppPrint.printf("	}\n\n");
                cppPrint.printf("	//set input layer\n");
                cppPrint.printf("	for (int i = 0; i < numInputs; i++) {\n");
                cppPrint.printf("		inputNeurons[i] = (pattern[i] - inBases[i]) / inRanges[i];\n");
                cppPrint.printf("	}\n\n");
                cppPrint.printf("	//calculate hidden layer\n");
                cppPrint.printf("	for (int j=0; j < numHiddenNodes; j++) {\n");
                cppPrint.printf("		hiddenNeurons[j] = 0;\n");
                cppPrint.printf("		for (int i = 0; i <= numInputs; i++) {\n");
                cppPrint.printf("			hiddenNeurons[j] += inputNeurons[i] * weights[0][i][j];\n");
                cppPrint.printf("		}\n");
                cppPrint.printf("		hiddenNeurons[j] = activationFunction(hiddenNeurons[j]);\n");
                cppPrint.printf("	}\n");
                cppPrint.printf("	//calculate output\n");
                cppPrint.printf("	output = 0;\n");
                cppPrint.printf("	for (int k=0; k <= numHiddenNodes; k++){\n");
                cppPrint.printf("           output += hiddenNeurons[k] * wHiddenOutput[k];\n");
                cppPrint.printf("	}\n");
                cppPrint.printf("       output = (output * outRange) + outBase;");
                cppPrint.printf("	return output;\n");
                cppPrint.printf("}\n\n");
                cppPrint.printf("// Setup models ------------------------------------------------------------------------------\n\n");
            }
        }
        FileWriter cppWrite = new FileWriter(cppName, true);
        try (PrintWriter cppPrint = new PrintWriter(cppWrite)) {
            cppPrint.printf("neuralNetwork setup_model" + whichPath + "() {\n");
            int inputsPlusOne = numInputs + 1;
            int hiddenPlusOne = numHiddenNodes + 1;
            cppPrint.printf("    int whichInputs[" + numInputs +"] = {");
            boolean needComma = false;
            for (int i = 0; i < allInputNames.length; i++) {
                if (inputNames.contains(allInputNames[i])){
                    if (needComma) {
                        cppPrint.printf(", ");
                    } else {
                        needComma = true;
                    }
                    cppPrint.printf(String.valueOf(i));    
                }
            }
            cppPrint.printf("};\n");
            int totalLayers = numHiddenLayers + 1;
            int maxNodes = Math.max(numInputs, numHiddenNodes) + 1;
            cppPrint.printf("    int totalLayers = " + totalLayers + ";\n");
            cppPrint.printf("    int maxNodes = " + maxNodes + ";\n");
            cppPrint.printf("    double ***weights = new double **[totalLayers];\n");
            cppPrint.printf("    for (int i = 0; i < totalLayers; ++i) {;\n");
            cppPrint.printf("       weights[i] = new double*[maxNodes];\n");
            cppPrint.printf("       for (int j = 0; j < maxNodes; ++j) {\n");
            cppPrint.printf("           weights[i][j] = new double[maxNodes];\n");
            cppPrint.printf("       }\n");
            cppPrint.printf("    }\n");
            
            cppPrint.printf("    double *wHiddenOutput = new(double[" + hiddenPlusOne + "]);\n\n");        
            cppPrint.printf("	//weights between input and hidden\n");

            String modelDescriptionLines[] = modelDescription.split("\\r?\\n");
            for (int i = 0; i < numInputs; i++) {
                for (int j = 0; j < numHiddenNodes; j++) {
                    int offset = (6 + numHiddenNodes + i) + (j * (3 + numInputs)); //TODO: Make this look better. MZ
                    String nodeWeight[] = modelDescriptionLines[offset].split("\\s+");
                    cppPrint.printf("	weights[0][" + i + "][" + j + "] = " + nodeWeight[3] + ";\n");
                }
            }
            for (int j = 0; j < numHiddenNodes; j++) {
                int offset = (5 + numHiddenNodes) + (j * (3 + numInputs)); //TODO: Make this look better. MZ
                String biasWeight[] = modelDescriptionLines[offset].split("\\s+");
                cppPrint.printf("	weights[0][" + numInputs + "][" + j + "] = " + biasWeight[2] + ";\n");
            }
            cppPrint.printf("\n");
            if (numHiddenLayers > 1) {
                cppPrint.printf("	//weights between hidden layers\n");
                for (int k = 1; k < numHiddenLayers; k++) {
                    for (int i = 0; i < numHiddenNodes; i++) {
                        for (int j = 0; j < numHiddenNodes; j++) {
                            int offset = (6 + numHiddenNodes + i) + ((j + (numHiddenNodes * k)) * (3 + numHiddenNodes));
                            String nodeWeight[] = modelDescriptionLines[offset].split("\\s+");
                            cppPrint.printf("	weights[" + k + "][" + i + "][" + j + "] = " + nodeWeight[3] + ";\n");
                        }
                    }
                    for (int j = 0; j < numHiddenNodes; j++) {
                        int offset = (5 + numHiddenNodes) + ((j + (numHiddenNodes * k)) * (3 + numInputs));
                        String biasWeight[] = modelDescriptionLines[offset].split("\\s+");
                        cppPrint.printf("	weights[" + k + "][" + numInputs + "][" + j + "] = " + biasWeight[2] + ";\n");
                    }
                }
            }

            cppPrint.printf("\n");
            cppPrint.printf("	//weights between hidden and output\n");
            cppPrint.printf("	wHiddenOutput = new double[" + hiddenPlusOne + "];\n");
            cppPrint.printf("	for (int i = 0; i <= " + numHiddenNodes + "; i++) {\n");
            cppPrint.printf("		 wHiddenOutput[i] = 0;\n");
            cppPrint.printf("	}\n");
            for (int i = 0; i < numHiddenNodes; i++) {
                String nodeWeight[] = modelDescriptionLines[i + 3].split("\\s+");
                cppPrint.printf("	wHiddenOutput[" + i + "] = " + nodeWeight[3] + ";\n");
            }
            String biasWeight[] = modelDescriptionLines[2].split("\\s+");
            cppPrint.printf("	wHiddenOutput[" + numHiddenNodes + "] = " + biasWeight[2] + ";\n\n");
            cppPrint.printf("   //For normalization\n");
            double[] inMaxes = new double[numInputs];
            double[] inMins = new double[numInputs];
            double outMax = Double.NEGATIVE_INFINITY;
            double outMin = Double.POSITIVE_INFINITY;
            for (int i = 0; i < numInputs; i++) {
                inMaxes[i] = Double.NEGATIVE_INFINITY;
                inMins[i] = Double.POSITIVE_INFINITY;
            }
            for (int i = 0; i < numExamples; i++) {
                String[] splitInstance = insts.instance(i).toString().split(",");
                for (int j = 0; j < numInputs; j++) {
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
            cppPrint.printf("   double inMaxes[" + numInputs + "] = { ");
            for (int i = 0; i < numInputs; i++) {
                if (i > 0) {
                    cppPrint.printf(", ");
                }
                cppPrint.printf(Double.toString(inMaxes[i]));      
            }
            cppPrint.printf(" };\n");
            cppPrint.printf("   double inMins[" + numInputs + "] = { ");
            for (int i = 0; i < numInputs; i++) {
                if (i > 0) {
                    cppPrint.printf(", ");
                }
                cppPrint.printf(Double.toString(inMins[i]));      
            }
            cppPrint.printf(" };\n\n");
            cppPrint.printf("   double outMax = " + outMax + ";\n");
            cppPrint.printf("   double outMin = " + outMin + ";\n\n");
            cppPrint.printf("   neuralNetwork neuralNetwork" + whichPath);
            cppPrint.printf(" (" + numInputs + ", whichInputs, " + numHiddenNodes);
            cppPrint.printf(", weights, wHiddenOutput, inMaxes, inMins, outMax, outMin);\n\n");
            cppPrint.printf("   return neuralNetwork" + whichPath +";\n\n}");
            cppPrint.printf("/* Full model description\n");
            cppPrint.printf(modelDescription + "\n");
            cppPrint.printf("*/\n");
        }
    }
}

