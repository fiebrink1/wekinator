/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study1analysis;

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
    public String[] userFeatures;
    public String[] backwardsFeatures;
    public String[] forwardsFeatures;
    public String[] allFeatures;
    public String[] infoGainFeatures;
    public String[] randomFeatures;
    public String[] rawFeatures;
    public String[] meanFeatures;
    
    public Participant(int numFeatureSets)
    {
        testSetResults = new double[numFeatureSets];
        trainingSetResults = new double[numFeatureSets];
    }
}
