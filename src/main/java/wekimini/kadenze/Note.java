/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.util.HashMap;
import org.json.*;

/**
 *
 * @author rebecca
 */
public class Note {
    private final String description;
    private final HashMap<String,String> values;

    public Note(String description) {
        this.description = description;
        this.values = new HashMap<>();
    }
    
    public Note (String description, String valKey, String valval) {
        this.description = description;
        this.values = new HashMap<>();
        values.put(valKey, valval);
    }
    
    public String toHumanString() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);
        if (values.size() > 0) {
            sb.append(":");
        }
        for (String val : values.keySet()) {
            sb.append("\n\t").append(val).append(": ").append(values.get(val));
        }
        return sb.toString();
    }
    
    public void addValue(String name, String value) {
        values.put(name, value);
    }
    
    //Uses http://www.json.org/javadoc/org/json/JSONStringer.html
    public void appendToJSonStringer(JSONStringer s) {
        s.object();
        //            System.out.println("About to output " + description );

        s.key("description");
        s.value(description);
        if (values.isEmpty()) {
            s.endObject();
            return;
        }
        s.key("values");
        if (values.size() != 0) {
             s.object();
        }
        for (String valueKey : values.keySet()) {
           // System.out.println("About to output " description + " key " + valueKey);
           
            s.key(valueKey);
            s.value(values.get(valueKey));
            
        }
        if (values.size() != 0) {
            s.endObject();
        }
        s.endObject();
    }
    
    public static void main(String[] args) {
        Note n = new Note("My note 1");
        n.addValue("assignment", "assignment1");
        JSONStringer s= new JSONStringer();
        n.appendToJSonStringer(s);
        System.out.println(s.toString());
    }
}

