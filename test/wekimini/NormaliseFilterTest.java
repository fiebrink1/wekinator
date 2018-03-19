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

/**
 *
 * @author louismccallum
 */
public class NormaliseFilterTest {
    
    private Instances testSet;
    
    @Before
    public void setup()
    {
        FastVector ff = new FastVector(3);
        ff.addElement(new Attribute("input1"));
        ff.addElement(new Attribute("input2"));
        ff.addElement(new Attribute("input3"));
        ff.addElement(new Attribute("output"));
        testSet  = new Instances("dataset", ff, 100);
        testSet.setClassIndex(3);
        for(int i = 0; i <= 100; i++)
        {
            double[] vals = new double[4];
            vals[0] = i;
            vals[1] = i - 50;
            vals[2] = i - 100;
            vals[3] = i;
            testSet.add(new Instance(1, vals));
        }
    }
    
    @Test
    public void testBatchFilter()
    {
        BatchNormaliseFilter filter = new BatchNormaliseFilter();
        try {
            filter.setInputFormat(testSet);
            testSet = Filter.useFilter(testSet, filter);
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
    
}
