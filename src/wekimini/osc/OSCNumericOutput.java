/*
 * Information about an output, not the value of the output itself (that is handled by manager)
 */
package wekimini.osc;

import java.util.Random;
import wekimini.ModelBuilder;
import wekimini.SimpleModelBuilder;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCNumericOutput implements OSCOutput {
    private final String name;
    public static enum NumericOutputType {INTEGER, REAL};
    public static enum LimitType {HARD, SOFT};
    private final float min;
    private final float max;
    private final NumericOutputType outputType;
    private final LimitType limitType;
    //Issue: need to figure out how to handle outputGroup in XML; most likely don't want to store it explicitly
    private OSCOutputGroup outputGroup = null;
    
    
    //ID must be unique
    public OSCNumericOutput(String name, 
            float min, float max, 
            NumericOutputType outputType, LimitType limitType) {
        
        if (min > max) {
            throw new IllegalArgumentException("Illegal argument: min cannot be greater than max");
        }
        
        this.name = name;
        this.min = min;
        this.max = max;
        this.outputType = outputType;
        this.limitType = limitType;
    }

    public String getName() {
        return name;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public NumericOutputType getOutputType() {
        return outputType;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public OSCOutputGroup getOutputGroup() {
        return outputGroup;
    }

    public void setOutputGroup(OSCOutputGroup outputGroup) {
        this.outputGroup = outputGroup;
    }
    
    
    
    /*public static void writeToXMLFile(OSCNumericOutput o, String filename) {
        Util.writeToXMLFile(o, "OSCNumericOutput", OSCNumericOutput.class, filename);
        //TODO: Better exception handling here: Util shouldn't catch all
    }

    public static OSCNumericOutput readFromXMLFile(String filename) throws Exception {
        //TODO: Better exception handling
        return (OSCNumericOutput) Util.readFromXMLFile("OSCNumericOutput", OSCNumericOutput.class, filename);
    }  */

    @Override
    public String toString() {
        return Util.toXMLString(this, "OSCNumericOutput", OSCNumericOutput.class);
    }

    //TODO: test this!
    @Override
    public double generateRandomValue() {
        if (outputType == NumericOutputType.INTEGER) {
           Random r = new Random();
           int i = r.nextInt((int)(max - min) + 1);
           return min + i;
        } else {
            return Math.random() * (max - min) + min;
        }
    }
    
    @Override
    public double getDefaultValue() {
        return min;
    }
    
    @Override
    public ModelBuilder getDefaultModelBuilder() {
        return new SimpleModelBuilder();
    }
}
