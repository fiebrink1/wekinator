/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import com.sun.glass.events.KeyEvent;
import java.awt.CardLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import wekimini.Path;
import wekimini.Path.ModelState;
import wekimini.Wekinator;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.util.Util;

/**
 *
 * @author fiebrink
 */
public class SimpleLearningRow extends javax.swing.JPanel {

    private Wekinator w;
    private Path myPath;
    private boolean isClassifier = false;
    private float minValue = 0;
    private float maxValue = 1;
    private boolean isNumericInteger = false;
    private boolean isHardLimits = false;
    private String modelName = "modelName";
    private double sliderScale = 1.0;
    private double sliderScaleInv = 1.0;
    // private double value = 1.0; //TODO make property w/ listeners & notification
    private static final DecimalFormat dFormat = new DecimalFormat("#.#####");

    //Colors for text entry
    private static final Color doneColor = Color.WHITE;
    private static final Color notDoneColor = new Color(255, 200, 200);

    //Colors for Path status
    private static final Color notTrainedColor = new Color(235, 235, 235);
    private static final Color trainedColor = new Color(153, 255, 0);
    private static final Color needsRetrainingColor = new Color(255, 153, 0);
    private static final Color trainingColor = new Color(0, 204, 255);

    private double value = 1.0;

    public static final String PROP_VALUE = "value";

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
    public SimpleLearningRow() {
        initComponents();
    }

