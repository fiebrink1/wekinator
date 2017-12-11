/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import javafx.scene.shape.Line;
import javax.swing.JPanel;

/**
 *
 * @author louismccallum
 */
public class PlotPanel extends JPanel {
    
    private final static int AREA_SIZE = 200;
    private BufferedImage image = new BufferedImage(AREA_SIZE, AREA_SIZE, BufferedImage.TYPE_INT_ARGB);
    int pointsPerRow = 100;
    ArrayList<Plot> plots = new ArrayList<Plot>(1);
    //Gap at top of screen
    int topGap = 30;
    //Vertical gap between plots
    int vertGap = 10;
    
    public PlotPanel()
    {
        setPreferredSize(new Dimension(AREA_SIZE, AREA_SIZE));
        setBackground(Color.white);
        Plot p = new Plot(AREA_SIZE - 20, AREA_SIZE - topGap - 10, pointsPerRow, 10, topGap);
        plots.add(p);
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        return isPreferredSizeSet() ? super.getPreferredSize() : new Dimension(AREA_SIZE, AREA_SIZE);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        //System.out.println("repaint");
        if (image != null)
        {
            g.drawImage(image, 0, 0, null);
        }

        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLUE);
        for(Plot plot : plots)
        {
            float lastPointX = 0;
            float lastPointY = 0;
            int numPts = plot.points.size();
            double ptSize = (double)plot.plotWidth / (double)numPts;
            int n = 0;
            for(float f : plot.points)
            {
                float thisX = plot.labelWidth + (float)(n * plot.horizontalScale) + plot.x;
                n++;
                float thisY = plot.y + plot.pHeight - ((f - plot.min)/(plot.max - plot.min)) * plot.pHeight;

                if (n == 1) {
                  //It's the first point
                  lastPointX = (float)thisX;
                  lastPointY = (float)thisY;
                } else {
                  //Draw a line from the last point to this point
                  g2d.draw(new Line2D.Double(lastPointX, lastPointY, thisX, thisY));
                  lastPointX = thisX;
                  lastPointY = thisY;
                }
            }
        }
    }
    
    public void clear()
    {
        createEmptyImage();
        repaint();
    }

    private void createEmptyImage()
    {
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        g2d.setColor(Color.BLACK);
        g2d.drawString("Add a rectangle by doing mouse press, drag and release!", 40, 15);
    }
    
    public void addPoint(float pt, int plot)
    {
        //System.out.println("new point added");
        plots.get(plot).addPoint(pt);
        repaint();
    }        
            
    
}
