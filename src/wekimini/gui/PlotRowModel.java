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
    private final static int POINTS_PER_ROW = 100;
    public Feature feature = new Feature("feature1", false);
    int pathIndex = 0;
    int rowIndex = 0;
    boolean isStreaming = false;
    protected LinkedList<Double> points = new LinkedList();
    protected LinkedList<Double> classes = new LinkedList();

    public void addPoint(double pt)
    {
        synchronized(this) {
            points.add(pt);
            if(isStreaming)
            {
                while (points.size() > POINTS_PER_ROW) {
                    points.removeFirst();
                }
            }
        }
    } 
}
