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
public class Assignment5Grade {

    public Assignment5Grade() {
    }

    private final NotedCriterion validSubmission = new NotedCriterion("valid_submission");
    private final NotedCriterion part1aCorrectSetup = new NotedCriterion("part1a_setup");
    private final NotedCriterion part1aExperimented = new NotedCriterion("part1a_experimented");
    private final NotedCriterion part1bUsedCorrectModel = new NotedCriterion("part1b_used_correct_model");
    private final NotedCriterion part1bQuality = new NotedCriterion("part1b_model_quality");
    private final NotedCriterion part1bNumExamples = new NotedCriterion("part1b_num_examples");
    private final NotedCriterion part1cUsedCorrectModel = new NotedCriterion("part1c_used_correct_model");
    private final NotedCriterion part1cQuality = new NotedCriterion("part1c_model_quality");
    private final NotedCriterion part1cNumExamples= new NotedCriterion("part1c_num_examples");
    private final NotedCriterion part2Experimented = new NotedCriterion("part2_experimented");
    private final NotedCriterion part3aBuiltRegression = new NotedCriterion("part3a_built_regression");
    private final NotedCriterion part3bModelAccuracy = new NotedCriterion("part3b_model_accuracy");
    private static final Logger logger = Logger.getLogger(Assignment5Grade.class.getName());

    public static enum Assignment5Part {

        PART1A, PART1B, PART1C, PART2, PART3A, PART3B
    };

    void scorePartFail(Assignment5Part assignmentPart, Outcome outcome) {
        switch (assignmentPart) {
            case PART1A:
                score1aSetup(outcome, 0.0);
                score1aExperimented(outcome, 0.0);
                break;
            case PART1B:
                score1bModelQuality(outcome, 0.0, 0);
                score1bModelType(outcome, 0);
                score1bNumExamples(outcome, 0, 0);
                break;
            case PART1C:
                score1cModelQuality(outcome, 0.0, 0);
                score1cModelType(outcome, 0);
                score1cNumExamples(outcome, 0, 0);
                break;
            case PART2:
                score2Experimented(outcome, 0.0);
                break;
            case PART3A:
                score3ABuiltRegression(outcome, 0.0);
                break;
            case PART3B:
                score3bModelQuality(outcome, 0.0);
                break;
            default:
                logger.log(Level.SEVERE, "Invalid score part: " + assignmentPart);
        }
    }

    public void validSubmission(Outcome o, double score) {
        validSubmission.setScore(score);
        validSubmission.setNoteWithErrValue(o, "assignment5");
    }

    public void score1aSetup(Outcome o, double score) {
        part1aCorrectSetup.setScore(score);
        part1aCorrectSetup.setNoteWithErrValue(o, "assignment5_1A");
    }

    public void score1aExperimented(Outcome o, double score) {
        part1aExperimented.setScore(score);
        part1aExperimented.setNoteWithErrValue(o, "assignment5_1A");
    }

    public void score1aExperimented_addValues(String algorithmList, int numTrain, int numRun) {
        String[] vals = {algorithmList, Integer.toString(numTrain), Integer.toString(numRun)};
        part1aExperimented.addVals(vals);
    }

    void score2Experimented_addValues(int numTrain, int numRun) {
        String[] vals = {Integer.toString(numTrain), Integer.toString(numRun)};
        part2Experimented.addVals(vals);
    }

    public void score1bModelType(Outcome o, double score) {
        part1bUsedCorrectModel.setScore(score);
        part1bUsedCorrectModel.setNoteWithErrValue(o, "assignment5_1B");
    }
    
    public void score1bModelType_addValues(String modelType, int numInputs, int numOutputs) {
        String[] vals = {modelType, Integer.toString(numInputs), Integer.toString(numOutputs)};
        part1bUsedCorrectModel.addVals(vals);
    }

    public void score1bModelQuality(Outcome o, double score, double RMS) {
        part1bQuality.setScore(score);
        part1bQuality.setNoteWithErrValue(o, "assignment5_1B");
        if (o == Outcome.SUCCESS) {
            part1bQuality.setValue("val", KadenzeUtils.formatDouble(RMS));
        }
    }
    
