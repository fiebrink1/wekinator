/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import wekimini.gui.PlotFrame.PlotRowModel;

/**
 *
 * @author louismccallum
 */
public interface PlotRowDelegate {
    
    public void modelChanged(PlotRowModel model); 
    public void closeButtonPressed(PlotRowModel model);
    public void streamingToggleChanged(PlotRowModel model);
    public void wasScrolled(int xPos);
}
