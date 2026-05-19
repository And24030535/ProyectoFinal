package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the recommendations collection
public class RecommendationDAO {

    private final Firestore db;

    public RecommendationDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Create
    public void save(Recommendation recommendation) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("recommendations").document();
        recommendation.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(recommendation);
        result.get();
    }

    // Read
    public Recommendation getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("recommendations").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Recommendation recommendation = document.toObject(Recommendation.class);
        if (recommendation != null && (recommendation.getId() == null || recommendation.getId().isEmpty())) {
            recommendation.setId(document.getId());
        }
        return recommendation;
    }

    public List<Recommendation> getAll() throws ExecutionException, InterruptedException {
        List<Recommendation> recommendations = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("recommendations").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Recommendation recommendation = document.toObject(Recommendation.class);
            if (recommendation != null && (recommendation.getId() == null || recommendation.getId().isEmpty())) {
                recommendation.setId(document.getId());
            }
            recommendations.add(recommendation);
        }
        return recommendations;
    }

    public List<Recommendation> getByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<Recommendation> recommendations = new ArrayList<>();
        Query query = db.collection("recommendations")
                .whereEqualTo("patientId", patientId)
                .orderBy("generatedAt", Query.Direction.DESCENDING);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Recommendation recommendation = document.toObject(Recommendation.class);
            if (recommendation != null && (recommendation.getId() == null || recommendation.getId().isEmpty())) {
                recommendation.setId(document.getId());
            }
            recommendations.add(recommendation);
        }
        return recommendations;
    }

    // Update
    public void update(Recommendation recommendation) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("recommendations").document(recommendation.getId());
        ApiFuture<WriteResult> result = docRef.set(recommendation);
        result.get();
    }

    // Delete
    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("recommendations").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
