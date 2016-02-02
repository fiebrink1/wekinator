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
public class NotedCriterion {
    private final Criterion c;
    private Note n = null;
    
    public enum Outcome {

        SUCCESS,
        FAILURE,
        COULD_BE_BETTER,
        LOG_MISSING,
        LOG_ERROR,
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
            case LOG_ERROR:
                return "log_error";
            case BAD_ZIP:
                return "bad_zip";
            default:
                return "other";
        }
    }

    
    
    public NotedCriterion(int id, String name) {
        c = new Criterion(id, name);
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
        n = new Note(prefix + "." + c.getName() + ".reporting." + getSuffix(o));
    }

    public void setNote(Outcome o, String value) {
       n = new Note(prefix + "." + c.getName() + ".reporting." + getSuffix(o));
       n.addValue("val", value);
    }

    public void setNoteWithErrValue(Outcome o, String value) {
        n = new Note(prefix + "." + c.getName() + ".reporting." + getSuffix(o));
        if (o == Outcome.LOG_ERROR || o == Outcome.LOG_MISSING) {
            n.addValue("val", value);
        }
    }
    
}
