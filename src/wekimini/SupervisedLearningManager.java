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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingworker.SwingWorker;
import weka.core.Instances;
import weka.core.Instance;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCReceiver;
import wekimini.util.WeakListenerSupport;

/**
 *
 * @author rebecca
 */
public class SupervisedLearningManager implements ConnectsInputsToOutputs {

    private final List<Path> paths = new ArrayList<>(5);
    private final Wekinator w;
    private final WekinatorSupervisedLearningController controller;
    private final WeakListenerSupport wls = new WeakListenerSupport();
    private boolean[] pathRecordingMask;
    private boolean[] pathRunningMask;
    private double[] myComputedOutputs;
    private int recordingRound = 0;
    private final HashMap<String, Integer> inputNamesToIndices;
    private final HashMap<Path, Integer> pathsToOutputIndices;

    public static final String PROP_LEARNINGSTATE = "learningState";
    private LearningState learningState = LearningState.NOT_READY_TO_TRAIN;

    private RunningState runningState = RunningState.NOT_RUNNING;
    public static final String PROP_RUNNINGSTATE = "runningState";

    private RecordingState recordingState = RecordingState.NOT_RECORDING;
    public static final String PROP_RECORDINGSTATE = "recordingState";
   
    public static final String PROP_RECORDINGROUND = "recordingRound";

    private int numExamplesThisRound;

    public static final String PROP_NUMEXAMPLESTHISROUND = "numExamplesThisRound";
    private static final Logger logger = Logger.getLogger(SupervisedLearningManager.class.getName());

    protected PropertyChangeListener trainingWorkerListener;

    private boolean ableToRecord = false;

    public static final String PROP_ABLE_TO_RECORD = "ableToRecord";

    private boolean ableToRun = false;

    public static final String PROP_ABLE_TO_RUN = "ableToRun";
    private boolean notifyPathsOfDatasetChange = true;

    private final List<PathOutputTypeEditedListener> pathEditedListeners = new LinkedList<>();
    private final List<InputOutputConnectionsListener> inputOutputConnectionsListeners = new LinkedList<>();

    private boolean computeDistribution = false;

    /**
     * Get the value of computeDistribution
     *
     * @return the value of computeDistribution
     */
    /*public boolean isComputeDistribution() {
     return computeDistribution;
     } */
    /**
     * Set the value of computeDistribution
     *
     * @param computeDistribution new value of computeDistribution
     */
    /* public void setComputeDistribution(boolean computeDistribution) {
     this.computeDistribution = computeDistribution;
     } */
    boolean isLegalTrainingValue(int whichOutput, float value) {
        return getPaths().get(whichOutput).getOSCOutput().isLegalTrainingValue(value);
    }

    private List<List<Double>> computeBundleValues(int numInputs, List<List<Double>> values, boolean[] computeMask) {
        //int numPoints = (Integer) values.get(0);
        List<List<Double>> newValues = new ArrayList<List<Double>>();
        ///int whichValue = 1; //Starts at 1
        for (List<Double> nextValue : values) {
        
        //for (int n = 0; n < numPoints; n++) {
            double[] nextInput = new double[numInputs];
            for (int i = 0; i< numInputs; i++) {
                nextInput[i] = nextValue.get(i);
            }

            List<Double> theseValues = new ArrayList<Double>(computeMask.length);
            for (int i = 0; i < computeMask.length; i++) {
                if (computeMask[i] && paths.get(i).canCompute()) {
                    Instance instance = w.getDataManager().getClassifiableInstanceForOutput(nextInput, i);
                    try {
                        //myComputedOutputs[i] = paths.get(i).compute(instance);
                        theseValues.add(paths.get(i).compute(instance));
                    } catch (Exception ex) {
                        theseValues.add(new Double(0));
                        logger.log(Level.SEVERE, "Error encountered in computing: {0}", ex.getMessage());
                    }
                } else {
                    theseValues.add(w.getOutputManager().getCurrentValues()[i]);
                    //myComputedOutputs[i] = w.getOutputManager().getCurrentValues()[i];
                }
            }
            newValues.add(theseValues);
        }
        return newValues;
    }

