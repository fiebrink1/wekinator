/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

/**
 *
 * @author rebecca
 */
public interface ConnectsInputsToOutputs {

    public void addConnectionsListener(InputOutputConnectionsListener l);

    public boolean[][] getConnectionMatrix();

    public boolean removeConnectionsListener(InputOutputConnectionsListener l);

    //newConnections[i][j] is true if input i is connected to output j
    public void updateInputOutputConnections(boolean[][] newConnections);
 
    public interface InputOutputConnectionsListener {
        public void newConnectionMatrix(boolean[][] connections);
    }
    
}
