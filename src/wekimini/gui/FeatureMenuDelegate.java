/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

/**
 *
 * @author louismccallum
 */
public interface FeatureMenuDelegate {
    public void addFeaturePressed();
    public void autoSelectPressed();
    public void removeFeaturePressed();
    public void backPressed();
    public void filtersUpdated();
    public void updateFeatures();
    public void selectAllFeatures();
}
