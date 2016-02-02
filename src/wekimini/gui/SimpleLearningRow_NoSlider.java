/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.event.KeyEvent;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import wekimini.Path;
import wekimini.Path.ModelState;
import wekimini.WekiMiniRunner;
import wekimini.Wekinator;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.util.Util;

/**
 *
 * @author fiebrink
 */
public class SimpleLearningRow_NoSlider extends javax.swing.JPanel implements LearningRow {

    private Wekinator w;
    private Path myPath;
    private boolean isClassifier = false;
    private float minValue = 0;
    private float maxValue = 1;
    private boolean isNumericInteger = false;
    private boolean isHardLimits = false;
    private String modelName = "modelName";
    // private double value = 1.0; //TODO make property w/ listeners & notification
    private static final DecimalFormat dFormat = new DecimalFormat("#.##");

    //Colors for text entry
    private static final Color doneColor = Color.WHITE;
    private static final Color notDoneColor = new Color(255, 200, 200);
    
    private boolean hasChanged = false;
    private boolean learnerRecord = true;
    private boolean learnerRun = true;
    
    private final ImageIcon recordIconOn = new ImageIcon(getClass().getResource("/wekimini/icons/record1.png"));
    private final ImageIcon recordIconOff = new ImageIcon(getClass().getResource("/wekimini/icons/record6.png"));
    private final ImageIcon playIconOn = new ImageIcon(getClass().getResource("/wekimini/icons/play1.png"));
    private final ImageIcon playIconOff = new ImageIcon(getClass().getResource("/wekimini/icons/play2.png"));

    
    //Colors for Path status
   /* private static final Color notTrainedColor = new Color(235, 235, 235);
    private static final Color trainedColor = new Color(153, 255, 0);
    private static final Color needsRetrainingColor = new Color(255, 153, 0);
    private static final Color trainingColor = new Color(0, 204, 255); */
    private double value = 1.0;

    public static final String PROP_VALUE = "value";

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private String statusText = "No model built";
    private final ImageIcon noModelIcon = new ImageIcon(getClass().getResource("/wekimini/icons/whitelight.png")); // NOI18N
    private final ImageIcon trainedIcon = new ImageIcon(getClass().getResource("/wekimini/icons/greenlight.png")); // NOI18N
    private final ImageIcon needsRetrainingIcon = new ImageIcon(getClass().getResource("/wekimini/icons/yellowlight.png")); // NOI18N
    private final ImageIcon trainingIcon = new ImageIcon(getClass().getResource("/wekimini/icons/pinklight.png")); // NOI18N
    private static final Logger logger = Logger.getLogger(SimpleLearningRow_NoSlider.class.getName());
    
    
    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Creates new form TestLearningRow1
     */
    public SimpleLearningRow_NoSlider() {
        initComponents();
    }

    public SimpleLearningRow_NoSlider(Wekinator w, Path p) {
        initComponents();
        this.w = w;
        myPath = p;
        modelName = p.getCurrentModelName();
        labelModelName.setText(p.getCurrentModelName());
        initForPath();
    }

    public boolean isChecked() {
        return checkbox1.isSelected();
    }

    /**
     * Get the value of value
     *
     * @return the value of value
     */
    public double getValue() {
        return value;
    }

    /**
     * Set the value of value
     *
     * @param value new value of value
     * TODO: This should probably NOT result in call to learning manager output value change!
     *      Put that in a separate function that's called only when GUI is modified.
     */
    public void setValue(double value) {
        double oldValue = this.value;
        setValueQuietly(value);
        propertyChangeSupport.firePropertyChange(PROP_VALUE, oldValue, value);
    }
    
    public void setComputedValue(double value) {
        setValueOnlyForDisplay(value);
    }

    //This should only be 
    public void setValueQuietly(double value) {
        setValueOnlyForDisplay(value);
        w.getSupervisedLearningManager().setOutputValueForPath(value, myPath);
    }
    
    public void setValueOnlyForDisplay(double value) {
        this.value = value;
        if (isClassifier) {
            if (comboClassifier.getSelectedIndex() != ((int)value) - 1) {
                comboClassifier.setSelectedIndex((int) value - 1 );
            }
        } else {
           // setSliderValueScaled(value);
           // textModelValue.setText(dFormat.format(value)); //TODO pretty format?
        }
    }

