/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
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
public class FeatureGroup {
    
    private List<ModifiedInput> modifiers;
    private int numOutputTypes;
    private int dimensionality;
    private boolean hasDependencies;
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private transient double[] currentValues;
    private transient double[] lastInputs;
    private boolean dirtyFlag = true;
    
    public FeatureGroup(List<ModifiedInput> modifiers) 
    {
        this.modifiers = new LinkedList<>(modifiers);
        update();
    }
    
    public FeatureGroup(FeatureGroup groupFromFile) {
        this.modifiers = new LinkedList<>(groupFromFile.getModifiers());
        this.numOutputTypes = groupFromFile.numOutputTypes;
        this.dimensionality = groupFromFile.dimensionality;
        this.hasDependencies = groupFromFile.hasDependencies;
    }
    
    private void update()
    {
        this.numOutputTypes = this.modifiers.size();
        int s = 0;
        boolean d = false;
        for (ModifiedInput output : modifiers) {
            s += output.getSize();
            if (output instanceof UsesInputsAndOutputs) {
                d = true;
            } 
        }
        hasDependencies = d;
        dimensionality = s;
        currentValues = new double[s];
    }
    
    protected void addModifier(ModifiedInput modifier)
    {
        modifiers.add(modifier);
        update();
        setDirty();
    }
    
    protected void removeModifier(int index)
    {
        modifiers.remove(index);
        update();
        setDirty();
    }
    
    protected boolean isDirty()
    {
        return dirtyFlag;
    }
    
    protected void setDirty()
    {
       dirtyFlag = true;
       for(ModifiedInput modifier:modifiers)
       {
           modifier.reset();
       }
    }
    
    protected void didRecalculateFeatures()
    {
        dirtyFlag = false;
    }
    
    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public double[] getCurrentOutputs() {
         return currentValues;
    }
    
    //Doesn't do computation unless has dependencies that require it
    public void updateInputValues(double[] newInputs) {
        if (! hasDependencies) {
            for (ModifiedInput modifier : modifiers) {
                ((UsesOnlyOriginalInputs)modifier).updateForInputs(newInputs);
            }
        } else {
            computeValuesForNewInputs(newInputs);
        }
    }
    
    public void computeValuesForNewInputs(double[] newInputs) {
         //Compute output values that
        int currentIndex = 0;
        
        //First do computations with no dependencies other than current inputs
        for (ModifiedInput modifier : modifiers) {
            if (modifier instanceof UsesOnlyOriginalInputs) {
                ((UsesOnlyOriginalInputs)modifier).updateForInputs(newInputs);
                if (modifier instanceof ModifiedInputSingle) {
                    currentValues[currentIndex] = ((ModifiedInputSingle)modifier).getValue();
                } else {
                    System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, currentValues, currentIndex, modifier.getSize());
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
                    currentValues[currentIndex] = ((ModifiedInputSingle)modifier).getValue();
                } else {
                    System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, currentValues, currentIndex, modifier.getSize());
                }
            } 
            currentIndex += modifier.getSize();
        }
    }

    public double[] computeAndGetValuesForNewInputs(double[] newInputs) {
        computeValuesForNewInputs(newInputs);
        lastInputs = newInputs;
        return currentValues;
    }
    
    public double[] getLastInputs() {
        return lastInputs; //is returning null when nothing is computed yet...
    }
    
    public int getOutputDimensionality() {
        return dimensionality;
    }
    
    public int getNumOutputTypes() {
        return numOutputTypes;
    }
    
    public String[] getOutputNames() {
        int currentIndex = 0;
        String[] s = new String[getOutputDimensionality()];
        for (ModifiedInput o : modifiers) {
            if (o instanceof ModifiedInputSingle) {
                s[currentIndex] = ((ModifiedInputSingle)o).getName();
                currentIndex++;
            } else {
                for (int i = 0 ; i < o.getSize(); i++) {
                    s[currentIndex] = ((ModifiedInputVector)o).getNames()[i];
                    currentIndex++;
                }
            }
        }
        return s;
    }  
    
    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public int getOutputNumber(FeatureGroup o) {
        for (int i= 0; i < modifiers.size(); i++) {
            if (modifiers.get(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }
    
    public List<ModifiedInput> getModifiers() {
        return modifiers;
    }
    
    public ModifiedInput getModifier(int which) {
        return modifiers.get(which);
    }
    
}
