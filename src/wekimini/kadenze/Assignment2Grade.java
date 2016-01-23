/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import wekimini.kadenze.NotedCriterion.Outcome;

/**
 *
 * @author rebecca
 */
public class Assignment2Grade {
    private final String graderName;
    private final String graderVersion;
    private final String jsonApi;

    public Assignment2Grade(String graderName, String graderVersion, String jsonApi) {
        this.graderName = graderName;
        this.graderVersion = graderVersion;
        this.jsonApi = jsonApi;
    }
    
    private final NotedCriterion validSubmission = new NotedCriterion(1, "valid_submission");
    private final NotedCriterion part1aCorrectSetup = new NotedCriterion(2, "part1a_setup");

    private final NotedCriterion part1aExperimented = new NotedCriterion(3, "part1a_experimented");
    private final NotedCriterion part1bUsedCorrectClassifier = new NotedCriterion(4, "part1b_used_correct_classifier");
    private final NotedCriterion part1bQuality = new NotedCriterion(5, "part1b_classifier_quality");
    private final NotedCriterion part1cUsedCorrectClassifier = new NotedCriterion(6, "part1c_used_correct_classifier");
    private final NotedCriterion part1cQuality = new NotedCriterion(7, "part1c_classifier_quality");
    private final NotedCriterion part1dUsedCorrectClassifier = new NotedCriterion(8, "part1d_used_correct_classifier");
    private final NotedCriterion part1dQuality = new NotedCriterion(9, "part1d_classifier_quality");
    private final NotedCriterion part2Experimented = new NotedCriterion(10, "part2_experimented");
    private final NotedCriterion part3aBuiltClassifier = new NotedCriterion(11, "part3a_built_classifier");
    private final NotedCriterion part3bClassifierAccuracy = new NotedCriterion(12, "part3b_classifier_accuracy");

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

    public void score1bClassifierType(Outcome o, double score) {
        part1bUsedCorrectClassifier.setScore(score);
        part1bUsedCorrectClassifier.setNoteWithErrValue(o, "assignment2_1B");
    }

    public void score1bClassifierQuality(Outcome o, double score) {
        part1bQuality.setScore(score);
        part1bQuality.setNoteWithErrValue(o, "assignment2_1B");
    }

    public void score1cClassifierType(Outcome o, double score) {
        part1cUsedCorrectClassifier.setScore(score);
        part1cUsedCorrectClassifier.setNoteWithErrValue(o, "assignment2_1C");
    }

    public void score1cClassifierQuality(Outcome o, double score) {
        part1cQuality.setScore(score);
        part1cQuality.setNoteWithErrValue(o, "assignment2_1C");
    }

    public void score1dClassifierType(Outcome o, double score) {
        part1dUsedCorrectClassifier.setScore(score);
        part1dUsedCorrectClassifier.setNoteWithErrValue(o, "assignment2_1D");
    }

    public void score1dClassifierQuality(Outcome o, double score) {
        part1dQuality.setScore(score);
        part1dQuality.setNoteWithErrValue( o, "assignment2_1D");
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
        part3bClassifierAccuracy.setNoteWithErrValue(o, "assignment2_3b");
    }
    
    public void setCompletelyInvalidSubmission(Outcome o) {
        validSubmission(o, 0.0);
        score1aSetup(Outcome.LOG_ERROR, 0.0);
        score1aExperimented(Outcome.LOG_ERROR, 0.0);
        score1bClassifierQuality(Outcome.LOG_ERROR, 0);
        score1bClassifierType(Outcome.LOG_ERROR, 0);
        score1cClassifierQuality(Outcome.LOG_ERROR, 0);
        score1cClassifierType(Outcome.LOG_ERROR, 0);
        score1dClassifierQuality(Outcome.LOG_ERROR, 0);
        score1dClassifierType(Outcome.LOG_ERROR, 0);
        score2Experimented(Outcome.LOG_ERROR, 0);
        score3ABuiltClassifier(Outcome.LOG_ERROR, 0);
        score3bClassifierQuality(Outcome.LOG_ERROR, 0);
    }
    
    
    public Grade getGrade() {
        Grade g = new Grade(graderName, graderVersion, jsonApi);
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
