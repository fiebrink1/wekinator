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
import wekimini.osc.OSCOutput;

/**
 *
 * @author mzed
 */
public class CppWriter {
    private static final Logger logger = Logger.getLogger(CppWriter.class.getName());
    private int numNeighbours;
    private int numClasses;
    
    public void writeToFiles(String filename, int numExamples, int numFeatures, OSCOutput output, LearningModelBuilder modelBuilder, Instances insts) throws IOException {
        //Get numNeighbours from modelBuilder and numClasses from output
        try {
            Method getNumNeighbors = modelBuilder.getClass().getMethod("getNumNeighbors", null);
            numNeighbours = (int)getNumNeighbors.invoke(modelBuilder, null);
            Method getNumClasses = output.getClass().getMethod("getNumClasses",null);
            numClasses = (int)getNumClasses.invoke(output, null);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not write to Cpp file ", ex.getMessage());
        }
        //Write header
        String headerName = filename + ".h";
        FileWriter headerWrite = new FileWriter(headerName, true);
        PrintWriter headerPrint = new PrintWriter(headerWrite);
        headerPrint.printf("#ifndef classify_h\n");
        headerPrint.printf("#define classify_h\n\n");
        headerPrint.printf("#define NUM_NEIGHBOURS " + numNeighbours + "\n" );
        headerPrint.printf("#define NUM_CLASSES " + numClasses + "\n");
        headerPrint.printf("#define NUM_EXAMPLES " + numExamples + "\n");
        headerPrint.printf("#define NUM_FEATURES " + numFeatures + "\n\n");
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
        for (int i = 0; i < numExamples; i++){
            String[] splitInstance = insts.instance(i).toString().split(",");
            cppPrint.printf("	neighbours[" + i + "] = {");
            cppPrint.printf(splitInstance[splitInstance.length - 1] + ", {");
            for (int j = 3; j < numFeatures + 3; j++){
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
        cppPrint.printf("		}\n" +
"		euclidianDistance = sqrt(euclidianDistance);\n" +
"		if (i < NUM_NEIGHBOURS) {\n" +
"			//save the first k neighbours\n" +
"			nearestNeighbours[i] = {i, euclidianDistance};\n" +
"			if (euclidianDistance > farthestNN.second) {\n" +
"				farthestNN = {i, euclidianDistance};\n" +
"			}\n" +
"		} else if (euclidianDistance < farthestNN.second) {\n" +
"			//replace farthest, if new neighbour is closer\n" +
"			nearestNeighbours[farthestNN.first] = {i, euclidianDistance};\n" +
"			farthestNN = {i, euclidianDistance};\n" +
"		}\n" +
"	}\n" +
"	//majority vote on nearest neighbours\n" +
"	int numVotesPerClass[NUM_CLASSES] = {};\n" +
"	for (int i = 0; i < NUM_NEIGHBOURS; i++){\n" +
"		numVotesPerClass[neighbours[nearestNeighbours[i].first].classNum - 1]++;\n" +
"	}\n" +
"	foundClass = 0;\n" +
"	int mostVotes = 0;\n" +
"	for (int i = 0; i < NUM_CLASSES; i++) {\n" +
"		if (numVotesPerClass[i] > mostVotes) { //TODO: Handle ties the same way Wekinator does\n" +
"			mostVotes = numVotesPerClass[i];\n" +
"			foundClass = i + 1;\n" +
"		}\n" +
"	}\n" +
"	return foundClass;\n" +
"}\n" +
"");
        cppPrint.close();
    }
}

/* neuralNetwork.h

#ifndef neuralNetwork_h
#define neuralNetwork_h

//from Wekinator
#define NUM_INPUTS 2
#define NUM_HIDDEN 2
#define NUM_OUTPUT 2

class neuralNetwork {

public:
	
	neuralNetwork();
	~neuralNetwork();
	
	double* feedForwardPattern( double* pattern );
	
private:
	
	double* inputNeurons;
	double* hiddenNeurons;
	double* outputNeurons;
	
	double** wInputHidden;
	double** wHiddenOutput;
	
	inline double activationFunction(double x);
	void feedForward (double* pattern);
};

#endif

*/

/* neuralNetwork.cpp

#include <math.h>
#include "neuralNetwork.h"

neuralNetwork::neuralNetwork() {
	
	//input neurons, including bias
	inputNeurons = new(double[NUM_INPUTS + 1]);
	for (int i=0; i < NUM_INPUTS; i++){
		inputNeurons[i] = 0;
	}
	inputNeurons[NUM_INPUTS] = -1;
	
	//hidden neurons, including bias
	hiddenNeurons = new(double[NUM_HIDDEN + 1]);
	for (int i=0; i < NUM_HIDDEN; i++){
		hiddenNeurons[i] = 0;
	}
	hiddenNeurons[NUM_HIDDEN] = -1;
	
	//outputNeurons
	outputNeurons = new( double[NUM_OUTPUT + 1] );
	for (int i=0; i < NUM_OUTPUT; i++){
		hiddenNeurons[i] = 0;
	}
	
	//weights between input and hidden
	wInputHidden = new(double*[NUM_INPUTS + 1]);
	for (int i = 0; i <= NUM_INPUTS; i++) {
		wInputHidden[i] = new (double[NUM_HIDDEN]);
		for (int j=0; j < NUM_HIDDEN; j++) {
			wInputHidden[i][j] = 0;
		}
	}
	wInputHidden[0][0] = 0.9;//FIXME: Arbitrary weights for testing. Populate from Wekinator
	wInputHidden[0][1] = 0.1;
	wInputHidden[1][0] = 0.1;
	wInputHidden[1][1] = 0.9;
	
	//weights between hidden and output
	wHiddenOutput = new(double*[NUM_HIDDEN + 1]);
	for (int i = 0; i <= NUM_HIDDEN; i++) {
		wHiddenOutput[i] = new (double[NUM_OUTPUT]);
		for (int j=0; j < NUM_OUTPUT; j++) {
			wHiddenOutput[i][j] = 0;
		}
	}
	wHiddenOutput[0][0] = 0.9;//FIXME: Arbitrary weights for testing. Populate from Wekinator
	wHiddenOutput[0][1] = 0.1;
	wHiddenOutput[1][0] = 0.1;
	wHiddenOutput[1][1] = 0.9;		
}

neuralNetwork::~neuralNetwork() {
	delete[] inputNeurons;
	delete[] hiddenNeurons;
	delete[] outputNeurons;
	
	for (int i=0; i <= NUM_INPUTS; i++) {
		delete[] wInputHidden[i];
	}
	delete[] wInputHidden;
	
	for (int j=0; j <= NUM_HIDDEN; j++) {
		delete[] wHiddenOutput[j];
	}
	delete[] wHiddenOutput;
}

inline double neuralNetwork::activationFunction(double x) {
	//sigmoid
	return 1/(1 + exp(-x));
}

double* neuralNetwork::feedForwardPattern(double* pattern) {
	feedForward(pattern);
	return outputNeurons;
}

void neuralNetwork::feedForward(double* pattern) {
	//set input layer
	for (int i = 0; i < NUM_INPUTS; i++) {
		inputNeurons[i] = pattern[i];
	}
	
	//calculate hidden layer
	for (int j=0; j < NUM_HIDDEN; j++) {
		hiddenNeurons[j] = 0;
		for (int i = 0; i < NUM_INPUTS; i++) {
			hiddenNeurons[j] += inputNeurons[i] * wInputHidden[i][j];
		}
		hiddenNeurons[j] = activationFunction(hiddenNeurons[j]);
	}
	
	//calculate output layer
	for (int k=0; k < NUM_OUTPUT; k++){
		outputNeurons[k] = 0;
		for (int j = 0; j < NUM_HIDDEN; j++) {
			outputNeurons[k] += hiddenNeurons[j] * wHiddenOutput[j][k];
		}
		
	}
}

*/
