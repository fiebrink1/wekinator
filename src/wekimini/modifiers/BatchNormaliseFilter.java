/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;
import java.util.HashMap;
import weka.core.*;
import weka.core.Capabilities.*;
import weka.filters.*;

/**
 *
 * @author louismccallum
 */
public class BatchNormaliseFilter extends SimpleBatchFilter {
    
    private HashMap<String, Double> min = new HashMap();
    private HashMap<String, Double> max = new HashMap();

    
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
        resetMinMax(input);
        updateMinMax(input);
        Instances result = new Instances(determineOutputFormat(input), 0);
        for(int i = 0; i < input.numInstances(); i ++)
        {
            Instance instance = input.instance(i);
            double[] newVals = new double[result.numAttributes()];
            for(int j = 0; j < instance.numAttributes(); j++)
            {
                Attribute a = instance.attribute(j);
                double val = instance.value(j);
                double minVal = min.get(a.name());
                double maxVal = max.get(a.name());
                double delta =  maxVal - minVal;
                if(delta == 0)
                {
                    delta = 1.0;
                }
                if(j != result.classIndex())
                {
                    //newVals[j] = ((val - minVal) / delta) * 10000000;
                    newVals[j] = ((val - minVal) / delta);
                }
                else
                {
                    newVals[j] = val;
                }
                
            }
            result.add(new Instance(1, newVals));
        }
                
        return result;
    }
    
    private void updateMinMax(Instances input)
    {
        for(int i = 0; i < input.numInstances(); i ++)
        {
            Instance instance = input.instance(i);
            for(int j = 0; j < instance.numAttributes(); j++)
            {
                Attribute a = instance.attribute(j);
                double val = instance.value(j);
                if(val < min.get(a.name()))
                {
                    min.replace(a.name(), val);
                }
                if(val > max.get(a.name()))
                {
                    max.replace(a.name(), val);
                }
            }     
        }
    }
    
    private void resetMinMax(Instances input)
    {
        min.clear();
        max.clear();
        Instance instance = input.instance(0);
        for(int j = 0; j < instance.numAttributes(); j++)
        {
            Attribute a = instance.attribute(j);
            min.put(a.name(), Double.POSITIVE_INFINITY);
            max.put(a.name(), Double.NEGATIVE_INFINITY);
        }   
    }
    
    public double maxForAttribute(Attribute a)
    {
        return max.get(a.name());
    }
    
    public double minForAttribute(Attribute a)
    {
        return min.get(a.name());
    }
    
    public boolean isValid(Instance in)
    {
        return min.size() == in.numAttributes() && max.size() == in.numAttributes();
    }
}
