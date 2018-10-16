/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.featureanalysis;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.NumericToNominal;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.gui.ModelEvaluationFrame;
import wekimini.learning.ModelEvaluator;
import wekimini.util.ConfusionParser;

/**
 *
 * @author louismccallum
 */
public class BestInfoSelector {
    
    public Classifier classifier;
    private ModelEvaluator e = null;
    private Wekinator w;
    private int interval = 20;
    private int start = 5;
    private int max = -1;
    private int[] bestFeatures;
    private double bestAcc;
    private int setSize = start;
    private int[] ranked;
    private int[] thresholded;
    public int outputIndex = 0;
    int classIndex = 0;
    BestInfoResultsReceiver receiver;
    
    private BestInfoSelector(){}
    
    public BestInfoSelector(Wekinator w)
    {
        this.w = w;
    }
    
    public BestInfoSelector(Wekinator w,int start, int interval, int max)
    {
        this.w = w;
        this.interval = interval;
        this.max = max;
        this.start = start;
    }
    
    public void getAttributeIndicesForInstances(Instances instances, BestInfoResultsReceiver receiver)
    {
        try {
            System.out.println("starting best info selection");
            this.receiver = receiver;
            Discretize dis = new Discretize();
            dis.setInputFormat(instances);     
            Instances discreted = Filter.useFilter(instances, dis); 
            NumericToNominal nom = new NumericToNominal();
            nom.setInputFormat(discreted);   
            discreted = Filter.useFilter(discreted, nom);
            
            AttributeSelection attsel = new AttributeSelection();
            InfoGainAttributeEval eval = new InfoGainAttributeEval();
            Ranker search = new Ranker();
            attsel.setEvaluator(eval);
            attsel.setSearch(search);
            classIndex = discreted.numAttributes() - 1;
            discreted.setClassIndex(classIndex);
            
            System.out.println("starting best info ranking");
            attsel.SelectAttributes(discreted);
            System.out.println("finished best info ranking");  
            
            //Return best results from ranked array
            ranked =  attsel.selectedAttributes();            
            setSize = start;
            if(max > ranked.length || max < 0)
            {
                max = ranked.length - 1;
            }
            
            bestFeatures = ranked;
            bestAcc = 0;
            thresholded = new int[setSize];
            System.arraycopy(ranked, 0, thresholded, 0, setSize);
            evaluate(thresholded);
   
        } catch (Exception ex) {
            Logger.getLogger(InfoGainSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void evaluate(int[] attributes)
    {
        System.out.println("evaluate with set size " + attributes.length);
        e = new ModelEvaluator(w, new ModelEvaluator.EvaluationResultsReceiver() {

            @Override
            public void finishedModel(int modelNum, String results, String confusion) {
                cvModelFinished(modelNum, results, confusion);
            }

            @Override
            public void finished(String[] results) {
                cvFinished(results);
            }

            @Override
            public void cancelled() {
                cvCancelled();
            }
        });
        ModelEvaluationFrame.EvaluationMode eval = ModelEvaluationFrame.EvaluationMode.CROSS_VALIDATION;
        e.givenIndices = attributes;
        Path p = w.getSupervisedLearningManager().getPaths().get(outputIndex);
        LinkedList<Path> paths = new LinkedList<>();
        paths.add(p);
        e.evaluateAll(paths, eval, 10, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                cvPropertyChanged(evt);
            }

        });
    }
    
    
    private void cvModelFinished(int modelNum, String results, String confusion) 
    {
        System.out.println("Model " + modelNum + ": " + results);
        
    }

    private void cvCancelled() 
    {
        System.out.println("CV Cancelled!!!");
        
    }

    private void cvFinished(String[] results) 
    {
        System.out.println("CV Finished");
        setSize += interval;
        double res = Double.parseDouble((results[0].replaceAll("%", "")));
        if(res > bestAcc)
        {
            bestFeatures = thresholded;
            bestAcc = res;
        }
        
        if(setSize < max)
        {
            System.out.println("starting new eval with set size " + setSize);
            thresholded = new int[setSize];
            System.arraycopy(ranked, 0, thresholded, 0, setSize);
            evaluate(thresholded);
        }
        else
        {
            System.out.println("Done evaluating, returning best");
            for(int s:bestFeatures)
            {
                if(s == classIndex)
                {
                    System.out.println("removing class index");
                    int[] noClass = new int[bestFeatures.length-1];
                    System.arraycopy(bestFeatures, 0, noClass, 0, noClass.length);
                    receiver.finished(noClass);
                    return;
                }
            }
            receiver.finished(bestFeatures);
        }
    }
    
    private void cvPropertyChanged(PropertyChangeEvent evt) 
    {
        System.out.println("CV property changed");
    }
    
    public interface BestInfoResultsReceiver {
        public void finished(int[] features);
    }
}
