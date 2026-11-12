package edu.student.urinalysis.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Document(collection = "urinalysis_tests")
public class UrinalysisTest {
    @Id
    private String id;

    @NotBlank(message = "Sample ID is required")
    private String sampleId;

    private LocalDate testDate;

    @Pattern(regexp = "^$|^([0-9]+(\\.[0-9]+)?)$", message = "pH must be numeric")
    private String ph;

    @Pattern(regexp = "^$|^([0-9]+(\\.[0-9]+)?)$", message = "Specific gravity must be numeric")
    private String specificGravity;

    private String glucose;
    private String protein;
    private String ketones;
    private String blood;
    private String nitrite;
    private String leukocytes;
    private String urineColor = "Pale yellow";
    private String notes;
    private List<String> flags = new ArrayList<>();
    private List<String> normalFindings = new ArrayList<>();
    private List<String> abnormalFindings = new ArrayList<>();
    private String reviewStatus = "No flags";
    private String priorityLevel = "Low";
    private String patientMessage = "No abnormal findings were detected in this urine sample.";
    private String doctorSummary = "No abnormal findings were detected in this urine sample.";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UrinalysisTest() {}

    public void prepareForSave() {
        if (testDate == null) testDate = LocalDate.now();
        if (glucose == null) glucose = "Negative";
        if (protein == null) protein = "Negative";
        if (ketones == null) ketones = "Negative";
        if (blood == null) blood = "Negative";
        if (nitrite == null) nitrite = "Negative";
        if (leukocytes == null) leukocytes = "Negative";
        if (urineColor == null || urineColor.isBlank()) urineColor = "Pale yellow";
        if (ph == null) ph = "";
        if (specificGravity == null) specificGravity = "";
        if (notes == null) notes = "";
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        flags = new ArrayList<>();
        normalFindings = new ArrayList<>();
        abnormalFindings = new ArrayList<>();

        evaluateColor();
        evaluateNumericRange();
        evaluatePositiveFinding("glucose", glucose, "glucose is detected");
        evaluatePositiveFinding("protein", protein, "protein is detected");
        evaluatePositiveFinding("ketones", ketones, "ketones are detected");
        evaluatePositiveFinding("blood", blood, "blood is detected");
        evaluatePositiveFinding("nitrite", nitrite, "nitrite is detected");
        evaluatePositiveFinding("leukocytes", leukocytes, "leukocytes are elevated");

        reviewStatus = abnormalFindings.isEmpty() ? "No flags" : "Review recommended";
        priorityLevel = determinePriorityLevel();
        patientMessage = buildPatientMessage();
        doctorSummary = buildDoctorSummary();
    }

    private void evaluateColor() {
        String normalized = urineColor == null ? "" : urineColor.trim();
        if (normalized.isBlank()) {
            abnormalFindings.add("urine color is not recorded");
            return;
        }

        if (normalized.equalsIgnoreCase("Pale yellow") || normalized.equalsIgnoreCase("Clear") || normalized.equalsIgnoreCase("Light yellow")) {
            normalFindings.add("urine color is " + normalized.toLowerCase() + ", which is within the expected appearance");
        } else {
            abnormalFindings.add("urine color is " + normalized.toLowerCase() + ", which may suggest dehydration or another concern");
        }
    }

    private void evaluateNumericRange() {
        try {
            if (!ph.isBlank()) {
                double value = Double.parseDouble(ph);
                if (value >= 4.5 && value <= 8.0) {
                    normalFindings.add("pH is " + ph + ", which is within the expected range");
                } else {
                    abnormalFindings.add("pH is " + ph + " outside the expected range of 4.5 to 8.0");
                    flags.add("pH outside illustrative range (4.5–8.0)");
                }
            }
            if (!specificGravity.isBlank()) {
                double value = Double.parseDouble(specificGravity);
                if (value >= 1.005 && value <= 1.030) {
                    normalFindings.add("specific gravity is " + specificGravity + ", which is within the expected range");
                } else {
                    abnormalFindings.add("specific gravity is " + specificGravity + " outside the expected range of 1.005 to 1.030");
                    flags.add("Specific gravity outside illustrative range (1.005–1.030)");
                }
            }
        } catch (NumberFormatException ignored) {
            // Numeric input is also validated by the API's field pattern.
        }
    }

