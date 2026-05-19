package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Specialty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the specialties collection
public class SpecialtyDAO {

    private final Firestore db;

    public SpecialtyDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Specialty specialty) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("specialties").document();
        specialty.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(specialty);
        result.get();
    }

    public Specialty getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("specialties").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Specialty specialty = document.toObject(Specialty.class);
        if (specialty != null && (specialty.getId() == null || specialty.getId().isEmpty())) {
            specialty.setId(document.getId());
        }
        return specialty;
    }

    public List<Specialty> getAll() throws ExecutionException, InterruptedException {
        List<Specialty> specialties = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("specialties").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Specialty specialty = document.toObject(Specialty.class);
            if (specialty != null && (specialty.getId() == null || specialty.getId().isEmpty())) {
                specialty.setId(document.getId());
            }
            specialties.add(specialty);
        }
        return specialties;
    }

    public void update(Specialty specialty) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("specialties").document(specialty.getId());
        ApiFuture<WriteResult> result = docRef.set(specialty);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("specialties").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
