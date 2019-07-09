/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import wekimini.modifiers.ReplaceColumnFilter;

/**
 *
 * @author louismccallum
 */
public class ReplaceColumnFilterTest {
    
    private Instances srcSet;
    private Instances destSet;
    
    @Before
    public void setup()
    {
        srcSet = getTestSet(100, 0);
        destSet = getTestSet(100, 1);
    }
    
    public Instances getTestSet(int size, int offset)
    {
        FastVector ff = new FastVector(3);
        ff.addElement(new Attribute("input1"));
        ff.addElement(new Attribute("input2"));
        ff.addElement(new Attribute("input3"));
        ff.addElement(new Attribute("output"));
        Instances set  = new Instances("dataset", ff, size);
        set.setClassIndex(3);
        for(int i = offset; i <= size+offset; i++)
        {
            double[] vals = new double[4];
            vals[0] = i;
            vals[1] = i - size / 2.0;
            vals[2] = i - size;
            vals[3] = i + 1;
            set.add(new Instance(1, vals));
        }
        return set;
    }
    
    @Test
    public void testSwap() throws Exception
    {
        ReplaceColumnFilter replace = new ReplaceColumnFilter();
        replace.setInputFormat(destSet);
        replace.source = srcSet;
        replace.sourceAttributeIndex = 0;
        replace.targetAttributeIndex = 3;
        //Not equal before
        for(int i = 0; i < destSet.numInstances(); i++)
        {
            assertNotEquals(destSet.instance(i).value(0), srcSet.instance(i).value(0), 0.0);
            assertNotEquals(destSet.instance(i).value(1), srcSet.instance(i).value(1), 0.0);
            assertNotEquals(destSet.instance(i).value(2), srcSet.instance(i).value(2), 0.0);
            assertNotEquals(destSet.instance(i).value(3), srcSet.instance(i).value(3), 0.0);
        }
        Instances newDestSet = Filter.useFilter(destSet, replace);
        //Only the specified column has been switched, other remain
        for(int i = 0; i < destSet.numInstances(); i++)
        {
            assertNotEquals(newDestSet.instance(i).value(0), srcSet.instance(i).value(0), 0.0);
            assertNotEquals(newDestSet.instance(i).value(1), srcSet.instance(i).value(1), 0.0);
            assertNotEquals(newDestSet.instance(i).value(2), srcSet.instance(i).value(2), 0.0);
            assertNotEquals(newDestSet.instance(i).value(3), srcSet.instance(i).value(3), 0.0);
            assertEquals(newDestSet.instance(i).value(0), destSet.instance(i).value(0), 0.0);
            assertEquals(newDestSet.instance(i).value(1), destSet.instance(i).value(1), 0.0);
            assertEquals(newDestSet.instance(i).value(2), destSet.instance(i).value(2), 0.0);
            assertEquals(newDestSet.instance(i).value(3), srcSet.instance(i).value(0), 0.0);
        }
    }
}
