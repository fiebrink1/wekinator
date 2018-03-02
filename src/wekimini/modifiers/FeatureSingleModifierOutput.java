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
public class FeatureSingleModifierOutput extends Feature {
    
    int outputModifierID;
    ModifiedInput outputModifier;

    public FeatureSingleModifierOutput(String name, boolean doNormalise) {
        super(name, doNormalise);
    }
    
    public int getOutputModifierID()
    {
        return outputModifierID;
    }
    
    public void setOutputModifier(int modifierID, ModifiedInput outputModifier)
    {
        this.outputModifierID = modifierID;
        this.outputModifier = outputModifier;
    }
    
    public void addNormalise(ModifierCollection mc)
    {
        if(outputModifier instanceof ModifiedInputSingle)
        {
            Normalise n = new Normalise("normalise", outputModifier.inputIndex, 0);
            n.addRequiredModifierID(outputModifierID);
            int nID = addModifier(mc,n);

            n.addToOutput = true;
            outputModifier.addToOutput = false; 

            setOutputModifier(nID, n);
        }
        else if(outputModifier instanceof ModifiedInputVector)
        {
            String[] names = new String[outputModifier.getSize()];
            for(int i = 0; i < names.length; i ++)
            {
                names[i] = "normalise" + ((ModifiedInputVector)outputModifier).names[i];
            }
            NormaliseVector nV = new NormaliseVector(names, 0);
            nV.addRequiredModifierID(outputModifierID);
            int nvID = addModifier(mc,nV);
            
            nV.addToOutput = true;
            outputModifier.addToOutput = false; 
            
            setOutputModifier(nvID, nV);
        }
    }
}
