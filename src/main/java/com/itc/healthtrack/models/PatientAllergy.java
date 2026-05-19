package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Representa la relacion entre un paciente y una alergia
public class PatientAllergy {
    private String id;             // Identificador unico del documento
    private String patientId;      // ID del paciente relacionado
    private String allergyId;      // ID de la alergia relacionada
    private Timestamp detectionDate; // Fecha en la que se detecto la alergia
    private String notes;          // Notas adicionales

    // Constructor vacio requerido por Firestore
    public PatientAllergy() {}

    // Devuelve el ID del registro
    public String getId() {
        return id;
    }

    // Asigna el ID del registro
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

    // Devuelve el ID de la alergia
    public String getAllergyId() {
        return allergyId;
    }

    // Asigna el ID de la alergia
    public void setAllergyId(String allergyId) {
        this.allergyId = allergyId;
    }

    // Devuelve la fecha de deteccion
    public Timestamp getDetectionDate() {
        return detectionDate;
    }

    // Asigna la fecha de deteccion
    public void setDetectionDate(Timestamp detectionDate) {
        this.detectionDate = detectionDate;
    }

    // Devuelve las notas
    public String getNotes() {
        return notes;
    }

    // Asigna las notas
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
