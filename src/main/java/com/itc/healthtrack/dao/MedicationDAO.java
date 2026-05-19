package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Medication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the medications collection
public class MedicationDAO {

    private final Firestore db;

    public MedicationDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Medication medication) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("medications").document();
        medication.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(medication);
        result.get();
    }

    public Medication getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("medications").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Medication medication = document.toObject(Medication.class);
        if (medication != null && (medication.getId() == null || medication.getId().isEmpty())) {
            medication.setId(document.getId());
        }
        return medication;
    }

    public List<Medication> getAll() throws ExecutionException, InterruptedException {
        List<Medication> medications = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("medications").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Medication medication = document.toObject(Medication.class);
            if (medication != null && (medication.getId() == null || medication.getId().isEmpty())) {
                medication.setId(document.getId());
            }
            medications.add(medication);
        }
        return medications;
    }

    public void update(Medication medication) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("medications").document(medication.getId());
        ApiFuture<WriteResult> result = docRef.set(medication);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("medications").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
