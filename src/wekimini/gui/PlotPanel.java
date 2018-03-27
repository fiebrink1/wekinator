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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author louismccallum
 */
public class PlotPanel extends JPanel {
    
    private double w = 1;
    private double imageHeight = 1;
    private double plotHeight = 1;
    private BufferedImage image;
    private double x = 0;
    private double y = 0;
    protected double horizontalScale = 1;
    private PlotRowModel model;
    public boolean renderWindow = false;
    private double plotY = 0;
    
    private PlotPanel(){}
    
    public PlotPanel(int w, int h)
    {
        this.w = w;
        imageHeight = h;
        plotHeight = (h * 0.875) - 10.0;
        plotY = 10.0;
        image = new BufferedImage(w, (int)imageHeight, BufferedImage.TYPE_INT_ARGB);
        model = new PlotRowModel(20);
        setUp();
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
        //System.out.println("ppr:" + model.pointsPerRow + " points:" + model.points.size());
        this.model = model;
        rescale();
        if(model.points.size() > 0)
        {
            repaint();
        }
    }
    
    public void updateWidth(int newW)
    {
        this.w = newW;
        setPreferredSize(new Dimension((int)w,getHeight()));
        createEmptyImage((int)w);
        rescale();
    }
    
    public void updateWidth(boolean isStreaming)
    {
        double width = isStreaming ? w : (double)((horizontalScale * model.points.size()));
        width = width < 1 ? 1 : width;
        setPreferredSize(new Dimension((int)width,getHeight()));
        createEmptyImage((int)width);
        rescale();
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
        for(int n = 0; n < model.points.size(); n++)
        {
            double f  = model.points.get(n);
            double thisX = (double)(n * horizontalScale);
            double proportion = ((f - model.getMin())/(model.getMax() - model.getMin()));
            double thisY = plotY + y + plotHeight - (proportion * plotHeight);
            if (n == 0) 
            {
                lastPointX = thisX;
                lastPointY = thisY;
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
        if(renderWindow)
        {
            g2d.setColor(new Color(1.0f,1.0f,0.0f,0.2f));
            double rectWidth = (double) (model.windowSize * horizontalScale);
            double rectX =  (w - rectWidth);
            g2d.fill(new Rectangle2D.Double(rectX, 0, rectWidth, imageHeight));
            g2d.setPaint(new Color(0.0f,0.0f,1.0f,1.0f));
            g2d.drawString("Window:"+model.windowSize, (float)rectX + 5, 10.0f);
        }
    }
   
   protected void rescale() 
   {
        horizontalScale = (double)w/(double)model.pointsPerRow;
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
        image = new BufferedImage(width, (int)imageHeight, BufferedImage.TYPE_INT_ARGB);
    }       
}
