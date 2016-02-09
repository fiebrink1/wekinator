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
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class LinearRegressionModel implements SupervisedLearningModel {
    
    private final String prettyName;
    private final String timestamp;
    private final String myId;
    private transient LinearRegression wmodel;
    //private int exponent;
    //private int numInputs;
    private final LinearRegressionAttributeTransformer transformer;
    private static final Logger logger = Logger.getLogger(LinearRegressionModel.class.getName());
    
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
        
        double[] coeffs = wmodel.coefficients();
        StringBuilder sb = new StringBuilder("\n\nNon-truncated coefficients:\n");
        for (int i= 0; i < coeffs.length-2; i++) {
            sb.append(coeffs[i]).append("\n");
        }
        sb.append(coeffs[coeffs.length-1]); //coeffs[length-2] seems to always be an unnecessary 0 - ? skip it.
        
        return wmodel.toString() + sb.toString(); 
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
        //return wmodel.toString();
        double[] coeffs = wmodel.coefficients();
        StringBuilder sb = new StringBuilder("\n\nNon-truncated coefficients:\n");
        for (int i= 0; i < coeffs.length-2; i++) {
            sb.append(coeffs[i]).append("\n");
        }
        sb.append(coeffs[coeffs.length-1]); //coeffs[length-2] seems to always be an unnecessary 0 - ? skip it.
        
        return wmodel.toString() + sb.toString(); 
    }
    
    @Override
    public double[] computeDistribution(Instance instance) {
        logger.log(Level.WARNING, "Cannot compute distribution for linear regression");
        return new double[0];
        
    }
    
    @Override
    public Classifier getClassifier() {
        return wmodel;
    }
    
}
