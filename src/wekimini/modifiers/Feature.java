/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.ArrayList;

/**
 *
 * @author louismccallum
 */

public class Feature 
{
    public enum InputDiagram {
        ACCX,ACCY,ACCZ,GYROX,GYROY,GYROZ,MULTIPLE,UNKNOWN
    }
    
    public final String name;
    public final ArrayList<Integer> ids = new ArrayList();
    public InputDiagram diagram = InputDiagram.UNKNOWN;
    public ArrayList<String> tags = new ArrayList();
    public String description;

//    public enum InputDiagram {
//        ACCX,ACCY,ACCZ,GYROX,GYROY,GYROZ,MULTIPLE,UNKNOWN
//    }
    
    public Feature(String name)
    {
        this.name = name;
    }
    
    public void addFeature(ModifierCollection mc)
    {
        
    }  
    
    public int addModifier(ModifierCollection mc, ModifiedInput mod)
    {
        int id = mc.addModifier(mod);
        ids.add(id);
        return id;
    }  
    
    @Override
    public String toString()
    {
        return name;
    }
}