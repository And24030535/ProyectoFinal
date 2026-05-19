package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Represents a scheduled appointment
public class Appointment {
    private String id;
    private String patientId;
    private String doctorId;
    private Timestamp scheduledDatetime;
    private String status;
    private String reason;

    public Appointment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public Timestamp getScheduledDatetime() { return scheduledDatetime; }
    public void setScheduledDatetime(Timestamp scheduledDatetime) { this.scheduledDatetime = scheduledDatetime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
