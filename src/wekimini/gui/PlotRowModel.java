/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.util.LinkedList;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class PlotRowModel
{
    public Feature feature = new Feature("PassThroughAll");
    public int pathIndex = 0;
    public int rowIndex = 0;
    private double min;
    private double max;
    public boolean isStreaming = false;
    protected LinkedList<Double> points = new LinkedList();
    protected LinkedList<Double> classes = new LinkedList();
    private int pointsPerRow = 20;
    
    private PlotRowModel(){}
    
    public PlotRowModel(int pointsPerRow)
    {
        this.pointsPerRow = pointsPerRow;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }
    
    public double getMin()
    {
        return min;
    }
    
    public double getMax()
    {
        return max;
    }
    
    public void addPoint(double pt)
    {
        synchronized(this) {
            points.add(pt);
            if(pt < min)
            {
                min = pt;
            }
            if(pt > max)
            {
                max = pt;
            }
            if(isStreaming)
            {
                while (points.size() > pointsPerRow) {
                    points.removeFirst();
                }
            }
        }
    } 
}
