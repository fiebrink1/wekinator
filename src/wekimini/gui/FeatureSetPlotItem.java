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
public class FeatureSetPlotItem {
    
    Feature feature;
    FeaturePlotItemState state = FeaturePlotItemState.NORMAL;
    int ranking;
    double x;
    double y; 
    
    public enum FeaturePlotItemState {
        ADDING, REMOVING, NORMAL, HIDE, FADED
    };
}
