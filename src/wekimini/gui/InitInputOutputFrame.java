/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Button;
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
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import wekimini.LearningModelBuilder;
import wekimini.WekiMiniRunner;
import wekimini.WekiMiniRunner.Closeable;
import wekimini.Wekinator;
import wekimini.WekinatorController;
import wekimini.WekinatorFileData;
import wekimini.learning.AdaboostModelBuilder;
import wekimini.learning.J48ModelBuilder;
import wekimini.learning.KNNModelBuilder;
import wekimini.learning.LearningAlgorithmRegistry;
import wekimini.learning.ModelBuilder;
import wekimini.learning.SVMModelBuilder;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCDtwOutput;
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
public class InitInputOutputFrame extends javax.swing.JFrame implements Closeable {

    private Wekinator w = null;
    private PropertyChangeListener oscReceiverListener = null;
    private final WeakListenerSupport wls = new WeakListenerSupport();
    private String[] currentInputNames = new String[0];
    private String[] currentOutputNames = new String[0];

    private final static int COMBO_REGRESSION_INDEX = 0;
    private final static int COMBO_CLASSIFICATION_INDEX = 1;
    private final static int COMBO_DTW_INDEX = 2;
    private final static int COMBO_CUSTOM_INDEX = 3;

    private final static int COMBO_KNN_INDEX = 0;
    private final static int COMBO_ADABOOST_INDEX = 2;
    private final static int COMBO_SVM_INDEX = 1;
    private final static int COMBO_J48_INDEX = 3;
    
    private int lastNumOutputs = 5;

    private final ButtonGroup classificationRadioGroup = new ButtonGroup();
    private LearningModelBuilder[] classificationModelBuilders;
    private JRadioButtonMenuItem[] classificationRadios;

    private final ButtonGroup regressionRadioGroup = new ButtonGroup();
    private LearningModelBuilder[] regressionModelBuilders;
    private JRadioButtonMenuItem[] regressionRadios;

    private final WekinatorController.NamesListener inputNamesListener;
    private final WekinatorController.NamesListener outputNamesListener;

    private OutputConfigurationFrame outputConfigViewer = null;
    private GuiIONameCustomise inputCustomiser = null;
    private GuiIONameCustomise outputCustomiser = null;

    private ModelBuilder[] customModelBuilders = null;
    private boolean isCustomDtw = false;

    //private final OutputConfigurationFrame.OutputGroupReceiver outputGroupReceiver = this::initFormForOutputGroup;
    private final OutputConfigurationFrame.OutputConfigurationReceiver outputGroupReceiver = new OutputConfigurationFrame.OutputConfigurationReceiver() {
        @Override
        public void outputConfigurationReady(OSCOutputGroup g, ModelBuilder[] mb) {
            initFormForOutputGroup(g);
            customModelBuilders = mb;
        }
    };

    private OSCOutputGroup customConfiguredOutput = null;
    private static final Logger logger = Logger.getLogger(InitInputOutputFrame.class.getName());

    private boolean isCloseable = false;

    /**
     * Not used: Just for GUI design Creates new form initInputOutputFrame
     */
    public InitInputOutputFrame() {
        initComponents();
        inputNamesListener = null;
        outputNamesListener = null;
    }

    public InitInputOutputFrame(Wekinator w) {
        initComponents();
        setWekinator(w);
        setupAlgorithmChoices();
        updateOutputCard();
        updateOutputOptions();
        inputNamesListener = new WekinatorController.NamesListener() {

            @Override
            public void newNamesReceived(String[] names) {
                receivedInputNamesFromOSC(names);
            }

        };

        outputNamesListener = new WekinatorController.NamesListener() {

            @Override
            public void newNamesReceived(String[] names) {
                receivedOutputNamesFromOSC(names);
            }

        };

        w.getWekinatorController().addInputNamesListener(inputNamesListener);
        w.getWekinatorController().addOutputNamesListener(outputNamesListener);

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

    private void setupAlgorithmChoices() {
        makeClassificationRadioGroup();
        makeRegressionRadioGroup();
    }

    private void makeClassificationRadioGroup() {
        classificationModelBuilders = LearningAlgorithmRegistry.getClassificationModelBuilders();
        classificationRadios = new JRadioButtonMenuItem[classificationModelBuilders.length];
        for (int i = 0; i < classificationModelBuilders.length; i++) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem();
            button.setText(classificationModelBuilders[i].getPrettyName());
            classificationRadios[i] = button;
            if (i == 0) {
                classificationRadios[i].setSelected(true);
            }
            classificationRadioGroup.add(classificationRadios[i]);
        }
    }

