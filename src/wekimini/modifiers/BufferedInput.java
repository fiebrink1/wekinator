/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author rebecca
 */
public class BufferedInput extends ModifiedInputVectorOutput {
    public final int bufferSize;
    private transient double[] history;
    private transient int startPointer;
    
    @Override
    public void reset()
    {
        history = new double[bufferSize];
        values = new double[bufferSize];
        startPointer = 0;
    }
    
    public BufferedInput(String originalName, int index, int bufferSize, int increment) {
        names = new String[bufferSize];
        if (increment == 1) {
            names[bufferSize-1] = originalName + "[n]";
            for (int i = 2; i <= bufferSize; i++) {
                names[bufferSize - i] = originalName + "[n-" + (i-1) + "]";
            }
        } else {
            names[bufferSize-1] = originalName + "[n](" + increment + ")";
            for (int i = 2; i <= bufferSize; i++) {
                names[bufferSize - i] = originalName + "[n-" + (i-1) + "](" + increment + ")";
            }
        }
        
        this.inputIndex = index;
        this.bufferSize = bufferSize;
        history = new double[bufferSize];
        values = new double[bufferSize];
        startPointer = 0;
    }

    @Override
    public void updateForInputs(double[] inputs) {
        history[startPointer] = inputs[inputIndex];
        startPointer++;
        if (startPointer == bufferSize) {
            startPointer = 0;
        }
        dirty = true;
    }

    @Override
    public int getSize() {
        return bufferSize;
    }

    @Override
    public double[] getValues() {
        if(dirty)
        {
            System.arraycopy(history, startPointer, values, 0, history.length - startPointer);
            System.arraycopy(history, 0, values, history.length - startPointer, startPointer);
            dirty = false;
        }
        return values;
    }
    
    public static void main(String[] args) {
        BufferedInput bi = new BufferedInput("feat1", 0, 3, 1);
        String[] s = bi.getNames();
        for (int i= 0; i < s.length; i++) {
            System.out.print(s[i] + ",");
        }
                
        for (int i = 0; i < 10; i++) {
            System.out.print(i + ": ");
            bi.updateForInputs(new double[] {i});
            double[] d = bi.getValues();
            for (int j = 0; j < bi.getSize(); j++) {
                System.out.print(d[j] + " ");
            }
        }
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        history = new double[bufferSize];
        values = new double[bufferSize];
        startPointer = 0;
    }
}
