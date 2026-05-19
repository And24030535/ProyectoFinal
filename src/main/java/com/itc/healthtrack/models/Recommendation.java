package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Representa una recomendacion clinica enviada a un paciente
public class Recommendation {
    private String id;          // Identificador unico del documento
    private String patientId;   // ID del paciente relacionado
    private Timestamp generatedAt; // Fecha y hora de generacion
    private String type;        // Tipo de recomendacion
    private String title;       // Titulo visible
    private String message;     // Mensaje completo
    private Boolean isRead;     // Indica si ya fue leida

    // Constructor vacío requerido por Firestore
    public Recommendation() {}

    // Devuelve el ID de la recomendacion
    public String getId() {
        return id;
    }

    // Asigna el ID de la recomendacion
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

    // Devuelve la fecha de generacion
    public Timestamp getGeneratedAt() {
        return generatedAt;
    }

    // Asigna la fecha de generacion
    public void setGeneratedAt(Timestamp generatedAt) {
        this.generatedAt = generatedAt;
    }

    // Devuelve el tipo de recomendacion
    public String getType() {
        return type;
    }

    // Asigna el tipo de recomendacion
    public void setType(String type) {
        this.type = type;
    }

    // Devuelve el titulo
    public String getTitle() {
        return title;
    }

    // Asigna el titulo
    public void setTitle(String title) {
        this.title = title;
    }

    // Devuelve el mensaje
    public String getMessage() {
        return message;
    }

    // Asigna el mensaje
    public void setMessage(String message) {
        this.message = message;
    }

    // Devuelve si ya fue leida
    public Boolean getIsRead() {
        return isRead;
    }

    // Asigna si ya fue leida
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
