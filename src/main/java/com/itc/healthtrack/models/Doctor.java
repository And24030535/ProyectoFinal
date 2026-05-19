package com.itc.healthtrack.models;

// Represents a doctor profile
public class Doctor {
    private String id;
    private String userId;
    private String specialtyId;
    private String licenseNumber;
    private String firstName;
    private String lastName;

    public Doctor() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(String specialtyId) { this.specialtyId = specialtyId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
