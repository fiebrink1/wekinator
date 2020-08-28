/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.featureanalysis;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Instances;
import weka.filters.Filter;
import wekimini.modifiers.AddNoise;

/**
 *
 * @author louismccallum
 */
public class InfoGainSelector extends RankedFeatureSelector {
    
    @Override
    public int[] getAttributeIndicesForInstances(Instances instances)
    {
        try {
            int classIndex = instances.numAttributes() - 1;

            System.out.println(instances.firstInstance().classAttribute().isNominal());
            System.out.println(instances.firstInstance().classAttribute().isNumeric());
            AttributeSelection attsel = new AttributeSelection();
            InfoGainAttributeEval eval = new InfoGainAttributeEval();
            Ranker search = new Ranker();
            attsel.setEvaluator(eval);
            attsel.setSearch(search);
            instances.setClassIndex(classIndex);
            
            attsel.SelectAttributes(instances);
            
            //Return best results from ranked array
            int[] ranked =  attsel.selectedAttributes();
            System.out.println("returned " + ranked.length + " features");
            if(useThreshold)
            {
                featuresToPick = (int)(((double)ranked.length)*threshold);
            }
            else
            {
                featuresToPick = ranked.length;
            }
            int[] thresholded = new int[featuresToPick];
            System.arraycopy(ranked, 0, thresholded, 0, featuresToPick);
            System.out.println("featuresToPick = " + featuresToPick);
            for(int s:thresholded)
            {
                if(s == classIndex)
                {
                    System.out.println("removing class index");
                    int[] noClass = new int[thresholded.length-1];
                    System.arraycopy(thresholded, 0, noClass, 0, noClass.length);
                    return noClass;
                }
            }
            return thresholded;
            
        } catch (Exception ex) {
            System.out.println("adding noise to avoid collision in discretisation");
            Logger.getLogger(InfoGainSelector.class.getName()).log(Level.SEVERE, null, ex);
            String detail = ex.getMessage();
            String[] split = detail.split(" ");
            int ptr = 0;
            boolean found = false;
            while(ptr < split.length && !found)
            {
                if(split[ptr].contains("feature"))
                {
                    found = true;
                    
                    try {
                        int index = Integer.parseInt(split[ptr].substring(8, split[ptr].length() - 1));
                        AddNoise noise = new AddNoise();
                        noise.setInputFormat(instances);
                        noise.index = index;
                        instances = Filter.useFilter(instances, noise);
                    } catch (Exception ex1) {
                        Logger.getLogger(InfoGainSelector.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                ptr++;
            }
            return getAttributeIndicesForInstances(instances);
        }
    }
    
}
