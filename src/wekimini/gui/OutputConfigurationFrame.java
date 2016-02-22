/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import wekimini.Wekinator;
import wekimini.gui.UpDownDeleteGUI.UpDownDeleteNotifiable;
import wekimini.learning.ModelBuilder;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;
import wekimini.osc.OSCSender;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
//TODO: 
public class OutputConfigurationFrame extends javax.swing.JFrame implements UpDownDeleteNotifiable {

    private Wekinator w;
    private int numOutputs = 0;
    private ArrayList<JSeparator> separators = new ArrayList<>();
    private ArrayList<OutputConfigRow> outputPanels = new ArrayList<>();
    private ArrayList<UpDownDeleteGUI> upDownDeletePanels = new ArrayList<>();
    private ArrayList<JPanel> outputParentPanels = new ArrayList<>();
    private int highestOutputNumber = 1;
    private String baseName = "output";
    private OutputConfigurationReceiver outputGroupReceiver = null;

    /**
     * Creates new form GUIAddEditOutputGroup For GUI testing only
     */
    public OutputConfigurationFrame() {
        initComponents();
        panelOutputsContainer.removeAll();
        addOutputPanel("output-1", null, null);
    }

    public OutputConfigurationFrame(Wekinator w, String msg, String host, int port, int numOutputs, List<OSCOutput> existingOutputs, String[] currentNames, String baseName, ModelBuilder[] modelBuilders, OutputConfigurationReceiver recv) {
        initComponents();
        this.w = w;
        textHost.setText(host);
        textPort.setText(Integer.toString(port));
        textOSCMessage.setText(msg);
        this.outputGroupReceiver = recv;
        this.baseName = baseName;
        addPanels(numOutputs, existingOutputs, currentNames, modelBuilders);
    }

    private void addPanels(int howMany, List<OSCOutput> existingOutputs, String[] names, ModelBuilder[] modelBuilders) {
        panelOutputsContainer.removeAll();
        for (int i = 0; i < howMany; i++) {
            String name;
            if (i < names.length) {
                name = names[i];
            } else {
                name = generateOutputName(i + 1);
            }
            ModelBuilder mb = null;
            if (modelBuilders != null && modelBuilders.length > i) {
                mb = modelBuilders[i];
            }
            
            if (existingOutputs == null || existingOutputs.size() < i+1) {
                addOutputPanel(name, null, mb);
            } else {
                addOutputPanel(name, existingOutputs.get(i), mb);
            }
        }
        JScrollBar vertical = scrollOutputsList.getVerticalScrollBar();
        vertical.setValue(vertical.getMinimum());
    }

    //Adds a panel to the bottom
    //output and mb can both be null
    private void addOutputPanel(String name, OSCOutput output, ModelBuilder mb) {
        numOutputs++;
        JPanel p;
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        UpDownDeleteGUI upDown = new UpDownDeleteGUI(numOutputs, this);

        p.add(upDown);
        upDownDeletePanels.add(upDown);

        OutputConfigRow oscp;
        if (output == null) {
            oscp = new OutputConfigRow(numOutputs, name, mb);
        } else {
            oscp = new OutputConfigRow(numOutputs, name, output, mb);
        }

        p.add(oscp);
        outputPanels.add(oscp);

        panelOutputsContainer.add(p);
        outputParentPanels.add(p);

        JSeparator jsep = new JSeparator();
        jsep.setBackground(new java.awt.Color(255, 255, 255));
        jsep.setForeground(new java.awt.Color(0, 0, 0));
        jsep.setMaximumSize(new java.awt.Dimension(32767, 5));
        jsep.setPreferredSize(new java.awt.Dimension(50, 1));
        panelOutputsContainer.add(jsep);
        separators.add(jsep);

        //repaint and validate etc.
        panelOutputsContainer.revalidate(); //needed to update scrollpane slider
        scrollOutputsList.validate();
        JScrollBar vertical = scrollOutputsList.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
        repaint();

        resetButtonEnabledStates();
    }

