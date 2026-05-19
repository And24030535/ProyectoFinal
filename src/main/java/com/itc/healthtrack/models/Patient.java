package com.itc.healthtrack.models;

// Representa un paciente dentro del sistema
public class Patient {
    private String id;              // Identificador unico del documento
    private String userProfileId;   // ID del perfil de usuario asociado
    private String primaryDoctorId; // ID del doctor principal asignado
    private String firstName;       // Nombre del paciente
    private String lastName;        // Apellido del paciente
    private String birthDate;       // Fecha de nacimiento en formato texto
    private String gender;          // Genero del paciente
    private Double height;          // Estatura en metros

    // Constructor vacio requerido por Firestore
    public Patient() {}

    // Devuelve el ID del paciente
    public String getId() {
        return id;
    }

    // Asigna el ID del paciente
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el ID del perfil de usuario
    public String getUserProfileId() {
        return userProfileId;
    }

    // Asigna el ID del perfil de usuario
    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }

    // Devuelve el ID del doctor asignado
    public String getPrimaryDoctorId() {
        return primaryDoctorId;
    }

    // Asigna el ID del doctor asignado
    public void setPrimaryDoctorId(String primaryDoctorId) {
        this.primaryDoctorId = primaryDoctorId;
    }

    // Devuelve el nombre del paciente
    public String getFirstName() {
        return firstName;
    }

    // Asigna el nombre del paciente
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Devuelve el apellido del paciente
    public String getLastName() {
        return lastName;
    }

    // Asigna el apellido del paciente
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Devuelve la fecha de nacimiento
    public String getBirthDate() {
        return birthDate;
    }

    // Asigna la fecha de nacimiento
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    // Devuelve el genero del paciente
    public String getGender() {
        return gender;
    }

    // Asigna el genero del paciente
    public void setGender(String gender) {
        this.gender = gender;
    }

    // Devuelve la estatura del paciente
    public Double getHeight() {
        return height;
    }

    // Asigna la estatura del paciente
    public void setHeight(Double height) {
        this.height = height;
    }

    // Devuelve un texto legible para listas y combos
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
