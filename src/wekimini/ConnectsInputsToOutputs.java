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

    public void addConnectionsListener(InputOutputConnectionsListener l, boolean features);

    public boolean[][] getConnectionMatrix(boolean features);

    public boolean removeConnectionsListener(InputOutputConnectionsListener l, boolean features);

    //newConnections[i][j] is true if input i is connected to output j
    public void updateInputOutputConnections(boolean[][] newConnections, boolean features);
 
    public interface InputOutputConnectionsListener {
        public void newConnectionMatrix(boolean[][] connections);
    }
    
}
