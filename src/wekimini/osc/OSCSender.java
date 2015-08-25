/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.osc;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 *
 * @author rebecca
 */
public class OSCSender {

    //Mapped to OSC OutputGroup ID
    private OSCPortOut sender = null;
    private InetAddress hostname = null;
    private int port = -1;
   // private String sendMessage;
  //  private final String DEFAULT_SEND_MESSAGE = "/wek/outputs";
    private final int DEFAULT_SEND_PORT = 6453;
    private boolean isValidState = false;

    protected EventListenerList listenerList = new EventListenerList();
    private ChangeEvent changeEvent = null;
    private static final Logger logger = Logger.getLogger(OSCSender.class.getName());

    public boolean hasValidHostAndPort() {
        return isValidState;
    }

    public OSCSender() throws UnknownHostException, SocketException {
//        hostname = InetAddress.getByName("localhost");
//        port = DEFAULT_SEND_PORT;
      //  sendMessage = DEFAULT_SEND_MESSAGE;
//        sender = new OSCPortOut(hostname, port);
    }

   /* public OSCSender(InetAddress hostname, int port) throws SocketException {
        this.hostname = hostname;
        this.port = port;
        sendMessage = DEFAULT_SEND_MESSAGE;
        sender = new OSCPortOut(hostname, port);
        isValidState = true;
    } */

    public void setDefaultHostAndPort() throws SocketException, UnknownHostException {
        setHostnameAndPort(InetAddress.getByName("localhost"), DEFAULT_SEND_PORT);
    }

    /*
    public void setSendMessage(String sendMessage) throws SocketException {
        this.sendMessage = sendMessage;
        sender = new OSCPortOut(hostname, port);
    }

    public String getSendMessage() {
        return sendMessage;
    } */

    public InetAddress getHostname() {
        return hostname;
    }

    public void setHostnameAndPort(InetAddress hostname, int port) throws SocketException {
        sender = new OSCPortOut(hostname, port);
        this.port = port;
        this.hostname = hostname;
        isValidState = true;
    }

    public int getPort() {
        return port;
    }

    //Does not establish long-term sender
    //Use for connection testing
    public static void sendTestMessage(String message, InetAddress hostname, int port, int numFloats) throws SocketException, IOException {
        OSCPortOut s = new OSCPortOut(hostname, port);
        Object[] o = new Object[numFloats];
        for (int i = 0; i < o.length; i++) {
            o[i] = new Float(i);
        }
        OSCMessage msg = new OSCMessage(message, o);
        s.send(msg);
    }

    public void sendOutputMessage(String msgName) throws IOException {
        if (isValidState) {
            try {
                OSCMessage msg = new OSCMessage(msgName);
                sender.send(msg);
                fireSendEvent();
            } catch (IOException ex) {
                Logger.getLogger(OSCSender.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        } else {
            logger.log(Level.WARNING, "Could not send OSC message: Invalid state");
        }
    }

      public void sendOutputValueMessage(String msgName, double data) throws IOException {
        if (isValidState) {
            Object[] o = new Object[1];
            try {
                o[0] = (float) data;
                OSCMessage msg = new OSCMessage(msgName, o);
                sender.send(msg);
                fireSendEvent();
            } catch (IOException ex) {
                Logger.getLogger(OSCSender.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        } else {
            logger.log(Level.WARNING, "Could not send OSC message: Invalid state");
        }

    }

    public void sendOutputValuesMessage(String msgName, double[] data) throws IOException {
        if (isValidState) {
            Object[] o = new Object[data.length];
            try {
                for (int i = 0; i < data.length; i++) {
                    o[i] = (float) data[i];
                }
                OSCMessage msg = new OSCMessage(msgName, o);
                sender.send(msg);
                fireSendEvent();
            } catch (IOException ex) {
                Logger.getLogger(OSCSender.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        } else {
            logger.log(Level.WARNING, "Could not send OSC message: Invalid state");
        }

    }

    public void addSendEventListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeSendEventListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    private void fireSendEvent() {
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

    public void sendOutputBundleValuesMessage(String oscMessage, List<List<Double>> allOutputs) throws IOException {
        if (isValidState) {
            List<Object> o = new LinkedList<Object>();
            o.add(new Integer(allOutputs.size()));
            for (List<Double> thisList : allOutputs) {
                for (Double d : thisList) {
                    o.add(d);
                }
            }
            try {
                OSCMessage msg = new OSCMessage(oscMessage + "/bundle", o);
                sender.send(msg);
                fireSendEvent();
            } catch (IOException ex) {
                Logger.getLogger(OSCSender.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        } else {
            logger.log(Level.WARNING, "Could not send OSC message: Invalid state");
        }
    }

    public void sendOutputBundleValuesMessage(String oscMessage, double[][] allDistributions) throws IOException {
        if (isValidState) {
            List<Object> o = new LinkedList<Object>();
            o.add(new Integer(allDistributions.length));
            for (int i = 0; i < allDistributions.length; i++) {
                for (int j = 0; j < allDistributions[i].length; j++) {
                    o.add(allDistributions[i][j]);
                }
            }
            try {
                OSCMessage msg = new OSCMessage(oscMessage + "/bundle", o);
                sender.send(msg);
                fireSendEvent();
            } catch (IOException ex) {
                Logger.getLogger(OSCSender.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        } else {
            logger.log(Level.WARNING, "Could not send OSC message: Invalid state");
        }
    }

}
