/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning.dtw;

import static wekimini.learning.dtw.DtwSettings.RunningType.LABEL_CONTINUOUSLY;

/**
 *
 * @author rebecca
 */
public class DtwSettings {
    public static final double DEFAULT_MATCH_THRESHOLD = 0.3;
    public static final int DEFAULT_MIN_ALLOWED_GESTURE_LENGTH = 5; //TODO: Do somethign more reasonable with this (set from data)
    public static final int DEFAULT_HOP_SIZE_FOR_CONTINUOUS_SEARCH = 1;
    public static final RunningType DEFAULT_RUNNING_TYPE = LABEL_CONTINUOUSLY;
    public static final int DEFAULT_MATCH_WIDTH = 5;
    
   // private final double matchThreshold;
    private final int minAllowedGestureLength;
    private final int hopSizeForContinuousSearch;
    private final int matchWidth;

    public void dumpToConsole() {
        System.out.println("Min allowed length: " + minAllowedGestureLength   );
        System.out.println("Hop size: " + hopSizeForContinuousSearch);
        System.out.println("Match width: " + matchWidth);
    }
    
    public static enum RunningType {

        LABEL_CONTINUOUSLY, LABEL_ONCE_PER_RECORD
    }
    private final RunningType runningType;
    
    public DtwSettings() {
      //  matchThreshold = DEFAULT_MATCH_THRESHOLD;
        minAllowedGestureLength = DEFAULT_MIN_ALLOWED_GESTURE_LENGTH;
        hopSizeForContinuousSearch = DEFAULT_HOP_SIZE_FOR_CONTINUOUS_SEARCH;
        runningType = DEFAULT_RUNNING_TYPE;
        matchWidth = DEFAULT_MATCH_WIDTH;
    }
    
    public DtwSettings(int matchWidth, int minAllowedGestureLength, int hopSizeForContinuousSearch, RunningType runningType) {
       // this.matchThreshold = matchThreshold;
        this.minAllowedGestureLength = minAllowedGestureLength;
        this.hopSizeForContinuousSearch = hopSizeForContinuousSearch;
        this.runningType = runningType;
        this.matchWidth = matchWidth;
    }
    
    public DtwSettings(DtwSettings existing) {
       // this.matchThreshold = existing.matchThreshold;
        this.minAllowedGestureLength = existing.minAllowedGestureLength;
        this.hopSizeForContinuousSearch = existing.hopSizeForContinuousSearch;
        this.runningType = existing.runningType;
        this.matchWidth = existing.matchWidth;
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
    
}
