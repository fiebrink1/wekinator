/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini; //m22

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Enumeration;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddValues;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.instance.RemoveWithValues;
import wekimini.featureanalysis.WrapperSelector;
import wekimini.gui.DatasetViewer;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.learning.SupervisedLearningModel;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
public class DataManager {

    protected EventListenerList listenerList = new EventListenerList();
    private ChangeEvent changeEvent = null;

    private final Wekinator w;
    //  private int[] outputInstanceCounts;
    private Filter[] trainingFilters; //Filter to produce instances for training
    private Filter[] runningFilters; //Filters to produce instances for running //TODO output after one or more models are trained, for correct models
    private Filter[] inputSavingFilters; //Filters to produce instances for saving on a per-output basis
    private boolean isInitialized = false;
    private OSCOutputGroup outputGroup;
    private String[] inputNames;
    private String[] outputNames;
    private List<int[]> inputListsForOutputsTraining;
    private List<int[]> inputListsForOutputsRunning; //TODO: Output after a model or all models are trained
    private Instances inputInstances = null;
    private List<Instances> featureInstances = null;
    public FeatureManager featureManager;
    private Instances allFeaturesInstances = null;
    public String[][] selectedFeatureNames;
    private int nextID = 1;
    private int numOutputs = 0;

    private static final int numMetaData = 3; //TODO

    private static final int idIndex = 0;
    private static final int timestampIndex = 1;
    private static final int recordingRoundIndex = 2;

    private Instances dummyInstances;
    private boolean[] isDiscrete;
    private int[] numClasses;

    private boolean hasInstances = false;

    public static final String PROP_HASINSTANCES = "hasInstances";

    private int[] numExamplesPerOutput;

    public static final String PROP_NUMEXAMPLESPEROUTPUT = "numExamplesPerOutput";

    private Instance[] deletedTrainingRound = null;
    private DatasetViewer viewer = null;
    private static final Logger logger = Logger.getLogger(DataManager.class.getName());

    /**
     * Get the value of numExamplesPerOutput
     *
     * @return the value of numExamplesPerOutput
     */
    public int[] getNumExamplesPerOutput() {
        return numExamplesPerOutput;
    }

    /**
     * Set the value of numEamplesPerOutput
     *
     * @param numExamplesPerOutput new value of numExamplesPerOutput
     */
    public void setNumExamplesPerOutput(int[] numExamplesPerOutput) {
        int[] oldNumExamplesPerOutput = this.numExamplesPerOutput;
        this.numExamplesPerOutput = numExamplesPerOutput;
        propertyChangeSupport.firePropertyChange(PROP_NUMEXAMPLESPEROUTPUT, oldNumExamplesPerOutput, numExamplesPerOutput);
    }

    /**
     * Get the value of numEamplesPerOutput at specified index
     *
     * @param index the index of numEamplesPerOutput
     * @return the value of numEamplesPerOutput at specified index
     */
    public int getNumExamplesPerOutput(int index) {
        return this.numExamplesPerOutput[index];
    }

    /**
     * Set the value of numEamplesPerOutput at specified index.
     *
     * @param index the index of numEamplesPerOutput
     * @param numEamplesPerOutput new value of numEamplesPerOutput at specified
     * index
     */
    public void setNumExamplesPerOutput(int index, int numEamplesPerOutput) {
        int oldNumEamplesPerOutput = this.numExamplesPerOutput[index];
        this.numExamplesPerOutput[index] = numEamplesPerOutput;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_NUMEXAMPLESPEROUTPUT, index, oldNumEamplesPerOutput, numEamplesPerOutput);
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

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final String prettyDateFormatString = "yyyy/MM/dd HH:mm:ss:SSS";
    private static final SimpleDateFormat prettyDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
    private static final DecimalFormat decimalFormat = new DecimalFormat("#################");
    //TODO: Should use min/max hard limits here too

    public DataManager(Wekinator w) {
        this.w = w;
        w.getOutputManager().addIndividualOutputEditListener(new OutputManager.OutputTypeEditListener() {

            @Override
            public void outputTypeEdited(OSCOutput newOutput, OSCOutput oldOutput, int which) {
                updateForOutputTypeEdit(newOutput, oldOutput, which);
            }
        });
        featureManager = new FeatureManager();
 
    }
    
    public void updateForOutputTypeEdit(OSCOutput newOutput, OSCOutput oldOutput, int which) {
        if (newOutput instanceof OSCClassificationOutput && oldOutput instanceof OSCClassificationOutput) {
            OSCClassificationOutput newO = (OSCClassificationOutput) newOutput;
            OSCClassificationOutput oldO = (OSCClassificationOutput) oldOutput;

            if (newO.getNumClasses() != oldO.getNumClasses()) {
                updateNumClasses(which, newO.getNumClasses(), oldO.getNumClasses());
            }
        } else if (newOutput instanceof OSCNumericOutput && oldOutput instanceof OSCNumericOutput) {
            OSCNumericOutput newO = (OSCNumericOutput) newOutput;
            OSCNumericOutput oldO = (OSCNumericOutput) oldOutput;
            if (newO.getLimitType() == OSCNumericOutput.LimitType.HARD && newO.getMin() > oldO.getMin() || newO.getMax() < oldO.getMax()) {
                updateMinMax(which, newO, oldO);
            }
        } else {
            logger.log(Level.SEVERE, "Unsupported change between classifier and numeric output");
        }

    }

