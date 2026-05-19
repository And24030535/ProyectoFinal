package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion recommendations
public class RecommendationDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public RecommendationDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda una nueva recomendacion
    public void saveRecommendation(Recommendation recommendation) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("recommendations").document();
        recommendation.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(recommendation);
        result.get();
    }

    // Obtiene una recomendacion por su ID
    public Recommendation getRecommendationById(String recommendationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("recommendations").document(recommendationId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Recommendation recommendation = snapshot.toObject(Recommendation.class);
            if (recommendation != null) {
                recommendation.setId(snapshot.getId());
            }
            return recommendation;
        }
        return null;
    }

    // Obtiene recomendaciones por ID de paciente, ordenadas por fecha
    public List<Recommendation> getRecommendationsByPatient(String patientId) throws ExecutionException, InterruptedException {
        List<Recommendation> recommendations = new ArrayList<>();

        Query query = db.collection("recommendations")
                .whereEqualTo("patientId", patientId)
                .orderBy("generatedAt", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Recommendation recommendation = snapshot.toObject(Recommendation.class);
            if (recommendation != null) {
                recommendation.setId(snapshot.getId());
                recommendations.add(recommendation);
            }
        }
        return recommendations;
    }

    // Actualiza una recomendacion existente
    public void updateRecommendation(Recommendation recommendation) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("recommendations").document(recommendation.getId());
        ApiFuture<WriteResult> result = docRef.set(recommendation);
        result.get();
    }

    // Elimina una recomendacion por ID
    public void deleteRecommendation(String recommendationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("recommendations").document(recommendationId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
