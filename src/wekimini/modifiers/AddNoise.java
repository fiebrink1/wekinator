/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;
import java.util.HashMap;
import java.util.Random;
import weka.core.*;
import weka.core.Capabilities.*;
import weka.filters.*;

/**
 *
 * @author louismccallum
 */
public class AddNoise extends SimpleBatchFilter {

    public int index = 0;
    
    @Override
    public String globalInfo() {
        return "A filter to normalise Instances, attribute by attribute";
    }

    @Override
    protected Instances determineOutputFormat(Instances i){
        return i;
    }

    @Override
    protected Instances process(Instances input) {
        
        Instances result = new Instances(determineOutputFormat(input), 0);
        for(int i = 0; i < input.numInstances(); i ++)
        {
            Instance instance = input.instance(i);
            double[] newVals = new double[result.numAttributes()];
            Attribute a = instance.attribute(index);
            double val = instance.value(index);
            Random r = new Random();
            double randomValue = -0.0001 + (0.0001 - -0.0001) * r.nextDouble();     
            newVals[index] = val + randomValue;
            result.add(new Instance(1, newVals));
        }
                
        return result;
    }
}
