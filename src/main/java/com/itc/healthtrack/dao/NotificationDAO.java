package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the notifications collection
public class NotificationDAO {

    private final Firestore db;

    public NotificationDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Notification notification) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("notifications").document();
        notification.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(notification);
        result.get();
    }

    public Notification getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("notifications").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Notification notification = document.toObject(Notification.class);
        if (notification != null && (notification.getId() == null || notification.getId().isEmpty())) {
            notification.setId(document.getId());
        }
        return notification;
    }

    public List<Notification> getAll() throws ExecutionException, InterruptedException {
        List<Notification> notifications = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("notifications").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Notification notification = document.toObject(Notification.class);
            if (notification != null && (notification.getId() == null || notification.getId().isEmpty())) {
                notification.setId(document.getId());
            }
            notifications.add(notification);
        }
        return notifications;
    }

    public List<Notification> getByUserId(String userId) throws ExecutionException, InterruptedException {
        List<Notification> notifications = new ArrayList<>();
        Query query = db.collection("notifications").whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Notification notification = document.toObject(Notification.class);
            if (notification != null && (notification.getId() == null || notification.getId().isEmpty())) {
                notification.setId(document.getId());
            }
            notifications.add(notification);
        }
        return notifications;
    }

    public void update(Notification notification) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("notifications").document(notification.getId());
        ApiFuture<WriteResult> result = docRef.set(notification);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("notifications").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
