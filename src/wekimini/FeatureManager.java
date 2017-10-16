/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;
import java.util.List;
import wekimini.modifiers.ModifiedInput;
import wekimini.modifiers.ModifiedInputSingle;
import wekimini.modifiers.ModifiedInputVector;
import wekimini.modifiers.UsesOnlyOriginalInputs;
import wekimini.modifiers.UsesInputsAndOutputs;
/**
 *
 * @author louismccallum
 */
public class FeatureManager 
{
    
    private List<ModifiedInput> modifiers;
    
    protected double[] modifyInputs(double[] newInputs)
    {        
         //Compute output values that
        int currentIndex = 0;
        double[] newValues = new double[numModifiedInputs()];
        //First do computations with no dependencies other than current inputs
        for (ModifiedInput modifier : modifiers) {
            if (modifier instanceof UsesOnlyOriginalInputs) {
                ((UsesOnlyOriginalInputs)modifier).updateForInputs(newInputs);
                if (modifier instanceof ModifiedInputSingle) {
                    newValues[currentIndex] = ((ModifiedInputSingle)modifier).getValue();
                } else {
                    System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, newValues, currentIndex, modifier.getSize());
                }
            } 
            currentIndex += modifier.getSize();
        }
        
        //Do the rest of the computations now
        for (ModifiedInput modifier : modifiers) {
            currentIndex = 0;
            if (modifier instanceof UsesInputsAndOutputs) {
                ((UsesOnlyOriginalInputs)modifier).updateForInputs(newInputs);
                if (modifier instanceof ModifiedInputSingle) {
                    newValues[currentIndex] = ((ModifiedInputSingle)modifier).getValue();
                } else {
                    System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, newValues, currentIndex, modifier.getSize());
                }
            } 
            currentIndex += modifier.getSize();
        }
        return newValues;
    }
    
    protected int numModifiedInputs()
    {
        int sum = 0;
        for(int i = 0; i < modifiers.size(); i++)
        {
            sum += modifiers.get(i).getSize();
        }
        return sum;
    }
}