    public void setModelName(String name) {
        modelName = name;
        labelModelName.setText(modelName);
    }

    private void initForPath() {
        OSCOutput o = myPath.getOSCOutput();
        labelNumExamples.setText(Integer.toString(myPath.getNumExamples()));
        if (o instanceof OSCClassificationOutput) {
            initForClassifier(o);
        } else {
            initForNumeric(o);
        }

        updateNumExamples(myPath.getNumExamples());
        updateModelState(myPath.getModelState());
        updateRecordEnabled(myPath.isRecordEnabled());
        updateRunEnabled(myPath.isRunEnabled());

        myPath.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == Path.PROP_NUMEXAMPLES) {
                    updateNumExamples(myPath.getNumExamples());
                } else if (evt.getPropertyName() == Path.PROP_RECORDENABLED) {
                    updateRecordEnabled(myPath.isRecordEnabled());
                } else if (evt.getPropertyName() == Path.PROP_RUNENABLED) {
                    updateRunEnabled(myPath.isRunEnabled());
                } else if (evt.getPropertyName() == Path.PROP_MODELSTATE) {
                    updateModelState(myPath.getModelState());
                } else if (evt.getPropertyName() == Path.PROP_CURRENTMODELNAME) {
                    updateModelName(myPath.getCurrentModelName());
                }
            }
        });
    }
    private void updateModelName(String name) {
        labelModelName.setText(name);
    }
    
    private void updateModelState(Path.ModelState modelState) {
        Color c;
        if (modelState == ModelState.NOT_READY || modelState == ModelState.READY_FOR_BUILDING) {
            labelModelStatus.setIcon(noModelIcon);
            labelModelStatus.setToolTipText("No model built");
        } else if (modelState == ModelState.BUILDING) {
            labelModelStatus.setIcon(trainingIcon);
            labelModelStatus.setToolTipText("Model is being trained...");

        } else if (modelState == ModelState.BUILT) {
            labelModelStatus.setIcon(trainedIcon);
            labelModelStatus.setToolTipText("Model is trained and up to date");

        } else { // if (modelState == ModelState.NEEDS_REBUILDING) {
            labelModelStatus.setIcon(needsRetrainingIcon);
            labelModelStatus.setToolTipText("Model is out of date; needs re-training");
        }

        //panelMain.setBackground(c);
    }

    private void updateNumExamples(int num) {
        labelNumExamples.setText(Integer.toString(num));
    }

    private void updateRecordEnabled(boolean enabled) {
        learnerRecord = enabled;
        if (learnerRecord) {
            buttonLearnerRecord.setIcon(recordIconOn); // NOI18N
        } else {
            buttonLearnerRecord.setIcon(recordIconOff);
        }
    }

    private void updateRunEnabled(boolean enabled) {
        learnerRun = enabled;
        if (learnerRun) {
            buttonLearnerPlay.setIcon(playIconOn); // NOI18N
        } else {
            buttonLearnerPlay.setIcon(playIconOff);
        }
    }

    private void initForClassifier(OSCOutput o) {
        isClassifier = true;
        int numClasses = ((OSCClassificationOutput) o).getNumClasses();
        CardLayout c = ((CardLayout) panelModelOutput.getLayout());
        DefaultComboBoxModel m = new DefaultComboBoxModel(Util.numbersFromAtoBAsStrings(1, numClasses));
        comboClassifier.setModel(m);
      //  sliderModelValue.setVisible(false);
        c.show(panelModelOutput, "cardClassifier");
    }

    private void initForNumeric(OSCOutput o) {
        OSCNumericOutput no = (OSCNumericOutput) o;
        minValue = no.getMin();
        maxValue = no.getMax();
        isNumericInteger = (no.getOutputType() == OSCNumericOutput.NumericOutputType.INTEGER);
        isHardLimits = (no.getLimitType() == OSCNumericOutput.LimitType.HARD);

        //sliderModelValue.setMinimum(WIDTH);
      //  setupSlider();

        CardLayout c = ((CardLayout) panelModelOutput.getLayout());
        c.show(panelModelOutput, "cardNumeric");
    }

    /*private void setupSlider() {
        if (isNumericInteger) {
            //We only want integers anyway, so this is easy
            sliderModelValue.setMinimum((int) minValue);
            sliderModelValue.setMaximum((int) maxValue);
            sliderScale = 1.0;
            sliderScaleInv = 1.0;
        } else {
            float dist = maxValue - minValue;
            //We want at least 100 discrete points on slider between min and max
            sliderScale = 100. / dist;
            sliderScaleInv = 1.0 / sliderScale;
            sliderModelValue.setMinimum((int) (minValue * sliderScale));
            sliderModelValue.setMaximum((int) (maxValue * sliderScale));
        }
    } */

   /* private double getSliderValueScaled() {
        return sliderModelValue.getValue() * sliderScaleInv;
    }

    private void setSliderValueScaled(double value) {
        sliderModelValue.setValue((int) (value * sliderScale));
    } */

    public void setSelected(boolean s) {
        checkbox1.setSelected(s);
    }
    
    public void setRecordEnabled(boolean e) {
        learnerRecord = e;
        if (learnerRecord) {
            buttonLearnerRecord.setIcon(recordIconOn); // NOI18N
        } else {
            buttonLearnerRecord.setIcon(recordIconOff);
        }
        myPath.setRecordEnabled(e);
    }
    
    public void setRunEnabled(boolean e) {
        learnerRun = e;
        if (learnerRun) {
            buttonLearnerPlay.setIcon(playIconOn); // NOI18N
        } else {
            buttonLearnerPlay.setIcon(playIconOff);
        }
        myPath.setRunEnabled(e);
    }
    
    public boolean isSelected() {
        return checkbox1.isSelected();
    }
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame();
                f.setSize(500, 500);
                Wekinator w;
                try {
                    w = new Wekinator(WekiMiniRunner.generateNextID());

                    OSCNumericOutput no = new OSCNumericOutput("model1", 5, 10, OSCNumericOutput.NumericOutputType.REAL, OSCNumericOutput.LimitType.HARD);
                    String[] names = new String[]{"abc", "def"};
                    Path p = new Path(no, names, w, null);
                    SimpleLearningRow_NoSlider r = new SimpleLearningRow_NoSlider(w, p);
                    f.add(r);
                    f.setVisible(true);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        });

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
        checkbox1 = new javax.swing.JCheckBox();
        panelMain = new javax.swing.JPanel();
        labelModelName = new javax.swing.JLabel();
        labelNumExamples = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        buttonDeleteLearnerExamples = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        buttonLearnerRecord = new javax.swing.JButton();
        buttonEditLearner = new javax.swing.JButton();
        warningPanel = new javax.swing.JPanel();
        labelModelStatus = new javax.swing.JLabel();
        panelModelOutput = new javax.swing.JPanel();
        cardClassifier = new javax.swing.JPanel();
        comboClassifier = new javax.swing.JComboBox();
        cardNumeric = new javax.swing.JPanel();
        textModelValue = new javax.swing.JTextField();
        buttonLearnerPlay = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(568, 74));
        setMinimumSize(new java.awt.Dimension(568, 40));
        setPreferredSize(new java.awt.Dimension(568, 40));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(28, 74));

        checkbox1.setBackground(new java.awt.Color(255, 255, 255));
        checkbox1.setAlignmentY(0.0F);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(checkbox1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(checkbox1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        add(jPanel1);

        panelMain.setBackground(new java.awt.Color(255, 255, 255));
        panelMain.setMaximumSize(new java.awt.Dimension(540, 70));
        panelMain.setPreferredSize(new java.awt.Dimension(540, 40));

        labelModelName.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        labelModelName.setText("MaxVolume1_1");
        labelModelName.setToolTipText("Model name");

        labelNumExamples.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        labelNumExamples.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelNumExamples.setText("135800");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        buttonDeleteLearnerExamples.setBackground(new java.awt.Color(255, 255, 255));
        buttonDeleteLearnerExamples.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonDeleteLearnerExamples.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/x2.png"))); // NOI18N
        buttonDeleteLearnerExamples.setToolTipText("Delete training examples for this model (will not affect other models)");
        buttonDeleteLearnerExamples.setMaximumSize(new java.awt.Dimension(31, 32));
        buttonDeleteLearnerExamples.setMinimumSize(new java.awt.Dimension(31, 32));
        buttonDeleteLearnerExamples.setPreferredSize(new java.awt.Dimension(31, 32));
        buttonDeleteLearnerExamples.setSize(new java.awt.Dimension(30, 30));
        buttonDeleteLearnerExamples.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteLearnerExamplesActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        buttonLearnerRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/record1.png"))); // NOI18N
        buttonLearnerRecord.setToolTipText("Enable recording of new training data");
        buttonLearnerRecord.setIconTextGap(5);
        buttonLearnerRecord.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buttonLearnerRecord.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonLearnerRecord.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonLearnerRecord.setPreferredSize(new java.awt.Dimension(30, 30));
        buttonLearnerRecord.setSize(new java.awt.Dimension(30, 30));
        buttonLearnerRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLearnerRecordActionPerformed(evt);
            }
        });

        buttonEditLearner.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonEditLearner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/pencil2.png"))); // NOI18N
        buttonEditLearner.setToolTipText("Edit this model");
        buttonEditLearner.setEnabled(false);
        buttonEditLearner.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonEditLearner.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonEditLearner.setPreferredSize(new java.awt.Dimension(30, 30));
        buttonEditLearner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditLearnerActionPerformed(evt);
            }
        });

        warningPanel.setBackground(new java.awt.Color(255, 255, 255));
        warningPanel.setOpaque(false);
        warningPanel.setPreferredSize(new java.awt.Dimension(30, 30));

        labelModelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/yellowlight.png"))); // NOI18N
        labelModelStatus.setToolTipText(statusText);
        labelModelStatus.setAlignmentX(0.5F);

        javax.swing.GroupLayout warningPanelLayout = new javax.swing.GroupLayout(warningPanel);
        warningPanel.setLayout(warningPanelLayout);
        warningPanelLayout.setHorizontalGroup(
            warningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(warningPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelModelStatus)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        warningPanelLayout.setVerticalGroup(
            warningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(warningPanelLayout.createSequentialGroup()
                .addComponent(labelModelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        panelModelOutput.setLayout(new java.awt.CardLayout());

        cardClassifier.setBackground(new java.awt.Color(255, 255, 255));

        comboClassifier.setBackground(new java.awt.Color(255, 255, 255));
        comboClassifier.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "100" }));
        comboClassifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboClassifierActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout cardClassifierLayout = new javax.swing.GroupLayout(cardClassifier);
        cardClassifier.setLayout(cardClassifierLayout);
        cardClassifierLayout.setHorizontalGroup(
            cardClassifierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(comboClassifier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        cardClassifierLayout.setVerticalGroup(
            cardClassifierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(comboClassifier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelModelOutput.add(cardClassifier, "cardClassifier");

        cardNumeric.setBackground(new java.awt.Color(255, 255, 255));

        textModelValue.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        textModelValue.setText("13.83");
        textModelValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                textModelValueFocusLost(evt);
            }
        });
        textModelValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textModelValueActionPerformed(evt);
            }
        });
        textModelValue.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                textModelValuePropertyChange(evt);
            }
        });
        textModelValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textModelValueKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textModelValueKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textModelValueKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout cardNumericLayout = new javax.swing.GroupLayout(cardNumeric);
        cardNumeric.setLayout(cardNumericLayout);
        cardNumericLayout.setHorizontalGroup(
            cardNumericLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textModelValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );
        cardNumericLayout.setVerticalGroup(
            cardNumericLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardNumericLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(textModelValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelModelOutput.add(cardNumeric, "cardNumeric");

        buttonLearnerPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/play1.png"))); // NOI18N
        buttonLearnerPlay.setToolTipText("Enable running on new data");
        buttonLearnerPlay.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonLearnerPlay.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonLearnerPlay.setPreferredSize(new java.awt.Dimension(30, 30));
        buttonLearnerPlay.setSize(new java.awt.Dimension(30, 30));
        buttonLearnerPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLearnerPlayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelModelName, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelModelOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelNumExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(buttonDeleteLearnerExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonLearnerRecord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonLearnerPlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(buttonEditLearner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(warningPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonEditLearner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(warningPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonDeleteLearnerExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelNumExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonLearnerRecord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonLearnerPlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelModelOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelModelName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))))
        );

        add(panelMain);
    }// </editor-fold>//GEN-END:initComponents

    private void textModelValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textModelValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textModelValueActionPerformed

    private void textModelValuePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_textModelValuePropertyChange
    }//GEN-LAST:event_textModelValuePropertyChange

    private void setValueFromText() {
        try {
            double d = Double.parseDouble(textModelValue.getText());
                /*if (isHardLimits) {
                    if (d < minValue) {
                        d = minValue;
                        textModelValue.setText(dFormat.format(d));
                    } else if (d > maxValue) {
                        d = maxValue;
                        textModelValue.setText(dFormat.format(d));
                    }
                } */
                if (!myPath.getOSCOutput().isLegalTrainingValue(d)) {
                    d = myPath.getOSCOutput().forceLegalTrainingValue(d);
                    textModelValue.setText(dFormat.format(d));
                }
                colorDone();
                setValue(d);
                
        } catch (NumberFormatException ex) {
            textModelValue.setText(dFormat.format(value));
        }
    }
    
    private void textModelValueKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textModelValueKeyReleased
        //TODO: what to do if hard limits in place???
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_TAB) {
            try {
                setValueFromText();
            } catch (NumberFormatException ex) {
                //Could be blank: Do nothing
            }
        } else {
            if (!hasChanged) {
                colorNotDone();
            }
        }

    }//GEN-LAST:event_textModelValueKeyReleased

    private void colorDone() {
        hasChanged = false;
        textModelValue.setBackground(doneColor);
    }

    private void colorNotDone() {
        hasChanged = true;
        textModelValue.setBackground(notDoneColor);
    }

    private void textModelValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textModelValueKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter) || enter == '.' || enter == '-')) {
            evt.consume();
        }
    }//GEN-LAST:event_textModelValueKeyTyped

    private void toggleLearnerRecord() {
        learnerRecord = !learnerRecord;
        setRecordEnabled(learnerRecord);
    }
    
    private void toggleLearnerRun() {
        learnerRun = !learnerRun;
        setRunEnabled(learnerRun);
    }
    
    private void buttonDeleteLearnerExamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteLearnerExamplesActionPerformed
        w.getSupervisedLearningManager().deleteExamplesForPath(myPath);
    }//GEN-LAST:event_buttonDeleteLearnerExamplesActionPerformed

    private void buttonEditLearnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditLearnerActionPerformed
        //w.getSupervisedLearningManager().
        w.getMainGUI().showPathEditor(myPath);
    }//GEN-LAST:event_buttonEditLearnerActionPerformed

    private void textModelValueKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textModelValueKeyPressed
        //System.out.println("DOWN" + evt.getKeyCode());
    }//GEN-LAST:event_textModelValueKeyPressed

    private void textModelValueFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textModelValueFocusLost
        if (hasChanged) {
            setValueFromText();
        }
    }//GEN-LAST:event_textModelValueFocusLost

    /*private void userUpdatedSlider() {
        double d = getSliderValueScaled();
        if (value != d) {        
            setValue(getSliderValueScaled());
        }
    } */
    
    private void comboClassifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboClassifierActionPerformed
        setValue(comboClassifier.getSelectedIndex()+1);
    }//GEN-LAST:event_comboClassifierActionPerformed

    private void buttonLearnerRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLearnerRecordActionPerformed
        toggleLearnerRecord();
    }//GEN-LAST:event_buttonLearnerRecordActionPerformed

    private void buttonLearnerPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLearnerPlayActionPerformed
        toggleLearnerRun();
    }//GEN-LAST:event_buttonLearnerPlayActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDeleteLearnerExamples;
    private javax.swing.JButton buttonEditLearner;
    private javax.swing.JButton buttonLearnerPlay;
    private javax.swing.JButton buttonLearnerRecord;
    private javax.swing.JPanel cardClassifier;
    private javax.swing.JPanel cardNumeric;
    private javax.swing.JCheckBox checkbox1;
    private javax.swing.JComboBox comboClassifier;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelModelName;
    private javax.swing.JLabel labelModelStatus;
    private javax.swing.JLabel labelNumExamples;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelModelOutput;
    private javax.swing.JTextField textModelValue;
    private javax.swing.JPanel warningPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void updateValueGUI() {
        //TODO HACK
        textModelValue.setText(dFormat.format(value));
    }
}