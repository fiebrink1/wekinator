/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.Date;

/**
 *
 * @author rebecca
 */
public class SimpleModel implements Model {
    
    private final String prettyName;
    private final String timestamp;
    private final String myId;
    
    public SimpleModel(String name) { 
        this.prettyName = name;
        Date d= new Date();
        timestamp = Long.toString(d.getTime());
        myId = this.prettyName + "_" + timestamp;
    }
    
    @Override
    public double computeOutput(double[] inputs) {
        return 1.0f;
    }
    
    @Override
    public String getUniqueIdentifier() {
        return myId;
    }
    
   /*
    public static void main(String[] args) {
        SimpleModel m = new SimpleModel("hi");
        System.out.println(m.getUniqueIdentifier());
    } */

    @Override
    public String getPrettyName() {
        return prettyName;
    }

   
    
}
