package com.itc.healthtrack.models;

// Representa el detalle de un tratamiento con medicamentos
public class TreatmentDetail {
    private String id;           // Identificador unico del documento
    private String treatmentId;  // ID del tratamiento
    private String medicationId; // ID del medicamento
    private String dosage;       // Dosis indicada
    private String frequency;    // Frecuencia indicada

    // Constructor vacio requerido por Firestore
    public TreatmentDetail() {}

    // Devuelve el ID del detalle
    public String getId() {
        return id;
    }

    // Asigna el ID del detalle
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el ID del tratamiento
    public String getTreatmentId() {
        return treatmentId;
    }

    // Asigna el ID del tratamiento
    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    // Devuelve el ID del medicamento
    public String getMedicationId() {
        return medicationId;
    }

    // Asigna el ID del medicamento
    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    // Devuelve la dosis
    public String getDosage() {
        return dosage;
    }

    // Asigna la dosis
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    // Devuelve la frecuencia
    public String getFrequency() {
        return frequency;
    }

    // Asigna la frecuencia
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
