/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.featureanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
/**
 *
 * @author louismccallum
 */
public class FeatureSelector {
    
    public int[] getAttributeIndicesForInstances(Instances instances){
        return new int[0];
    }
    
    public static Instances downSample(Instances instances, double proportion)
    {
        try
        {
            int numClasses = instances.numClasses() - 1;
            int targetSize = (int) Math.floor((double)instances.numInstances() * proportion);
            ArrayList<Integer> indices = new ArrayList();
            int targetClass = 0;
            Instances downSampled = new Instances(instances,0,0);
            while(indices.size() < targetSize)
            {
                int[] allIndicies = IntStream.range(0, instances.numInstances()).toArray();
                RandomSelector.shuffleArray(allIndicies);
                for(int i = 0; i < allIndicies.length; i++)
                {
                    int index = allIndicies[i];
                    Instance in = instances.instance(index);
                    int classValue = (int)in.classValue();
                    if(classValue == (targetClass + 1) && !indices.contains(index))
                    {
                        indices.add(index);
                        downSampled.add(in);
                        targetClass = (targetClass + 1) % numClasses;
                        break;
                    }
                }
            }
            return downSampled;
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(FeatureSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return instances;
    }
    
    public static Instances filterAttributes(Instances instances, int[] indices)
    {
        try
        {
            int classIndex = instances.classAttribute().index();
            int [] toRemove = {classIndex};
            int[] withClassIndex = new int[indices.length+1];
            System.arraycopy(indices, 0, withClassIndex, 0, indices.length);
            System.arraycopy(toRemove, 0, withClassIndex, indices.length, toRemove.length);
            Remove keep = new Remove();
            keep.setInvertSelection(true);
            keep.setAttributeIndicesArray(withClassIndex);
            keep.setInputFormat(instances);  
            Instances withClass = Filter.useFilter(instances, keep); 
            return withClass;  
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(FeatureSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return instances;
    }
    
}
