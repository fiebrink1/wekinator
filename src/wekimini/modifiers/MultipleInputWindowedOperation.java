/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

/**
 *
 * @author louismccallum
 */

public class MultipleInputWindowedOperation extends ModifiedInputSingle {
    
    private final int windowSize;
    private double[][] history;
    private transient int startPointer;
    private final MultipleInputOperation op;
    protected int[] inputIndexes;

    public static String makeName(String originalName, String shortName, int windowSize, int nameIncrement) {
        if (nameIncrement == 1) {
            return originalName + "_" + shortName + Integer.toString(windowSize);
        } else {
            return originalName + "_" + shortName + Integer.toString(windowSize) + "(" + nameIncrement + ")";
        }
    }

    public MultipleInputOperation getOp() {
        return op;
    }
    
    public MultipleInputWindowedOperation(String originalName, MultipleInputOperation op, int windowSize, int nameIncrement) {
        name = makeName(originalName, op.shortName(), windowSize, nameIncrement);       
        this.windowSize = windowSize;
        this.op = op;
        history = new double[1][windowSize];
        startPointer = 0;
    }
    
    @Override
    public void reset()
    {
        history = new double[inputIndexes.length][windowSize];
        value = 0;
        startPointer = 0;
    }

    @Override
    public void updateForInputs(double[] inputs) {
        for(int i = 0; i < inputIndexes.length; i++)
        {
            history[i][startPointer] = inputs[inputIndexes[i]];
        }
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
    
    @Override
    public void addRequiredInput(int id)
    {
        requiredInputs.add(id);
        inputIndexes = new int[requiredInputs.size()];
        history = new double[inputIndexes.length][windowSize];
    }
    
    public interface MultipleInputOperation {
        public double doOperation(double[][] vals, int startPointer);
        public String shortName();
    }
    
}
