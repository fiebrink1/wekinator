/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.modifiers;

import java.util.ArrayList;
import java.util.List;
import wekimini.FeatureGroup;

/**
 *
 * @author louismccallum
 */
public class Feature 
{
    public final String name;
    public final ArrayList<Integer> ids = new ArrayList();
    
    public Feature(String name)
    {
        this.name = name;
    }
    
    public void addFeature(FeatureGroup fg)
    {
        
    }  
}