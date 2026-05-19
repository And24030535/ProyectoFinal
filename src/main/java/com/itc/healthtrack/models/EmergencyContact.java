package com.itc.healthtrack.models;

// Represents an emergency contact for a patient
public class EmergencyContact {
    private String id;
    private String patientId;
    private String fullName;
    private String phoneNumber;
    private String relationship;

    public EmergencyContact() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
}
