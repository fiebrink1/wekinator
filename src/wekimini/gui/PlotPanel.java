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
    
    private final static int PLOT_W = 500;
    private final static int PLOT_H = 75;
    private final static int WINDOW_H = 800;
    private BufferedImage image = new BufferedImage(PLOT_W, WINDOW_H, BufferedImage.TYPE_INT_ARGB);
    int topGap = 30;
    int vertGap = 10;
    protected int pHeight = 100;
    protected int totalWidth = 200;
    protected int labelWidth = 50;
    protected int plotWidth = totalWidth - labelWidth;
    protected int x = 0;
    protected int y = 0;
    protected double min = 0.0001;
    protected double max = 0.; 
    protected double horizontalScale = 1;
    String sMin = "0.0001";
    String sMax = "0.0";
    public boolean firstData = true;
    private int pointsPerRow = 0;
    private LinkedList<Double> points;
    private final static int POINTS_PER_ROW = 100;
    
    public PlotPanel()
    {
        setUp(POINTS_PER_ROW);
    }
    
    public void setUp(int pointsPerRow)
    {
        setBackground(Color.white);
        totalWidth = PLOT_W - 20;
        pHeight = PLOT_H - topGap - 10;
        plotWidth = totalWidth - labelWidth;
        x = 10;
        y = topGap;
        this.pointsPerRow = pointsPerRow;
    }
    
    public void updatePoints(LinkedList<Double> points)
    {
        this.points = points;
        if(points.size() > 0)
        {
            for(double pt : points)
            {
                rescaleWithPoint(pt);
            }
            repaint();
        }

    }
    
    public void updateWidth()
    {
        int width = (int)(labelWidth + (horizontalScale * points.size()));
        setPreferredSize(new Dimension(width,getHeight()));
        createEmptyImage(width);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (image != null)
        {
            g.drawImage(image, 0, 0, null);
        }

        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLUE);
        double lastPointX = 0;
        double lastPointY = 0;
        int numPts = points.size() < POINTS_PER_ROW ? points.size() : POINTS_PER_ROW;
        for(int n = 0; n < points.size(); n++)
        {
            double f  = points.get(n);
            double thisX = (double)(n * horizontalScale);
            double proportion = ((f - min)/(max - min));
            //System.out.println("f:" + f + " max:" + max + " min:" + min + " proportion:" + proportion);
            double thisY = y + pHeight - (proportion * pHeight);
            //System.out.println("n:" + n + " x:" + thisX + " y:" + thisY);
            if (n == 0) 
            {
                lastPointX = thisX;
                lastPointY = thisY;
            } 
            else 
            {
                g2d.draw(new Line2D.Double(lastPointX, lastPointY, thisX, thisY));
                lastPointX = thisX;
                lastPointY = thisY;
            }
        }
    }
   
   protected void rescale() 
   {
        horizontalScale = (double)plotWidth/(double)pointsPerRow;
        sMin = Double.toString(min);
        sMax = Double.toString(max);
   }
   
   public void rescaleWithPoint(double p) 
   {
        //System.out.println("new point:" + p);
        if (firstData) 
        {
            min = (double) (p - 0.0001);
            max = (double) (p + 0.0001);
            rescale();
            firstData = false;
        }
     
        if (p < min) 
        {
            min = p;
            rescale();
        }
        
        if (p > max) 
        {
            max = p;
            rescale();
        }
        //System.out.println("max:" + max + " min:" + min);
   }
    
    public void clear()
    {
        createEmptyImage(getWidth());
        repaint();
    }

    private void createEmptyImage(int width)
    {
        image = new BufferedImage(width, WINDOW_H, BufferedImage.TYPE_INT_ARGB);
    }       
}
