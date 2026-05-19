package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Represents a health metric entry for a patient
public class HealthMetric {
    private String id;
    private String patientId;
    private Timestamp timestamp;
    private Integer systolic;
    private Integer diastolic;
    private Integer heartRate;
    private Double weight;
    private Double bmi;
    private Double glucoseLevel;

    // Required empty constructor for Firestore
    public HealthMetric() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public Integer getSystolic() { return systolic; }
    public void setSystolic(Integer systolic) { this.systolic = systolic; }

    public Integer getDiastolic() { return diastolic; }
    public void setDiastolic(Integer diastolic) { this.diastolic = diastolic; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }

    public Double getGlucoseLevel() { return glucoseLevel; }
    public void setGlucoseLevel(Double glucoseLevel) { this.glucoseLevel = glucoseLevel; }
}
