/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.osc;

import wekimini.learning.ModelBuilder;

/**
 *
 * @author rebecca
 */
public interface OSCOutput {
   // public static enum OutputType {CLASSIFICATION, REGRESSION};
   // public static enum RegressionType {INTEGER, FLOAT};
    
    public String getName();

    public double generateRandomValue();

    public double getDefaultValue();
        
    public boolean isLegalTrainingValue(double value);
    
    public boolean isLegalOutputValue(double value);
    
    public double forceLegalTrainingValue(double value);
    
    public double forceLegalOutputValue(double value);
    
    public String toLogString();

}
