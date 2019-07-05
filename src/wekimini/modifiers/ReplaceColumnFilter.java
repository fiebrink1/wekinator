/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;
import weka.core.*;
import weka.filters.*;
/**
 *
 * @author louismccallum
 */
public class ReplaceColumnFilter extends SimpleBatchFilter {
    
    public Instances source;
    public int sourceAttributeIndex = 0;
    public int targetAttributeIndex = 0;
    
    @Override
    public String globalInfo() {
        return "A swaps all values from a source attribute of a source instances into a target instances";
    }

    @Override
    protected Instances determineOutputFormat(Instances i){
        return i;
    }

    @Override
    protected Instances process(Instances input)
    {        
        for(int i = 0; i < input.numInstances(); i ++)
        {
            Instance srcInstance = source.instance(i);
            Instance targetInstance = input.instance(i);
            double srcValue = srcInstance.value(sourceAttributeIndex);
            targetInstance.setValue(targetAttributeIndex, srcValue);
        }
            
        return source;
    }
    
}
