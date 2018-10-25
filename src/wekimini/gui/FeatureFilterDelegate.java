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
public interface FeatureFilterDelegate {
    public void backPressed();
    public void filtersUpdated();
    public void updateFeatures();
    public void selectAllFeatures();
    public void selectNoFeatures();
}
