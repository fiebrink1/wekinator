/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import weka.core.Instances;
import wekimini.osc.OSCOutput;

/**
 * Listens for appropriate changes at InputManager, sends inputs to Model,
 * notifies output that new value is ready
 * @author rebecca
 */
public class Path {
    //Input list (group/name pairs)
    //Input listeners (modified if any input groups are modified)
    //Trigger type: Any input (group), specific input (group)
    //Output (name - to know when to modify)
    //*** Type (must correspond to output type) ?
    //  Can probably set output name & type in constructor (final)
    //ModelBuilder (takes care of holding data where applicable, as well as training)
    //Model (trained; computes output from inputs)
    //*** State: Valid, Invalid (i.e., outputs & inputs all exist and are of OK type)
    
    //How to deal with scheduling?
    //If output group triggered on 1st model, then will always go when 1st model is computed
    //and others are still in process!
    //probably don't want "any" in group to be an option!
    //When we receive an input group, let's make list of all paths that are affected
    //and use this to make list of all outputs that are affected
    //Then do computation for all
    //Then tell output manager to trigger for list of outputs all at once
    
    protected EventListenerList listenerList = new EventListenerList();
    private ChangeEvent changeEvent = null;

    
    private Model model = null;
    private ModelBuilder modelBuilder = null;
    private final OSCOutput output;
    private String outputName;
    private final Wekinator w;
    private double outputValue;
    //
    private final List<String> inputNames;
    private final LearningManager learningManager;
    
    public static enum ModelState {NOT_READY, READY_FOR_BUILDING, BUILDING, BUILT, NEEDS_REBUILDING};
    //private boolean hasData = false;
    
    private ModelState modelState = ModelState.NOT_READY;

    public static final String PROP_MODELSTATE = "modelState";

    private boolean recordEnabled = true;

    public static final String PROP_RECORDENABLED = "recordEnabled";

    private boolean runEnabled = true;

    public static final String PROP_RUNENABLED = "runEnabled";

    private int numExamples = 0;

    public static final String PROP_NUMEXAMPLES = "numExamples";

        private String currentModelName = "model";

    public static final String PROP_CURRENTMODELNAME = "currentModelName";



    public String[] getSelectedInputs() {
        return inputNames.toArray(new String[0]);
    }
    
    public void setSelectedInputs(String[] s) {
        String[] oldInputNames = inputNames.toArray(new String[0]);
        boolean hasChanged = (s.length != inputNames.size());
        
        inputNames.clear();
        for (int i= 0; i < s.length; i++) {
            inputNames.add(s[i]);
            
            if (!hasChanged && !s[i].equals(oldInputNames[i])) {
                hasChanged = true;
            }
        }
        
        if (hasChanged) {
            fireInputSelectionChanged();
        }
        
        if (hasChanged && modelState == ModelState.BUILT) {
            setModelState(ModelState.NEEDS_REBUILDING);
        }
        
    }
    
    
    public Path(OSCOutput output, String[] inputs, Wekinator w) {
        this.w = w;
        this.learningManager = w.getLearningManager();
        //TODO want a better model name, unique-ish identifier (e.g. output name + version number)
        //this.model = new SimpleModel("model1");
        //this.modelBuilder = new ModelBuilder();
        this.inputNames = new LinkedList<>();
        for (String input : inputs) {
            inputNames.add(input);
        }
        this.output = output;
        this.outputName = output.getName();
        this.modelBuilder = output.getDefaultModelBuilder();
        //updateSchedulerRegistration();
        
       /* w.getInputManager().addInputGroupChangeListener(new InputManager.InputGroupChangeListener() {

            @Override
            public void inputGroupChange(InputManager.InputGroupChangeEvent evt) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        
        w.getOutputManager().addOutputGroupChangeListener(new OutputManager.OutputGroupChangeListener() {

            @Override
            public void outputGroupChange(OutputManager.OutputGroupChangeEvent evt) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }); */
        
        //TODO: Add listener for output manager - change in output names will be important
        //TODO: Also add listener for input name changes! This will screw us up...
        
        //Best approach is to make in/out edit GUI make people be explicit about re-ordering / renaming, and then pass those on as specific event types.
    }
    
