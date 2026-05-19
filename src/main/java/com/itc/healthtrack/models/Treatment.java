package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Representa un tratamiento medico asociado a un paciente
public class Treatment {
    private String id;           // Identificador unico del documento
    private String patientId;    // ID del paciente
    private String doctorId;     // ID del doctor
    private Timestamp startDate; // Fecha de inicio del tratamiento
    private Timestamp endDate;   // Fecha de fin del tratamiento
    private String diagnosis;    // Diagnostico principal

    // Constructor vacio requerido por Firestore
    public Treatment() {}

    // Devuelve el ID del tratamiento
    public String getId() {
        return id;
    }

    // Asigna el ID del tratamiento
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

    // Devuelve el ID del doctor
    public String getDoctorId() {
        return doctorId;
    }

    // Asigna el ID del doctor
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    // Devuelve la fecha de inicio
    public Timestamp getStartDate() {
        return startDate;
    }

    // Asigna la fecha de inicio
    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    // Devuelve la fecha de fin
    public Timestamp getEndDate() {
        return endDate;
    }

    // Asigna la fecha de fin
    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    // Devuelve el diagnostico
    public String getDiagnosis() {
        return diagnosis;
    }

    // Asigna el diagnostico
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
}
