/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public interface FeatureEditorDelegate {
    
    public void newFeatureSelected(Feature ft);
    public void featureListUpdated();
    public void featureLibraryUpdated(boolean sizeDidChange);
    
}
