package com.itc.healthtrack.services;

import com.itc.healthtrack.models.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service that simulates external notifications (email/SMS) to patients and doctors
 * when critical health metric trends are detected.
 */
public class NotificationService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Simulates sending an email/SMS notification to a patient.
     * In a production environment, this would integrate with an SMTP
     * server or a messaging API (e.g., Twilio, SendGrid).
     *
     * @param patient The patient to notify.
     * @param message The notification message body.
     */
    public void notifyPatient(User patient, String message) {
        sendNotification("PATIENT", patient.getEmail(), getFullName(patient), message);
    }

    /**
     * Simulates sending an email/SMS notification to a doctor.
     *
     * @param doctor  The doctor to notify.
     * @param message The notification message body.
     */
    public void notifyDoctor(User doctor, String message) {
        sendNotification("DOCTOR", doctor.getEmail(), "Dr. " + getFullName(doctor), message);
    }

    /**
     * Returns the full display name for a user.
     *
     * @param user The user whose name to format.
     * @return First name + " " + last name.
     */
    private String getFullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    /**
     * Core method that simulates the dispatch of an external notification.
     * Logs the notification details to stdout as a simulation.
     *
     * @param recipientType  Role label ("PATIENT" or "DOCTOR").
     * @param email          Recipient email address.
     * @param recipientName  Full display name of the recipient.
     * @param message        The message body to deliver.
     */
    private void sendNotification(String recipientType, String email,
                                   String recipientName, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("=== HealthTrack Notification Dispatch ===");
        System.out.println("Timestamp  : " + timestamp);
        System.out.println("Recipient  : [" + recipientType + "] " + recipientName);
        System.out.println("Email      : " + email);
        System.out.println("Message    : " + message);
        System.out.println("Status     : SENT (simulated)");
        System.out.println("=========================================");
    }
}
