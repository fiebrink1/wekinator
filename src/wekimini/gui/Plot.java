/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.util.LinkedList;

/**
 *
 * @author louismccallum
 */
public class Plot {
       //The points to plot
   protected LinkedList<Float> points;
   
   //Determines how plot is shown on screen
   protected int pHeight = 100;
   protected int totalWidth = 200;
   protected int labelWidth = 50;
   protected int plotWidth = totalWidth - labelWidth;
   protected int numPointsToPlot = 100;
   protected int x = 0;
   protected int y = 0;
   protected float min = (float) 0.0001;
   protected float max = (float) 0.; 
   protected double horizontalScale = 1;
   String sMin = "0.0001";
   String sMax = "0.0";
   
   public Plot(int plotWidth, int plotHeight, int numPoints, int x, int y) {
      this.pHeight = plotHeight;
      this.totalWidth = plotWidth;
      this.plotWidth = totalWidth - labelWidth;
      this.numPointsToPlot = numPoints;
      this.x = x;
      this.y = y;
      points = new LinkedList<Float>();
   }
   
   //Resize plot after it's been created
   public void resize(int newWidth, int newHeight, int newX, int newY) {
     this.pHeight = newHeight;
      this.totalWidth = newWidth;
      this.plotWidth = totalWidth - labelWidth;
      this.x = newX;
      this.y = newY;  
      rescale();
   }  
   
   //Call when min, max, width, or number of points to plot changes
   protected void rescale() {
     horizontalScale = (double)plotWidth/numPointsToPlot;
     sMin = Float.toString(min);
     sMax = Float.toString(max);
   }
   
   //Add a new point to the data series we're plotting
   public void addPoint(float p) {
     if (points.size() == 0) {
       min = (float) (p - 0.0001);
       max = (float) (p + 0.0001);
       rescale();
     }
     
     if (p < min) {
       min = p;
       rescale();
     }
     if (p > max) {
       max = p;
       rescale();
     }
     
     //Use synchronized so we don't read from and edit linkedlist simultaneously
     synchronized(this) {
       points.add(p);
       while (points.size() > numPointsToPlot) {
         points.removeFirst();
       }
     }
   } 
}
