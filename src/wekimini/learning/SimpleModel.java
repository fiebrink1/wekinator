/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instance;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public abstract class SimpleModel implements SupervisedLearningModel {
    
    private final String prettyName;
    private final String timestamp;
    private final String myId;
    private static final Logger logger = Logger.getLogger(SimpleModel.class.getName());
    
    public SimpleModel(String name) { 
        this.prettyName = name;
        Date d= new Date();
        timestamp = Long.toString(d.getTime());
        myId = this.prettyName + "_" + timestamp;
    }
    
    @Override
    public double computeOutput(Instance instance) {
        return 1.0f;
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
        return true;
    }

    @Override
    public void writeToOutputStream(ObjectOutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static Model readFromInputStream(ObjectInputStream is) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } 

     @Override
    public String getModelDescription() {
        return "A simple model that does nothing";
    }
    
    @Override
    public double[] computeDistribution(Instance instance) {
        return new double[0];
    }
}
