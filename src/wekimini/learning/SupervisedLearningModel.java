/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.learning;

import java.io.IOException;
import java.io.ObjectOutputStream;
import weka.core.Instance;
import wekimini.osc.OSCOutput;

/**
 *
 * @author rebecca
 */
public interface SupervisedLearningModel extends Model {
    public double computeOutput(Instance inputs) throws Exception;
    
  }
