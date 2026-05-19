package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Represents a user notification
public class Notification {
    private String id;
    private String userId;
    private Timestamp sentAt;
    private String message;
    private Boolean isDelivered;

    public Notification() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsDelivered() { return isDelivered; }
    public void setIsDelivered(Boolean isDelivered) { this.isDelivered = isDelivered; }
}
