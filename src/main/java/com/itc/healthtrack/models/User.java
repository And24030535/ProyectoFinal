package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Represents an authenticated user in the system
public class User {
    private String id;
    private String roleId;
    private String email;
    private String password;
    private Timestamp registeredAt;

    // Required empty constructor for Firestore
    public User() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Timestamp getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Timestamp registeredAt) { this.registeredAt = registeredAt; }
}
