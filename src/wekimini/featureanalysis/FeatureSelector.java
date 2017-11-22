/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.featureanalysis;

import java.util.List;
import weka.core.Instances;
/**
 *
 * @author louismccallum
 */
public interface FeatureSelector {
    
    public int[] getFeaturesForInstances(Instances instances); 
    
}