    private void swap(int id1, int id2) {
        //Tell panels themselves their ids are changing
        outputPanels.get(id1 - 1).setNum(id2);
        outputPanels.get(id2 - 1).setNum(id1);

        JPanel parent1 = outputParentPanels.get(id1 - 1);
        JPanel parent2 = outputParentPanels.get(id2 - 1);

        parent1.remove(outputPanels.get(id1 - 1));
        parent2.remove(outputPanels.get(id2 - 1));

        parent2.add(outputPanels.get(id1 - 1));
        parent1.add(outputPanels.get(id2 - 1));

        OutputConfigRow opTemp = outputPanels.get(id1 - 1);
        outputPanels.set(id1 - 1, outputPanels.get(id2 - 1));
        outputPanels.set(id2 - 1, opTemp);

        repaint();
//        JPanel p = outputParentPanels.get(id1-1);
//        Component[] cs = p.getComponents();
//        for (int i = 0; i < cs.length; i++) {
//            System.out.println(i + "," + cs[i].getClass().getName());
//        }

    }

    @Override
    public void up(int id) {
        swap(id, id - 1);
    }

    @Override
    public void down(int id) {
        swap(id, id + 1);
    }

    @Override
    public void delete(int id) {
        if (numOutputs == 1) {
            return;
        }

        //Last output? If no, then update everyone's numbering
        if (id != numOutputs) {
            //Change numbering
            for (int i = id; i < numOutputs; i++) {
                outputPanels.get(i).setNum(i);
                upDownDeletePanels.get(i).setID(i);
            }
        }

        numOutputs--;
        JPanel parentPanel = outputParentPanels.get(id - 1);
        panelOutputsContainer.remove(parentPanel);
        JSeparator jsep = separators.get(id - 1);
        panelOutputsContainer.remove(jsep);

        //Remove from arrays
        outputParentPanels.remove(id - 1);
        separators.remove(id - 1);
        outputPanels.remove(id - 1);
        upDownDeletePanels.remove(id - 1);

        //Repaint
        panelOutputsContainer.revalidate(); //needed to update scrollpane slider
        scrollOutputsList.validate();
        JScrollBar vertical = scrollOutputsList.getVerticalScrollBar();
        //vertical.setValue( vertical.getMaximum() );
        repaint();

        resetButtonEnabledStates();

    }

    //TODO probably want a shortcut when # outputs is very high...?
    //Inefficient, but who cares right now?
    private void resetButtonEnabledStates() {
        if (numOutputs == 1) {
            upDownDeletePanels.get(0).setDeleteEnabled(false);
            upDownDeletePanels.get(0).setUpEnabled(false);
            upDownDeletePanels.get(0).setDownEnabled(false);
        } else {
            for (int i = 0; i < upDownDeletePanels.size() - 1; i++) {
                upDownDeletePanels.get(i).setDownEnabled(true);
                upDownDeletePanels.get(i).setDeleteEnabled(true);
            }
            upDownDeletePanels.get(upDownDeletePanels.size() - 1).setDeleteEnabled(true);
            upDownDeletePanels.get(upDownDeletePanels.size() - 1).setDownEnabled(false);

            for (int i = 1; i < upDownDeletePanels.size(); i++) {
                upDownDeletePanels.get(i).setUpEnabled(true);
            }
        }
    }

