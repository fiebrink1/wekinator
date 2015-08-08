/*
 * Information about an output, not the value of the output itself (that is handled by manager)
 */
package wekimini.osc;

import java.util.Random;
import wekimini.learning.DtwModelBuilder;
import wekimini.learning.ModelBuilder;
import wekimini.util.Util;

/**
 * XXX in future: need to know whne to send:
 * - whenever new feature received
 * - whenever new output gesture matched
 * 
 * - send all as bundle vs send individual messages (one per output), or both
 * @author rebecca
 */
public class OSCDtwOutput implements OSCOutput {
    
    private final String name;
    private final int numGestures;
    private final String[] gestureNames;
    private OSCOutputGroup outputGroup;

    public OSCDtwOutput(String name, int numGestures) {
        this.name = name;
        this.numGestures = numGestures;
        this.gestureNames = new String[numGestures];
        populateNames();
    }
    
    public OSCDtwOutput(String name, String[] gestureNames, int numGestures) {
        this.name = name;
        this.numGestures = numGestures;
        if (gestureNames == null || gestureNames.length != numGestures) {
            throw new IllegalArgumentException("Gesture names must be same length as numGestures");
        }
        this.gestureNames = new String[numGestures];
        populateNames();
    }
    
    private void populateNames() {
        for (int i = 0; i < gestureNames.length; i++) {
            gestureNames[i] = name + "_" + (i+1);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public int getNumGestures() {
        return numGestures;
    }

    @Override
    public String toString() {
        return Util.toXMLString(this, "OSCDtwOutput", OSCDtwOutput.class);
    }
    
    @Override
    public double generateRandomValue() {
        Random r = new Random();
        int i = r.nextInt(numGestures);
        return i + 1;
    }
    
    @Override
    public double getDefaultValue() {
        return 1;
    }
    
    @Override
    public boolean isLegalTrainingValue(double value) {
        return isLegalOutputValue(value);
    }
    
    @Override
    public boolean isLegalOutputValue(double value) {
        if (value < 0 || value >= numGestures) { //out of range
            return false;
        }
        return Util.isInteger(value); //is it really an int?
    }
    
    @Override
    public double forceLegalTrainingValue(double value) {
        return forceLegalOutputValue(value);
    }
    
    @Override
    public double forceLegalOutputValue(double value) {
        int which = (int) value;
        if (which < 1) {
            which = 1;
        }
        if (which >numGestures) {
            which = numGestures;
        }
        return which;
    }
    
}
