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

    private static final Logger logger = Logger.getLogger(Assignment7Grade.class.getName());

    private Outcome setQuestionOutcomeFromScore(double q1) {
        if (q1 < 0.0001) {
            return Outcome.FAILURE;
        } else if (q1 > 0.999) {
            return Outcome.SUCCESS;
        } else {
            return Outcome.COULD_BE_BETTER;
        }
    }

    public static enum Assignment7Part {

        PART1A, PART1B, PART1C, PART2
    };

    //TODO
    void scorePartFail(Assignment7Part assignmentPart, Outcome outcome) {
        switch (assignmentPart) {
            case PART1A:
                score1aSetup(outcome, 0.0);
                break;
            case PART1B:
                score1bKnnSetup(outcome, 0.0);
                score1bAdaBoostSetup(outcome, 0.0);
                score1bDTreeSetup(outcome, 0.0);
                score1bSVMSetup(outcome, 0.0);
                score1bKnnAccuracy(outcome, 0.0);
                score1bAdaBoostAccuracy(outcome, 0.0);
                score1bDTreeAccuracy(outcome, 0.0);
                score1bSVMAccuracy(outcome, 0.0);
                break;
            case PART1C:
                score1cKnnSetup(outcome, 0.0);
                score1cAdaBoostSetup(outcome, 0.0);
                score1cDTreeSetup(outcome, 0.0);
                score1cSVMSetup(outcome, 0.0);
                score1cKnnAccuracy(outcome, 0.0);
                score1cAdaBoostAccuracy(outcome, 0.0);
                score1cDTreeAccuracy(outcome, 0.0);
                score1cSVMAccuracy(outcome, 0.0);
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
        validSubmission.setNoteWithErrValue(o, "assignment7");
    }

    public void score1aSetup(Outcome o, double score) {
        part1aCorrectSetup.setScore(score);
        part1aCorrectSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1bKnnSetup(Outcome o, double score) {
        part1bKnnSetup.setScore(score);
        part1bKnnSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1bAdaBoostSetup(Outcome o, double score) {
        part1bAdaboostSetup.setScore(score);
        part1bAdaboostSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1bDTreeSetup(Outcome o, double score) {
        part1bDTreeSetup.setScore(score);
        part1bDTreeSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1bSVMSetup(Outcome o, double score) {
        part1bSVMSetup.setScore(score);
        part1bSVMSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1bKnnAccuracy(Outcome o, double score) {
        part1bKnnAccuracy.setScore(score);
        part1bKnnAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1bKnnAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }
    }

    public void score1bAdaBoostAccuracy(Outcome o, double score) {
        part1bAdaboostAccuracy.setScore(score);
        part1bAdaboostAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1bAdaboostAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }

    }

    public void score1bDTreeAccuracy(Outcome o, double score) {
        part1bDTreeAccuracy.setScore(score);
        part1bDTreeAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1bDTreeAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }
    }

    public void score1bSVMAccuracy(Outcome o, double score) {
        part1bSVMAccuracy.setScore(score);
        part1bSVMAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1bSVMAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }
    }

    public void score1cKnnSetup(Outcome o, double score) {
        part1cKnnSetup.setScore(score);
        part1cKnnSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1cAdaBoostSetup(Outcome o, double score) {
        part1cAdaboostSetup.setScore(score);
        part1cAdaboostSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1cDTreeSetup(Outcome o, double score) {
        part1cDTreeSetup.setScore(score);
        part1cDTreeSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1cSVMSetup(Outcome o, double score) {
        part1cSVMSetup.setScore(score);
        part1cSVMSetup.setNoteWithErrValue(o, "assignment7_1A");
    }

    public void score1cKnnAccuracy(Outcome o, double score) {
        part1cKnnAccuracy.setScore(score);
        part1cKnnAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1cKnnAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }
    }

    public void score1cAdaBoostAccuracy(Outcome o, double score) {
        part1cAdaboostAccuracy.setScore(score);
        part1cAdaboostAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1cAdaboostAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }
    }

    public void score1cDTreeAccuracy(Outcome o, double score) {
        part1cDTreeAccuracy.setScore(score);
        part1cDTreeAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1cDTreeAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }
    }

    public void score1cSVMAccuracy(Outcome o, double score) {
        part1cSVMAccuracy.setScore(score);
        part1cSVMAccuracy.setNoteWithErrValue(o, "assignment7_1A");
        if (o == Outcome.SUCCESS) {
            part1cSVMAccuracy.setValue("val", KadenzeUtils.formatDouble(100.*score));
        }
    }

    public void score2Experimented(Outcome o, double score) {
        part2Experimented.setScore(score);
        part2Experimented.setNoteWithErrValue(o, "assignment7_2");
    }

    public void score2AddValues(int numTrain, int numRun, double numMinutes) {
        String[] vals = {Integer.toString(numTrain), Integer.toString(numRun), KadenzeUtils.formatDouble(numMinutes)};
        part2Experimented.addVals(vals);
    }

    public void score2Questions(double q1, double q2, double q3,
            double q4, double q5, double q6, double q7) {
        part2_Q1.setScore(q1);
        Outcome o = setQuestionOutcomeFromScore(q1);
        part2_Q1.setNoteWithErrValue(o, "assignment7_2");

        part2_Q2.setScore(q2);
        o = setQuestionOutcomeFromScore(q2);

        part2_Q2.setNoteWithErrValue(o, "assignment7_2");

        part2_Q3.setScore(q3);
        o = setQuestionOutcomeFromScore(q3);
        part2_Q3.setNoteWithErrValue(o, "assignment7_2");

        part2_Q4.setScore(q4);
        o = setQuestionOutcomeFromScore(q4);
        part2_Q4.setNoteWithErrValue(o, "assignment7_2");

        part2_Q5.setScore(q5);
        o = setQuestionOutcomeFromScore(q5);
        part2_Q5.setNoteWithErrValue(o, "assignment7_2");

        part2_Q6.setScore(q6);
        o = setQuestionOutcomeFromScore(q6);
        part2_Q6.setNoteWithErrValue(o, "assignment7_2");

        part2_Q7.setScore(q7);
        o = setQuestionOutcomeFromScore(q7);
        part2_Q7.setNoteWithErrValue(o, "assignment7_2");
    }

    public void setCompletelyInvalidSubmission(Outcome o) {
        validSubmission(o, 0.0);
        score1aSetup(o, 0.0);
        score1bKnnSetup(o, 0.0);
        score1bAdaBoostSetup(o, 0.0);
        score1bDTreeSetup(o, 0.0);
        score1bSVMSetup(o, 0.0);
        score1bKnnAccuracy(o, 0.0);
        score1bAdaBoostAccuracy(o, 0.0);
        score1bDTreeAccuracy(o, 0.0);
        score1bSVMAccuracy(o, 0.0);
        score1cKnnSetup(o, 0.0);
        score1cAdaBoostSetup(o, 0.0);
        score1cDTreeSetup(o, 0.0);
        score1cSVMSetup(o, 0.0);
        score1cKnnAccuracy(o, 0.0);
        score1cAdaBoostAccuracy(o, 0.0);
        score1cDTreeAccuracy(o, 0.0);
        score1cSVMAccuracy(o, 0.0);
        score2Experimented(o, 0.0);
        score2Questions(0, 0, 0, 0, 0, 0, 0);
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
        return g;
    }

}
