/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package wekimini.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class FeatureSetPlotPanel extends javax.swing.JPanel {
    private BufferedImage image;
    private double w = 1;
    private double imageHeight = 1;
    private double plotHeight = 1;
    FeatureSetPlotItem[] features = new FeatureSetPlotItem[0];
    double librarySize = 202;
    private static final double RADIUS = 15;
    private static final double MAX_MANHATTAN = 25;
    private Boolean loading = true;
    ImageIcon loadingIcon;
    private double threshold = 0.5;
    private Boolean highlightThreshold = false;
    private Timer thresholdTimer = null;
    private static final int THRESHOLD_HEIGHLIGHT_DECAY = 800;
    private final static BasicStroke DOTTED_STROKE = new BasicStroke(5, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_ROUND,
            0, 
            new float[]{9}, 
            0);
    private final static BasicStroke CIRCLE_STROKE = new BasicStroke(2, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_ROUND);
    
    public FeatureSetPlotPanel()
    {
        java.net.URL imgUrl = getClass().getResource("/wekimini/icons/ajax-loader.gif");
        loadingIcon = new ImageIcon(imgUrl);
    }
    
    public void showLoading()
    {
        loading = true;
    }
    
    public void hideLoading()
    {
        loading = false;
    }
    
    public void updateThreshold(double threshold)
    {
        if(thresholdTimer != null)
        {
            if(thresholdTimer.isRunning())
            {
                thresholdTimer.stop();
                thresholdTimer = null;
            }
        }
        
        this.threshold = threshold;
        highlightThreshold = true;
        repaint();
        
        thresholdTimer = new Timer(THRESHOLD_HEIGHLIGHT_DECAY, (ActionEvent evt) -> {
            System.out.println("timer done");
            highlightThreshold = false;
            repaint();
        });
        thresholdTimer.start();
    }
    
    public void setDimensions(int w, int h)
    {
        this.w = w;
        imageHeight = h;
        plotHeight = (h * 0.875) - 10.0;
        image = new BufferedImage(w, (int)imageHeight, BufferedImage.TYPE_INT_ARGB);
        setBackground(Color.white);
        clear();
    }
    
    public void update(FeatureSetPlotItem[] features)
    {
        this.features = features;
        repaint();
    }
    
    public Feature getNearest(double x, double y)
    {
        double min = Double.POSITIVE_INFINITY;
        Feature nearest = null;
        for(FeatureSetPlotItem f : features)
        {
           double dist = Math.abs(x - f.x) + Math.abs(y - f.y);
           if(dist < min)
           {
               min = dist;
               nearest = f.feature;
           }
        }
        return min < MAX_MANHATTAN ? nearest : null;
    }
    
    private double yForFeature(Feature ft)
    {
        double y = 0;
        double totalSensors = 7;
        switch(ft.sensor) {
            case ACCX:
                y = plotHeight * (0 / totalSensors);
                break;
            case ACCY:
                y = plotHeight * (1 / totalSensors);
                break;
            case ACCZ:
                y = plotHeight * (2 / totalSensors);
                break;
            case GYROX:
                y = plotHeight * (3 / totalSensors);
                break;
            case GYROY:
                y = plotHeight * (4 / totalSensors);
                break;
            case GYROZ:
                y = plotHeight * (5 / totalSensors);
                break;
            case MULTIPLE:
                y = plotHeight * (6 / totalSensors);
                break;
        }
        return y;
    }

    public static Color colorForTag(String tag, Boolean faded)
    {
        int alpha = faded ? 140 : 255;
        switch (tag) {
            case "Mean":
                return new Color(208, 2, 27, alpha);
            case "Standard Deviation":
                return new Color(254, 168, 35, alpha);
            case "Energy":
                return new Color(248, 231, 28, alpha);
            case "Max":
                return new Color(126, 211, 35, alpha);
            case "Min":
                return new Color(65, 117, 5, alpha);
            case "FFT":
                return new Color(189, 16, 224, alpha);
            case "1st Order Diff":
                return new Color(144, 19, 254, alpha);
            case "IQR":
                return new Color(74, 144, 26, alpha);
            case "Raw":
                return new Color(80, 227, 194, alpha);
            case "Magnitude":
                return new Color(18, 33, 219, alpha);
            case "Correlation":
                return new Color(139, 119, 120, alpha);
            default:
                break;
        }
        return new Color(255, 255, 255, alpha);
    }
    
    public static Color colorForTags(ArrayList<String> tags, Boolean faded)
    {
        if(tags.contains("Mean"))
        {
            return colorForTag("Mean", faded);
        }
        else if(tags.contains("Standard Deviation"))
        {
            return colorForTag("Standard Deviation", faded);
        }
        else if(tags.contains("Energy"))
        {
            return colorForTag("Energy", faded);
        }
        else if(tags.contains("Max"))
        {
            return colorForTag("Max", faded);
        }
        else if(tags.contains("Min"))
        {
            return colorForTag("Min", faded);
        }
        else if(tags.contains("FFT"))
        {
            return colorForTag("FFT", faded);
        }
        else if(tags.contains("1st Order Diff"))
        {
            return colorForTag("1st Order Diff", faded);
        }
        else if(tags.contains("IQR"))
        {
            return colorForTag("IQR", faded);
        }
        else if(tags.contains("Raw"))
        {
            return colorForTag("Raw", faded);
        }
        return colorForTag("Raw", faded);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if(loading)
        {
            loadingIcon.paintIcon(this, g, (int)w/2, (int)plotHeight/2);
            return;
        }
        
        if (image != null)
        {
            g.drawImage(image, 0, 0, null);
        }
        
        Graphics2D g2d = (Graphics2D)g;
        double thresholdX = ((1-threshold) * w);
        if(highlightThreshold)
        {
            g2d.setColor(Color.LIGHT_GRAY);
            double thresholdW = w - thresholdX;
            g2d.fill(new Rectangle2D.Double(thresholdX, 0, thresholdW, plotHeight));
        }
        else
        {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(DOTTED_STROKE);
            g2d.draw(new Line2D.Double(thresholdX, 0, thresholdX, plotHeight));
        }
        for(FeatureSetPlotItem f:features)
        {
            f.x = ((librarySize-f.ranking)/librarySize) * w;
            f.y = yForFeature(f.feature);
            Color c = colorForTags(f.feature.tags, false);
            g2d.setColor(c);
            g2d.setStroke(CIRCLE_STROKE);
            double r = RADIUS;
            if(f.isSelected)
            {
                r += 5;
            }
            if(f.isInSet)
            {
                g2d.fill(new Ellipse2D.Double(f.x, f.y, r, r));
            }
            else
            {
                g2d.draw(new Ellipse2D.Double(f.x, f.y, r, r));
            }
        }
    }
    
    public void clear()
    {
        createEmptyImage((int)w);
        repaint();
    }

    private void createEmptyImage(int width)
    {
        image = new BufferedImage(width, (int)imageHeight, BufferedImage.TYPE_INT_ARGB);
    } 
         

}
