/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JToggleButton;

/**
 *
 * @author rebecca
 */
public class LabeledSelectionGrid_1 extends javax.swing.JFrame {

    private boolean isMouseDragging = false;
    private boolean isDraggingTurningOn = false;
    private boolean isClickTurningOn = false;
    private final ButtonLocation lastClickLocation = new ButtonLocation(-1, -1);
    private final ButtonLocation lastPressStart = new ButtonLocation(-1, -1);
    MyToggle[][] toggles;
   

    /**
     * Creates new form MockupInputSelection
     */
    public LabeledSelectionGrid_1(int numRows, int numCols) {

        initComponents();

        jPanel1.removeAll();
        jPanel1.setLayout(new java.awt.GridLayout(numRows, numCols));
        toggles = new MyToggle[numRows][numCols];
        for (int i = 0; i < numRows; i++) { 
            for (int j = 0; j < numCols; j++) {
                final int x = i;
                final int y = j;
                ButtonLocation bl = new ButtonLocation(x, y);
                MyToggle mt = new MyToggle(bl);
                mt.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        mouseExit(x, y);
                    }

                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        mouseEnter(x, y);
                    }

                    @Override
                    public void mousePressed(java.awt.event.MouseEvent evt) {
                        mousePress(x, y);
                    }

                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        mouseRelease(x, y);
                    }

                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        mouseClick(x, y, evt);
                    }
                }
                );
                /*mt.addActionListener(new ActionListener() {

                 @Override
                 public void actionPerformed(ActionEvent e) {
                 // System.out.println("Action " + x + "," + y);
                 }
                 }); */
                toggles[i][j] = mt;
                jPanel1.add(mt);
            }
        }
        repaint();
    }

    private void mouseExit(int x, int y) {
        if (isMouseDragging) {
            if (lastPressStart.x == x && lastPressStart.y == y) {
                toggles[x][y].setSelected(isDraggingTurningOn);
            }
        }
    }

    private void mouseEnter(int x, int y) {
        //System.out.println("enter " + x + y);
        if (isMouseDragging) {
            toggles[x][y].setSelected(isDraggingTurningOn);
        }
    }

    private void mousePress(int x, int y) {
       // System.out.println("press" + x + y);
        isMouseDragging = true;
        boolean selected = toggles[x][y].isSelected();
       // System.out.println("SElected =" + selected);
        //toggles[x][y].setSelected(!selected);
        isDraggingTurningOn = !selected;
        lastPressStart.x = x;
        lastPressStart.y = y;

    }

    private void mouseRelease(int x, int y) {
       // System.out.println("release" + x + y);
        if (isMouseDragging) {
            //for (int i = lastClickLocation.x; i < x; i++) {
            //    for (int j = lastClickLocation.y; j < y; j++) {
            //       toggles[i][j].setSelected(isDraggingTurningOn);

            //   }
            // }
            isMouseDragging = false;
        }
    }

    private void mouseClick(int x, int y, java.awt.event.MouseEvent evt) {
        if (evt.isShiftDown() && lastClickLocation.x != -1) {
            int startx, starty, endx, endy;
            if (lastClickLocation.x < x) {
                startx = lastClickLocation.x;
                endx = x;
              
            } else {
                startx = x;
                endx = lastClickLocation.x;
            }
            
            if (lastClickLocation.y < y) {
                starty = lastClickLocation.y;
                endy = y;
                
            } else {
                starty = y;
                endy = lastClickLocation.y;
            }
            
            for (int i = startx ; i <= endx; i++) {
                for (int j = starty; j <= endy; j++) {
                    toggles[i][j].setSelected(isClickTurningOn);

                }
            }

            lastClickLocation.x = -1;
            lastClickLocation.y = -1;
        } else {
           // System.out.println("B");
            isClickTurningOn = toggles[x][y].isSelected();
            //toggles[x][y].setSelected(isClickTurningOn);
            lastClickLocation.x = x;
            lastClickLocation.y = y;
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
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton9 = new javax.swing.JToggleButton();
        jToggleButton10 = new javax.swing.JToggleButton();
        jToggleButton11 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jPanel1.setMaximumSize(new java.awt.Dimension(100, 100));
        jPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel1KeyPressed(evt);
            }
        });
        jPanel1.setLayout(new java.awt.GridLayout(4, 4));

        jToggleButton1.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton1.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton1.setSize(new java.awt.Dimension(25, 25));
        jToggleButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton1MousePressed(evt);
            }
        });
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton1);

        jToggleButton2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jToggleButton2MouseDragged(evt);
            }
        });
        jToggleButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jToggleButton2MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jToggleButton2MouseEntered(evt);
            }
        });
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton2);

        jToggleButton3.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton3.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton3.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton3.setSize(new java.awt.Dimension(25, 25));
        jToggleButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton3MousePressed(evt);
            }
        });
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton3);

        jToggleButton4.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton4.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton4.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton4.setSize(new java.awt.Dimension(25, 25));
        jToggleButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton4MousePressed(evt);
            }
        });
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton4);

        jToggleButton5.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton5.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton5.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton5.setSize(new java.awt.Dimension(25, 25));
        jToggleButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton5MousePressed(evt);
            }
        });
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton5);

        jToggleButton6.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton6.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton6.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton6.setSize(new java.awt.Dimension(25, 25));
        jToggleButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton6MousePressed(evt);
            }
        });
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton6);

        jToggleButton7.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton7.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton7.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton7.setSize(new java.awt.Dimension(25, 25));
        jToggleButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton7MousePressed(evt);
            }
        });
        jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton7ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton7);

        jToggleButton8.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton8.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton8.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton8.setSize(new java.awt.Dimension(25, 25));
        jToggleButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton8MousePressed(evt);
            }
        });
        jToggleButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton8ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton8);

        jToggleButton9.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton9.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton9.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton9.setSize(new java.awt.Dimension(25, 25));
        jToggleButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton9MousePressed(evt);
            }
        });
        jToggleButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton9ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton9);

        jToggleButton10.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton10.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton10.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton10.setSize(new java.awt.Dimension(25, 25));
        jToggleButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton10MousePressed(evt);
            }
        });
        jToggleButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton10ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton10);

        jToggleButton11.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton11.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton11.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton11.setSize(new java.awt.Dimension(25, 25));
        jToggleButton11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton11MousePressed(evt);
            }
        });
        jToggleButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton11ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton11);

        jToggleButton12.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton12.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton12.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton12.setSize(new java.awt.Dimension(25, 25));
        jToggleButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton12MousePressed(evt);
            }
        });
        jToggleButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton12ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton12);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton2MouseDragged
        //System.out.println("DRAG");
    }//GEN-LAST:event_jToggleButton2MouseDragged

    private void jToggleButton2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton2MouseEntered
        //System.out.println("Mouse enter");
    }//GEN-LAST:event_jToggleButton2MouseEntered

    private void jToggleButton2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton2MouseExited
        //System.out.println("Mouse exit");
    }//GEN-LAST:event_jToggleButton2MouseExited

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
       // System.out.println("AC");
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton1MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton1MousePressed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
       // System.out.println("KEY PRESSED");
    }//GEN-LAST:event_formKeyPressed

    private void jPanel1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel1KeyPressed
       // System.out.println("KEY PRESSED");
    }//GEN-LAST:event_jPanel1KeyPressed

    private void jToggleButton3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton3MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton3MousePressed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton4MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton4MousePressed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jToggleButton5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton5MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton5MousePressed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton5ActionPerformed

    private void jToggleButton6MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton6MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton6MousePressed

    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton7MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton7MousePressed

    private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton7ActionPerformed

    private void jToggleButton8MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton8MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton8MousePressed

    private void jToggleButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton8ActionPerformed

    private void jToggleButton9MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton9MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton9MousePressed

    private void jToggleButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton9ActionPerformed

    private void jToggleButton10MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton10MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton10MousePressed

    private void jToggleButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton10ActionPerformed

    private void jToggleButton11MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton11MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton11MousePressed

    private void jToggleButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton11ActionPerformed

    private void jToggleButton12MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton12MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton12MousePressed

    private void jToggleButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton12ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /* try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
         javax.swing.UIManager.setLookAndFeel(info.getClassName());
         break;
         }
         }
         } catch (ClassNotFoundException ex) {
         java.util.logging.Logger.getLogger(MockupInputSelection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
         java.util.logging.Logger.getLogger(MockupInputSelection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
         java.util.logging.Logger.getLogger(MockupInputSelection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(MockupInputSelection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LabeledSelectionGrid_1(5, 4).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    // End of variables declaration//GEN-END:variables

    public class MyToggle extends JToggleButton {

        int x, y;

        public MyToggle(ButtonLocation bl) {
            super();
            this.x = bl.x;
            this.y = bl.y;
           // System.out.println("h");
            setMinimumSize(new Dimension(30, 30));
            this.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                   // System.out.println("BUTTON CHANGE");
                }
            });

            this.getModel().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                   // System.out.println("MODEL CHANGE");
                    if (isSelected()) {

                        setText("X");
                    } else {
                        setText("");
                    }

                }

            });
        }
           // addActionListener(this::actionPerformed);

        public void actionPerformed(java.awt.event.ActionEvent evt) {
           // System.out.println("H");
           /* if (isSelected()) {
                setText("I");
            } else {
                setText("J");
            }
            repaint(); */
        }

        @Override
        public void setSelected(boolean s) {
            //System.out.println("SETTING SELECTED");
            this.getModel().setSelected(s);
            if (s) {
                setText("X");
            } else {
                setText("");
            }
        }

    }

    public class ButtonLocation {

        public int x;
        public int y;

        public ButtonLocation(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}
