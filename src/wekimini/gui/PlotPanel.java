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
    protected float min = (float) 0.0001;
    protected float max = (float) 0.; 
    protected double horizontalScale = 1;
    String sMin = "0.0001";
    String sMax = "0.0";
    boolean firstData = true;
    int pointsPerRow = 0;
    private LinkedList<Float> points;
    private final static int POINTS_PER_ROW = 100;
    
    public PlotPanel()
    {
        setUp(POINTS_PER_ROW);
    }
    
    public void setUp(int pointsPerRow)
    {
        setPreferredSize(new Dimension(PLOT_W, PLOT_H));
        setBackground(Color.white);
        totalWidth = PLOT_W - 20;
        pHeight = PLOT_H - topGap - 10;
        plotWidth = totalWidth - labelWidth;
        x = 10;
        y = topGap;
        this.pointsPerRow = pointsPerRow;
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        return isPreferredSizeSet() ? super.getPreferredSize() : new Dimension(PLOT_W, WINDOW_H);
    }
    
    public void updatePoints(LinkedList<Float> points)
    {
        this.points = points;
        if(points.size() > 0)
        {
            rescaleWithPoint(points.getLast());
            repaint();
        }

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
        float lastPointX = 0;
        float lastPointY = 0;
        int numPts = points.size();
        double ptSize = (double)plotWidth / (double)numPts;
        int n = 0;
        for(float f : points)
        {
            float thisX = labelWidth + (float)(n * horizontalScale) + x;
            n++;
            float thisY = y + pHeight - ((f - min)/(max - min)) * pHeight;

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
    
   public void resize(int newWidth, int newHeight, int newX, int newY) {
        this.pHeight = newHeight;
        this.totalWidth = newWidth;
        this.plotWidth = totalWidth - labelWidth;
        this.x = newX;
        this.y = newY;  
        rescale();
   }  
   
   //Call when min, max, width, or number of points to plot changes
   protected void rescale() 
   {
        horizontalScale = (double)plotWidth/(double)pointsPerRow;
        sMin = Float.toString(min);
        sMax = Float.toString(max);
   }
   
   public void rescaleWithPoint(float p) 
   {
        if (firstData) {
          min = (float) (p - 0.0001);
          max = (float) (p + 0.0001);
          rescale();
          firstData = false;
        }
     
        if (p < min) {
          min = p;
          rescale();
        }
        if (p > max) {
          max = p;
          rescale();
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
}