    //Learning manager must call this
    //TODO: Have to be very careful here if model is in process of training... disallow certain operations!
    public void notifyExamplesChanged(int numExamples) {
        setNumExamples(numExamples);
       // this.numExamples = numExamples;
       // hasData = (numExamples > 0);
        
        if (modelState == ModelState.NOT_READY) {
            if (numExamples > 0) {
                setModelState(ModelState.READY_FOR_BUILDING);
            }
        } else if (modelState == ModelState.READY_FOR_BUILDING) {
            if (numExamples == 0) {
                setModelState(ModelState.NOT_READY);
            }
        } else if (modelState == ModelState.BUILT) {
            setModelState(ModelState.NEEDS_REBUILDING);
        }
    }
    
    //TODO: do this in separate thread, get callback when done
    public void buildModel(String name, Instances i) {
        setModelState(ModelState.BUILDING);
        if (modelBuilder instanceof LearningModelBuilder) {
            ((LearningModelBuilder)modelBuilder).setTrainingExamples(i);
        }
        model = modelBuilder.build(name);
        setModelState(ModelState.BUILT);

    }

    /**
     * Set the value of outputValue
     *
     * @param outputValue new value of outputValue
     */
    public void setOutputValue(double outputValue) {
        this.outputValue = outputValue;
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

    
    public double compute(double[] inputs) {
        //Do something
       // float[] inputs = {1.0f, 1.0f};
        //setOutputValue(model.computeOutput(inputs));
       return model.computeOutput(inputs);
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public OSCOutput getOSCOutput() {
        return output;
    }
           
    public void prepareToDie() {
        //TODO: Remove all of my listeners
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Get the value of modelState
     *
     * @return the value of modelState
     */
    public ModelState getModelState() {
        return modelState;
    }

    /**
     * Set the value of modelState
     *
     * @param modelState new value of modelState
     */
    public void setModelState(ModelState modelState) {
        ModelState oldModelState = this.modelState;
        this.modelState = modelState;
        propertyChangeSupport.firePropertyChange(PROP_MODELSTATE, oldModelState, modelState);
    }
    
    public void addInputSelectionChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeInputSelectionChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }
    
    private void fireInputSelectionChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    

        /**
     * Get the value of recordEnabled
     *
     * @return the value of recordEnabled
     */
    public boolean isRecordEnabled() {
        return recordEnabled;
    }

    /**
     * Set the value of recordEnabled
     *
     * @param recordEnabled new value of recordEnabled
     */
    public void setRecordEnabled(boolean recordEnabled) {
        boolean oldRecordEnabled = this.recordEnabled;
        this.recordEnabled = recordEnabled;
        propertyChangeSupport.firePropertyChange(PROP_RECORDENABLED, oldRecordEnabled, recordEnabled);
    }
    
        /**
     * Get the value of runEnabled
     *
     * @return the value of runEnabled
     */
    public boolean isRunEnabled() {
        return runEnabled;
    }

    /**
     * Set the value of runEnabled
     *
     * @param runEnabled new value of runEnabled
     */
    public void setRunEnabled(boolean runEnabled) {
        boolean oldRunEnabled = this.runEnabled;
        this.runEnabled = runEnabled;
        propertyChangeSupport.firePropertyChange(PROP_RUNENABLED, oldRunEnabled, runEnabled);
    }
    
        /**
     * Get the value of numExamples
     *
     * @return the value of numExamples
     */
    public int getNumExamples() {
        return numExamples;
    }

    /**
     * Set the value of numExamples
     *
     * @param numExamples new value of numExamples
     */
    private void setNumExamples(int numExamples) {
        int oldNumExamples = this.numExamples;
        this.numExamples = numExamples;
        propertyChangeSupport.firePropertyChange(PROP_NUMEXAMPLES, oldNumExamples, numExamples);
    }
    
    /**
     * Get the value of currentModelName
     *
     * @return the value of currentModelName
     */
    public String getCurrentModelName() {
        return currentModelName;
    }

    /**
     * Set the value of currentModelName
     *
     * @param currentModelName new value of currentModelName
     */
    public void setCurrentModelName(String currentModelName) {
        String oldCurrentModelName = this.currentModelName;
        this.currentModelName = currentModelName;
        propertyChangeSupport.firePropertyChange(PROP_CURRENTMODELNAME, oldCurrentModelName, currentModelName);
    }


}
