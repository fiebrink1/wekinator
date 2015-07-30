/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import weka.attributeSelection.AttributeTransformer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author rebecca
 */
public class LinearRegressionAttributeTransformer implements AttributeTransformer {
    private Instances exampleInstances = null;
    private final int numInputs;
    private final int exponent;
    
    public LinearRegressionAttributeTransformer(int numInputs, int exponent) {
        this.numInputs = numInputs;
        this.exponent = exponent;
        setExampleInstances();
    }
    
    private void setExampleInstances() {
        FastVector attributes = new FastVector();
        for (int i = numInputs - 1; i >= 0; i--) {
            int num_attrs = numInputs * exponent;
            int[] coeff_inds;

            // build final attName string
            for (int j = 0; j < exponent; j++) {
                StringBuffer attName = new StringBuffer();
                attName.append("Input").append(Integer.toString(i+1)).append("^");
                attName.append(Integer.toString(j+1));
                attributes.addElement(new Attribute(attName.toString()));
            }
        }
        //Add output
        attributes.addElement(new Attribute("Output"));
        
        exampleInstances = new Instances("LinearRegression",attributes, 0);
        exampleInstances.setClassIndex(exampleInstances.numAttributes()-1);
    }
            
    
    @Override
    public Instances transformedHeader() throws Exception {
        return exampleInstances;
    }

    @Override
    public Instances transformedData(Instances data) throws Exception {
        Instances output;
        output = new Instances(exampleInstances);

        for (int i=0;i<data.numInstances();i++) {
            Instance converted = convertInstance(data.instance(i));
            output.add(converted);
        }
        return output;
    }

    @Override
    public Instance convertInstance(Instance instance) throws Exception {
        double[] newVals = new double[numInputs* exponent + 1];
        int next = 0;
        for (int i = 0; i < instance.numAttributes()-1; i++) {
            for (int j = 1; j <= exponent; j++) {
                newVals[next] = Math.pow(instance.value(i), j);
                next++;
            }
        }
        
        //Now add class:
        newVals[newVals.length-1] = instance.classValue();
        
        //Convert:
        return new Instance(instance.weight(), newVals);
    }
}
