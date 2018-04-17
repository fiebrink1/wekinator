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
    public String participantID;
    public double[] results;
    public String[] userFeatures;
    public String[] backwardsFeatures;
    public String[] forwardsFeatures;
    public String[] allFeatures;
    public String[] infoGainFeatures;
    public String[] randomFeatures;
    
    public Participant()
    {
        results = new double[6];
        userFeatures = new String[6];
        backwardsFeatures = new String[6];
        forwardsFeatures = new String[6];
        allFeatures = new String[6];
        infoGainFeatures = new String[6];
        randomFeatures = new String[6];
    }
}
