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
public class Assignment7Grade {

    public Assignment7Grade() {
    }

    private final NotedCriterion validSubmission = new NotedCriterion("valid_submission");
    private final NotedCriterion part1aCorrectSetup = new NotedCriterion("part1a_setup");
    private final NotedCriterion part1bKnnSetup = new NotedCriterion("part1b_knn_setup");
    private final NotedCriterion part1bKnnAccuracy = new NotedCriterion("part1b_knn_accuracy");
    private final NotedCriterion part1bAdaboostSetup = new NotedCriterion("part1b_adaboost_setup");
    private final NotedCriterion part1bAdaboostAccuracy = new NotedCriterion("part1b_adaboost_accuracy");
    private final NotedCriterion part1bDTreeSetup = new NotedCriterion("part1b_decisiontree_setup");
    private final NotedCriterion part1bDTreeAccuracy = new NotedCriterion("part1b_decisiontree_accuracy");
    private final NotedCriterion part1bSVMSetup = new NotedCriterion("part1b_svm_setup");
    private final NotedCriterion part1bSVMAccuracy = new NotedCriterion("part1b_svm_accuracy");

    private final NotedCriterion part1cKnnSetup = new NotedCriterion("part1c_knn_setup");
    private final NotedCriterion part1cKnnAccuracy = new NotedCriterion("part1c_knn_accuracy");
    private final NotedCriterion part1cAdaboostSetup = new NotedCriterion("part1c_adaboost_setup");
    private final NotedCriterion part1cAdaboostAccuracy = new NotedCriterion("part1c_adaboost_accuracy");
    private final NotedCriterion part1cDTreeSetup = new NotedCriterion("part1c_decisiontree_setup");
    private final NotedCriterion part1cDTreeAccuracy = new NotedCriterion("part1c_decisiontree_accuracy");
    private final NotedCriterion part1cSVMSetup = new NotedCriterion("part1c_svm_setup");
    private final NotedCriterion part1cSVMAccuracy = new NotedCriterion("part1c_svm_accuracy");

    private final NotedCriterion part2Experimented = new NotedCriterion("part2_experimentation");
    private final NotedCriterion part2_Q1 = new NotedCriterion("part2_q1");
    private final NotedCriterion part2_Q2 = new NotedCriterion("part2_q2");
    private final NotedCriterion part2_Q3 = new NotedCriterion("part2_q3");
    private final NotedCriterion part2_Q4 = new NotedCriterion("part2_q4");
    private final NotedCriterion part2_Q5 = new NotedCriterion("part2_q5");
    private final NotedCriterion part2_Q6 = new NotedCriterion("part2_q6");
    private final NotedCriterion part2_Q7 = new NotedCriterion("part2_q7");
    private final NotedCriterion part2_Q8 = new NotedCriterion("part2_q8");

    private static final Logger logger = Logger.getLogger(Assignment7Grade.class.getName());

    public static enum Assignment5Part {
        PART1A, PART1B, PART1C, PART2
    };

    //TODO
    void scorePartFail(Assignment5Part assignmentPart, Outcome outcome) {
        switch (assignmentPart) {
            case PART1A:
                score1aSetup(outcome, 0.0);
                //TODO : Fail all parts
            default:
                logger.log(Level.SEVERE, "Invalid score part: " + assignmentPart);
        }
    }

    public void validSubmission(Outcome o, double score) {
        validSubmission.setScore(score);
        validSubmission.setNoteWithErrValue(o, "assignment7");
    }

    public void score1aSetup(Outcome o, double score) {
        part1aCorrectSetup.setScore(score);
        part1aCorrectSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score2Experimented(Outcome o, double score) {
        part2Experimented.setScore(score);
        part2Experimented.setNoteWithErrValue(o, "assignment5_2");
    }

    

    public void setCompletelyInvalidSubmission(Outcome o) {
        validSubmission(o, 0.0);
        score1aSetup(Outcome.LOG_READ_ERROR, 0.0);
        //TODO;
    }

    public Grade getGrade() {
        Grade g = new Grade();
        g.addNotedCriterion(validSubmission);
        g.addNotedCriterion(part1aCorrectSetup);
        g.addNotedCriterion(part1bAdaboostAccuracy);
        g.addNotedCriterion(part1bAdaboostSetup);
        g.addNotedCriterion(part1bKnnAccuracy);
        g.addNotedCriterion(part1bKnnSetup);
        g.addNotedCriterion(part1bDTreeAccuracy);
        g.addNotedCriterion(part1bDTreeSetup);
        g.addNotedCriterion(part1bSVMAccuracy);
        g.addNotedCriterion(part1bSVMSetup);
        
        g.addNotedCriterion(part1cAdaboostAccuracy);
        g.addNotedCriterion(part1cAdaboostSetup);
        g.addNotedCriterion(part1cKnnAccuracy);
        g.addNotedCriterion(part1cKnnSetup);
        g.addNotedCriterion(part1cDTreeAccuracy);
        g.addNotedCriterion(part1cDTreeSetup);
        g.addNotedCriterion(part1cSVMAccuracy);
        g.addNotedCriterion(part1cSVMSetup);
        
        g.addNotedCriterion(part2Experimented);
        g.addNotedCriterion(part2_Q1);
        g.addNotedCriterion(part2_Q2);
        g.addNotedCriterion(part2_Q3);
        g.addNotedCriterion(part2_Q4);
        g.addNotedCriterion(part2_Q5);
        g.addNotedCriterion(part2_Q6);
        g.addNotedCriterion(part2_Q7);
        g.addNotedCriterion(part2_Q8);
        return g;
    }

}
