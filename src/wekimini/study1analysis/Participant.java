/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.study1analysis;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author louismccallum
 */
public class Participant {
    public double timeTakenForwards;
    public double timeTakenBackwards;
    public int numExamples;
    public String participantID;
    public HashMap<String,Double> testSetResults;
    public HashMap<String,Double> trainingSetResults;
    public HashMap<String, String[]> features;
    public int trainingSetSize;
    public int testSetSize;
    
    public Participant()
    {
        features = new HashMap();
        testSetResults = new HashMap();
        trainingSetResults = new HashMap();
    }
}
