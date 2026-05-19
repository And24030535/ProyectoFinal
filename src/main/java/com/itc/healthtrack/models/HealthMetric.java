package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Representa una medicion clinica asociada a un paciente
public class HealthMetric {
    private String id;           // Identificador unico del documento
    private String patientId;    // ID del paciente relacionado
    private Timestamp timestamp; // Fecha y hora de la medicion
    private Integer systolic;    // Presion sistolica
    private Integer diastolic;   // Presion diastolica
    private Integer heartRate;   // Frecuencia cardiaca
    private Double weight;       // Peso en kilogramos
    private Double bmi;          // Indice de masa corporal
    private Double glucoseLevel; // Nivel de glucosa

    // Constructor vacio requerido por Firestore
    public HealthMetric() {}

    // Devuelve el ID de la medicion
    public String getId() {
        return id;
    }

    // Asigna el ID de la medicion
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el ID del paciente
    public String getPatientId() {
        return patientId;
    }

    // Asigna el ID del paciente
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    // Devuelve la marca de tiempo
    public Timestamp getTimestamp() {
        return timestamp;
    }

    // Asigna la marca de tiempo
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // Devuelve la presion sistolica
    public Integer getSystolic() {
        return systolic;
    }

    // Asigna la presion sistolica
    public void setSystolic(Integer systolic) {
        this.systolic = systolic;
    }

    // Devuelve la presion diastolica
    public Integer getDiastolic() {
        return diastolic;
    }

    // Asigna la presion diastolica
    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
    }

    // Devuelve la frecuencia cardiaca
    public Integer getHeartRate() {
        return heartRate;
    }

    // Asigna la frecuencia cardiaca
    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    // Devuelve el peso del paciente
    public Double getWeight() {
        return weight;
    }

    // Asigna el peso del paciente
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    // Devuelve el indice de masa corporal
    public Double getBmi() {
        return bmi;
    }

    // Asigna el indice de masa corporal
    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    // Devuelve el nivel de glucosa
    public Double getGlucoseLevel() {
        return glucoseLevel;
    }

    // Asigna el nivel de glucosa
    public void setGlucoseLevel(Double glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
    }
}
