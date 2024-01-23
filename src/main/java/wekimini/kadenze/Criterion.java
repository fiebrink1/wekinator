/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import org.json.JSONStringer;

/**
 *
 * @author rebecca
 */
public class Criterion {
    //private final int id;
    private final String name;
    private double score = 0.0;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Criterion(String name) {
        this.name = name;
    }

  /*  public int getId() {
        return id;
    } */

    public String getName() {
        return name;
    }
    
    
    
    //Uses http://www.json.org/javadoc/org/json/JSONStringer.html
    public void appendToJSonStringer(JSONStringer s) {
        s.object();
       // s.key("grading_criterion_id");
       // s.value(id);
        s.key("feature_id");
        s.value(name);
        s.key("score");
        s.value(score);
        s.endObject();
    }
    
    public String toHumanString() {
        return "Name: " + name + ", Score: " + score;
    }
    
    public static void main(String[] args) {
        Criterion c = new Criterion("valid_submission");
        c.setScore(0.3);
        JSONStringer s = new JSONStringer();
        c.appendToJSonStringer(s);
        System.out.println(s.toString());
    }
}
