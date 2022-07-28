/*
 * Information about an output, not the value of the output itself (that is handled by manager)
 */
package wekimini.osc;

import java.util.Random;
import wekimini.LearningModelBuilder;
import wekimini.learning.KNNModelBuilder;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCClassificationOutput implements OSCSupervisedLearningOutput {
    
    private final String name;
    private final int numClasses;
    private final boolean isSendingDistribution;
    //d


    public OSCClassificationOutput(String name, int numClasses, boolean isSendingDistribution) {
        this.name = name;
        this.numClasses = numClasses;
        this.isSendingDistribution = isSendingDistribution;
    }

    public boolean isSendingDistribution() {
        return isSendingDistribution;
    }
    
    
    public String getName() {
        return name;
    }
    
    public int getNumClasses() {
        return numClasses;
    }

    /* public static void writeToXMLFile(OSCClassificationOutput o, String filename) {
     Util.writeToXMLFile(o, "OSCClassificationOutput", OSCClassificationOutput.class, filename);
     //TODO: Better exception handling here: Util shouldn't catch all
     }

     public static OSCClassificationOutput readFromXMLFile(String filename) throws Exception {
     //TODO: Better exception handling
     return (OSCClassificationOutput) Util.readFromXMLFile(
     "OSCClassificationOutput", OSCClassificationOutput.class, filename);
     }  */
    @Override
    public String toString() {
        return Util.toXMLString(this, "OSCClassificationOutput", OSCClassificationOutput.class);
    }
    
    @Override
    public double generateRandomValue() {
        Random r = new Random();
        int i = r.nextInt(numClasses);
        return i + 1;
    }
    
    @Override
    public double getDefaultValue() {
        return 1;
    }
    
    @Override
    public LearningModelBuilder getDefaultModelBuilder() {
        return new KNNModelBuilder();
    }
    
    @Override
    public boolean isLegalTrainingValue(double value) {
        return isLegalOutputValue(value);
    }
    
    @Override
    public boolean isLegalOutputValue(double value) {
        if (value < 0 || value > numClasses) { //out of range
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
        if (which >numClasses) {
            which = numClasses;
        }
        return which;
    }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder("CLASSIFICATION,");
        sb.append(",NAME=").append(name);
        sb.append(",NUM_CLASSES=").append(numClasses);
        sb.append(",SENDING_DISTRIBUTION=").append(isSendingDistribution);
        return sb.toString();
    }
    
}
