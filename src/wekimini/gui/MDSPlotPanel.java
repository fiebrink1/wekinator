/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author louismccallum
 */
public class MDSPlotPanel extends JPanel {
    
    private double w = 1;
    private double maxVal = 0;
    private double imageHeight = 1;
    private double plotHeight = 1;
    private BufferedImage image;
    private double x = 0;
    private double y = 0;
    private double plotY = 0;
    private final static BasicStroke STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    private Instances points;
    
    private MDSPlotPanel(){}
    
    public MDSPlotPanel(int w, int h)
    {
        this.w = w;
        imageHeight = h;
        plotHeight = (h * 0.875) - 10.0;
        plotY = 10.0;
        image = new BufferedImage(w, (int)imageHeight, BufferedImage.TYPE_INT_ARGB);
        setBackground(Color.white);
        x = 0;
        y = 0;
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
        g2d.setStroke(STROKE);
        
        for(int i = 0; i < points.numInstances(); i++)
        {
            Instance pt = points.instance(i);
            double x = pt.value(0);
            double y = pt.value(1);
            x *= (double)w / maxVal;
            y *= (double)imageHeight / maxVal;
            g2d.draw(new Line2D.Double(x, y, x + 1, y +1));
        }
    }
    
    public void updateWithInstances(Instances scaled)
    {
        points = scaled;
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < points.numInstances(); i++)
        {
            Instance pt = points.instance(i);
            if(pt.value(0) > max)
            {
                max = pt.value(0);
            }
            if(pt.value(1) > max)
            {
                max = pt.value(1);
            }
        }
        repaint();
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
