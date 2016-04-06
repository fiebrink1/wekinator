/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.util.logging.Level;
import java.util.logging.Logger;
import wekimini.kadenze.NotedCriterion.Outcome;

/**
 *
 * @author rebecca
 */
public class Assignment11Grade {

    public Assignment11Grade() {
    }

    private final NotedCriterion validSubmission = new NotedCriterion("valid_submission");
    private final NotedCriterion part1setup = new NotedCriterion("part1_setup");
    private final NotedCriterion part1demonstrated = new NotedCriterion("part1_demonstrated");
    private final NotedCriterion part1has4gestures = new NotedCriterion("part1_has4gestures");
    private final NotedCriterion part2Experimented = new NotedCriterion("part2_experimentation");
    private final NotedCriterion part2_Q1 = new NotedCriterion("part2_q1");
    private final NotedCriterion part2_Q2 = new NotedCriterion("part2_q2");
    private final NotedCriterion part2_Q3 = new NotedCriterion("part2_q3");
    private final NotedCriterion part2_Q4 = new NotedCriterion("part2_q4");
    private final NotedCriterion part2_Q5 = new NotedCriterion("part2_q5");
    private final NotedCriterion part2_Q6 = new NotedCriterion("part2_q6");

    private static final Logger logger = Logger.getLogger(Assignment11Grade.class.getName());

    private Outcome setQuestionOutcomeFromScore(double q1) {
        if (q1 < 0.0001) {
            return Outcome.FAILURE;
        } else if (q1 > 0.999) {
            return Outcome.SUCCESS;
        } else {
            return Outcome.COULD_BE_BETTER;
        }
    }

    public static enum Assignment11Part {

        PART1, PART2
    };

    //TODO
    void scorePartFail(Assignment11Part assignmentPart, Outcome outcome) {
        switch (assignmentPart) {
            case PART1:
                score1setup(outcome, 0.0);
                score1experimented(outcome, 0.0);
                score1has4gestures(outcome, 0.0);
                break;
            case PART2:
                score2Experimented(outcome, 0.0);
                //Don't fail questions; these may be answered elsewhere
                break;
            default:
                logger.log(Level.SEVERE, "Invalid score part: " + assignmentPart);
        }
    }

    public void validSubmission(Outcome o, double score) {
        validSubmission.setScore(score);
        validSubmission.setNoteWithErrValue(o, "assignment11");
    }

    public void score1setup(Outcome o, double score) {
        part1setup.setScore(score);
        part1setup.setNoteWithErrValue(o, "assignment11_1");
    }

    public void score1experimented(Outcome o, double score) {
        part1demonstrated.setScore(score);
        part1demonstrated.setNoteWithErrValue(o, "assignment11_1");
    }

    void score1experimented(Outcome o, double score, int totalGesturesSeen) {
        part1demonstrated.setScore(score);
        part1demonstrated.setNoteWithErrValue(o, "assignment11_1");
        String[] vals = {Integer.toString(totalGesturesSeen)};
        part1demonstrated.addVals(vals);
    }

    public void score1has4gestures(Outcome o, double score) {
        part1has4gestures.setScore(score);
        part1has4gestures.setNoteWithErrValue(o, "assignment11_1");
    }

    public void score2Experimented(Outcome o, double score) {
        part2Experimented.setScore(score);
        part2Experimented.setNoteWithErrValue(o, "assignment11_2");
    }

    public void score2Experimented(Outcome o, double score, int numRuns) {
        part2Experimented.setScore(score);
        part2Experimented.setNoteWithErrValue(o, "assignment11_2");
        String[] vals = {Integer.toString(numRuns)};
        part2Experimented.addVals(vals);
    }

    public void score2Questions(double q1, double q2, double q3,
            double q4, double q5, double q6) {
        part2_Q1.setScore(q1);
        Outcome o = setQuestionOutcomeFromScore(q1);
        part2_Q1.setNoteWithErrValue(o, "assignment11_2");

        part2_Q2.setScore(q2);
        o = setQuestionOutcomeFromScore(q2);

        part2_Q2.setNoteWithErrValue(o, "assignment11_2");

        part2_Q3.setScore(q3);
        o = setQuestionOutcomeFromScore(q3);
        part2_Q3.setNoteWithErrValue(o, "assignment11_2");

        part2_Q4.setScore(q4);
        o = setQuestionOutcomeFromScore(q4);
        part2_Q4.setNoteWithErrValue(o, "assignment11_2");

        part2_Q5.setScore(q5);
        o = setQuestionOutcomeFromScore(q5);
        part2_Q5.setNoteWithErrValue(o, "assignment11_2");

        part2_Q6.setScore(q6);
        o = setQuestionOutcomeFromScore(q6);
        part2_Q6.setNoteWithErrValue(o, "assignment11_2");
    }

    public void setCompletelyInvalidSubmission(Outcome o) {
        validSubmission(o, 0.0);
        score1setup(o, 0.0);
        score1experimented(o, 0.);
        score1has4gestures(o, 0.);
        score2Experimented(o, 0.0);
        score2Questions(0, 0, 0, 0, 0, 0);
    }

    public Grade getGrade() {
        Grade g = new Grade();
        g.addNotedCriterion(validSubmission);
        g.addNotedCriterion(part1setup);
        g.addNotedCriterion(part1demonstrated);
        g.addNotedCriterion(part1has4gestures);
        g.addNotedCriterion(part2Experimented);
        g.addNotedCriterion(part2_Q1);
        g.addNotedCriterion(part2_Q2);
        g.addNotedCriterion(part2_Q3);
        g.addNotedCriterion(part2_Q4);
        g.addNotedCriterion(part2_Q5);
        g.addNotedCriterion(part2_Q6);
        return g;
    }

}
