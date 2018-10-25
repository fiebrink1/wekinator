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
    public void selectThresholdPressed(boolean above);
    public void removeFeaturePressed();
    public void exploreFeaturePressed();
}
