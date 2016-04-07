/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.util.logging.Logger;
import wekimini.kadenze.NotedCriterion.Outcome;

/**
 *
 * @author rebecca
 */
public class Assignment12Grade {

    public Assignment12Grade() {
    }

    private final NotedCriterion validSubmission = new NotedCriterion("valid_submission");
    private final NotedCriterion experimented = new NotedCriterion("experimented");
    private final NotedCriterion my_q1 = new NotedCriterion("q1");
    private final NotedCriterion my_q2 = new NotedCriterion("q2");
    private final NotedCriterion my_q3 = new NotedCriterion("q3");
    private final NotedCriterion my_q4 = new NotedCriterion("q4");
    private final NotedCriterion my_q5 = new NotedCriterion("q5");
    private final NotedCriterion my_q6 = new NotedCriterion("q6");
    private final NotedCriterion my_q7 = new NotedCriterion("q7");
    private final NotedCriterion my_q8 = new NotedCriterion("q8");

    private static final Logger logger = Logger.getLogger(Assignment12Grade.class.getName());

    private Outcome setQuestionOutcomeFromScore(double q1) {
        if (q1 < 0.0001) {
            return Outcome.FAILURE;
        } else if (q1 > 0.999) {
            return Outcome.SUCCESS;
        } else {
            return Outcome.COULD_BE_BETTER;
        }
    }

    public void validSubmission(Outcome o, double score) {
        validSubmission.setScore(score);
        validSubmission.setNoteWithErrValue(o, "assignment12");
    }

    public void scoreExperimented(Outcome o, double score) {
        experimented.setScore(score);
        experimented.setNoteWithErrValue(o, "assignment12");
    }

    public void scoreExperimented(Outcome o, double score, int numRuns, double numMinutes) {
        experimented.setScore(score);
        experimented.setNoteWithErrValue(o, "assignment12");
        String[] vals = {Integer.toString(numRuns), KadenzeUtils.formatDouble(numMinutes)};
        experimented.addVals(vals);
    }
    
    public void scoreQuestions(double[] qs) {
        my_q1.setScore(qs[0]);
        Outcome o = setQuestionOutcomeFromScore(qs[0]);
        my_q1.setNoteWithErrValue(o, "assignment12");

        my_q2.setScore(qs[1]);
        o = setQuestionOutcomeFromScore(qs[2]);
        my_q2.setNoteWithErrValue(o, "assignment12");

        my_q3.setScore(qs[2]);
        o = setQuestionOutcomeFromScore(qs[2]);
        my_q3.setNoteWithErrValue(o, "assignment12");

        my_q4.setScore(qs[2]);
        o = setQuestionOutcomeFromScore(qs[3]);
        my_q4.setNoteWithErrValue(o, "assignment12");

        my_q5.setScore(qs[4]);
        o = setQuestionOutcomeFromScore(qs[4]);
        my_q5.setNoteWithErrValue(o, "assignment12");

        my_q6.setScore(qs[5]);
        o = setQuestionOutcomeFromScore(qs[5]);
        my_q6.setNoteWithErrValue(o, "assignment12");
        
        my_q7.setScore(qs[6]);
        o = setQuestionOutcomeFromScore(qs[6]);
        my_q7.setNoteWithErrValue(o, "assignment12");
        
        my_q8.setScore(qs[7]);
        o = setQuestionOutcomeFromScore(qs[7]);
        my_q8.setNoteWithErrValue(o, "assignment12");
    }

    public void setCompletelyInvalidSubmission(Outcome o) {
        validSubmission(o, 0.0);
        scoreExperimented(o, 0.);
        double[] qs = {0., 0., 0., 0., 0., 0., 0., 0.};
        scoreQuestions(qs);
    }

    public Grade getGrade() {
        Grade g = new Grade();
        g.addNotedCriterion(validSubmission);
        g.addNotedCriterion(experimented);
        g.addNotedCriterion(my_q1);
        g.addNotedCriterion(my_q2);
        g.addNotedCriterion(my_q3);
        g.addNotedCriterion(my_q4);
        g.addNotedCriterion(my_q5);
        g.addNotedCriterion(my_q6);
        g.addNotedCriterion(my_q7);
        g.addNotedCriterion(my_q8);
        return g;
    }
}
