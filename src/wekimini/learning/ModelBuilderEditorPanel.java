/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import javax.swing.JPanel;

/**
 *
 * @author rebecca
 */
public abstract class ModelBuilderEditorPanel extends JPanel {
    public abstract ModelBuilder buildFromPanel();
    
    public abstract boolean validateForm();
}
