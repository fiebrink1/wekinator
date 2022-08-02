/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DataViewer.java
 *
 * Created on Oct 24, 2009, 2:38:19 PM
 */
package wekimini.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import com.illposed.osc.OSCSerializeException;
import wekimini.OutputManager;
import wekimini.OutputTableModel;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.osc.OSCOutputGroup;
import wekimini.osc.OSCSender;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OutputViewerTable extends javax.swing.JFrame {

    private  OSCOutputGroup outputGroup;
    private  javax.swing.JTable table;
    private  OutputTableModel model;
    private static final Logger logger = Logger.getLogger(OutputViewerTable.class.getName());
    private  List<Path> paths;
    private final Wekinator w;
    private OutputViewerTableSettingsEditor settingsChanger = null;
    private int port = 0;
    private String message = "";
    private String host = "";
    private AddOutputFrame addOutputFrame = null;
    
    
    public OutputViewerTable(Wekinator w) {
        initComponents();
        this.w = w;
        this.outputGroup = w.getOutputManager().getOutputGroup();
        
        w.getOutputManager().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                outputManagerPropertyChanged(evt);
            }
        });
        setForOutputGroup();
    }
    
    private void setForOutputGroup() {
        this.paths = w.getSupervisedLearningManager().getPaths();
        //this.port = outputGroup.getOutputPort();
        this.message = outputGroup.getOscMessage();
        //this.host = w.getOutputManager().getOutputGroup().getHostname();
        this.port = w.getOSCSender().getPort();
        this.host = w.getOSCSender().getHostname().getHostName();
        updateLabel();
        populateTable();
    }
    
    private void updateLabel() {
        label1.setText("Sending " + outputGroup.getNumOutputs() + " outputs "
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
        scrollTable = new javax.swing.JScrollPane();
        label1 = new javax.swing.JLabel();
        buttonSendTest = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        label2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Current output information");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        label1.setText("Sending 12 outputs with message /wek/outputs");

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

        jButton2.setText("Add new output...");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(label1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(label2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jButton1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonSendTest))
                            .add(jButton2))
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
                .add(scrollTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 161, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButton2))
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
                    outputGroup.getNumOutputs());
            
        } catch (IOException ex) {
            Util.showPrettyErrorPane(this, "Could not send message " + ex.getMessage());
            Logger.getLogger(OutputViewerTable.class.getName()).log(Level.SEVERE, null,ex);
        } catch (OSCSerializeException ex) {
            Logger.getLogger(OutputViewerTable.class.getName()).log(Level.SEVERE, null, ex);
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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (addOutputFrame != null) {
            addOutputFrame.toFront();
        } else {
           int numOutputs = w.getOutputManager().getOutputGroup().getNumOutputs();
           addOutputFrame = new AddOutputFrame(numOutputs+1, "outputs-" + (numOutputs+1), w);
           
           Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    //Set to null
                    addOutputFrame = null;
                }
            };
            Util.callOnClosed(addOutputFrame, callMe);
           
           
           addOutputFrame.setVisible(true);
           addOutputFrame.toFront();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

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
        } catch (SocketException ex) {
            Logger.getLogger(OutputViewerTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(OutputViewerTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OutputViewerTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateLabel();
    }
    
    private void outputManagerPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OutputManager.PROP_OUTPUTGROUP)) {
            this.outputGroup = (OSCOutputGroup)(evt.getNewValue());
            setForOutputGroup();
        }
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
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JScrollPane scrollTable;
    // End of variables declaration//GEN-END:variables

    private void populateTable() {
        model = new OutputTableModel(outputGroup, paths);
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        
        setTableColumns();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollTable.setViewportView(table);
        table.repaint();
        
    }

    private void setTableColumns() {
        // table.getColumnModel().get
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        
       /* table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(50); */
       /* for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn c = table.getColumnModel().getColumn(i);
            c.setPreferredWidth(80);
            c.setMinWidth(20);
            c.setResizable(true);
        } */
    }
}
