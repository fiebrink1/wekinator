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
    
    private Model model = null;
    private ModelBuilder modelBuilder = null;
    private final OSCOutput output;
    private String outputName;
    private final Wekinator w;
    private double outputValue;
    private boolean readyToCompute = false;

    //
    private final List<String> inputNames;
    private Instances myData;
    


    public Path(OSCOutput output, String[] inputs, Wekinator w) {
        this.w = w;
        //TODO want a better model name, unique-ish identifier (e.g. output name + version number)
        //this.model = new SimpleModel("model1");
        //this.modelBuilder = new ModelBuilder();
        this.inputNames = new LinkedList<>();
        for (String input : inputs) {
            inputNames.add(input);
        }
        this.output = output;
        this.outputName = output.getName();
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
    
    public void buildModel(String name) {
        model = modelBuilder.build(name);
    }
    
   /* public void setComputeTriggerAny() {
        triggerType = ComputeTriggerType.ANY;
        updateSchedulerRegistration();
    }
    
    public void setComputeTriggerSelected(InputIdentifier selectedInput) {
        triggerType = ComputeTriggerType.SELECTED;
        selectedIdentifier = selectedInput;
        updateSchedulerRegistration();
    } */
    
    /*private void updateSchedulerRegistration() {
        if (triggerType == ComputeTriggerType.ANY) {
            for (InputIdentifier in : inputIdentifiers) {
                w.getScheduler().registerPathForInputGroup(this, in.groupName);
            }
        } else {
            for (InputIdentifier in : inputIdentifiers) {
                boolean found = false;
                if (in.equals(selectedIdentifier)) {
                    found = true;
                    w.getScheduler().registerPathForInputGroup(this, in.groupName);
                } else {
                    w.getScheduler().unregisterPathForInputGroup(this, in.groupName);
                }
                if (! found) {
                    System.out.println("ERROR in Path: selected identifier " + selectedIdentifier + " not a valid group!");
                }
            }
        }
    } */
        /**
     * Get the value of outputValue
     * If we keep this pull and not push direct to OutputManager, 
     * makes it possible to have multiple Paths active in parallel 
     * (e.g. alternative candidate models)
     * Alternatively, trigger event when new value is computed, and output
     * manager can organise listeners to the appropriate events.
     * (Probably want to advertise to listeners anyway)
     *
     * @return the value of outputValue
     */
   /* public double getOutputValue() {
        return outputValue;
    } */

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
}
