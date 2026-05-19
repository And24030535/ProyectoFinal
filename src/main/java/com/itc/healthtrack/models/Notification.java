package com.itc.healthtrack.models;

import com.google.cloud.Timestamp;

// Representa una notificacion enviada a un usuario
public class Notification {
    private String id;          // Identificador unico del documento
    private String userId;      // ID del usuario relacionado
    private Timestamp sentAt;   // Fecha y hora de envio
    private String message;     // Mensaje de la notificacion
    private Boolean isDelivered; // Indica si se entrego la notificacion

    // Constructor vacio requerido por Firestore
    public Notification() {}

    // Devuelve el ID de la notificacion
    public String getId() {
        return id;
    }

    // Asigna el ID de la notificacion
    public void setId(String id) {
        this.id = id;
    }

    // Devuelve el ID del usuario
    public String getUserId() {
        return userId;
    }

    // Asigna el ID del usuario
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Devuelve la fecha de envio
    public Timestamp getSentAt() {
        return sentAt;
    }

    // Asigna la fecha de envio
    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    // Devuelve el mensaje
    public String getMessage() {
        return message;
    }

    // Asigna el mensaje
    public void setMessage(String message) {
        this.message = message;
    }

    // Devuelve si se entrego la notificacion
    public Boolean getIsDelivered() {
        return isDelivered;
    }

    // Asigna si se entrego la notificacion
    public void setIsDelivered(Boolean isDelivered) {
        this.isDelivered = isDelivered;
    }
}
