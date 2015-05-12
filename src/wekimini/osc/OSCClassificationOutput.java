/*
 * Information about an output, not the value of the output itself (that is handled by manager)
 */
package wekimini.osc;

import java.util.Random;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCClassificationOutput implements OSCOutput {

    private final String name;
    private final int numClasses;
    private OSCOutputGroup outputGroup;

    //ID must be unique
    public OSCClassificationOutput(String name, int numClasses) {
        this.name = name;
        this.numClasses = numClasses;
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

}
