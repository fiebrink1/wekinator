/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.serial;
import com.fazecast.jSerialComm.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 *
 * @author louismccallum
 */

public class SerialPortInput {
    
    public SerialPortDelegate delegate = null;
    private static final int NUM_VALS = 6;
    private static final int BAUD_RATE = 38400;
    private static final boolean PRINT_PACKETS = false;
    String leftOver = "";
    int sendPtr = 0;
    double[] toSend =  new double[NUM_VALS];
    
    public SerialPortInput() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for(SerialPort p : ports)
        {
            System.out.println(p.getSystemPortName());
            if(p.getSystemPortName().contains("tty.wchusbserial"))
            {
                p.setBaudRate(BAUD_RATE);
                p.openPort();
                p.addDataListener(new SerialPortDataListener() {
                   @Override
                   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                   @Override
                   public void serialEvent(SerialPortEvent event)
                   {
                        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                            return;
                        byte[] newData = new byte[p.bytesAvailable()];
                        int numRead = p.readBytes(newData, newData.length);
                   }
                });
            }
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
                toSend[sendPtr] = Double.parseDouble(str);
                sendPtr++;
                if(sendPtr == NUM_VALS)
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