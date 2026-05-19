package com.itc.healthtrack.models;

// Represents a medication detail inside a treatment
public class TreatmentDetail {
    private String id;
    private String treatmentId;
    private String medicationId;
    private String dosage;
    private String frequency;

    public TreatmentDetail() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTreatmentId() { return treatmentId; }
    public void setTreatmentId(String treatmentId) { this.treatmentId = treatmentId; }

    public String getMedicationId() { return medicationId; }
    public void setMedicationId(String medicationId) { this.medicationId = medicationId; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
}
