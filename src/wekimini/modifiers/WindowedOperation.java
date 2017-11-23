/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;


/**
 *
 * @author rebecca
 */
public class WindowedOperation extends ModifiedInputSingle {
    protected final int windowSize;
    private transient double[] history;
    private transient int startPointer;
    private final Operation op;
    
    public static String makeName(String originalName, String shortName, int windowSize, int nameIncrement) {
        if (nameIncrement == 1) {
            return originalName + "_" + shortName + Integer.toString(windowSize);
        } else {
            return originalName + "_" + shortName + Integer.toString(windowSize) + "(" + nameIncrement + ")";
        }
    }

    public Operation getOp() {
        return op;
    }
    
    public WindowedOperation(String originalName, Operation op, int index, int windowSize, int nameIncrement) {
        name = makeName(originalName, op.shortName(), windowSize, nameIncrement);       
        this.inputIndex = index;
        this.windowSize = windowSize;
        this.op = op;
        history = new double[windowSize];
        startPointer = 0;
    }
    
    @Override
    public void reset()
    {
        history = new double[windowSize];
        startPointer = 0;
    }

    @Override
    public void updateForInputs(double[] inputs) {
        history[startPointer] = inputs[inputIndex];
        startPointer++;
        if (startPointer == windowSize) {
            startPointer = 0;
        }
        dirty = true;
    }
    
    public int getWindowSize()
    {
        return windowSize;
    }

    @Override
    public double getValue() {
        if(dirty)
        {
            value = op.doOperation(history, startPointer);
            dirty = false;
        }
        return value;
    }
    
    public static void main(String[] args) {
        
        Operation avg = new Operation() {

            @Override
            public double doOperation(double[] vals, int startPtr) {
                double sum = 0;
                for (int i = 0; i < vals.length; i++) {
                    sum += vals[i];
                }
                return sum / vals.length;
            }

            @Override
            public String shortName() {
                return "Avg";
            }
        };
        WindowedOperation bi = new WindowedOperation("feat1", avg, 0, 3, 1);
   
        for (int i = 0; i < 10; i++) {
            bi.updateForInputs(new double[] {i});
            double d = bi.getValue();
        }
    }
    
    public interface Operation {
        public double doOperation(double[] vals, int startPointer);
        public String shortName();
    }
}
