/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import javax.swing.JPanel;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public interface ModelBuilder {
    
   /* public void SimpleModelBuilder() {
        
    }
    
    //Will want to override this: to build from training data, equation, etc.
    public Model build() {
        return new SimpleModel("name1");
    }  */
    
    public Model build(String name) throws Exception;
    
    public boolean isCompatible(OSCOutput o);
    
    public ModelBuilder fromTemplate(ModelBuilder template);
    
    public String getPrettyName();

    public LearningModelBuilderEditorPanel getEditorPanel();
    
    public String toLogString();
    
}
