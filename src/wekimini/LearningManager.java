/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
//TODO Might want to make this an interface?
public class LearningManager implements ConnectsInputsToOutputs {

    @Override
    public void addConnectionsListener(InputOutputConnectionsListener l) {
        assert (connector != null); //XXX check all these valid
        connector.addConnectionsListener(l);
    }

    @Override
    public boolean[][] getConnectionMatrix() {
        assert (connector != null);
        return connector.getConnectionMatrix();
    }

    @Override
    public boolean removeConnectionsListener(InputOutputConnectionsListener l) {
        assert (connector != null);
        return connector.removeConnectionsListener(l);
    }

    @Override
    public void updateInputOutputConnections(boolean[][] newConnections) {
        assert (connector != null);
        connector.updateInputOutputConnections(newConnections);
    }

    boolean isLegalTrainingValue(int whichOutput, float value) {
        if (learningType == LearningType.SUPERVISED_LEARNING) {
            return supervisedLearningManager.isLegalTrainingValue(whichOutput, value);
        } else if (learningType == LearningType.TEMPORAL_MODELING) {
            return dtwLearningManager.isLegalTrainingValue(whichOutput, value);
        } else {
            return true;
        }
    }
    
    public static enum LearningType {INITIALIZATION, SUPERVISED_LEARNING, TEMPORAL_MODELING};
    //private LearningType learningType = LearningType.INITIALIZATION;
    private SupervisedLearningManager supervisedLearningManager = null;
    private DtwLearningManager dtwLearningManager = null;
    private ConnectsInputsToOutputs connector;
    
    private final Wekinator w;
    
        private LearningType learningType = LearningType.INITIALIZATION;

    public static final String PROP_LEARNINGTYPE = "learningType";

    /**
     * Get the value of learningType
     *
     * @return the value of learningType
     */
    public LearningType getLearningType() {
        return learningType;
    }

    /**
     * Set the value of learningType
     *
     * @param learningType new value of learningType
     */
    private void setLearningType(LearningType learningType) {
        LearningType oldLearningType = this.learningType;
        this.learningType = learningType;
        propertyChangeSupport.firePropertyChange(PROP_LEARNINGTYPE, oldLearningType, learningType);
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
        this. w = w;
    }
    

    public void setSupervisedLearning() {
        if (learningType != LearningType.INITIALIZATION) {
            throw new IllegalStateException("Learning type cannot be changed after initialization is complete");
        }   
        supervisedLearningManager = new SupervisedLearningManager(w);
        connector = supervisedLearningManager;
        setLearningType(LearningType.SUPERVISED_LEARNING);
    }
    
    public void setDtw(OSCOutputGroup outputGroup) {
        if (learningType != LearningType.INITIALIZATION) {
            throw new IllegalStateException("Learning type cannot be changed after initialization is complete");
        }   
        setLearningType(LearningType.TEMPORAL_MODELING);
        dtwLearningManager = new DtwLearningManager(w, outputGroup);
        connector = dtwLearningManager;
    }
    
    public SupervisedLearningManager getSupervisedLearningManager() {
        assert (supervisedLearningManager != null);
        return supervisedLearningManager;         
    }
    
    public DtwLearningManager getDtwLearningManager() {
        assert (dtwLearningManager != null);
        return dtwLearningManager;
    }
    
    public void deleteAllExamples() {
        if (learningType == LearningType.SUPERVISED_LEARNING) {
            supervisedLearningManager.deleteAllExamples();
        } else if (learningType == LearningType.TEMPORAL_MODELING) {
            dtwLearningManager.deleteAllExamples();
        }
    }
}
