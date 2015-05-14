/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import weka.core.Instance;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public interface Model {
    
    public String getPrettyName();
    
    public double computeOutput(Instance inputs) throws Exception;
    
    public String getUniqueIdentifier();
    
    public boolean isCompatible(OSCOutput o);
    
    
}