    private void updateIndividualOutputs(String name) {
        //todo
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelTop = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textOSCMessage = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        textHost = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        textPort = new javax.swing.JTextField();
        scrollOutputsList = new javax.swing.JScrollPane();
        panelOutputsContainer = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        gUIUpDownDelete1 = new wekimini.gui.UpDownDeleteGUI();
        gUIOSCOutputPanel1 = new wekimini.gui.OutputConfigRow();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        gUIUpDownDelete2 = new wekimini.gui.UpDownDeleteGUI();
        gUIOSCOutputPanel2 = new wekimini.gui.OutputConfigRow();
        panelButtons = new javax.swing.JPanel();
        buttonAddGroup = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();
        buttonSendTestMessage = new javax.swing.JButton();
        buttonAdd = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Customise Output Types");
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        panelTop.setBackground(new java.awt.Color(255, 255, 255));
        panelTop.setMaximumSize(new java.awt.Dimension(32767, 76));
        panelTop.setMinimumSize(new java.awt.Dimension(664, 76));
        panelTop.setPreferredSize(new java.awt.Dimension(675, 76));

        jLabel1.setText("OSC message:");

        textOSCMessage.setText("/Wek/outputs_1/");
        textOSCMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textOSCMessageActionPerformed(evt);
            }
        });

        jLabel28.setText("Host (IP address or name):");

        textHost.setText("localhost");

        jLabel29.setText("Port:");

        textPort.setText("6453");
        textPort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textPortKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout panelTopLayout = new javax.swing.GroupLayout(panelTop);
        panelTop.setLayout(panelTopLayout);
        panelTopLayout.setHorizontalGroup(
            panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTopLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textOSCMessage))
                    .addGroup(panelTopLayout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textHost, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel29)
                        .addGap(0, 0, 0)
                        .addComponent(textPort, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelTopLayout.setVerticalGroup(
            panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textOSCMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(textHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        getContentPane().add(panelTop);

        panelOutputsContainer.setLayout(new javax.swing.BoxLayout(panelOutputsContainer, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));
        jPanel4.add(gUIUpDownDelete1);
        jPanel4.add(gUIOSCOutputPanel1);

        panelOutputsContainer.add(jPanel4);

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator2.setMaximumSize(new java.awt.Dimension(32767, 5));
        jSeparator2.setPreferredSize(new java.awt.Dimension(50, 1));
        panelOutputsContainer.add(jSeparator2);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));
        jPanel3.add(gUIUpDownDelete2);
        jPanel3.add(gUIOSCOutputPanel2);

        panelOutputsContainer.add(jPanel3);

        scrollOutputsList.setViewportView(panelOutputsContainer);

        getContentPane().add(scrollOutputsList);

        panelButtons.setBackground(new java.awt.Color(255, 255, 255));
        panelButtons.setMaximumSize(new java.awt.Dimension(32767, 40));
        panelButtons.setMinimumSize(new java.awt.Dimension(0, 40));

        buttonAddGroup.setText("Done");
        buttonAddGroup.setMaximumSize(new java.awt.Dimension(97, 40));
        buttonAddGroup.setMinimumSize(new java.awt.Dimension(97, 40));
        buttonAddGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddGroupActionPerformed(evt);
            }
        });

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonSendTestMessage.setText("Send test OSC message");
        buttonSendTestMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSendTestMessageActionPerformed(evt);
            }
        });

        buttonAdd.setText("Add another output");
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelButtonsLayout.createSequentialGroup()
                .addComponent(buttonAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSendTestMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 142, Short.MAX_VALUE)
                .addComponent(buttonCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonAddGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelButtonsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonAddGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonSendTestMessage)
                    .addComponent(buttonAdd))
                .addContainerGap())
        );

        getContentPane().add(panelButtons);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textOSCMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textOSCMessageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textOSCMessageActionPerformed

    private void buttonAddGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddGroupActionPerformed
        //For testing:
        if (validateForm()) {
            try {
                OSCOutputGroup group = createGroupFromForm();
                ModelBuilder[] modelBuilders = getModelBuildersFromForm();
                outputGroupReceiver.outputConfigurationReady(group, modelBuilders);
                //System.out.println(Util.toXMLString(group, OSCOutputGroup.class.getName(), OSCOutputGroup.class));
                dispose();
            } catch (Exception ex) {
                //Problem with form not caught by validation; do nothing.
            }
        }

    }//GEN-LAST:event_buttonAddGroupActionPerformed

    private OSCOutputGroup createGroupFromForm() throws IllegalStateException {
        List<OSCOutput> outputs = new LinkedList<>();
        for (OutputConfigRow p : outputPanels) {
            try {
                OSCOutput output = p.getOSCOutputFromForm();
                outputs.add(output);
            } catch (IllegalStateException ex) {
                Util.showPrettyErrorPane(this, ex.getMessage());
                throw ex;
            }
        }

        String oscMessage = textOSCMessage.getText().trim();
        String hostName = textHost.getText().trim();
        String portString = textPort.getText().trim();
        int port = Integer.parseInt(portString);

        OSCOutputGroup group = new OSCOutputGroup(outputs,
                oscMessage,
                hostName,
                port);

        return group;
    }

    private ModelBuilder[] getModelBuildersFromForm() {
        ModelBuilder[] modelBuilders = new ModelBuilder[outputPanels.size()];
        for (int i = 0; i < modelBuilders.length; i++) {
            modelBuilders[i] = outputPanels.get(i).getModelBuilderFromForm();
        }
        return modelBuilders;
    }

    
    private boolean validateForm() {

        if (!Util.checkNotBlank(textOSCMessage, "OSC message string", this)) {
            return false;
        }
        if (!Util.checkNotBlank(textHost, "Host name", this)) {
            return false;
        }
        if (!Util.checkNotBlank(textPort, "Port number", this)) {
            return false;
        }
        if (!Util.checkNoSpace(textOSCMessage, "OSC message string", this)) {
            return false;
        }
        if (!Util.checkNoSpace(textPort, "Port number", this)) {
            return false;
        }
        if (!Util.checkIsPositiveNumber(textPort, "Port number", this)) {
            return false;
        }

        if (textOSCMessage.getText().trim().contains(" ")) {
            Util.showPrettyErrorPane(this,
                    "OSC message name must not contain spaces"); //TODO: check for tab, etc.
        }

        HashSet<String> outputNames = new HashSet<>();
        for (OutputConfigRow panel : outputPanels) {
            String nextName = panel.getOutputName();
            if (outputNames.contains(nextName)) {
                Util.showPrettyErrorPane(this,
                        "Individual output names in a group must be unique. ("
                        + nextName + " is reused.)");
                return false;
            }
            outputNames.add(nextName);
        }

        return true;
    }

    private void textPortKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textPortKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_textPortKeyTyped

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed
        addOutputPanel(generateOutputName(outputPanels.size() + 1), null, null);
    }//GEN-LAST:event_buttonAddActionPerformed

    private String generateOutputName(int which) {
        return baseName + "-" + which;
    }

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonSendTestMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSendTestMessageActionPerformed

        String message = textOSCMessage.getText().trim();
        String hostName = textHost.getText().trim();
        InetAddress address;
        try {
            // try {
            address = InetAddress.getByName(hostName);
            //  } catch 
        } catch (UnknownHostException ex) {
            Util.showPrettyErrorPane(this, "Unknown host " + hostName);
            return;
        }

        String portString = textPort.getText().trim();

        if (!Util.checkIsPositiveNumber(textPort, "Port number", this)) {
            return;
        }
        int port = Integer.parseInt(textPort.getText());
        try {
            //try {
            OSCSender.sendTestMessage(message, address, port, numOutputs);
        } catch (IOException ex) {
            Util.showPrettyErrorPane(this, "Could not send message: " + ex.getMessage());
        }

    }//GEN-LAST:event_buttonSendTestMessageActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OutputConfigurationFrame().setVisible(true);
            }
        });
    }

    void adjustForNewNames(String[] names) {
        while (names.length < numOutputs) {
            delete(outputPanels.size() - 1);
        }
        while (names.length > numOutputs) {
            addOutputPanel(generateOutputName(outputPanels.size() + 1), null, null);
        }

        for (int i = 0; i < names.length; i++) {
            outputPanels.get(i).setOutputName(names[i]);
        }

    }

    public interface OutputConfigurationReceiver {

        void outputConfigurationReady(OSCOutputGroup g, ModelBuilder[] modelBuilders);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonAddGroup;
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonSendTestMessage;
    private wekimini.gui.OutputConfigRow gUIOSCOutputPanel1;
    private wekimini.gui.OutputConfigRow gUIOSCOutputPanel2;
    private wekimini.gui.UpDownDeleteGUI gUIUpDownDelete1;
    private wekimini.gui.UpDownDeleteGUI gUIUpDownDelete2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelOutputsContainer;
    private javax.swing.JPanel panelTop;
    private javax.swing.JScrollPane scrollOutputsList;
    private javax.swing.JTextField textHost;
    private javax.swing.JTextField textOSCMessage;
    private javax.swing.JTextField textPort;
    // End of variables declaration//GEN-END:variables
}
