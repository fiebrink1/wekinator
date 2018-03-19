/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.util.ArrayList;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class PlottedFeatureTableModel extends FeatureTableModel {
    
    private ArrayList<PlotRowModel> models = new ArrayList();
    
    public PlottedFeatureTableModel(Feature[] f) {
        super(f);
        for(Feature ft : f)
        {
            PlotRowModel model = new PlotRowModel(10);
            model.isStreaming = true;
            model.feature = ft;
            models.add(model);
        }
    }
    
    public PlotRowModel getModel(int index)
    {
        return models.get(index);
    }
    
    @Override
    public int getColumnCount()
    {
        return 3;
    }
    
}