    private void makeRegressionRadioGroup() {
        regressionModelBuilders = LearningAlgorithmRegistry.getNumericModelBuilders();
        regressionRadios = new JRadioButtonMenuItem[regressionModelBuilders.length];
        for (int i = 0; i < regressionModelBuilders.length; i++) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem();
            button.setText(regressionModelBuilders[i].getPrettyName());
            regressionRadios[i] = button;
            if (i == 0) {
                regressionRadios[i].setSelected(true);
            }
            regressionRadioGroup.add(regressionRadios[i]);
        }
    }

    private void receivedInputNamesFromOSC(String[] names) {
        receivedNewInputNames(names);
        fieldNumInputs.setText(Integer.toString(names.length));
        //What if customisation box already open??
        if (inputCustomiser != null) {
            if (names.length == inputCustomiser.getNumNames()) {
                inputCustomiser.setNames(names);
            } else {
                inputCustomiser.dispose();
                //Make new one
                customiseInputNames();
            }
        }
    }

    private void receivedOutputNamesFromOSC(String[] names) {
        receivedNewOutputNames(names);
        fieldNumOutputs.setText(Integer.toString(names.length));
        //What if customisation box already open??
        if (outputConfigViewer != null) {
            outputConfigViewer.adjustForNewNames(names);
        }
        if (outputCustomiser != null) {
            if (names.length == outputCustomiser.getNumNames()) {
                outputCustomiser.setNames(names);
            } else {
                outputCustomiser.dispose();
                customiseOutputNames();
            }
        }
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
        //oscReceiverListener = this::oscReceiverPropertyChanged;
        oscReceiverListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oscReceiverPropertyChanged(evt);
            }
        };

        w.getOSCReceiver().addPropertyChangeListener(wls.propertyChange(oscReceiverListener));
    }

    private void updateGUIForConnectionState(OSCReceiver.ConnectionState cs) {
        if (cs == OSCReceiver.ConnectionState.CONNECTED) {
            labelOscStatus.setText("Listening on port " + w.getOSCReceiver().getReceivePort());
            buttonOscListen.setText("Stop listening");
            //  buttonNext.setEnabled(true);
        } else if (cs == OSCReceiver.ConnectionState.FAIL) {
            labelOscStatus.setText("Failed to start listener");
            buttonOscListen.setText("Start listening");
            //  buttonNext.setEnabled(false);
        } else if (cs == OSCReceiver.ConnectionState.NOT_CONNECTED) {
            labelOscStatus.setText("Not listening");
            buttonOscListen.setText("Start listening");
            //  buttonNext.setEnabled(false);
        } else if (cs == OSCReceiver.ConnectionState.CONNECTING) {
            labelOscStatus.setText("Connecting...");
            buttonOscListen.setText("Stop listening");
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

        popupMenuInputOptions = new javax.swing.JPopupMenu();
        menuCustomiseInputNames = new javax.swing.JMenuItem();
        menuLoadFromFile = new javax.swing.JMenuItem();
        popupMenuOutputOptions = new javax.swing.JPopupMenu();
        menuCustomiseOutputNames = new javax.swing.JMenuItem();
        menuLoadOutputFromFile = new javax.swing.JMenuItem();
        menuChooseAlgorithm = new javax.swing.JMenu();
        buttonKNN = new javax.swing.JRadioButtonMenuItem();
        buttonAdaboost = new javax.swing.JRadioButtonMenuItem();
        buttonSVM = new javax.swing.JRadioButtonMenuItem();
        buttonJ48 = new javax.swing.JRadioButtonMenuItem();
        classifierRadioGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fieldOscPort = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        buttonOscListen = new javax.swing.JButton();
        labelOscStatus = new javax.swing.JLabel();
        panelInputs = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        fieldNumInputs = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        fieldInputOSCMessage = new javax.swing.JTextField();
        buttonInputOptions = new javax.swing.JButton();
        panelOutputs = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        fieldNumOutputs = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        fieldOutputOSCMessage = new javax.swing.JTextField();
        comboOutputType = new javax.swing.JComboBox();
        panelOutputTypes = new javax.swing.JPanel();
        cardBlank = new javax.swing.JPanel();
        cardNumClasses = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        fieldNumClasses = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        panelCustom = new javax.swing.JPanel();
        buttonConfigureOutputs = new javax.swing.JButton();
        cardDTW = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        fieldNumDtwTypes = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        buttonOutputOptions = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        fieldHostName = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        fieldSendPort = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        buttonNext = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuItemNewProject = new javax.swing.JMenuItem();
        menuItemOpenProject = new javax.swing.JMenuItem();

        menuCustomiseInputNames.setText("Customise names");
        menuCustomiseInputNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCustomiseInputNamesActionPerformed(evt);
            }
        });
        popupMenuInputOptions.add(menuCustomiseInputNames);

        menuLoadFromFile.setText("Load from file");
        menuLoadFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLoadFromFileActionPerformed(evt);
            }
        });
        popupMenuInputOptions.add(menuLoadFromFile);

        menuCustomiseOutputNames.setText("Customise names");
        menuCustomiseOutputNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCustomiseOutputNamesActionPerformed(evt);
            }
        });
        popupMenuOutputOptions.add(menuCustomiseOutputNames);

        menuLoadOutputFromFile.setText("Load from file");
        menuLoadOutputFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLoadOutputFromFileActionPerformed(evt);
            }
        });
        popupMenuOutputOptions.add(menuLoadOutputFromFile);

        menuChooseAlgorithm.setText("Choose algorithm");
        menuChooseAlgorithm.setEnabled(false);

        classifierRadioGroup.add(buttonKNN);
        buttonKNN.setSelected(true);
        buttonKNN.setText("k-Nearest Neighbor");
        buttonKNN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonKNNActionPerformed(evt);
            }
        });
        menuChooseAlgorithm.add(buttonKNN);

        classifierRadioGroup.add(buttonAdaboost);
        buttonAdaboost.setText("AdaBoost");
        menuChooseAlgorithm.add(buttonAdaboost);

        classifierRadioGroup.add(buttonSVM);
        buttonSVM.setText("Support Vector Machine");
        menuChooseAlgorithm.add(buttonSVM);

        classifierRadioGroup.add(buttonJ48);
        buttonJ48.setText("Decision Tree");
        menuChooseAlgorithm.add(buttonJ48);

        popupMenuOutputOptions.add(menuChooseAlgorithm);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Create new project");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

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
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelOscStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fieldOscPort, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(buttonOscListen))
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
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fieldOscPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(buttonOscListen)
                .addContainerGap())
        );

        panelInputs.setBackground(new java.awt.Color(255, 255, 255));
        panelInputs.setBorder(javax.swing.BorderFactory.createTitledBorder("Inputs"));

        jLabel3.setText("# inputs:");

        fieldNumInputs.setText("5");
        fieldNumInputs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldNumInputsKeyTyped(evt);
            }
        });

        jLabel4.setText("OSC message:");

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

        buttonInputOptions.setText("Options");
        buttonInputOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonInputOptionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelInputsLayout = new javax.swing.GroupLayout(panelInputs);
        panelInputs.setLayout(panelInputsLayout);
        panelInputsLayout.setHorizontalGroup(
            panelInputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInputsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldInputOSCMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldNumInputs, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonInputOptions))
        );
        panelInputsLayout.setVerticalGroup(
            panelInputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInputsLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(panelInputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(fieldInputOSCMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldNumInputs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(buttonInputOptions)))
        );

        panelOutputs.setBackground(new java.awt.Color(255, 255, 255));
        panelOutputs.setBorder(javax.swing.BorderFactory.createTitledBorder("Outputs"));

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

        jLabel6.setText("OSC message:");

        fieldOutputOSCMessage.setText("/wek/outputs");
        fieldOutputOSCMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldOutputOSCMessageActionPerformed(evt);
            }
        });

        comboOutputType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All continuous (default settings)", "All classifiers (default settings)", "All dynamic time warping (default settings)", "Custom" }));
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
            .addGap(0, 534, Short.MAX_VALUE)
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

        jLabel10.setText("classes");

        javax.swing.GroupLayout cardNumClassesLayout = new javax.swing.GroupLayout(cardNumClasses);
        cardNumClasses.setLayout(cardNumClassesLayout);
        cardNumClassesLayout.setHorizontalGroup(
            cardNumClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardNumClassesLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldNumClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addContainerGap(353, Short.MAX_VALUE))
        );
        cardNumClassesLayout.setVerticalGroup(
            cardNumClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardNumClassesLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(cardNumClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(fieldNumClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)))
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
                .addGap(38, 38, 38)
                .addComponent(buttonConfigureOutputs)
                .addContainerGap(390, Short.MAX_VALUE))
        );
        panelCustomLayout.setVerticalGroup(
            panelCustomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCustomLayout.createSequentialGroup()
                .addComponent(buttonConfigureOutputs)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        panelOutputTypes.add(panelCustom, "cardCustom");

        cardDTW.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setText("with");

        fieldNumDtwTypes.setText("5");
        fieldNumDtwTypes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldNumDtwTypesKeyTyped(evt);
            }
        });

        jLabel12.setText("gesture types");

        javax.swing.GroupLayout cardDTWLayout = new javax.swing.GroupLayout(cardDTW);
        cardDTW.setLayout(cardDTWLayout);
        cardDTWLayout.setHorizontalGroup(
            cardDTWLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDTWLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldNumDtwTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addContainerGap(314, Short.MAX_VALUE))
        );
        cardDTWLayout.setVerticalGroup(
            cardDTWLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDTWLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(cardDTWLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(fieldNumDtwTypes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)))
        );

        panelOutputTypes.add(cardDTW, "cardDTW");

        buttonOutputOptions.setText("Options");
        buttonOutputOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOutputOptionsActionPerformed(evt);
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

        jLabel7.setText("Type:");

        javax.swing.GroupLayout panelOutputsLayout = new javax.swing.GroupLayout(panelOutputs);
        panelOutputs.setLayout(panelOutputsLayout);
        panelOutputsLayout.setHorizontalGroup(
            panelOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOutputsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOutputsLayout.createSequentialGroup()
                        .addGroup(panelOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelOutputTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panelOutputsLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(0, 0, 0)
                                .addComponent(fieldOutputOSCMessage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)
                                .addGap(0, 0, 0)
                                .addComponent(fieldNumOutputs, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelOutputsLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addGap(0, 0, 0)
                                .addComponent(fieldHostName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14)
                                .addGap(0, 0, 0)
                                .addComponent(fieldSendPort, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(panelOutputsLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 0, 0)
                        .addComponent(comboOutputType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonOutputOptions))))
        );
        panelOutputsLayout.setVerticalGroup(
            panelOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOutputsLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(panelOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(fieldOutputOSCMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldNumOutputs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(0, 0, 0)
                .addGroup(panelOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(fieldHostName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fieldSendPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGap(0, 0, 0)
                .addGroup(panelOutputsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonOutputOptions)
                    .addComponent(comboOutputType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
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
                    .addComponent(panelOutputs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelInputs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(panelInputs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelOutputs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(buttonNext)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        menuItemNewProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.META_MASK));
        menuItemNewProject.setText("New project");
        menuItemNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemNewProjectActionPerformed(evt);
            }
        });
        jMenu1.add(menuItemNewProject);

        menuItemOpenProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        menuItemOpenProject.setText("Open project...");
        menuItemOpenProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOpenProjectActionPerformed(evt);
            }
        });
        jMenu1.add(menuItemOpenProject);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 586, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 325, Short.MAX_VALUE)
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

    private void tryToStartListening() {
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
    }
    
    private void buttonOscListenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOscListenActionPerformed
        tryToStartListening();
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

    private void loadInputsFromFile() {
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
    }

    private void customiseInputNames() {
        if (!checkInputNumberValid()) {
            return;
        }
        int numNames = Integer.parseInt(fieldNumInputs.getText());

        GuiIONameCustomise.NamesListReceiver r = new GuiIONameCustomise.NamesListReceiver() {
            @Override
            public void setNames(String[] names) {
                receivedNewInputNames(names);
                inputCustomiser = null;
            }

            @Override
            public void cancel() {
                inputCustomiser = null;
            }
        };

        String baseName = setBaseNameFromOscField(fieldInputOSCMessage, "Input");
        inputCustomiser = new GuiIONameCustomise(
                numNames,
                baseName,
                currentInputNames,
                r,
                GuiIONameCustomise.IOType.INPUT);
        inputCustomiser.setAlwaysOnTop(true);
        inputCustomiser.setVisible(true);
        /*Util.callOnClosed(inputCustomiser, new Util.CallableOnClosed() {
         @Override
         public void callMe() {
         inputCustomiser = null;
         }
         }); */
    }

    private void buttonInputOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonInputOptionsActionPerformed
        //String = fieldNumInputs.ge
        //customiseInputNames();
        popupMenuInputOptions.show(panelInputs, buttonInputOptions.getX(), buttonInputOptions.getY());
    }//GEN-LAST:event_buttonInputOptionsActionPerformed

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
        updateOutputOptions();
        updateNumOutputsOption();
    }//GEN-LAST:event_comboOutputTypeActionPerformed

    private void updateNumOutputsOption() {
       int index = comboOutputType.getSelectedIndex();
       CardLayout layout = (CardLayout) panelOutputTypes.getLayout();

        if (index == COMBO_REGRESSION_INDEX || index == COMBO_CLASSIFICATION_INDEX || index == COMBO_CUSTOM_INDEX) {
          //  fieldNumOutputs.setText(Integer.toString(lastNumOutputs));
            fieldNumOutputs.setEnabled(true);
        } else {
           /* try {
                lastNumOutputs = Integer.parseInt(fieldNumOutputs.getText());
            } catch (NumberFormatException ex) {
                lastNumOutputs = 1;
            } */
            fieldNumOutputs.setText("1");
            fieldNumOutputs.setEnabled(false);
        }
    }
    
    private void updateOutputOptions() {
        int index = comboOutputType.getSelectedIndex();
        CardLayout layout = (CardLayout) panelOutputTypes.getLayout();

        if (index == COMBO_REGRESSION_INDEX) {
            menuChooseAlgorithm.setEnabled(true);
            menuChooseAlgorithm.removeAll();
            for (JRadioButtonMenuItem regressionRadio : regressionRadios) {
                menuChooseAlgorithm.add(regressionRadio);
            }
        } else if (index == COMBO_CLASSIFICATION_INDEX) {
            menuChooseAlgorithm.setEnabled(true);
            menuChooseAlgorithm.removeAll();
            for (JRadioButtonMenuItem classificationRadio : classificationRadios) {
                menuChooseAlgorithm.add(classificationRadio);
            }
        } else {
            menuChooseAlgorithm.setEnabled(false);
        }
    }

    private void fieldNumClassesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldNumClassesKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_fieldNumClassesKeyTyped

    private void customiseOutputNames() {
        if (!Util.checkIsPositiveNumber(fieldNumOutputs, "Number of outputs", this)) {
            return;
        }
        int numNames = Integer.parseInt(fieldNumOutputs.getText());

        GuiIONameCustomise.NamesListReceiver r = new GuiIONameCustomise.NamesListReceiver() {
            @Override
            public void setNames(String[] names) {
                receivedNewOutputNames(names);
                outputCustomiser = null;
            }

            @Override
            public void cancel() {
                outputCustomiser = null;
            }
        };

        String baseName = setBaseNameFromOscField(fieldOutputOSCMessage, "Output");
        outputCustomiser = new GuiIONameCustomise(
                numNames,
                baseName,
                currentOutputNames,
                r,
                GuiIONameCustomise.IOType.OUTPUT);
        outputCustomiser.setAlwaysOnTop(true);
        outputCustomiser.setVisible(true);
        /*Util.callOnClosed(outputCustomiser, new Util.CallableOnClosed() {
         @Override
         public void callMe() {
         outputCustomiser = null;
         }
         }); */
    }

    private void buttonOutputOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOutputOptionsActionPerformed
        popupMenuOutputOptions.show(panelOutputs, buttonOutputOptions.getX(), buttonOutputOptions.getY());
    }//GEN-LAST:event_buttonOutputOptionsActionPerformed

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

        setCurrentOutputNames(numOutputs);

        if (comboOutputType.getSelectedIndex() == COMBO_CLASSIFICATION_INDEX) {
            List<OSCOutput> outputs = new LinkedList<>();
            int numClasses = Integer.parseInt(fieldNumClasses.getText());
            for (int i = 0; i < numOutputs; i++) {
                OSCClassificationOutput o = new OSCClassificationOutput(currentOutputNames[i], numClasses, false);
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
        } else if (comboOutputType.getSelectedIndex() == COMBO_DTW_INDEX) {
            List<OSCOutput> outputs = new LinkedList<>();
            int numGestures = Integer.parseInt(fieldNumDtwTypes.getText());
            for (int i = 0; i < numOutputs; i++) {
                if (currentOutputNames[i].equals("outputs-1")) {
                    currentOutputNames[i] = "output";
                }
                
                OSCDtwOutput o = new OSCDtwOutput(
                        currentOutputNames[i],
                        numGestures);
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

    private void setCurrentOutputNames(int numOutputs) {
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
    }

    private OSCInputGroup getInputGroupFromForm() {
        String name = "Inputs";
        String oscMessage = fieldInputOSCMessage.getText().trim();
        int numInputs = Integer.parseInt(fieldNumInputs.getText());
        setCurrentInputNames(numInputs);
        OSCInputGroup ig = new OSCInputGroup(name, oscMessage, numInputs, currentInputNames);
        return ig;
    }

    private void setCurrentInputNames(int numInputs) {
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
        //if (checkOSCReady() && checkInputReady() && checkOutputReady() && checkNamesUnique() && customConfigMatchesGUI()) {
        if (checkInputReady() && checkOutputReady() && checkNamesUnique() && customConfigMatchesGUI()) {

            //System.out.println("READY TO GO");
            try {
                configureOSCSenderFromForm();

                OSCInputGroup inputGroup = getInputGroupFromForm();
                OSCOutputGroup outputGroup = getOutputGroupFromForm();

                int selectedIndex = comboOutputType.getSelectedIndex();
                if (selectedIndex == COMBO_CLASSIFICATION_INDEX || selectedIndex == COMBO_REGRESSION_INDEX
                        || (selectedIndex == COMBO_CUSTOM_INDEX && !isCustomDtw)) {
                    //Doing classification and/or regression 
                    w.getInputManager().setOSCInputGroup(inputGroup);
                    w.getOutputManager().setOSCOutputGroup(outputGroup);
                    w.getLearningManager().setSupervisedLearning();
                   // w.getSupervisedLearningManager().initializeInputsAndOutputs();

                    if (selectedIndex == COMBO_CLASSIFICATION_INDEX) {
                        initClassificationModelBuilders(outputGroup);
                    } else if (selectedIndex == COMBO_REGRESSION_INDEX) {
                        initRegressionModelBuilders(outputGroup);
                    } else {
                        //Custom
                        initCustomNonTemporalModelBuilders();
                    }
                    finalizeSetup();
                    if (w.getOSCReceiver().getConnectionState() != OSCReceiver.ConnectionState.CONNECTED) {
                        tryToStartListening();
                    } 
                } else {
                    if (!fieldNumOutputs.getText().trim().equals("1")) {
                        Util.showPrettyErrorPane(this, "DTW is only working for 1 output right now, sorry!");
                        return;
                    }
                    w.getInputManager().setOSCInputGroup(inputGroup);
                    w.getOutputManager().setOSCOutputGroup(outputGroup);
                    w.getLearningManager().setDtw(outputGroup);
                    if (selectedIndex == COMBO_CUSTOM_INDEX) {
                        //Custom
                        initForCustomDtw();
                    }
                    finalizeSetup();
                    if (w.getOSCReceiver().getConnectionState() != OSCReceiver.ConnectionState.CONNECTED) {
                        tryToStartListening();
                    }   
                }

            } catch (UnknownHostException ex) {
                Util.showPrettyErrorPane(this, "Host name " + fieldHostName.getText() + " is invalid; please try a different host.");
            } catch (SocketException ex) {
                Util.showPrettyErrorPane(this, "Error setting up OSC sender: " + ex.getMessage());
            }

        } else {
            logger.log(Level.INFO, "Error encountered in setting up inputs/outputs");
        }
    }//GEN-LAST:event_buttonNextActionPerformed

    private void finalizeSetup() {
        w.getMainGUI().setVisible(true);
        WekiMiniRunner.getInstance().transferControl(w, this, w.getMainGUI());
        removeListeners();
        this.dispose();
    }

    private void initClassificationModelBuilders(OSCOutputGroup outputGroup) {
        LearningModelBuilder mb = null;
        for (int i = 0; i < classificationRadios.length; i++) {
            if (classificationRadios[i].isSelected()) {
                mb = classificationModelBuilders[i];
            }
        }
        if (mb == null) {
            logger.log(Level.WARNING, "No model type is selected in GUI! Choosing default");
            mb = classificationModelBuilders[0];
        }

        for (int i = 0; i < outputGroup.getNumOutputs(); i++) {
            LearningModelBuilder mbnew = mb.fromTemplate(mb);
            logger.log(Level.INFO, "Setting model builder to" + mbnew.getPrettyName());
            w.getSupervisedLearningManager().setModelBuilderForPath(mbnew, i);
        }
    }

    private void initRegressionModelBuilders(OSCOutputGroup outputGroup) {
        LearningModelBuilder mb = null;
        for (int i = 0; i < regressionRadios.length; i++) {
            if (regressionRadios[i].isSelected()) {
                mb = regressionModelBuilders[i];
            }
        }
        if (mb == null) {
            logger.log(Level.WARNING, "No model type is selected in GUI! Choosing default");
            mb = regressionModelBuilders[0];
        }
        for (int i = 0; i < outputGroup.getNumOutputs(); i++) {
            LearningModelBuilder mbnew = mb.fromTemplate(mb);
            logger.log(Level.INFO, "Setting model builder to {0}", mbnew.getPrettyName());
            w.getSupervisedLearningManager().setModelBuilderForPath(mbnew, i);
        }
    }

    private void initCustomNonTemporalModelBuilders() {
        //TODO: Replace with custom setup object
        // XXX
        /*for (int i = 0; i < customModelBuilders.length; i++) {
            ModelBuilder mb = customModelBuilders[i];
            logger.log(Level.INFO, "Setting model builder to {0}", mb.getPrettyName());
            w.getDtwLearningManager().setModelBuilderForPath(mb, i);
        } */
    }


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

        outputConfigViewer = new OutputConfigurationFrame(w, sendMsg, hostname, port, numOutputs, existingOutputs, currentOutputNames, baseName, customModelBuilders, outputGroupReceiver);

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

    private void loadOutputsFromFile() {
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
    }

    private void fieldNumOutputsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldNumOutputsActionPerformed
    }//GEN-LAST:event_fieldNumOutputsActionPerformed

    private void menuCustomiseInputNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCustomiseInputNamesActionPerformed
        customiseInputNames();
    }//GEN-LAST:event_menuCustomiseInputNamesActionPerformed

    private void menuLoadFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoadFromFileActionPerformed
        loadInputsFromFile();
    }//GEN-LAST:event_menuLoadFromFileActionPerformed

    private void menuCustomiseOutputNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCustomiseOutputNamesActionPerformed
        customiseOutputNames();
    }//GEN-LAST:event_menuCustomiseOutputNamesActionPerformed

    private void menuLoadOutputFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoadOutputFromFileActionPerformed
        loadOutputsFromFile();
    }//GEN-LAST:event_menuLoadOutputFromFileActionPerformed

    private void menuItemNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemNewProjectActionPerformed
        WekiMiniRunner.getInstance().runNewProject();
    }//GEN-LAST:event_menuItemNewProjectActionPerformed

    private void menuItemOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOpenProjectActionPerformed
        String homeDir = System.getProperty("user.home");
        File f = Util.findLoadFile(WekinatorFileData.FILENAME_EXTENSION, "Wekinator file", homeDir, this);
        if (f != null) {
            try {
                //TODO: Check this isn't same wekinator as mine! (don't load from my same place, or from something already open...)
                WekiMiniRunner.getInstance().runFromFile(f.getAbsolutePath());
                w.close();
                removeListeners();
                this.dispose();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_menuItemOpenProjectActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // if (isCloseable) {
        if (w.getOSCReceiver().getConnectionState() == OSCReceiver.ConnectionState.CONNECTED) {
            w.getOSCReceiver().stopListening();
        }
        w.close();
        removeListeners();
        this.dispose();
        //} else {
        //do nothing
        //}

    }//GEN-LAST:event_formWindowClosing

    private void buttonKNNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonKNNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonKNNActionPerformed

    private void fieldNumDtwTypesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldNumDtwTypesKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_fieldNumDtwTypesKeyTyped

    private void removeListeners() {
        w.getWekinatorController().removeInputNamesListener(inputNamesListener);
        w.getWekinatorController().removeOutputNamesListener(outputNamesListener);
    }

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
                    w = new Wekinator(WekiMiniRunner.generateNextID());
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
        if (index == COMBO_DTW_INDEX) {
            return Util.checkIsPositiveNumber(fieldNumDtwTypes, "Number of DTW gesture types", this);

        }
        return true;
    }

    /* Requires input and output fields are properly formatted as ints */
    private boolean checkNamesUnique() {
        int numInputs = Integer.parseInt(fieldNumInputs.getText());
        int numOutputs = Integer.parseInt(fieldNumOutputs.getText());
        setCurrentInputNames(numInputs);
        setCurrentOutputNames(numOutputs);
        String[] allNames = new String[numInputs + numOutputs];
        System.arraycopy(currentInputNames, 0, allNames, 0, currentInputNames.length);
        System.arraycopy(currentOutputNames, 0, allNames, currentInputNames.length, currentOutputNames.length);
        boolean unique = Util.checkAllUnique(allNames);
        if (!unique) {
            Util.showPrettyErrorPane(this, "Input and output names must all be unique");
        }
        return unique;
    }

    public boolean isCloseable() {
        return isCloseable;
    }

    public void setCloseable(boolean isCloseable) {
        if (isCloseable) {
            // this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        } else {
            //this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        }
        this.isCloseable = isCloseable;
    }

    private void updateOSCListener() {
        logger.log(Level.WARNING, "updateOSCListener is not implemented");
    }

    private void updateOutputCard() {
        int index = comboOutputType.getSelectedIndex();
        CardLayout layout = (CardLayout) panelOutputTypes.getLayout();
        if (index == COMBO_REGRESSION_INDEX) {
            layout.show(panelOutputTypes, "cardBlank");
        } else if (index == COMBO_CLASSIFICATION_INDEX) {
            layout.show(panelOutputTypes, "cardClassification");
        } else if (index == COMBO_DTW_INDEX) {
            layout.show(panelOutputTypes, "cardDTW");
        } else if (index == COMBO_CUSTOM_INDEX) {
            layout.show(panelOutputTypes, "cardCustom");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButtonMenuItem buttonAdaboost;
    private javax.swing.JButton buttonConfigureOutputs;
    private javax.swing.JButton buttonInputOptions;
    private javax.swing.JRadioButtonMenuItem buttonJ48;
    private javax.swing.JRadioButtonMenuItem buttonKNN;
    private javax.swing.JButton buttonNext;
    private javax.swing.JButton buttonOscListen;
    private javax.swing.JButton buttonOutputOptions;
    private javax.swing.JRadioButtonMenuItem buttonSVM;
    private javax.swing.JPanel cardBlank;
    private javax.swing.JPanel cardDTW;
    private javax.swing.JPanel cardNumClasses;
    private javax.swing.ButtonGroup classifierRadioGroup;
    private javax.swing.JComboBox comboOutputType;
    private javax.swing.JTextField fieldHostName;
    private javax.swing.JTextField fieldInputOSCMessage;
    private javax.swing.JTextField fieldNumClasses;
    private javax.swing.JTextField fieldNumDtwTypes;
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
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel labelOscStatus;
    private javax.swing.JMenu menuChooseAlgorithm;
    private javax.swing.JMenuItem menuCustomiseInputNames;
    private javax.swing.JMenuItem menuCustomiseOutputNames;
    private javax.swing.JMenuItem menuItemNewProject;
    private javax.swing.JMenuItem menuItemOpenProject;
    private javax.swing.JMenuItem menuLoadFromFile;
    private javax.swing.JMenuItem menuLoadOutputFromFile;
    private javax.swing.JPanel panelCustom;
    private javax.swing.JPanel panelInputs;
    private javax.swing.JPanel panelOutputTypes;
    private javax.swing.JPanel panelOutputs;
    private javax.swing.JPopupMenu popupMenuInputOptions;
    private javax.swing.JPopupMenu popupMenuOutputOptions;
    // End of variables declaration//GEN-END:variables

    @Override
    public Wekinator getWekinator() {
        return w;
    }

    private void initForCustomDtw() {
        //XXX
    }
}
