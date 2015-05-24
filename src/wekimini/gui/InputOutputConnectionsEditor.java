/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import wekimini.Wekinator;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class InputOutputConnectionsEditor extends javax.swing.JFrame {

    private boolean isMouseDragging = false;
    private boolean isDraggingTurningOn = false;
    private boolean isClickTurningOn = false;
    private final ButtonLocation lastClickLocation = new ButtonLocation(-1, -1);
    private final ButtonLocation lastPressStart = new ButtonLocation(-1, -1);
    private  MyToggle[][] toggles;
    private  JLabel[] rowNames;
    private  JLabel[] colNames;
    private  JCheckBox[] rowChecks;
    private  JCheckBox[] colChecks;
    private  boolean[][] originallyEnabled;
    //private  ConnectionsListener listener;
    private  Wekinator w;

    public InputOutputConnectionsEditor(Wekinator w) {
        initComponents();
        this.w = w;
        setup(w.getInputManager().getNumInputs(),
                w.getOutputManager().getOutputGroup().getNumOutputs(),
                w.getInputManager().getInputNames(),
                w.getOutputManager().getOutputGroup().getOutputNames(),
                w.getLearningManager().getConnectionMatrix());
        
    }
    
    /**
     * Creates new form MockupInputSelection
     */
    private void setup(int numRows, int numCols, String[] rowNames, String[] colNames, boolean[][] enabled) {

      //  this.listener = listener;

        if (rowNames == null || colNames == null || rowNames.length != numRows || colNames.length != numCols) {
            throw new IllegalArgumentException("Number of rows/columns must match number of row/column names");
        }

        if (enabled == null || rowNames.length == 0 || enabled.length != rowNames.length || enabled[0] == null || enabled[0].length != colNames.length) {
            throw new IllegalArgumentException("Dimensions of enabled must match dimensions of row/column names");
        }

        this.rowNames = new JLabel[rowNames.length];
        rowChecks = new JCheckBox[rowNames.length];
        this.colNames = new JLabel[colNames.length];
        colChecks = new JCheckBox[colNames.length];

        originallyEnabled = new boolean[rowNames.length][colNames.length];

        panelRowNames.removeAll();
        panelRowNames.setLayout(new java.awt.GridLayout(numRows, 1));
        for (int i = 0; i < rowNames.length; i++) {
            JPanel p = makeRowPanel(rowNames[i], i);
            panelRowNames.add(p);
        }

        panelColumnNames.removeAll();
        panelColumnNames.setLayout(new java.awt.GridLayout(1, numCols));

        for (int i = 0; i < colNames.length; i++) {
            JPanel p = makeColPanel(colNames[i], i);
            panelColumnNames.add(p);
        }

        panelButtons.removeAll();
        panelButtons.setLayout(new java.awt.GridLayout(numRows, numCols));
        toggles = new MyToggle[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                int x = i;
                int y = j;
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
                originallyEnabled[i][j] = enabled[i][j];
                mt.setSelected(enabled[i][j]);
                toggles[i][j] = mt;
                panelButtons.add(mt);
            }
        }
        
        buttonsMainPanel.revalidate(); //needed to update scrollpane slider
        scrollPanel.validate();
        repaint();
    }

    private void revert() {
        for (int i = 0; i < toggles.length; i++) {
            for (int j = 0; j < toggles[0].length; j++) {
                toggles[i][j].setSelected(originallyEnabled[i][j]);
            }
        }
    }

    private void rowCheckChanged(ActionEvent e) {
        //Hacky but OK for now
        //TODO: replace with map
        for (int i = 0; i < rowChecks.length; i++) {
            if (e.getSource() == rowChecks[i]) {
                for (MyToggle toggle : toggles[i]) {
                    toggle.setSelected(rowChecks[i].isSelected());
                }
                return;
            }
        }
    }

    private void colCheckChanged(ActionEvent e) {
        for (int i = 0; i < colChecks.length; i++) {
            if (e.getSource() == colChecks[i]) {
                for (int j = 0; j < toggles.length; j++) {
                    toggles[j][i].setSelected(colChecks[i].isSelected());
                }

                return;
            }
        }
    }

    private void mouseExit(int x, int y) {
        //System.out.println("exit " + x + y);
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

       // System.out.println("Click " + x + y);

        if (evt.isShiftDown() && lastClickLocation.x != -1) {
            //System.out.println("A");
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

            for (int i = startx; i <= endx; i++) {
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

    private JPanel makeColPanel(String name, int i) {
        JPanel p = new JPanel();
        JLabel l = new JLabel();
        l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        l.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        l.setText(name);
        JCheckBox jc = new JCheckBox();
        jc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colCheckChanged(e);
            }
        });
        colChecks[i] = jc;

        javax.swing.GroupLayout pLayout = new javax.swing.GroupLayout(p);
        /* p.setLayout(pLayout);
         pLayout.setHorizontalGroup(
         pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(l, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         pLayout.setVerticalGroup(
         pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pLayout.createSequentialGroup()
         .addComponent(l, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(jc))
         ); */

        p.setLayout(pLayout);
        pLayout.setHorizontalGroup(
                pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(l, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                .addComponent(jc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pLayout.setVerticalGroup(
                pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(l)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jc))
        );
        return p;
    }

    private JPanel makeRowPanel(String name, int i) {
        JPanel p = new JPanel();
        JLabel l = new JLabel();
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        l.setText(name);
        JCheckBox jc = new JCheckBox();
        jc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rowCheckChanged(e);
            }
        });
        rowChecks[i] = jc;

        javax.swing.GroupLayout pLayout = new javax.swing.GroupLayout(p);
        p.setLayout(pLayout);
        pLayout.setHorizontalGroup(
                pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pLayout.createSequentialGroup()
                        .addComponent(l, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jc))
        );
        pLayout.setVerticalGroup(
                pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(l, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        return p;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenuAction = new javax.swing.JPopupMenu();
        menuItemRevert = new javax.swing.JMenuItem();
        menuItemEnableAll = new javax.swing.JMenuItem();
        menuItemDisableAll = new javax.swing.JMenuItem();
        jPanel2 = new javax.swing.JPanel();
        scrollPanel = new javax.swing.JScrollPane();
        buttonsMainPanel = new javax.swing.JPanel();
        panelRowNames = new javax.swing.JPanel();
        testPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
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
        jToggleButton13 = new javax.swing.JToggleButton();
        jToggleButton14 = new javax.swing.JToggleButton();
        jToggleButton15 = new javax.swing.JToggleButton();
        jToggleButton16 = new javax.swing.JToggleButton();
        panelColumnNames = new javax.swing.JPanel();
        testColPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        testColPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        testColPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jCheckBox4 = new javax.swing.JCheckBox();
        testColPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jCheckBox5 = new javax.swing.JCheckBox();
        testColPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jCheckBox6 = new javax.swing.JCheckBox();
        testColPanel5 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jCheckBox7 = new javax.swing.JCheckBox();
        testColPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jCheckBox8 = new javax.swing.JCheckBox();
        testColPanel7 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jCheckBox9 = new javax.swing.JCheckBox();
        testColPanel8 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jCheckBox10 = new javax.swing.JCheckBox();
        testColPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jCheckBox11 = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        menuItemRevert.setText("Revert all changes");
        menuItemRevert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRevertActionPerformed(evt);
            }
        });
        popupMenuAction.add(menuItemRevert);

        menuItemEnableAll.setText("Connect all");
        menuItemEnableAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemEnableAllActionPerformed(evt);
            }
        });
        popupMenuAction.add(menuItemEnableAll);

        menuItemDisableAll.setText("Disconnect all");
        menuItemDisableAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemDisableAllActionPerformed(evt);
            }
        });
        popupMenuAction.add(menuItemDisableAll);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        scrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panelRowNames.setLayout(new java.awt.GridLayout(4, 0));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("label 1asdfasdf");

        javax.swing.GroupLayout testPanel1Layout = new javax.swing.GroupLayout(testPanel1);
        testPanel1.setLayout(testPanel1Layout);
        testPanel1Layout.setHorizontalGroup(
            testPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(testPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1))
        );
        testPanel1Layout.setVerticalGroup(
            testPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        panelRowNames.add(testPanel1);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("label 1");
        panelRowNames.add(jLabel2);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("label 1");
        panelRowNames.add(jLabel3);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("label 1");
        panelRowNames.add(jLabel6);

        panelButtons.setMaximumSize(new java.awt.Dimension(100, 100));
        panelButtons.setMinimumSize(new java.awt.Dimension(100, 100));
        panelButtons.setPreferredSize(new java.awt.Dimension(100, 100));
        panelButtons.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                panelButtonsKeyPressed(evt);
            }
        });
        panelButtons.setLayout(new java.awt.GridLayout(4, 4));

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
        panelButtons.add(jToggleButton1);

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
        panelButtons.add(jToggleButton2);

        jToggleButton3.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton3.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton3.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton3);

        jToggleButton4.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton4.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton4.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton4);

        jToggleButton5.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton5.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton5.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton5);

        jToggleButton6.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton6.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton6.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton6);

        jToggleButton7.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton7.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton7.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton7);

        jToggleButton8.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton8.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton8.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton8);

        jToggleButton9.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton9.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton9.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton9);

        jToggleButton10.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton10.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton10.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton10);

        jToggleButton11.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton11.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton11.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton11);

        jToggleButton12.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton12.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton12.setPreferredSize(new java.awt.Dimension(25, 25));
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
        panelButtons.add(jToggleButton12);

        jToggleButton13.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton13.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton13.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton13MousePressed(evt);
            }
        });
        jToggleButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton13ActionPerformed(evt);
            }
        });
        panelButtons.add(jToggleButton13);

        jToggleButton14.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton14.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton14.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton14MousePressed(evt);
            }
        });
        jToggleButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton14ActionPerformed(evt);
            }
        });
        panelButtons.add(jToggleButton14);

        jToggleButton15.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton15.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton15.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton15MousePressed(evt);
            }
        });
        jToggleButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton15ActionPerformed(evt);
            }
        });
        panelButtons.add(jToggleButton15);

        jToggleButton16.setMaximumSize(new java.awt.Dimension(25, 25));
        jToggleButton16.setMinimumSize(new java.awt.Dimension(25, 25));
        jToggleButton16.setPreferredSize(new java.awt.Dimension(25, 25));
        jToggleButton16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToggleButton16MousePressed(evt);
            }
        });
        jToggleButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton16ActionPerformed(evt);
            }
        });
        panelButtons.add(jToggleButton16);

        panelColumnNames.setLayout(new java.awt.GridLayout(1, 0));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("label 1");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanelLayout = new javax.swing.GroupLayout(testColPanel);
        testColPanel.setLayout(testColPanelLayout);
        testColPanelLayout.setHorizontalGroup(
            testColPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanelLayout.setVerticalGroup(
            testColPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2))
        );

        panelColumnNames.add(testColPanel);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("label 1");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel1Layout = new javax.swing.GroupLayout(testColPanel1);
        testColPanel1.setLayout(testColPanel1Layout);
        testColPanel1Layout.setHorizontalGroup(
            testColPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel1Layout.setVerticalGroup(
            testColPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3))
        );

        panelColumnNames.add(testColPanel1);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("label 1");
        jLabel8.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel2Layout = new javax.swing.GroupLayout(testColPanel2);
        testColPanel2.setLayout(testColPanel2Layout);
        testColPanel2Layout.setHorizontalGroup(
            testColPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel2Layout.setVerticalGroup(
            testColPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4))
        );

        panelColumnNames.add(testColPanel2);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("label 1");
        jLabel9.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel3Layout = new javax.swing.GroupLayout(testColPanel3);
        testColPanel3.setLayout(testColPanel3Layout);
        testColPanel3Layout.setHorizontalGroup(
            testColPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel3Layout.setVerticalGroup(
            testColPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox5))
        );

        panelColumnNames.add(testColPanel3);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("label 1");
        jLabel10.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel4Layout = new javax.swing.GroupLayout(testColPanel4);
        testColPanel4.setLayout(testColPanel4Layout);
        testColPanel4Layout.setHorizontalGroup(
            testColPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel4Layout.setVerticalGroup(
            testColPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox6))
        );

        panelColumnNames.add(testColPanel4);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("label 1");
        jLabel11.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel5Layout = new javax.swing.GroupLayout(testColPanel5);
        testColPanel5.setLayout(testColPanel5Layout);
        testColPanel5Layout.setHorizontalGroup(
            testColPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel5Layout.setVerticalGroup(
            testColPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox7))
        );

        panelColumnNames.add(testColPanel5);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("label 1");
        jLabel12.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel6Layout = new javax.swing.GroupLayout(testColPanel6);
        testColPanel6.setLayout(testColPanel6Layout);
        testColPanel6Layout.setHorizontalGroup(
            testColPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel6Layout.setVerticalGroup(
            testColPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox8))
        );

        panelColumnNames.add(testColPanel6);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("label 1");
        jLabel13.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel7Layout = new javax.swing.GroupLayout(testColPanel7);
        testColPanel7.setLayout(testColPanel7Layout);
        testColPanel7Layout.setHorizontalGroup(
            testColPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel7Layout.setVerticalGroup(
            testColPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox9))
        );

        panelColumnNames.add(testColPanel7);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("label 1");
        jLabel14.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel8Layout = new javax.swing.GroupLayout(testColPanel8);
        testColPanel8.setLayout(testColPanel8Layout);
        testColPanel8Layout.setHorizontalGroup(
            testColPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel8Layout.setVerticalGroup(
            testColPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox10))
        );

        panelColumnNames.add(testColPanel8);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("label 1");
        jLabel15.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jCheckBox11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout testColPanel9Layout = new javax.swing.GroupLayout(testColPanel9);
        testColPanel9.setLayout(testColPanel9Layout);
        testColPanel9Layout.setHorizontalGroup(
            testColPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        testColPanel9Layout.setVerticalGroup(
            testColPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testColPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox11))
        );

        panelColumnNames.add(testColPanel9);

        javax.swing.GroupLayout buttonsMainPanelLayout = new javax.swing.GroupLayout(buttonsMainPanel);
        buttonsMainPanel.setLayout(buttonsMainPanelLayout);
        buttonsMainPanelLayout.setHorizontalGroup(
            buttonsMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelRowNames, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(buttonsMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelColumnNames, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        buttonsMainPanelLayout.setVerticalGroup(
            buttonsMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelColumnNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(buttonsMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(panelRowNames, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        scrollPanel.setViewportView(buttonsMainPanel);

        jLabel5.setText("<html>An \"X\" means that the value of the output corresponding to the column will be influenced by the value of the input corresponding to the row.</html>");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton1.setText("Actions...");
        jButton1.setComponentPopupMenu(popupMenuAction);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("OK");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Cancel");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton2MouseDragged
        //System.out.println("DRAG");
    }//GEN-LAST:event_jToggleButton2MouseDragged

    private void jToggleButton2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton2MouseEntered
       // System.out.println("Mouse enter");
    }//GEN-LAST:event_jToggleButton2MouseEntered

    private void jToggleButton2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton2MouseExited
       //// System.out.println("Mouse exit");
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
        //System.out.println("KEY PRESSED");
    }//GEN-LAST:event_formKeyPressed

    private void panelButtonsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_panelButtonsKeyPressed
        //System.out.println("KEY PRESSED");
    }//GEN-LAST:event_panelButtonsKeyPressed

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

    private void jToggleButton13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton13MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton13MousePressed

    private void jToggleButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton13ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton13ActionPerformed

    private void jToggleButton14MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton14MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton14MousePressed

    private void jToggleButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton14ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton14ActionPerformed

    private void jToggleButton15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton15MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton15MousePressed

    private void jToggleButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton15ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton15ActionPerformed

    private void jToggleButton16MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton16MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton16MousePressed

    private void jToggleButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton16ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton16ActionPerformed

    private void menuItemRevertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRevertActionPerformed
        revert();
    }//GEN-LAST:event_menuItemRevertActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jButton1.getComponentPopupMenu().show(jButton1, 0, 0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void menuItemEnableAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemEnableAllActionPerformed
        setAll(true);
    }//GEN-LAST:event_menuItemEnableAllActionPerformed

    private void menuItemDisableAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemDisableAllActionPerformed
        setAll(false);
    }//GEN-LAST:event_menuItemDisableAllActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         boolean[][] c = getConnectionsFromForm();
        if (checkValid(c)) {
            w.getLearningManager().updateInputOutputConnections(c);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            this.dispose();
            
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private boolean checkValid(boolean[][] c) {
        for (int output = 0; output < c[0].length; output++) {
            int sum = 0;
            for (int input = 0; input < c.length; input++) {
                if (c[input][output]) {
                    sum++;
                }
            }
            if (sum ==0) {
                Util.showPrettyErrorPane(this, "At least one input must be selected for each output");
                return false;
            }
        }
        return true;
    }
    
    private boolean[][] getConnectionsFromForm() {
        boolean[][] c = new boolean[toggles.length][toggles[0].length];
        for (int i = 0; i < toggles.length; i++) {
            for (int j = 0; j < toggles[0].length; j++) {
                c[i][j] = toggles[i][j].isSelected();
            }
        }
        return c;
    }
    
    private void setAll(boolean b) {
        for (int i = 0; i < toggles.length; i++) {
            for (int j = 0; j < toggles[0].length; j++) {
                toggles[i][j].setSelected(b);
            }
        }
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
                String[] rowNames = {"a", "b"};
                String[] colNames = {"1", "2"};
                boolean[][] enabled = {{true, true}, {false, true}};
/*
                new InputOutputConnectionsEditor(rowNames.length, colNames.length, rowNames, colNames, enabled, new ConnectionsListener() {

                    @Override
                    public void notify(boolean[][] connections) {
                        System.out.println("Got them");
                        for (int i = 0; i < connections.length; i++) {
                            for (int j = 0; j < connections[0].length; j++) {
                                System.out.print(connections[i][j] + "/");
                            }
                            System.out.println("");
                        }
                    }
                }).setVisible(true); */
            } 
        });
                
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsMainPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton13;
    private javax.swing.JToggleButton jToggleButton14;
    private javax.swing.JToggleButton jToggleButton15;
    private javax.swing.JToggleButton jToggleButton16;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.JMenuItem menuItemDisableAll;
    private javax.swing.JMenuItem menuItemEnableAll;
    private javax.swing.JMenuItem menuItemRevert;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelColumnNames;
    private javax.swing.JPanel panelRowNames;
    private javax.swing.JPopupMenu popupMenuAction;
    private javax.swing.JScrollPane scrollPanel;
    private javax.swing.JPanel testColPanel;
    private javax.swing.JPanel testColPanel1;
    private javax.swing.JPanel testColPanel2;
    private javax.swing.JPanel testColPanel3;
    private javax.swing.JPanel testColPanel4;
    private javax.swing.JPanel testColPanel5;
    private javax.swing.JPanel testColPanel6;
    private javax.swing.JPanel testColPanel7;
    private javax.swing.JPanel testColPanel8;
    private javax.swing.JPanel testColPanel9;
    private javax.swing.JPanel testPanel1;
    // End of variables declaration//GEN-END:variables

    public class MyToggle extends JToggleButton {

        int x, y;

        public MyToggle(ButtonLocation bl) {
            super();
            this.x = bl.x;
            this.y = bl.y;
            // System.out.println("h");
            setMinimumSize(new Dimension(30, 30));
            //setMaximumSize(new Dimension(80, 80)); //Doesn't seem to work
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

    //doesn't really work
//See http://stackoverflow.com/questions/14777926/java-how-to-make-a-jlabel-with-vertical-text
    public class JVertLabel extends JComponent {

        private String text;

        public JVertLabel(String s) {
            text = s;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.rotate(Math.toRadians(270.0));
            g2d.drawString(text, 0, 0);
        }
    }
    
    public interface ConnectionsListener {
        public void notify(boolean[][] connections);
    }

}
