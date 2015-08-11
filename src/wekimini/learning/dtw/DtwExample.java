/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning.dtw;

import com.timeseries.TimeSeries;

/**
 *
 * @author rebecca
 */
public class DtwExample {

    private final TimeSeries timeSeries;
    private TimeSeries downsampledTimeSeries;
    private final int gestureClass;
    private final int id;
    public static int MAX_ID = 0;

    public DtwExample(TimeSeries timeSeries, int id, int gestureClass) {
        this.timeSeries = timeSeries;
        this.downsampledTimeSeries = new TimeSeries(timeSeries);
        this.id = id;
        if (id > MAX_ID) {
            MAX_ID = id;
        }
        this.gestureClass = gestureClass;
    }
    
    public DtwExample(TimeSeries timeSeries, TimeSeries downsampled, int id, int gestureClass) {
        this.timeSeries = timeSeries;
        this.downsampledTimeSeries = downsampled;
        this.id = id;
        if (id > MAX_ID) {
            MAX_ID = id;
        }
        this.gestureClass = gestureClass;
    }

    public static int generateNextID() {
        return MAX_ID + 1;
    }

    /**
     * Get the value of timeSeries
     *
     * @return the value of timeSeries
     */
    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    public int getId() {
        return id;
    }

    public void setDownsampledTimeSeries(TimeSeries downsampledTimeSeries) {
        this.downsampledTimeSeries = downsampledTimeSeries;
    }
    
    public TimeSeries getDownsampledTimeSeries() {
        return downsampledTimeSeries;
    }
    
    //Starts with 0
    public int getGestureClass() {
        return gestureClass;
    }
    
}
