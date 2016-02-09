/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONStringer;

/**
 *
 * @author rebecca
 */
public class Grade {

    private final List<Criterion> criteria;
    private final List<Note> notes;

    public Grade() {
        this.criteria = new LinkedList<>();
        this.notes = new LinkedList<>();
    }

    public void addCriterionAndNote(Criterion c, Note n) {
        criteria.add(c);
        notes.add(n);
    }
    
    public void addNotedCriterion(NotedCriterion nc) {
        criteria.add(nc.getCriterion());
        notes.add(nc.getNote());
    }

    public String toJSONString() {
        JSONStringer s = new JSONStringer();
        s.object();
        s.key("grade_results");
        s.array();
        for (Criterion c : criteria) {
            c.appendToJSonStringer(s);
        }
        s.endArray();

        s.key("grader_notes");
        s.array();
        for (Note n : notes) {
            n.appendToJSonStringer(s);
        }
        s.endArray();
        s.endObject();
        return s.toString();
    }
    
    public String toHumanString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < criteria.size(); i++) {
            Criterion c = criteria.get(i);
            Note n = notes.get(i);
            sb.append(c.toHumanString()).append('\n');
            if (n != null) {
                sb.append("NOTE: ").append(n.toHumanString()).append('\n');
            }
        }
        return sb.toString();       
    }

    //For testing
    public static void main(String[] args) {
        Grade g = new Grade();
        Criterion c = new Criterion("valid_submission");
        c.setScore(1.0);
        Note n = new Note("grad.txt.goldsmiths_ml.criteria.valid_submission.reporting.success");
        n.addValue("assignment", "Assignment 1");
                
        Criterion c2 = new Criterion("supervised_training");
        c2.setScore(1.0);
        Note n2 = new Note("grad.txt.goldsmiths_ml.criteria.supervised_training.reporting.success");
        
        
        Criterion c3 = new Criterion("experimentation_time");
        c3.setScore(0.3588);
        Note n3 = new Note("grad.txt.goldsmiths_ml.criteria.experimentation_time.reporting.spend_more_time");
        
        g.addCriterionAndNote(c, n);
        g.addCriterionAndNote(c2,n2);
        g.addCriterionAndNote(c3, n3);
        
        System.out.println(g.toJSONString());
    }
}
