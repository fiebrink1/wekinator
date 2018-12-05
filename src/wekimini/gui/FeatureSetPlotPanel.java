/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package wekimini.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
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
    private double plotWidth = 1;
    private double imageHeight = 1;
    private double imageWidth = 1;
    private double plotHeight = 1;
    FeatureSetPlotItem[] features = new FeatureSetPlotItem[0];
    double librarySize = 202;
    private static final double RADIUS = 15;
    private static final double MAX_MANHATTAN = 25;
    private Boolean loading = true;
    ImageIcon loadingIcon;
    private double threshold = 0.5;
    private Boolean highlightThreshold = false;
    public Boolean showThreshold = true;
    private Timer thresholdTimer = null;
    private static final int THRESHOLD_HEIGHLIGHT_DECAY = 200;
    private static final int PADDING = 20;
    private static final int FADED_ALPHA = 50;
    private static final int NORMAL_ALPHA = 220;
    private static final int FULL_ALPHA = 255;
    private final static BasicStroke DOTTED_STROKE = new BasicStroke(5, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_ROUND,
            0, 
            new float[]{9}, 
            0);
    private final static BasicStroke CIRCLE_STROKE = new BasicStroke(4, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_ROUND);
    private final static BasicStroke THIN_STROKE = new BasicStroke(2, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_ROUND);
    private final static BasicStroke SELECTED_STROKE = new BasicStroke(3, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_ROUND);
    public FeatureSetPlotItem selectedFeature;
    private FeatureSetPlotItem hoveredFeature;
        
    public FeatureSetPlotPanel()
    {
        java.net.URL imgUrl = getClass().getResource("/wekimini/icons/ajax-loader.gif");
        loadingIcon = new ImageIcon(imgUrl);
        MouseMotionListener plotMouseMotionListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                FeatureSetPlotItem newItem = getNearest(e.getX(), e.getY());
                if(hoveredFeature  != newItem)
                {
                    //System.out.println("hovered feature " + newItem.feature.name);
                    hoveredFeature = newItem;
                    repaint();
                }
            }
        };
        addMouseMotionListener(plotMouseMotionListener);
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
            highlightThreshold = false;
            repaint();
        });
        thresholdTimer.setRepeats(false);
        thresholdTimer.start();
    }
    
    public void setDimensions(int w, int h)
    {
        imageWidth = w;
        imageHeight = h;
        plotHeight = h - (2 * PADDING);
        plotWidth = w  - (2 * PADDING);
        setBackground(Color.white);
        clear();
    }
    
    public void update(FeatureSetPlotItem[] features, int librarySize)
    {
        this.features = features;
        this.librarySize = librarySize;
        
        repaint();
    }
    
    public FeatureSetPlotItem getNearest(double x, double y)
    {
        double min = Double.POSITIVE_INFINITY;
        FeatureSetPlotItem nearest = null;
        for(FeatureSetPlotItem f : features)
        {
           double dist = Math.abs(x - f.x) + Math.abs(y - f.y);
           if(dist < min)
           {
               min = dist;
               nearest = f;
           }
        }
        return min < MAX_MANHATTAN ? nearest : null;
    }
    
    private double yForFeature(Feature ft)
    {
        double y = 0;
        double totalSensors = 15;
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
           case EMG1:
                y = plotHeight * (6 / totalSensors);
                break;
            case EMG2:
                y = plotHeight * (7 / totalSensors);
                break;
            case EMG3:
                y = plotHeight * (8 / totalSensors);
                break;
            case EMG4:
                y = plotHeight * (9 / totalSensors);
                break;
            case EMG5:
                y = plotHeight * (10 / totalSensors);
                break;
            case EMG6:
                y = plotHeight * (11 / totalSensors);
                break;
            case EMG7:
                y = plotHeight * (12 / totalSensors);
                break;
            case EMG8:
                y = plotHeight * (13 / totalSensors);
                break;
            case MULTIPLE:
                y = plotHeight * (14 / totalSensors);
                break;
        }
        return y + (2 * PADDING);
    }

    public static Color colorForTag(String tag, int alpha)
    {
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
           case "Buffer":
                return new Color(139, 250, 120, alpha);
            default:
                break;
        }
        return null;
    }
    
    public static Color colorForTags(ArrayList<String> tags, int alpha)
    {
        if(tags.contains("Mean"))
        {
            return colorForTag("Mean", alpha);
        }
        else if(tags.contains("Standard Deviation"))
        {
            return colorForTag("Standard Deviation", alpha);
        }
        else if(tags.contains("Energy"))
        {
            return colorForTag("Energy", alpha);
        }
        else if(tags.contains("Max"))
        {
            return colorForTag("Max", alpha);
        }
        else if(tags.contains("Min"))
        {
            return colorForTag("Min", alpha);
        }
        else if(tags.contains("FFT"))
        {
            return colorForTag("FFT", alpha);
        }
        else if(tags.contains("1st Order Diff"))
        {
            return colorForTag("1st Order Diff", alpha);
        }
        else if(tags.contains("IQR"))
        {
            return colorForTag("IQR", alpha);
        }
        else if(tags.contains("Raw"))
        {
            return colorForTag("Raw", alpha);
        }
        else if(tags.contains("Buffer"))
        {
            return colorForTag("Buffer", alpha);
        }
        return colorForTag("Raw", alpha);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if(loading)
        {
            loadingIcon.paintIcon(this, g, (int)plotWidth/2, (int)plotHeight/2);
            return;
        }
        
        if (image != null)
        {
            g.drawImage(image, 0, 0, null);
        }
        
        Graphics2D g2d = (Graphics2D)g;
        double thresholdX = ((1-threshold) * plotWidth) + PADDING;
        if(highlightThreshold)
        {
            g2d.setColor(Color.LIGHT_GRAY);
            double thresholdW = plotWidth - thresholdX + (2 * PADDING);
            g2d.fill(new Rectangle2D.Double(thresholdX, 0, thresholdW, imageHeight));
        }
        
        for(FeatureSetPlotItem f:features)
        {
            f.x = ((librarySize-f.ranking)/librarySize) * plotWidth;
            f.x += PADDING;
            f.y = yForFeature(f.feature);
            int alpha = f.state == FeatureSetPlotItem.FeaturePlotItemState.FADED ? FADED_ALPHA : NORMAL_ALPHA;
            Color c = colorForTags(f.feature.tags, alpha);
            g2d.setColor(c);
            double r = RADIUS;
            if(f.state == FeatureSetPlotItem.FeaturePlotItemState.NORMAL
                    || f.state == FeatureSetPlotItem.FeaturePlotItemState.FADED)
            {
                g2d.setStroke(CIRCLE_STROKE);
                g2d.fill(new Ellipse2D.Double(f.x, f.y, r, r));
            }
        }
        for(FeatureSetPlotItem f:features)
        {
            Color c = colorForTags(f.feature.tags, FULL_ALPHA);
            g2d.setColor(c);
            double r  = RADIUS;
            if (f.state == FeatureSetPlotItem.FeaturePlotItemState.ADDING)
            {
                g2d.setStroke(CIRCLE_STROKE);
                g2d.draw(new Line2D.Double(f.x , f.y + r / 2, f.x + r, f.y + r / 2));
                g2d.draw(new Line2D.Double(f.x + r / 2 , f.y, f.x + r / 2, f.y + r));
            }
            else if (f.state == FeatureSetPlotItem.FeaturePlotItemState.REMOVING)
            {
                g2d.setStroke(CIRCLE_STROKE);
                g2d.draw(new Line2D.Double(f.x , f.y + r / 2, f.x + r, f.y + r / 2));
                
                int margin = 2;
                double x1Points[] = {f.x-margin, f.x + r + (margin), f.x + r + (margin) , f.x-margin};
                double y1Points[] = {f.y + (r / 2) + (margin*2), f.y + (r / 2) + (margin*2), f.y + (r / 2) - (margin*2), f.y + (r / 2) - (margin*2)};
                GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x1Points.length);
                polygon.moveTo(x1Points[0], y1Points[0]);
                for (int index = 1; index < x1Points.length; index++) 
                {
                    polygon.lineTo(x1Points[index], y1Points[index]);
                }
                polygon.closePath();
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(THIN_STROKE);
                g2d.draw(polygon);
            }
        }
        
        if(showThreshold)
        {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(DOTTED_STROKE);
            g2d.draw(new Line2D.Double(thresholdX, 0, thresholdX, imageHeight));
            AffineTransform at = new AffineTransform();
            at.rotate(Math.PI / 2);
            g2d.setTransform(at);
            g2d.setPaint(new Color(0.0f,0.0f,0.0f,1.0f));
            g2d.drawString("Threshold", (int)50, (int)-(thresholdX + 10));
            at.rotate(-Math.PI / 2);
            g2d.setTransform(at);
        }
        
        if(hoveredFeature != null)
        {
            g2d.setStroke(CIRCLE_STROKE);
            g2d.setColor(Color.MAGENTA);
            double r = RADIUS;
            int x = (int)hoveredFeature.x;
            int y =  (int)hoveredFeature.y;
            g2d.draw(new Ellipse2D.Double(x, y, r, r));
            String name = hoveredFeature.feature.name;
            int padding = 3;
            int width = g.getFontMetrics().stringWidth(name) + (padding *2);
            g2d.setColor(new Color(200, 200, 200, 200));
            int left = (int)(x - (width /2));
            int right = (int)(x + (width /2));
            
            int x1Points[] = {x, x - 10, left , left, right, right, x + 10};
            int y1Points[] = {y, y - 10, y - 10, y - 30, y - 30, y - 10, y - 10};
            GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x1Points.length);
            polygon.moveTo(x1Points[0], y1Points[0]);
            for (int index = 1; index < x1Points.length; index++) 
            {
                polygon.lineTo(x1Points[index], y1Points[index]);
            }
            polygon.closePath();
            g2d.fill(polygon);
            
            g2d.setPaint(new Color(0.0f,0.0f,1.0f,1.0f));
            g2d.drawString(name, left + padding, y - 14);
        }
    }
    
    public void clear()
    {
        createEmptyImage();
        repaint();
    }

    private void createEmptyImage()
    {
        image = new BufferedImage((int)imageWidth, (int)imageHeight, BufferedImage.TYPE_INT_ARGB);
    } 
         

}
