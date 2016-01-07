/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 *
 * @author mzed
 */
public class CppWriter {
    private int numNeighbours = 3;
    private int numClasses = 2;
    private int numExamples = 5;
    private int numFeatures = 5;
    
    public int getNumNeighbours() {
        return numNeighbours;
    }
    public void setNumNeighbours(int numNeighbours) {
        this.numNeighbours = numNeighbours;
    }
    
    public int getNumClasses() {
        return numClasses;
    }
    public void setNumClasses(int numClasses) {
        this.numClasses = numClasses;
    }
    
    public int getNumExamples() {
        return numExamples;
    }
    public void setnumExamples(int numExamples) {
        this.numExamples = numExamples;
    }
    
    public int getNumFeatures() {
        return numFeatures;
    }
    public void setnumFeatures(int numFeatures) {
        this.numExamples = numFeatures;
    }
    
    public void writeToFiles(String filename) throws IOException {
        
        //Write header
        String headerName = filename + ".h";
        FileWriter headerWrite = new FileWriter(headerName, true);
        PrintWriter headerPrint = new PrintWriter(headerWrite);
        headerPrint.printf("#ifndef classify_h\n" +
"#define classify_h\n" +
"\n" +
"//from Wekinator\n" +
"#define NUM_NEIGHBOURS " + numNeighbours + "\n" +
"#define NUM_CLASSES " + numClasses + "\n" +
"#define NUM_EXAMPLES " + numExamples + "\n" +
"#define NUM_FEATURES " + numFeatures + "\n" +
"\n" +
"struct neighbour {\n" +
"	int classNum;\n" +
"	float features[NUM_EXAMPLES];\n" +
"};\n" +
"\n" +
"class knnClassification {\n" +
"\n" +
"public:\n" +
"	knnClassification();\n" +
"	int getClass(neighbour inputVector);\n" +
"	\n" +
"private:\n" +
"	neighbour neighbours[NUM_EXAMPLES];\n" +
"	int foundClass;\n" +
"};\n" +
"\n" +
"#endif");
        headerPrint.close();

        //Write cpp
        String cppName = filename + ".cpp";
        FileWriter cppWrite = new FileWriter(cppName, true);
        PrintWriter cppPrint = new PrintWriter(cppWrite);
        cppPrint.printf("//Copyright © Goldsmiths College and Goldsmiths RAPID-MIX investigators\n" +
"//All Rights Reserved\n" +
"//Unauthorised copying of this file, via any medium is strictly prohibited\n" +
"//Proprietary and confidential\n" +
"//Written by Michael Zbyszyński, m.zbyszynski@gold.ac.uk, 4 December 2015\n" +
"\n" +
"#include <math.h>\n" +
"#include <utility>\n" +
"#include \"classify.h\"\n" +
"\n" +
"knnClassification::knnClassification() {\n" +
"	//These values should come directly from wekinator\n" +
"	neighbours[0] = { 1, {0., 0., 0., 0., 0.}};\n" +
"	neighbours[1] = { 1, {0.2, 0.2, 0.2, 0.2, 0.2}};\n" +
"	neighbours[2] = { 2, {0.5, 0.5, 0.5, 0.5, 0.5}};\n" +
"	neighbours[3] = { 2, {0.7, 0.7, 0.7, 0.7, 0.7}};\n" +
"	neighbours[4] = { 2, {1., 1., 1., 1., 1.}};\n" +
"	foundClass = 0;\n" +
"}\n" +
"\n" +
"int knnClassification::getClass(neighbour inputVector) {\n" +
"	std::pair<int, double> nearestNeighbours[NUM_NEIGHBOURS];\n" +
"	std::pair<int, double> farthestNN = {0, 0.};\n" +
"	//Find k nearest neighbours\n" +
"	for (int i = 0; i < NUM_EXAMPLES; i++) {\n" +
"		//find Euclidian distance for this neighbor\n" +
"		double euclidianDistance = 0;\n" +
"		for(int j = 0; j < NUM_EXAMPLES ; j++){\n" +
"			euclidianDistance = euclidianDistance + pow((inputVector.features[j] - neighbours[i].features[j]),2);\n" +
"		}\n" +
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
