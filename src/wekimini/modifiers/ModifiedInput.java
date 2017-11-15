/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author rebecca
 */
public class ModifiedInput {
    
    protected int index = 0;
    protected ArrayList<Integer> requiredInputs = new ArrayList();
    public Boolean addToOutput = true;
    protected int inputsCalculated = 0;
    private double[] inputValues = new double[0];
    public int inputID;
    protected boolean dirty = true;
    
    @Override
    public boolean equals(Object obj)
    {
        boolean classMatch = this.getClass().isAssignableFrom(obj.getClass());
        if(classMatch)
        {
            ModifiedInput comparator = (ModifiedInput)obj;
            for(int input1:comparator.requiredInputs)
            {
                for(int input2:requiredInputs)
                {
                    if(input1 != input2)
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.requiredInputs);
        return hash;
    }
    
    public int getSize()
    {
        return 0;
    }
    
    public void reset()
    {
        
    }
    
    public void updateForInputs(double[] inputs)
    {
        dirty = true;
    }
    
    public Boolean hasAllInputs()
    {
        return inputsCalculated == requiredInputs.size();
    }
    
    public void collateInputsFromModifiers(List<ModifiedInput> modifiers)
    {
        for(ModifiedInput modifier: modifiers)
        {
            for(int input: requiredInputs)
            {
                if(input == modifier.inputID)
                {
                    double newInputs[] = new double[1];
                    if (modifier instanceof ModifiedInputSingle) 
                    {
                        newInputs[0] = ((ModifiedInputSingle)modifier).getValue();
                    }
                    else    
                    {
                        newInputs = ((ModifiedInputVector)modifier).getValues();
                    }
                    double [] newVals = new double[newInputs.length + inputValues.length];
                    System.arraycopy(inputValues, 0, newVals, 0, inputValues.length);
                    System.arraycopy(newInputs,0 , newVals, inputValues.length, newInputs.length);
                    inputValues = newVals;
                }
            }
        }
        updateForInputs(inputValues);
    }
    
    public void checkIfInputRequired(ModifiedInput modifier)
    {
        for(int input: requiredInputs)
        {
            if(input == modifier.inputID)
            {
                inputsCalculated++;
                break;
            }
        }
    }
    
    public void prepareForNewInputs()
    {
        inputsCalculated = 0;
        inputValues = new double[0];
    }
    
    public void addRequiredInput(int id)
    {
        requiredInputs.add(id);
    }
}
