/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DataViewer.java
 *
 * Created on Oct 24, 2009, 2:38:19 PM
 */
package wekimini.dtw.gui;

import wekimini.gui.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import wekimini.OutputTableModel;
import wekimini.Wekinator;
import wekimini.osc.OSCDtwOutput;
import wekimini.osc.OSCOutputGroup;
import wekimini.util.Util;
import com.illposed.osc.OSCSerializeException;

/**
 *
 * @author rebecca
 */
public class DtwOutputEditor extends javax.swing.JFrame {

    private final OSCOutputGroup outputGroup;
    private  javax.swing.JTable table;
    private  OutputTableModel model;
    private static final Logger logger = Logger.getLogger(DtwOutputEditor.class.getName());
    private final Wekinator w;
    private OutputViewerTableSettingsEditor settingsChanger = null;
    private int port = 0;
    private String message = "";
    private String host = "";
    
    
    public DtwOutputEditor(Wekinator w) {
        initComponents();
        this.w = w;
        this.outputGroup = w.getOutputManager().getOutputGroup();
        //this.port = outputGroup.getOutputPort();
        this.message = outputGroup.getOscMessage();
        //this.host = w.getOutputManager().getOutputGroup().getHostname();
        this.port = w.getOSCSender().getPort();
        this.host = w.getOSCSender().getHostname().getHostName();
        updateLabel();
    }
    
    private void updateLabel() {
        OSCDtwOutput o = (OSCDtwOutput)(outputGroup.getOutput(0));
        label1.setText("Sending " + o.getNumGestures() + " DTW distances "
                 + "with message " + message);
        label2.setText("Sending to " + host + " at port " + port);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        label1 = new javax.swing.JLabel();
        buttonSendTest = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        label2 = new javax.swing.JLabel();
        label3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Current output information");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        label1.setText("Sending 12 DTW distances with message /wek/outputs");

        buttonSendTest.setText("Send test message");
        buttonSendTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSendTestActionPerformed(evt);
            }
        });

        jButton1.setText("Change host, port, or message");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        label2.setText("Sending to localhost at port 12000");

        label3.setText("<html>Also sending individual OSC messages for each matched DTW gesture (edit this using DTW model editor)</html>");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(label2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(label1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 420, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(label3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 404, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jButton1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonSendTest)))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(label1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(label2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(buttonSendTest))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(label3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    }//GEN-LAST:event_formWindowClosing

    private void buttonSendTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSendTestActionPerformed
        try {     
            OSCDtwOutput o = (OSCDtwOutput)(outputGroup.getOutput(0));

            
            InetAddress address;
            try {
                // try {
                address = InetAddress.getByName(host);
                //  } catch
            } catch (UnknownHostException ex) {
                Util.showPrettyErrorPane(this, "Unknown host " + host);
                return;
            }
            
            w.getOSCSender().sendTestMessage(message,
                    address,
                    port,
                    o.getNumGestures());
            
        } catch (IOException ex) {
            Util.showPrettyErrorPane(this, "Could not send message " + ex.getMessage());
            Logger.getLogger(DtwOutputEditor.class.getName()).log(Level.SEVERE, null,ex);
        } catch (OSCSerializeException ex) {
            Util.showPrettyErrorPane(this, "Could not send message " + ex.getMessage());
            Logger.getLogger(DtwOutputEditor.class.getName()).log(Level.SEVERE, null,ex);
        }

    }//GEN-LAST:event_buttonSendTestActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (settingsChanger == null) {
            settingsChanger = new OutputViewerTableSettingsEditor(port, message, host, new OutputViewerTableSettingsEditor.OutputOSCSettingsReceiver() {
                @Override
                public void receiveNewSettings(int port, String message, String host) {
                    updateSettings(port, message, host);
                }
            });
            settingsChanger.setVisible(true);
            settingsChanger.toFront();
        } else {
            settingsChanger.setVisible(true);
            settingsChanger.toFront();
        } 
    }//GEN-LAST:event_jButton1ActionPerformed

    private void updateSettings(int port, String message, String hostname) {
        this.port = port;
        this.message = message;
        this.host = hostname;
        
        outputGroup.setOutputPort(port);
        outputGroup.setOscMessage(message);
        outputGroup.setHostName(hostname);
        
        try {
            //Problem: No clear responsibility for this: outputgroup vs outputmanager vs sender..
            //Probably want these saved with output group (because specific to an output, e.g. max patch)
            //HOWEVER output group current has these as final fields...
            //So we have to do this at top level too:
            w.getOSCSender().setHostnameAndPort(InetAddress.getByName(host), port);
        } catch (IOException ex) {
            Logger.getLogger(DtwOutputEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateLabel();
    }
   
    /*private File findArffFileToSave() throws IOException {
     return null;
     //TODOTODOTODO: handle this after get fileext support in.
     } */
    /**
     * @param args the command line arguments
     */
   /* public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                boolean isDiscrete[] = {true, false};
                int numVals[] = {3, 3};
                //String featureNames[] = {"F1", "f2", "F3", "f4", "f5"};
                String featureNames[] = new String[100];
                for (int i = 0; i < featureNames.length; i++) {
                    featureNames[i] = "F" + i;
                }
                String paramNames[] = {"P1", "p2"};
                SimpleDataset s = new SimpleDataset(featureNames.length, 2, isDiscrete, numVals, featureNames, paramNames);

                new DatasetViewer(s).setVisible(true);
            }
        });
    } */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonSendTest;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JLabel label3;
    // End of variables declaration//GEN-END:variables

}
