package com.itc.healthtrack.models;

// Representa una alergia catalogada en el sistema
public class Allergy {
    private String id;       // Identificador unico del documento
    private String name;     // Nombre de la alergia
    private String severity; // Nivel de severidad

    // Constructor vacio requerido por Firestore
    public Allergy() {}

    // Devuelve el ID de la alergia
    public String getId() {
        return id;
    }

    // Asigna el ID de la alergia
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el nombre de la alergia
    public String getName() {
        return name;
    }

    // Asigna el nombre de la alergia
    public void setName(String name) {
        this.name = name;
    }

    // Devuelve la severidad
    public String getSeverity() {
        return severity;
    }

    // Asigna la severidad
    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
