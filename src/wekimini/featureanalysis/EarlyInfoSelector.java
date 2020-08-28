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
public class EarlyInfoSelector {
    
    public Classifier classifier;
    private ModelEvaluator e = null;
    private Wekinator w;
    public int interval = 20;
    private int start = 5;
    private int max = -1;
    private int[] bestFeatures;
    private double bestAcc;
    private int setSize = start;
    private int[] ranked;
    private int[] thresholded;
    public int outputIndex = 0;
    int classIndex = 0;
    EarlyInfoResultsReceiver receiver;
    
    private EarlyInfoSelector(){}
    
    public EarlyInfoSelector(Wekinator w)
    {
        this.w = w;
    }
    
    public EarlyInfoSelector(Wekinator w,int start, int interval, int max)
    {
        this.w = w;
        this.interval = interval;
        this.max = max;
        this.start = start;
    }
    
    public void getAttributeIndicesForInstances(Instances instances, EarlyInfoResultsReceiver receiver)
    {
        try {
            System.out.println("starting best info selection");
            this.receiver = receiver;
            
            AttributeSelection attsel = new AttributeSelection();
            InfoGainAttributeEval eval = new InfoGainAttributeEval();
            Ranker search = new Ranker();
            attsel.setEvaluator(eval);
            attsel.setSearch(search);
            classIndex = instances.numAttributes() - 1;
            instances.setClassIndex(classIndex);
            
            System.out.println("starting best info ranking");
            attsel.SelectAttributes(instances);
            System.out.println("finished best info ranking");  
            
            //Return best results from ranked array
            ranked =  attsel.selectedAttributes(); 
            for(int s:ranked)
            {
                if(s == classIndex)
                {
                    System.out.println("removing class index");
                    int[] noClass = new int[ranked.length-1];
                    System.arraycopy(ranked, 0, noClass, 0, noClass.length);
                    ranked = noClass;
                    break;
                }
            }
            if(max > ranked.length || max < 0)
            {
                max = ranked.length - 1;
            }
            setSize = max;
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
        e.evaluateAll(paths, eval, 10, (PropertyChangeEvent evt) -> {
            cvPropertyChanged(evt);
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
        boolean error = results[0].contains("RMS");
        String resultString = (results[0].replaceAll("%", ""));
        resultString = (resultString.replaceAll("RMS", ""));
        resultString = (resultString.replaceAll(" ", ""));
        resultString = (resultString.replaceAll("\\p{P}", ""));
        double res = Double.parseDouble(resultString);
        //Invert if reporting error (not accuracy) as we want a LOW number
        if(error)
        {
            res = 1.0 - res;
        }
        //If first run, start run beginning
        if(setSize == max)
        {
            bestAcc = res;
            bestFeatures = thresholded;
            setSize = start;
            System.out.println("starting new eval with set size " + setSize);
            thresholded = new int[setSize];
            System.arraycopy(ranked, 0, thresholded, 0, setSize);
            evaluate(thresholded);
        }
        else
        {
            //If not build up until we pass ALL, or reach the end
            setSize += interval;
            if(setSize < max && res < bestAcc)
            {
                System.out.println("starting new eval with set size " + setSize);
                thresholded = new int[setSize];
                System.arraycopy(ranked, 0, thresholded, 0, setSize);
                evaluate(thresholded);
            }
            else
            {
                bestFeatures = thresholded;
                System.out.println("Done evaluating, returning best");
                receiver.finished(bestFeatures);
            }
        }
    }
    
    private void cvPropertyChanged(PropertyChangeEvent evt) 
    {
        System.out.println("CV property changed ");
    }
    
    public interface EarlyInfoResultsReceiver {
        public void finished(int[] features);
    }
}
