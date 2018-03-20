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
    
    private double w = 1;
    private double h = 1;
    private BufferedImage image;
    private double x = 0;
    private double y = 0;
    protected double horizontalScale = 1;
    private PlotRowModel model;
    private double pointsPerRow = 20;
    
    private PlotPanel(){}
    
    public PlotPanel(int w, int h, int pointsPerRow)
    {
        this.w = w;
        this.h = h * 0.875;
        this.pointsPerRow = pointsPerRow;
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        setUp();
        model = new PlotRowModel(pointsPerRow);
    }
    
    private void setUp()
    {
        setBackground(Color.white);
        x = 0;
        y = 0;
        reset();
    }
    
    public void updateModel(PlotRowModel model)
    {
        this.model = model;
        if(model.points.size() > 0)
        {
            repaint();
        }

    }
    
    public void updateWidth(boolean isStreaming)
    {
        double width = isStreaming ? w : (double)((horizontalScale * model.points.size()));
        width = width < 1 ? 1 : width;
        setPreferredSize(new Dimension((int)width,getHeight()));
        createEmptyImage((int)width);
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
        double numPts = model.points.size() < pointsPerRow ? model.points.size() : pointsPerRow;
        for(int n = 0; n < model.points.size(); n++)
        {
            double f  = model.points.get(n);
            double thisX = (double)(n * horizontalScale);
            double proportion = ((f - model.getMin())/(model.getMax() - model.getMin()));
            double thisY = y + h - (proportion * h);
            if (n == 0) 
            {
                lastPointX = thisX;
                lastPointY = thisY;
//                if(model.feature.name.equals("MeanAccX"))
//                {
//                    System.out.println("----");
//                    System.out.println(model.feature.outputIndex);
//                    System.out.println("f:" + f + " max:" + model.getMax() + " min:" + model.getMin() + " proportion:" + proportion);
//                }
            } 
            else 
            {
                if(model.classes.size() > n)
                {
                    g2d.setColor(colorForClass(model.classes.get(n)));
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
   }
   
   public void reset()
   {
       rescale();
   }
    
    public void clear()
    {
        createEmptyImage(getWidth());
        repaint();
    }

    private void createEmptyImage(int width)
    {
        image = new BufferedImage(width, (int)h, BufferedImage.TYPE_INT_ARGB);
    }       
}
