/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.CardLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import wekimini.Wekinator;
import wekimini.learning.AdaboostModelBuilder;
import wekimini.learning.J48ModelBuilder;
import wekimini.learning.KNNModelBuilder;
import wekimini.learning.ModelBuilder;
import wekimini.learning.SVMModelBuilder;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCInputGroup;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.osc.OSCOutputGroup;
import wekimini.osc.OSCReceiver;
import wekimini.util.Util;
import wekimini.util.WeakListenerSupport;

/**
 *
 * @author rebecca
 */
public class InitInputOutputFrame extends javax.swing.JFrame {

    private Wekinator w = null;
    private PropertyChangeListener oscReceiverListener = null;
    private final WeakListenerSupport wls = new WeakListenerSupport();
    private String[] currentInputNames = new String[0];
    private String[] currentOutputNames = new String[0];

    private final static int COMBO_REGRESSION_INDEX = 0;
    private final static int COMBO_CLASSIFICATION_INDEX = 1;
    private final static int COMBO_CUSTOM_INDEX = 2;

    private final static int COMBO_KNN_INDEX = 0;
    private final static int COMBO_ADABOOST_INDEX = 2;
    private final static int COMBO_SVM_INDEX = 1;
    private final static int COMBO_J48_INDEX = 3;

    private OutputConfigurationFrame outputConfigViewer = null;
    private final OutputConfigurationFrame.OutputGroupReceiver outputGroupReceiver = this::initFormForOutputGroup;
    private OSCOutputGroup customConfiguredOutput = null;
    private static final Logger logger = Logger.getLogger(InitInputOutputFrame.class.getName());

    /**
     * Creates new form initInputOutputFrame
     */
    public InitInputOutputFrame() {
        initComponents();
    }

    public InitInputOutputFrame(Wekinator w) {
        initComponents();
        setWekinator(w);
        updateOutputCard();
        /* fieldNumOutputs.getDocument().addDocumentListener(new DocumentListener() {

         @Override
         public void insertUpdate(DocumentEvent e) {
         updateCustomOutputPermissions();
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
         updateCustomOutputPermissions();
         }

         @Override
         public void changedUpdate(DocumentEvent e) {
         updateCustomOutputPermissions();
         }
         }); */
    }

    private void initFormForOutputGroup(OSCOutputGroup g) {
        customConfiguredOutput = g;
        fieldOutputOSCMessage.setText(g.getOscMessage());
        fieldHostName.setText(g.getHostname());
        fieldSendPort.setText(Integer.toString(g.getOutputPort()));
        fieldNumOutputs.setText(Integer.toString(g.getNumOutputs()));
        currentOutputNames = new String[g.getNumOutputs()];
        System.arraycopy(g.getOutputNames(), 0, currentOutputNames, 0, currentOutputNames.length);
        comboOutputType.setSelectedIndex(COMBO_CUSTOM_INDEX);
    }

    public void setWekinator(Wekinator w) {
        this.w = w;
        updateGUIForConnectionState(w.getOSCReceiver().getConnectionState());
        oscReceiverListener = this::oscReceiverPropertyChanged;
        w.getOSCReceiver().addPropertyChangeListener(wls.propertyChange(oscReceiverListener));
    }

    private void updateGUIForConnectionState(OSCReceiver.ConnectionState cs) {
        if (cs == OSCReceiver.ConnectionState.CONNECTED) {
            labelOscStatus.setText("Connected on port " + w.getOSCReceiver().getReceivePort());
            buttonOscListen.setText("Disconnect");
            //  buttonNext.setEnabled(true);
        } else if (cs == OSCReceiver.ConnectionState.FAIL) {
            labelOscStatus.setText("Failed to connect");
            buttonOscListen.setText("Connect");
            //  buttonNext.setEnabled(false);
        } else if (cs == OSCReceiver.ConnectionState.NOT_CONNECTED) {
            labelOscStatus.setText("Not connected");
            buttonOscListen.setText("Connect");
            //  buttonNext.setEnabled(false);
        } else if (cs == OSCReceiver.ConnectionState.CONNECTING) {
            labelOscStatus.setText("Connecting...");
            buttonOscListen.setText("Disconnect");
            //  buttonNext.setEnabled(false);
        }
    }

