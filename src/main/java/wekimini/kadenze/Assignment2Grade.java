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
public class Assignment2Grade {

    public Assignment2Grade() {
    }

    private final NotedCriterion validSubmission = new NotedCriterion("valid_submission");
    private final NotedCriterion part1aCorrectSetup = new NotedCriterion("part1a_setup");
    private final NotedCriterion part1aExperimented = new NotedCriterion("part1a_experimented");
    private final NotedCriterion part1bUsedCorrectClassifier = new NotedCriterion("part1b_used_correct_classifier");
    private final NotedCriterion part1bQuality = new NotedCriterion("part1b_classifier_quality");
    private final NotedCriterion part1cUsedCorrectClassifier = new NotedCriterion("part1c_used_correct_classifier");
    private final NotedCriterion part1cQuality = new NotedCriterion("part1c_classifier_quality");
    private final NotedCriterion part1dUsedCorrectClassifier = new NotedCriterion("part1d_used_correct_classifier");
    private final NotedCriterion part1dQuality = new NotedCriterion("part1d_classifier_quality");
    private final NotedCriterion part2Experimented = new NotedCriterion("part2_experimented");
    private final NotedCriterion part3aBuiltClassifier = new NotedCriterion("part3a_built_classifier");
    private final NotedCriterion part3bClassifierAccuracy = new NotedCriterion("part3b_classifier_accuracy");
    private static final Logger logger = Logger.getLogger(Assignment2Grade.class.getName());

    public static enum Assignment2Part {

        PART1A, PART1B, PART1C, PART1D, PART2, PART3A, PART3B
    };

    void scorePartFail(Assignment2Part assignment2Part, Outcome outcome) {
        switch (assignment2Part) {
            case PART1A:
                score1aSetup(outcome, 0.0);
                score1aExperimented(outcome, 0.0);
                break;
            case PART1B:
                score1bClassifierQuality(outcome, 0.0);
                score1bClassifierType(outcome, 0.0);
                break;
            case PART1C:
                score1cClassifierQuality(outcome, 0.0);
                score1cClassifierType(outcome, 0.0);
                break;
            case PART1D:
                score1dClassifierQuality(outcome, 0.0);
                score1dClassifierType(outcome, 0.0);
                break;
            case PART2:
                score2Experimented(outcome, 0.0);
                break;
            case PART3A:
                score3ABuiltClassifier(outcome, 0.0);
                break;
            case PART3B:
                score3bClassifierQuality(outcome, 0.0);
                break;
            default:
                logger.log(Level.SEVERE, "Invalid score part: " + assignment2Part);
        }
    }

    public void validSubmission(Outcome o, double score) {
        validSubmission.setScore(score);
        validSubmission.setNoteWithErrValue(o, "assignment2");
    }

    public void score1aSetup(Outcome o, double score) {
        part1aCorrectSetup.setScore(score);
        part1aCorrectSetup.setNoteWithErrValue(o, "assignment2_1A");
    }

    public void score1aExperimented(Outcome o, double score) {
        part1aExperimented.setScore(score);
        part1aExperimented.setNoteWithErrValue(o, "assignment2_1A");
    }

    public void score1aExperimented_addValues(int numClassifiers, int numTrain, int numRun) {
        String[] vals = {Integer.toString(numClassifiers), Integer.toString(numTrain), Integer.toString(numRun)};
        part1aExperimented.addVals(vals);
    }

    void score2Experimented_addValues(int numTrain, int numRun) {
        String[] vals = {Integer.toString(numTrain), Integer.toString(numRun)};
        part2Experimented.addVals(vals);
    }

    public void score1bClassifierType(Outcome o, double score) {
        part1bUsedCorrectClassifier.setScore(score);
        part1bUsedCorrectClassifier.setNoteWithErrValue(o, "assignment2_1B");
    }

