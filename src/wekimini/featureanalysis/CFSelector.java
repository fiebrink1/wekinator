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
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;

/**
 *
 * @author louismccallum
 */
public class CFSelector implements FeatureSelector {
    
    @Override
    public int[] getFeaturesForInstances(Instances instances)
    {
        try {
            AttributeSelection attsel = new AttributeSelection();
            CfsSubsetEval eval = new CfsSubsetEval();
            GreedyStepwise search = new GreedyStepwise();
            search.setSearchBackwards(true);
            attsel.setEvaluator(eval);
            attsel.setSearch(search);
            attsel.SelectAttributes(instances);
            int[] indices = attsel.selectedAttributes();
            return indices;
        } catch (Exception ex) {
            Logger.getLogger(CFSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new int[0];
    }
    
}
