package com.itc.healthtrack.models;

// Representa un medicamento disponible en el sistema
public class Medication {
    private String id;           // Identificador unico del documento
    private String genericName;  // Nombre generico del medicamento
    private String brandName;    // Nombre comercial del medicamento
    private String manufacturer; // Fabricante del medicamento

    // Constructor vacio requerido por Firestore
    public Medication() {}

    // Devuelve el ID del medicamento
    public String getId() {
        return id;
    }

    // Asigna el ID del medicamento
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el nombre generico
    public String getGenericName() {
        return genericName;
    }

    // Asigna el nombre generico
    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    // Devuelve el nombre comercial
    public String getBrandName() {
        return brandName;
    }

    // Asigna el nombre comercial
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    // Devuelve el fabricante
    public String getManufacturer() {
        return manufacturer;
    }

    // Asigna el fabricante
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
