package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Represents a treatment plan
public class Treatment {
    private String id;
    private String patientId;
    private String doctorId;
    private Timestamp startDate;
    private Timestamp endDate;
    private String diagnosis;

    public Treatment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
}
