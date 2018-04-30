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
public class WindowedOperation extends ModifiedInputSingleOutput {
    public final int windowSize;
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
    
    public interface Operation {
        public double doOperation(double[] vals, int startPointer);
        public String shortName();
    }
}
