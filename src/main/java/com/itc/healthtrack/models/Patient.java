package com.itc.healthtrack.models;

// Represents a patient profile
public class Patient {
    private String id;
    private String userId;
    private String primaryDoctorId;
    private String firstName;
    private String lastName;
    private String birthDate;
    private String gender;
    private Double height;

    public Patient() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPrimaryDoctorId() { return primaryDoctorId; }
    public void setPrimaryDoctorId(String primaryDoctorId) { this.primaryDoctorId = primaryDoctorId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
}
