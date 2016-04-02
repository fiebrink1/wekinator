/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui.path;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import wekimini.LearningModelBuilder;
import wekimini.LoggingManager;
import wekimini.Path;
import wekimini.Wekinator;
import wekimini.gui.path.ModelEditorFrame.ModelBuilderReceiver;
import wekimini.gui.path.OutputEditFrame.OutputEditReceiver;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.learning.Model;
import wekimini.osc.OSCClassificationOutput;
import wekimini.osc.OSCNumericOutput;
import wekimini.osc.OSCOutput;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class PathEditorFrame extends javax.swing.JFrame {

    private final static HashMap<Path, PathEditorFrame> pathsBeingEdited = new HashMap<>();
    private final Path p;
    private static final Logger logger = Logger.getLogger(PathEditorFrame.class.getName());
    private OutputEditFrame outputEditor = null;
    private OSCOutput newOutput = null;
    private ModelEditorFrame modelEditor = null;
    private LearningModelBuilder newModelBuilder = null;
    private boolean hasValidModelType = false;
    private JCheckBox inputs[] = null;
    private String[] inputNames = null;
    private Wekinator w;
    /**
     * Creates new form PathEditorFrame
     */
    public PathEditorFrame() {
        initComponents();
        p = null;
    }

    public PathEditorFrame(Path p, String[] inputNames, Wekinator w) {
        initComponents();
        
        jButton1.setVisible(false);
        jButton2.setVisible(false);
        
        this.p = p;
        initFormForPath();
        this.w = w;
        initInputsPanel(p, inputNames);
        setTitle("Editing " + p.getOSCOutput().getName());
    }

    private void initInputsPanel(Path p, String[] inputNames) {
        panelInputList.removeAll();
        this.inputNames = new String[inputNames.length];
        System.arraycopy(inputNames, 0, this.inputNames, 0, inputNames.length);
        inputs = new JCheckBox[inputNames.length];
        for (int i = 0; i < inputNames.length; i++) {
            final int which = i;
            inputs[i] = new JCheckBox(inputNames[i]);
            inputs[i].setBackground(new java.awt.Color(255, 255, 255));
            inputs[i].setSelected(p.isUsingInput(inputNames[i]));
            inputs[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    mouseClick(which);
                }
            });
            inputs[i].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    updateNumInputs();
                }
            });
            panelInputList.add(inputs[i]);
        }
        updateNumInputs();
    }

    private void mouseClick(int whichCheckbox) {
        //System.out.println("Click on " + whichCheckbox);
    }

    private void initFormForPath() {
        initModelType();
        updateFormForOutput(p.getOSCOutput());
    }

    private void initModelType() {
        labelModelType.setText(p.getModelBuilder().getPrettyName());
        hasValidModelType = true;
    }

    private void updateNumInputs() {
        labelConnectedInputs.setText(getNumberInputsSelected() + " connected inputs:");
    }

    private void updateFormForOutput(OSCOutput o) {
        labelOutputName.setText("Name: " + o.getName());

        StringBuilder sb = new StringBuilder("<html>");
        if (o instanceof OSCNumericOutput) {
            OSCNumericOutput no = (OSCNumericOutput) o;
            sb.append("Numeric output, ");
            if (no.getOutputType() == OSCNumericOutput.NumericOutputType.REAL) {
                sb.append("real values<br>");
            } else {
                sb.append("integer values<br>");
            }

            sb.append("Values between min=");
            sb.append(Util.prettyDecimalFormat(no.getMin(), 2));
            sb.append(" and max=").append(Util.prettyDecimalFormat(no.getMax(), 2));

            if (no.getLimitType() == OSCNumericOutput.LimitType.HARD) {
                sb.append(" (hard limits)</html>");
            } else {
                sb.append(" (soft limits)</html>");
            }
        } else if (o instanceof OSCClassificationOutput) {
            sb.append("Classification output with ");
            sb.append(((OSCClassificationOutput) o).getNumClasses());
            sb.append(" classes");
            if (((OSCClassificationOutput) o).isSendingDistribution()) {
                sb.append("<br>Sending probabilities using OSC message /");
                sb.append(o.getName());
            }
            sb.append("</html>");
        } else {
            sb.append("Unknown type</html>");
            logger.log(Level.SEVERE, "Uknown output type: {0}", o.getClass().getCanonicalName());
        }
        labelOutputType.setText(sb.toString());
    }

    /*public void initInputList() {
     String[] inputNames = p.getSelectedInputs();
     labelConnectedInputs.setText(inputNames.length + " connected inputs:");
        
     panelInputList.removeAll();
     for (int i = 0; i < inputNames.length; i++) {
     panelInputList.add(new JLabel(inputNames[i]));
     }
     panelInputList.repaint();
     panelInputList.validate();
     scrollPaneInputs.revalidate();
        
     } */
    public static boolean pathEditorExists(Path p) {
        return pathsBeingEdited.containsKey(p);
    }

    public static PathEditorFrame getEditorForPath(Path p, String[] inputs, Wekinator w) {
        if (pathEditorExists(p)) {
            return pathsBeingEdited.get(p);
        } else {
            PathEditorFrame pef = new PathEditorFrame(p, inputs, w);
            pathsBeingEdited.put(p, pef);
            return pef;
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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        buttonEditOutput = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        labelOutputType = new javax.swing.JLabel();
        labelOutputName = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        buttonEditModelType = new javax.swing.JButton();
        labelModelType = new javax.swing.JLabel();
        buttonPrintToConsole = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        labelConnectedInputs = new javax.swing.JLabel();
        scrollPaneInputs = new javax.swing.JScrollPane();
        panelInputList = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jButton1.setText("Load from file...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Save to file...");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Apply changes");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonEditOutput.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonEditOutput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/pencil2.png"))); // NOI18N
        buttonEditOutput.setToolTipText("Edit this model");
        buttonEditOutput.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonEditOutput.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonEditOutput.setPreferredSize(new java.awt.Dimension(30, 30));
        buttonEditOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditOutputActionPerformed(evt);
            }
        });

        jLabel3.setText("Type:");

        labelOutputType.setText("<html>Continuous output, real values<br>Values between Min=0.2, Max=0.3 (soft limits)<br></html>");

        labelOutputName.setText("Name: Output 1");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonEditOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelOutputName, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                        .addContainerGap(81, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelOutputType, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonEditOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelOutputName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelOutputType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Model type:");

        buttonEditModelType.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonEditModelType.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/pencil2.png"))); // NOI18N
        buttonEditModelType.setToolTipText("Edit this model");
        buttonEditModelType.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonEditModelType.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonEditModelType.setPreferredSize(new java.awt.Dimension(30, 30));
        buttonEditModelType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditModelTypeActionPerformed(evt);
            }
        });

        labelModelType.setText("Neural network");
        labelModelType.setMaximumSize(new java.awt.Dimension(259, 16));
        labelModelType.setMinimumSize(new java.awt.Dimension(259, 16));
        labelModelType.setPreferredSize(new java.awt.Dimension(259, 16));

        buttonPrintToConsole.setText("Display in console");
        buttonPrintToConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrintToConsoleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonEditModelType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelModelType, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPrintToConsole)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelModelType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonPrintToConsole)
                            .addComponent(buttonEditModelType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelConnectedInputs.setText("12 Connected inputs:");

        panelInputList.setBackground(new java.awt.Color(255, 255, 255));
        panelInputList.setLayout(new javax.swing.BoxLayout(panelInputList, javax.swing.BoxLayout.Y_AXIS));

        jCheckBox1.setText("jCheckBox1");
        jCheckBox1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBox1MouseClicked(evt);
            }
        });
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        panelInputList.add(jCheckBox1);

        jCheckBox2.setText("jCheckBox1");
        panelInputList.add(jCheckBox2);

        jCheckBox3.setText("jCheckBox1");
        panelInputList.add(jCheckBox3);

        scrollPaneInputs.setViewportView(panelInputList);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(labelConnectedInputs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(6, 6, 6))
                    .addComponent(scrollPaneInputs, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelConnectedInputs, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneInputs, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton4.setText("Cancel");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addGap(0, 0, 0)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(0, 0, 0)
                .addComponent(jButton3))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonEditModelTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditModelTypeActionPerformed
        showModelBuilderEditor();
    }//GEN-LAST:event_buttonEditModelTypeActionPerformed

    private void buttonEditOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditOutputActionPerformed
        showOutputEditor();
    }//GEN-LAST:event_buttonEditOutputActionPerformed

    private void showOutputEditor() {
        if (outputEditor != null) {
            outputEditor.dispose();
        }
        //if (outputEditor == null) {
        OutputEditReceiver r = new OutputEditFrame.OutputEditReceiver() {
            @Override
            public void outputReady(OSCOutput o) {
                newOutputReceived(o);
            }

            @Override
            public void outputEditorCancelled() {
                outputEditor = null;
            }

        };
        if (newOutput != null) {
            outputEditor = new OutputEditFrame(newOutput, r);
        } else {
            outputEditor = new OutputEditFrame(p.getOSCOutput(), r);
        }
        outputEditor.setVisible(true);
        /*  } else {
         outputEditor.setVisible(true);
         outputEditor.toFront();
         } */
    }

    private void newOutputReceived(OSCOutput o) {
        newOutput = o;
        updateFormForOutput(o);
        updateModelForOutput(o);
        outputEditor = null;
    }

    private void updateModelForOutput(OSCOutput o) {
        if (p.getModelBuilder().isCompatible(o)) {
            hasValidModelType = true;
            labelModelType.setText(p.getModelBuilder().getPrettyName());
        } else {
            hasValidModelType = false;
            labelModelType.setText("None - Please edit");
        }
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (validateForm()) {
            applyChanges();
            dispose();
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        loadModelFromFile();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        saveModelToFile();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jCheckBox1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBox1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1MouseClicked

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void buttonPrintToConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrintToConsoleActionPerformed
        w.showConsole();
        Model m = p.getModel();
        if (m == null) {
            logger.log(LoggingManager.USER_INFO, "Model is not trained");
        } else {
            logger.log(LoggingManager.USER_INFO, m.getModelDescription());
        }
        KadenzeLogging.getLogger().logSupervisedModelPrintedToConsole(w, p);
    }//GEN-LAST:event_buttonPrintToConsoleActionPerformed

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
            java.util.logging.Logger.getLogger(PathEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PathEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PathEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PathEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PathEditorFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonEditModelType;
    private javax.swing.JButton buttonEditOutput;
    private javax.swing.JButton buttonPrintToConsole;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labelConnectedInputs;
    private javax.swing.JLabel labelModelType;
    private javax.swing.JLabel labelOutputName;
    private javax.swing.JLabel labelOutputType;
    private javax.swing.JPanel panelInputList;
    private javax.swing.JScrollPane scrollPaneInputs;
    // End of variables declaration//GEN-END:variables

    private void showModelBuilderEditor() {
        if (modelEditor != null) {
            modelEditor.dispose();
        }

        // if (modelEditor == null) {
        ModelBuilderReceiver r = new ModelBuilderReceiver() {
            @Override
            public void modelBuilderReady(LearningModelBuilder mb) {
                newModelBuilderReady(mb);
            }

            @Override
            public void modelBuilderCancelled() {
                modelEditor = null;
            }
        };

        boolean isClassifier;
        if (newOutput != null) {
            isClassifier = (newOutput instanceof OSCClassificationOutput);
        } else {
            isClassifier = (p.getOSCOutput() instanceof OSCClassificationOutput);
        }
        LearningModelBuilder m;
        if (newModelBuilder != null) {
            m = newModelBuilder;
        } else {
            m = p.getModelBuilder();
        }
        modelEditor = new ModelEditorFrame(m, r, isClassifier);
        modelEditor.setVisible(true);
        /* } else {
         modelEditor.setVisible(true);
         modelEditor.toFront();
         } */
    }

    private void newModelBuilderReady(LearningModelBuilder mb) {
        newModelBuilder = mb;
        labelModelType.setText(mb.getPrettyName());
        //Error check:
        if (newOutput == null) {
            if (!mb.isCompatible(p.getOSCOutput())) {
                logger.log(Level.WARNING, "Trying to set incompatible model and output");
            }
        } else if (!mb.isCompatible(newOutput)) {
            logger.log(Level.WARNING, "Trying to set incompatible model and output");
        }
        modelEditor = null;
    }

    private boolean validateForm() {
        if (getNumberInputsSelected() == 0) {
            Util.showPrettyErrorPane(this, "At least one input must be selected");
            return false;
        }

        OSCOutput o;
        if (newOutput != null) {
            o = newOutput; //TODO CHECK THE FOLLOWING:
            if (! o.getName().equals(p.getOSCOutput().getName())) {
                //Output name has changed
                if (w.getOutputManager().containsOutputName(o.getName())) {
                    Util.showPrettyErrorPane(this, "Output name " + o.getName() 
                            + " is being used by a different output. Please choose a different name.");
                    return false;
                }
            }
            
        } else {
            o = p.getOSCOutput();
        }

        LearningModelBuilder mb;
        if (newModelBuilder != null) {
            mb = newModelBuilder;
        } else {
            mb = p.getModelBuilder();
        }
        if (!mb.isCompatible(o)) {
            Util.showPrettyErrorPane(this, "This output cannot be used your previous model type (" + mb.getPrettyName()
                    + "). Please select a different model type.");
            return false;
        }

        if (newOutput != null) {
            boolean v = validateOutputChange();
            if (!v) {
                return false;
            }
        }

        return true;
    }

    //Do we know how and whether to change this output?
    private boolean validateOutputChange() {
        OSCOutput oldOutput = p.getOSCOutput();
        if (newOutput instanceof OSCClassificationOutput && oldOutput instanceof OSCClassificationOutput) {
            //Check if # classes is different, and prompt if new numclasses < old num
            int oldNum = ((OSCClassificationOutput) oldOutput).getNumClasses();
            int newNum = ((OSCClassificationOutput) newOutput).getNumClasses();
            if (oldNum > newNum) {

                int proceed = Util.showPrettyOptionPane(this, "You are changing the number of classes from "
                        + oldNum + " to " + newNum + ". This will result in all existing examples "
                        + "with classes higher than " + newNum + " being deleted from this model's"
                        + " training set. (No other models will be affected.) Do you wish to proceed?", "Warning");

                if (proceed == Util.CANCEL_OPTION) {
                    return false;
                }
            }
        } else if (newOutput instanceof OSCClassificationOutput && oldOutput instanceof OSCNumericOutput) {
            Util.showPrettyErrorPane(this, "Currently it is not possible to change numeric ouput into a classification output");
            return false;
            //Ask whether to
            // delete all old instances
            // round all to nearest class
            // round to nearest class if valid, but discard otherwise
        } else if (newOutput instanceof OSCNumericOutput && oldOutput instanceof OSCClassificationOutput) {
            Util.showPrettyErrorPane(this, "Currently it is not possible to change classification ouput into a numeric output");
            return false;
            //Aks whether to delete or keep & treat as classifier
            // If keep, ask about any out of bounds data
        } else { //Numeric -> Numeric
            //Check on ranges, limits
            if (((OSCNumericOutput) newOutput).getLimitType() == OSCNumericOutput.LimitType.HARD) {
                float newMin = ((OSCNumericOutput) newOutput).getMin();
                float newMax = ((OSCNumericOutput) newOutput).getMax();
                float oldMin = ((OSCNumericOutput) p.getOSCOutput()).getMin();
                float oldMax = ((OSCNumericOutput) p.getOSCOutput()).getMax();
                if (oldMin < newMin || oldMax > newMax) {
                    //New limits are more restrictive
                    int proceed = Util.showPrettyOptionPane(this, "You are using HARD limits and changing the limit values from "
                            + "(" + oldMin + ", " + oldMax + ") to (" + newMin + ", " + newMax
                            + "). This will result in all existing examples "
                            + "with output values outside (" + newMin + ", " + newMax
                            + ") being deleted from this model's"
                            + " training set. (No other models will be affected.) Do you wish to proceed?", "Warning");
                    if (proceed == Util.CANCEL_OPTION) {
                        return false;
                    }
                }
            }
        } // end numeric -> numeric
        return true;
    }

    private int getNumberInputsSelected() {
        int sum = 0;
        for (JCheckBox j : inputs) {
            if (j.isSelected()) {
                sum++;
            }
        }
        return sum;
    }
    
    private String[] getSelectedInputNames() {
        List<String> selected = new LinkedList<>();
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i].isSelected()) {
                selected.add(inputNames[i]);
            }
        }
        return selected.toArray(new String[0]);
    }

    private void applyChanges() {
      //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

      //if (newOutput != null) {
          w.getSupervisedLearningManager().updatePath(p, newOutput, newModelBuilder, getSelectedInputNames());
          
         //w.getOutputManager().updateOutput(newOutput, p.getOSCOutput());
       // w.getSupervisedLearningManager().updateOutput()
        //New P or existing p??
        //Need to update model state, path output name, 
    //  }
  //TODO: also Update output info: XOutputManager, learning manager, data manager, sender?, path, all GUIs (through listeners)
      
     /* if (newModelBuilder != null) {
          p.setModelBuilder(newModelBuilder);
      }
      
      p.setSelectedInputs(getSelectedInputNames()); */
      
        //Update modelbuilder: path
        //Update input list: Path, learning manager, data manager
    }

    private void loadModelFromFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void saveModelToFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
