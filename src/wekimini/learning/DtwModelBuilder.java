/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import wekimini.osc.OSCDtwOutput;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public class DtwModelBuilder {
    DtwSettings settings;
    
    public DtwModelBuilder() {
        settings = new DtwSettings();
    }
    
    public DtwModelBuilder(DtwSettings s) {
        settings = new DtwSettings();
    }
    
    public boolean isCompatible(OSCOutput o) {
        return (o instanceof OSCDtwOutput);
    }
    
    public DtwModelBuilder fromTemplate(ModelBuilder b) {
        if (b instanceof DtwModelBuilder) {
            return new DtwModelBuilder(((DtwModelBuilder)b).settings);
        }
        return null;
    }

    public String getPrettyName() {
        return "Dynamic time warping";
    }

    public DtwEditorPanel getEditorPanel() {
        return new DtwEditorPanel(settings);
    }
}
