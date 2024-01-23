/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning.dtw;

import static wekimini.learning.dtw.DtwSettings.MinimumLengthRestriction.SET_FROM_EXAMPLES;
import static wekimini.learning.dtw.DtwSettings.RunningType.LABEL_CONTINUOUSLY;

/**
 *
 * @author rebecca
 */
public class DtwSettings {
    public static final double DEFAULT_MATCH_THRESHOLD = 0.3;
    public static final int DEFAULT_MIN_ALLOWED_GESTURE_LENGTH = 5;
    public static final int DEFAULT_HOP_SIZE_FOR_CONTINUOUS_SEARCH = 1;
    public static final RunningType DEFAULT_RUNNING_TYPE = LABEL_CONTINUOUSLY;
    public static final int DEFAULT_MATCH_WIDTH = 5;
    public static final MinimumLengthRestriction DEFAULT_MIN_LENGTH_RESTRICTION = MinimumLengthRestriction.SET_CONSTANT;
    public static final DownsamplePolicy DEFAULT_DOWN_SAMPLE_POLICY = DownsamplePolicy.DOWNSAMPLE_TO_MAX_LENGTH;
    public static final int DEFAULT_DOWNSAMPLE_MAX_LENGTH = 10;
    public static final int DEFAULT_DOWNSAMPLE_FACTOR = 5;
    
   // private final double matchThreshold;
    private final int minAllowedGestureLength;
    private final int hopSizeForContinuousSearch;
    private final int matchWidth;
    private final int downsampleFactor;
    
    public void dumpToConsole() {
        System.out.println("Hop size: " + hopSizeForContinuousSearch);
        System.out.println("Match width: " + matchWidth);
        System.out.println("Running type: " + runningType);
        System.out.println("Minimum length restriction: " + minimumLengthRestriction);
        System.out.println("Min allowed length: " + minAllowedGestureLength   );
        System.out.println("Downsample policy: " + downsamplePolicy);
        System.out.println("Downsample constant factor: " + downsampleFactor);
        System.out.println("Downsample example length: " + downsampleMaxLength);
    }

    public Object toLogInfoString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HOP=").append(hopSizeForContinuousSearch);
        sb.append(",WIDTH=").append(matchWidth);
        sb.append(",TYPE=").append(runningType);
        sb.append(",MIN_LEN_R=").append(minimumLengthRestriction);
        sb.append(",MIN_ALLOWED_LEN=").append(minAllowedGestureLength);
        sb.append(",DOWN_POL=").append(downsamplePolicy);
        sb.append(",DOWN_FACT=").append(downsampleFactor);
        sb.append(",DOWN_EX_LEN=").append(downsampleMaxLength);
        return sb.toString();
    }
    
    public static enum RunningType {

        LABEL_CONTINUOUSLY, LABEL_ONCE_PER_RECORD
    }
    private final RunningType runningType;
    
    public static enum MinimumLengthRestriction {
        SET_FROM_EXAMPLES, SET_CONSTANT
    };
    private final MinimumLengthRestriction minimumLengthRestriction;
    
    public static enum DownsamplePolicy {
        DOWNSAMPLE_TO_MAX_LENGTH,
        DOWNSAMPLE_BY_CONSTANT_FACTOR,
        NO_DOWNSAMPLING
    } ;
    
    private final DownsamplePolicy downsamplePolicy;
    private final int downsampleMaxLength;
    
    public DtwSettings() {
      //  matchThreshold = DEFAULT_MATCH_THRESHOLD;
        minAllowedGestureLength = DEFAULT_MIN_ALLOWED_GESTURE_LENGTH;
        hopSizeForContinuousSearch = DEFAULT_HOP_SIZE_FOR_CONTINUOUS_SEARCH;
        runningType = DEFAULT_RUNNING_TYPE;
        matchWidth = DEFAULT_MATCH_WIDTH;
        minimumLengthRestriction = DEFAULT_MIN_LENGTH_RESTRICTION;
        downsampleMaxLength = DEFAULT_DOWNSAMPLE_MAX_LENGTH;
        downsamplePolicy = DEFAULT_DOWN_SAMPLE_POLICY;
        downsampleFactor = DEFAULT_DOWNSAMPLE_FACTOR;
    }
    
    public DtwSettings(int matchWidth, 
            int minAllowedGestureLength, 
            int hopSizeForContinuousSearch, 
            RunningType runningType, 
            MinimumLengthRestriction minLengthRestriction,
            DownsamplePolicy downsamplePolicy,
            int downsampleMaxLength, 
            int downsampleFactor) {
       // this.matchThreshold = matchThreshold;
        this.minAllowedGestureLength = minAllowedGestureLength;
        this.hopSizeForContinuousSearch = hopSizeForContinuousSearch;
        this.runningType = runningType;
        this.matchWidth = matchWidth;
        this.minimumLengthRestriction = minLengthRestriction;
        this.downsamplePolicy = downsamplePolicy;
        this.downsampleMaxLength = downsampleMaxLength;
        this.downsampleFactor = downsampleFactor;
    }
    
    public DtwSettings(DtwSettings existing) {
       // this.matchThreshold = existing.matchThreshold;
        this.minAllowedGestureLength = existing.minAllowedGestureLength;
        this.hopSizeForContinuousSearch = existing.hopSizeForContinuousSearch;
        this.runningType = existing.runningType;
        this.matchWidth = existing.matchWidth;
        this.minimumLengthRestriction = existing.minimumLengthRestriction;
        this.downsamplePolicy = existing.downsamplePolicy;
        this.downsampleMaxLength = existing.downsampleMaxLength;
        this.downsampleFactor = existing.downsampleFactor;
    }

   /* public double getMatchThreshold() {
        return matchThreshold;
    } */

    public int getMinAllowedGestureLength() {
        return minAllowedGestureLength;
    }

    public int getHopSizeForContinuousSearch() {
        return hopSizeForContinuousSearch;
    }

    public RunningType getRunningType() {
        return runningType;
    }

    public int getMatchWidth() {
        return matchWidth;
    }
    
    public DtwEditorPanel getEditorPanel() {
        return new DtwEditorPanel(this);
    }

    public MinimumLengthRestriction getMinimumLengthRestriction() {
        return minimumLengthRestriction;
    }

    public DownsamplePolicy getDownsamplePolicy() {
        return downsamplePolicy;
    }

    public int getDownsampleMaxLength() {
        return downsampleMaxLength;
    }

    public int getDownsampleFactor() {
        return downsampleFactor;
    }
    
    
    
}
