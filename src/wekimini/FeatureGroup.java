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

/**
 *
 * @author louismccallum
 * 
 * This has all the modifiers for a single path/output
 */
public class FeatureGroup {
    
    private List<ModifiedInput> modifiers;
    private int dimensionality;
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private transient double[] currentValues;
    private transient double[] lastInputs;
    private boolean dirtyFlag = true;
    
    public FeatureGroup(List<ModifiedInput> modifiers) 
    {
        this.modifiers = new LinkedList<>(modifiers);
        refreshState();
    }
    
    public FeatureGroup(FeatureGroup groupFromFile) {
        this.modifiers = new LinkedList<>(groupFromFile.getModifiers());
        this.dimensionality = groupFromFile.dimensionality;
    }
    
    private void refreshState()
    {
        int s = 0;
        boolean d = false;
        for (ModifiedInput output : modifiers) {
            s += output.getSize();
        }
        dimensionality = s;
        currentValues = new double[s];
    }
    
    //Modifiers 
    
    protected void addModifier(ModifiedInput modifier)
    {
        modifiers.add(modifier);
        refreshState();
        setDirty();
    }
    
    protected void removeModifier(int index)
    {
        modifiers.remove(index);
        refreshState();
        setDirty();
    }
    
    public List<ModifiedInput> getModifiers() {
        return modifiers;
    }
    
    public int getNumModifiers() {
        return modifiers.size();
    }
    
    public ModifiedInput getModifier(int which) {
        return modifiers.get(which);
    }
    
    //Dirty State
    
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
    
    //Outputs
    
    public int getOutputDimensionality() {
        return dimensionality;
    }
    
    public int getOutputNumber(FeatureGroup o) {
        for (int i= 0; i < modifiers.size(); i++) {
            if (modifiers.get(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }
    
    //Calculate outputs
    
    private void computeValuesForNewInputs(double[] newInputs) {
        int currentIndex = 0;
        
        //First do computations with no dependencies other than current inputs
        for (ModifiedInput modifier : modifiers) {
            modifier.updateForInputs(newInputs);
            if (modifier instanceof ModifiedInputSingle) {
                currentValues[currentIndex] = ((ModifiedInputSingle)modifier).getValue();
            } else {
                System.arraycopy(((ModifiedInputVector)modifier).getValues(), 0, currentValues, currentIndex, modifier.getSize());
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
    
    //Property Change Listeners
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
}
