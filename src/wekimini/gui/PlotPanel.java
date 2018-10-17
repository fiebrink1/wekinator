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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
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
    public boolean renderWindowOverlay = false;
    private boolean highlightWindowOverlay = false;
    public boolean isDraggingWindowOverlay = false;
    public boolean interpolatePoints = true;
    private double plotY = 0;
    private final static BasicStroke THIN_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    private final static BasicStroke THICK_STROKE = new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    private boolean showLoadingSpinner = false;
    ImageIcon loadingIcon;
    
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
        java.net.URL imgUrl = getClass().getResource("/wekimini/icons/ajax-loader.gif");
        loadingIcon = new ImageIcon(imgUrl);
    }
    
    private void setUp()
    {
        setBackground(Color.white);
        x = 0;
        y = 0;
        reset();
    }
    
    public void showLoading(boolean show)
    {
        showLoadingSpinner = show;
        repaint();
    }
    
    public void updateModel(PlotRowModel model)
    {
        if(!showLoadingSpinner)
        {
            this.model = model;
            rescale();
            if(model.points.size() > 0)
            {
                repaint();
            }
        }
        
    }
    
    public void mouseMoved(int x)
    {
        double rectWidth = (double) (model.windowSize * horizontalScale);
        double rectX =  (w - rectWidth);
        boolean newVal = x > rectX;
        if(!highlightWindowOverlay == newVal)
        {
            repaint();
        }
        highlightWindowOverlay = newVal;
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
        
        if(showLoadingSpinner)
        {
            loadingIcon.paintIcon(this, g, (int)w/2, (int)plotHeight/2);
            return;
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
                if(interpolatePoints)
                {
                    if(model.classes.size() > n)
                    {
                        g2d.setColor(colorForClass(model.classes.get(n)));
                    }
                    g2d.setStroke(THIN_STROKE);
                    g2d.draw(new Line2D.Double(lastPointX, lastPointY, thisX, thisY)); 
                }
                else
                {
                    g2d.setStroke(THICK_STROKE);
                    g2d.setColor(colorForClass(f));
                    g2d.draw(new Line2D.Double(lastPointX, thisY, thisX, thisY)); 
                }
                lastPointX = thisX;
                lastPointY = thisY;
            }
        }
        if(renderWindowOverlay)
        {
            double rectWidth = (double) (model.windowSize * horizontalScale);
            double rectX =  (w - rectWidth);
            Color wColor;
            if(isDraggingWindowOverlay)
            {
                wColor = new Color(1.0f,0.8f,0.0f,0.5f);
            }
            else if (highlightWindowOverlay)
            {
                wColor = new Color(1.0f,1.0f,0.0f,0.2f).darker();
            }
            else
            {
                wColor = new Color(1.0f,1.0f,0.0f,0.2f);
            }
            
            int x1Points[] = {(int)rectX, (int)rectX - 10, (int)rectX};
            int y1Points[] = {(int)(imageHeight * 0.0), (int)(imageHeight * 0.5), (int)(imageHeight * 1.0)};
            GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD,x1Points.length);
            polygon.moveTo(x1Points[0], y1Points[0]);
            for (int index = 1; index < x1Points.length; index++) 
            {
                    polygon.lineTo(x1Points[index], y1Points[index]);
            }
            polygon.closePath();
            g2d.setColor(wColor.darker());
            g2d.fill(polygon);
            
            int x2Points[] = {(int)rectX, (int)rectX + 10, (int)rectX};
            int y2Points[] = {(int)(imageHeight * 0.0), (int)(imageHeight * 0.5), (int)(imageHeight * 1.0)};
            polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x1Points.length);
            polygon.moveTo(x2Points[0], y2Points[0]);
            for (int index = 1; index < x2Points.length; index++) 
            {
                polygon.lineTo(x2Points[index], y2Points[index]);
            }
            polygon.closePath();
            g2d.setColor(wColor.darker());
            g2d.fill(polygon);
            
            g2d.setColor(wColor);
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
