package com.itc.healthtrack.models;

// Representa una especialidad medica disponible
public class Specialty {
    private String id;    // Identificador unico del documento
    private String name;  // Nombre de la especialidad

    // Constructor vacio requerido por Firestore
    public Specialty() {}

    // Devuelve el ID de la especialidad
    public String getId() {
        return id;
    }

    // Asigna el ID de la especialidad
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el nombre de la especialidad
    public String getName() {
        return name;
    }

    // Asigna el nombre de la especialidad
    public void setName(String name) {
        this.name = name;
    }
}
