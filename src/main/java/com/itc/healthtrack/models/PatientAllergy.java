package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Represents the relationship between a patient and an allergy
public class PatientAllergy {
    private String id;
    private String patientId;
    private String allergyId;
    private Timestamp detectionDate;
    private String notes;

    public PatientAllergy() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getAllergyId() { return allergyId; }
    public void setAllergyId(String allergyId) { this.allergyId = allergyId; }

    public Timestamp getDetectionDate() { return detectionDate; }
    public void setDetectionDate(Timestamp detectionDate) { this.detectionDate = detectionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
