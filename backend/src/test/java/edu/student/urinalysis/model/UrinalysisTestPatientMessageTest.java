package edu.student.urinalysis.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UrinalysisTestPatientMessageTest {

    @Test
    void shouldGeneratePatientFriendlySummaryForAbnormalFindings() {
        UrinalysisTest test = new UrinalysisTest();
        test.setSampleId("SAMPLE-1001");
        test.setUrineColor("Dark yellow");
        test.setPh("3.2");
        test.setSpecificGravity("1.040");
        test.setGlucose("Trace");
        test.setProtein("Positive");
        test.setBlood("Moderate");
        test.prepareForSave();

        String message = test.getPatientMessage();

        assertTrue(message.contains("pH is 3.2"));
        assertTrue(message.contains("glucose is detected"));
        assertTrue(message.contains("protein is detected"));
        assertTrue(message.contains("blood is detected"));
        assertTrue(test.getAbnormalFindings().stream().anyMatch(item -> item.contains("color")));
        assertTrue(test.getPriorityLevel().contains("High"));
        assertTrue(test.getDoctorSummary().contains("likely"));
    }
}
