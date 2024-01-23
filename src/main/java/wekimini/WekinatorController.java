/*
 * Executes basic control over learning actions
 */
package wekimini;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import wekimini.LearningManager.LearningType;
import wekimini.learning.SupervisedLearningModel;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCController;
import wekimini.osc.OSCDtwOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class WekinatorController {

    private final Wekinator w;
    private final OSCController oscController;
    private final List<NamesListener> inputNamesListeners;
    private final List<NamesListener> outputNamesListeners;
    private static final Logger logger = Logger.getLogger(WekinatorController.class.getName());

    public WekinatorController(Wekinator w) {
        this.w = w;
        oscController = new OSCController(w);
        inputNamesListeners = new LinkedList<>();
        outputNamesListeners = new LinkedList<>();
    }

    public boolean isOscControlEnabled() {
        return oscController.getOscControlEnabled();
    }

    public void setOscControlEnabled(boolean enabled) {
        oscController.setOscControlEnabled(enabled);
    }


    /*public boolean isTraining() {
     return w.getSupervisedLearningManager().getLearningState() == SupervisedLearningManager.LearningState.TRAINING;
     }
    
     public boolean isRunning() {
     return (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.RUNNING);
     }
    
     public boolean isRecording() {
     return (w.getSupervisedLearningManager().getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING);
     }
    
     public void train() {
     if (w.getSupervisedLearningManager().getLearningState() != SupervisedLearningManager.LearningState.TRAINING) {
     w.getSupervisedLearningManager().buildAll();
     w.getStatusUpdateCenter().update(this, "Training");
     }
     }

     public void cancelTrain() {
     w.getSupervisedLearningManager().cancelTraining();
     w.getStatusUpdateCenter().update(this, "Cancelling training");
     }

     public void startRun() {
     if (w.getSupervisedLearningManager().getRecordingState() == SupervisedLearningManager.RecordingState.RECORDING) {
     w.getSupervisedLearningManager().stopRecording();
     }
     if (w.getSupervisedLearningManager().getRunningState() == SupervisedLearningManager.RunningState.NOT_RUNNING) {
     w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.RUNNING);
     w.getStatusUpdateCenter().update(this, "Running - waiting for inputs to arrive");
     }
     }

     public void stopRun() {
     w.getSupervisedLearningManager().setRunningState(SupervisedLearningManager.RunningState.NOT_RUNNING);
     w.getStatusUpdateCenter().update(this, "Running stopped");
     }*/
    public void deleteAllExamples() {
        w.getLearningManager().deleteAllExamples();
    }

    //Requires modelNum be a valid output (starting from 1)
   /* public void setModelRecordEnabled(int modelNum, boolean enableRecord) {
     try {
     List<Path> ps = w.getSupervisedLearningManager().getPaths();
     Path p = ps.get(modelNum - 1);
     p.setRecordEnabled(enableRecord);
     if (enableRecord) {
     w.getStatusUpdateCenter().update(this, "Enabled model recording");
     } else {
     w.getStatusUpdateCenter().update(this, "Disabled model recording");
     }

     } catch (Exception ex) {
     w.getStatusUpdateCenter().warn(this, "Unable to change record state for output " + modelNum);
     }
     }

     //Requires modelNum be a valid output (starting from 1)
     public void setModelRunEnabled(int modelNum, boolean enableRun) {
     try {
     List<Path> ps = w.getSupervisedLearningManager().getPaths();
     Path p = ps.get(modelNum - 1);
     p.setRunEnabled(enableRun);
     if (enableRun) {
     w.getStatusUpdateCenter().update(this, "Enabled model running");
     } else {
     w.getStatusUpdateCenter().update(this, "Disabled model running");
     }
     } catch (Exception ex) {
     w.getStatusUpdateCenter().warn(this, "Unable to change record state for output " + modelNum);
     }
     }

     public boolean canRecord() {
     return w.getSupervisedLearningManager().isAbleToRecord();
     }

     public boolean canTrain() {
     SupervisedLearningManager.LearningState ls = w.getSupervisedLearningManager().getLearningState();
     return (ls == SupervisedLearningManager.LearningState.DONE_TRAINING ||
     ls == SupervisedLearningManager.LearningState.READY_TO_TRAIN);
     }
    
     public boolean canRun() {
     return w.getSupervisedLearningManager().isAbleToRun();
     }  */
    //Requires either no input group set up yet, or length matches # inputs
    //Listeners can register in case wekinator isn't set up yet (e.g. initial project config screen)
    public void setInputNames(String[] inputNames) {
        if (w.getInputManager().hasValidInputs()) {
            //w.getInputManager().getOSCInputGroup().setInputNames(inputNames);
            //TODO: Fix this, with attention to how Path, SupervisedLearningManager use input names.
            w.getStatusUpdateCenter().warn(this, "Input names cannot be changed after project is created (will be implemented in later version)");
        } else {
            notifyNewInputNames(inputNames);
        }
    }

    //Requires either no output group set up yet, or length matches # outputs
    //Listeners can register in case wekinator isn't set up yet (e.g. initial project config screen)
    public void setOutputNames(String[] outputNames) {
        if (w.getOutputManager().hasValidOutputGroup()) {
            // w.getOutputManager().getOutputGroup().setOutputNames(outputNames);
            w.getStatusUpdateCenter().warn(this, "Output names cannot be changed after project is created (will be implemented in later version)");
        } else {
            notifyNewOutputNames(outputNames);
        }
    }

    //Requires whichInputs and outputNum are valid
    //WhichInputs and outputNum are indexed from 1 here
    public void setInputsForOutput(int[] whichInputs, int outputNum) {
        boolean[][] currentConnections = w.getLearningManager().getConnectionMatrix();
        boolean[][] newConnections = new boolean[currentConnections.length][currentConnections[0].length];
        //currentConnections[i][j] is true if input i is connected to output j
        for (int i = 0; i < currentConnections.length; i++) {
            System.arraycopy(currentConnections[i], 0, newConnections[i], 0, currentConnections[i].length);
            newConnections[i][outputNum - 1] = false;
        }
        for (int i = 0; i < whichInputs.length; i++) {
            newConnections[whichInputs[i] - 1][outputNum - 1] = true;
        }
        w.getLearningManager().updateInputOutputConnections(newConnections);
    }

    public void addInputNamesListener(NamesListener l) {
        inputNamesListeners.add(l);
    }

    public boolean removeInputNamesListener(NamesListener l) {
        return inputNamesListeners.remove(l);
    }

    public void addOutputNamesListener(NamesListener l) {
        outputNamesListeners.add(l);
    }

    public boolean removeOutputNamesListener(NamesListener l) {
        return outputNamesListeners.remove(l);
    }

    /* public void addConnectionsListener(InputOutputConnectionListener l) {
     inputOutputConnectionsListeners.add(l);
     }

     public boolean removeConnectionsListener(InputOutputConnectionListener l) {
     return inputOutputConnectionsListeners.remove(l);
     } */
    private void notifyNewInputNames(String[] newNames) {
        for (NamesListener nl : inputNamesListeners) {
            nl.newNamesReceived(newNames);
        }
    }

    private void notifyNewOutputNames(String[] newNames) {
        for (NamesListener nl : outputNamesListeners) {
            nl.newNamesReceived(newNames);
        }
    }

    /* private void notifyNewInputOutputConnections(boolean[][] connections) {
     for (InputOutputConnectionListener l: inputOutputConnectionsListeners) {
     l.newConnectionMatrix(connections);
     }
     } */
    //Delete examples for model i, starting with i = 0
    public void deleteExamplesForOutput(Integer i) {
        if (w.getLearningManager().getLearningType() == LearningType.SUPERVISED_LEARNING) {
            if (i >= 1 && i <= w.getSupervisedLearningManager().getPaths().size()) {
                w.getSupervisedLearningManager().deleteExamplesForPath(w.getSupervisedLearningManager().getPaths().get(i - 1));
            } else {
                logger.log(Level.WARNING, "Invalid output index {0}: index needs to be between 1 and number of outputs", i);
            }
        } else {
            if (i >= 1 && i <= w.getDtwLearningManager().getModel().getNumGestures()) {
                w.getDtwLearningManager().getModel().getData().deleteExamplesForGesture(i - 1);
            } else {
                logger.log(Level.WARNING, "Invalid output index {0}: index needs to be between 1 and number of DTW gestures", i);
            }
        }
    }

    //Uses modelNum with 0 indexing
    public void loadModelFromFilename(int modelNum, String filename, boolean importData) {
        File file;
        PathAndDataLoader loader;
        try {
            file = new File(filename);
            loader = new PathAndDataLoader();
            loader.tryLoadFromFile(file.getCanonicalPath());

        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            w.getStatusUpdateCenter().warn(this, "Cannot load from file" + filename);
            return;
        }
        final Path loadedPath = loader.getLoadedPath();
        OSCOutput existingOutput = w.getOutputManager().getOutputGroup().getOutput(modelNum);
        //Check compatibility
        if (!isCompatibleType(existingOutput, loadedPath) || !isCompatibleFeatures(loadedPath)) {
            loader.discardLoaded();
            return;
        }

        int[] initialMatches = proposeInitialMatches(loadedPath.getSelectedInputs(), w.getInputManager().getInputNames());
        if (initialMatches.length == 0) {
            loader.discardLoaded();
            return;
        }

        String[] selectedInputs = createStringArrayFromMatchedIndices(initialMatches);

        OSCOutput loadedOutput = loadedPath.getOSCOutput();
        Path oldPath = w.getSupervisedLearningManager().getPaths().get(modelNum);

        w.getSupervisedLearningManager().replacePath(
                oldPath,
                loadedOutput,
                loadedPath.getModelBuilder(),
                selectedInputs,
                (SupervisedLearningModel) loadedPath.getModel(),
                loadedPath.getModelState(),
                false);

        //TODO:Add instances to dataset.
        if (importData) {
            Instances loadedInstances = loader.getLoadedInstances();
            //w.getDataManager().addLoadedDataForPath(loadedInstances, selectedInputs, loadedPath);
            w.getSupervisedLearningManager().addLoadedDataForPathToTraining(loadedInstances, initialMatches, modelNum);
        }
        w.getSupervisedLearningManager().getPaths().get(modelNum).setModelState(loadedPath.getModelState());
        w.getStatusUpdateCenter().update(this, "Model " + (modelNum+1) + " loaded from file " + filename);

        loader.discardLoaded();
    }

    public void saveModelToFilename(int modelNum, String filename) {
        //Get full filename, should end in .xml
        File file = new File(filename);
        try {
            Path p = w.getSupervisedLearningManager().getPaths().get(modelNum);
            p.writeToFile(filename);
            w.getStatusUpdateCenter().update(this, "Model " + (modelNum+1) + " saved to file " + filename);
        } catch (IOException ex) {
            w.getStatusUpdateCenter().warn(this, "Could not write to file " + filename + ex.getMessage());
        }
    }

    private int[] proposeInitialMatches(String[] pathInputs, String[] projectInputs) {
        //A very simple approach:
        int[] proposedIndices = new int[pathInputs.length];
        for (int i = 0; i < pathInputs.length; i++) {
            boolean matched = false;
            for (int j = 0; j < projectInputs.length; j++) {
                if (pathInputs[i].equals(projectInputs[j])) {
                    proposedIndices[i] = j;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                w.getStatusUpdateCenter().warn(this, "Model cannot be loaded: "
                        + "Input names do not match (No match for "
                        + " input " + pathInputs[i] + ")");
                return new int[0]; //THIS IS DIFFERENT FROM PATH EDITOR VERSION
            }
        }
        return proposedIndices;
    }

    private String[] createStringArrayFromMatchedIndices(int[] initialMatches) {
        String[] names = w.getInputManager().getInputNames();
        String[] matchedNames = new String[initialMatches.length];
        for (int i = 0; i < initialMatches.length; i++) {
            matchedNames[i] = names[initialMatches[i]];
        }
        return matchedNames;
    }

    public interface NamesListener {

        public void newNamesReceived(String[] names);
    }

    private boolean isCompatibleFeatures(Path newPath) {
        String[] newInputs = newPath.getSelectedInputs();
        if (newInputs.length > w.getInputManager().getInputNames().length) {
            w.getStatusUpdateCenter().warn(this,
                    "Not enough inputs available: current project has "
                    + w.getInputManager().getInputNames().length
                    + " inputs, but the model in this file requires "
                    + newInputs.length + " inputs.");
            return false;
        }
        return true;
    }

    //TODO This code also appears in PathEditorFrame.java. Consolidate this.
    private boolean isCompatibleType(OSCOutput original, Path newPath) {
        OSCOutput newOutput = newPath.getOSCOutput();
        if (original instanceof OSCNumericOutput && newOutput instanceof OSCNumericOutput) {
            return true;
        } else if (original instanceof OSCClassificationOutput && newOutput instanceof OSCClassificationOutput) {
            return true;
        } else if (original instanceof OSCDtwOutput || newOutput instanceof OSCDtwOutput) {
            w.getStatusUpdateCenter().warn(this,
                    "Model loading is not yet implemented for DTW, sorry!");
            return false;
        } else {
            String msg = "Output types are not compatible: Current type is "
                    + getTypeString(original) + ", but this file uses type "
                    + getTypeString(newOutput);
            w.getStatusUpdateCenter().warn(this, "Could not load from file: " + msg);
            return false;
        }
    }

    private String getTypeString(OSCOutput o) {
        if (o instanceof OSCNumericOutput) {
            return "continuous/numeric";
        } else if (o instanceof OSCClassificationOutput) {
            return "classification";
        } else {
            return "dynamic time warping";
        }
    }

    /*private int[] getInputsOrdering(Path loadedPath) {
     String[] selectedInputs = loadedPath.getSelectedInputs();
     String[] projectInputs = w.getInputManager().getInputNames();
     int[] selectedInputIndices = new int[selectedInputs.length];
     for (int i = 0; i < selectedInputs.length; i++) {
     int correspondingIndex = findStringIndex(selectedInputs[i], projectInputs);
     if (correspondingIndex >= 0) {
     selectedInputIndices[i] = correspondingIndex;
     } else {
     selectedInputIndices[i] = 0;
     System.out.println("Error: No corresponding name found");
     }     
     }
     return selectedInputIndices;
     } */
    private int findStringIndex(String selectedInput, String[] projectInputs) {
        for (int i = 0; i < projectInputs.length; i++) {
            if (projectInputs[i].equals(selectedInput)) {
                return i;
            }
        }
        return -1;
    }
}
