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
public class TestExampleRegression {

    private final double input;
    private final double trueOutput;

    public TestExampleRegression(double in, double o) {
        this.input = in;
        this.trueOutput = o;
    }

    public double getTrueOutput() {
        return trueOutput;
    }
    
    public double getInput() {
        return input;
    }
}
