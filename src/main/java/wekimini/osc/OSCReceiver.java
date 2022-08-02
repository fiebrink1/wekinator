/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.osc;

import com.illposed.osc.*;
import com.illposed.osc.messageselector.*;
import com.illposed.osc.transport.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.SocketException;
import java.io.IOException;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCReceiver {
    private int receivePort = 6448;
    private OSCPortIn receiver;    
    
    private String oscOutputReceiveString = "/wekinator/control/outputs";
    
    public OSCReceiver() {
    }
    
    public enum ConnectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED, FAIL
    };
    public static final String PROP_CONNECTIONSTATE = "connectionState";
    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    public int getReceivePort() {
        return receivePort;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
   
    private void setConnectionState(ConnectionState connectionState) {
        ConnectionState oldConnectionState = this.connectionState;
        this.connectionState = connectionState;
        propertyChangeSupport.firePropertyChange(PROP_CONNECTIONSTATE, oldConnectionState, connectionState);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void setReceivePort(int port) {
        receivePort = port;
    }
    
    public void startListening() {
        try {
            receiver = new OSCPortIn(receivePort);
        } catch (IOException ex) {
            Util.showPrettyErrorPane(/*caller=*/null,
                    "Could not bind to port " + receivePort
                            + ". Please ensure that nothing else is trying to listen on this same port.");
        }
        receiver.startListening();
        setConnectionState(ConnectionState.CONNECTED);

        //TODO:
        /*w.getInputGroups().addInputGroupListeners(receiver); //input features
        w.getOutputGroups().addOutputGroupListeners(receiver); //ouput updates (optional)
        w.getOscController().addOscControlListeners(receiver); //control messages (optional) */
        
       
        
        //TODO: Need to ensure that we're not adding duplicate listeners later on (e.g. when we change some input names and not others)
        //However, also need to update all listeners appropriately if port has changed (i.e. add everyone)
    }
    
    public void stopListening() {
        //need to notify listeners that receiver has changed?
        if (receiver != null) {
            receiver.stopListening();
            try {
                receiver.close(); //this line causes errors!! No way to get rid of them, nothing to worry about.
            } catch (IOException ex) {
                System.out.println("Failed to close the receiving :(");
            }
        }
        setConnectionState(ConnectionState.NOT_CONNECTED);
    }

    public void addOSCMessageListener(String message, OSCMessageListener listener) {
        if (receiver != null) {
            MessageSelector selector = new OSCPatternAddressMessageSelector(message);
            receiver.getDispatcher().addListener(selector, listener);
        }
    }
    
    public void addOSCOutputValueListener(OSCMessageListener listener) {
        if (receiver != null) {
            MessageSelector selector = new OSCPatternAddressMessageSelector(oscOutputReceiveString);
            receiver.getDispatcher().addListener(selector, listener);
        }
    }
    
    public static void main(String[] args) {
       /* try {
            OSCPortIn receiver = new OSCPortIn(6448);
            OSCMessageListener listener = new OSCMessageListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message) {
                    System.out.println("Message 1 received!");
                }
            };
            OSCMessageListener listener2 = new OSCMessageListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message) {
                    System.out.println("Message 2 received!");
                }
            };


            receiver.startListening();
            receiver.addListener("/m1", listener);
            receiver.addListener("/m2", listener2);
        } catch (SocketException ex) {
            Logger.getLogger(OSCHandler.class.getName()).log(Level.SEVERE, null, ex);
        } */
        
    }
}
