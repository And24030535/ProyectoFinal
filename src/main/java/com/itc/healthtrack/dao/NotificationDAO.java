package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion notifications
public class NotificationDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public NotificationDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda una nueva notificacion
    public void saveNotification(Notification notification) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("notifications").document();
        notification.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(notification);
        result.get();
    }

    // Obtiene una notificacion por su ID
    public Notification getNotificationById(String notificationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("notifications").document(notificationId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Notification notification = snapshot.toObject(Notification.class);
            if (notification != null) {
                notification.setId(snapshot.getId());
            }
            return notification;
        }
        return null;
    }

    // Obtiene notificaciones por ID de usuario
    public List<Notification> getNotificationsByUserId(String userId) throws ExecutionException, InterruptedException {
        List<Notification> notifications = new ArrayList<>();
        Query query = db.collection("notifications").whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Notification notification = snapshot.toObject(Notification.class);
            if (notification != null) {
                notification.setId(snapshot.getId());
                notifications.add(notification);
            }
        }
        return notifications;
    }

    // Actualiza una notificacion existente
    public void updateNotification(Notification notification) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("notifications").document(notification.getId());
        ApiFuture<WriteResult> result = docRef.set(notification);
        result.get();
    }

    // Elimina una notificacion por ID
    public void deleteNotification(String notificationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("notifications").document(notificationId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
