/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
//TODO Might want to make this an interface?
public class LearningManager implements ConnectsInputsToOutputs {
    private static final Logger logger = Logger.getLogger(LearningManager.class.getName());

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

    //Base filename has no extension; add arff for ARFF files and CSV for DTW
    void writeDataToFile(File projectDir, String baseFilename) throws IOException {
        if (learningType == LearningType.SUPERVISED_LEARNING) {
           File f = new File(projectDir + baseFilename + ".arff");
           w.getDataManager().writeInstancesToArff(f);
        } else if (learningType == LearningType.TEMPORAL_MODELING) {
            dtwLearningManager.writeDataToFile(projectDir, baseFilename);
        } else {
            logger.log(Level.WARNING, "Unknown learning manager type");
        }
    
    }

    void saveModels(String directory, Wekinator w) throws IOException {
        if (learningType == LearningType.SUPERVISED_LEARNING) {
            w.getSupervisedLearningManager().saveModels(directory, w);
        } else if (learningType == LearningType.TEMPORAL_MODELING) {
            w.getDtwLearningManager().saveModels(directory, w);
        } else {
            logger.log(Level.WARNING, "Unknown learning manager type");
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
        this.w = w;
    }
    
    public void setSupervisedLearning() {
        if (learningType != LearningType.INITIALIZATION) {
            throw new IllegalStateException("Learning type cannot be changed after initialization is complete");
        }   
        supervisedLearningManager = new SupervisedLearningManager(w);
        connector = supervisedLearningManager;
        setLearningType(LearningType.SUPERVISED_LEARNING);
    }
    
    public void setSupervisedLearningWithExisting(Instances data, List<Path> paths) {
        if (learningType != LearningType.INITIALIZATION) {
            throw new IllegalStateException("Learning type cannot be changed after initialization is complete");
        }   
        supervisedLearningManager = new SupervisedLearningManager(w, data, paths);
        connector = supervisedLearningManager;
        setLearningType(LearningType.SUPERVISED_LEARNING);
    }
    
    
    public void setDtw(OSCOutputGroup outputGroup) {
        if (learningType != LearningType.INITIALIZATION) {
            throw new IllegalStateException("Learning type cannot be changed after initialization is complete");
        }   
        dtwLearningManager = new DtwLearningManager(w, outputGroup);
        connector = dtwLearningManager;
        setLearningType(LearningType.TEMPORAL_MODELING);
        //initialize inputs and outputs etc?
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
