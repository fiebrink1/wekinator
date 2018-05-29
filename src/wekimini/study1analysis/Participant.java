/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study1analysis;

import java.util.ArrayList;

/**
 *
 * @author louismccallum
 */
public class Participant {
    public double timeTakenForwards;
    public double timeTakenBackwards;
    public int numExamples;
    public String participantID;
    public double[] testSetResults;
    public double[] trainingSetResults;
    public ArrayList<String[]> features;
    public int trainingSetSize;
    public int testSetSize;
    
    public Participant(int numFeatureSets)
    {
        features = new ArrayList();
        testSetResults = new double[numFeatureSets];
        trainingSetResults = new double[numFeatureSets];
    }
}
