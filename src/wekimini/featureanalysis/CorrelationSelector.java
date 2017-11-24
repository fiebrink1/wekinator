///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package wekimini.featureanalysis;
//
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import weka.attributeSelection.AttributeSelection;
//import weka.attributeSelection.CorrelationAttributeEval;
//import weka.attributeSelection.Ranker;
//import weka.core.Instances;
//import weka.filters.Filter;
//import weka.filters.unsupervised.attribute.Remove;
//
///**
// *
// * @author louismccallum
// */
//public class CorrelationSelector extends FeatureSelector {
//    
//    @Override
//    public int[] getAttributeIndicesForInstances(Instances instances)
//    {
//        try {
//
//            int classIndex = instances.classAttribute().index();
//            int [] toRemove = {classIndex};
//            
//            Remove removeClass = new Remove();   
//            removeClass.setAttributeIndicesArray(toRemove);
//            removeClass.setInputFormat(instances);                          
//            Instances noClass = Filter.useFilter(instances, removeClass);  
//
//            AttributeSelection attsel = new AttributeSelection();
//            CorrelationAttributeEval eval = new CorrelationAttributeEval();
//            Ranker search = new Ranker();
//            search.setOptions(new String[]{"-T","0.05"});
//            attsel.setEvaluator(eval);
//            attsel.setSearch(search);
//            instances.setClassIndex(instances.numAttributes() - 1);
//            
//            System.out.println("starting selection");
//            attsel.SelectAttributes(instances);
//            System.out.println("DONE");  
//            
//            return attsel.selectedAttributes();
//            
//        } catch (Exception ex) {
//            Logger.getLogger(CFSelector.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return new int[0];
//    }
//}
