/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.featureanalysis;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    
    public Instances filterInstances(Instances instances, int[] indices)
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
