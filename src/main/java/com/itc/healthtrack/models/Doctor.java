package com.itc.healthtrack.models;

// Representa un doctor dentro del sistema
public class Doctor {
    private String id;             // Identificador unico del documento
    private String userProfileId;  // ID del perfil de usuario asociado
    private String specialtyId;    // ID de la especialidad del doctor
    private String licenseNumber;  // Numero de licencia profesional
    private String firstName;      // Nombre del doctor
    private String lastName;       // Apellido del doctor

    // Constructor vacio requerido por Firestore
    public Doctor() {}

    // Devuelve el ID del doctor
    public String getId() {
        return id;
    }

    // Asigna el ID del doctor
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

    // Devuelve el ID de la especialidad
    public String getSpecialtyId() {
        return specialtyId;
    }

    // Asigna el ID de la especialidad
    public void setSpecialtyId(String specialtyId) {
        this.specialtyId = specialtyId;
    }

    // Devuelve el numero de licencia
    public String getLicenseNumber() {
        return licenseNumber;
    }

    // Asigna el numero de licencia
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    // Devuelve el nombre del doctor
    public String getFirstName() {
        return firstName;
    }

    // Asigna el nombre del doctor
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Devuelve el apellido del doctor
    public String getLastName() {
        return lastName;
    }

    // Asigna el apellido del doctor
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Devuelve un texto legible para listas y combos
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
