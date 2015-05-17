/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingworker.SwingWorker;
import weka.core.Instances;
import weka.core.Instance;
import wekimini.learning.ModelBuilder;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCReceiver;
import wekimini.util.WeakListenerSupport;

/**
 *
 * @author rebecca
 */
public class LearningManager {


    public static enum LearningState {DONE_TRAINING, TRAINING, READY_TO_TRAIN, NOT_READY_TO_TRAIN};
    public static enum RunningState {RUNNING, NOT_RUNNING};
    public static enum RecordingState {RECORDING, NOT_RECORDING};
    
    //private RunningState runningState = RunningState.NOT_RUNNING;
    private final List<Path> paths = new ArrayList<>(5);
    private final Wekinator w;
    private final WeakListenerSupport wls = new WeakListenerSupport();
    private boolean[] pathRecordingMask;
    private boolean[] pathRunningMask;
    private double[] myComputedOutputs;
    //private int trainingRound = 0;
    private int recordingRound = 0;
    private final HashMap<String, Integer> inputNamesToIndices;
    private final HashMap<Path, Integer> pathsToOutputIndices;
    //private boolean wasCancelled = false;
    //private final HashMap<String, Integer> outputNamesToIndices;
    
    public static final String PROP_LEARNINGSTATE = "learningState";
    private LearningState learningState = LearningState.NOT_READY_TO_TRAIN;

    private RunningState runningState = RunningState.NOT_RUNNING;
    public static final String PROP_RUNNINGSTATE = "runningState";
    
    private RecordingState recordingState = RecordingState.NOT_RECORDING;
    public static final String PROP_RECORDINGSTATE = "recordingState";
   
    private int numExamplesThisRound;

    public static final String PROP_NUMEXAMPLESTHISROUND = "numExamplesThisRound";
    private static final Logger logger = Logger.getLogger(LearningManager.class.getName());

    
    protected PropertyChangeListener trainingWorkerListener = this::trainingWorkerChanged;
    
    private boolean ableToRecord = false;

    public static final String PROP_ABLE_TO_RECORD = "ableToRecord";

        private boolean ableToRun = false;