    private void oscReceiverPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == OSCReceiver.PROP_CONNECTIONSTATE) {
            updateGUIForConnectionState((OSCReceiver.ConnectionState) evt.getNewValue());
        }
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fieldOscPort = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        buttonOscListen = new javax.swing.JButton();
        labelOscStatus = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        fieldNumInputs = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        fieldInputOSCMessage = new javax.swing.JTextField();
        buttonLoadInputsFromFile = new javax.swing.JButton();
        buttonCustomiseInputNames = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        fieldNumOutputs = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        fieldOutputOSCMessage = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        comboOutputType = new javax.swing.JComboBox();
        panelOutputTypes = new javax.swing.JPanel();
        cardBlank = new javax.swing.JPanel();
        cardNumClasses = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        fieldNumClasses = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        comboClassifierType = new javax.swing.JComboBox();
        panelCustom = new javax.swing.JPanel();
        buttonConfigureOutputs = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        buttonCustomiseOutputNames = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        fieldHostName = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        fieldSendPort = new javax.swing.JTextField();
        buttonChooseOutputFile = new javax.swing.JButton();
        buttonNext = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Receiving OSC"));

        jLabel1.setText("Wekinator listening for inputs and control on port:");

        fieldOscPort.setText("6448");
        fieldOscPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldOscPortActionPerformed(evt);
            }
        });
        fieldOscPort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldOscPortKeyTyped(evt);
            }
        });

        jLabel2.setText("Status:");

        buttonOscListen.setText("Start listening");
        buttonOscListen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOscListenActionPerformed(evt);
            }
        });

        labelOscStatus.setText("Not connected");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(buttonOscListen)
                        .addGap(250, 250, 250))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelOscStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldOscPort, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(labelOscStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fieldOscPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonOscListen)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Inputs"));

        jLabel3.setText("# inputs:");

        fieldNumInputs.setText("5");
        fieldNumInputs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldNumInputsKeyTyped(evt);
            }
        });

        jLabel4.setText("OSC message name:");

        fieldInputOSCMessage.setText("/wek/inputs");
        fieldInputOSCMessage.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldInputOSCMessageFocusLost(evt);
            }
        });
        fieldInputOSCMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldInputOSCMessageActionPerformed(evt);
            }
        });
        fieldInputOSCMessage.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldInputOSCMessageKeyTyped(evt);
            }
        });

        buttonLoadInputsFromFile.setText("Load from file");
        buttonLoadInputsFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadInputsFromFileActionPerformed(evt);
            }
        });

        buttonCustomiseInputNames.setText("Customise names");
        buttonCustomiseInputNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomiseInputNamesActionPerformed(evt);
            }
        });

        jLabel11.setText("Optional:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldInputOSCMessage))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldNumInputs, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCustomiseInputNames)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonLoadInputsFromFile)
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(fieldInputOSCMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(fieldNumInputs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(buttonCustomiseInputNames)
                    .addComponent(buttonLoadInputsFromFile))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Outputs"));

        jLabel5.setText("# outputs:");

        fieldNumOutputs.setText("5");
        fieldNumOutputs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldNumOutputsActionPerformed(evt);
            }
        });
        fieldNumOutputs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldNumOutputsKeyTyped(evt);
            }
        });

        jLabel6.setText("OSC message name:");

        fieldOutputOSCMessage.setText("/wek/outputs");
        fieldOutputOSCMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldOutputOSCMessageActionPerformed(evt);
            }
        });

        jLabel7.setText("Output types:");

        comboOutputType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All continuous (default settings)", "All classifiers (default settings)", "Custom" }));
        comboOutputType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboOutputTypeActionPerformed(evt);
            }
        });

        panelOutputTypes.setLayout(new java.awt.CardLayout());

        cardBlank.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout cardBlankLayout = new javax.swing.GroupLayout(cardBlank);
        cardBlank.setLayout(cardBlankLayout);
        cardBlankLayout.setHorizontalGroup(
            cardBlankLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 509, Short.MAX_VALUE)
        );
        cardBlankLayout.setVerticalGroup(
            cardBlankLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 34, Short.MAX_VALUE)
        );

        panelOutputTypes.add(cardBlank, "cardBlank");

        cardNumClasses.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setText("with");

        fieldNumClasses.setText("5");
        fieldNumClasses.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldNumClassesKeyTyped(evt);
            }
        });

        jLabel10.setText("classes, using");

        comboClassifierType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "k-Nearest Neighbor", "Support Vector Machine", "AdaBoost", "Decision Tree" }));

        javax.swing.GroupLayout cardNumClassesLayout = new javax.swing.GroupLayout(cardNumClasses);
        cardNumClasses.setLayout(cardNumClassesLayout);
        cardNumClassesLayout.setHorizontalGroup(
            cardNumClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardNumClassesLayout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldNumClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboClassifierType, 0, 224, Short.MAX_VALUE)
                .addContainerGap())
        );
        cardNumClassesLayout.setVerticalGroup(
            cardNumClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardNumClassesLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(cardNumClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(fieldNumClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(comboClassifierType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelOutputTypes.add(cardNumClasses, "cardClassification");

        panelCustom.setBackground(new java.awt.Color(255, 255, 255));

        buttonConfigureOutputs.setText("Configure");
        buttonConfigureOutputs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConfigureOutputsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCustomLayout = new javax.swing.GroupLayout(panelCustom);
        panelCustom.setLayout(panelCustomLayout);
        panelCustomLayout.setHorizontalGroup(
            panelCustomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCustomLayout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addComponent(buttonConfigureOutputs)
                .addContainerGap(312, Short.MAX_VALUE))
        );
        panelCustomLayout.setVerticalGroup(
            panelCustomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCustomLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(buttonConfigureOutputs))
        );

        panelOutputTypes.add(panelCustom, "cardCustom");

        jLabel12.setText("Optional:");

        buttonCustomiseOutputNames.setText("Customise names");
        buttonCustomiseOutputNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomiseOutputNamesActionPerformed(evt);
            }
        });

        jLabel13.setText("Host (IP address or name):");

        fieldHostName.setText("localhost");

        jLabel14.setText("Port:");

        fieldSendPort.setText("12000");
        fieldSendPort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldSendPortKeyTyped(evt);
            }
        });

        buttonChooseOutputFile.setText("Load from file");
        buttonChooseOutputFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonChooseOutputFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelOutputTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldOutputOSCMessage))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldHostName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldSendPort, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldNumOutputs, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCustomiseOutputNames)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonChooseOutputFile))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboOutputType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(fieldOutputOSCMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(fieldHostName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldSendPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(fieldNumOutputs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(buttonCustomiseOutputNames)
                    .addComponent(buttonChooseOutputFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(comboOutputType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(panelOutputTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        buttonNext.setText("Next >");
        buttonNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonNext)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(buttonNext)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 545, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 469, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fieldOscPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldOscPortActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldOscPortActionPerformed

    private void fieldOscPortKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldOscPortKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldOscPortKeyTyped

    private void buttonOscListenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOscListenActionPerformed
        if (w.getOSCReceiver().getConnectionState()
                == OSCReceiver.ConnectionState.CONNECTED) {
            w.getOSCReceiver().stopListening();
        } else {
            int port = 0;
            try {
                port = Integer.parseInt(fieldOscPort.getText());
            } catch (NumberFormatException ex) {
                Util.showPrettyErrorPane(this, "Port must be a valid integer greater than 0");
                return;
            }
            if (port <= 0) {
                Util.showPrettyErrorPane(this, "Port must be a valid integer greater than 0");
                return;
            }

            w.getOSCReceiver().setReceivePort(port);
            w.getOSCReceiver().startListening();
        }
    }//GEN-LAST:event_buttonOscListenActionPerformed

    private void fieldNumInputsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldNumInputsKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldNumInputsKeyTyped

    private void fieldInputOSCMessageFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldInputOSCMessageFocusLost
        updateOSCListener();
    }//GEN-LAST:event_fieldInputOSCMessageFocusLost

    private void fieldInputOSCMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldInputOSCMessageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldInputOSCMessageActionPerformed

    private void fieldInputOSCMessageKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldInputOSCMessageKeyTyped
        /* char enter = evt.getKeyChar();

         if (enter == '\n') {
         System.out.println("One");
         } else if (enter == '\r') {
         System.out.println("two");
         } else {
         System.out.println("Other: " + enter + ".");
         }
         */
        /* if (keyIsEnter) {
         updateOSCListener();
         }*/
    }//GEN-LAST:event_fieldInputOSCMessageKeyTyped

    private void initFormForInputGroup(OSCInputGroup group) {
        fieldInputOSCMessage.setText(group.getOscMessage());
        fieldNumInputs.setText(Integer.toString(group.getNumInputs()));
        currentInputNames = new String[group.getNumInputs()];
        System.arraycopy(group.getInputNames(), 0, currentInputNames, 0, currentInputNames.length);

    }

    private void buttonLoadInputsFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadInputsFromFileActionPerformed
        String homeDir = System.getProperty("user.home");
        File f = Util.findLoadFile("xml", "Input configuration file", homeDir, this);
        if (f == null) {
            return;
        }
        try {
            OSCInputGroup inputGroup = OSCInputGroup.readFromFile(f.getAbsolutePath());
            initFormForInputGroup(inputGroup);
        } catch (Exception ex) {
            Util.showPrettyErrorPane(this, "Could not load inputs from file " + f.getAbsolutePath() + ". Error: " + ex.getMessage());
            Logger.getLogger(InitInputOutputFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonLoadInputsFromFileActionPerformed

    private void buttonCustomiseInputNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomiseInputNamesActionPerformed
        //String = fieldNumInputs.ge
        if (!checkInputNumberValid()) {
            return;
        }
        int numNames = Integer.parseInt(fieldNumInputs.getText());

        GuiIONameCustomise.NamesListReceiver r = new GuiIONameCustomise.NamesListReceiver() {
            @Override
            public void setNames(String[] names) {
                receivedNewInputNames(names);
            }
        };

        String baseName = setBaseNameFromOscField(fieldInputOSCMessage, "Input");
        GuiIONameCustomise customiser = new GuiIONameCustomise(
                numNames,
                baseName,
                currentInputNames,
                r,
                GuiIONameCustomise.IOType.INPUT);
        customiser.setAlwaysOnTop(true);
        customiser.setVisible(true);
    }//GEN-LAST:event_buttonCustomiseInputNamesActionPerformed

    private boolean customConfigMatchesGUI() {
        boolean matches = false;
        if (comboOutputType.getSelectedIndex() != COMBO_CUSTOM_INDEX) {
            return true;
        }
        if (customConfiguredOutput == null) {
            Util.showPrettyErrorPane(this, "You have selected a custom set of output types. Please hit \"Configure\" to configure these outputs.");
            return false;
        }
        int numOutputsOnForm;
        try {
            numOutputsOnForm = Integer.parseInt(fieldNumOutputs.getText());
        } catch (NumberFormatException ex) {
            Util.showPrettyErrorPane(this, "The number of outputs must match the custom configuration entered using \"Configure\" or loaded from a file.");
            return false;
        }

        if (customConfiguredOutput.getNumOutputs() != numOutputsOnForm) {
            Util.showPrettyErrorPane(this, "The number of OSC outputs must match the custom configuration entered using \"Configure\" or loaded from a file. Hit \"Configure\" to fix this.");
            return false;
        } else {
            return true;
        }
    }

    /* private void updateCustomOutputPermissions() {
     System.out.println("Computing");
     if (comboOutputType.getSelectedIndex() != COMBO_CUSTOM_INDEX) {
     buttonNext.setEnabled(true);
     return;
     }
     if (customConfiguredOutput == null) {
     buttonNext.setEnabled(false);
     return;
     }
     int numOutputsOnForm;
     try {
     numOutputsOnForm = Integer.parseInt(fieldNumOutputs.getText());
     } catch (NumberFormatException ex) {
     buttonNext.setEnabled(false);
     return;
     }
        
     if (customConfiguredOutput.getNumOutputs() != numOutputsOnForm) {
     buttonNext.setEnabled(false);
     } else {
     buttonNext.setEnabled(true);
     }
     } */

    private void fieldNumOutputsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldNumOutputsKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldNumOutputsKeyTyped

    private void fieldOutputOSCMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldOutputOSCMessageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldOutputOSCMessageActionPerformed

    private void comboOutputTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboOutputTypeActionPerformed
        updateOutputCard();
    }//GEN-LAST:event_comboOutputTypeActionPerformed

    private void fieldNumClassesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldNumClassesKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldNumClassesKeyTyped

    private void buttonCustomiseOutputNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomiseOutputNamesActionPerformed
        if (!Util.checkIsPositiveNumber(fieldNumOutputs, "Number of outputs", this)) {
            return;
        }
        int numNames = Integer.parseInt(fieldNumOutputs.getText());

        GuiIONameCustomise.NamesListReceiver r = new GuiIONameCustomise.NamesListReceiver() {
            @Override
            public void setNames(String[] names) {
                receivedNewOutputNames(names);
            }
        };

        String baseName = setBaseNameFromOscField(fieldOutputOSCMessage, "Output");
        GuiIONameCustomise customiser = new GuiIONameCustomise(
                numNames,
                baseName,
                currentOutputNames,
                r,
                GuiIONameCustomise.IOType.OUTPUT);
        customiser.setAlwaysOnTop(true);
        customiser.setVisible(true);
    }//GEN-LAST:event_buttonCustomiseOutputNamesActionPerformed

    private void fieldSendPortKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldSendPortKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldSendPortKeyTyped

    private OSCOutputGroup getOutputGroupFromForm() {
        String name = "Outputs";
        String oscMessage = fieldOutputOSCMessage.getText().trim();
        String hostname = getHostnameFromForm();
        int port = getSendPortFromForm();
        int numOutputs = Integer.parseInt(fieldNumOutputs.getText());

        if (currentOutputNames.length != numOutputs) {
            if (currentOutputNames.length > numOutputs) {
                String[] newNames = new String[numOutputs];
                System.arraycopy(currentOutputNames, 0, newNames, 0, numOutputs);
                currentOutputNames = newNames;
            } else { //We need to add some new names
                String[] newNames = new String[numOutputs];
                System.arraycopy(currentOutputNames, 0, newNames, 0, currentOutputNames.length);
                String baseName = setBaseNameFromOscField(fieldOutputOSCMessage, "Output");
                for (int i = currentOutputNames.length; i < numOutputs; i++) {
                    newNames[i] = baseName + "-" + (i + 1);
                }
                currentOutputNames = newNames;
            }
        }

        if (comboOutputType.getSelectedIndex() == COMBO_CLASSIFICATION_INDEX) {
            List<OSCOutput> outputs = new LinkedList<>();
            int numClasses = Integer.parseInt(fieldNumClasses.getText());
            for (int i = 0; i < numOutputs; i++) {
                OSCClassificationOutput o = new OSCClassificationOutput(currentOutputNames[i], numClasses);
                outputs.add(o);
            }
            OSCOutputGroup og = new OSCOutputGroup(outputs, oscMessage, hostname, port);
            return og;
        } else if (comboOutputType.getSelectedIndex() == COMBO_REGRESSION_INDEX) {
            List<OSCOutput> outputs = new LinkedList<>();
            for (int i = 0; i < numOutputs; i++) {
                OSCNumericOutput o = new OSCNumericOutput(
                        currentOutputNames[i],
                        0,
                        1,
                        OSCNumericOutput.NumericOutputType.REAL,
                        OSCNumericOutput.LimitType.SOFT);
                outputs.add(o);
            }
            OSCOutputGroup og = new OSCOutputGroup(outputs, oscMessage, hostname, port);
            return og;
        } else {
            List<OSCOutput> outputs = customConfiguredOutput.getOutputs();
            OSCOutputGroup og = new OSCOutputGroup(outputs, oscMessage, hostname, port);
            return og;
        }
    }

    private OSCInputGroup getInputGroupFromForm() {
        String name = "Inputs";
        String oscMessage = fieldInputOSCMessage.getText().trim();
        int numInputs = Integer.parseInt(fieldNumInputs.getText());

        if (currentInputNames.length != numInputs) {
            if (currentInputNames.length > numInputs) {
                String[] newNames = new String[numInputs];
                System.arraycopy(currentInputNames, 0, newNames, 0, numInputs);
                currentInputNames = newNames;
            } else { //We need to add some new names
                String[] newNames = new String[numInputs];
                System.arraycopy(currentInputNames, 0, newNames, 0, currentInputNames.length);
                String baseName = setBaseNameFromOscField(fieldInputOSCMessage, "Input");
                for (int i = currentInputNames.length; i < numInputs; i++) {
                    newNames[i] = baseName + "-" + (i + 1);
                }
                currentInputNames = newNames;
            }
        }
        OSCInputGroup ig = new OSCInputGroup(name, oscMessage, numInputs, currentInputNames);
        return ig;
    }

    private String getHostnameFromForm() {
        return fieldHostName.getText().trim();
    }

    private int getSendPortFromForm() {
        return Integer.parseInt(fieldSendPort.getText());
    }

    private void configureOSCSenderFromForm() throws UnknownHostException, SocketException {
        String hostName = getHostnameFromForm();
        int port = getSendPortFromForm();
        w.getOSCSender().setHostnameAndPort(InetAddress.getByName(hostName), port);
    }

    private void buttonNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNextActionPerformed
        //TODO: have to do more if configuringOSC on next screen...
        if (checkOSCReady() && checkInputReady() && checkOutputReady() && customConfigMatchesGUI()) {
            //System.out.println("READY TO GO");
            try {
                configureOSCSenderFromForm();

                OSCInputGroup inputGroup = getInputGroupFromForm();
                OSCOutputGroup outputGroup = getOutputGroupFromForm();
                w.getInputManager().setOSCInputGroup(inputGroup);
                w.getOutputManager().setOSCOutputGroup(outputGroup);
                w.getLearningManager().initializeInputsAndOutputs();
                if (comboOutputType.getSelectedIndex() == COMBO_CLASSIFICATION_INDEX) {
                    int whichClassifier = comboClassifierType.getSelectedIndex();
                    ModelBuilder mb;
                    if (whichClassifier == COMBO_KNN_INDEX) {
                        mb = new KNNModelBuilder();
                    } else if (whichClassifier == COMBO_ADABOOST_INDEX) {
                        mb = new AdaboostModelBuilder();
                    } else if (whichClassifier == COMBO_SVM_INDEX) {
                        mb = new SVMModelBuilder();
                    } else if (whichClassifier == COMBO_J48_INDEX) {
                        mb = new J48ModelBuilder();
                    } else {
                        mb = new KNNModelBuilder();
                        logger.log(Level.WARNING, "Classifier index not found");
                    }
                    for (int i = 0; i < outputGroup.getNumOutputs(); i++) {
                        ModelBuilder mbnew = mb.fromTemplate(mb);
                        w.getLearningManager().setModelBuilderForPath(mbnew, i);
                    }
                    

                }
                w.getMainGUI().initializeInputsAndOutputs();
                w.getMainGUI().setVisible(true);
                this.dispose();
            } catch (UnknownHostException ex) {
                Util.showPrettyErrorPane(this, "Host name " + fieldHostName.getText() + " is invalid; please try a different host.");
            } catch (SocketException ex) {
                Util.showPrettyErrorPane(this, "Error setting up OSC sender: " + ex.getMessage());
            }

        } else {
            System.out.println("ERROR SOMEWHERE");
        }
    }//GEN-LAST:event_buttonNextActionPerformed

    private void buttonConfigureOutputsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfigureOutputsActionPerformed
        if (outputConfigViewer != null) {
            outputConfigViewer.toFront();
            return;
        }

        String sendMsg = fieldOutputOSCMessage.getText().trim();
        String hostname = getHostnameFromForm();
        int port = getSendPortFromForm();
        String baseName = setBaseNameFromOscField(fieldOutputOSCMessage, "output");
        int numOutputs = Integer.parseInt(fieldNumOutputs.getText());
        List<OSCOutput> existingOutputs = null;
        if (customConfiguredOutput != null) {
            List<OSCOutput> customConfigOutputs = customConfiguredOutput.getOutputs();
            if (customConfigOutputs.size() == numOutputs) {
                existingOutputs = customConfigOutputs;
            } else {
                existingOutputs = new LinkedList<>();
                for (int i = 0; (i < customConfigOutputs.size() && i < numOutputs); i++) {
                    existingOutputs.add(customConfigOutputs.get(i));
                }

            }
        }

        outputConfigViewer = new OutputConfigurationFrame(w, sendMsg, hostname, port, numOutputs, existingOutputs, currentOutputNames, baseName, outputGroupReceiver);

        outputConfigViewer.setAlwaysOnTop(true);
        outputConfigViewer.setVisible(true);
        outputConfigViewer.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                outputConfigViewer = null;
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

    }//GEN-LAST:event_buttonConfigureOutputsActionPerformed

    private void buttonChooseOutputFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonChooseOutputFileActionPerformed
        String homeDir = System.getProperty("user.home");
        File f = Util.findLoadFile("xml", "Output configuration file", homeDir, this);
        if (f == null) {
            return;
        }
        try {
            OSCOutputGroup outputGroup = OSCOutputGroup.readFromFile(f.getAbsolutePath());
            initFormForOutputGroup(outputGroup);
        } catch (Exception ex) {
            Util.showPrettyErrorPane(this, "Could not load outputs from file " + f.getAbsolutePath() + ". Error: " + ex.getMessage());
            Logger.getLogger(InitInputOutputFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonChooseOutputFileActionPerformed

    private void fieldNumOutputsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldNumOutputsActionPerformed
    }//GEN-LAST:event_fieldNumOutputsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InitInputOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InitInputOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InitInputOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InitInputOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Wekinator w;
                try {
                    w = new Wekinator();
                    InitInputOutputFrame p = new InitInputOutputFrame(w);
                    p.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(InitInputOutputFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(InitInputOutputFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });
    }

    private boolean checkOutputNumberValid() {
        return Util.checkIsPositiveNumber(fieldNumOutputs, "Number of outputs", this);
    }

    private boolean checkOutputHostValid() {
        boolean isNotBlank = Util.checkNotBlank(fieldHostName, "host name", this);
        if (!isNotBlank) {
            return false;
        }

        String hostname = fieldHostName.getText().trim();
        try {
            InetAddress address = InetAddress.getByName(hostname);
        } catch (UnknownHostException ex) {
            Util.showPrettyErrorPane(this, "Invalid OSC output hostname");
            return false;
        }
        return true;
    }

    private boolean checkOutputPortValid() {
        return Util.checkIsPositiveNumber(fieldSendPort, "OSC output port", this);
    }

    private boolean checkOutputOSCValid() {
        if (!Util.checkNotBlank(fieldOutputOSCMessage, "OSC output message", this)) {
            return false;
        }
        return Util.checkNoSpace(fieldOutputOSCMessage, "OSC output message", this);
    }

    private boolean checkInputNumberValid() {
        return Util.checkIsPositiveNumber(fieldNumInputs, "Number of inputs", this);
    }

    private String setBaseNameFromOscField(JTextField f, String defaultName) {
        String currentInputOSC = f.getText().trim();
        String baseName = defaultName;
        if (currentInputOSC.length() > 0) {
            if (!currentInputOSC.contains("/")) {
                baseName = currentInputOSC;
            } else {
                String[] s = currentInputOSC.split("/");
                if (s.length > 0 && s[s.length - 1].length() > 0) {
                    baseName = s[s.length - 1];
                }
            }
        }
        return baseName;
    }

    private void receivedNewInputNames(String[] names) {
        currentInputNames = new String[names.length];
        System.arraycopy(names, 0, currentInputNames, 0, names.length);
    }

    private void receivedNewOutputNames(String[] names) {
        currentOutputNames = new String[names.length];
        System.arraycopy(names, 0, currentOutputNames, 0, names.length);
    }

    private boolean checkOSCReady() {
        boolean ready = (w != null && w.getOSCReceiver().getConnectionState() == OSCReceiver.ConnectionState.CONNECTED);
        if (!ready) {
            Util.showPrettyErrorPane(this, "Please start OSC listener above in order to proceed");
        }
        return ready;
    }

    private boolean checkInputReady() {
        return checkInputNumberValid() && checkOscInputValid();
    }

    private boolean checkOscInputValid() {
        boolean notBlank = Util.checkNotBlank(fieldInputOSCMessage, "Input OSC message", this);
        if (notBlank) {
            return Util.checkNoSpace(fieldInputOSCMessage, "Input OSC message", this);
        } else {
            return false;
        }
    }

    private boolean checkOutputReady() {
        boolean ok = checkOutputNumberValid() && checkOutputHostValid() && checkOutputPortValid() && checkOutputOSCValid();
        if (!ok) {
            return false;
        }
        int index = comboOutputType.getSelectedIndex();
        if (index == COMBO_CLASSIFICATION_INDEX) {
            return Util.checkIsPositiveNumber(fieldNumClasses, "Number of classes", this);
        }
        return true;
    }

    private void updateOSCListener() {
        System.out.println("ERROR: updateOSCListener is not implemented");
    }

    private void updateOutputCard() {
        int index = comboOutputType.getSelectedIndex();
        CardLayout layout = (CardLayout) panelOutputTypes.getLayout();
        if (index == COMBO_REGRESSION_INDEX) {
            layout.show(panelOutputTypes, "cardBlank");
        } else if (index == COMBO_CLASSIFICATION_INDEX) {
            layout.show(panelOutputTypes, "cardClassification");
        } else {
            layout.show(panelOutputTypes, "cardCustom");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonChooseOutputFile;
    private javax.swing.JButton buttonConfigureOutputs;
    private javax.swing.JButton buttonCustomiseInputNames;
    private javax.swing.JButton buttonCustomiseOutputNames;
    private javax.swing.JButton buttonLoadInputsFromFile;
    private javax.swing.JButton buttonNext;
    private javax.swing.JButton buttonOscListen;
    private javax.swing.JPanel cardBlank;
    private javax.swing.JPanel cardNumClasses;
    private javax.swing.JComboBox comboClassifierType;
    private javax.swing.JComboBox comboOutputType;
    private javax.swing.JTextField fieldHostName;
    private javax.swing.JTextField fieldInputOSCMessage;
    private javax.swing.JTextField fieldNumClasses;
    private javax.swing.JTextField fieldNumInputs;
    private javax.swing.JTextField fieldNumOutputs;
    private javax.swing.JTextField fieldOscPort;
    private javax.swing.JTextField fieldOutputOSCMessage;
    private javax.swing.JTextField fieldSendPort;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labelOscStatus;
    private javax.swing.JPanel panelCustom;
    private javax.swing.JPanel panelOutputTypes;
    // End of variables declaration//GEN-END:variables

}
