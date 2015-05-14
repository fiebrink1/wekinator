/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.Date;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class NeuralNetworKModel implements Model {
    
    private final String prettyName;
    private final String timestamp;
    private final String myId;
    private final MultilayerPerceptron wmodel;
    
    public NeuralNetworKModel(String name, MultilayerPerceptron wmodel) { 
        this.prettyName = name;
        Date d= new Date();
        timestamp = Long.toString(d.getTime());
        this.wmodel = wmodel;
        myId = this.prettyName + "_" + timestamp;
    }
    
    @Override
    public double computeOutput(Instance instance) throws Exception {
        //TODO: Where does instances come from?
        return wmodel.classifyInstance(instance);
    }
    
    @Override
    public String getUniqueIdentifier() {
        return myId;
    }
    
    @Override
    public String getPrettyName() {
        return prettyName;
    }

    @Override
    public boolean isCompatible(OSCOutput o) {
        //Might tweak this for hard/soft limits... Not sure how to handle this ; in path?
        return true;
    }
    
}
