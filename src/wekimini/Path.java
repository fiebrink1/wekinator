/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import wekimini.learning.Model;
import com.thoughtworks.xstream.XStream;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.EventListener;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import wekimini.learning.SupervisedLearningModel;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCSupervisedLearningOutput;

/**
 * Listens for appropriate changes at InputManager, sends inputs to Model,
 * notifies output that new value is ready
 *
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
    protected transient EventListenerList listenerList = new EventListenerList();
    private transient ChangeEvent changeEvent = null;

    private transient SupervisedLearningModel model = null;
    private LearningModelBuilder modelBuilder = null;
    private final OSCOutput output;
    private transient final Wekinator w;
    private final List<String> inputNames;
    private transient final SupervisedLearningManager learningManager;
    private final boolean outputNeedsCorrection;
    private final PathOutputCorrecter outputCorrector;

    public void inheritModel(Path p) {
        setModel(p.model); //TODO: may have to make real copy later...
        setModelState(p.modelState);
    }
    
    public void inheritModelAndBuilder(Path p) {
        setModel(p.model); //TODO: may have to make real copy later...
        setModelBuilder(p.modelBuilder);
        setModelState(p.modelState);
    }

    double[] computeDistribution(Instance instance) {
        return model.computeDistribution(instance);
    }

    //Call this if we're about to delete this path and don't want memory leak.
    //Will have to repeat top section if we ever add things that aren't ChangeListeners
    void removeListeners() {
       ChangeListener[] listeners =listenerList.getListeners(ChangeListener.class);
       for (int i =0 ; i < listeners.length; i++) {
           listenerList.remove(ChangeListener.class, listeners[i]);
       }
       
       PropertyChangeListener pls[] = propertyChangeSupport.getPropertyChangeListeners();
       for (int i = 0; i < pls.length; i++) {
           propertyChangeSupport.removePropertyChangeListener(pls[i]);
       }
    }

    public static enum ModelState {
        NOT_READY, READY_FOR_BUILDING, BUILDING, BUILT, NEEDS_REBUILDING
    };
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

    private SupervisedLearningModel lastModel = null;
    private ModelState lastModelState = ModelState.NOT_READY;
    private boolean trainingCompleted = false;
    private static final Logger logger = Logger.getLogger(Path.class.getName());

    public String[] getSelectedInputs() {
        return inputNames.toArray(new String[0]);
    }

    //Important: Ordering of inputs is meaningless here!!!
    //We assume that the trained model (if any) wants inputs in the same order as they're
    //coming into our system. If we want to be able to load models from other projects where
    //ordering might be different, we'll need to re-arrange features first (probably w/in Model object)
    public void setSelectedInputs(String[] s) {
        String[] oldInputNames = inputNames.toArray(new String[0]);
        boolean hasChanged = (s.length != inputNames.size());

        inputNames.clear();
        for (int i = 0; i < s.length; i++) {
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

    //Copy from existing path
    public Path(Path p, Wekinator w) {
        //Must include all non-transient fields here.
        this.currentModelName = p.currentModelName;
        this.inputNames = new LinkedList<>(p.inputNames);
        this.lastModel = p.lastModel;
        this.lastModelState = p.lastModelState;
        this.learningManager = p.learningManager;
        //this.modelBuilder =   p.modelBuilder;
        this.modelBuilder = p.modelBuilder.fromTemplate(p.modelBuilder); //hack for now: modelBuilder not loaded correctly from file
        this.model = p.model;
        this.modelState = p.modelState;
        this.numExamples = p.numExamples;
        this.output = p.output;
        this.recordEnabled = p.recordEnabled;
        this.runEnabled = p.runEnabled;
        this.trainingCompleted = p.trainingCompleted;
        this.w = w; //Can't set from p: w is transient
        this.outputNeedsCorrection = p.outputNeedsCorrection;
        this.outputCorrector = new PathOutputCorrecter(this.output);

        //TODO: Add listener for output manager - change in output names will be important
        //TODO: Also add listener for input name changes! This will screw us up...
    }

    public boolean isUsingInput(String input) {
        return (inputNames.contains(input)); //Not the most efficient, but who cares right now
    }

    //TODO XXX Make this OSCSupervisedLearning output
    public Path(OSCOutput output, String[] inputs, Wekinator w, SupervisedLearningManager lm) {
        this.w = w;
        this.learningManager = lm;
        //TODO want a better model name, unique-ish identifier (e.g. output name + version number)
        //this.model = new SimpleModel("model1");
        //this.modelBuilder = new ModelBuilder();
        this.inputNames = new LinkedList<>();
        for (String input : inputs) {
            inputNames.add(input);
        }
        this.output = output;
        this.modelBuilder = ((OSCSupervisedLearningOutput)output).getDefaultModelBuilder();
        setCurrentModelName(output.getName());
        outputNeedsCorrection = PathOutputCorrecter.needsCorrecting(output);
        outputCorrector = new PathOutputCorrecter(output);
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

    public void buildModel(String name, Instances i) throws Exception {
        lastModel = model;
        lastModelState = modelState;
        trainingCompleted = false;

        try {
            setModelState(ModelState.BUILDING);
            if (modelBuilder instanceof LearningModelBuilder) {
                ((LearningModelBuilder) modelBuilder).setTrainingExamples(i);
            }
            model = modelBuilder.build(name);
            setCurrentModelName(name);
            setModelState(ModelState.BUILT);
        } catch (Exception ex) {
            model = lastModel;
            setModelState(lastModelState);
            logger.log(Level.WARNING, "Exception encountered in building: " + ex.getMessage());
            logger.log(Level.SEVERE, "Exception encountered in building : {0}", ex.getMessage());
            logger.log(Level.INFO, "Setting model state to {0}", lastModelState);
            trainingCompleted = true;
            throw ex;
        }
        trainingCompleted = true;

    }

    /**
     * Set the value of outputValue
     *
     * @param outputValue new value of outputValue
     */
    /* public void setOutputValue(double outputValue) {
     this.outputValue = outputValue;
     } */
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

    public double compute(Instance instance) throws Exception {
        //TODO: replace with more principled & efficient solution: model output adapter
        if (!outputNeedsCorrection) {
            return model.computeOutput(instance);
        } else {
            return outputCorrector.correct(model.computeOutput(instance));
        }
    }


    public boolean canCompute() {
        return (modelState != ModelState.NOT_READY && modelState != ModelState.READY_FOR_BUILDING);
    }

    public Model getModel() {
        return model;
    }

    public LearningModelBuilder getModelBuilder() {
        return modelBuilder;
    }

    public void setModel(SupervisedLearningModel model) {
        this.model = model;
    }

    public OSCOutput getOSCOutput() {
        return output;
    }

    //not called anywhere...
    public void prepareToDie() {
        //TODO: Remove all of my listeners
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        logger.log(Level.INFO, "Setting model state to {0}", modelState);
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
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
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
    public void setNumExamples(int numExamples) {
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

    public void trainingWasInterrupted() {
        if (!trainingCompleted) {
            setModel(lastModel); //Make sure this isn't a problem when null
            setModelState(lastModelState);
        }
    }

    public boolean canBuild() {
        if (modelState == ModelState.NOT_READY) {
            return false;
        }
        if (numExamples == 0) {
            return false;
        }
        return true;
    }

    /*   @Override
     public String toString() {
     return Util.toXMLString(this, "Path", Path.class);
     } */
    public void writeToFile(String filename) throws IOException {
        boolean success = false;
        IOException myEx = new IOException();

        FileOutputStream outstream = null;
        ObjectOutputStream objout = null;
        try {
            outstream = new FileOutputStream(filename);
            objout = new ObjectOutputStream(outstream);

            XStream xstream = new XStream();
            xstream.alias("Path", Path.class);
            String xml = xstream.toXML(this);
            objout.writeObject(xml);
            if (model != null) {
                String modelClassName = model.getClass().getName();
                objout.writeObject(modelClassName);
                model.writeToOutputStream(objout);
            } else {
                objout.writeObject("null");
            }
            Instances i = w.getSupervisedLearningManager().getTrainingDataForPath(this, true);
                    //w.getSupervisedLearningManager().getTrainingDataForPath(this);
            
            
            if (i != null) {
                objout.writeObject("instances");
                ArffSaver saver = new ArffSaver();
                Instances temp = new Instances(i);
                saver.setDestination(objout);
                saver.setInstances(temp);
                saver.writeBatch();
            } else {
                objout.writeObject("null");
            }
            success = true;
        } catch (IOException ex) {
            success = false;
            myEx = ex;
            logger.log(Level.WARNING, "Could not write to file {0", ex.getMessage());
        } finally {
            try {
                if (objout != null) {
                    objout.close();
                }
                if (outstream != null) {
                    outstream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not close file objects");
            }
        }
        if (!success) {
            throw myEx;
        }

       // os.writeObject(xml);
        // os.writeObject(wmodel);
        //Util.writeToXMLFile(this, "Path", Path.class, filename);
    }

    /* public static Path readFromFile(String filename) throws Exception {
        
        

     //Path g = (Path) Util.readFromXMLFile("Path", Path.class, filename);
     //return g;
     } */
    public void setModelBuilder(LearningModelBuilder mb) {
        this.modelBuilder = mb;
        if (modelState == ModelState.BUILT) {
            setModelState(ModelState.NEEDS_REBUILDING);
        }
    }

    public boolean shouldResetOnEmptyData() {
        if (numExamples == 0 && modelState == ModelState.NEEDS_REBUILDING) {
            return true;
        }
        return false;
    }

    public void resetOnEmptyData() {
        model = null;
        setModelState(ModelState.NOT_READY);
    }

    public static class PathAndDataLoader {

        private static boolean isLoaded = false;
        private static Path loadedPath = null;
        private static Instances loadedInstances = null;

        public static void tryLoadFromFile(String filename) throws Exception {
            //Danger: Will not have any transient fields initialised!
            Path p = null;
            FileInputStream instream = null;
            ObjectInputStream objin = null;
            Object o = null;
            boolean err = false;
            Exception myEx = new Exception();
            try {
                instream = new FileInputStream(filename);
                objin = new ObjectInputStream(instream);
           // o = objin.readObject();

                String xml = (String) objin.readObject();
                XStream xstream = new XStream();
                xstream.alias("Path", Path.class);
                p = (Path) xstream.fromXML(xml);
                //Is p state correct here? yes
                String modelClassName = (String) objin.readObject();
                Model m = null;
                if (!modelClassName.equals("null")) {
                    Class c = Class.forName(modelClassName);
                    m = ModelLoader.loadModel(c, objin);
                }
                p.setModel((SupervisedLearningModel)m);
                
                try {
                    String instancesString = (String) objin.readObject();
                    if (!instancesString.equals("null")) {
                        ArffLoader al = new ArffLoader();
                        al.setSource(objin);
                        loadedInstances = al.getDataSet();
                    } else {
                        loadedInstances = null;
                    }
                } catch (Exception ex) {
                    //Could not load instances: not necessarily a problem
                    logger.log(Level.WARNING, "No instances found in path file; not loading them");
                    loadedInstances = null;
                }
            } catch (Exception ex) {
                myEx = ex;
                err = true;
                logger.log(Level.WARNING, "Error encountered in reading from file: {0}", ex.getMessage());
            } finally {
                try {
                    if (objin != null) {
                        objin.close();
                    }
                    if (instream != null) {
                        instream.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Encountered error closing file objects");
                }

            }
            if (err) {
                throw myEx;
            }
            loadedPath = p;
            isLoaded = true;
        }
    

        public static Path getLoadedPath() {
            if (isLoaded) {
               return loadedPath;
            } else {
                logger.log(Level.WARNING, "Attempt to load path but isLoaded is false");
                return null;
            }
        }
        
        //TODO: getSelectedInputs() isn't ordered yet
        public static String[] getOrderedInputNamesForLoadedPath() {
            if (isLoaded) {
                return loadedPath.getSelectedInputs();
            } else {
                logger.log(Level.WARNING, "Attempt to load input names but isLoaded is false");
                return null;
            }
        }

        //Returns null if no instances could be loaded
        public static Instances getLoadedInstances() {
            if (isLoaded) {
                return loadedInstances;
                //If no instances, fail gracefully?
            } else {
                logger.log(Level.WARNING, "Attempt to load instances but isLoaded is false");
                return null;
            }
        }

        public static void discardLoaded() {
            loadedPath = null;
            loadedInstances = null;
            isLoaded = false;
        }
    }
    
}
