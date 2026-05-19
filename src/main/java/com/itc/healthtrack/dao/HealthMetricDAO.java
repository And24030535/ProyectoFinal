package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.HealthMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion healthMetrics
public class HealthMetricDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public HealthMetricDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda una nueva medicion en Firestore
    public void saveHealthMetric(HealthMetric metric) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("healthMetrics").document();
        metric.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(metric);
        result.get();
    }

    // Obtiene una medicion por su ID
    public HealthMetric getHealthMetricById(String metricId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("healthMetrics").document(metricId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            HealthMetric metric = snapshot.toObject(HealthMetric.class);
            if (metric != null) {
                metric.setId(snapshot.getId());
            }
            return metric;
        }
        return null;
    }

    // Obtiene todas las mediciones de un paciente, ordenadas por fecha
    public List<HealthMetric> getHealthMetricsByPatient(String patientId) throws ExecutionException, InterruptedException {
        List<HealthMetric> metrics = new ArrayList<>();

        Query query = db.collection("healthMetrics")
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            HealthMetric metric = snapshot.toObject(HealthMetric.class);
            if (metric != null) {
                metric.setId(snapshot.getId());
                metrics.add(metric);
            }
        }
        return metrics;
    }

    // Actualiza una medicion existente
    public void updateHealthMetric(HealthMetric metric) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("healthMetrics").document(metric.getId());
        ApiFuture<WriteResult> result = docRef.set(metric);
        result.get();
    }

    // Elimina una medicion por ID
    public void deleteHealthMetric(String metricId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("healthMetrics").document(metricId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
