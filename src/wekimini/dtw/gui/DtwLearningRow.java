/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.dtw.gui;

//import com.sun.glass.events.KeyEvent;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import wekimini.Path;
import wekimini.WekiMiniRunner;
import wekimini.Wekinator;
import wekimini.learning.dtw.DtwData;
import wekimini.learning.dtw.DtwModel;
import wekimini.osc.OSCDtwOutput;
import wekimini.osc.OSCNumericOutput;

/**
 *
 * @author fiebrink
 */
public class DtwLearningRow extends javax.swing.JPanel {

    private Wekinator w;
    private String gestureName = "gestureName";
    private int versionNumber;

    private boolean gestureEnabled = true;

    private final ImageIcon playIconOn = new ImageIcon(getClass().getResource("/wekimini/icons/play1.png"));
    private final ImageIcon playIconOff = new ImageIcon(getClass().getResource("/wekimini/icons/noplay1.png"));

    private double matchValue = 1.0;

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final ImageIcon noMatchIcon = new ImageIcon(getClass().getResource("/wekimini/icons/whitelight.png")); // NOI18N
    private final ImageIcon matchIcon = new ImageIcon(getClass().getResource("/wekimini/icons/greenlight.png")); // NOI18N
    private static final Logger logger = Logger.getLogger(DtwLearningRow.class.getName());

