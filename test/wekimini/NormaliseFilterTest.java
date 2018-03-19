/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import wekimini.modifiers.BatchNormaliseFilter;
import wekimini.modifiers.StreamNormaliseFilter;

/**
 *
 * @author louismccallum
 */
public class NormaliseFilterTest {
    
    private Instances testSet;
    
    @Before
    public void setup()
    {
        testSet = getTestSet(100);
    }
    
    public Instances getTestSet(int size)
    {
        FastVector ff = new FastVector(3);
        ff.addElement(new Attribute("input1"));
        ff.addElement(new Attribute("input2"));
        ff.addElement(new Attribute("input3"));
        ff.addElement(new Attribute("output"));
        Instances set  = new Instances("dataset", ff, size);
        set.setClassIndex(3);
        for(int i = 0; i <= size; i++)
        {
            double[] vals = new double[4];
            vals[0] = i;
            vals[1] = i - size / 2.0;
            vals[2] = i - size;
            vals[3] = i;
            set.add(new Instance(1, vals));
        }
        return set;
    }
    
    @Test
    public void testBatchFilter()
    {
        BatchNormaliseFilter batchFilter = new BatchNormaliseFilter();
        try {
            batchFilter.setInputFormat(testSet);
            testSet = Filter.useFilter(testSet, batchFilter);
        } catch (Exception ex) {
            Logger.getLogger(NormaliseFilterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(double i = 0; i < testSet.numInstances(); i++)
        {
            Instance inst = testSet.instance((int)i);
            assertEquals(i / 100, inst.value(0), 0.0);
            assertEquals(i / 100, inst.value(1), 0.0);
            assertEquals(i / 100, inst.value(2), 0.0);
            assertEquals(i, inst.value(3), 0.0);
        }
    }
    
    @Test
    public void testStreamFilter()
    {
        BatchNormaliseFilter batchFilter = new BatchNormaliseFilter();
        StreamNormaliseFilter streamFilter = new StreamNormaliseFilter();
        try {
            batchFilter.setInputFormat(testSet);
            streamFilter.setInputFormat(testSet);
            testSet = Filter.useFilter(testSet, batchFilter);
            streamFilter.batchFilter = batchFilter;
            Instances localSet = getTestSet(100);
            localSet = Filter.useFilter(localSet, streamFilter);
            for(double i = 0; i < localSet.numInstances(); i++)
            {
                Instance inst = localSet.instance((int)i);
                assertEquals(i / 100, inst.value(0), 0.0);
                assertEquals(i / 100, inst.value(1), 0.0);
                assertEquals(i / 100, inst.value(2), 0.0);
                assertEquals(i, inst.value(3), 0.0);
            }
        } catch (Exception ex) {
            Logger.getLogger(NormaliseFilterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
