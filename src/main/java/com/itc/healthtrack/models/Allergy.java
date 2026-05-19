package com.itc.healthtrack.models;

// Represents an allergy definition
public class Allergy {
    private String id;
    private String name;
    private String severity;

    public Allergy() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
