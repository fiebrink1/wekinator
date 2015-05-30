/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author rebecca
 */
public class LabeledButtonGrid extends javax.swing.JFrame {

    /**
     * Creates new form Tester2
     */
    public LabeledButtonGrid() {
        initComponents();
    }

    public static void Testing(String[] rowLabels, String[] colLabels) {
        JLabel[] rowLs = new JLabel[rowLabels.length];
        int maxRowWidth = 0;
        for (int i = 0; i < rowLabels.length; i++) { 
            JLabel l = new JLabel(rowLabels[i]);
            l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            l.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
           // setAllSizes(leftColumnWidth, buttonHeight, l);
            if (maxRowWidth < l.getPreferredSize().width) {
                maxRowWidth = l.getPreferredSize().width;
            }
            rowLs[i] = l;
        }
        int leftColumnWidth = maxRowWidth + 10;
        
        JLabel[] colLs = new VerticalLabel[colLabels.length];
        int maxColHeight = 0;
        for (int i = 0; i < colLabels.length; i++) { 
            JLabel l = new VerticalLabel();
            l.setText(colLabels[i]);
            System.out.println("p is " + l.getPreferredSize());
            if (maxColHeight < l.getPreferredSize().width) {
                maxColHeight = l.getPreferredSize().width;
            }
            colLs[i] = l;
        }
        int topRowHeight = maxColHeight + 10;
        System.out.println("Row height is " + topRowHeight);
        
        int buttonWidth = 30;
        int buttonHeight = 30;
        int buttonPanelWidth = colLabels.length * buttonWidth;
        int buttonPanelHeight = rowLabels.length * buttonHeight;
        
        
        
        
        JFrame f = new JFrame();
        JPanel pMain = new JPanel();
        JPanel pTop = new JPanel();
        JPanel pBottom = new JPanel();
        JPanel pTopLeft = new JPanel();
        JPanel pTopRight = new JPanel();
        JPanel pBottomLeft = new JPanel();
        JPanel pBottomRight = new JPanel();

        f.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        f.setPreferredSize(new java.awt.Dimension(leftColumnWidth + buttonPanelWidth + 10, topRowHeight + buttonPanelHeight + 10));

        pMain.setPreferredSize(new java.awt.Dimension(leftColumnWidth + buttonPanelWidth + 10, topRowHeight + buttonPanelHeight + 10));
        pMain.setLayout(new javax.swing.BoxLayout(pMain, javax.swing.BoxLayout.Y_AXIS));

        pTop.setPreferredSize(new java.awt.Dimension(leftColumnWidth + buttonPanelWidth, topRowHeight));
        pTop.setLayout(new javax.swing.BoxLayout(pTop, javax.swing.BoxLayout.X_AXIS));

        setAllSizes(leftColumnWidth, topRowHeight, pTopLeft);
        /*pTopLeft.setPreferredSize(new java.awt.Dimension(100, 150));
        pTopLeft.setMaximumSize(new java.awt.Dimension(100, 150));
        pTopLeft.setMinimumSize(new java.awt.Dimension(100, 150)); */
        pTopLeft.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        //TopLeft.setLayout(new javax.swing.BoxLayout(pTopLeft, javax.swing.BoxLayout.X_AXIS));

        pTop.add(pTopLeft);

        setAllSizes(buttonPanelWidth, topRowHeight, pTopRight); 
        /*pTopRight.setPreferredSize(new java.awt.Dimension(200, 150));
        pTopRight.setMaximumSize(new java.awt.Dimension(200, 150));
        pTopRight.setMinimumSize(new java.awt.Dimension(200, 150)); */
        pTopRight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pTopRight.setLayout(new javax.swing.BoxLayout(pTopRight, javax.swing.BoxLayout.X_AXIS));

        
       for (int i = 0; i < colLs.length; i++) {
            setAllSizes(buttonWidth, topRowHeight, colLs[i]);
            colLs[i].setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            colLs[i].setVerticalAlignment(javax.swing.SwingConstants.CENTER);
             pTopRight.add(colLs[i]);     
        } 

        
        pTop.add(pTopRight);

        pMain.add(pTop);

        pBottom.setPreferredSize(new java.awt.Dimension(leftColumnWidth + buttonPanelWidth, buttonPanelHeight));
        pBottom.setLayout(new javax.swing.BoxLayout(pBottom, javax.swing.BoxLayout.X_AXIS));

        setAllSizes(leftColumnWidth, buttonPanelHeight, pBottomLeft);
        /*pBottomLeft.setPreferredSize(new java.awt.Dimension(100, 150));
        pBottomLeft.setMaximumSize(new java.awt.Dimension(100, 150));
        pBottomLeft.setMinimumSize(new java.awt.Dimension(100, 150)); */
        pBottomLeft.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        pBottomLeft.setLayout(new javax.swing.BoxLayout(pBottomLeft, javax.swing.BoxLayout.Y_AXIS));

        for (int i = 0; i < rowLs.length; i++) { 
            setAllSizes(leftColumnWidth, buttonHeight, rowLs[i]);
            pBottomLeft.add(rowLs[i]);
        }
        
        
        pBottom.add(pBottomLeft);

        setAllSizes(buttonPanelWidth, buttonPanelHeight, pBottomRight);
       /* pBottomRight.setPreferredSize(new java.awt.Dimension(200, 150));
        pBottomRight.setMaximumSize(new java.awt.Dimension(200, 150));
        pBottomRight.setMinimumSize(new java.awt.Dimension(200, 150)); */

        pBottomRight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        pBottomRight.setLayout(new GridLayout(rowLabels.length, colLabels.length));
        
        for (int i = 0; i < colLabels.length; i++) {
            for (int j = 0; j < rowLabels.length; j++) {
                JButton b = new JButton(".");
                b.setSize(buttonWidth, buttonHeight);
                pBottomRight.add(b);
            }   
        }

        pBottom.add(pBottomRight);

        pMain.add(pBottom);

        /*GroupLayout layout = new javax.swing.GroupLayout(f.getContentPane());
        f.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pMain, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pMain, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
        ); */
        f.add(pMain);

        //pTopLeft.add(new JLabel("Top left"));
        //pTopRight.add(new JLabel("Top right"));
        //pBottomLeft.add(new JLabel("Bottom Lfet"));
        //pBottomRight.add(new JLabel("Bottom Right"));

        f.repaint();
        f.pack();
        f.setVisible(true);
        System.out.println("Main size is " + pMain.getSize());
        System.out.println("f size is " + f.getSize());

    }
    
