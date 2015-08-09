/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import javax.swing.JPanel;
import wekimini.LearningModelBuilder;

/**
 *
 * @author rebecca
 */
public abstract class LearningModelBuilderEditorPanel extends JPanel {
    public abstract LearningModelBuilder buildFromPanel();
    
    public abstract boolean validateForm();
}
