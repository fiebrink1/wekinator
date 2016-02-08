/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import com.thoughtworks.xstream.XStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import wekimini.DataManager;
import wekimini.ModelLoader;
import wekimini.Path;
import wekimini.learning.Model;
import wekimini.learning.SupervisedLearningModel;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class LoadableInstanceMaker {

    private static final Logger logger = Logger.getLogger(LoadableInstanceMaker.class.getName());
    private final int numInputs;
    private final int numOutputs;
    private final int numMetaData;
    private transient Instances dummyInstances;
    private transient Reorder outputFilter;
    private final String outputFilterString;

    public LoadableInstanceMaker(int numInputs, int numOutputs, int numMetaData, Instances dummyInstances, String outputFilterString) throws Exception {
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numMetaData = numMetaData;
        this.dummyInstances = dummyInstances;
        //this.outputFilter = outputFilter;
        this.outputFilterString = outputFilterString;
        
        this.outputFilter = new Reorder();
        outputFilter.setAttributeIndices(outputFilterString);
        outputFilter.setInputFormat(dummyInstances);
    }

    public int getNumInputs() {
        return numInputs;
    }

    public int getNumOutputs() {
        return numOutputs;
    }

    public Instance convertInputsToInstance(double[] vals) {
        double data[] = new double[numMetaData + numInputs + numOutputs];
        System.arraycopy(vals, 0, data, numMetaData, vals.length);
        Instance instance = new Instance(1.0, data);
        Instances tmp = new Instances(dummyInstances);
        tmp.add(instance);
        try {
            tmp = Filter.useFilter(tmp, outputFilter);
            tmp.setClassIndex(tmp.numAttributes() - 1);
            instance = tmp.firstInstance();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not filter");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        tmp.setClassIndex(tmp.numAttributes() - 1);
        return instance;
    }
    
    //When you have just 1 input
    public Instance convertInputsToInstance(double val) {
        double data[] = new double[numMetaData + numInputs + numOutputs];
        data[numMetaData] = val;
        Instance instance = new Instance(1.0, data);
        Instances tmp = new Instances(dummyInstances);
        tmp.add(instance);
        try {
            tmp = Filter.useFilter(tmp, outputFilter);
            tmp.setClassIndex(tmp.numAttributes() - 1);
            instance = tmp.firstInstance();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not filter");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        tmp.setClassIndex(tmp.numAttributes() - 1);
        return instance;
    }

    //TODO: NEED TO SAVE DATA TO ARFF, not this xml business :o
    //Or will have to re-create dummy in another way.
    public void writeToFile(String filename) throws IOException {
        //Util.writeToXMLFile(this, "LoadableInstanceMaker", LoadableInstanceMaker.class, filename);
        boolean success = false;
        IOException myEx = new IOException();

        FileOutputStream outstream = null;
        ObjectOutputStream objout = null;
        try {
            outstream = new FileOutputStream(filename);
            objout = new ObjectOutputStream(outstream);

            XStream xstream = new XStream();
            xstream.alias("LoadableInstanceMaker", LoadableInstanceMaker.class);
            String xml = xstream.toXML(this);
            objout.writeObject(xml);

            //Write instances as ARFF, not as XML
            ArffSaver saver = new ArffSaver();
            Instances temp = new Instances(dummyInstances);
            saver.setDestination(objout);
            saver.setInstances(temp);
            saver.writeBatch();
            success = true;
        } catch (IOException ex) {
            success = false;
            myEx = ex;
            logger.log(Level.WARNING, "Could not write to file {0", ex.getMessage());
        } finally {
            try {
                if (objout != null) {
                    objout.close();
                }
                if (outstream != null) {
                    outstream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not close file objects");
            }
        }
        if (!success) {
            throw myEx;
        }
    }

    public static LoadableInstanceMaker readFromFile(String filename) throws IOException, Exception {
        LoadableInstanceMaker m = null;
        /* LoadableInstanceMaker g = (LoadableInstanceMaker) Util.readFromXMLFile("LoadableInstanceMaker", LoadableInstanceMaker.class, filename);
         return g; */
        FileInputStream instream = null;
        ObjectInputStream objin = null;
        Object o = null;
        boolean err = false;
        Exception myEx = new Exception();
        try {
            instream = new FileInputStream(filename);
            objin = new ObjectInputStream(instream);
            // o = objin.readObject();

            String xml = (String) objin.readObject();
            XStream xstream = new XStream();
            xstream.alias("LoadableInstanceMaker", LoadableInstanceMaker.class);
            m = (LoadableInstanceMaker) xstream.fromXML(xml);

            try {
                //String instancesString = (String) objin.readObject();
                //if (!instancesString.equals("null")) {
                    ArffLoader al = new ArffLoader();
                    al.setSource(objin);
                    m.dummyInstances = al.getDataSet();
                //} else {
                //    m.dummyInstances = null;
               // }
            } catch (Exception ex) {
                //Could not load instances: not necessarily a problem
                logger.log(Level.WARNING, "No instances found in path file; not loading them");
                m.dummyInstances = null;
            }
            
            if (m.dummyInstances != null) {
                m.outputFilter = new Reorder();
                m.outputFilter.setAttributeIndices(m.outputFilterString);
                m.outputFilter.setInputFormat(m.dummyInstances);
            }
            
        } catch (Exception ex) {
            myEx = ex;
            err = true;
            logger.log(Level.WARNING, "Error encountered in reading from file: {0}", ex.getMessage());
        } finally {
            try {
                if (objin != null) {
                    objin.close();
                }
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Encountered error closing file objects");
            }

        }
        if (err || m == null || m.dummyInstances == null) {
            throw new Exception("Could not load LoadableInstanceMaker from file");
        }
        return m;
    }
}
