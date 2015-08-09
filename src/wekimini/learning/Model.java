/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import weka.core.Instance;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public interface Model {
    
    public String getPrettyName();
    
    public String getModelDescription();
    
    public String getUniqueIdentifier();
    
    public boolean isCompatible(OSCOutput o);

    public void writeToOutputStream(ObjectOutputStream os) throws IOException;
    
    //TODO: FIgure out how to make this static in helper
    //public Model readFromInputStream(ObjectInputStream is) throws IOException, ClassNotFoundException;
}