    private void evaluatePositiveFinding(String label, String value, String description) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (value.equalsIgnoreCase("negative") || value.equalsIgnoreCase("normal") || value.equalsIgnoreCase("not detected")) {
            normalFindings.add(label + " is " + value.toLowerCase() + ", which is reassuring");
            return;
        }

        abnormalFindings.add(description);
        flags.add(label.substring(0, 1).toUpperCase() + label.substring(1) + " recorded as " + value + " — review");
    }

    private String determinePriorityLevel() {
        if (abnormalFindings.isEmpty()) return "Low";

        int highRiskSignals = 0;
        if (containsAny("blood is detected") || containsAny("protein is detected") || containsAny("nitrite is detected") || containsAny("leukocytes are elevated")) highRiskSignals++;
        if (containsAny("pH is ") || containsAny("specific gravity is ")) highRiskSignals++;
        if (containsAny("urine color is " )) highRiskSignals++;

        if (highRiskSignals >= 3 || containsAny("blood is detected")) return "High";
        if (highRiskSignals >= 2) return "Moderate";
        return "Low";
    }

    private boolean containsAny(String fragment) {
        return abnormalFindings.stream().anyMatch(item -> item.contains(fragment));
    }

    private String buildPatientMessage() {
        if (abnormalFindings.isEmpty()) {
            return "No abnormal findings were detected. This urine sample is within the illustrative reference range.";
        }

        return "Possible concern: " + String.join(", ", abnormalFindings) + ". Please discuss these findings with a clinician for review.";
    }

    private String buildDoctorSummary() {
        if (abnormalFindings.isEmpty()) {
            return "No abnormal findings were detected. This sample appears within the illustrative reference range and does not suggest an immediate concern in the demo model.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("This pattern is likely associated with dehydration, urinary irritation, a possible urinary tract infection, proteinuria, hematuria, or metabolic stress. The report should be reviewed alongside hydration status, symptoms, and repeat testing. Abnormal findings: ");
        builder.append(String.join("; ", abnormalFindings));
        builder.append(".");
        return builder.toString();
    }

    private void addPositiveFlag(String label, String value) {
        if (value != null && !value.isBlank() &&
            !value.equalsIgnoreCase("negative") && !value.equalsIgnoreCase("normal") &&
            !value.equalsIgnoreCase("not detected")) {
            flags.add(label + " recorded as " + value + " — review");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }
    public LocalDate getTestDate() { return testDate; }
    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }
    public String getPh() { return ph; }
    public void setPh(String ph) { this.ph = ph; }
    public String getSpecificGravity() { return specificGravity; }
    public void setSpecificGravity(String specificGravity) { this.specificGravity = specificGravity; }
    public String getGlucose() { return glucose; }
    public void setGlucose(String glucose) { this.glucose = glucose; }
    public String getProtein() { return protein; }
    public void setProtein(String protein) { this.protein = protein; }
    public String getKetones() { return ketones; }
    public void setKetones(String ketones) { this.ketones = ketones; }
    public String getBlood() { return blood; }
    public void setBlood(String blood) { this.blood = blood; }
    public String getNitrite() { return nitrite; }
    public void setNitrite(String nitrite) { this.nitrite = nitrite; }
    public String getLeukocytes() { return leukocytes; }
    public void setLeukocytes(String leukocytes) { this.leukocytes = leukocytes; }
    public String getUrineColor() { return urineColor; }
    public void setUrineColor(String urineColor) { this.urineColor = urineColor; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }
    public List<String> getNormalFindings() { return normalFindings; }
    public void setNormalFindings(List<String> normalFindings) { this.normalFindings = normalFindings; }
    public List<String> getAbnormalFindings() { return abnormalFindings; }
    public void setAbnormalFindings(List<String> abnormalFindings) { this.abnormalFindings = abnormalFindings; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }
    public String getPatientMessage() { return patientMessage; }
    public void setPatientMessage(String patientMessage) { this.patientMessage = patientMessage; }
    public String getDoctorSummary() { return doctorSummary; }
    public void setDoctorSummary(String doctorSummary) { this.doctorSummary = doctorSummary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
