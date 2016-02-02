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
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class AdaboostModel implements SupervisedLearningModel {
    
    private final String prettyName;
    private final String timestamp;
    private final String myId;
    private transient AdaBoostM1 wmodel;
    private static final Logger logger = Logger.getLogger(AdaboostModel.class.getName());
    public AdaboostModel(String name, AdaBoostM1 wmodel) { 
        this.prettyName = name;
        Date d = new Date();
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
        return (o instanceof OSCClassificationOutput);
    }
    
    public void writeToOutputStream(ObjectOutputStream os) throws IOException {
        XStream xstream = new XStream();
        xstream.alias("AdaboostModel", AdaboostModel.class);
        String xml = xstream.toXML(this);
        os.writeObject(xml);
        os.writeObject(wmodel); //Uses default java serialization
//Util.writeToXMLFile(this, "Path", Path.class, filename);
    }
    
    public static AdaboostModel readFromInputStream(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String xml = (String)is.readObject();
        XStream xstream = new XStream();
        xstream.alias("AdaboostModel", AdaboostModel.class);
        AdaboostModel model = (AdaboostModel) xstream.fromXML(xml);
        AdaBoostM1 ada = (AdaBoostM1)is.readObject();
        model.wmodel = ada;
        return model;
    }

    @Override
    public String getModelDescription() {
        return wmodel.toString();
    }

    @Override
    public double[] computeDistribution(Instance instance) {
        try {
            return wmodel.distributionForInstance(instance);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not compute distribution");
            return new double[0];
        }
    }

    @Override
    public Classifier getClassifier() {
        return wmodel;
    }
}
