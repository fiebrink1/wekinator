/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import wekimini.learning.KNNModelBuilder;
import wekimini.learning.Model;
import wekimini.learning.NeuralNetModelBuilder;
import wekimini.osc.OSCOutput;

/**
 *
 * @author mzed
 */
public class CppWriter {

    private static final Logger logger = Logger.getLogger(CppWriter.class.getName());

    public void writeToFiles(String filename, int numExamples, int numInputs, OSCOutput output, LearningModelBuilder modelBuilder, Instances insts, Model model) throws IOException {
        if (modelBuilder instanceof NeuralNetModelBuilder) {
            try {
                writeNNModel(filename, numExamples, numInputs, modelBuilder, model);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not write to NN model to Cpp file ", ex.getMessage());
            }
        } else if (modelBuilder instanceof KNNModelBuilder) {
            try {
                writeKNNModel(filename, numExamples, numInputs, output, modelBuilder, insts);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not write kNN model to Cpp file ", ex.getMessage());
            }
        } else {
            logger.log(Level.INFO, "Cannot write C++ for this kind of model.");
        }
    }

    private void writeKNNModel(String filename, int numExamples, int numInputs, OSCOutput output, LearningModelBuilder modelBuilder, Instances insts) throws IOException {
        //Get numNeighbours from modelBuilder and numClasses from output
        int numNeighbours = 1;
        int numClasses = 1;
        try {
            Method getNumNeighbors = modelBuilder.getClass().getMethod("getNumNeighbors", null);
            numNeighbours = (int) getNumNeighbors.invoke(modelBuilder, null);
            Method getNumClasses = output.getClass().getMethod("getNumClasses", null);
            numClasses = (int) getNumClasses.invoke(output, null);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write to Cpp file ", ex.getMessage());
        }
        //Write header
        String headerName = filename + ".h";
        FileWriter headerWrite = new FileWriter(headerName, true);
        PrintWriter headerPrint = new PrintWriter(headerWrite);
        headerPrint.printf("#ifndef classify_h\n");
        headerPrint.printf("#define classify_h\n\n");
        headerPrint.printf("#define NUM_NEIGHBOURS " + numNeighbours + "\n");
        headerPrint.printf("#define NUM_CLASSES " + numClasses + "\n");
        headerPrint.printf("#define NUM_EXAMPLES " + numExamples + "\n");
        headerPrint.printf("#define NUM_FEATURES " + numInputs + "\n\n");
        headerPrint.printf("struct neighbour {\n");
        headerPrint.printf("	int classNum;\n");
        headerPrint.printf("	float features[NUM_FEATURES];\n");
        headerPrint.printf("};\n\n");
        headerPrint.printf("class knnClassification {\n\n");
        headerPrint.printf("public:\n");
        headerPrint.printf("	knnClassification();\n");
        headerPrint.printf("	int getClass(neighbour inputVector);\n\n");
        headerPrint.printf("private:\n");
        headerPrint.printf("	neighbour neighbours[NUM_EXAMPLES];\n");
        headerPrint.printf("	int foundClass;\n");
        headerPrint.printf("};\n\n");
        headerPrint.printf("#endif");
        headerPrint.close();

        //Write cpp
        String cppName = filename + ".cpp";
        FileWriter cppWrite = new FileWriter(cppName, true);
        PrintWriter cppPrint = new PrintWriter(cppWrite);
        cppPrint.printf("#include <math.h>\n");
        cppPrint.printf("#include <utility>\n");
        cppPrint.printf("#include \"classify.h\"\n\n");
        cppPrint.printf("knnClassification::knnClassification() {\n");
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
        cppPrint.printf("	foundClass = 0;\n}\n\n");
        cppPrint.printf("int knnClassification::getClass(neighbour inputVector) {\n");
        cppPrint.printf("	std::pair<int, double> nearestNeighbours[NUM_NEIGHBOURS];\n");
        cppPrint.printf("	std::pair<int, double> farthestNN = {0, 0.};\n");
        cppPrint.printf("	//Find k nearest neighbours\n");
        cppPrint.printf("	for (int i = 0; i < NUM_EXAMPLES; i++) {\n");
        cppPrint.printf("		//find Euclidian distance for this neighbor\n");
        cppPrint.printf("		double euclidianDistance = 0;\n");
        cppPrint.printf("		for(int j = 0; j < NUM_EXAMPLES ; j++){\n");
        cppPrint.printf("			euclidianDistance = euclidianDistance + pow((inputVector.features[j] - neighbours[i].features[j]),2);\n");
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
        cppPrint.printf("	int numVotesPerClass[NUM_CLASSES] = {};\n"
                + "	for (int i = 0; i < NUM_NEIGHBOURS; i++){\n"
                + "		numVotesPerClass[neighbours[nearestNeighbours[i].first].classNum - 1]++;\n"
                + "	}\n"
                + "	foundClass = 0;\n"
                + "	int mostVotes = 0;\n"
                + "	for (int i = 0; i < NUM_CLASSES; i++) {\n"
                + "		if (numVotesPerClass[i] > mostVotes) { //TODO: Handle ties the same way Wekinator does\n"
                + "			mostVotes = numVotesPerClass[i];\n"
                + "			foundClass = i + 1;\n"
                + "		}\n"
                + "	}\n"
                + "	return foundClass;\n"
                + "}\n");
        cppPrint.close();
    }

