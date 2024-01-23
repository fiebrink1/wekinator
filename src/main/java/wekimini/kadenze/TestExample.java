/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

/**
 *
 * @author rebecca
 */
public class TestExample {

    private final double[] inputs;
    private final double trueOutput;

    public TestExample(double[] is, double o) {
        this.inputs = is;
        this.trueOutput = o;
    }

    public double getTrueOutput() {
        return trueOutput;
    }
    
    public double[] getInputs() {
        return inputs;
    }
}
