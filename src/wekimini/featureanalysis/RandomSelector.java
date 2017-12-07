/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.featureanalysis;

import java.util.Random;
import java.util.stream.IntStream;
import weka.core.Instances;

/**
 *
 * @author louismccallum
 */
public class RandomSelector extends FeatureSelector {
    
    double threshold = 0.2; 
    
    @Override
    public int[] getAttributeIndicesForInstances(Instances instances)
    {
        int len = instances.numAttributes() - 2;
        int[] indicies = IntStream.range(0, len).toArray();
        RandomSelector.shuffleArray(indicies);
        int i = (int)(((double)indicies.length)*threshold);
        int[] thresholded = new int[i];
        System.arraycopy(indicies, 0, thresholded, 0, i);
        return thresholded;
    }
    
    public static void shuffleArray(int[] array)
    {
        int index, temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}