    public void score1bNumExamples(Outcome o, double score, int numExamples) {
        part1bNumExamples.setScore(score);
        part1bNumExamples.setNoteWithErrValue(o, "assignment5_1B");
        if (o == Outcome.SUCCESS) { //TODO: HELP HERE!
            part1bNumExamples.setValue("val", Integer.toString(numExamples));
        }
    }
    
    

    public void score1cModelType(Outcome o, double score) {
        part1cUsedCorrectModel.setScore(score);
        part1cUsedCorrectModel.setNoteWithErrValue(o, "assignment5_1C");
    }
    
    public void score1cModelType_addValues(String modelType, int numInputs, int numOutputs) {
        String[] vals = {modelType, Integer.toString(numInputs), Integer.toString(numOutputs)};
        part1cUsedCorrectModel.addVals(vals);
    }

    public void score1cModelQuality(Outcome o, double score, double RMS) {
        part1cQuality.setScore(score);
        part1cQuality.setNoteWithErrValue(o, "assignment5_1C");
        if (o == Outcome.SUCCESS) {
            part1cQuality.setValue("val", KadenzeUtils.formatDouble(RMS));
        }
    }
    
        
    public void score1cNumExamples(Outcome o, double score, int numExamples) {
        part1cNumExamples.setScore(score);
        part1cNumExamples.setNoteWithErrValue(o, "assignment5_1C");
        if (o == Outcome.SUCCESS) {
            part1cNumExamples.setValue("val", Integer.toString(numExamples));
        }
    }

    public void score2Experimented(Outcome o, double score) {
        part2Experimented.setScore(score);
        part2Experimented.setNoteWithErrValue(o, "assignment5_2");
    }

    public void score3ABuiltRegression(Outcome o, double score) {
        part3aBuiltRegression.setScore(score);
        part3aBuiltRegression.setNoteWithErrValue(o, "assignment5_3a");
    }

    public void score3bModelQuality(Outcome o, double score) {
        part3bModelAccuracy.setScore(score);
        //part3bClassifierAccuracy.setNoteWithErrValue(o, "assignment2_3b");
        if (o == Outcome.COULD_BE_BETTER || o == Outcome.SUCCESS) {
            part3bModelAccuracy.setNote(o, KadenzeUtils.formatDouble(score * 100));
        } else {
            part3bModelAccuracy.setNoteWithErrValue(o, "assignment5_3b");
        }
    }

    public void setCompletelyInvalidSubmission(Outcome o) {
        //TODO: Might want to change following outcomes to all be "o" instead of log_read_error
        validSubmission(o, 0.0);
        score1aSetup(Outcome.LOG_READ_ERROR, 0.0);
        score1aExperimented(Outcome.LOG_READ_ERROR, 0.0);
        score1bModelQuality(Outcome.LOG_READ_ERROR, 0, 0);
        score1bModelType(Outcome.LOG_READ_ERROR,0);
        score1bNumExamples(Outcome.LOG_READ_ERROR, 0,0);
        score1cModelQuality(Outcome.LOG_READ_ERROR, 0, 0);
        score1cModelType(Outcome.LOG_READ_ERROR,0);
        score1cNumExamples(Outcome.LOG_READ_ERROR, 0, 0);
        score2Experimented(Outcome.LOG_READ_ERROR, 0);
        score3ABuiltRegression(Outcome.LOG_READ_ERROR, 0);
        score3bModelQuality(Outcome.LOG_READ_ERROR, 0);
    }

    public Grade getGrade() {
        Grade g = new Grade();
        g.addNotedCriterion(validSubmission);
        g.addNotedCriterion(part1aCorrectSetup);
        g.addNotedCriterion(part1aExperimented);
        g.addNotedCriterion(part1bUsedCorrectModel);
        g.addNotedCriterion(part1bQuality);
        g.addNotedCriterion(part1bNumExamples);
        g.addNotedCriterion(part1cUsedCorrectModel);
        g.addNotedCriterion(part1cQuality);
        g.addNotedCriterion(part1cNumExamples);
        g.addNotedCriterion(part2Experimented);
        g.addNotedCriterion(part3aBuiltRegression);
        g.addNotedCriterion(part3bModelAccuracy);
        return g;
    }

}