    private static void setAllSizes(int width, int height, JComponent j) {
        j.setPreferredSize(new Dimension(width, height));
        j.setMaximumSize(new Dimension(width, height));
        j.setMinimumSize(new Dimension(width, height));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelTop = new javax.swing.JPanel();
        panelTopLeft = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        panelTopRight = new javax.swing.JPanel();
        panelBottom = new javax.swing.JPanel();
        panelBottomLeft = new javax.swing.JPanel();
        panelBottomRight = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(300, 300));

        panelMain.setPreferredSize(new java.awt.Dimension(300, 300));
        panelMain.setLayout(new javax.swing.BoxLayout(panelMain, javax.swing.BoxLayout.Y_AXIS));

        panelTop.setPreferredSize(new java.awt.Dimension(300, 150));
        panelTop.setLayout(new javax.swing.BoxLayout(panelTop, javax.swing.BoxLayout.X_AXIS));

        panelTopLeft.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelTopLeft.setPreferredSize(new java.awt.Dimension(100, 150));
        panelTopLeft.setLayout(new javax.swing.BoxLayout(panelTopLeft, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setText("jLabel1");
        panelTopLeft.add(jLabel1);

        panelTop.add(panelTopLeft);

        panelTopRight.setPreferredSize(new java.awt.Dimension(200, 150));

        javax.swing.GroupLayout panelTopRightLayout = new javax.swing.GroupLayout(panelTopRight);
        panelTopRight.setLayout(panelTopRightLayout);
        panelTopRightLayout.setHorizontalGroup(
            panelTopRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 320, Short.MAX_VALUE)
        );
        panelTopRightLayout.setVerticalGroup(
            panelTopRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        panelTop.add(panelTopRight);

        panelMain.add(panelTop);

        panelBottom.setPreferredSize(new java.awt.Dimension(300, 150));
        panelBottom.setLayout(new javax.swing.BoxLayout(panelBottom, javax.swing.BoxLayout.X_AXIS));

        panelBottomLeft.setPreferredSize(new java.awt.Dimension(100, 150));

        javax.swing.GroupLayout panelBottomLeftLayout = new javax.swing.GroupLayout(panelBottomLeft);
        panelBottomLeft.setLayout(panelBottomLeftLayout);
        panelBottomLeftLayout.setHorizontalGroup(
            panelBottomLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );
        panelBottomLeftLayout.setVerticalGroup(
            panelBottomLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        panelBottom.add(panelBottomLeft);

        panelBottomRight.setPreferredSize(new java.awt.Dimension(200, 150));
        panelBottomRight.setLayout(new java.awt.GridLayout());
        panelBottom.add(panelBottomRight);

        panelMain.add(panelBottom);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(68, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(73, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*  try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
         javax.swing.UIManager.setLookAndFeel(info.getClassName());
         break;
         }
         }
         } catch (ClassNotFoundException ex) {
         java.util.logging.Logger.getLogger(Tester2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
         java.util.logging.Logger.getLogger(Tester2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
         java.util.logging.Logger.getLogger(Tester2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(Tester2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new Tester2().setVisible(true);
                String[] r = {"ababcabcabcabcabcc", "def", "ghi", "abc", "def", "ghi"};
                String[] c = {"1111111111111111111", "2", "3", "1", "2", "3"};
                Testing(r, c);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel panelBottomLeft;
    private javax.swing.JPanel panelBottomRight;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelTop;
    private javax.swing.JPanel panelTopLeft;
    private javax.swing.JPanel panelTopRight;
    // End of variables declaration//GEN-END:variables
}