    private double[][] computeBundleDistributions(int numInputs, List<List<Double>> values, int whichOutput) {
       // int numPoints = (Integer) values.get(0);
        double[][] allDistributions = new double[values.size()][];
       // int whichValue = 1; //starts at 1
        
        
        if (paths.get(whichOutput).canCompute()) {
            int n = 0;
            for (List<Double> nextValue : values) {
            
            //for (int n = 0; n < numPoints; n++) {
                double[] nextInput = new double[numInputs];
                for (int i = 0; i < numInputs; i++) {
                    nextInput[i] = nextValue.get(i);
                }
                Instance instance = w.getDataManager().getClassifiableInstanceForOutput(nextInput, whichOutput);
                double[] thisDist = paths.get(whichOutput).computeDistribution(instance);
                allDistributions[n++] = thisDist; 
            }
        } else {
            for (int n = 0; n < values.size(); n++) {
                double[] nextDist = new double[((OSCClassificationOutput) paths.get(whichOutput).getOSCOutput()).getNumClasses()];
                allDistributions[n] = nextDist;
            }
        }
        return allDistributions;
    }

    void saveModels(String directory, Wekinator w) throws IOException {
        //paths = getPaths();
        String location = directory + File.separator;
        for (int i = 0; i < paths.size(); i++) {
            String filename = location + "model" + i + ".xml";
            paths.get(i).writeToFile(filename);
        }
    }

    public static enum LearningState {

        DONE_TRAINING, TRAINING, READY_TO_TRAIN, NOT_READY_TO_TRAIN
    };

    public static enum RunningState {

        RUNNING, NOT_RUNNING
    };

    public static enum RecordingState {

        RECORDING, NOT_RECORDING
    };

    public void addPathEditedListener(PathOutputTypeEditedListener l) {
        pathEditedListeners.add(l);
    }

    public void removePathEditedListener(PathOutputTypeEditedListener l) {
        pathEditedListeners.remove(l);
    }

    private void trainingWorkerChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                try {
                    //SwingWorker.StateValue.
                    //System.out.println("!!!TODOTODOTODOshould be setting training to false here");
                    KadenzeLogging.getLogger().supervisedTrainFinished(w);
                } catch (IOException ex) {
                    Logger.getLogger(SupervisedLearningManager.class.getName()).log(Level.SEVERE, null, ex);
                }

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
    
    public void setRecordingRound(int newRound) {
        int oldRecordingRound = this.recordingRound;
        this.recordingRound = newRound;
        propertyChangeSupport.firePropertyChange(PROP_RECORDINGROUND, oldRecordingRound, this.recordingRound);
    }

    public void startRecording() {
        numExamplesThisRound = 0;
       // KadenzeLogging.getLogger().logEvent(w, Assignment12Logger.KEvent.SUPERVISED_RECORD_START);
        KadenzeLogging.getLogger().supervisedLearningRecordStarted(w);
        setRecordingRound(recordingRound+1);
        setRecordingState(RecordingState.RECORDING);
    }

    public void stopRecording() {
        KadenzeLogging.getLogger().supervisedLearningRecordStopped(w);
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
            KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.TRAIN_CANCEL);
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
    
