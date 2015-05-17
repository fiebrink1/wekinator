/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.gui.InitInputOutputFrame;

/**
 *
 * @author rebecca
 */
public class WekiMiniRunner {
    private static final Logger logger = Logger.getLogger(WekiMiniRunner.class.getName());
    
    public static void main(String[] args) {
        //WelcomeScreen
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
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WelcomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                runNewProject();
            }
        });  
        
        
    }
    
    /*public static void runNewProject() {
        try {
            
            
            
            Wekinator w = new Wekinator();
            InitInputOutputFrame f = new InitInputOutputFrame(w);
            f.setVisible(true);
        } catch (IOException ex) {
            Util.showPrettyErrorPane(null, "Error encountered in starting Wekinator: " + ex.getMessage());
            Logger.getLogger(WekiMiniRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    } */
    
    public static void runNewProject() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                try {
                    Wekinator w = new Wekinator();
                    InitInputOutputFrame f = new InitInputOutputFrame(w);
                    f.setVisible(true);
                    
                } catch (IOException | SecurityException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
