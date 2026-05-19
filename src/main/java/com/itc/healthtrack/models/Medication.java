package com.itc.healthtrack.models;

// Represents a medication catalog entry
public class Medication {
    private String id;
    private String genericName;
    private String brandName;
    private String manufacturer;

    public Medication() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
}
