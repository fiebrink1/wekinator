/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author rebecca
 */
public class ModelLoader {
    
    public static Model loadModel(Class c, ObjectInputStream is) throws IOException, ClassNotFoundException {
        if (c == SimpleModel.class) {
            return SimpleModel.readFromInputStream(is);
        } else if (c == NeuralNetworKModel.class) {
            return NeuralNetworKModel.readFromInputStream(is);  
        } else {
            return null;
        }
    }
    
    
}
