/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import wekimini.osc.OSCOutputGroup;

/**
 *
 * @author rebecca
 */
class DtwLearningManager implements ConnectsInputsToOutputs {

    public DtwLearningManager(Wekinator w, OSCOutputGroup group) {
        
    }
    
    @Override
    public void addConnectionsListener(InputOutputConnectionsListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean[][] getConnectionMatrix() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeConnectionsListener(InputOutputConnectionsListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateInputOutputConnections(boolean[][] newConnections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void deleteAllExamples() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean isLegalTrainingValue(int whichOutput, float value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
