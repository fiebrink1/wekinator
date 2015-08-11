/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning.dtw;

import com.timeseries.TimeSeries;
import com.timeseries.TimeSeriesPoint;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.ConnectsInputsToOutputs;
import wekimini.DtwLearningManager;
import wekimini.Wekinator;

/**
 *
 * @author rebecca
 */
public class DtwData {

    private final transient List<DtwDataListener> dataListeners = new LinkedList<>();
    private transient long currentTime = 0;
    private transient TimeSeries currentTimeSeries;
    private final int numGestures;
    private transient int currentClass;
    private transient final Wekinator w;
    private final ArrayList<LinkedList<DtwExample>> allExamples;
    private final HashMap<Integer, LinkedList<DtwExample>> exampleListForIds;
    private final HashMap<Integer, DtwExample> examplesForIds;

    private int minSizeInExamples = Integer.MAX_VALUE;
    private int maxSizeInExamples = 0;
    private int minSizeInDownsampledExamples = Integer.MAX_VALUE;
    private int maxSizeInDownsampledExamples = 0;

    private int maxLengthToRetainDuringRun = Integer.MAX_VALUE;
    private static final Logger logger = Logger.getLogger(DtwData.class.getName());
    //private int numTotalExamples = 0;

    private int numTotalExamples = 0;
    private final int numInputs;
    private int numActiveInputs;

    public static final String PROP_NUMTOTALEXAMPLES = "numTotalExamples";

    private int downsampleFactor = 1;
    private int downsampleToLength = 10;
    //private boolean isDownsampling = false;
    private transient int downsampleCounter = 0;
    private DtwSettings.DownsamplePolicy downsamplePolicy = DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING;

    private DtwExample lastExample = null;
    private int lastExampleCategory = 0;

    private DtwExample lastDeletedExample = null;
    private int lastDeletedExampleCategory = 0;

    public int getDownsampleFactor() {
        return downsampleFactor;
    }

    public void setDownsampleByConstantFactor(int downsampleFactor) {
        downsamplePolicy = DtwSettings.DownsamplePolicy.DOWNSAMPLE_BY_CONSTANT_FACTOR;
        downsampleCounter = 0;
        this.downsampleFactor = downsampleFactor;
        //if (this.downsampleFactor != downsampleFactor) { // Might be inefficient, but otherwise danger is of not having downsampled newer training examples
        //    this.downsampleFactor = downsampleFactor;
        redoDownsampleTraining();
        updateMaxDownsampledSize();
        updateMinDownsampledSize();
        //}
    }

    public void setNoDownsample() {
        downsamplePolicy = DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING;
    }

    public void setDownsampleToMaxLength(int len) {
        downsamplePolicy = DtwSettings.DownsamplePolicy.DOWNSAMPLE_TO_MAX_LENGTH;
        downsampleCounter = 0;
        downsampleToLength = len;
        downsampleFactor = computeDownsampleFactorForMaxLength();
        redoDownsampleTraining();
        updateMaxDownsampledSize();
        updateMinDownsampledSize();
    }

    private int computeDownsampleFactorForMaxLength() {
        int f = maxSizeInExamples / downsampleToLength;
        if (f == 0) {
            f = 1;
        }
        return f;
    }

    public DtwSettings.DownsamplePolicy getDownsamplePolicy() {
        return downsamplePolicy;
    }

    //Requires downsampleFactor is reset
    private void redoDownsampleTraining() {
        //TODO XXX make sure doesn't break if done while running/training; may have to pause running/training
        for (List<DtwExample> exampleList : allExamples) {
            for (DtwExample ex : exampleList) {
                ex.setDownsampledTimeSeries(downSample(downsampleFactor, ex));
            }
        }
    }

    private static TimeSeries downSample(int downsampleFactor, DtwExample ex) {
        return downSample(downsampleFactor, ex.getTimeSeries());
    }

    private static TimeSeries downSample(int downsampleFactor, TimeSeries ts) {
        TimeSeries downsampled = new TimeSeries(ts.numOfDimensions());

        for (int i = 0; i < ts.numOfPts(); i += downsampleFactor) {
            double t = ts.getTimeAtNthPoint(i);
            double[] vals = ts.getMeasurementVector(i);
            downsampled.addLast(t, new TimeSeriesPoint(vals));
        }

        return downsampled;
    }

