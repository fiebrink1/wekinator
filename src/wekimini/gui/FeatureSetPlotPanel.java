/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package wekimini.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
    FeatureSetPlotItem[] features;
    double librarySize = 202;
    private static final double RADIUS = 15;
    
    public FeatureSetPlotPanel(){
        initComponents();
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
            default:
                break;
        }
        return new Color(80, 227, 194, alpha);
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
        if (image != null)
        {
            g.drawImage(image, 0, 0, null);
        }
        Graphics2D g2d = (Graphics2D)g;
        for(FeatureSetPlotItem f:features)
        {
            double x = ((librarySize-f.ranking)/librarySize) * w;
            x += (2 * RADIUS);
            double y = yForFeature(f.feature);
            Color c = colorForTags(f.feature.tags, false);
            g2d.setColor(c);
            double r = RADIUS;
            if(f.isSelected)
            {
                r += 5;
            }
            if(f.isInSet)
            {
                g2d.fill(new Ellipse2D.Double(x, y, r, r));
            }
            else
            {
                g2d.draw(new Ellipse2D.Double(x, y, r, r));
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 578, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