    private void updateNumClasses(int index, int newNumClasses, int oldNumClasses) {
        //Update output values for
        if (newNumClasses < oldNumClasses) {
            makeMissingClassesGreaterThan(index, newNumClasses);
            updateInstancesForNewLowerMaxClass(index, newNumClasses);
        } else {
            updateInstancesForNewHigherMaxClass(index, newNumClasses);
        }

        //Finally:
        numClasses[index] = newNumClasses;
        try {
            updateTrainingAndSavingFiltersForOutput(index);
            updateRunningFiltersForOutput(index);

        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateMinMax(int index, OSCNumericOutput newOutput, OSCNumericOutput oldOutput) {
        //We know new output has hard limits and more restrictive limits
        makeMissingOutputsLessThan(index, newOutput.getMin());
        makeMissingOutputsGreaterThan(index, newOutput.getMax());
    }

    private void makeMissingClassesGreaterThan(int index, int maxClass) {
        for (int i = 0; i < inputInstances.numInstances(); i++) {
            double o = getOutputValue(i, index);
            if (o > maxClass) {
                setOutputMissing(i, index);
            }
        }
    }

    //TODO: MAKE SURE THIS WORKS FOR CLASSIFIER!
    private void makeMissingOutputsGreaterThan(int index, double max) {
        for (int i = 0; i < inputInstances.numInstances(); i++) {
            double o = getOutputValue(i, index);
            if (o > max) {
                setOutputMissing(i, index);
            }
        }
    }

    private void makeMissingOutputsLessThan(int index, double min) {
        for (int i = 0; i < inputInstances.numInstances(); i++) {
            double o = getOutputValue(i, index);
            if (o < min) {
                setOutputMissing(i, index);
            }
        }
    }

    private void updateInstancesForNewHigherMaxClass(int index, int newNumClasses) {
        //Change inputInstances, dummyInstances
        AddValues a = new AddValues();
        int oldMaxClasses = numClasses[index];
        StringBuilder sb = new StringBuilder();
        for (int i = oldMaxClasses + 1; i < newNumClasses; i++) {
            sb.append(Integer.toString(i)).append(",");
        }
        sb.append(Integer.toString(newNumClasses));
        
        Instances newAll;
        try {
            a.setAttributeIndex(Integer.toString(numMetaData + getNumInputs() + index + 1)); //Weka indexing stupidity
            a.setLabels(sb.toString());
            a.setSort(false);
            a.setInputFormat(inputInstances);
            newAll = Filter.useFilter(inputInstances, a);
        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (newAll.numInstances() != inputInstances.numInstances()) {
            logger.log(Level.SEVERE, "Problem: deleted instances when removing class attribute");
        }
        inputInstances = newAll;

        Instances newD;
        try {
            newD = Filter.useFilter(dummyInstances, a);
        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        dummyInstances = newD;
    }

    
    //REQUIRES that no instances with this value still exist in dataset
    private void updateInstancesForNewLowerMaxClass(int index, int newNumClasses) {
        //Change inputInstances, dummyInstances
        RemoveWithValues r = new RemoveWithValues();
        String rangeList = "1-" + (newNumClasses+1); //String indices start at 1 in weka

        Instances newAll;
        try {
            r.setAttributeIndex(Integer.toString(numMetaData + getNumInputs() + index + 1)); //Weka indexing stupidity
            r.setNominalIndices(rangeList);
            r.setInvertSelection(true); //Keep all classes from 0 to newNumClasses
            r.setMatchMissingValues(false);
            r.setModifyHeader(true);
            r.setInputFormat(inputInstances);

            newAll = Filter.useFilter(inputInstances, r);
        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (newAll.numInstances() != inputInstances.numInstances()) {
            logger.log(Level.SEVERE, "Problem: deleted instances when removing class attribute");
        }
        inputInstances = newAll;

        Instances newD;
        try {
            newD = Filter.useFilter(dummyInstances, r);
        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        dummyInstances = newD;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    //TODO: Property change support?
    private void setInitialized(boolean i) {
        isInitialized = i;
    }

    //Returns -1 if not found
    private int getInputIndexForName(String name) {
        for (int i = 0; i < inputNames.length; i++) {
            if (inputNames[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void setInputIndicesForOutput(int[] indices, int outputIndex, boolean waitForRetrainToApply) {
        int[] myIndices = new int[indices.length];
        System.arraycopy(indices, 0, myIndices, 0, indices.length);
        
        try {
            inputListsForOutputsTraining.set(outputIndex, myIndices);
            updateTrainingAndSavingFiltersForOutput(outputIndex);
            if (!waitForRetrainToApply) {
                inputListsForOutputsRunning.set(outputIndex, myIndices);
                updateRunningFiltersForOutput(outputIndex);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not update input selection filter", ex);
        }
    }

    public void addToTraining(double[] inputs, double[] outputs, boolean[] recordingMask, int recordingRound) {
        int thisId = nextID;
        nextID++;

        double myVals[] = new double[numMetaData + getNumInputs() + numOutputs];
        myVals[idIndex] = thisId;
        myVals[recordingRoundIndex] = recordingRound;

        Date now = new Date();

        String pretty = prettyDateFormat.format(now);
        try {
            myVals[timestampIndex] = inputInstances.attribute(timestampIndex).parseDate(pretty);
        } catch (ParseException ex) {
            myVals[timestampIndex] = 0;
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.arraycopy(inputs, 0, myVals, numMetaData, inputs.length); //TODO DOUBLECHECK
        System.arraycopy(outputs, 0, myVals, numMetaData + getNumInputs(), outputs.length);

        Instance in = new Instance(1.0, myVals);
        for (int i = 0; i < recordingMask.length; i++) {
            if (!recordingMask[i]) {
                in.setMissing(numMetaData + getNumInputs() + i);
            } else {
                setNumExamplesPerOutput(i, getNumExamplesPerOutput(i) + 1);
                // outputInstanceCounts[i]++;
            }
        }
        in.setDataset(inputInstances);
        inputInstances.add(in);
        setHasInstances(true);
        fireStateChanged();
    }

    public int getNumInputs() {
        return inputNames.length;
    }

    public int getNumOutputs() {
        return numOutputs;
    }

    public String getInputName(int i) {
        return inputNames[i];
    }

    public String getOutputName(int i) {
        return outputNames[i];
    }

    public void initialize(String[] inputNames, OSCOutputGroup outputGroup, Instances data) {
        initialize(inputNames, outputGroup);
        setFromDataset(data);
    }

    private void setFromDataset(Instances data) {
        inputInstances = new Instances(data);
        updateOutputCounts();
        updateNextId();
    }

    public int getMaxRecordingRound() {
        AttributeStats a = inputInstances.attributeStats(recordingRoundIndex);
        return (int) a.numericStats.max;
    }

    private void updateNextId() {
        AttributeStats a = inputInstances.attributeStats(idIndex);
        nextID = (int) a.numericStats.max + 1;
    }

    //Problem: gets called when loading from file...
    private void updateOutputCounts() {
        int[] examplesSum = new int[numOutputs];

        if (inputInstances.numInstances() > 0) {
            for (int i = inputInstances.numInstances() - 1; i >= 0; i--) {
                for (int j = 0; j < numOutputs; j++) {
                    if (!inputInstances.instance(i).isMissing(numMetaData + getNumInputs() + j)) {
                        examplesSum[j]++;
                    }
                }
            }

            for (int j = 0; j < numOutputs; j++) {
                setNumExamplesPerOutput(j, examplesSum[j]);
            }
            setHasInstances(true);
        } else {
            for (int j = 0; j < numOutputs; j++) {
                setNumExamplesPerOutput(j, 0);
            }
            setHasInstances(false);
        }
    }

    public boolean isOutputClassifier(int outputIndex) {
        return (outputGroup.getOutput(outputIndex) instanceof OSCClassificationOutput);
    }

    public void initialize(String[] inputNames, OSCOutputGroup outputGroup) {
        numOutputs = outputGroup.getNumOutputs();
        numExamplesPerOutput = new int[numOutputs];
        this.inputNames = new String[inputNames.length];
        System.arraycopy(inputNames, 0, this.inputNames, 0, inputNames.length);
        this.outputGroup = outputGroup;
        outputNames = outputGroup.getOutputNames();
        
        initializeOutputData();
        initializeInputLists();
        initializeInstances();

        try {
            setupFilters();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Couldn't set up filters correctly");
            WekiMiniRunner.getInstance().quitWithoutPrompt();
        }
        setInitialized(true);
    }
    
    public Instances getDummyInstances() {
        return dummyInstances;
    }
    
    public int getNumMetaData() {
        return numMetaData;
    }
    
    public String getTrainingFilterString(int which) {
        Reorder r = (Reorder)trainingFilters[which];
        return r.getAttributeIndices();
    }
    
    public String getRunningFilterString(int which) {
        Reorder r = (Reorder)runningFilters[which];
        return r.getAttributeIndices();
    }
    
    private void initializeInstances() {
        //Set up instances
        FastVector ff = new FastVector(getNumInputs() + numOutputs + numMetaData); //Include ID, timestamp, training round
        //add ID, timestamp, and training round #
        ff.addElement(new Attribute("ID"));
        ff.addElement(new Attribute("Time", prettyDateFormatString));
        ff.addElement(new Attribute("Training round"));
        //new Attribute

        //Add inputs
        for (int i = 0; i < getNumInputs(); i++) {
            ff.addElement(new Attribute(this.inputNames[i]));
        }
        featureInstances = new ArrayList(numOutputs);

        //Add outputs
        for (int i = 0; i < numOutputs; i++) {
            if (isDiscrete[i]) {
                //Create fastVector w/ possible
                FastVector classes = new FastVector(numOutputs);
                classes.addElement(new Integer(0).toString()); //Allow for 0 "no class" in ARFF specification
                for (int val = 0; val < numClasses[i]; val++) {
                    //System.out.println("Adding legal value " + (new Integer(val + 1)).toString());
                    classes.addElement((new Integer(val + 1)).toString()); //Values 1 to numClasses
                }
                ff.addElement(new Attribute(outputNames[i], classes));

            } else {
                ff.addElement(new Attribute(outputNames[i]));
            }
        }

        inputInstances = new Instances("dataset", ff, 100);
        //Set up dummy instances to reflect state of actual instances
        dummyInstances = new Instances(inputInstances);
    }
    
    private void updateInstancesForNewOutput(int which) {
        Attribute a;
        if (isDiscrete[which]) {
            //Create fastVector w/ possible
            FastVector classes = new FastVector(numOutputs);
            classes.addElement(new Integer(0).toString()); //Allow for 0 "no class" in ARFF specification
            for (int val = 0; val < numClasses[which]; val++) {
                    //System.out.println("Adding legal value " + (new Integer(val + 1)).toString());
                classes.addElement((new Integer(val + 1)).toString()); //Values 1 to numClasses
            }
            a = new Attribute(outputNames[which], classes);

        } else {
            a = new Attribute(outputNames[which]);
        }

        inputInstances.insertAttributeAt(a, numMetaData + getNumInputs() + which);
        dummyInstances = new Instances(inputInstances, 0);
    }

    private void initializeOutputData() {
        isDiscrete = new boolean[numOutputs];
        numClasses = new int[numOutputs];
        for (int i = 0; i < numOutputs; i++) {
            isDiscrete[i] = (outputGroup.getOutput(i) instanceof OSCClassificationOutput);
            if (isDiscrete[i]) {
                numClasses[i] = ((OSCClassificationOutput) outputGroup.getOutput(i)).getNumClasses();
            }
        }
        featureManager.addOutputs(numOutputs, inputNames);
    }
    
    private void updateOutputDataForNewOutput(int which) {
        boolean isNewDiscrete = (outputGroup.getOutput(which) instanceof OSCClassificationOutput);
        isDiscrete = insertIntoArray(isNewDiscrete, isDiscrete, which);
        if (isNewDiscrete) {
            int numNewClasses = ((OSCClassificationOutput) outputGroup.getOutput(which)).getNumClasses();
            numClasses = insertIntoArray(numNewClasses, numClasses, which);
        } else {
            numClasses = insertIntoArray(0, numClasses, which);
        }
    }

    private void initializeInputLists() {
        inputListsForOutputsTraining = new ArrayList<>(numOutputs);
        inputListsForOutputsRunning = new ArrayList<>(numOutputs);

        int inputList[] = new int[getNumInputs()];
        for (int i = 0; i < getNumInputs(); i++) {
            inputList[i] = i;
        }
        for (int i = 0; i < numOutputs; i++) {
            inputListsForOutputsTraining.add(inputList);
            inputListsForOutputsRunning.add(inputList);
        }
    }
    
    private void updateInputListsForNewOutput(int which) {
        int inputList[] = new int[getNumInputs()];
        for (int i = 0; i < getNumInputs(); i++) {
            inputList[i] = i;
        }
        inputListsForOutputsTraining.add(which, inputList);
        inputListsForOutputsRunning.add(which, inputList);
    }

    //Problem: This is being called when feature selection is changed,
    //before retraining happens (if at all)!
    private void updateTrainingAndSavingFiltersForOutput(int output) throws Exception {
        Reorder r = new Reorder();
        Reorder s = new Reorder();

        int[] inputList = inputListsForOutputsTraining.get(output); //includes only "selected" inputs
        int[] reordering = new int[inputList.length + 1];
        int[] saving = new int[numMetaData + inputList.length + 1];

        //Metadata
        for (int f = 0; f < numMetaData; f++) {
            saving[f] = f;
        }

        //Features
        for (int f = 0; f < inputList.length; f++) {
            reordering[f] = inputList[f] + numMetaData;
            saving[f + numMetaData] = inputList[f] + numMetaData;
        }

        //The actual "class" output
        reordering[reordering.length - 1] = numMetaData + getNumInputs() + output;
        saving[saving.length - 1] = numMetaData + getNumInputs() + output;

        r.setAttributeIndicesArray(reordering);
        r.setInputFormat(dummyInstances);

        s.setAttributeIndicesArray(saving);
        s.setInputFormat(dummyInstances);

        trainingFilters[output] = r;
        inputSavingFilters[output] = s;
    }

    private void updateRunningFiltersForOutput(int output) throws Exception {
        Reorder r = new Reorder();

        int[] inputList = inputListsForOutputsRunning.get(output); //includes only "selected" inputs
        int[] reordering = new int[inputList.length + 1];

        //Features
        for (int f = 0; f < inputList.length; f++) {
            reordering[f] = inputList[f] + numMetaData;
        }

        //The actual "class" output
        reordering[reordering.length - 1] = numMetaData + getNumInputs() + output;

        r.setAttributeIndicesArray(reordering);
        r.setInputFormat(dummyInstances);

        runningFilters[output] = r;
    }

    
    //This sets up filters in basic case, when no input reordering must be done.
    private void setupFilters() throws Exception {
        trainingFilters = new Reorder[numOutputs];
        runningFilters = new Reorder[numOutputs];
        inputSavingFilters = new Reorder[numOutputs];

        for (int i = 0; i < numOutputs; i++) {
            Reorder r = new Reorder();
            Reorder s = new Reorder();

            int[] inputList = inputListsForOutputsTraining.get(i);
            int[] reordering = new int[inputList.length + 1];
            int[] saving = new int[numMetaData + inputList.length + 1];

            //Metadata
            for (int f = 0; f < numMetaData; f++) {
                saving[f] = f;
            }

            //Features
            for (int f = 0; f < inputList.length; f++) {
                reordering[f] = inputList[f] + numMetaData;
                saving[f + numMetaData] = inputList[f] + numMetaData;
            }

            //The actual "class" output
            reordering[reordering.length - 1] = numMetaData + getNumInputs() + i;
            saving[saving.length - 1] = numMetaData + getNumInputs() + i;

            r.setAttributeIndicesArray(reordering);
            r.setInputFormat(dummyInstances);

            s.setAttributeIndicesArray(saving);
            s.setInputFormat(dummyInstances);

            trainingFilters[i] = r;
            runningFilters[i] = Reorder.makeCopy(r);
            inputSavingFilters[i] = s;
        }
    }
    
    private void updateFiltersForNewOutput(int which) {
    
        Reorder r = new Reorder();
        Reorder s = new Reorder();

        int[] inputList = inputListsForOutputsTraining.get(which);
        int[] reordering = new int[inputList.length + 1];
        int[] saving = new int[numMetaData + inputList.length + 1];

        //Metadata
        for (int f = 0; f < numMetaData; f++) {
            saving[f] = f;
        }

        //Features
        for (int f = 0; f < inputList.length; f++) {
            reordering[f] = inputList[f] + numMetaData;
            saving[f + numMetaData] = inputList[f] + numMetaData;
        }

        //The actual "class" output
        reordering[reordering.length - 1] = numMetaData + getNumInputs() + which;
        saving[saving.length - 1] = numMetaData + getNumInputs() + which;

        try {
            r.setAttributeIndicesArray(reordering);
            r.setInputFormat(dummyInstances);
            s.setAttributeIndicesArray(saving);
            s.setInputFormat(dummyInstances);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Encountered exception setting filters");
        }

        
        trainingFilters = insertIntoArray(r, trainingFilters, which);
        try {
            runningFilters = insertIntoArray(Reorder.makeCopy(r), runningFilters, which);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Encountered exception copying Reorder array");
        }
        inputSavingFilters = insertIntoArray(s, inputSavingFilters, which);
    }
    
    public void selectFeaturesAutomatically()
    {
        if(featureManager.isAllFeaturesDirty())
        {
            updateAllFeaturesInstances();
            featureManager.didRecalculateAllFeatures();
        }
        Instances data = allFeaturesInstances;
        
        WrapperSelector wrapperSelector = new WrapperSelector();
        selectedFeatureNames = new String[numOutputs][];
        for(int outputIndex = 0; outputIndex < numOutputs; outputIndex++)
        {
            Path path = w.getSupervisedLearningManager().getPaths().get(outputIndex);
            Classifier c = path.getModelBuilder().getClassifier();
            wrapperSelector.classifier = c;
            int[] indices = wrapperSelector.getAttributeIndicesForInstances(data);
            selectedFeatureNames[outputIndex] = new String[indices.length];
            int ptr = 0;
            for(int attributeIndex:indices)
            {
                selectedFeatureNames[outputIndex][ptr] = featureManager.getAllFeaturesGroup().valueMap[attributeIndex];
                ptr++;
            }
            Instances selectedInstances = wrapperSelector.filterInstances(data, indices);
            if(outputIndex < featureInstances.size())
            {
               featureInstances.set(outputIndex, selectedInstances);
            }
            else
            {
               featureInstances.add(selectedInstances);
            }
            featureManager.didRecalculateFeatures(outputIndex); 
        }
    }
    
    private void updateFeatureInstances(int index)
    { 
        Instances newInstances = featureManager.getNewInstances(index);
        try{
            Instances filteredInputs = Filter.useFilter(inputInstances, trainingFilters[index]);
            for (int i = 0; i < filteredInputs.numInstances(); i++)
            {
                double[] input = filteredInputs.instance(i).toDoubleArray();
                double output = input[input.length-1];
                double[] justInput = new double[input.length-1];
                System.arraycopy(input, 0, justInput, 0, justInput.length);
                double[] features = featureManager.modifyInputsForOutput(justInput, index);
                double[] withOutput = new double[features.length + 1];
                withOutput[withOutput.length-1] = output;
                System.arraycopy(features, 0, withOutput, 0, features.length);
                Instance featureInstance = new Instance(1.0,withOutput);
                newInstances.add(featureInstance);
                newInstances.setClassIndex(withOutput.length - 1);
            }
            if(index < featureInstances.size())
            {
               featureInstances.set(index, newInstances);
            }
            else
            {
               featureInstances.add(newInstances);
            }
            featureManager.didRecalculateFeatures(index); 
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void updateAllFeaturesInstances()
    { 
        Instances newInstances = featureManager.getAllFeaturesNewInstances();
        try{
            Instances filteredInputs = Filter.useFilter(inputInstances, trainingFilters[0]);
            for (int i = 0; i < filteredInputs.numInstances(); i++)
            {
                double[] input = filteredInputs.instance(i).toDoubleArray();
                double output = input[input.length-1];
                double[] justInput = new double[input.length-1];
                System.arraycopy(input, 0, justInput, 0, justInput.length);
                double[] features = featureManager.modifyInputsForAllFeatures(justInput);
                double[] withOutput = new double[features.length + 1];
                withOutput[withOutput.length-1] = output;
                System.arraycopy(features, 0, withOutput, 0, features.length);
                Instance featureInstance = new Instance(1.0,withOutput);
                newInstances.add(featureInstance);
                newInstances.setClassIndex(withOutput.length - 1);
            }
            allFeaturesInstances = newInstances; 
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected List<Instances> getFeatureInstances()
    {
        return featureInstances;
    }
    
    protected Instances getAllFeaturesInstances()
    {
        return allFeaturesInstances;
    }

    public Instances getTrainingDataForOutput(int index) {
        try {
            if(featureManager.isDirty(index))
            {
                updateFeatureInstances(index);
            }
            Instances in = featureInstances.get(index);
            in.setClassIndex(in.numAttributes() - 1);
            in.deleteWithMissingClass();
            return in;
        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    } 
    
    //This will need to use *new* filters, not old ones
    public Instances getInputDataForOutput(int index, boolean includeMetadataFields) {
        Filter filter = includeMetadataFields ? inputSavingFilters[index]:trainingFilters[index];
        try {
            Instances in = Filter.useFilter(inputInstances, filter);
            in.setClassIndex(in.numAttributes() - 1);
            in.deleteWithMissingClass();
            return in;
        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //This will use old filters, not new ones.
    public Instance getClassifiableInstanceForOutput(double[] vals, int which) {
        
        double[] features = featureManager.modifyInputsForOutput(vals, which);
        double data[] = new double[features.length + 1];
        System.arraycopy(features, 0, data, 0, features.length);
           
        Instances instances = featureManager.getNewInstances(which);
        Instance featureInstance = new Instance(1.0, data);
        instances.add(featureInstance);
        instances.setClassIndex(data.length - 1);
        featureInstance = instances.firstInstance();
        //featureInstance.setClassMissing();
        return featureInstance;
    }

    //Could probably make this more efficient...
    public int getNumExamplesInRound(int round) {
        int num = 0;
        for (int i = 0; i < inputInstances.numInstances(); i++) {
            Instance in = inputInstances.instance(i);
            if (in.value(recordingRoundIndex) == round) {
                num++;
            }
        }
        return num;
    }

    //This will use old filters, not new ones.
    public Instance[] getClassifiableInstancesForAllOutputs(double[] vals) {

        double data[] = new double[numMetaData + getNumInputs() + numOutputs];

        System.arraycopy(vals, 0, data, numMetaData, vals.length);

        Instance[] is = new Instance[numOutputs];
        for (int i = 0; i < numOutputs; i++) {
            is[i] = new Instance(1.0, data);
            Instances tmp = new Instances(dummyInstances);
            tmp.add(is[i]);
            try {
                tmp = Filter.useFilter(tmp, runningFilters[i]);
                tmp.setClassIndex(tmp.numAttributes() - 1);
                is[i] = tmp.firstInstance();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Could not filter");
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            tmp.setClassIndex(tmp.numAttributes() - 1);
        }
        return is;
    }

    public int getNumExamples() {
        return inputInstances.numInstances();
    }

    public void deleteExample(int whichExample) {
        for (int j = 0; j < numOutputs; j++) {
            if (!inputInstances.instance(whichExample).isMissing(numMetaData + getNumInputs() + j)) {
                setNumExamplesPerOutput(j, getNumExamplesPerOutput(j) - 1);
            }
        }
        inputInstances.delete(whichExample);

    }

    public boolean deleteTrainingRound(int which) {
        List<Instance> deleted = new LinkedList<>();

        if (inputInstances.numInstances() > 0) {
            int r = which;
            for (int i = inputInstances.numInstances() - 1; i >= 0; i--) {
                if (inputInstances.instance(i).value(recordingRoundIndex) == r) {
                    for (int j = 0; j < numOutputs; j++) {
                        if (!inputInstances.instance(i).isMissing(numMetaData + getNumInputs() + j)) {
                            setNumExamplesPerOutput(j, getNumExamplesPerOutput(j) - 1);
                        }
                    }
                    deleted.add(inputInstances.instance(i));
                    inputInstances.delete(i);
                }
            }
            if (inputInstances.numInstances() == 0) {
                setHasInstances(false);
            }
            deletedTrainingRound = deleted.toArray(new Instance[0]);
            fireStateChanged();
            return true;
        } else {
            return false;
        }
    }

    public void reAddDeletedTrainingRound() {
        if (deletedTrainingRound != null) {
            for (Instance in : deletedTrainingRound) {
                for (int j = 0; j < numOutputs; j++) {
                    if (!in.isMissing(numMetaData + getNumInputs() + j)) {
                        setNumExamplesPerOutput(j, getNumExamplesPerOutput(j) + 1);
                    }
                }

                in.setDataset(inputInstances);
                inputInstances.add(in);
                setHasInstances(true);
                fireStateChanged();
            }

            //Could get interesting behavior if we allow multiple re-adds; don't do this now.
            deletedTrainingRound = null;
        }
    }

    public int getNumDeletedTrainingRound() {
        if (deletedTrainingRound == null) {
            return 0;
        } else {
            return deletedTrainingRound.length;
        }

    }

    public void deleteAll() {
        setHasInstances(false);
        inputInstances.delete();
        for (int i = 0; i < numOutputs; i++) {
            setNumExamplesPerOutput(i, 0);
        }
        KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.SUPERVISED_DELETE_ALL_EXAMPLES);
        fireStateChanged();
    }

    //TODO: implement Import as arff too... (make up ID, timestamp, metadata)
    //TODO: Also ADD instances from arff
    public void setOutputValue(int index, int whichOutput, double val) {
        Instance i = inputInstances.instance(index);
        if (i == null) {
            return;
        }

        boolean changesNumberOfInstances = i.isMissing(numMetaData + getNumInputs() + whichOutput);

        if (isDiscrete[whichOutput]) {
            int v = (int) val;
            Attribute a = i.attribute(numMetaData + getNumInputs() + whichOutput);
            if (a.isNominal() && v >= 0 && v <= numClasses[whichOutput]) {
                i.setValue(numMetaData + getNumInputs() + whichOutput, v);
            } else {
                logger.log(Level.SEVERE, "Attribute value out of range");
            }
        } else {
            //TODO insert error checking / range limiting for this version!
            i.setValue(numMetaData + getNumInputs() + whichOutput, val);
        }
        if (changesNumberOfInstances) {
            setNumExamplesPerOutput(whichOutput, getNumExamplesPerOutput(whichOutput) + 1);
        }
    }

    public void setOutputMissing(int index, int outputNum) {
        Instance i = inputInstances.instance(index);
        if (!i.isMissing(numMetaData + getNumInputs() + outputNum)) {
            i.setMissing(numMetaData + getNumInputs() + outputNum);
            setNumExamplesPerOutput(outputNum, getNumExamplesPerOutput(outputNum) - 1);
        }
    }

    public void setOutputMissingForAll(int outputNum) {
        for (int i = 0; i < inputInstances.numInstances(); i++) {
            Instance in = inputInstances.instance(i);
            in.setMissing(numMetaData + getNumInputs() + outputNum);
        }
        setNumExamplesPerOutput(outputNum, 0);
    }

    public boolean isOutputMissing(int index, int outputNum) {
        Instance i = inputInstances.instance(index);
        return (i.isMissing(numMetaData + getNumInputs() + outputNum));
    }

    public void setInputValue(int index, int whichInput, double val) {
        Instance i = inputInstances.instance(index);
        if (i != null) {
            i.setValue(numMetaData + whichInput, val);
        } //else TODO ?
    }

    public double getOutputValue(int index, int whichOutput) {
        Instance i = inputInstances.instance(index);
        if (i == null || i.numAttributes() < (getNumInputs() + numMetaData + whichOutput)) {
            return Double.NaN;
        }
        if (i.isMissing(numMetaData + getNumInputs() + whichOutput)) {
            return Double.NaN;
        }
        return i.value(numMetaData + getNumInputs() + whichOutput);
    }

    public double getInputValue(int index, int whichInput) {
        Instance i = inputInstances.instance(index);
        if (i == null || i.numAttributes() < (whichInput + numMetaData)) {
            return Double.NaN;
        }
        return i.value(whichInput + numMetaData);
    }

    public int getIndexForID(int id) {
        for (int i = 0; i < inputInstances.numInstances(); i++) {
            int thisId = getID(i);
            if (thisId == id) {
                return i;
            }
        }
        return -1;
    }

    public int getID(int index) {
        if (index >= 0 && index < inputInstances.numInstances()) {
            Instance in = inputInstances.instance(index);
            if (in != null) {
                return (int) in.value(idIndex);
            }
        }
        return 0;
    }

    /**
     * Set the value of hasInstances
     *
     * @param hasInstances new value of hasInstances
     */
    private void setHasInstances(boolean hasInstances) {
        boolean oldHasInstances = this.hasInstances;
        this.hasInstances = hasInstances;
        propertyChangeSupport.firePropertyChange(PROP_HASINSTANCES, oldHasInstances, hasInstances);
    }

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    private void fireStateChanged() {
        
        featureManager.setAllOutputsDirty();
        
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    public void writeInstancesToArff(File file) throws IOException {
        ArffSaver saver = new ArffSaver();
        Instances temp = new Instances(inputInstances);
        saver.setInstances(temp);
        saver.setFile(file);
        saver.writeBatch();
    }

    public static String dateDoubleToString(double d) { //TODO: test!
        Date date;
        try {

            String s = decimalFormat.format(d);
            date = dateFormat.parse(s);
            return prettyDateFormat.format(date);

        } catch (ParseException ex) {
            logger.log(Level.WARNING, "Bad date: {0}", ex.getMessage());
            return "";
        }
    }

    @Override
    public String toString() {
        return inputInstances.toString();
    }

    public String getTimestampAsString(int index) {
        if (index >= 0 && index < inputInstances.numInstances()) {
            Instance in = inputInstances.instance(index);
            if (in != null) {
                return in.attribute(timestampIndex).formatDate(in.value(timestampIndex));
            }
        }
        return "error";
    }

    public int getRecordingRound(int index) {
        if (index >= 0 && index < inputInstances.numInstances()) {
            Instance in = inputInstances.instance(index);
            if (in != null) {
                return (int) in.value(recordingRoundIndex);
            }
        }
        return -1;
    }

    public boolean isProposedOutputLegal(double value, int whichOutput) {
        OSCOutput o = outputGroup.getOutput(whichOutput);
        if (o != null) {
            return o.isLegalTrainingValue(value);
        } else {
            logger.log(Level.WARNING, "Attempted to check invalid output index {0}", whichOutput);
            return false;
        }
    }

    public void showViewer() {
        KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.SUPERVISED_DATA_VIEWED);
        if (viewer != null) {
            viewer.toFront();
            return;
        }

        viewer = new DatasetViewer(this, w);
        viewer.setVisible(true);
        viewer.toFront();
        viewer.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                viewer = null;
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

    }
    
    //Replace the run filters with the training filters
    //(call when this model has been trained, and should be run with same inputs)
    public void useTrainingInputSelectionForRunning(int whichModel) {
        try {
            runningFilters[whichModel] = Reorder.makeCopy(trainingFilters[whichModel]);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private int[] findMissingInputsList(int[] selectedInputIndices) {
        boolean[] isMissing = new boolean[getNumInputs()];
        for (int i = 0; i < isMissing.length; i++) {
            isMissing[i] = true;
        }
        for (int i = 0; i < selectedInputIndices.length; i++) {
            isMissing[selectedInputIndices[i]] = false;
        }
        
        int[] missingInputs = new int[getNumInputs() - selectedInputIndices.length];
        int next = 0;
        for (int i = 0; i < getNumInputs(); i++) {
            if (isMissing[i]) {
                missingInputs[next++] = i;
            }
        }
        return missingInputs;
    }
    
    //Instances are formatted for this path
    //Inputs may need to be reordered
    //Some inputs from this project may not be present
    public void addLoadedDataForPath(Instances loadedInstances, int[] selectedInputIndices, int pathNum, int recordingRound) {
        if (loadedInstances.numInstances() == 0) {
            return;
        }
        
        int numInstances = loadedInstances.numInstances();
        int[] missingInputs = findMissingInputsList(selectedInputIndices);
        
        for (int i = 0; i < numInstances; i++) {
            Instance instance = loadedInstances.instance(i);
            int thisId = nextID;
            nextID++;
            double myVals[] = new double[numMetaData + getNumInputs() + numOutputs];
            myVals[idIndex] = thisId;
            myVals[recordingRoundIndex] = recordingRound;
            
            //Copy available feature values from loaded instance
            for (int j = 0; j < selectedInputIndices.length; j++) {
                myVals[numMetaData + selectedInputIndices[j]] = instance.value(j + numMetaData);
            }
            
            //Copy output value from loaded instance
            myVals[numMetaData + getNumInputs() + pathNum] = instance.value(instance.numAttributes()-1);
            setNumExamplesPerOutput(pathNum, getNumExamplesPerOutput(pathNum) + 1);
            
            //Create new instance with these values
            Instance newInstance = new Instance(1.0, myVals);
 
            //Use same timestamp as original data
            newInstance.setValue(timestampIndex, instance.value(timestampIndex));
            
            //Set missing inputs
            for (int j = 0; j < missingInputs.length; j++) {
                newInstance.setMissing(numMetaData + missingInputs[j]);
            } 
            
            //Set missing outputs
            for (int j = 0; j < numOutputs; j++) {
                if (j != pathNum) {
                    newInstance.setMissing(numMetaData + getNumInputs() + j);
                }
            }
        
            newInstance.setDataset(inputInstances);
            inputInstances.add(newInstance);
        }
        setHasInstances(true);
        fireStateChanged();

        //TODO: need to update output counts? Update id?
    }

    public void newOutputAdded(int which) {
        outputGroup = w.getOutputManager().getOutputGroup();
        numExamplesPerOutput = insertIntoArray(0, numExamplesPerOutput, which);
        outputNames = outputGroup.getOutputNames();
        numOutputs = outputGroup.getNumOutputs();
        updateOutputDataForNewOutput(which);
        updateInputListsForNewOutput(which);
        updateInstancesForNewOutput(which);
        updateFiltersForNewOutput(which);
        setNumExamplesPerOutput(which, 0);
        fireStateChanged();
    }

    private int[] insertIntoArray(int newValue, int[] existingArray, int which) {
        int[] newArray = new int[existingArray.length+1];
        System.arraycopy(existingArray, 0, newArray, 0, which);
        newArray[which] = newValue;
        for (int i = which+1; i <= existingArray.length; i++) {
            newArray[i] = existingArray[i-1];
        }
        return newArray;
    }
    
    private boolean[] insertIntoArray(boolean newValue, boolean[] existingArray, int which) {
        boolean[] newArray = new boolean[existingArray.length+1];
        System.arraycopy(existingArray, 0, newArray, 0, which);
        newArray[which] = newValue;
        for (int i = which+1; i <= existingArray.length; i++) {
            newArray[i] = existingArray[i-1];
        }
        return newArray;
    }
    
    private Filter[] insertIntoArray(Filter newValue, Filter[] existingArray, int which) {
        Filter[] newArray = new Filter[existingArray.length+1];
        System.arraycopy(existingArray, 0, newArray, 0, which);
        newArray[which] = newValue;
        for (int i = which+1; i <= existingArray.length; i++) {
            newArray[i] = existingArray[i-1];
        }
        return newArray;
    }

    void addToTraining(double[] inputs, double[] outputs, boolean[] inputMask, boolean[] outputMask, int recordingRound) {
        int thisId = nextID;
        nextID++;

        double myVals[] = new double[numMetaData + getNumInputs() + numOutputs];
        myVals[idIndex] = thisId;
        myVals[recordingRoundIndex] = recordingRound;

        Date now = new Date();

        String pretty = prettyDateFormat.format(now);
        try {
            myVals[timestampIndex] = inputInstances.attribute(timestampIndex).parseDate(pretty);
        } catch (ParseException ex) {
            myVals[timestampIndex] = 0;
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.arraycopy(inputs, 0, myVals, numMetaData, inputs.length); //TODO DOUBLECHECK
        System.arraycopy(outputs, 0, myVals, numMetaData + getNumInputs(), outputs.length);

        Instance in = new Instance(1.0, myVals);
        for (int i = 0; i < inputMask.length; i++) {
            if (!inputMask[i]) {
                in.setMissing(numMetaData + i);
            }
        }
        
        for (int i = 0; i < outputMask.length; i++) {
            if (!outputMask[i]) {
                in.setMissing(numMetaData + getNumInputs() + i);
            } else {
                setNumExamplesPerOutput(i, getNumExamplesPerOutput(i) + 1);
            }
        }
        in.setDataset(inputInstances);
        inputInstances.add(in);
        setHasInstances(true);
        fireStateChanged();
    }
    
}
