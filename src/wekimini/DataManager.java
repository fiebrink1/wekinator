/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import wekimini.gui.DatasetViewer;
import wekimini.osc.OSCClassificationOutput;
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
    private Filter[] outputFilters;
    private boolean isInitialized = false;
    private OSCOutputGroup outputGroup;
    private String[] inputNames;
    private String[] outputNames;
    private int numOutputs = 0;
    private int numInputs = 0;
    private List<int[]> inputListsForOutputs;
    private Instances allInstances = null;
    private int nextID = 1;

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

    public void setInputIndicesForOutput(int[] indices, int outputIndex) {
        //TODO: adjust filters
        int[] myIndices = new int[indices.length];
        System.arraycopy(indices, 0, myIndices, 0, indices.length);
        inputListsForOutputs.set(outputIndex, myIndices);
    }

    public void addToTraining(double[] inputs, double[] outputs, boolean[] recordingMask, int recordingRound) {

        int thisId = nextID;
        nextID++;

        double myVals[] = new double[numMetaData + numInputs + numOutputs];
        myVals[idIndex] = thisId;
        myVals[recordingRoundIndex] = recordingRound;

        Date now = new Date();
        //myVals[timestampIndex] = Double.parseDouble(dateFormat.format(now)); //Error: This gives us scientific notation!

        String pretty = prettyDateFormat.format(now);
        try {
            myVals[timestampIndex] = allInstances.attribute(timestampIndex).parseDate(pretty);
            //myVals[timestampIndex] =
        } catch (ParseException ex) {
            myVals[timestampIndex] = 0;
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*for (int i = 0; i < numInputs; i++) {
         myVals[numMetaData + i] = featureVals[i];
         } */
        System.arraycopy(inputs, 0, myVals, numMetaData, inputs.length); //TODO DOUBLECHECK


        /*for (int i = 0; i < numParams; i++) {
         if (isParamDiscrete[i] && (paramVals[i] < 0 || paramVals[i] >= numParamValues[i])) {
         throw new IllegalArgumentException("Invalid value for this discrete parameter");
         }

         myVals[numMetaData + numFeatures + i] = paramVals[i];
         } */
        System.arraycopy(outputs, 0, myVals, numMetaData + numInputs, outputs.length);

        Instance in = new Instance(1.0, myVals);
        for (int i = 0; i < recordingMask.length; i++) {
            if (!recordingMask[i]) {
                in.setMissing(numMetaData + numInputs + i);
            } else {
                setNumExamplesPerOutput(i, getNumExamplesPerOutput(i) + 1);
                // outputInstanceCounts[i]++;
            }
        }
        in.setDataset(allInstances);
        allInstances.add(in);
        setHasInstances(true);
        fireStateChanged();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getNumInputs() {
        return numInputs;
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
        allInstances = new Instances(data);
        updateOutputCounts();
        updateNextId();
    }

    public int getMaxRecordingRound() {
       //Attribute a = allInstances.attribute(recordingRoundIndex);
       AttributeStats a = allInstances.attributeStats(recordingRoundIndex);
       return (int) a.numericStats.max;  
    }
    
    private void updateNextId() {
        AttributeStats a = allInstances.attributeStats(idIndex);
        nextID = (int)a.numericStats.max + 1;
    }
    
    private void updateOutputCounts() {
        int[] examplesSum = new int[numOutputs];

        if (allInstances.numInstances() > 0) {
            for (int i = allInstances.numInstances() - 1; i >= 0; i--) {
                for (int j = 0; j < numOutputs; j++) {
                    if (!allInstances.instance(i).isMissing(numMetaData + numInputs + j)) {
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
        numInputs = inputNames.length;
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
            System.out.println("Error: Couldn't set up filters correctly");
            System.exit(0);
        }
        setInitialized(true);
    }

    private void initializeInstances() {
        //Set up instances
        FastVector ff = new FastVector(numInputs + numOutputs + numMetaData); //Include ID, timestamp, training round
        //add ID, timestamp, and training round #
        ff.addElement(new Attribute("ID"));
        // ff.addElement(new Attribute("Timestamp")); //yyMMddHHmmss format; stored as String

        ff.addElement(new Attribute("Time", prettyDateFormatString));
        ff.addElement(new Attribute("Training round"));
        //new Attribute

        //Add inputs
        for (int i = 0; i < numInputs; i++) {
            ff.addElement(new Attribute(this.inputNames[i]));
        }

        //Add outputs
        for (int i = 0; i < numOutputs; i++) {
            if (isDiscrete[i]) {
                //Create fastVector w/ possible
                FastVector classes = new FastVector(numOutputs);
                for (int val = 0; val < numClasses[i]; val++) {
                    System.out.println("Adding legal value " + (new Integer(val + 1)).toString());
                    classes.addElement((new Integer(val + 1)).toString()); //Values 1 to numClasses
                }
                ff.addElement(new Attribute(outputNames[i], classes));

            } else {
                ff.addElement(new Attribute(outputNames[i]));
            }
        }

        allInstances = new Instances("dataset", ff, 100);

        //Set up dummy instances to reflect state of actual instances
        dummyInstances = new Instances(allInstances);
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
    }

    private void initializeInputLists() {
        inputListsForOutputs = new ArrayList<>(numOutputs);
        int inputList[] = new int[numInputs];
        for (int i = 0; i < numInputs; i++) {
            inputList[i] = i;
        }
        for (int i = 0; i < numOutputs; i++) {
            inputListsForOutputs.add(inputList);
        }
    }

    private void setupFilters() throws Exception {
        outputFilters = new Reorder[numOutputs];
        for (int i = 0; i < numOutputs; i++) {
            Reorder r = new Reorder();
            int[] inputList = inputListsForOutputs.get(i);
            int[] reordering = new int[inputList.length + 1];

            //Features
            for (int f = 0; f < inputList.length; f++) {
                reordering[f] = inputList[f] + numMetaData;
            }

            //The actual "class" output
            reordering[reordering.length - 1] = numMetaData + numInputs + i;
            r.setAttributeIndicesArray(reordering);
            r.setInputFormat(dummyInstances);

            outputFilters[i] = r;
        }
    }

    public Instances getTrainingDataForOutput(int which) {
        try {
            Instances in = Filter.useFilter(allInstances, outputFilters[which]);
            in.setClassIndex(in.numAttributes() - 1);
            in.deleteWithMissingClass();
            return in;
        } catch (Exception ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //Untested
    public Instance getClassifiableInstanceForOutput(double[] vals, int which) {
        double data[] = new double[numMetaData + numInputs + numOutputs];
        System.arraycopy(vals, 0, data, numMetaData, vals.length);
        /* for (int i = 0; i < numFeatures; i++) {
         data[numMetaData + i] = d[i];
         } */

        Instance instance = new Instance(1.0, data);
        Instances tmp = new Instances(dummyInstances);
        tmp.add(instance);
        try {
            tmp = Filter.useFilter(tmp, outputFilters[which]);
            tmp.setClassIndex(tmp.numAttributes() - 1);
            instance = tmp.firstInstance();
        } catch (Exception ex) {
            System.out.println("Error: could not filter");
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        tmp.setClassIndex(tmp.numAttributes() - 1);

        return instance;
    }

    //Could probably make this more efficient...
    public int getNumExamplesInRound(int round) {
        int num = 0;
        for (int i = 0; i < allInstances.numInstances(); i++) {
            Instance in = allInstances.instance(i);
            if (in.value(recordingRoundIndex) == round) {
                num++;
            }
        }
        return num;
    }

    public Instance[] getClassifiableInstancesForAllOutputs(double[] vals) {

        double data[] = new double[numMetaData + numInputs + numOutputs];

        System.arraycopy(vals, 0, data, numMetaData, vals.length);
        /* for (int i = 0; i < numFeatures; i++) {
         data[numMetaData + i] = d[i];
         } */

        Instance[] is = new Instance[numOutputs];
        for (int i = 0; i < numOutputs; i++) {
            is[i] = new Instance(1.0, data);
            Instances tmp = new Instances(dummyInstances);
            tmp.add(is[i]);
            try {
                tmp = Filter.useFilter(tmp, outputFilters[i]);
                tmp.setClassIndex(tmp.numAttributes() - 1);
                is[i] = tmp.firstInstance();
            } catch (Exception ex) {
                System.out.println("Error: could not filter");
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            tmp.setClassIndex(tmp.numAttributes() - 1);
        }
        return is;
    }

    public int getNumExamples() {
        return allInstances.numInstances();
    }

    /*public int getNumInstancesForOutput(int which) {
     return outputInstanceCounts[which];
     } */
    public void deleteExample(int whichExample) {
        for (int j = 0; j < numOutputs; j++) {
            if (!allInstances.instance(whichExample).isMissing(numMetaData + numInputs + j)) {
                setNumExamplesPerOutput(j, getNumExamplesPerOutput(j) - 1);
                //soutputInstanceCounts[j]--; //TODO: Test this
            }
        }
        allInstances.delete(whichExample);

    }

    public boolean deleteTrainingRound(int which) {
        List<Instance> deleted = new LinkedList<>();

        if (allInstances.numInstances() > 0) {
            int r = which;
            for (int i = allInstances.numInstances() - 1; i >= 0; i--) {
                if (allInstances.instance(i).value(recordingRoundIndex) == r) {
                    for (int j = 0; j < numOutputs; j++) {
                        if (!allInstances.instance(i).isMissing(numMetaData + numInputs + j)) {
                            setNumExamplesPerOutput(j, getNumExamplesPerOutput(j) - 1);
                            //soutputInstanceCounts[j]--; //TODO: Test this
                        }
                    }
                    deleted.add(allInstances.instance(i));
                    allInstances.delete(i);
                }
            }
            if (allInstances.numInstances() == 0) {
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
                    if (!in.isMissing(numMetaData + numInputs + j)) {
                        setNumExamplesPerOutput(j, getNumExamplesPerOutput(j) + 1);
                    }
                }

                in.setDataset(allInstances);
                allInstances.add(in);
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
        allInstances.delete();
        for (int i = 0; i < numOutputs; i++) {
            setNumExamplesPerOutput(i, 0);
            // outputInstanceCounts[i] = 0;
        }
        fireStateChanged();
    }

    //TODO: implement Import as arff too... (make up ID, timestamp, metadata)
    //TODO: Also ADD instances from arff
    public void setOutputValue(int index, int whichOutput, double val) {
        Instance i = allInstances.instance(index);
        if (i == null) {
            return;
        }

        boolean changesNumberOfInstances = i.isMissing(numMetaData + numInputs + whichOutput);

        if (isDiscrete[whichOutput]) {
            int v = (int) val;
            Attribute a = i.attribute(numMetaData + numInputs + whichOutput);
            if (a.isNominal() && v >= 0 && v <= numClasses[whichOutput]) {
                i.setValue(numMetaData + numInputs + whichOutput, v);
            } else {
                System.out.println("error: attribute value out of range");
                //TODO: CHeck this
            }
        } else {
            //TODO insert error checking / range limiting for this version!
            i.setValue(numMetaData + numInputs + whichOutput, val);
        }
        if (changesNumberOfInstances) {
            setNumExamplesPerOutput(whichOutput, getNumExamplesPerOutput(whichOutput) + 1);
        }
    }

    public void setOutputMissing(int index, int outputNum) {
        //if (paramNum >= 0 && paramNum < numParams) {
        Instance i = allInstances.instance(index);
        if (!i.isMissing(numMetaData + numInputs + outputNum)) {
            i.setMissing(numMetaData + numInputs + outputNum);
            setNumExamplesPerOutput(outputNum, getNumExamplesPerOutput(outputNum) - 1);
        }

        //Need to recompute numOutputs!
        //}
    }

    public void setOutputMissingForAll(int outputNum) {
        for (int i = 0; i < allInstances.numInstances(); i++) {
            Instance in = allInstances.instance(i);
            in.setMissing(numMetaData + numInputs + outputNum);
        }
        setNumExamplesPerOutput(outputNum, 0);
    }

    public boolean isOutputMissing(int index, int outputNum) {
        Instance i = allInstances.instance(index);
        return (i.isMissing(numMetaData + numInputs + outputNum));
    }

    public void setInputValue(int index, int whichInput, double val) {
        /*  if (whichInput < 0 || whichInput >= numInputs) {
         throw new IllegalArgumentException("Invalid input number in setInputValue");
         } */
        Instance i = allInstances.instance(index);
        if (i != null) {
            i.setValue(numMetaData + whichInput, val);
        } //else TODO ?
    }

    public double getOutputValue(int index, int whichOutput) {
        Instance i = allInstances.instance(index);
        if (i == null || i.numAttributes() < (numInputs + numMetaData + whichOutput)) {
            return Double.NaN;
        }
        if (i.isMissing(numMetaData + numInputs + whichOutput)) {
            return Double.NaN;
        }
        if (i.attribute(numMetaData + numInputs + whichOutput).isNumeric()) {
            return i.value(numMetaData + numInputs + whichOutput);
        } else {
            //What we need to do if we allow classes that don't start at 1:
            //return Double.parseDouble(i.attribute(numMetaData + numInputs + whichOutput).value((int)i.value(numMetaData + numInputs + whichOutput)));
            return i.value(numMetaData + numInputs + whichOutput) + 1;
        }
    }

    public double getInputValue(int index, int whichInput) {
        Instance i = allInstances.instance(index);
        if (i == null || i.numAttributes() < (whichInput + numMetaData)) {
            return Double.NaN;
        }
        return i.value(whichInput + numMetaData);
    }

    public int getIndexForID(int id) {
        for (int i = 0; i < allInstances.numInstances(); i++) {
            int thisId = getID(i);
            if (thisId == id) {
                return i;
            }
        }
        return -1;
    }

    public int getID(int index) {
        //   if (idMap.containsKey(index)) {
        if (index >= 0 && index < allInstances.numInstances()) {

            //Instance i = idMap.get(index);
            Instance in = allInstances.instance(index);
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
        Instances temp = new Instances(allInstances);
        //Attribute niceDate = new Attribute("Time", nullF); 
      /*  Attribute niceDate = new Attribute("Time", prettyDateFormatString);
         temp.insertAttributeAt(niceDate, timestampIndex);

         Date d;
         for (int i = 0; i < temp.numInstances(); i++) {
         double ddate = temp.instance(i).value(timestampIndex+1);
         String niceDecimal = decimalFormat.format(ddate);
         try {
         d = dateFormat.parse(niceDecimal);
         String pretty = prettyDateFormat.format(d);
         temp.instance(i).setValue(timestampIndex, niceDate.parseDate(pretty));

         } catch (ParseException ex) {
         temp.instance(i).setValue(timestampIndex, 0);
         Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
         } 
           
           
         }
        
         temp.deleteAttributeAt(timestampIndex+1);
         */

        /*for (int i = 0; i < numFeatures; i++) {
         temp.renameAttribute(i, featureNames[i]);
         }*/
        saver.setInstances(temp);
        saver.setFile(file);
        saver.writeBatch();
    }

    public static String dateDoubleToString(double d) { //TODO: test!
        Date date;
        try {
            /* String ds = Double.toString(d); */ //hack
            /*String ds = "" + (int) d;

             while (ds.length() < 9) {
             ds = "0" + ds;
             } */

            String s = decimalFormat.format(d);
            date = dateFormat.parse(s);
            //date = dateFormat.parse(ds);
            return prettyDateFormat.format(date);

        } catch (ParseException ex) {
            logger.log(Level.WARNING, "Bad date: {0}", ex.getMessage());
            return "";
        }
    }

    @Override
    public String toString() {
        return allInstances.toString();
    }

    public String getTimestampAsString(int index) {
        if (index >= 0 && index < allInstances.numInstances()) {
            Instance in = allInstances.instance(index);
            if (in != null) {
                return in.attribute(timestampIndex).formatDate(in.value(timestampIndex));
                // return in.value(timestampIndex);
            }
        }
        return "error";
    }

    public int getRecordingRound(int index) {
        if (index >= 0 && index < allInstances.numInstances()) {
            Instance in = allInstances.instance(index);
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
        /*if (viewer == null) {
         viewer = new DatasetViewer(this);
         }
         viewer.setVisible(true);
         viewer.toFront();
         */
        if (viewer != null) {
            viewer.toFront();
            return;
        }

        viewer = new DatasetViewer(this);
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
}