package com.itc.healthtrack.models;

// Representa un rol del sistema (por ejemplo: patient, doctor, admin)
public class Role {
    private String id;          // Identificador unico del documento
    private String name;        // Nombre del rol en ingles
    private String description; // Descripcion simple del rol

    // Constructor vacio requerido por Firestore
    public Role() {}

    // Devuelve el ID del rol
    public String getId() {
        return id;
    }

    // Asigna el ID del rol
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el nombre del rol
    public String getName() {
        return name;
    }

    // Asigna el nombre del rol
    public void setName(String name) {
        this.name = name;
    }

    // Devuelve la descripcion del rol
    public String getDescription() {
        return description;
    }

    // Asigna la descripcion del rol
    public void setDescription(String description) {
        this.description = description;
    }
}