    public static final String PROP_ABLE_TO_RUN = "ableToRun";


    
    private void trainingWorkerChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                //SwingWorker.StateValue.
                //System.out.println("!!!TODOTODOTODOshould be setting training to false here");
                setLearningState(LearningState.DONE_TRAINING);
            }
        } // else if = progress: TODO do anything with tthis?
    }
    
    

    /**
     * Get the value of recordingState
     *
     * @return the value of recordingState
     */
    public RecordingState getRecordingState() {
        return recordingState;
    }
    
    public int getRecordingRound() {
        return recordingRound;
    }

    /**
     * Set the value of recordingState
     *
     * @param recordingState new value of recordingState
     */
    public void setRecordingState(RecordingState recordingState) {
        RecordingState oldRecordingState = this.recordingState;
        this.recordingState = recordingState;
        propertyChangeSupport.firePropertyChange(PROP_RECORDINGSTATE, oldRecordingState, recordingState);
    }

    public void startRecording() {
        numExamplesThisRound = 0;
        recordingRound++;
        setRecordingState(RecordingState.RECORDING);
    }
    
    public void stopRecording() {
        setRecordingState(RecordingState.NOT_RECORDING);
    }
    
    /**
     * Get the value of runningState
     *
     * @return the value of runningState
     */
    public RunningState getRunningState() {
        return runningState;
    }

    /**
     * Set the value of runningState
     *
     * @param runningState new value of runningState
     */
    public void setRunningState(RunningState runningState) {
        RunningState oldRunningState = this.runningState;
        this.runningState = runningState;
        propertyChangeSupport.firePropertyChange(PROP_RUNNINGSTATE, oldRunningState, runningState);
    }

    public void cancelTraining() {
        if (learningState == LearningState.TRAINING) {
            w.getTrainingRunner().cancel();
            //trainingWorker.cancel(true);
        }
    }
    
    /**
     * Get the value of learningState
     *
     * @return the value of learningState
     */
    public LearningState getLearningState() {
        return learningState;
    }

    /**
     * Set the value of learningState
     *
     * @param learningState new value of learningState
     */
    private void setLearningState(LearningState learningState) {
        LearningState oldLearningState = this.learningState;
        this.learningState = learningState;
        updateAbleToRun();
        propertyChangeSupport.firePropertyChange(PROP_LEARNINGSTATE, oldLearningState, learningState);
    }

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

    
    public LearningManager(Wekinator w) {
        this.w = w;
        inputNamesToIndices = new HashMap<>();
        pathsToOutputIndices = new HashMap<>();
        //TODO listen for changes in input names, # outputs or inputs, etc.
        w.getInputManager().addPropertyChangeListener(this::inputGroupChanged);
        w.getOutputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == OutputManager.PROP_OUTPUTGROUP) {
                    outputGroupChanged(evt);
                }
            }
        }); 
        
        w.getDataManager().addPropertyChangeListener(this::dataManagerPropertyChange); 
        
        w.getOSCReceiver().addPropertyChangeListener(this::oscReceiverPropertyChanged);
    }
    
    private void oscReceiverPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == OSCReceiver.PROP_CONNECTIONSTATE) {
            updateAbleToRun();
            updateAbleToRecord();
        }
    }
    
    private void dataManagerPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataManager.PROP_NUMEXAMPLESPEROUTPUT) {
            IndexedPropertyChangeEvent evt1 = (IndexedPropertyChangeEvent)evt;
            int newVal = (Integer)evt1.getNewValue();
            paths.get(evt1.getIndex()).notifyExamplesChanged(newVal);
            pathNumExamplesChanged(evt1.getIndex());
            
        }
    }
    
    //For now, assume only 1 path per output is allowed in system
    public List<Path> getPaths() {
        return paths;
    }
    
    private void initializeInputIndices(String[] inputNames) {
        inputNamesToIndices.clear();
        for (int i = 0; i < inputNames.length; i++) {
            inputNamesToIndices.put(inputNames[i], i);
        }
    }
    
    //TODO (low): merge this with other init function
    //TODO: set able to record, able to run here
    public void initializeInputsAndOutputsWithExisting(Instances data, List<Path> paths) {
        String[] inputNames = w.getInputManager().getInputNames();
        initializeInputIndices(inputNames);
        int numOutputs = w.getOutputManager().getOutputGroup().getNumOutputs();
        pathRecordingMask = new boolean[numOutputs];
        pathRunningMask = new boolean[numOutputs];
        myComputedOutputs = new double[numOutputs];
        
        for (int i = 0; i < numOutputs; i++) {
            pathRecordingMask[i] = paths.get(i).isRecordEnabled();
            pathRunningMask[i] = paths.get(i).isRunEnabled();
            //OSCOutput o = w.getOutputManager().getOutputGroup().getOutput(i);
            //Path p = new Path(o, inputNames, w);
            Path p = paths.get(i);
            PropertyChangeListener pChange = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    pathChanged(p, evt);
                }
            };
            p.addPropertyChangeListener(wls.propertyChange(pChange));
            p.addInputSelectionChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    pathInputsChanged((Path)e.getSource());
                }
            });
            pathsToOutputIndices.put(p, i);
            this.paths.add(p);
        }  
        w.getDataManager().initialize(inputNames, w.getOutputManager().getOutputGroup(), data);

        if (w.getDataManager().getNumExamples() >0) {
            setLearningState(LearningState.READY_TO_TRAIN);
        } else {
            setLearningState(LearningState.NOT_READY_TO_TRAIN);
        }        
        w.getInputManager().addInputValueListener(this::updateInputs);
        updateAbleToRecord();
        updateAbleToRun();
    }
    
    private void updateAbleToRecord() {
       if (w.getOSCReceiver().getConnectionState() != OSCReceiver.ConnectionState.CONNECTED) {
            setAbleToRecord(false);
        } else {
           setAbleToRecord(true);
       }    
    }
    
    private void updateAbleToRun() {
        //Requires models in runnable state (at least some)
        if (w.getOSCReceiver().getConnectionState() != OSCReceiver.ConnectionState.CONNECTED) {
            setAbleToRun(false);
            return;
        }
        if (learningState == LearningState.DONE_TRAINING) {
            for (Path p : paths) {
                if (p.canCompute()) {
                    setAbleToRun(true);
                    return;
                }
            }   
            setAbleToRun(false);   
        } else {
            setAbleToRun(false);
        }
    }
    
    //Call when both Input and Output Managers are ready
    public void initializeInputsAndOutputs() {
        String[] inputNames = w.getInputManager().getInputNames();
        initializeInputIndices(inputNames);
        int numOutputs = w.getOutputManager().getOutputGroup().getNumOutputs();
      //  initializeOutputIndices(outputNames);
        w.getDataManager().initialize(inputNames, w.getOutputManager().getOutputGroup());
        pathRecordingMask = new boolean[numOutputs];
        pathRunningMask = new boolean[numOutputs];
        myComputedOutputs = new double[numOutputs];
        
        for (int i = 0; i < numOutputs; i++) {
            pathRecordingMask[i] = true;
            pathRunningMask[i] = true;
            
            OSCOutput o = w.getOutputManager().getOutputGroup().getOutput(i);
            Path p = new Path(o, inputNames, w);
            PropertyChangeListener pChange = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    pathChanged(p, evt);
                }
            };
            p.addPropertyChangeListener(wls.propertyChange(pChange));
            p.addInputSelectionChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    pathInputsChanged((Path)e.getSource());
                }
            });
            pathsToOutputIndices.put(p, i);
            paths.add(p);
        }  
        setLearningState(LearningState.NOT_READY_TO_TRAIN);
        
        w.getInputManager().addInputValueListener(this::updateInputs);
    }
    
    private void pathInputsChanged(Path p) {
        Integer outputIndex = pathsToOutputIndices.get(p);
        if (outputIndex == null) {
            System.out.println("ERROR: path not found in pathInputsChanged");
            return;
        }
        String[] inputs = p.getSelectedInputs();
        int[] indices = convertInputNamesToIndices(inputs);
        w.getDataManager().setInputIndicesForOutput(indices, outputIndex);
    }
    
    private int[] convertInputNamesToIndices(String[] names) {
        int[] indices = new int[names.length];
        for (int i = 0; i < names.length; i++) {
            Integer index = inputNamesToIndices.get(names[i]);
            if (index == null) {
                indices[i] = 0;
                System.out.println("ERROR name " + names[i] + " not found");
            } else {
                indices[i] = index;
            }
        } 
        return indices;
    }
    
    private void pathRecordChanged(Path p) {
        Integer index = pathsToOutputIndices.get(p);
        if (index != null) {
            pathRecordingMask[index] = p.isRecordEnabled();
        } else {
            System.out.println("ERROR : Null path in pathRecordChanged");
        }
    }
    
    private void pathRunChanged(Path p) {
        Integer index = pathsToOutputIndices.get(p);
        if (index != null) {
            pathRunningMask[index] = p.isRunEnabled();
        } else {
            System.out.println("ERROR : Null path in pathRunChanged");
        }
    }
    
    private boolean noExamplesAnywhere() {
        for (Path p : paths) {
            if (p.getNumExamples() > 0) {
                return false;
            }
        }
        return true;
    }
    
    private void pathNumExamplesChanged(int pathIndex) {
        
        if (learningState == LearningState.NOT_READY_TO_TRAIN && paths.get(pathIndex).getNumExamples() >0){
            setLearningState(LearningState.READY_TO_TRAIN);
        } else if (learningState == LearningState.READY_TO_TRAIN && paths.get(pathIndex).getNumExamples() == 0) {
            if (noExamplesAnywhere()) {
                setLearningState(LearningState.NOT_READY_TO_TRAIN);
            }
        }
    }
    
    //Called when modelState changed
    private void pathChanged(Path p, PropertyChangeEvent evt) {
        if (evt.getPropertyName() == Path.PROP_RECORDENABLED) {
            pathRecordChanged(p);
        } else if (evt.getPropertyName() == Path.PROP_RUNENABLED) {
            pathRunChanged(p);         
        } /*else if (evt.getPropertyName() == Path.PROP_NUMEXAMPLES) {
            pathNumExamplesChanged(p);
        } */ //this is called explicitly when we hear back from Datamanager

        //if (evt.getPropertyName() == Path.)
        //TODO: listen for record/run enable change and update our Mask
       // System.out.println("What do we do? Path changed for output: " + p.getOSCOutput().getName());
        
    }
    
    //Right now, this simply won't change indices where mask is false
    public double[] computeValues(double[] inputs, boolean[] computeMask) {
        for (int i = 0; i < computeMask.length; i++) {
            if (computeMask[i] && paths.get(i).canCompute()) {
                Instance instance = w.getDataManager().getClassifiableInstanceForOutput(inputs, i);
                try {
                    myComputedOutputs[i] = paths.get(i).compute(instance);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error encountered in computing: {0}", ex.getMessage());
                }
            } else {
                myComputedOutputs[i] = w.getOutputManager().getCurrentValues()[i];
            }
        }
        return myComputedOutputs;
    }
    
    public void addToTraining(double[] inputs, double[] outputs, boolean[] recordingMask) {
        /*double[] trainingOutputs = new double[outputs.length];
        
        for (int i = 0; i < outputs.length; i++) {
            if (recordingMask[i]) {
                
            }
        } */
        setNumExamplesThisRound(numExamplesThisRound+1);
        w.getDataManager().addToTraining(inputs, outputs, recordingMask, recordingRound);
    } 
    
   /* public boolean wasCancelled() {
        return wasCancelled;
    } */
    
    public int numRunnableModels() {
        int i = 0; 
        for (Path p : paths) {
            if (p.canCompute()) {
                i++;
            }
        }
        return i;
    }
    
    //TODO: Need to do this in background and change training state
   /* public void buildModels(boolean trainMask[]) {
        for (int i = 0; i < trainMask.length; i++) {
            if (trainMask[i]) {
                paths.get(i).buildModel(
                        paths.get(i).getOSCOutput().getName() + "-" + trainingRound,
                        w.getDataManager().getTrainingDataForOutput(i));
            }
        }
        trainingRound++;
    } */
    
    //
    
    public void updateInputs(double[] inputs) {
        if (recordingState == RecordingState.RECORDING) {
            addToTraining(inputs, w.getOutputManager().getCurrentValues(), pathRecordingMask);
        } else if (runningState == RunningState.RUNNING) {
            double[] d = computeValues(inputs, pathRunningMask);
            w.getOutputManager().setNewComputedValues(d);
        }
    }
    
    private void outputGroupChanged(PropertyChangeEvent evt) {
        System.out.println("ERROR: LearningManager doesn't know how to handle this output group change");
    }
    
    private void inputGroupChanged(PropertyChangeEvent evt) {
        System.out.println("ERROR: LearningManager doesn't know how to handle this input group change");
    }
    
    public void deleteExamplesForPath(Path myPath) {
        Integer whichPath = pathsToOutputIndices.get(myPath);
        if (whichPath == null) {
            System.out.println("ERROR: My Path not found in deleteDataForPath");
            return;
        }
        w.getDataManager().setOutputMissingForAll(whichPath);
    }
    
    //Currently coming from Path GUI (row)
    public void setOutputValueForPath(double value, Path p) {
        w.getOutputManager().setNewValueFromGUI(pathsToOutputIndices.get(p), value);
    }

    public void deleteAllExamples() {
        w.getDataManager().deleteAll();
        setLearningState(LearningState.NOT_READY_TO_TRAIN);
    }
    
    public void buildAll() {
        //Launch training threads & get notified ...        
        synchronized (this) {
            List<Instances> data = new ArrayList<>(paths.size());
            for (Path p : paths) {
                data.add(w.getDataManager().getTrainingDataForOutput(pathsToOutputIndices.get(p)));
            }
            w.getTrainingRunner().buildAll(paths, data, trainingWorkerListener);
            setLearningState(LearningState.TRAINING);      
        }      
    }
    
   
    /**
     * Get the value of numExamplesThisRound
     *
     * @return the value of numExamplesThisRound
     */
    public int getNumExamplesThisRound() {
        return numExamplesThisRound;
    }

    /**
     * Set the value of numExamplesThisRound
     *
     * @param numExamplesThisRound new value of numExamplesThisRound
     */
    public void setNumExamplesThisRound(int numExamplesThisRound) {
        int oldNumExamplesThisRound = this.numExamplesThisRound;
        this.numExamplesThisRound = numExamplesThisRound;
        propertyChangeSupport.firePropertyChange(PROP_NUMEXAMPLESTHISROUND, oldNumExamplesThisRound, numExamplesThisRound);
    }
    
        /**
     * Get the value of ableToRun
     *
     * @return the value of ableToRun
     */
    public boolean isAbleToRun() {
        return ableToRun;
    }

    /**
     * Set the value of ableToRun
     *
     * @param ableToRun new value of ableToRun
     */
    public void setAbleToRun(boolean ableToRun) {
        boolean oldAbleToRun = this.ableToRun;
        this.ableToRun = ableToRun;
        propertyChangeSupport.firePropertyChange(PROP_ABLE_TO_RUN, oldAbleToRun, ableToRun);
    }

    
    /**
     * Get the value of ableToRecord
     *
     * @return the value of ableToRecord
     */
    public boolean isAbleToRecord() {
        return ableToRecord;
    }

    /**
     * Set the value of ableToRecord
     *
     * @param ableToRecord new value of ableToRecord
     */
    public void setAbleToRecord(boolean ableToRecord) {
        boolean oldAbleToRecord = this.ableToRecord;
        this.ableToRecord = ableToRecord;
        propertyChangeSupport.firePropertyChange(PROP_ABLE_TO_RECORD, oldAbleToRecord, ableToRecord);
    }

    public void setModelBuilderForPath(ModelBuilder mb, int i) {
        paths.get(i).setModelBuilder(mb);
    }
    
}

