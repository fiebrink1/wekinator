/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning.dtw;

import java.awt.CardLayout;
import wekimini.learning.dtw.DtwSettings.RunningType;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class DtwEditorPanel extends javax.swing.JPanel {

    /**
     * Creates new form DtwEditorPanel
     */
    public DtwEditorPanel() {
        initComponents();
        setToDefaults();
    }

    public DtwEditorPanel(DtwSettings s) {
        initComponents();
        setToDefaults();
        initFormForSettings(s);
    }

    private void initFormForSettings(DtwSettings s) {
        DtwSettings.RunningType rt = s.getRunningType();
        if (rt == RunningType.LABEL_CONTINUOUSLY) {
            comboRunningType.setSelectedIndex(0);
        } else {
            comboRunningType.setSelectedIndex(1);
        }

        DtwSettings.DownsamplePolicy policy = s.getDownsamplePolicy();
        if (policy == DtwSettings.DownsamplePolicy.DOWNSAMPLE_TO_MAX_LENGTH) {
            comboDownsample.setSelectedIndex(0);
            textDownsampleMax.setText(Integer.toString(s.getDownsampleMaxLength()));
        } else if (policy == DtwSettings.DownsamplePolicy.DOWNSAMPLE_BY_CONSTANT_FACTOR) {
            comboDownsample.setSelectedIndex(1);
            textDownsampleConstant.setText(Integer.toString(s.getDownsampleFactor()));
        } else {
            comboDownsample.setSelectedIndex(2);
        }
        
        DtwSettings.MinimumLengthRestriction lengthRestriction = s.getMinimumLengthRestriction();
        if (lengthRestriction == DtwSettings.MinimumLengthRestriction.SET_CONSTANT) {
            comboMatchLength.setSelectedIndex(0);
            textMinLength.setText(Integer.toString(s.getMinAllowedGestureLength()));
        } else {
            comboMatchLength.setSelectedIndex(1);
        }
        
        int matchWidth = s.getMatchWidth();
        textMatchWidth.setText(Integer.toString(matchWidth));
        
        int hopSize = s.getHopSizeForContinuousSearch();
        textHopSize.setText(Integer.toString(hopSize)); 
    }

    public DtwSettings buildFromPanel() {
        RunningType rt;
        if (comboRunningType.getSelectedIndex() == 0) {
            rt = RunningType.LABEL_CONTINUOUSLY;
        } else {
            rt = RunningType.LABEL_ONCE_PER_RECORD;
        }

        DtwSettings.DownsamplePolicy policy;
        int downsampleMax;
        int downsampleFactor;
        if (comboDownsample.getSelectedIndex() == 0) {
            policy = DtwSettings.DownsamplePolicy.DOWNSAMPLE_TO_MAX_LENGTH;
            downsampleMax = Integer.parseInt(textDownsampleMax.getText());
            downsampleFactor = DtwSettings.DEFAULT_DOWNSAMPLE_FACTOR;
        } else if (comboDownsample.getSelectedIndex() == 1) {
            policy = DtwSettings.DownsamplePolicy.DOWNSAMPLE_BY_CONSTANT_FACTOR;
            downsampleMax = DtwSettings.DEFAULT_DOWNSAMPLE_MAX_LENGTH;
            downsampleFactor = Integer.parseInt(textDownsampleConstant.getText());
        } else {
            policy = DtwSettings.DownsamplePolicy.NO_DOWNSAMPLING;
            downsampleFactor = DtwSettings.DEFAULT_DOWNSAMPLE_FACTOR;
            downsampleMax = DtwSettings.DEFAULT_DOWNSAMPLE_MAX_LENGTH;
        }
        
        int minLengthConstant = DtwSettings.DEFAULT_MIN_ALLOWED_GESTURE_LENGTH;
        DtwSettings.MinimumLengthRestriction minLengthType = DtwSettings.MinimumLengthRestriction.SET_FROM_EXAMPLES;
        if (comboMatchLength.getSelectedIndex() == 0 ) {
            minLengthConstant = Integer.parseInt(textMinLength.getText());
            minLengthType = DtwSettings.MinimumLengthRestriction.SET_CONSTANT;
        }
        

        int hopSize = Integer.parseInt(textHopSize.getText());
        int matchWidth = Integer.parseInt(textMatchWidth.getText());

        return new DtwSettings(matchWidth, 
                minLengthConstant, 
                hopSize, 
                rt,
                minLengthType, 
                policy, 
                downsampleMax, 
                downsampleFactor);
    }

    public boolean validateForm() {
        if (!Util.checkIsPositiveNumber(textHopSize, "hop size", this)) {
            return false;
        }
        if (!Util.checkIsPositiveNumber(textMatchWidth, "match width", this)) {
            return false;
        }

        if (comboDownsample.getSelectedIndex() == 0) {
            if (!Util.checkIsPositiveNumber(textDownsampleMax, "downsample max length", this)) {
                return false;
            }
        } else if (comboDownsample.getSelectedIndex() == 1) {
            if (!Util.checkIsPositiveNumber(textDownsampleConstant, "downsample length", this)) {
                return false;
            }
        }
        if (comboMatchLength.getSelectedIndex() == 0) {
            if (!Util.checkIsPositiveNumber(textMinLength, "minimum match length", this)) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textMatchWidth = new javax.swing.JTextField();
        comboRunningType = new javax.swing.JComboBox();
        textHopSize = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        buttonReset = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        comboDownsample = new javax.swing.JComboBox();
        panelDownsampleCards = new javax.swing.JPanel();
        panelDownsampleMax = new javax.swing.JPanel();
        textDownsampleMax = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        panelDownsampleConstant = new javax.swing.JPanel();
        textDownsampleConstant = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        panelNoDownsample = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        comboMatchLength = new javax.swing.JComboBox();
        panelMinLengthCards = new javax.swing.JPanel();
        panelMinLengthValue = new javax.swing.JPanel();
        textMinLength = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        panelMinLengthFromExamples = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        textMatchWidth.setText("100");
        textMatchWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textMatchWidthKeyTyped(evt);
            }
        });

        comboRunningType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Matches computed continuously while running", "Matches computed only once running stops" }));
        comboRunningType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboRunningTypeActionPerformed(evt);
            }
        });

        textHopSize.setText("1");
        textHopSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textHopSizeActionPerformed(evt);
            }
        });
        textHopSize.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textHopSizeKeyTyped(evt);
            }
        });

        jLabel1.setText("Match width");

        jLabel2.setText("Match hop size");

        buttonReset.setText("Reset to defaults");
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel7.setText("lower = faster, more selective");

        jLabel8.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel8.setText("lower = faster, more selective");

        comboDownsample.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Downsample so examples have max length of:", "Downsample by constant rate:", "Don't downsample" }));
        comboDownsample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboDownsampleActionPerformed(evt);
            }
        });

        panelDownsampleCards.setLayout(new java.awt.CardLayout());

        panelDownsampleMax.setBackground(new java.awt.Color(255, 255, 255));

        textDownsampleMax.setText("10");
        textDownsampleMax.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textDownsampleMaxKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textDownsampleMaxKeyPressed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel12.setText("<html>lower = faster to compute, possibly less accurate</html>");

        javax.swing.GroupLayout panelDownsampleMaxLayout = new javax.swing.GroupLayout(panelDownsampleMax);
        panelDownsampleMax.setLayout(panelDownsampleMaxLayout);
        panelDownsampleMaxLayout.setHorizontalGroup(
            panelDownsampleMaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDownsampleMaxLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(textDownsampleMax, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelDownsampleMaxLayout.setVerticalGroup(
            panelDownsampleMaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textDownsampleMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelDownsampleCards.add(panelDownsampleMax, "cardDownsampleMax");

        panelDownsampleConstant.setBackground(new java.awt.Color(255, 255, 255));

        textDownsampleConstant.setText("10");
        textDownsampleConstant.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textDownsampleConstantKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textDownsampleConstantKeyPressed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel13.setText("<html>higher = faster to compute, possibly less accurate</html>");

        javax.swing.GroupLayout panelDownsampleConstantLayout = new javax.swing.GroupLayout(panelDownsampleConstant);
        panelDownsampleConstant.setLayout(panelDownsampleConstantLayout);
        panelDownsampleConstantLayout.setHorizontalGroup(
            panelDownsampleConstantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDownsampleConstantLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(textDownsampleConstant, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelDownsampleConstantLayout.setVerticalGroup(
            panelDownsampleConstantLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textDownsampleConstant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelDownsampleCards.add(panelDownsampleConstant, "cardDownsampleConstant");

        panelNoDownsample.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel9.setText("This may be slow if inputs are arriving at a fast rate");

        javax.swing.GroupLayout panelNoDownsampleLayout = new javax.swing.GroupLayout(panelNoDownsample);
        panelNoDownsample.setLayout(panelNoDownsampleLayout);
        panelNoDownsampleLayout.setHorizontalGroup(
            panelNoDownsampleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNoDownsampleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelNoDownsampleLayout.setVerticalGroup(
            panelNoDownsampleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNoDownsampleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelDownsampleCards.add(panelNoDownsample, "cardNoDownsample");

        comboMatchLength.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Continuous matches use a minimum length of:", "Continuous matches use a min length equal to the shortest example" }));
        comboMatchLength.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMatchLengthActionPerformed(evt);
            }
        });

        panelMinLengthCards.setLayout(new java.awt.CardLayout());

        panelMinLengthValue.setBackground(new java.awt.Color(255, 255, 255));

        textMinLength.setText("10");
        textMinLength.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textMinLengthKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textMinLengthKeyPressed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel11.setText("<html>lower = possibly more likely to match, but longer time to compute</html>");

        javax.swing.GroupLayout panelMinLengthValueLayout = new javax.swing.GroupLayout(panelMinLengthValue);
        panelMinLengthValue.setLayout(panelMinLengthValueLayout);
        panelMinLengthValueLayout.setHorizontalGroup(
            panelMinLengthValueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMinLengthValueLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(textMinLength, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelMinLengthValueLayout.setVerticalGroup(
            panelMinLengthValueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMinLengthValueLayout.createSequentialGroup()
                .addGroup(panelMinLengthValueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textMinLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelMinLengthCards.add(panelMinLengthValue, "cardMinFromValue");

        panelMinLengthFromExamples.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Lucida Grande", 2, 10)); // NOI18N
        jLabel10.setText("Very short examples may lead to undesired behaviours");

        javax.swing.GroupLayout panelMinLengthFromExamplesLayout = new javax.swing.GroupLayout(panelMinLengthFromExamples);
        panelMinLengthFromExamples.setLayout(panelMinLengthFromExamplesLayout);
        panelMinLengthFromExamplesLayout.setHorizontalGroup(
            panelMinLengthFromExamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMinLengthFromExamplesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(72, Short.MAX_VALUE))
        );
        panelMinLengthFromExamplesLayout.setVerticalGroup(
            panelMinLengthFromExamplesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMinLengthFromExamplesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        panelMinLengthCards.add(panelMinLengthFromExamples, "cardMinFromExamples");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(comboRunningType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelMinLengthCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelDownsampleCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboDownsample, 0, 0, Short.MAX_VALUE)
                    .addComponent(comboMatchLength, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textMatchWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textHopSize, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8)
                    .addComponent(buttonReset))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(comboRunningType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboDownsample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panelDownsampleCards, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboMatchLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panelMinLengthCards, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textMatchWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textHopSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonReset)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboRunningTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboRunningTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboRunningTypeActionPerformed

    private void textMatchWidthKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textMatchWidthKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_textMatchWidthKeyTyped

    private void textHopSizeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textHopSizeKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_textHopSizeKeyTyped

    private void textHopSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textHopSizeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textHopSizeActionPerformed

    private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
        setToDefaults();
    }//GEN-LAST:event_buttonResetActionPerformed

    private void setToDefaults() {
        comboRunningType.setSelectedIndex(0);
        comboDownsample.setSelectedIndex(0);
        textDownsampleMax.setText(Integer.toString(DtwSettings.DEFAULT_DOWNSAMPLE_MAX_LENGTH));
        textDownsampleConstant.setText(Integer.toString(DtwSettings.DEFAULT_DOWNSAMPLE_FACTOR));
        comboMatchLength.setSelectedIndex(0);
        textMinLength.setText(Integer.toString(DtwSettings.DEFAULT_MIN_ALLOWED_GESTURE_LENGTH));
        textHopSize.setText(Integer.toString(DtwSettings.DEFAULT_HOP_SIZE_FOR_CONTINUOUS_SEARCH));
        textMatchWidth.setText(Integer.toString(DtwSettings.DEFAULT_MATCH_WIDTH));
    }

    private void textDownsampleMaxKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textDownsampleMaxKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_textDownsampleMaxKeyPressed

    private void textDownsampleMaxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textDownsampleMaxKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_textDownsampleMaxKeyTyped

    private void comboDownsampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboDownsampleActionPerformed
        CardLayout c = (CardLayout) panelDownsampleCards.getLayout();
        if (comboDownsample.getSelectedIndex() == 0) {
            c.show(panelDownsampleCards, "cardDownsampleMax");
        } else if (comboDownsample.getSelectedIndex() == 1) {
            c.show(panelDownsampleCards, "cardDownsampleConstant");
        } else {
            c.show(panelDownsampleCards, "cardNoDownsample");
        }
    }//GEN-LAST:event_comboDownsampleActionPerformed

    private void comboMatchLengthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMatchLengthActionPerformed
        CardLayout c = (CardLayout) panelMinLengthCards.getLayout();
        if (comboMatchLength.getSelectedIndex() == 0) {
            c.show(panelMinLengthCards, "cardMinFromValue");
        } else {
            c.show(panelMinLengthCards, "cardMinFromExamples");
        }

    }//GEN-LAST:event_comboMatchLengthActionPerformed

    private void textMinLengthKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textMinLengthKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_textMinLengthKeyTyped

    private void textMinLengthKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textMinLengthKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_textMinLengthKeyPressed

    private void textDownsampleConstantKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textDownsampleConstantKeyTyped
        char enter = evt.getKeyChar();
        if (!(Character.isDigit(enter))) {
            evt.consume();
        }
    }//GEN-LAST:event_textDownsampleConstantKeyTyped

    private void textDownsampleConstantKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textDownsampleConstantKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_textDownsampleConstantKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonReset;
    private javax.swing.JComboBox comboDownsample;
    private javax.swing.JComboBox comboMatchLength;
    private javax.swing.JComboBox comboRunningType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel panelDownsampleCards;
    private javax.swing.JPanel panelDownsampleConstant;
    private javax.swing.JPanel panelDownsampleMax;
    private javax.swing.JPanel panelMinLengthCards;
    private javax.swing.JPanel panelMinLengthFromExamples;
    private javax.swing.JPanel panelMinLengthValue;
    private javax.swing.JPanel panelNoDownsample;
    private javax.swing.JTextField textDownsampleConstant;
    private javax.swing.JTextField textDownsampleMax;
    private javax.swing.JTextField textHopSize;
    private javax.swing.JTextField textMatchWidth;
    private javax.swing.JTextField textMinLength;
    // End of variables declaration//GEN-END:variables
}
