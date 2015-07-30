/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import com.thoughtworks.xstream.XStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class LinearRegressionModel implements Model {
    
    private final String prettyName;
    private final String timestamp;
    private final String myId;
    private transient LinearRegression wmodel;
    //private int exponent;
    //private int numInputs;
    private final LinearRegressionAttributeTransformer transformer;
    
    public LinearRegressionModel(String name, LinearRegression wmodel, LinearRegressionAttributeTransformer transformer) { 
        this.prettyName = name;
        Date d= new Date();
        timestamp = Long.toString(d.getTime());
        this.wmodel = wmodel;
        myId = this.prettyName + "_" + timestamp;
        this.transformer = transformer;
    }
    
    @Override
    public double computeOutput(Instance instance) throws Exception {
        Instance i;
        if (transformer != null ) {
            i = transformer.convertInstance(instance);
        } else {
            i = instance;
        }
        
        return wmodel.classifyInstance(i);
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
    
    public String getModelString() {
        return wmodel.toString();
    }
    
    public void writeToOutputStream(ObjectOutputStream os) throws IOException {
        XStream xstream = new XStream();
        xstream.alias("LinearRegressionModel", LinearRegressionModel.class);
        String xml = xstream.toXML(this);
        os.writeObject(xml);
        os.writeObject(wmodel);
//Util.writeToXMLFile(this, "Path", Path.class, filename);
    }
    
    public static LinearRegressionModel readFromInputStream(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String xml = (String)is.readObject();
        XStream xstream = new XStream();
        xstream.alias("LinearRegressionModel", LinearRegressionModel.class);
        LinearRegressionModel lrm = (LinearRegressionModel) xstream.fromXML(xml);
        LinearRegression lr = (LinearRegression)is.readObject();
        lrm.wmodel = lr;
        return lrm;
    }
    
    @Override
    public String getModelDescription() {
        return wmodel.toString();
    }
    
}
