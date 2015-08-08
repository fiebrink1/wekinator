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
public interface OSCSupervisedLearningOutput extends OSCOutput {
    public ModelBuilder getDefaultModelBuilder();
}
