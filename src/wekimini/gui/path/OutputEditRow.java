/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui.path;

import java.awt.CardLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import wekimini.gui.OutputConfigurationFrame;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OutputEditRow extends javax.swing.JPanel {

    private static int CLASSIFICATION_INDEX = 0;
    private static int REGRESSION_INDEX = 1;
    private static int REGRESSION_INT = 1;
    private static int REGRESSION_FLOAT = 0;
    private static int HARD_LIMIT_INDEX = 1;
    private static int SOFT_LIMIT_INDEX = 0;
    private int number = 1;
    private static final Logger logger = Logger.getLogger(OutputEditRow.class.getName());

    /**
     * Creates new form GUIOSCOutputPanel
     */
    public OutputEditRow() {
        initComponents();
        // finishSetup();
    }

    public OutputEditRow(int num, String name) {
        initComponents();
        setNum(num);
        comboType.setSelectedIndex(REGRESSION_INDEX);
        setToRegression();
        textName.setText(name);
        updateOSCHint();

        // finishSetup();
    }

    public OutputEditRow(OSCOutput currentOutput) {
        initComponents();
       // setNum(num);
        textName.setText(currentOutput.getName());
        if (currentOutput != null) {
            setValuesFromCurrent(currentOutput);
        } else {
            comboType.setSelectedIndex(REGRESSION_INDEX);
            setToRegression();
        }
    }

    private void setValuesFromCurrent(OSCOutput currentOutput) {
        if (currentOutput instanceof OSCNumericOutput) {
            comboType.setSelectedIndex(REGRESSION_INDEX);
            //setToRegression();
            OSCNumericOutput no = (OSCNumericOutput) currentOutput;
            if (no.getOutputType() == OSCNumericOutput.NumericOutputType.INTEGER) {
                comboRegressionType.setSelectedIndex(REGRESSION_INT);
            } else {
                comboRegressionType.setSelectedIndex(REGRESSION_FLOAT);
            }
            
            if (no.getLimitType() == OSCNumericOutput.LimitType.HARD) {
                comboLimitType.setSelectedIndex(HARD_LIMIT_INDEX);
            } else {
                comboLimitType.setSelectedIndex(SOFT_LIMIT_INDEX);
            }
            
            textMax.setText(Float.toString(no.getMax()));
            textMin.setText(Float.toString(no.getMin()));
            
            
            
        } else if (currentOutput instanceof OSCClassificationOutput) {
            //setToClassification();
            comboType.setSelectedIndex(CLASSIFICATION_INDEX);
            OSCClassificationOutput co = (OSCClassificationOutput) currentOutput;
            textNumClasses.setText(Integer.toString(co.getNumClasses()));
            checkDistribution.setSelected(co.isSendingDistribution());
            updateOSCHint();
        } else {
            throw new UnsupportedOperationException("Unknown output type: " + currentOutput.getClass());
        }
    }

    public final void setNum(int num) {
        number = num;
        labelName.setText(number + ". Name:");
    }

  /*  public OutputEditRow(OSCOutput o) {
        initComponents();
        initFromOSCOutput(o);
        // finishSetup();
    } */

    /* private void finishSetup() {
     textNumClasses.getDocument().addDocumentListener(new DocumentListener() {
     @Override
     public void insertUpdate(DocumentEvent e) {
     classChanged();
     }

     @Override
     public void removeUpdate(DocumentEvent e) {
     classChanged();

     }

     @Override
     public void changedUpdate(DocumentEvent e) {
     classChanged();

     }
     }); */
    public OSCOutput getOSCOutputFromForm() throws IllegalStateException {
        if (comboType.getSelectedIndex() == CLASSIFICATION_INDEX) {
            return makeClassificationOutput();
        } else {
            return makeRegressionOutput();
        }
    }
    
    public boolean validateForm() {
        if (!Util.checkNotBlank(textName, "Output name", this)) {
            return false;
        }
        
        if (comboType.getSelectedIndex() == CLASSIFICATION_INDEX) {
           try {
               int c = Integer.parseInt(textNumClasses.getText());
           } catch (NumberFormatException ex) {
               Util.showPrettyErrorPane(this, "Number of classes must be an integer greater than 1");
               return false;
           }
        } else {
           double min, max;
           try {
               min = Double.parseDouble(textMin.getText());
               max = Double.parseDouble(textMax.getText());
               if (min >= max) {
                   Util.showPrettyErrorPane(this, "Minimum must be less than maximum");
                   return false;
               }
           } catch (NumberFormatException ex) {
               Util.showPrettyErrorPane(this, "Min and max values must be numbers");
               return false;
           }
        }
        return true;
    }

    public String getOutputName() {
        return textName.getText().trim();
    }

    //TODO: Test all
    private OSCClassificationOutput makeClassificationOutput() throws IllegalStateException {
        String name = getOutputName();
        String id = name;
        String numClassesString = textNumClasses.getText();
        try {
            int numClasses = Integer.parseInt(numClassesString);
            if (numClasses > 0) {
                return new OSCClassificationOutput(name, numClasses, checkDistribution.isSelected());
            } else {
                throw new IllegalStateException("Number of classes must be an integer greater than 0.");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Number of classes must be an integer greater than 0.");
        }
    }

    //TODO: Test all 
    private OSCNumericOutput makeRegressionOutput() throws IllegalStateException {
        String name = getOutputName();
        String id = name;
        String minString = textMin.getText();
        String maxString = textMax.getText();
        OSCNumericOutput.NumericOutputType outputType
                = (comboRegressionType.getSelectedIndex() == REGRESSION_FLOAT
                        ? OSCNumericOutput.NumericOutputType.REAL : OSCNumericOutput.NumericOutputType.INTEGER);
        OSCNumericOutput.LimitType limitType
                = (comboLimitType.getSelectedIndex() == HARD_LIMIT_INDEX
                        ? OSCNumericOutput.LimitType.HARD : OSCNumericOutput.LimitType.SOFT);
        try {
            float min = Float.parseFloat(minString);
            float max = Float.parseFloat(maxString);
            if (min < max) {
                return new OSCNumericOutput(name, min, max, outputType, limitType);
            } else {
                throw new IllegalStateException("Min value must be less than max value");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Min and max must be numeric values with min less than max.");
        }
    }

    /*public void setName(String name) {
     textName.setText(name);
     }
   
     public String getOutputName() {
     return name;
     } */
    
    private void initFromOSCOutput(OSCOutput o) {
        textName.setText(o.getName());
        if (o instanceof OSCNumericOutput) {
            setToRegression();
            OSCNumericOutput no = (OSCNumericOutput) o;
            if (no.getOutputType() == OSCNumericOutput.NumericOutputType.INTEGER) {
                comboRegressionType.setSelectedIndex(REGRESSION_INT);
            } else if (no.getOutputType() == OSCNumericOutput.NumericOutputType.REAL) {
                comboRegressionType.setSelectedIndex(REGRESSION_FLOAT);
            }
            textMin.setText(Float.toString(no.getMin()));
            textMax.setText(Float.toString(no.getMax())); //TODO: might want to prettify these
            if (no.getLimitType() == OSCNumericOutput.LimitType.HARD) {
                comboLimitType.setSelectedIndex(HARD_LIMIT_INDEX);
            } else if (no.getLimitType() == OSCNumericOutput.LimitType.SOFT) {
                comboLimitType.setSelectedIndex(SOFT_LIMIT_INDEX);
            }
        } else if (o instanceof OSCClassificationOutput) {
            setToClassification();
            textNumClasses.setText(Integer.toString(((OSCClassificationOutput) o).getNumClasses()));
        } else {
            logger.log(Level.WARNING, "Error: Unknown output type " + o.getClass().getName());
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

        jPanel2 = new javax.swing.JPanel();
        labelName = new javax.swing.JLabel();
        textName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        comboType = new javax.swing.JComboBox();
        labelHint = new javax.swing.JLabel();
        panelClassificationRegression = new javax.swing.JPanel();
        cardClassification = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        textNumClasses = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        checkDistribution = new javax.swing.JCheckBox();
        labelOSCHint = new javax.swing.JLabel();
        cardRegression = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        comboRegressionType = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        textMax = new javax.swing.JTextField();
        comboLimitType = new javax.swing.JComboBox();
        textMin = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        labelName.setText("Name:");

        textName.setText("Max1");
        textName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textNameActionPerformed(evt);
            }
        });
        textName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textNameKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textName)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelName)
                    .addComponent(textName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel5.setText("Type:");

        comboType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Classification", "Numeric" }));
        comboType.setEnabled(false);
        comboType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboTypeActionPerformed(evt);
            }
        });

        labelHint.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        labelHint.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelHint.setText("<html><i>Outputs are unordered categories</i></html>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(labelHint, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(comboType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelClassificationRegression.setLayout(new java.awt.CardLayout());

        cardClassification.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setText("with");

        textNumClasses.setText("5");
        textNumClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textNumClassesActionPerformed(evt);
            }
        });
        textNumClasses.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textNumClassesKeyTyped(evt);
            }
        });

        jLabel8.setText("classes");

        checkDistribution.setText("Send class probabilities via OSC");
        checkDistribution.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkDistributionActionPerformed(evt);
            }
        });

        labelOSCHint.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        labelOSCHint.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelOSCHint.setText("<html><i>OSC message will be /Max1</i></html>");
        labelOSCHint.setEnabled(false);
        labelOSCHint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                labelOSCHintKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout cardClassificationLayout = new javax.swing.GroupLayout(cardClassification);
        cardClassification.setLayout(cardClassificationLayout);
        cardClassificationLayout.setHorizontalGroup(
            cardClassificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardClassificationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardClassificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardClassificationLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textNumClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8))
                    .addComponent(checkDistribution)
                    .addComponent(labelOSCHint, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(105, Short.MAX_VALUE))
        );
        cardClassificationLayout.setVerticalGroup(
            cardClassificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardClassificationLayout.createSequentialGroup()
                .addGroup(cardClassificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(textNumClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(0, 0, 0)
                .addComponent(checkDistribution)
                .addGap(0, 0, 0)
                .addComponent(labelOSCHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelClassificationRegression.add(cardClassification, "cardClassification");

        cardRegression.setBackground(new java.awt.Color(255, 255, 255));
        cardRegression.setLayout(new javax.swing.BoxLayout(cardRegression, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel26.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setText("with");

        comboRegressionType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "real-valued (float)", "integer" }));
        comboRegressionType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboRegressionTypeActionPerformed(evt);
            }
        });

        jLabel6.setText("outputs");

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addGap(0, 0, 0)
                .addComponent(comboRegressionType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(comboRegressionType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(0, 0, 0))
        );

        cardRegression.add(jPanel26);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setText("Min:");

        jLabel11.setText("Max:");

        textMax.setText("1");
        textMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textMaxActionPerformed(evt);
            }
        });
        textMax.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textMaxKeyTyped(evt);
            }
        });

        comboLimitType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "soft limits", "hard limits" }));
        comboLimitType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboLimitTypeActionPerformed(evt);
            }
        });

        textMin.setText("0");
        textMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textMinActionPerformed(evt);
            }
        });
        textMin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textMinKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textMin, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textMax, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboLimitType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(0, 14, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(textMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(textMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboLimitType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        cardRegression.add(jPanel9);

        panelClassificationRegression.add(cardRegression, "cardRegression");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelClassificationRegression, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelClassificationRegression, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void textNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textNameActionPerformed

    private void comboTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboTypeActionPerformed
        //should make it here
        if (comboType.getSelectedIndex() == CLASSIFICATION_INDEX) {
            setToClassification();
        } else {
            setToRegression();
        }
    }//GEN-LAST:event_comboTypeActionPerformed

    private void setToClassification() {
        ((CardLayout) panelClassificationRegression.getLayout()).show(panelClassificationRegression, "cardClassification");
        updateHint();
    }

    private void setToRegression() {
        ((CardLayout) panelClassificationRegression.getLayout()).show(panelClassificationRegression, "cardRegression");
        updateHint();
    }

    private void textNumClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textNumClassesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textNumClassesActionPerformed

    private void textMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textMaxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textMaxActionPerformed

    private void comboLimitTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboLimitTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboLimitTypeActionPerformed

    private void comboRegressionTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboRegressionTypeActionPerformed
        updateHint();
    }//GEN-LAST:event_comboRegressionTypeActionPerformed

    private void textMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textMinActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textMinActionPerformed

    private void textMinKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textMinKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter) || enter == '.' || enter == '-')) {
            evt.consume();
        }
    }//GEN-LAST:event_textMinKeyTyped

    private void textMaxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textMaxKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter) || enter == '.' || enter == '-')) {
            evt.consume();
        }
    }//GEN-LAST:event_textMaxKeyTyped

    private void textNumClassesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textNumClassesKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_textNumClassesKeyTyped

    private void checkDistributionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkDistributionActionPerformed
        labelOSCHint.setEnabled(checkDistribution.isSelected());
    }//GEN-LAST:event_checkDistributionActionPerformed

    private void labelOSCHintKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_labelOSCHintKeyReleased
    }//GEN-LAST:event_labelOSCHintKeyReleased

    private void textNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textNameKeyReleased
        updateOSCHint();

    }//GEN-LAST:event_textNameKeyReleased

    private void updateOSCHint() {
        labelOSCHint.setText("OSC message will be /" + textName.getText());
    }
    
    
    private void updateHint() {
        if (comboType.getSelectedIndex() == CLASSIFICATION_INDEX) {
            labelHint.setText("<html><i>Outputs are unordered categories</i></html>");
        } else if (comboType.getSelectedIndex() == REGRESSION_INDEX) {
            if (comboRegressionType.getSelectedIndex() == REGRESSION_INT) {
                labelHint.setText("<html><i>Outputs are (ordered) integers</i></html>");
            } else if (comboRegressionType.getSelectedIndex() == REGRESSION_FLOAT) {
                labelHint.setText("<html><i>Outputs are floats</i></html>");
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame();
                f.setSize(200, 200);
                OutputEditRow p = new OutputEditRow();
                f.add(p);
                f.setVisible(true);

            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cardClassification;
    private javax.swing.JPanel cardRegression;
    private javax.swing.JCheckBox checkDistribution;
    private javax.swing.JComboBox comboLimitType;
    private javax.swing.JComboBox comboRegressionType;
    private javax.swing.JComboBox comboType;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel labelHint;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelOSCHint;
    private javax.swing.JPanel panelClassificationRegression;
    private javax.swing.JTextField textMax;
    private javax.swing.JTextField textMin;
    private javax.swing.JTextField textName;
    private javax.swing.JTextField textNumClasses;
    // End of variables declaration//GEN-END:variables
}
