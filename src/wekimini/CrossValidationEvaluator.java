/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.jdesktop.swingworker.SwingWorker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import wekimini.learning.ClassificationModelBuilder;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class CrossValidationEvaluator {

    public static boolean hasWarned = false;
    public static boolean isEvaluating = false;
    protected EvaluationStatus evaluationStatus = new EvaluationStatus();
    public static final String PROP_CV_PROGRESS = "cvProgress";
    private transient SwingWorker evalWorker = null;

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected EventListenerList cancelListenerList = new EventListenerList();
    private ChangeEvent cancelEvent = null;
    private boolean wasCancelled = false;
    private boolean hadError = false;
    private static final Logger logger = Logger.getLogger(CrossValidationEvaluator.class.getName());
    private final Wekinator w;
    private String[] results;
    private final CrossValidationResultsReceiver receiver;
    private static final DecimalFormat dFormat = new DecimalFormat(" #.##;-#.##");

    public static final String PROP_RESULTS = "results";

    /**
     * Get the value of results
     *
     * @return the value of results
     */
    public String[] getResults() {
        return results;
    }

    /**
     * Set the value of results
     *
     * @param results new value of results
     */
    public void setResults(String[] results) {
        String[] oldResults = this.results;
        this.results = results;
        propertyChangeSupport.firePropertyChange(PROP_RESULTS, oldResults, results);
    }

    /**
     * Get the value of results at specified index
     *
     * @param index the index of results
     * @return the value of results at specified index
     */
    public String getResults(int index) {
        return this.results[index];
    }

    /**
     * Set the value of results at specified index.
     *
     * @param index the index of results
     * @param results new value of results at specified index
     */
    public void setResults(int index, String results) {
        String oldResults = this.results[index];
        this.results[index] = results;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_RESULTS, index, oldResults, results);
    }

    
    public CrossValidationEvaluator(Wekinator w, CrossValidationResultsReceiver r) {
        this.w = w;
        this.receiver = r;
    }
    
    public boolean wasCancelled() {
        return wasCancelled;
    }
    
    public boolean errorEncountered(){
        return hadError;
    }

    public EvaluationStatus getEvaluationStatus() {
        return evaluationStatus;
    }
    
    public void addCancelledListener(ChangeListener l) {
        cancelListenerList.add(ChangeListener.class, l);
    }

    public void removeCancelledListener(ChangeListener l) {
        cancelListenerList.remove(ChangeListener.class, l);
    }
    
    private void fireCancelled() {
        Object[] listeners = cancelListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (cancelEvent == null) {
                    cancelEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(cancelEvent);
            }
        }
    }
        
            /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void cancel() {
        evalWorker.cancel(true);
        receiver.cancelled();
        //No need to call "fireCancelled": worker itself will do this.
    }

    public void setEvalStatus(EvaluationStatus evalStatus) {
        EvaluationStatus oldEvalStatus = this.evaluationStatus;
        //System.out.println("updating training progress: " + trainingProgress);
        this.evaluationStatus = evaluationStatus;
        propertyChangeSupport.firePropertyChange(PROP_CV_PROGRESS, oldEvalStatus, evaluationStatus);
    }
    
    private void cancelMe(Path p) {
        //logger.log(Level.INFO, "Training was cancelled");
        w.getStatusUpdateCenter().update(this, "Evaluation was cancelled");
        wasCancelled = true;
        fireCancelled();
    }
    
    public void evaluateAll(final List<Path> paths, final boolean isTraining, final int numFolds, PropertyChangeListener listener) {
        final List<Instances> data = new LinkedList<>();
        for (Path p : paths) {
            Instances i = w.getSupervisedLearningManager().getTrainingDataForPath(p, false);
            data.add(i);
        }

        //Todo: init paths and data before this
        setResults(new String[paths.size()]);
        if (evalWorker != null && evalWorker.getState() != SwingWorker.StateValue.DONE) {
            return;
        }
        
        evalWorker = new SwingWorker<Integer, Void>() {

            //trainingWorker.
            @Override
            public Integer doInBackground() {
                // train(); //TODO: Add status updates
                int progress = 0;
                //setProgress(progress);
                int numToEvaluate = 0;
                for (Path p : paths) {
                    if (p.canBuild()) {
                        numToEvaluate++;
                    }
                }

                int numEvaluated = 0;
                int numErr = 0;
                setEvalStatus(new EvaluationStatus(numToEvaluate, numEvaluated, numErr, false));

                for (int i = 0; i < paths.size(); i++) {
                    Path p = paths.get(i);
                    if (p.canBuild()) {
                        try {
                           System.out.println("Evaluating with " + numFolds);
                            //EVALUATE HERE: TODO 
                            Instances instances = w.getSupervisedLearningManager().getTrainingDataForPath(p, false);
                            Evaluation eval = new Evaluation(instances);
                            
                            Classifier c = ((LearningModelBuilder)p.getModelBuilder()).getClassifier();
                            //Classifier c = ((SupervisedLearningModel) p.getModel()).getClassifier();
                            if (! isTraining) {
                                System.out.println("COMPUTING CV");
                                Random r = new Random();
                                eval.crossValidateModel(c, instances, numFolds, r);
                            } else {
                                System.out.println("COMPUTING TRAINING");
                                Classifier c2 = Classifier.makeCopy(c);
                                c2.buildClassifier(instances);
                                eval.evaluateModel(c2, instances);
                            }
                            String result;
                            if (p.getModelBuilder() instanceof ClassificationModelBuilder) {
                                 result = dFormat.format(eval.pctCorrect()) + "%"; //WON"T WORK FOR NN
                            } else {
                                result = dFormat.format(eval.errorRate()) + " (RMS)";
                            }
                            //String s = Double.toString(eval.pctCorrect()); //TODO WON"T WORK FOR NN
                            setResults(i, result);  
                            finishedModel(i, result);
                            numEvaluated++;

                            if (isCancelled()) {
                                cancelMe(p);
                                setResults(i, "Cancelled");
                                return 0;
                            }

                        } catch (InterruptedException ex) {
                            cancelMe(p);
                            setResults(i, "Cancelled");
                            return 0; //Not sure this will be called...
                        } catch (Exception ex) {
                            numErr++;
                            Util.showPrettyErrorPane(null, "Error encountered during evaluation " + p.getCurrentModelName() + ": " + ex.getMessage());
                            logger.log(Level.SEVERE, ex.getMessage());
                                //Logger.getLogger(LearningManager.class.getName()).log(Level.SEVERE, null, ex);
                            //TODO: test this works when error actually occurs in learner
                        }
                        setEvalStatus(new EvaluationStatus(numToEvaluate, numEvaluated, numErr, false));
                    } else {
                        logger.log(Level.WARNING, "Could not evaluate path");
                    }

                }
                wasCancelled = false;
                hadError = evaluationStatus.numErrorsEncountered > 0;
                return 0;
            }

            @Override
            public void done() {
                //setProgress(numParams+1);
                //System.out.println("thread is done");
                if (isCancelled()) {
                    EvaluationStatus t = new EvaluationStatus(evaluationStatus.numToEvaluate, evaluationStatus.numEvaluated, evaluationStatus.numErrorsEncountered, true);
                    // trainingProgress.wasCancelled = true;
                    setEvalStatus(t);
                    //System.out.println("I was cancelled");
                }
                finished();
            }
        };
        evalWorker.addPropertyChangeListener(listener);
        evalWorker.execute();
    }

    
    
    /*public void evaluate(int numFolds, CrossValidationResultsReceiver r) {
        hasWarned = false;
        List<Path> paths = w.getSupervisedLearningManager().getPaths();
        for (Path p : paths) {
            CVWorker worker = new CVWorker(w, p, numFolds);
            worker.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    workerPropertyChanged(evt);
                }

            }
        }

        evaluationWorker = new EvaluationWorker();
        evaluationWorker.addPropertyChangeListener(evalWorkerListener);

        //   setEvaluationState(evaluationState.EVALUTATING);
        learnerToEvaluate = paramNum;
        this.numFolds = numFolds;
        evaluationType = EvaluationType.CV;
        evaluationWorker.execute();
        if (WekinatorRunner.isLogging()) {
            Plog.log(Msg.EVAL_START_CV, paramNum + "," + numFolds);
        }
    } */

 /*   private void workerPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SwingWorker.StateValue)) {
            
        }
    } */

    public static boolean isEvaluating() {
        return isEvaluating;
    }
    
    protected void finishedModel(int modelNum, String result) {
        receiver.finishedModel(modelNum, result);
    }
    
    protected void finished() {
        receiver.finished(results);
    }

    public interface CrossValidationResultsReceiver {
        public void finishedModel(int modelNum, String results);
        public void finished(String[] results);
        public void cancelled();
    }

   /* protected static class CVWorker extends SwingWorker<Double, Void> {

        private final Path p;
        private final Wekinator w;
        private final int numFolds;

        protected CVWorker(Wekinator w, Path p, int numFolds) {
            this.p = p;
            this.w = w;
            this.numFolds = numFolds;
        }

        @Override
        protected Double doInBackground() throws Exception {
            try {
                if (p.getNumExamples() == 0) {
                    return new Double(0); //todo; log somewhere
                }
                boolean hadErr = false;
                Instances instances = w.getSupervisedLearningManager().getTrainingDataForPath(p, false);
                Evaluation eval = new Evaluation(instances);
                Classifier c = ((SupervisedLearningModel) p.getModel()).getClassifier();
                Random r = new Random();
                eval.crossValidateModel(c, instances, numFolds, r);
                return eval.pctCorrect();
            } catch (InterruptedException ex) {
                return 0.0;
            }
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                //new EvalStatus(
                //Do anything?
            }
        }
    }
     */
    
    public static class EvaluationStatus {

        private int numToEvaluate = 0;
        private int numEvaluated = 0;
        private int numErrorsEncountered = 0;
        private boolean wasCancelled = false;

        public EvaluationStatus(int numToEvaluate, int numEvaluated, int numErr, boolean wasCancelled) {
            this.numToEvaluate = numToEvaluate;
            this.numEvaluated = numEvaluated;
            this.numErrorsEncountered = numErr;
            this.wasCancelled = wasCancelled;
        }
        
        public EvaluationStatus() {
        }

        public int getNumToEvaluate() {
            return numToEvaluate;
        }

        public int getNumEvaluated() {
            return numEvaluated;
        }

        public int getNumErrorsEncountered() {
            return numErrorsEncountered;
        }

        public boolean wasCancelled() {
            return wasCancelled;
        }

        @Override
        public String toString() {
            return numEvaluated + "/" + numToEvaluate + " evaluated, " + numErrorsEncountered + " errors, cancelled=" + wasCancelled;
        }
    }

}