    /**
     * Get the value of numTotalExamples
     *
     * @return the value of numTotalExamples
     */
    public int getNumTotalExamples() {
        return numTotalExamples;
    }

    /**
     * Set the value of numTotalExamples
     *
     * @param numTotalExamples new value of numTotalExamples
     */
    public void setNumTotalExamples(int numTotalExamples) {
        int oldNumTotalExamples = this.numTotalExamples;
        this.numTotalExamples = numTotalExamples;
        propertyChangeSupport.firePropertyChange(PROP_NUMTOTALEXAMPLES, oldNumTotalExamples, numTotalExamples);
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

    public DtwData(int numGestures, DtwLearningManager learningManager, Wekinator w) {
        this.numGestures = numGestures;
        this.w = w;
        this.numInputs = w.getInputManager().getNumInputs();
        this.numActiveInputs = numInputs; //XXX if learning manager has connection info here, need to use it now
        allExamples = new ArrayList<>();
        for (int i = 0; i < numGestures; i++) {
            allExamples.add(new LinkedList<DtwExample>());
        }
        exampleListForIds = new HashMap<>();
        examplesForIds = new HashMap<>();
        learningManager.addConnectionsListener(new ConnectsInputsToOutputs.InputOutputConnectionsListener() {

            @Override
            public void newConnectionMatrix(boolean[][] connections) {
                connectionsChanged();
            }
        });
    }

    private void connectionsChanged() {
        //XXX TODO feature selection
        //Change numActiveInputs
        //Continue putting all inputs into timeseires, but modify timeseires before distance calculation
        // (or change distance function)
    }

    public int getMaxLengthToRetainDuringRun() {
        return maxLengthToRetainDuringRun;
    }

    public void setMaxLengthToRetainDuringRun(int maxLengthToRetainDuringRun) {
        this.maxLengthToRetainDuringRun = maxLengthToRetainDuringRun;
    }

    public void delete(int id) {
        List<DtwExample> matchingList = exampleListForIds.get(id);
        if (matchingList == null) {
            logger.log(Level.WARNING, "ID {0} not found in exampleListForIds!", id);
            return;
        }
        DtwExample matchingExample = examplesForIds.get(id);
        if (matchingExample == null) {
            logger.log(Level.WARNING, "ID {0} not found in examplesForIds", id);
            return;
        }
        boolean removed = matchingList.remove(matchingExample);
        if (!removed) {
            logger.log(Level.WARNING, "ID {0} not found in examplesForIds", id);
            return;
        }

        if (matchingExample.getTimeSeries().size() == minSizeInExamples) {
            updateMinSize();
        }
        if (matchingExample.getTimeSeries().size() == maxSizeInExamples) {
            updateMaxSize();
        }
        if (matchingExample.getDownsampledTimeSeries().size() == minSizeInDownsampledExamples) {
            updateMinDownsampledSize();
        }
        boolean maxDownsampledUpdated = false;
        if (matchingExample.getDownsampledTimeSeries().size() == maxSizeInDownsampledExamples) {
            updateMaxDownsampledSize();
            maxDownsampledUpdated = true;
        }

        exampleListForIds.remove(id);
        examplesForIds.remove(id);

        int whichClass = allExamples.indexOf(matchingList);

        setNumTotalExamples(numTotalExamples - 1);

        if (maxDownsampledUpdated && downsamplePolicy == DtwSettings.DownsamplePolicy.DOWNSAMPLE_TO_MAX_LENGTH) {
            computeDownsampleFactorForMaxLength();
            redoDownsampleTraining();
        }

        notifyExamplesChangedListeners(whichClass, matchingList.size());
        notifyExampleDeletedListeners(whichClass);
    }

    public void deleteAll() {
        for (int i = 0; i < numGestures; i++) {
            allExamples.get(i).clear();
        }
        exampleListForIds.clear();
        examplesForIds.clear();
        setNumTotalExamples(0);
        
        minSizeInExamples = Integer.MAX_VALUE;
        minSizeInDownsampledExamples = Integer.MAX_VALUE;
        maxSizeInExamples = 0;
        maxSizeInDownsampledExamples = 0;
        setNumTotalExamples(0);
        for (int i = 0; i < numGestures; i++) {
            notifyExamplesChangedListeners(i, 0);
        }
        notifyAllExamplesDeletedListeners();
    }

    protected void startRecording(int currentClass) {
        currentTimeSeries = new TimeSeries(numActiveInputs);
        currentTime = 0;
        this.currentClass = currentClass;
    }

    protected void stopRecording() {
        addTrainingExample();
    }

    public void addTrainingVector(double[] d) {
        TimeSeriesPoint p = new TimeSeriesPoint(d);
        currentTimeSeries.addLast(currentTime, p);
        currentTime++;
    }

    public boolean addRunningVector(double[] d) {
        boolean added = false;
        if (downsamplePolicy == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING || downsampleCounter == 0) {

            TimeSeriesPoint p = new TimeSeriesPoint(d);
            currentTimeSeries.addLast(currentTime, p);
            added = true;
            while (currentTimeSeries.size() > maxLengthToRetainDuringRun) {
                currentTimeSeries.removeFirst();
            }
        }
        if (downsamplePolicy != DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING && (++downsampleCounter == downsampleFactor)) {
            downsampleCounter = 0;
        }

        currentTime++;
        return added;
    }

    public void addTrainingExample() {
        if (currentTimeSeries.size() == 0) {
            w.getStatusUpdateCenter().warn(this, "Could not add new example: length was 0");
            return;
        }
        int id = DtwExample.generateNextID();
        DtwExample ex;

        if (downsamplePolicy == DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING) {
            ex = new DtwExample(currentTimeSeries, id);
        } else {
            TimeSeries ds = downSample(downsampleFactor, currentTimeSeries);
            ex = new DtwExample(currentTimeSeries, ds, id);
        }
        addExample(ex, currentClass, id);
    }

    private void addExample(DtwExample ex, int gestureClass, int id) {
        LinkedList<DtwExample> list = allExamples.get(gestureClass);
        list.add(ex);
        if (currentTimeSeries.size() < minSizeInExamples) {
            minSizeInExamples = currentTimeSeries.size();
        }
        if (currentTimeSeries.size() > maxSizeInExamples) {
            maxSizeInExamples = currentTimeSeries.size();
            if (downsamplePolicy == DtwSettings.DownsamplePolicy.DOWNSAMPLE_TO_MAX_LENGTH) {
                downsampleFactor = computeDownsampleFactorForMaxLength();
                redoDownsampleTraining();
            }
        }
        if (ex.getDownsampledTimeSeries().size() < minSizeInDownsampledExamples) {
            minSizeInDownsampledExamples = ex.getDownsampledTimeSeries().size();
        }
        if (ex.getDownsampledTimeSeries().size() > maxSizeInDownsampledExamples) {
            maxSizeInDownsampledExamples = ex.getDownsampledTimeSeries().size();
        }

        exampleListForIds.put(id, list);
        examplesForIds.put(id, ex);

        lastExample = ex;
        lastExampleCategory = currentClass;

        setNumTotalExamples(numTotalExamples + 1);
        notifyExamplesChangedListeners(currentClass, list.size());
        notifyExampleAddedListeners(currentClass);
    }

    public int getNumExamplesForGesture(int gesture) {
        return allExamples.get(gesture).size();
    }

    public List<LinkedList<DtwExample>> getAllExamples() {
        return allExamples;
    }

    public List<DtwExample> getExamplesForGesture(int gesture) {
        return allExamples.get(gesture);
    }

    public void startRunning() {
        currentTime = 0;
        currentTimeSeries = new TimeSeries(numActiveInputs);
    }

    public void stopRunning() {

    }

    public void dumpExamplesForGesture(int whichGesture) {
        List<DtwExample> examples = allExamples.get(whichGesture);
        System.out.println(examples.size() + " EXAMPLES FOR GESTURE " + whichGesture + ":");
        int i = 0;
        for (DtwExample ex : examples) {
            System.out.println("   " + i + " - length: " + ex.getTimeSeries().numOfPts());
            System.out.println("    " + i++ + " downsampled length: " + ex.getDownsampledTimeSeries().numOfPts());
        }
    }

    public String getSummaryStringForGesture(int whichGesture) {
        List<DtwExample> examples = allExamples.get(whichGesture);
        StringBuilder sb = new StringBuilder();
        sb.append(examples.size()).append(" examples:\n");
        int i = 0;
        for (DtwExample ex : examples) {
            sb.append("Example ").append(i).append(": length=").append(ex.getTimeSeries().numOfPts());
            sb.append("\nDownsampled example: ").append(i++).append(": length=").append(ex.getDownsampledTimeSeries().numOfPts());
        }
        return sb.toString();
    }

    public void dumpAllExamples() {
        for (int i = 0; i < numGestures; i++) {
            dumpExamplesForGesture(i);
        }
    }

    public int getMinSizeInExamples() {
        return minSizeInExamples;
    }

    public int getMaxSizeInExamples() {
        return maxSizeInExamples;
    }

    public void addDataListener(DtwDataListener listener) {
        if (listener != null) {
            dataListeners.add(listener);
        }
    }

    public void removeDataListener(DtwDataListener listener) {
        if (listener != null) {
            dataListeners.remove(listener);
        }
    }

    private void updateMinSize() {
        int min = Integer.MAX_VALUE;
        for (List<DtwExample> list : allExamples) {
            for (DtwExample ex : list) {
                if (ex.getTimeSeries().size() < min) {
                    min = ex.getTimeSeries().size();
                }
            }
        }
        minSizeInExamples = min;
    }

    private void updateMinDownsampledSize() {
        int min = Integer.MAX_VALUE;
        for (List<DtwExample> list : allExamples) {
            for (DtwExample ex : list) {
                if (ex.getDownsampledTimeSeries().size() < min) {
                    min = ex.getDownsampledTimeSeries().size();
                }
            }
        }
        minSizeInDownsampledExamples = min;
    }

    private void updateMaxSize() {
        int max = 0;
        for (List<DtwExample> list : allExamples) {
            for (DtwExample ex : list) {
                if (ex.getTimeSeries().size() > max) {
                    max = ex.getTimeSeries().size();
                }
            }
        }
        maxSizeInExamples = max;
        if (downsamplePolicy == DtwSettings.DownsamplePolicy.DOWNSAMPLE_TO_MAX_LENGTH) {
            downsampleFactor = computeDownsampleFactorForMaxLength();
            redoDownsampleTraining();
        }
    }

    private void updateMaxDownsampledSize() {
        int max = 0;
        for (List<DtwExample> list : allExamples) {
            for (DtwExample ex : list) {
                if (ex.getDownsampledTimeSeries().size() > max) {
                    max = ex.getDownsampledTimeSeries().size();
                }
            }
        }
        maxSizeInDownsampledExamples = max;
    }

    //Finds time series between minSize and MaxSize in most recent set of examples
    public List<TimeSeries> getCandidateSeriesFromCurrentRun(int minSize, int maxSize, int hopSize) {
        List<TimeSeries> l = new LinkedList<>();
        //hop size = 1

        if (currentTimeSeries.size() < minSize) {
            // l.add(new TimeSeries(t)); //don't do anything
            //  System.out.println("T too small: " + t.size());
            return l;
        }

        int shortestStartPos = currentTimeSeries.size() - minSize;
        int longestStartPos;
        if (currentTimeSeries.size() > maxSize) {
            longestStartPos = currentTimeSeries.size() - maxSize;
        } else {
            longestStartPos = 0;
        }

        //TODO: Can we make this more efficent?
        for (int startPos = longestStartPos; startPos <= shortestStartPos; startPos = startPos + hopSize) {
            TimeSeries tt = new TimeSeries(currentTimeSeries.numOfDimensions());
            for (int i = 0; i < currentTimeSeries.size() - startPos; i++) {
                double[] next = currentTimeSeries.getMeasurementVector(startPos + i);
                tt.addLast(new Double(i), new TimeSeriesPoint(next));
            }
            l.add(tt);
        }
        return l;
    }

    public void notifyExampleAddedListeners(int whichClass) {
        for (DtwDataListener l : dataListeners) {
            l.exampleAdded(whichClass);
        }
    }

    public void notifyExampleDeletedListeners(int whichClass) {
        for (DtwDataListener l : dataListeners) {
            l.exampleDeleted(whichClass);
        }
    }

    public void notifyExamplesChangedListeners(int whichClass, int currentNumExamples) {
        for (DtwDataListener l : dataListeners) {
            l.numExamplesChanged(whichClass, currentNumExamples);
        }
    }

    public void notifyAllExamplesDeletedListeners() {
        for (DtwDataListener l : dataListeners) {
            l.allExamplesDeleted();
        }
    }

    public void printState() {
        for (int i = 0; i < numGestures; i++) {
            System.out.println("CLASS " + i + ": + "
                    + allExamples.get(i).size()
                    + "points:");
            for (DtwExample ts : allExamples.get(i)) {
                System.out.println(ts.getId() + ": " + ts.getTimeSeries());
            }
        }

        System.out.println("CURRENT Timeseries:");
        System.out.println(currentTimeSeries);
    }

    public void deleteExamplesForGesture(int gestureNum) {
        List<DtwExample> exs = getExamplesForGesture(gestureNum);
        int[] idsToDelete = new int[exs.size()];
        int i =0;
        for (DtwExample ex : exs) {
            idsToDelete[i++] = ex.getId();
        }
        for (i = 0; i < idsToDelete.length; i++) {
            delete(idsToDelete[i]);
        }
    }

    public void deleteMostRecentExample(int gestureNum) {
        DtwExample last = allExamples.get(gestureNum).getLast();
        if (last != null) {
            delete(last.getId());
        }
        
        /*DtwExample removed = allExamples.get(gestureNum).removeLast();
        if (removed != null) {
            notifyExampleDeletedListeners(gestureNum);
            notifyExamplesChangedListeners(gestureNum, allExamples.get(gestureNum).size());
        } */
    }

    public String getSummaryString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getNumTotalExamples()).append(" examples in ");
        sb.append(numGestures).append(" gesture categories\n");
        sb.append("Max example length is ").append(getMaxSizeInExamples());
        sb.append("\nMin example length is ").append(getMinSizeInExamples()).append("\n");