    private final int gestureNum;
    private final DtwModel myModel;

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport != null) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }
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
     * Creates new form Parameterless constructor just for GUI layout testing
     *
     */
    public DtwLearningRow() {
        initComponents();
        gestureNum = 0;
        myModel = null;
    }

    public DtwLearningRow(Wekinator w, int whichGesture) {
        initComponents();
        this.w = w;
        this.myModel = w.getDtwLearningManager().getModel();
        this.gestureNum = whichGesture;
        this.gestureName = myModel.getGestureName(whichGesture);
        this.versionNumber = myModel.getVersionNumber(whichGesture);
        myModel.getData().addDataListener(new DtwData.DtwDataListener() {

            @Override
            public void exampleAdded(int whichClass) {
            }

            @Override
            public void exampleDeleted(int whichClass) {
            }

            @Override
            public void numExamplesChanged(int whichClass, int currentNumExamples) {
                if (whichClass == gestureNum) {
                    dataNumExamplesChanged(currentNumExamples);
                }
            }

            @Override
            public void allExamplesDeleted() {
            }
        });
        
        myModel.addNormalizedDtwUpdateListener(new DtwModel.DtwUpdateListener1() {

            @Override
            public void dtwUpdateReceived(double[] normalizedDistances) {
                updateCurrentDistance(normalizedDistances[gestureNum]);
            }
        });

        myModel.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DtwModel.PROP_CURRENT_MATCH)) {
                    updateIcon(myModel.getCurrentMatch() == gestureNum);
                } else if (evt.getPropertyName().equals(DtwModel.PROP_VERSIONNUMBERS)) {
                    if (((IndexedPropertyChangeEvent)evt).getIndex() == gestureNum) {
                        setVersionNumber(myModel.getVersionNumber(gestureNum));
                    }
                }
            }
        });
        
        myModel.getOSCOutput().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OSCDtwOutput.PROP_GESTURE_NAMES)) {
                    updatePossibleNameChange();
                }
            }
        });
        
        initForGesture();
    }
    
    private void updatePossibleNameChange() {
        setGestureName(myModel.getOSCOutput().getGestureNames(gestureNum));
    }

    public void setGestureName(String name) {
        gestureName = name;
        updateNameLabel();
    }
    
    public void setVersionNumber(int newVersion) {
        versionNumber = newVersion;
        updateNameLabel();
    }

    private void updateNameLabel() {
        labelModelName.setText(gestureName + " (v" + versionNumber + ")");
    }

    private void dataNumExamplesChanged(int currentNum) {
        labelNumExamples.setText(Integer.toString(currentNum));
        updateButtonsForNum(currentNum);
    }

    private void updateButtonsForNum(int currentNum) {
        buttonDelete.setEnabled(currentNum != 0);
    }

    private void initForGesture() {
        updateNameLabel();
        updateIcon(false);
        int numExamples = myModel.getData().getNumExamplesForGesture(gestureNum);
        labelNumExamples.setText(Integer.toString(numExamples));
        updateRunEnabled(myModel.isGestureActive(gestureNum));
    }

    private void updateIcon(boolean isMatch) {
        if (isMatch) {
            iconMatch.setIcon(matchIcon);
        } else {
            iconMatch.setIcon(noMatchIcon);
        }
    }

    private void updateNumExamples(int num) {
        labelNumExamples.setText(Integer.toString(num));
    }

    private void updateRunEnabled(boolean enabled) {
        gestureEnabled = enabled;
        if (gestureEnabled) {
            buttonLearnerPlay.setIcon(playIconOn); // NOI18N
        } else {
            buttonLearnerPlay.setIcon(playIconOff);
        }
    }

    public void setRunEnabled(boolean e) {
        gestureEnabled = e;
        if (gestureEnabled) {
            buttonLearnerPlay.setIcon(playIconOn); // NOI18N
            distanceBar.setEnabled(true);
        } else {
            buttonLearnerPlay.setIcon(playIconOff);
            distanceBar.setValue(0);
            distanceBar.setEnabled(true);
        }
        
        myModel.setGestureActive(gestureNum, e); //XXX make property change
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
                    DtwLearningRow r = new DtwLearningRow(w, 0);
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
        panelMain = new javax.swing.JPanel();
        labelModelName = new javax.swing.JLabel();
        labelNumExamples = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        buttonDeleteLearnerExamples = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        warningPanel = new javax.swing.JPanel();
        buttonLearnerPlay = new javax.swing.JButton();
        buttonAdd = new javax.swing.JButton();
        iconMatch = new javax.swing.JLabel();
        distanceBar = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        buttonDelete = new javax.swing.JButton();
        buttonViewExamples = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(568, 74));
        setMinimumSize(new java.awt.Dimension(568, 74));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(28, 74));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 74, Short.MAX_VALUE)
        );

        add(jPanel1);

        panelMain.setBackground(new java.awt.Color(255, 255, 255));
        panelMain.setMaximumSize(new java.awt.Dimension(540, 70));
        panelMain.setPreferredSize(new java.awt.Dimension(540, 70));

        labelModelName.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        labelModelName.setText("Gesture_1_v5");
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
        buttonDeleteLearnerExamples.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteLearnerExamplesActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

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
            .addGap(0, 29, Short.MAX_VALUE)
        );

        buttonLearnerPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/play1.png"))); // NOI18N
        buttonLearnerPlay.setToolTipText("Enable running on new data");
        buttonLearnerPlay.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonLearnerPlay.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonLearnerPlay.setPreferredSize(new java.awt.Dimension(30, 30));
        buttonLearnerPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLearnerPlayActionPerformed(evt);
            }
        });

        buttonAdd.setText("+");
        buttonAdd.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                buttonAddMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonAddMouseReleased(evt);
            }
        });
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });
        buttonAdd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                buttonAddKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buttonAddKeyReleased(evt);
            }
        });

        iconMatch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/yellowlight.png"))); // NOI18N
        iconMatch.setToolTipText("");
        iconMatch.setAlignmentX(0.5F);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel1.setText("degree of match:");

        buttonDelete.setText("-");
        buttonDelete.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteActionPerformed(evt);
            }
        });

        buttonViewExamples.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        buttonViewExamples.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wekimini/icons/mag3.png"))); // NOI18N
        buttonViewExamples.setToolTipText("Edit this model");
        buttonViewExamples.setMaximumSize(new java.awt.Dimension(30, 30));
        buttonViewExamples.setMinimumSize(new java.awt.Dimension(30, 30));
        buttonViewExamples.setPreferredSize(new java.awt.Dimension(30, 30));
        buttonViewExamples.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonViewExamplesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(distanceBar, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iconMatch))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addComponent(labelModelName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(labelNumExamples)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonViewExamples, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonDeleteLearnerExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonLearnerPlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(73, 73, 73)))
                .addGap(0, 0, 0)
                .addComponent(warningPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(warningPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonDeleteLearnerExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelNumExamples, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labelModelName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(buttonAdd)
                                .addComponent(buttonDelete))
                            .addComponent(buttonLearnerPlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonViewExamples, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(distanceBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(iconMatch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(panelMain);
    }// </editor-fold>//GEN-END:initComponents

    private void toggleLearnerRun() {
        gestureEnabled = !gestureEnabled;
        setRunEnabled(gestureEnabled);
    }

    private void buttonDeleteLearnerExamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteLearnerExamplesActionPerformed
        myModel.getData().deleteExamplesForGesture(gestureNum);
    }//GEN-LAST:event_buttonDeleteLearnerExamplesActionPerformed


    private void buttonLearnerPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLearnerPlayActionPerformed
        toggleLearnerRun();
    }//GEN-LAST:event_buttonLearnerPlayActionPerformed

    private void buttonViewExamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonViewExamplesActionPerformed
        w.getMainGUI().showDtwData(gestureNum);
    }//GEN-LAST:event_buttonViewExamplesActionPerformed

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed
        //myModel.startRecording(gestureNum);
    }//GEN-LAST:event_buttonAddActionPerformed

    private void buttonAddMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonAddMousePressed
        myModel.startRecording(gestureNum);
    }//GEN-LAST:event_buttonAddMousePressed

    private void buttonAddKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buttonAddKeyPressed
      //  myModel.startRecording(gestureNum);
    }//GEN-LAST:event_buttonAddKeyPressed

    private void buttonAddKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buttonAddKeyReleased
       // myModel.stopRecording();
    }//GEN-LAST:event_buttonAddKeyReleased

    private void buttonAddMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonAddMouseReleased
        myModel.stopRecording(); //TODO TEST THIS IF MOUSE EXITSBEFORE RELEASE! XXX
    }//GEN-LAST:event_buttonAddMouseReleased

    private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteActionPerformed
        myModel.getData().deleteMostRecentExample(gestureNum);
    }//GEN-LAST:event_buttonDeleteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonDelete;
    private javax.swing.JButton buttonDeleteLearnerExamples;
    private javax.swing.JButton buttonLearnerPlay;
    private javax.swing.JButton buttonViewExamples;
    private javax.swing.JProgressBar distanceBar;
    private javax.swing.JLabel iconMatch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelModelName;
    private javax.swing.JLabel labelNumExamples;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel warningPanel;
    // End of variables declaration//GEN-END:variables

    private void updateCurrentDistance(double currentDistance) {
        distanceBar.setValue((int)currentDistance);
    }

   /* private void checkVersionChange() {
        if (versionNumber != w.getDtwLearningManager().getVersionNumber(gestureNum)) {
            setVersionNumber(w.getDtwLearningManager().getVersionNumber(gestureNum));
        }
    } */
}
