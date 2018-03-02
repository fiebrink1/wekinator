/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author louismccallum
 */

public class Feature 
{
//    public enum InputDiagram {
//        ACCX,ACCY,ACCZ,GYROX,GYROY,GYROZ,MULTIPLE,UNKNOWN
//    }
    
    public final String name;
    public final ArrayList<Integer> modifierIds = new ArrayList();
    public InputDiagram diagram = InputDiagram.UNKNOWN;
    public int outputIndex = 0;
    public ArrayList<String> tags = new ArrayList();
    public String description;
    public double[] mins;
    public double[] maxs;
    public boolean doNormalise = false;

    public enum InputDiagram {
        ACCX,ACCY,ACCZ,GYROX,GYROY,GYROZ,MULTIPLE,UNKNOWN
    }
    
    public Feature(String name, boolean doNormalise)
    {
        this.name = name;
        this.doNormalise = doNormalise;
    }
    
    public void addFeature(ModifierCollection mc)
    {
        
    }  
    
    public int addModifier(ModifierCollection mc, ModifiedInput mod)
    {
        int id = mc.addModifier(mod);
        modifierIds.add(id);
        return id;
    }  
    
    @Override
    public String toString()
    {
        return name;
    }
    
    @Override
    public boolean equals(Object ft)
    {
        if(!(ft instanceof Feature))
        {
            return false;
        }
        return name.equals(((Feature)ft).name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.name);
        return hash;
    }
}