    private void writeNNModel(String filename, int numExamples, int numInputs, LearningModelBuilder modelBuilder, Model model) throws IOException {
        int numHiddenNodes = 1;
        int numHiddenLayers = 1;
        String modelDescription = "";
        
        try {
            Method getNumHiddenLayers = modelBuilder.getClass().getMethod("getNumHiddenLayers", null);
            numHiddenLayers = (int) getNumHiddenLayers.invoke(modelBuilder, null);
            
            Method getNumNodesPerHiddenLayer = modelBuilder.getClass().getMethod("getNumNodesPerHiddenLayer", null);
            numHiddenNodes = (int) getNumNodesPerHiddenLayer.invoke(modelBuilder, null);
            
            Method getModelDescription = model.getClass().getMethod("getModelDescription", null);
            modelDescription = (String) getModelDescription.invoke(model, null);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write to Cpp file ", ex.getMessage());
        }
        
        //Write header
        String headerName = filename + ".h";
        FileWriter headerWrite = new FileWriter(headerName, true);
        PrintWriter headerPrint = new PrintWriter(headerWrite);
        headerPrint.printf("#ifndef neuralNetwork_h\n");
        headerPrint.printf("#define neuralNetwork_h\n\n");
        headerPrint.printf("#define NUM_INPUTS " + numInputs + "\n");
        headerPrint.printf("#define NUM_HIDDEN " + numHiddenNodes + "\n");
        headerPrint.printf("#define NUM_OUTPUT 1\n\n");
        headerPrint.printf("class neuralNetwork {\n\n");
        headerPrint.printf("public:\n\n");
        headerPrint.printf("	neuralNetwork();\n");
        headerPrint.printf("	~neuralNetwork();\n\n");
        headerPrint.printf("	double* feedForwardPattern( double* pattern );\n\n");
        headerPrint.printf("private:\n\n");
        headerPrint.printf("	double* inputNeurons;\n");
        headerPrint.printf("	double* hiddenNeurons;\n");
        headerPrint.printf("	double* outputNeurons;\n\n");
        headerPrint.printf("	double** wInputHidden;\n");
        headerPrint.printf("	double** wHiddenOutput;\n\n");
        headerPrint.printf("	inline double activationFunction(double x);\n");
        headerPrint.printf("	void feedForward (double* pattern);\n");
        headerPrint.printf("};\n\n");
        headerPrint.printf("#endif\n");
        headerPrint.close();

        //Write cpp
        String cppName = filename + ".cpp";
        FileWriter cppWrite = new FileWriter(cppName, true);
        PrintWriter cppPrint = new PrintWriter(cppWrite);
        cppPrint.printf("model description " + modelDescription + "\n"); 
        cppPrint.printf("#include <math.h>\n");
        cppPrint.printf("#include \"neuralNetwork.h\"\n\n");
        cppPrint.printf("neuralNetwork::neuralNetwork() {\n\n");
        cppPrint.printf("	//input neurons, including bias\n");
        cppPrint.printf("	inputNeurons = new(double[NUM_INPUTS + 1]);\n");
        cppPrint.printf("	for (int i=0; i < NUM_INPUTS; i++){\n");
        cppPrint.printf("		inputNeurons[i] = 0;\n");
        cppPrint.printf("	}\n");
        cppPrint.printf("	inputNeurons[NUM_INPUTS] = -1;\n\n");
        cppPrint.printf("	//hidden neurons, including bias\n"
                + "	hiddenNeurons = new(double[NUM_HIDDEN + 1]);\n"
                + "	for (int i=0; i < NUM_HIDDEN; i++){\n"
                + "		hiddenNeurons[i] = 0;\n"
                + "	}\n"
                + "	hiddenNeurons[NUM_HIDDEN] = -1;\n"
                + "	\n"
                + "	//outputNeurons\n"
                + "	outputNeurons = new( double[NUM_OUTPUT + 1] );\n"
                + "	for (int i=0; i < NUM_OUTPUT; i++){\n"
                + "		hiddenNeurons[i] = 0;\n"
                + "	}\n"
                + "	\n"
                + "	//weights between input and hidden\n"
                + "	wInputHidden = new(double*[NUM_INPUTS + 1]);\n"
                + "	for (int i = 0; i <= NUM_INPUTS; i++) {\n"
                + "		wInputHidden[i] = new (double[NUM_HIDDEN]);\n"
                + "		for (int j=0; j < NUM_HIDDEN; j++) {\n"
                + "			wInputHidden[i][j] = 0;\n"
                + "		}\n"
                + "	}\n");
        cppPrint.printf("//MZ: I would rather not have to parse this string to get the weights\n");
        cppPrint.printf(modelDescription + "\n"); 
        cppPrint.printf("	//weights between hidden and output\n"
                + "	wHiddenOutput = new(double*[NUM_HIDDEN + 1]);\n"
                + "	for (int i = 0; i <= NUM_HIDDEN; i++) {\n"
                + "		wHiddenOutput[i] = new (double[NUM_OUTPUT]);\n"
                + "		for (int j=0; j < NUM_OUTPUT; j++) {\n"
                + "			wHiddenOutput[i][j] = 0;\n"
                + "		}\n"
                + "	}\n"
                + "	wHiddenOutput[0][0] = 0.9;//FIXME: Arbitrary weights for testing. Populate from Wekinator\n"
                + "	wHiddenOutput[0][1] = 0.1;\n"
                + "	wHiddenOutput[1][0] = 0.1;\n"
                + "	wHiddenOutput[1][1] = 0.9;		\n"
                + "}\n"
                + "\n"
                + "neuralNetwork::~neuralNetwork() {\n"
                + "	delete[] inputNeurons;\n"
                + "	delete[] hiddenNeurons;\n"
                + "	delete[] outputNeurons;\n"
                + "	\n"
                + "	for (int i=0; i <= NUM_INPUTS; i++) {\n"
                + "		delete[] wInputHidden[i];\n"
                + "	}\n"
                + "	delete[] wInputHidden;\n"
                + "	\n"
                + "	for (int j=0; j <= NUM_HIDDEN; j++) {\n"
                + "		delete[] wHiddenOutput[j];\n"
                + "	}\n"
                + "	delete[] wHiddenOutput;\n"
                + "}\n"
                + "\n"
                + "inline double neuralNetwork::activationFunction(double x) {\n"
                + "	//sigmoid\n"
                + "	return 1/(1 + exp(-x));\n"
                + "}\n"
                + "\n"
                + "double* neuralNetwork::feedForwardPattern(double* pattern) {\n"
                + "	feedForward(pattern);\n"
                + "	return outputNeurons;\n"
                + "}\n"
                + "\n"
                + "void neuralNetwork::feedForward(double* pattern) {\n"
                + "	//set input layer\n"
                + "	for (int i = 0; i < NUM_INPUTS; i++) {\n"
                + "		inputNeurons[i] = pattern[i];\n"
                + "	}\n"
                + "	\n"
                + "	//calculate hidden layer\n"
                + "	for (int j=0; j < NUM_HIDDEN; j++) {\n"
                + "		hiddenNeurons[j] = 0;\n"
                + "		for (int i = 0; i <= NUM_INPUTS; i++) {\n"
                + "			hiddenNeurons[j] += inputNeurons[i] * wInputHidden[i][j];\n"
                + "		}\n"
                + "		hiddenNeurons[j] = activationFunction(hiddenNeurons[j]);\n"
                + "	}\n\n"
                + "	//calculate output layer\n"
                + "	for (int k=0; k < NUM_OUTPUT; k++){\n"
                + "		outputNeurons[k] = 0;\n"
                + "		for (int j = 0; j <= NUM_HIDDEN; j++) {\n"
                + "			outputNeurons[k] += hiddenNeurons[j] * wHiddenOutput[j][k];\n"
                + "		}\n\n"
                + "	}\n"
                + "}\n");
        cppPrint.close();
    }
}
