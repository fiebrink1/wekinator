/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;
import org.jtransforms.fft.DoubleFFT_1D;
/**
 *
 * @author louismccallum
 */
public class FFTModifier extends ModifiedInputVector {
    
    int[] outputBins;
    int windowSize;
    double[] history;
    int startPointer;
    double[] orderedBuffer;
    
    public FFTModifier(String originalName, int index, int windowSize, int[] outputBins)
    {
        this.windowSize = windowSize;
        this.outputBins = outputBins;
        this.inputIndex = index;
        names = new String[outputBins.length];
        for(int i = 0; i < outputBins.length; i++)
        {
            int binNum  = outputBins[i];
            names[i] = originalName + " " + binNum;
        }
        history = new double[windowSize];
        orderedBuffer = new double[windowSize];
        startPointer = 0;
    }
    
    @Override
    public void reset()
    {
        values = new double[getSize()];
        history = new double[windowSize];
        startPointer = 0;
    }
    
    @Override
    public void updateForInputs(double[] inputs) {
        history[startPointer] = inputs[inputIndex];
        startPointer++;
        if (startPointer == getSize()) {
            startPointer = 0;
        }
        dirty = true;
    }
    
    @Override
    public int getSize() 
    {
        return outputBins.length;
    }
    
    @Override
    public double[] getValues()
    {
        if(dirty)
        {
            
            System.arraycopy(history, startPointer, orderedBuffer, 0, history.length - startPointer);
            System.arraycopy(history, 0, orderedBuffer, history.length - startPointer, startPointer);
            DoubleFFT_1D fftDo = new DoubleFFT_1D(orderedBuffer.length);
            double[] fft = new double[orderedBuffer.length * 2];
            System.arraycopy(orderedBuffer, 0, fft, 0, orderedBuffer.length);
            fftDo.realForwardFull(fft);
            
            for(int i = 0; i < outputBins.length; i++)
            {
                int binNum  = outputBins[i];
                values[i] = fft[binNum];
            }
            dirty = false;
            
        }
        return values;
    }
}
