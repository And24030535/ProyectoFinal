package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Representa una cita medica programada
public class Appointment {
    private String id;                    // Identificador unico del documento
    private String patientId;             // ID del paciente
    private String doctorId;              // ID del doctor
    private Timestamp scheduledDatetime;  // Fecha y hora programada
    private String status;                // Estado de la cita
    private String reason;                // Motivo principal

    // Constructor vacio requerido por Firestore
    public Appointment() {}

    // Devuelve el ID de la cita
    public String getId() {
        return id;
    }

    // Asigna el ID de la cita
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

    // Devuelve la fecha y hora programada
    public Timestamp getScheduledDatetime() {
        return scheduledDatetime;
    }

    // Asigna la fecha y hora programada
    public void setScheduledDatetime(Timestamp scheduledDatetime) {
        this.scheduledDatetime = scheduledDatetime;
    }

    // Devuelve el estado de la cita
    public String getStatus() {
        return status;
    }

    // Asigna el estado de la cita
    public void setStatus(String status) {
        this.status = status;
    }

    // Devuelve el motivo de la cita
    public String getReason() {
        return reason;
    }

    // Asigna el motivo de la cita
    public void setReason(String reason) {
        this.reason = reason;
    }
}
