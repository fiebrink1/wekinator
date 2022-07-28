/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class NotedCriterion {
    private final Criterion c;
    private Note n = null;
    private static final Logger logger = Logger.getLogger(NotedCriterion.class.getName());
    
    public enum Outcome {
        SUCCESS,
        FAILURE,
        COULD_BE_BETTER,
        LOG_MISSING,
        LOG_READ_ERROR,
        LOG_FORMAT_ERROR,
        BAD_ZIP
    };

    public static String prefix = "grad.txt.goldsmiths_ml.criteria";


    public static String getSuffix(Outcome o) {
        switch (o) {
            case SUCCESS:
                return "success";
            case FAILURE:
                return "failure";
            case COULD_BE_BETTER:
                return "could_be_better";
            case LOG_MISSING:
                return "log_missing";
            case LOG_READ_ERROR:
                return "log_read_error";
            case LOG_FORMAT_ERROR:
                return "log_format_error";
            case BAD_ZIP:
                return "bad_zip";
            default:
                return "other";
        }
    }

    
    
    public NotedCriterion(String name) {
        c = new Criterion(name);
    }
    
    /*public void addNote(Note n) {
        this.n = n;
    } */
    
    public void setScore(double d) {
        c.setScore(d);
    }
    
    public Criterion getCriterion() {
        return c;
    }
    
    public Note getNote() {
        return n;
    }
    
    public void setNote(Outcome o) {
        n = new Note(c.getName() + "." + getSuffix(o));
    }

    public void setNote(Outcome o, String value) {
       n = new Note(c.getName() + "." + getSuffix(o));
       n.addValue("val", value);
    }
    
    //Adds in order as val1, val2, val3, ...
    void addVals(String[] vals) {
        if (n == null) {
            logger.log(Level.WARNING, "note is null");
            return;

        }
        for (int i = 0; i < vals.length; i++) {
            n.addValue("val" + (i+1), vals[i]);
        }
    }
    
    void setValue(String key, String val) {
        if (n == null) {
            logger.log(Level.WARNING, "note is null");
            return;
        }
        n.addValue(key, val);
    }

    //If the outcome is an error, insert value into the error string
    //e.g., value = "assignment 2 part a" : error can be "Error with {assignment} log"
    public void setNoteWithErrValue(Outcome o, String value) {
        n = new Note(c.getName() + "." + getSuffix(o));
        if (o == Outcome.LOG_FORMAT_ERROR || o == Outcome.LOG_READ_ERROR || o == Outcome.LOG_MISSING) {
            n.addValue("val", value);
        }
    }
    
}
