/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.osc;

import wekimini.LearningModelBuilder;

/**
 *
 * @author rebecca
 */
public interface OSCSupervisedLearningOutput extends OSCOutput {
    public LearningModelBuilder getDefaultModelBuilder();
}
