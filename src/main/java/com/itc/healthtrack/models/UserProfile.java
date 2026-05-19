package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Representa el perfil basico del usuario y su relacion con Firebase Auth
public class UserProfile {
    private String id;             // Identificador unico del documento
    private String authUid;        // UID generado por Firebase Authentication
    private String roleId;         // ID del rol almacenado en la coleccion roles
    private String email;          // Correo electronico del usuario
    private Timestamp registeredAt; // Fecha y hora de registro

    // Constructor vacio requerido por Firestore
    public UserProfile() {}

    // Devuelve el ID del perfil
    public String getId() {
        return id;
    }

    // Asigna el ID del perfil
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el UID de Firebase Auth
    public String getAuthUid() {
        return authUid;
    }

    // Asigna el UID de Firebase Auth
    public void setAuthUid(String authUid) {
        this.authUid = authUid;
    }

    // Devuelve el ID del rol
    public String getRoleId() {
        return roleId;
    }

    // Asigna el ID del rol
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    // Devuelve el correo del usuario
    public String getEmail() {
        return email;
    }

    // Asigna el correo del usuario
    public void setEmail(String email) {
        this.email = email;
    }

    // Devuelve la fecha de registro
    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    // Asigna la fecha de registro
    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }
}
