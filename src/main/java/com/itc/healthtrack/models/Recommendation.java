package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Represents a recommendation or alert sent to a patient
public class Recommendation {
    private String id;
    private String patientId;
    private Timestamp generatedAt;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;

    // Required empty constructor for Firestore
    public Recommendation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public Timestamp getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Timestamp generatedAt) { this.generatedAt = generatedAt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}
