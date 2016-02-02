/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class LabeledButtonGridPanel extends JPanel {

    private boolean isMouseDragging = false;
    private boolean isDraggingTurningOn = false;
    private boolean isClickTurningOn = false;
    private int lastClickLocationX = -1;
    private int lastClickLocationY = -1;
    private int lastPressStartX = -1;
    private int lastPressStartY = -1;
    private static final int buttonWidth = 30;
    private static final int buttonHeight = 30;

    private final MousableToggleButton[][] toggles;
    private final JLabel[] rowNames;
    private final JLabel[] colNames;
    private boolean[][] originallyEnabled;
    private final JPanel pAllButtons;

    private final boolean[] columnToggles;
    private final boolean[] rowToggles;

    public LabeledButtonGridPanel(String[] rowLabels, String[] colLabels, boolean[][] enabled) {
        super();
        this.setBackground(new java.awt.Color(255, 255, 255));
        //Instantiate objects
        toggles = new MousableToggleButton[rowLabels.length][colLabels.length];
        columnToggles = new boolean[colLabels.length];
        rowToggles = new boolean[rowLabels.length];
        originallyEnabled = new boolean[rowLabels.length][colLabels.length];

        //Rows setup
        rowNames = new JLabel[rowLabels.length];
        int maxRowWidth = 0;
        for (int i = 0; i < rowLabels.length; i++) {
            final int t = i;
            JLabel l = new JLabel(rowLabels[i]);
            l.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    toggleRow(t);
                }
            });

            l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            l.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
            if (maxRowWidth < l.getPreferredSize().width) {
                maxRowWidth = l.getPreferredSize().width;
            }
            rowNames[i] = l;
        }
        int leftColumnWidth = maxRowWidth + 10;

        //Columns setup
        colNames = new VerticalLabel[colLabels.length];
        int maxColHeight = 0;
        for (int i = 0; i < colLabels.length; i++) {
            final int t = i;
            JLabel l = new VerticalLabel();
            l.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    toggleColumn(t);
                }
            });
            l.setText(colLabels[i]);
            if (maxColHeight < l.getPreferredSize().width) {
                maxColHeight = l.getPreferredSize().width;
            }
            colNames[i] = l;
        }
        int topRowHeight = maxColHeight + 10;

        //Other sizing
        int buttonPanelWidth = colLabels.length * buttonWidth;
        int buttonPanelHeight = rowLabels.length * buttonHeight + 5;

        JPanel pTop = new JPanel();
        JPanel pBottom = new JPanel();
        JPanel pTopLeft = new JPanel();
        JPanel pTopRight = new JPanel();
        JPanel pBottomLeft = new JPanel();
        JPanel pBottomRight = new JPanel();

        pTop.setBackground(new java.awt.Color(255, 255, 255));
        pBottom.setBackground(new java.awt.Color(255, 255, 255));
        pTopLeft.setBackground(new java.awt.Color(255, 255, 255));
        pTopRight.setBackground(new java.awt.Color(255, 255, 255));
        pBottomLeft.setBackground(new java.awt.Color(255, 255, 255));
        pBottomRight.setBackground(new java.awt.Color(255, 255, 255));
        
        setPreferredSize(new java.awt.Dimension(leftColumnWidth + buttonPanelWidth + 10, topRowHeight + buttonPanelHeight + 20));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        pTop.setPreferredSize(new java.awt.Dimension(leftColumnWidth + buttonPanelWidth, topRowHeight));
        pTop.setLayout(new javax.swing.BoxLayout(pTop, javax.swing.BoxLayout.X_AXIS));

        setAllSizes(leftColumnWidth, topRowHeight, pTopLeft);
        //pTopLeft.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pTop.add(pTopLeft);

        setAllSizes(buttonPanelWidth, topRowHeight, pTopRight);
       // pTopRight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pTopRight.setLayout(new javax.swing.BoxLayout(pTopRight, javax.swing.BoxLayout.X_AXIS));

        for (int i = 0; i < colNames.length; i++) {
            setAllSizes(buttonWidth, topRowHeight, colNames[i]);
            colNames[i].setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            colNames[i].setVerticalAlignment(javax.swing.SwingConstants.CENTER);
            pTopRight.add(colNames[i]);
        }

        pTop.add(pTopRight);
        add(pTop);

        pBottom.setPreferredSize(new java.awt.Dimension(leftColumnWidth + buttonPanelWidth, buttonPanelHeight + 10));
        pBottom.setLayout(new javax.swing.BoxLayout(pBottom, javax.swing.BoxLayout.X_AXIS));

        setAllSizes(leftColumnWidth, buttonPanelHeight + 10, pBottomLeft);
        //pBottomLeft.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pBottomLeft.setLayout(new javax.swing.BoxLayout(pBottomLeft, javax.swing.BoxLayout.Y_AXIS));

        for (int i = 0; i < rowNames.length; i++) {
            setAllSizes(leftColumnWidth, buttonHeight, rowNames[i]);
            pBottomLeft.add(rowNames[i]);
        }
        pBottom.add(pBottomLeft);

        setAllSizes(buttonPanelWidth, buttonPanelHeight + 10, pBottomRight);
       // pBottomRight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pBottomRight.setLayout(new javax.swing.BoxLayout(pBottomRight, javax.swing.BoxLayout.Y_AXIS));

        pAllButtons = new JPanel();
        setAllSizes(buttonPanelWidth, buttonPanelHeight, pAllButtons);
        pAllButtons.setLayout(new GridLayout(rowLabels.length, colLabels.length));

        
            for (int j = 0; j < rowLabels.length; j++) {
                for (int i = 0; i < colLabels.length; i++) {
                final int x = i;
                final int y = j;
                MousableToggleButton b = new MousableToggleButton(i, j);
                b.setSize(buttonWidth, buttonHeight);
                b.setMargin(new Insets(2,2,2,2));
                b.addMouseListener(new java.awt.event.MouseAdapter() {
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
                originallyEnabled[j][i] = enabled[j][i];
                b.setSelected(enabled[j][i]);
                toggles[j][i] = b; //j = row, i= col
                pAllButtons.add(b);
            }
        }
        pBottomRight.add(pAllButtons);
        pBottom.add(pBottomRight);

        add(pBottom);
    }

    private void mouseExit(int x, int y) {
        if (isMouseDragging) {
            if (lastPressStartX == x && lastPressStartY == y) {
                toggles[y][x].setSelected(isDraggingTurningOn);
            }
        }
    }

    private void mouseEnter(int x, int y) {
        if (isMouseDragging) {
            toggles[y][x].setSelected(isDraggingTurningOn);
        }
    }

    private void mousePress(int x, int y) {
        isMouseDragging = true;
        boolean selected = toggles[y][x].isSelected();
        isDraggingTurningOn = !selected;
        lastPressStartX = x;
        lastPressStartY = y;
    }

    private void mouseRelease(int x, int y) {
        if (isMouseDragging) {
            isMouseDragging = false;
        }
    }

    private void mouseClick(int x, int y, java.awt.event.MouseEvent evt) {
        if (evt.isShiftDown() && lastClickLocationX != -1) {
            int startx, starty, endx, endy;
            if (lastClickLocationX < x) {
                startx = lastClickLocationX;
                endx = x;
            } else {
                startx = x;
                endx = lastClickLocationX;
            }

            if (lastClickLocationY < y) {
                starty = lastClickLocationY;
                endy = y;

            } else {
                starty = y;
                endy = lastClickLocationY;
            }

            for (int i = startx; i <= endx; i++) {
                for (int j = starty; j <= endy; j++) {
                    toggles[j][i].setSelected(isClickTurningOn);
                }
            }
            pAllButtons.repaint(); //needed to keep visual button state in sync for some reason

            lastClickLocationX = -1;
            lastClickLocationY = -1;
        } else {
            isClickTurningOn = toggles[y][x].isSelected();
            lastClickLocationX = x;
            lastClickLocationY = y;
        }
    }
    
          

    private static void setAllSizes(int width, int height, JComponent j) {
        j.setPreferredSize(new Dimension(width, height));
        j.setMaximumSize(new Dimension(width, height));
        j.setMinimumSize(new Dimension(width, height));
    }

    public boolean checkValid(boolean[][] c) {
        for (int output = 0; output < c[0].length; output++) {
            int sum = 0;
            for (int input = 0; input < c.length; input++) {
                if (c[input][output]) {
                    sum++;
                }
            }
            if (sum == 0) {
                Util.showPrettyErrorPane(this, "At least one input must be selected for each output");
                return false;
            }
        }
        return true;
    }

    public void setAll(boolean s) {
       for (int i = 0; i < toggles.length; i++) {
            for (int j = 0; j < toggles[0].length; j++) {
                toggles[i][j].setSelected(s);
            }
        } 
       pAllButtons.repaint();
    }
    
    public boolean[][] getConnectionsFromForm() {
        boolean[][] c = new boolean[toggles.length][toggles[0].length];
        for (int i = 0; i < toggles.length; i++) {
            for (int j = 0; j < toggles[0].length; j++) {
                c[i][j] = toggles[i][j].isSelected();
            }
        }
        return c;
    }

    //When true connection state set asynchronously (i.e. via OSC)
    public void setNewOriginal(boolean[][] connections) {
        for (int i= 0; i < connections.length; i++) {
            for (int j = 0; j < connections[0].length; j++) {
                originallyEnabled[i][j] = connections[i][j];
            }
        }
        revert();
    }
    
    public void revert() {
        for (int i = 0; i < toggles.length; i++) {
            for (int j = 0; j < toggles[0].length; j++) {
                toggles[i][j].setSelected(originallyEnabled[i][j]);
            }
        }
        pAllButtons.repaint();
    }

    private void toggleColumn(int which) {
        for (int j = 0; j < toggles.length; j++) {
            toggles[j][which].setSelected(columnToggles[which]);
        }
        columnToggles[which] = !columnToggles[which];
        pAllButtons.repaint();
    }

    private void toggleRow(int which) {
        for (int i = 0; i < toggles[0].length; i++) {
            toggles[which][i].setSelected(rowToggles[which]);
        }
        rowToggles[which] = !rowToggles[which];
        pAllButtons.repaint();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new Tester2().setVisible(true);
                String[] r = {"abc", "def", "ghi"};
                String[] c = {"1111111111111111111", "2", "3", "1", "2", "3"};
                JFrame j = new JFrame();

                boolean[][] enabled = new boolean[r.length][c.length];

                LabeledButtonGridPanel p = new LabeledButtonGridPanel(r, c, enabled);
                JScrollPane sp = new JScrollPane();
                sp.setViewportView(p);
                j.add(sp);
                j.setVisible(true);
            }
        });
    }

}
