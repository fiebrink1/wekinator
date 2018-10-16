/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.serial;
import com.fazecast.jSerialComm.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import wekimini.osc.OSCReceiver;

/**
 *
 * @author louismccallum
 */

public class SerialPortInput {
    
    public SerialPortDelegate delegate = null;
    private static final int PACKET_SIZE = 6;
    private static final int BAUD_RATE = 38400;
    private static final boolean PRINT_PACKETS = false;
    private static final String EMAKE_PREFIX = "tty.wchusbserial";
    String leftOver = "";
    int sendPtr = 0;
    double[] toSend =  new double[PACKET_SIZE];
    SerialPort port;
    public static final String PROP_CONNECTIONSTATE = "serialConnectionState";
    private SerialConnectionState connectionState = SerialConnectionState.NOT_CONNECTED;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public enum SerialConnectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED, FAIL
    };
    
    public SerialPortInput() {
        //connect();
    }
    
    public SerialConnectionState getConnectionState() {
        return connectionState;
    }
   
    private void setConnectionState(SerialConnectionState connectionState) {
        SerialConnectionState oldConnectionState = this.connectionState;
        this.connectionState = connectionState;
        propertyChangeSupport.firePropertyChange(PROP_CONNECTIONSTATE, oldConnectionState, connectionState);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
   
    public void connect()
    {
        if(port != null) {
            cleanUp();
        }
        SerialPort[] ports = SerialPort.getCommPorts();
        for(SerialPort p : ports)
        {
            System.out.println(p.getSystemPortName());
            if(p.getSystemPortName().contains(EMAKE_PREFIX))
            {
                this.port = p;
                setConnectionState(SerialConnectionState.CONNECTED);
                p.setBaudRate(BAUD_RATE);
                p.openPort();
                System.out.println("opening port....");
                p.addDataListener(new SerialPortDataListener() {
                   @Override
                   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                   @Override
                   public void serialEvent(SerialPortEvent event)
                   {
                       //System.out.println("serialEvent"); 
                        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                            return;
                        byte[] newData = new byte[p.bytesAvailable()];
                        p.readBytes(newData, newData.length);
                        processBytes(newData);
                   }
                });
            }
        }
    }
    
    public void cleanUp()
    {
        System.out.println("closing port....");
        sendPtr = 0;
        toSend =  new double[PACKET_SIZE];
        if(port != null)
        {
            port.removeDataListener();
            port.closePort();
            port = null;
            setConnectionState(SerialConnectionState.NOT_CONNECTED);
        }
        
    }
    
    private void processBytes(byte[] newData)
    {
        String str = leftOver;
        for(byte b : newData)
        {
            char c = (char)b;
            if(c == ',' || c == '\n')
            {
                Double val;
                try {
                   val = Double.parseDouble(str); 
                   toSend[sendPtr] = val;
                   sendPtr++;
                } catch (NumberFormatException e) {
                    
                }
                
                if(sendPtr == PACKET_SIZE)
                {
                    sendPacket();
                }
                str = "";
            }
            else
            {
               str = str + c;
            }
        }
        leftOver = str;
//        String s = new String(newData, "UTF-8");
//        System.out.println(s + "(" + numRead + " bytes, "+ s.length() + "characters)");
    }
    
    private void sendPacket()
    {
        if(PRINT_PACKETS)
        {
            System.out.print("Sending:");
            for(int j = 0; j < toSend.length; j++)
            {
                System.out.print(toSend[j]+",");
            }
            System.out.print("\n");
        }
        delegate.update(toSend);
        sendPtr = 0;
    }  
}