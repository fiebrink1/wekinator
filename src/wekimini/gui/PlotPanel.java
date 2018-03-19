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
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 *
 * @author louismccallum
 */
public class PlotPanel extends JPanel {
    
    private int w = 1;
    private int h = 1;
    private BufferedImage image;
    protected int x = 0;
    protected int y = 0;
    protected double min = 0.0001;
    protected double max = 0.; 
    protected double horizontalScale = 1;
    String sMin = "0.0001";
    String sMax = "0.0";
    public boolean firstData = true;
    private LinkedList<Double> points = new LinkedList();
    private LinkedList<Double> classes = new LinkedList();
    private int pointsPerRow = 20;
    
    private PlotPanel(){}
    
    public PlotPanel(int w, int h, int pointsPerRow)
    {
        this.w = w;
        this.h = h;
        this.pointsPerRow = pointsPerRow;
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        setUp();
    }
    
    private void setUp()
    {
        setBackground(Color.white);
        x = 0;
        y = 0;
    }
    
    public void updateModel(PlotRowModel model)
    {
        points = model.points;
        classes = model.classes;
        if(points.size() > 0)
        {
            for(double pt : points)
            {
                rescaleWithPoint(pt);
            }
            repaint();
        }

    }
    
    public void updateWidth(boolean isStreaming)
    {
        int width = isStreaming ? w : (int)((horizontalScale * points.size()));
        width = width < 1 ? 1 : width;
        setPreferredSize(new Dimension(width,getHeight()));
        createEmptyImage(width);
    }
    
    public Color colorForClass(double classVal)
    {
        switch((int)classVal)
        {
            case 1: return Color.BLUE;
            case 2: return Color.RED;
            case 3: return Color.ORANGE;
            case 4: return Color.PINK;
            case 5: return Color.BLACK;
            case 6: return Color.MAGENTA;
            case 7: return Color.GREEN;
            case 8: return Color.CYAN;
            case 9: return Color.YELLOW;
        }
        return Color.BLUE;
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
        int numPts = points.size() < pointsPerRow ? points.size() : pointsPerRow;
        for(int n = 0; n < points.size(); n++)
        {
            double f  = points.get(n);
            double thisX = (double)(n * horizontalScale);
            double proportion = ((f - min)/(max - min));
            //System.out.println("f:" + f + " max:" + max + " min:" + min + " proportion:" + proportion);
            double thisY = y + h - (proportion * h);
            //System.out.println("n:" + n + " x:" + thisX + " y:" + thisY);
            if (n == 0) 
            {
                lastPointX = thisX;
                lastPointY = thisY;
            } 
            else 
            {
                if(classes.size() > n)
                {
                    g2d.setColor(colorForClass(classes.get(n)));
                }
                g2d.draw(new Line2D.Double(lastPointX, lastPointY, thisX, thisY));
                lastPointX = thisX;
                lastPointY = thisY;
            }
        }
    }
   
   protected void rescale() 
   {
        horizontalScale = (double)w/(double)pointsPerRow;
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
        image = new BufferedImage(width, h, BufferedImage.TYPE_INT_ARGB);
    }       
}
