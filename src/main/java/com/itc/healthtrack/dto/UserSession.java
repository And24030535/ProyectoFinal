package com.itc.healthtrack.dto;

import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.User;

// Represents a joined user session with profile data
public class UserSession {
    private User user;
    private Patient patient;
    private Doctor doctor;
    private String assignedDoctorName;

    public UserSession() {}

    public UserSession(User user, Patient patient, Doctor doctor) {
        this.user = user;
        this.patient = patient;
        this.doctor = doctor;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public String getUserId() { return user != null ? user.getId() : null; }
    public String getRoleId() { return user != null ? user.getRoleId() : null; }
    public String getEmail() { return user != null ? user.getEmail() : null; }

    public String getPatientId() { return patient != null ? patient.getId() : null; }
    public String getDoctorId() { return doctor != null ? doctor.getId() : null; }

    public String getFirstName() {
        if (patient != null) return patient.getFirstName();
        if (doctor != null) return doctor.getFirstName();
        return null;
    }

    public String getLastName() {
        if (patient != null) return patient.getLastName();
        if (doctor != null) return doctor.getLastName();
        return null;
    }

    public String getBirthDate() { return patient != null ? patient.getBirthDate() : null; }
    public String getGender() { return patient != null ? patient.getGender() : null; }
    public Double getHeight() { return patient != null ? patient.getHeight() : null; }

    public String getPrimaryDoctorId() { return patient != null ? patient.getPrimaryDoctorId() : null; }
    public void setPrimaryDoctorId(String primaryDoctorId) {
        if (patient != null) {
            patient.setPrimaryDoctorId(primaryDoctorId);
        }
    }

    public String getAssignedDoctorName() { return assignedDoctorName; }
    public void setAssignedDoctorName(String assignedDoctorName) { this.assignedDoctorName = assignedDoctorName; }

    @Override
    public String toString() {
        if (getFirstName() != null && getLastName() != null) {
            return getFirstName() + " " + getLastName();
        }
        if (getFirstName() != null) {
            return getFirstName();
        }
        if (getLastName() != null) {
            return getLastName();
        }
        return getEmail() != null ? getEmail() : super.toString();
    }
}
