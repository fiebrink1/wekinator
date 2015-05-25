/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Component;
import java.beans.PropertyChangeListener;

/**
 *
 * @author rebecca
 */
public interface LearningRow {
    String PROP_VALUE = "value";

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Get the value of value
     *
     * @return the value of value
     */
    double getValue();

    boolean isChecked();

    boolean isSelected();

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    void setComputedValue(double value);

    void setModelName(String name);

    void setRecordEnabled(boolean e);

    void setRunEnabled(boolean e);

    void setSelected(boolean s);

    /**
     * Set the value of value
     *
     * @param value new value of value
     * TODO: This should probably NOT result in call to learning manager output value change!
     *      Put that in a separate function that's called only when GUI is modified.
     */
    void setValue(double value);

    void setValueOnlyForDisplay(double value);

    //This should only be
    void setValueQuietly(double value);
    
    public Component getComponent();
    
    public void updateValueGUI();
}