        for (int i = 0; i < numGestures; i++) {
            sb.append("Examples for gesture ").append(i).append(":\n");
            sb.append(getSummaryStringForGesture(i));
        }
        sb.append("\n");
        return sb.toString();
    }

    int getMinSizeInDownsampledExamples() {
        return minSizeInDownsampledExamples;
    }

    int getMaxSizeInDownsampledExamples() {
        return maxSizeInDownsampledExamples;
    }

    public void reAddLastExample() {
        if (lastDeletedExample != null) {
            int id = lastDeletedExample.getId();
            addExample(lastDeletedExample, lastDeletedExampleCategory, id);

            lastExample = lastDeletedExample;
            lastExampleCategory = lastDeletedExampleCategory;
            lastDeletedExample = null;
            lastDeletedExampleCategory = 0;
        }
    }

    public void deleteLastExample() {
        if (lastExample != null) {
            int id = lastExample.getId();
            delete(id);

            lastDeletedExample = lastExample;
            lastDeletedExampleCategory = lastExampleCategory;
            lastExample = null;
            lastExampleCategory = 0;
        }
    }

    TimeSeries getCurrentTimeSeries() {
        return currentTimeSeries;
    }

    public interface DtwDataListener {

        public void exampleAdded(int whichClass);

        public void exampleDeleted(int whichClass);

        public void numExamplesChanged(int whichClass, int currentNumExamples);

        public void allExamplesDeleted();
    }

    public static void main(String[] args) {
        //Testing:
        TimeSeries ts = new TimeSeries(1);
        for (int i = 0; i < 10; i++) {
            ts.addLast(i, new TimeSeriesPoint(new double[]{(double) i}));
        }

        System.out.println("Before downsample:");
        System.out.println(ts.toString());

        TimeSeries d = DtwData.downSample(2, new DtwExample(ts, 1));
        System.out.println("Downsample:");
        System.out.println(d.toString());

    }
}
