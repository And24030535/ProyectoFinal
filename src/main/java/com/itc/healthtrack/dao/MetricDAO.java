package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

//Gestiona las operaciones de lectura y escritura para la coleccion 'metrics'
public class MetricDAO {

    // Instancia de Firestore desde la configuración de Firebase
    private final Firestore db;

    // Constructor que inicializa la conexión a Firestore
    public MetricDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda una nueva medicion (presion, glucosa, peso) en Firestore
    public void saveMetric(Metric metric) throws ExecutionException, InterruptedException {
        // Se crea una referencia a un nuevo documento vacio para obtener un ID autogenerado
        DocumentReference docRef = db.collection("metrics").document();
        metric.setId(docRef.getId()); // Se asigna el ID autogenerado al objeto

        // Se guarda el objeto completo en la base de datos
        ApiFuture<WriteResult> result = docRef.set(metric);
        result.get(); // Espera a que la operación se complete
    }

    // Recupera el historial de mediciones de un paciente especifico, ordenado por fecha
    public List<Metric> getMetricsByPatient(String patientId) throws ExecutionException, InterruptedException {
        List<Metric> metricsList = new ArrayList<>();

        // Buscar donde patientId coincida y ordenar descendentemente
        Query query = db.collection("metrics")
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        // Convierte cada documento de Firestore en un objeto Metric de Java
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Metric metric = document.toObject(Metric.class);
            metricsList.add(metric);
        }

        return metricsList;
    }
    //Actualiza una medicion existente en la base de datos
    public void updateMetric(Metric metric) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("metrics").document(metric.getId());
        ApiFuture<WriteResult> result = docRef.set(metric);
        result.get(); // Espera a que la operación se complete
    }

    //Elimina el registro de una medicion de la base de datos usando su ID.
    public void deleteMetric(String metricId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("metrics").document(metricId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get(); // Espera a que la operación se complete
    }
}