    //TODO (low priority: merge constructors into one)
    public SupervisedLearningManager (Wekinator w, Instances data, List<Path> paths) {
        this.w = w;
        controller = new WekinatorSupervisedLearningController(this, w);
        inputNamesToIndices = new HashMap<>();
        pathsToOutputIndices = new HashMap<>();
        //TODO listen for changes in input names, # outputs or inputs, etc.

        trainingWorkerListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                trainingWorkerChanged(evt);
            }
        };

        // w.getInputManager().addPropertyChangeListener(this::inputGroupChanged);
        w.getInputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                inputGroupChanged(evt);
            }
        });

        w.getOutputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == OutputManager.PROP_OUTPUTGROUP) {
                    outputGroupChanged(evt);
                }
            }
        });

        // w.getDataManager().addPropertyChangeListener(this::dataManagerPropertyChange);
        w.getDataManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                dataManagerPropertyChange(evt);
            }
        });

        w.getOSCReceiver().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscReceiverPropertyChanged(evt);
            }
        });
        //updateAbleToRecord();
        initializeInputsAndOutputsWithExisting(data, paths);
        updateAbleToRecord();
        updateAbleToRun();
    }

    public SupervisedLearningManager(Wekinator w) {
        this.w = w;
        controller = new WekinatorSupervisedLearningController(this, w);
        inputNamesToIndices = new HashMap<>();
        pathsToOutputIndices = new HashMap<>();
        //TODO listen for changes in input names, # outputs or inputs, etc.

        trainingWorkerListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                trainingWorkerChanged(evt);
            }
        };

        // w.getInputManager().addPropertyChangeListener(this::inputGroupChanged);
        w.getInputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                inputGroupChanged(evt);
            }
        });

        w.getOutputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == OutputManager.PROP_OUTPUTGROUP) {
                    outputGroupChanged(evt);
                }
            }
        });

        // w.getDataManager().addPropertyChangeListener(this::dataManagerPropertyChange);
        w.getDataManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                dataManagerPropertyChange(evt);
            }
        });

        w.getOSCReceiver().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscReceiverPropertyChanged(evt);
            }
        });
        //updateAbleToRecord();
        initializeInputsAndOutputs();
        updateAbleToRecord();
        updateAbleToRun();

    }

    public WekinatorSupervisedLearningController getSupervisedLearningController() {
        return controller;
    }

    private void oscReceiverPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == OSCReceiver.PROP_CONNECTIONSTATE) {
            updateAbleToRun();
            updateAbleToRecord();
        }
    }

    private void dataManagerPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataManager.PROP_NUMEXAMPLESPEROUTPUT) {
            if (notifyPathsOfDatasetChange) {
                IndexedPropertyChangeEvent evt1 = (IndexedPropertyChangeEvent) evt;
                int newVal = (Integer) evt1.getNewValue();
                paths.get(evt1.getIndex()).notifyExamplesChanged(newVal);
                pathNumExamplesChanged(evt1.getIndex());
            }

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

    //newConnections[i][j] is true if input i is connected to output j
    @Override
    public void updateInputOutputConnections(boolean[][] newConnections) {
        if (newConnections.length != w.getInputManager().getInputNames().length
                || newConnections[0].length != w.getOutputManager().getOutputGroup().getNumOutputs()) {
            throw new IllegalArgumentException("newConnections must have same rows as number of inputs and same columns as number of outputs");
        }

        List<List<String>> newInputsForPaths = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            List<String> next = new ArrayList<>();
            newInputsForPaths.add(next);
        }

        for (int input = 0; input < newConnections.length; input++) {
            for (int output = 0; output < newConnections[0].length; output++) {
                if (newConnections[input][output]) {
                    //Output output uses input
                    newInputsForPaths.get(output).add(w.getInputManager().getInputNames()[input]);
                }
            }
        }
        int temp = 0; //Check here! TODO
        for (int i = 0; i < paths.size(); i++) {
            paths.get(i).setSelectedInputs(newInputsForPaths.get(i).toArray(new String[0]));
        }
        notifyNewInputOutputConnections(newConnections);
        w.getStatusUpdateCenter().update(this, "Input/Output connections updated.");
    }

    @Override
    public boolean[][] getConnectionMatrix() {
        boolean[][] b = new boolean[w.getInputManager().getNumInputs()][w.getOutputManager().getOutputGroup().getNumOutputs()];
        for (int input = 0; input < b.length; input++) {
            for (int output = 0; output < b[0].length; output++) {
                Path p = paths.get(output);
                b[input][output] = p.isUsingInput(w.getInputManager().getInputNames()[input]);
            }
        }
        return b;
    }

    //TODO (low): merge this with other init function
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
            final Path p = paths.get(i);
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
                    pathInputsChanged((Path) e.getSource());
                }
            });

            pathsToOutputIndices.put(p, i);
            this.paths.add(p);
            KadenzeLogging.getLogger().logModelBuilderUpdated(w, p.getModelBuilder(), i);
        }

        //Without this, paths will think that examples have changed since their training    
        notifyPathsOfDatasetChange = false;
        w.getDataManager().initialize(inputNames, w.getOutputManager().getOutputGroup(), data);
        notifyPathsOfDatasetChange = true;

        boolean anyTrained = false;
        for (Path p : paths) {
            if (p.canCompute()) {
                anyTrained = true;
                break;
            }
        }
        if (anyTrained) {
            setLearningState(LearningState.DONE_TRAINING);
        } else if (w.getDataManager().getNumExamples() > 0) {
            setLearningState(LearningState.READY_TO_TRAIN);
        } else {
            setLearningState(LearningState.NOT_READY_TO_TRAIN);
        }

        for (int i = 0; i < paths.size(); i++) {
            Path p = paths.get(i);
            String[] inputs = p.getSelectedInputs();
            int[] indices = convertInputNamesToIndices(inputs);
            w.getDataManager().setInputIndicesForOutput(indices, i);
        }
        // w.getInputManager().addInputValueListener(this::updateInputs);
        w.getInputManager().addInputValueListener(new InputManager.InputListener() {

            @Override
            public void update(double[] vals) {
                updateInputs(vals);
            }

            @Override
            public void notifyInputError() {

            }

            @Override
            public void updateBundle(List<List<Double>> values) {
                updateInputBundle(values);
            }
        });

        updateAbleToRecord();
        updateAbleToRun();
        setRecordingRound(w.getDataManager().getMaxRecordingRound());
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
        //if (learningState == LearningState.DONE_TRAINING ) {
        if (learningState != LearningState.TRAINING) {
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
            final Path p = new Path(o, inputNames, w, this);
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
                    pathInputsChanged((Path) e.getSource());
                }
            });
            pathsToOutputIndices.put(p, i);
            paths.add(p);
        }
        setLearningState(LearningState.NOT_READY_TO_TRAIN);

        //w.getInputManager().addInputValueListener(this::updateInputs);
        w.getInputManager().addInputValueListener(new InputManager.InputListener() {

            @Override
            public void update(double[] vals) {
                updateInputs(vals);
            }

            @Override
            public void notifyInputError() {
            }

            @Override
            public void updateBundle(List<List<Double>> values) {
                updateInputBundle(values);
            }
        });
    }

    private void pathInputsChanged(Path p) {
        Integer outputIndex = pathsToOutputIndices.get(p);
        if (outputIndex == null) {
            logger.log(Level.SEVERE, "Path not found in pathInputsChanged");
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
                logger.log(Level.SEVERE, "name {0} not found", names[i]);
                // System.out.println("ERROR name " + names[i] + " not found");
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
            logger.log(Level.SEVERE, "Null path in pathRecordChanged");
        }
    }

    private void pathRunChanged(Path p) {
        Integer index = pathsToOutputIndices.get(p);
        if (index != null) {
            pathRunningMask[index] = p.isRunEnabled();
        } else {
            logger.log(Level.SEVERE, "Null path in pathRunChanged");
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

        if (learningState == LearningState.NOT_READY_TO_TRAIN && paths.get(pathIndex).getNumExamples() > 0) {
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
        KadenzeLogging.getLogger().supervisedRunData(w, inputs, computeMask, myComputedOutputs);
        return myComputedOutputs;
    }

    public double[] computeProbabilisticOutputs(double[] inputs, int whichOutput) {
        double[] outputs;
        if (paths.get(whichOutput).canCompute()) {
            Instance instance = w.getDataManager().getClassifiableInstanceForOutput(inputs, whichOutput);
            outputs = paths.get(whichOutput).computeDistribution(instance);
        } else {
            outputs = new double[((OSCClassificationOutput) paths.get(whichOutput).getOSCOutput()).getNumClasses()];
        }
        return outputs;
    }

    public void addToTraining(double[] inputs, double[] outputs, boolean[] recordingMask) {
        /*double[] trainingOutputs = new double[outputs.length];
        
         for (int i = 0; i < outputs.length; i++) {
         if (recordingMask[i]) {
                
         }
         } */
        setNumExamplesThisRound(numExamplesThisRound + 1);
        w.getDataManager().addToTraining(inputs, outputs, recordingMask, recordingRound);
    }
    
    public void addBundleToTraining(List<List<Double>> inputs, double[] outputs, boolean[] recordingMask) {
        int numInputs = w.getInputManager().getNumInputs();
        //setNumExamplesThisRound(numExamplesThisRound + inputs.size());
       // int currentValue = 1; //data starts at 1!
        for (List<Double> thisList : inputs) {
        //while (currentValue < inputs.size()) {
            double[] nextInputs = new double[numInputs];
            for (int i = 0; i < numInputs; i++) {
                nextInputs[i] = thisList.get(i);
            }
            w.getDataManager().addToTraining(nextInputs, outputs, recordingMask, recordingRound);
        }
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
    //TODO: Want ability to compute distribution to be done model by model! (in model editor)
    public void updateInputs(double[] inputs) {
        if (recordingState == RecordingState.RECORDING) {
            addToTraining(inputs, w.getOutputManager().getCurrentValues(), pathRecordingMask);
        } else if (runningState == RunningState.RUNNING) {
            double[] d = computeValues(inputs, pathRunningMask);
            w.getOutputManager().setNewComputedValues(d);

            //TODO: Get rid of search every time : slows us down!
            for (int i = 0; i < w.getOutputManager().getOutputGroup().getNumOutputs(); i++) {
                if ((w.getOutputManager().getOutputGroup().getOutput(i) instanceof OSCClassificationOutput)
                        && ((OSCClassificationOutput) w.getOutputManager().getOutputGroup().getOutput(i)).isSendingDistribution()) {
                    double[] dist = computeProbabilisticOutputs(inputs, i);
                    w.getOutputManager().setDistribution(i, dist);
                }

            }
        }
    }

    private void updateInputBundle(List<List<Double>> values) {
        if (recordingState == RecordingState.RECORDING) {
            //addToTraining(inputs, w.getOutputManager().getCurrentValues(), pathRecordingMask);
            addBundleToTraining(values, w.getOutputManager().getCurrentValues(), pathRecordingMask);
        } else if (runningState == RunningState.RUNNING) {
            List<List<Double>> allOutputs = computeBundleValues(w.getInputManager().getNumInputs(), values, pathRunningMask);
           // double[] d = computeValues(inputs, pathRunningMask);
            w.getOutputManager().setNewComputedBundleValues(allOutputs);

            //TODO: Get rid of search every time : slows us down!
            for (int i = 0; i < w.getOutputManager().getOutputGroup().getNumOutputs(); i++) {
                if ((w.getOutputManager().getOutputGroup().getOutput(i) instanceof OSCClassificationOutput)
                        && ((OSCClassificationOutput) w.getOutputManager().getOutputGroup().getOutput(i)).isSendingDistribution()) {
                    //double[] dist = computeProbabilisticOutputs(numPoints,, i);
                    double[][] allDistributions = computeBundleDistributions(w.getInputManager().getNumInputs(), values, i);
                    w.getOutputManager().setNewComputedBundleDistribution(i, allDistributions);
                }

            }
        }
    }

    private void outputGroupChanged(PropertyChangeEvent evt) {
        logger.log(Level.WARNING, "LearningManager doesn't know how to handle this output group change");
    }

    private void inputGroupChanged(PropertyChangeEvent evt) {
        logger.log(Level.WARNING, "LearningManager doesn't know how to handle this input group change");
    }

    public void deleteExamplesForPath(Path myPath) {
        Integer whichPath = pathsToOutputIndices.get(myPath);
        if (whichPath == null) {
            logger.log(Level.SEVERE, "My Path not found in deleteDataForPath");
            return;
        }
        w.getDataManager().setOutputMissingForAll(whichPath);
        KadenzeLogging.getLogger().examplesDeletedForModel(w, whichPath);
    }

    //Currently coming from Path GUI (row) - should NOT be called when new output is computed!
    public void setOutputValueForPath(double value, Path p) {
        w.getOutputManager().setNewValueFromGUI(pathsToOutputIndices.get(p), value);
    }

    public void deleteAllExamples() {
        if (w.getDataManager().isInitialized()) {
            w.getDataManager().deleteAll();
            setLearningState(LearningState.NOT_READY_TO_TRAIN);
        }
    }

    public Instances getTrainingDataForPath(Path p, boolean includeMetadataFields) {
        return w.getDataManager().getTrainingDataForOutput(pathsToOutputIndices.get(p), includeMetadataFields);
    }

    public void buildAll() {
        KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.TRAIN_START);
        //Launch training threads & get notified ...        
        synchronized (this) {
            List<Instances> data = new ArrayList<>(paths.size());
            for (Path p : paths) {
                data.add(w.getDataManager().getTrainingDataForOutput(pathsToOutputIndices.get(p), false));
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

    public void setModelBuilderForPath(LearningModelBuilder mb, int i) {
        paths.get(i).setModelBuilder(mb);
        KadenzeLogging.getLogger().logModelBuilderUpdated(w, mb, i);
    }

    //BUG: This doesn't update listeners from old p to new p
    public void updatePath(Path p, OSCOutput newOutput, LearningModelBuilder newModelBuilder, String[] selectedInputNames) {
        //Which path?
        int which = paths.indexOf(p);
        if (which == -1) {
            logger.log(Level.WARNING, "Trying to update path that does not exist");
            throw new IllegalArgumentException("Trying to update path that does not exist");
        }

        KadenzeLogging.getLogger().logPathUpdated(w, which, p.getOSCOutput(), newOutput, p.getModelBuilder(), newModelBuilder, p.getSelectedInputs(), selectedInputNames);
        
        final Path newP;
        if (newOutput != null) {
            w.getOutputManager().updateOutput(newOutput, p.getOSCOutput()); //this triggers change in data manager
            newP = new Path(newOutput, selectedInputNames, w, this);
            newP.setNumExamples(p.getNumExamples());
            newP.setRecordEnabled(p.isRecordEnabled());
            newP.setRunEnabled(p.isRunEnabled());
            
            //TODO: Remove old property listeners!!
            p.removeListeners();
            PropertyChangeListener pChange = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    pathChanged(newP, evt);
                }
            };
            newP.addPropertyChangeListener(wls.propertyChange(pChange));
            newP.addInputSelectionChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    pathInputsChanged((Path) e.getSource());
                }
            });
            
            if (newModelBuilder != null) {
                newP.inheritModel(p);
                newP.setModelBuilder(newModelBuilder); //automatically indicates that re-training needed
            } else {
                newP.inheritModelAndBuilder(p);
                if (newP.getModelState() == Path.ModelState.BUILT) {
                    if (newOutput instanceof OSCClassificationOutput && p.getOSCOutput() instanceof OSCClassificationOutput) {
                        if (((OSCClassificationOutput) newOutput).getNumClasses() != ((OSCClassificationOutput) p.getOSCOutput()).getNumClasses()) {
                            newP.setModelState(Path.ModelState.NEEDS_REBUILDING);
                            //Don't currently have a good way of comparing Model itself with Output to see if rebuilding necessary
                        }
                    }
                }
                //If only output has changed, and not model builder, we may or may not need to update model state
                //DO need update if # classes has changed (if not for model output safety, then at least for saving safety)
                //DON'T need update if just NN doing int/real or hard/soft limits.
            }
        } else { //keep old output; just apply input names and possibly modelBuilder
            newP = p;
            newP.setSelectedInputs(selectedInputNames);
            if (newModelBuilder != null) {
                newP.setModelBuilder(newModelBuilder);
            }
        }

        //Finally: Only do this when path object has changed:
        if (newOutput != null) {
            //Update path here, then fire change 
            paths.remove(p);
            paths.add(which, newP);
            pathsToOutputIndices.remove(p);
            pathsToOutputIndices.put(newP, which);
            notifyPathEditedListeners(which, newP, p);
        }
    }

    private void notifyPathEditedListeners(int which, Path newPath, Path oldPath) {
        for (PathOutputTypeEditedListener l : pathEditedListeners) {
            l.pathOutputTypeEdited(which, newPath, oldPath);
        }
    }

    public interface PathOutputTypeEditedListener {

        public void pathOutputTypeEdited(int which, Path newPath, Path oldPath);
    }

    private void notifyNewInputOutputConnections(boolean[][] connections) {
        for (InputOutputConnectionsListener l : inputOutputConnectionsListeners) {
            l.newConnectionMatrix(connections);
        }
    }

    @Override
    public void addConnectionsListener(InputOutputConnectionsListener l) {
        inputOutputConnectionsListeners.add(l);
    }

    @Override
    public boolean removeConnectionsListener(InputOutputConnectionsListener l) {
        return inputOutputConnectionsListeners.remove(l);
    }

}
