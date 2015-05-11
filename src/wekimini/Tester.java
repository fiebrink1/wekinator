/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

/**
 *
 * @author rebecca
 */
public class Tester {
    double[] numbers;
    public Tester() {
        numbers = new double[5];
        numbers[0] = 10;
    }
    public String toString() {
        return Double.toString(numbers[0]);
    }
    public double[] vals() {
        return numbers;
    }
    
    public static void main(String[] args) {
        Tester t = new Tester();
        System.out.println("First t is " + t);
        double[] v = t.vals();
        v[0] = 2000;
        System.out.println("Then t is " + t);

    }
}
