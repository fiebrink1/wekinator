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
public class MousableToggleButton extends JToggleButton {

    int x, y;

    public MousableToggleButton(int x, int y) {
        super();
        this.x = x;
        this.y = y;
        // System.out.println("h");
        setMinimumSize(new Dimension(50,50));
        setMaximumSize(new Dimension(50, 50));
        setPreferredSize(new Dimension(50, 50));
        setSize(new Dimension(50,50));
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
        //System.out.println("SETTING SELECTED: " + s);
        this.getModel().setSelected(s);
        if (s) {
            setText("X");
        } else {
            setText("");
        }
    }

}
