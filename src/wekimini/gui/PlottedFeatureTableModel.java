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
    
    public PlottedFeatureTableModel(Feature[] f, int pointsPerRow) {
        super(f);
        for(Feature ft : f)
        {
            PlotRowModel model = new PlotRowModel(pointsPerRow);
            model.isStreaming = true;
            model.feature = ft;
            models.add(model);
        }
    }
   
    
    public PlotRowModel getModel(int index)
    {
        return models.get(index);
    }
    
    public PlotRowModel getModel(Feature ft)
    {
        for(int i = 0; i < models.size(); i++)
        {
            if(ft.name.equals(models.get(i).feature.name))
            {
                return models.get(i);
            }
        }
        return models.get(0);
    }
    
    @Override
    public int getColumnCount()
    {
        return 3;
    }
    
}
