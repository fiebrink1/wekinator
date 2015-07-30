/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.jdesktop.swingworker.SwingWorker;
import weka.core.Instances;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class TrainingRunner {

    private Wekinator w;
    private transient SwingWorker trainingWorker = null;
    protected TrainingStatus trainingProgress = new TrainingStatus();
    public static final String PROP_TRAININGPROGRESS = "trainingProgress";
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private int trainingRound = 0;
    protected EventListenerList cancelListenerList = new EventListenerList();
    private ChangeEvent cancelEvent = null;
    private boolean wasCancelled = false;
    private boolean hadError = false;
    private static final Logger logger = Logger.getLogger(TrainingRunner.class.getName());

    public TrainingRunner(Wekinator w) {
        this.w = w;
    }

    public boolean wasCancelled() {
        return wasCancelled;
    }
    
    public boolean errorEncountered(){
        return hadError;
    }

    /**
     * Get the value of trainingProgress
     *
     * @return the value of trainingProgress
     */
    public TrainingStatus getTrainingProgress() {
        return trainingProgress;
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
        trainingWorker.cancel(true);
        //No need to call "fireCancelled": worker itself will do this.
    }

    /**
     * Set the value of trainingProgress
     *
     * @param trainingProgress new value of trainingProgress
     */
    public void setTrainingProgress(TrainingStatus trainingProgress) {
        TrainingStatus oldTrainingProgress = this.trainingProgress;
        //System.out.println("updating training progress: " + trainingProgress);
        this.trainingProgress = trainingProgress;
        propertyChangeSupport.firePropertyChange(PROP_TRAININGPROGRESS, oldTrainingProgress, trainingProgress);
    }

    private void cancelMe(Path p) {
        //logger.log(Level.INFO, "Training was cancelled");
        w.getStatusUpdateCenter().update(this, "Training was cancelled");
        wasCancelled = true;
        fireCancelled();
       // System.out.println("Training was cancelled");
        p.trainingWasInterrupted();
    }

    public void buildAll(final List<Path> paths, final List<Instances> data, PropertyChangeListener listener) {
        if (trainingWorker != null && trainingWorker.getState() != SwingWorker.StateValue.DONE) {
            return;
        }
        trainingRound++;
        trainingWorker = new SwingWorker<Integer, Void>() {

            //trainingWorker.
            @Override
            public Integer doInBackground() {
                // train(); //TODO: Add status updates
                int progress = 0;
                //setProgress(progress);
                int numToTrain = 0;
                for (Path p : paths) {
                    if (p.canBuild()) {
                        numToTrain++;
                    }
                }

                int numTrained = 0;
                int numErr = 0;
                setTrainingProgress(new TrainingStatus(numToTrain, numTrained, numErr, false));

                for (int i = 0; i < paths.size(); i++) {
                    Path p = paths.get(i);
                    if (p.canBuild()) {
                        //TODO: Check if trainable!
                        try {
                            p.buildModel(
                                    p.getOSCOutput().getName() + " (v" + trainingRound + ")",
                                    data.get(i));
                            numTrained++;

                            if (isCancelled()) {
                                cancelMe(p);
                                return 0;
                            }

                        } catch (InterruptedException ex) {
                            cancelMe(p);
                            return 0; //Not sure this will be called...
                        } catch (Exception ex) {
                            numErr++;
                            Util.showPrettyErrorPane(null, "Error encountered during training " + p.getCurrentModelName() + ": " + ex.getMessage());
                            logger.log(Level.SEVERE, ex.getMessage());
                                //Logger.getLogger(LearningManager.class.getName()).log(Level.SEVERE, null, ex);
                            //TODO: test this works when error actually occurs in learner
                        }
                        setTrainingProgress(new TrainingStatus(numToTrain, numTrained, numErr, false));
                    } else if (p.shouldResetOnEmptyData()) {
                        p.resetOnEmptyData();
                        w.getOutputManager().setCurrentValue(i, p.getOSCOutput().getDefaultValue());
                    }

                }

                        // System.out.println("progress is " + progress);
                wasCancelled = false;
                hadError = trainingProgress.numErrorsEncountered > 0;
                return 0;
            }

            @Override
            public void done() {
                //setProgress(numParams+1);
                //System.out.println("thread is done");
                if (isCancelled()) {
                    TrainingStatus t = new TrainingStatus(trainingProgress.numToTrain, trainingProgress.numTrained, trainingProgress.numErrorsEncountered, true);
                    // trainingProgress.wasCancelled = true;
                    setTrainingProgress(t);
                    //System.out.println("I was cancelled");
                }
            }
        };
        trainingWorker.addPropertyChangeListener(listener);
        trainingWorker.execute();
    }

    public static class TrainingStatus {

        private int numToTrain = 0;
        private int numTrained = 0;
        private int numErrorsEncountered = 0;
        private boolean wasCancelled = false;

        public TrainingStatus(int numToTrain, int numTrained, int numErr, boolean wasCancelled) {
            this.numToTrain = numToTrain;
            this.numTrained = numTrained;
            this.numErrorsEncountered = numErr;
            this.wasCancelled = wasCancelled;
        }

        public TrainingStatus() {
        }

        public int getNumToTrain() {
            return numToTrain;
        }

        public int getNumTrained() {
            return numTrained;
        }

        public int getNumErrorsEncountered() {
            return numErrorsEncountered;
        }

        public boolean isWasCancelled() {
            return wasCancelled;
        }

        @Override
        public String toString() {
            return numTrained + "/" + numToTrain + " trained, " + numErrorsEncountered + " errors, cancelled=" + wasCancelled;
        }
    }
}
