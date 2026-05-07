package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/* Gestiona todas las operaciones de lectura y escritura de recomendaciones médicas
 (alertas, sugerencias, recordatorios) en Firestore*/
public class RecommendationDAO {

    // Instancia de Firestore desde la configuración de Firebase
    private final Firestore db;

    //Constructor que inicializa la conexión a Firestore
    public RecommendationDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    //Guarda una nueva recomendación (alerta, sugerencia o recordatorio) en Firestore
    public void saveRecommendation(Recommendation recommendation) throws ExecutionException, InterruptedException {
        // Se crea una referencia a un nuevo documento con ID autogenerado
        DocumentReference docRef = db.collection("recommendations").document();
        recommendation.setId(docRef.getId());

        // Se guarda el objeto completo en la base de datos
        ApiFuture<WriteResult> result = docRef.set(recommendation);
        result.get(); // Espera a que la operación se complete
    }

    //Obtiene las recomendaciones de un paciente, de la mas reciente a la mas antigua
    public List<Recommendation> getRecommendationsByPatient(String patientId) throws ExecutionException, InterruptedException {
        List<Recommendation> recList = new ArrayList<>();

        // buscar donde patientId coincida y ordenar descendente
        Query query = db.collection("recommendations")
                .whereEqualTo("patientId", patientId)
                .orderBy("generatedAt", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        // Convierte cada documento de Firestore en un objeto Recommendation de Java
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Recommendation rec = document.toObject(Recommendation.class);
            recList.add(rec);
        }

        return recList;
    }
}