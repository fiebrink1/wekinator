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
import java.util.ArrayList;
import javax.swing.ImageIcon;
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
    private double minVal = 0;
    private double imageHeight = 1;
    private double plotHeight = 1;
    private BufferedImage image;
    private double plotY = 0;
    private double plotX = 0;
    private final static BasicStroke STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    private Instances points;
    private ArrayList<Integer> keyClasses = new ArrayList();
    private boolean needsUpdating = true;
    private Boolean loading = false;
    ImageIcon loadingIcon;
    
    private MDSPlotPanel(){}
    
    public MDSPlotPanel(int w, int h)
    {
        plotY = 10.0;
        plotX = 10.0;
        this.w = w - plotX;
        imageHeight = h;
        plotHeight = (h * 0.875) - plotY;
        image = new BufferedImage(w, (int)imageHeight, BufferedImage.TYPE_INT_ARGB);
        setBackground(Color.white);
        java.net.URL imgUrl = getClass().getResource("/wekimini/icons/ajax-loader.gif");
        loadingIcon = new ImageIcon(imgUrl);
    }
    
    
    
    public Color colorForClass(double classVal)
    {
        switch((int)classVal)
        {
            case 1: return Color.BLUE;
            case 2: return Color.RED;
            case 3: return Color.ORANGE;
            case 4: return Color.GREEN;
            case 5: return Color.BLACK;
            case 6: return Color.MAGENTA;
            case 7: return Color.PINK;
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
        g2d.setStroke(STROKE);
        
        if(loading)
        {
            loadingIcon.paintIcon(this, g, (int)w/2, (int)plotHeight/2);
        }
        else if(needsUpdating)
        {
            g2d.setPaint(new Color(0.0f,0.0f,1.0f,1.0f));
            g2d.drawString("Needs Updating", (int)w/2, (int)plotHeight/2);
        }
        else if(points != null)
        {
            double delta = maxVal - minVal;
            for(int i = 0; i < points.numInstances(); i++)
            {
                Instance pt = points.instance(i);
                //System.out.println("vals (" + pt.value(0) + "," +pt.value(1) +")");
                double x = pt.value(0) - minVal;
                double y = pt.value(1) - minVal;
                g2d.setColor(colorForClass(pt.value(2)));
                x *= ((double)w / delta);
                y *= ((double)plotHeight / delta);
                y += plotY;
                x += plotX;
                //System.out.println("plotting (" + x + "," + y +")");
                g2d.draw(new Line2D.Double(x, y, x + 1, y +1));
            }
            int keyH = 15;
            int totalH = keyClasses.size() * keyH;
            int startY = ((int)plotHeight - totalH) / 2;
            for(int i = 0; i < keyClasses.size(); i++)
            {
                g2d.setPaint(colorForClass(keyClasses.get(i)));
                g2d.drawString( ""+(i+1), 2, startY + ((i+1)*keyH));
            }
        }  
    }
    
    public void showLoading()
    {
        loading = true;
        repaint();
    }
    
    public void hideLoading()
    {
        loading = false;
        repaint();
    }
    
    public void setOutOfDate()
    {
        needsUpdating = true;
        repaint();
    }
       
    public void updateWithInstances(Instances scaled)
    {
        needsUpdating = false;
        points = scaled;
        maxVal = Double.NEGATIVE_INFINITY;
        minVal = Double.POSITIVE_INFINITY;
        keyClasses.clear();
        for(int i = 0; i < points.numInstances(); i++)
        {
            Instance pt = points.instance(i);
            if(pt.value(0) > maxVal)
            {
                maxVal = pt.value(0);
            }
            if(pt.value(1) > maxVal)
            {
                maxVal = pt.value(1);
            }
            if(pt.value(0) < minVal)
            {
                minVal = pt.value(0);
            }
            if(pt.value(1) < minVal)
            {
                minVal = pt.value(1);
            }
            int keyClass = (int)pt.value(2);
            if(!keyClasses.contains(keyClass))
            {
                keyClasses.add(keyClass);
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
