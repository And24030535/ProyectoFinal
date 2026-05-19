package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.HealthMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the healthMetrics collection
public class HealthMetricDAO {

    private final Firestore db;

    public HealthMetricDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Create
    public void save(HealthMetric metric) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("healthMetrics").document();
        metric.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(metric);
        result.get();
    }

    // Read
    public HealthMetric getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("healthMetrics").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        HealthMetric metric = document.toObject(HealthMetric.class);
        if (metric != null && (metric.getId() == null || metric.getId().isEmpty())) {
            metric.setId(document.getId());
        }
        return metric;
    }

    public List<HealthMetric> getAll() throws ExecutionException, InterruptedException {
        List<HealthMetric> metrics = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("healthMetrics").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            HealthMetric metric = document.toObject(HealthMetric.class);
            if (metric != null && (metric.getId() == null || metric.getId().isEmpty())) {
                metric.setId(document.getId());
            }
            metrics.add(metric);
        }
        return metrics;
    }

    public List<HealthMetric> getByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<HealthMetric> metrics = new ArrayList<>();
        Query query = db.collection("healthMetrics")
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            HealthMetric metric = document.toObject(HealthMetric.class);
            if (metric != null && (metric.getId() == null || metric.getId().isEmpty())) {
                metric.setId(document.getId());
            }
            metrics.add(metric);
        }
        return metrics;
    }

    // Update
    public void update(HealthMetric metric) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("healthMetrics").document(metric.getId());
        ApiFuture<WriteResult> result = docRef.set(metric);
        result.get();
    }

    // Delete
    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("healthMetrics").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