    public void score1bClassifierQuality(Outcome o, double score) {
        part1bQuality.setScore(score);
        part1bQuality.setNoteWithErrValue(o, "assignment2_1B");
        if (o == Outcome.SUCCESS) {
            part1bQuality.setValue("val", KadenzeUtils.formatDouble(score*100));
        }
    }

    public void score1cClassifierType(Outcome o, double score) {
        part1cUsedCorrectClassifier.setScore(score);
        part1cUsedCorrectClassifier.setNoteWithErrValue(o, "assignment2_1C");
    }

    public void score1cClassifierQuality(Outcome o, double score) {
        part1cQuality.setScore(score);
        part1cQuality.setNoteWithErrValue(o, "assignment2_1C");
        if (o == Outcome.SUCCESS) {
            part1cQuality.setValue("val", KadenzeUtils.formatDouble(score*100));
        }
    }

    public void score1dClassifierType(Outcome o, double score) {
        part1dUsedCorrectClassifier.setScore(score);
        part1dUsedCorrectClassifier.setNoteWithErrValue(o, "assignment2_1D");
    }

    public void score1dClassifierQuality(Outcome o, double score) {
        part1dQuality.setScore(score);
        part1dQuality.setNoteWithErrValue(o, "assignment2_1D");
        if (o == Outcome.SUCCESS) {
            part1dQuality.setValue("val", KadenzeUtils.formatDouble(score*100));
        }
    }

    public void score2Experimented(Outcome o, double score) {
        part2Experimented.setScore(score);
        part2Experimented.setNoteWithErrValue(o, "assignment2_2");
    }

    public void score3ABuiltClassifier(Outcome o, double score) {
        part3aBuiltClassifier.setScore(score);
        part3aBuiltClassifier.setNoteWithErrValue(o, "assignment2_3a");
    }

    public void score3bClassifierQuality(Outcome o, double score) {
        part3bClassifierAccuracy.setScore(score);
        //part3bClassifierAccuracy.setNoteWithErrValue(o, "assignment2_3b");
        if (o == Outcome.COULD_BE_BETTER || o == Outcome.SUCCESS) {
            part3bClassifierAccuracy.setNote(o, KadenzeUtils.formatDouble(score * 100));
        } else {
            part3bClassifierAccuracy.setNoteWithErrValue(o, "assignment2_3b");
        }
    }

    public void setCompletelyInvalidSubmission(Outcome o) {
        validSubmission(o, 0.0);
        score1aSetup(Outcome.LOG_READ_ERROR, 0.0);
        score1aExperimented(Outcome.LOG_READ_ERROR, 0.0);
        score1bClassifierQuality(Outcome.LOG_READ_ERROR, 0);
        score1bClassifierType(Outcome.LOG_READ_ERROR, 0);
        score1cClassifierQuality(Outcome.LOG_READ_ERROR, 0);
        score1cClassifierType(Outcome.LOG_READ_ERROR, 0);
        score1dClassifierQuality(Outcome.LOG_READ_ERROR, 0);
        score1dClassifierType(Outcome.LOG_READ_ERROR, 0);
        score2Experimented(Outcome.LOG_READ_ERROR, 0);
        score3ABuiltClassifier(Outcome.LOG_READ_ERROR, 0);
        score3bClassifierQuality(Outcome.LOG_READ_ERROR, 0);
    }

    public Grade getGrade() {
        Grade g = new Grade();
        g.addNotedCriterion(validSubmission);
        g.addNotedCriterion(part1aCorrectSetup);
        g.addNotedCriterion(part1aExperimented);
        g.addNotedCriterion(part1bUsedCorrectClassifier);
        g.addNotedCriterion(part1bQuality);
        g.addNotedCriterion(part1cUsedCorrectClassifier);
        g.addNotedCriterion(part1cQuality);
        g.addNotedCriterion(part1dUsedCorrectClassifier);
        g.addNotedCriterion(part1dQuality);
        g.addNotedCriterion(part2Experimented);
        g.addNotedCriterion(part3aBuiltClassifier);
        g.addNotedCriterion(part3bClassifierAccuracy);
        return g;

    }

}