    public SimpleLearningRow(Wekinator w, Path p) {
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
     */
    public void setValue(double value) {
        double oldValue = this.value;
        setValueQuietly(value);
        propertyChangeSupport.firePropertyChange(PROP_VALUE, oldValue, value);
    }

    public void setValueQuietly(double value) {
        this.value = value;
        if (isClassifier) {
            comboClassifier.setSelectedIndex((int) value - 1);
        } else {
            setSliderValueScaled(value);
            textModelValue.setText(dFormat.format(value)); //TODO pretty format?
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
            c = notTrainedColor;
        } else if (modelState == ModelState.BUILDING) {
            c = trainingColor;
        } else if (modelState == ModelState.BUILT) {
            c = trainedColor;
        } else { // if (modelState == ModelState.NEEDS_REBUILDING) {
            c = needsRetrainingColor;
        }

        panelMain.setBackground(c);
    }

    private void updateNumExamples(int num) {
        labelNumExamples.setText(Integer.toString(num));
    }

    private void updateRecordEnabled(boolean enabled) {
        toggleLearnerRecord.setSelected(enabled);
    }

    private void updateRunEnabled(boolean enabled) {
        toggleLearnerPlay.setSelected(enabled);
    }

    private void initForClassifier(OSCOutput o) {
        isClassifier = true;
        int numClasses = ((OSCClassificationOutput) o).getNumClasses();
        CardLayout c = ((CardLayout) panelModelOutput.getLayout());
        DefaultComboBoxModel m = new DefaultComboBoxModel(Util.numbersFromAtoBAsStrings(1, numClasses));
        comboClassifier.setModel(m);
        sliderModelValue.setVisible(false);
        c.show(panelModelOutput, "cardClassifier");
    }

    private void initForNumeric(OSCOutput o) {
        OSCNumericOutput no = (OSCNumericOutput) o;
        minValue = no.getMin();
        maxValue = no.getMax();
        isNumericInteger = (no.getOutputType() == OSCNumericOutput.NumericOutputType.INTEGER);
        isHardLimits = (no.getLimitType() == OSCNumericOutput.LimitType.HARD);

        //sliderModelValue.setMinimum(WIDTH);
        setupSlider();

        CardLayout c = ((CardLayout) panelModelOutput.getLayout());
        c.show(panelModelOutput, "cardNumeric");
    }

    private void setupSlider() {
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
    }

    private double getSliderValueScaled() {
        return sliderModelValue.getValue() * sliderScaleInv;
    }

    private void setSliderValueScaled(double value) {
        sliderModelValue.setValue((int) (value * sliderScale));
    }

    public void setSelected(boolean s) {
        checkbox1.setSelected(s);
    }
    
    public void setRecordEnabled(boolean e) {
        toggleLearnerRecord.setSelected(e);
        myPath.setRecordEnabled(e);
    }
    
    public void setRunEnabled(boolean e) {
        toggleLearnerPlay.setSelected(e);
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
                    w = new Wekinator();

                    OSCNumericOutput no = new OSCNumericOutput("model1", 5, 10, OSCNumericOutput.NumericOutputType.REAL, OSCNumericOutput.LimitType.HARD);
                    String[] names = new String[]{"abc", "def"};
                    Path p = new Path(no, names, w);
                    SimpleLearningRow r = new SimpleLearningRow(w, p);
                    f.add(r);
                    f.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(InitInputOutput.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(InitInputOutput.class.getName()).log(Level.SEVERE, null, ex);
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
        sliderModelValue = new javax.swing.JSlider();
        labelNumExamples = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        buttonDeleteLearnerExamples = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        toggleLearnerPlay = new javax.swing.JToggleButton();
        toggleLearnerRecord = new javax.swing.JToggleButton();
        buttonEditLearner = new javax.swing.JButton();
        warningPanel = new javax.swing.JPanel();
        panelModelOutput = new javax.swing.JPanel();
        cardClassifier = new javax.swing.JPanel();
        comboClassifier = new javax.swing.JComboBox();
        cardNumeric = new javax.swing.JPanel();
        textModelValue = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(568, 74));
        setMinimumSize(new java.awt.Dimension(568, 74));
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
            .addComponent(checkbox1, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
        );

        add(jPanel1);

        panelMain.setBackground(new java.awt.Color(0, 204, 255));
        panelMain.setMaximumSize(new java.awt.Dimension(540, 70));
        panelMain.setPreferredSize(new java.awt.Dimension(540, 70));
        panelMain.setSize(new java.awt.Dimension(540, 70));

        labelModelName.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        labelModelName.setText("MaxVolume1_1");

        sliderModelValue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderModelValueStateChanged(evt);
            }
        });

        labelNumExamples.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        labelNumExamples.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelNumExamples.setText("135800");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        buttonDeleteLearnerExamples.setBackground(new java.awt.Color(255, 255, 255));
        buttonDeleteLearnerExamples.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonDeleteLearnerExamples.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/x2.png"))); // NOI18N
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

        toggleLearnerPlay.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        toggleLearnerPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/play.png"))); // NOI18N
        toggleLearnerPlay.setSelected(true);
        toggleLearnerPlay.setPreferredSize(new java.awt.Dimension(30, 30));
        toggleLearnerPlay.setSize(new java.awt.Dimension(30, 30));
        toggleLearnerPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleLearnerPlayActionPerformed(evt);
            }
        });

        toggleLearnerRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/record1.png"))); // NOI18N
        toggleLearnerRecord.setSelected(true);
        toggleLearnerRecord.setSize(new java.awt.Dimension(30, 30));
        toggleLearnerRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleLearnerRecordActionPerformed(evt);
            }
        });

        buttonEditLearner.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonEditLearner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/pencil1.png"))); // NOI18N
        buttonEditLearner.setToolTipText("");
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

        javax.swing.GroupLayout warningPanelLayout = new javax.swing.GroupLayout(warningPanel);
        warningPanel.setLayout(warningPanelLayout);
        warningPanelLayout.setHorizontalGroup(
            warningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        warningPanelLayout.setVerticalGroup(
            warningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        panelModelOutput.setLayout(new java.awt.CardLayout());

        cardClassifier.setBackground(new java.awt.Color(255, 255, 255));

        comboClassifier.setBackground(new java.awt.Color(255, 255, 255));
        comboClassifier.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "100" }));

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

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelModelName, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGap(0, 0, 0)
                .addComponent(toggleLearnerRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(toggleLearnerPlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(buttonEditLearner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(warningPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(sliderModelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(buttonDeleteLearnerExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelNumExamples, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(toggleLearnerPlay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jSeparator1)
                                    .addComponent(jSeparator2)
                                    .addComponent(toggleLearnerRecord, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                                    .addComponent(buttonEditLearner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(warningPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(panelModelOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelModelName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(sliderModelValue, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(panelMain);
    }// </editor-fold>//GEN-END:initComponents

    private void textModelValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textModelValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textModelValueActionPerformed

    private void sliderModelValueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderModelValueStateChanged
        // System.out.println(getSliderValueScaled());
        setValue(getSliderValueScaled());

       // textModelValue.setText(dFormat.format(getSliderValueScaled()));

    }//GEN-LAST:event_sliderModelValueStateChanged

    private void textModelValuePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_textModelValuePropertyChange
    }//GEN-LAST:event_textModelValuePropertyChange

    private void textModelValueKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textModelValueKeyReleased
        //TODO: what to do if hard limits in place???
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_TAB) {
            try {
                double d = Double.parseDouble(textModelValue.getText());
                if (isHardLimits) {
                    if (d < minValue) {
                        d = minValue;
                        textModelValue.setText(dFormat.format(d));
                    } else if (d > maxValue) {
                        d = maxValue;
                        textModelValue.setText(dFormat.format(d));
                    }
                }
                setValue(d);
                colorDone();
            } catch (NumberFormatException ex) {
                //Could be blank: Do nothing
            }
        } else {
            colorNotDone();
        }

    }//GEN-LAST:event_textModelValueKeyReleased

    private void colorDone() {
        textModelValue.setBackground(doneColor);
    }

    private void colorNotDone() {
        textModelValue.setBackground(notDoneColor);
    }

    private void textModelValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textModelValueKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter) || enter == '.' || enter == '-')) {
            evt.consume();
        }
    }//GEN-LAST:event_textModelValueKeyTyped

    private void toggleLearnerRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleLearnerRecordActionPerformed
        myPath.setRecordEnabled(toggleLearnerRecord.isSelected());
    }//GEN-LAST:event_toggleLearnerRecordActionPerformed

    private void toggleLearnerPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleLearnerPlayActionPerformed
        myPath.setRunEnabled(toggleLearnerPlay.isSelected());
    }//GEN-LAST:event_toggleLearnerPlayActionPerformed

    private void buttonDeleteLearnerExamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteLearnerExamplesActionPerformed
        w.getLearningManager().deleteExamplesForPath(myPath);
    }//GEN-LAST:event_buttonDeleteLearnerExamplesActionPerformed

    private void buttonEditLearnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditLearnerActionPerformed
        //w.getLearningManager().
    }//GEN-LAST:event_buttonEditLearnerActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDeleteLearnerExamples;
    private javax.swing.JButton buttonEditLearner;
    private javax.swing.JPanel cardClassifier;
    private javax.swing.JPanel cardNumeric;
    private javax.swing.JCheckBox checkbox1;
    private javax.swing.JComboBox comboClassifier;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelModelName;
    private javax.swing.JLabel labelNumExamples;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelModelOutput;
    private javax.swing.JSlider sliderModelValue;
    private javax.swing.JTextField textModelValue;
    private javax.swing.JToggleButton toggleLearnerPlay;
    private javax.swing.JToggleButton toggleLearnerRecord;
    private javax.swing.JPanel warningPanel;
    // End of variables declaration//GEN-END:variables
}
