package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Allergy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the allergies collection
public class AllergyDAO {

    private final Firestore db;

    public AllergyDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Allergy allergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("allergies").document();
        allergy.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(allergy);
        result.get();
    }

    public Allergy getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("allergies").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Allergy allergy = document.toObject(Allergy.class);
        if (allergy != null && (allergy.getId() == null || allergy.getId().isEmpty())) {
            allergy.setId(document.getId());
        }
        return allergy;
    }

    public List<Allergy> getAll() throws ExecutionException, InterruptedException {
        List<Allergy> allergies = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("allergies").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Allergy allergy = document.toObject(Allergy.class);
            if (allergy != null && (allergy.getId() == null || allergy.getId().isEmpty())) {
                allergy.setId(document.getId());
            }
            allergies.add(allergy);
        }
        return allergies;
    }

    public void update(Allergy allergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("allergies").document(allergy.getId());
        ApiFuture<WriteResult> result = docRef.set(allergy);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("allergies").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
