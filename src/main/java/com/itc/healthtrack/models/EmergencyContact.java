package com.itc.healthtrack.models;

// Representa un contacto de emergencia asociado a un paciente
public class EmergencyContact {
    private String id;           // Identificador unico del documento
    private String patientId;    // ID del paciente relacionado
    private String fullName;     // Nombre completo del contacto
    private String phoneNumber;  // Numero de telefono del contacto
    private String relationship; // Relacion con el paciente

    // Constructor vacio requerido por Firestore
    public EmergencyContact() {}

    // Devuelve el ID del contacto
    public String getId() {
        return id;
    }

    // Asigna el ID del contacto
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

    // Devuelve el nombre completo
    public String getFullName() {
        return fullName;
    }

    // Asigna el nombre completo
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Devuelve el numero de telefono
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Asigna el numero de telefono
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Devuelve la relacion con el paciente
    public String getRelationship() {
        return relationship;
    }

    // Asigna la relacion con el paciente
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
