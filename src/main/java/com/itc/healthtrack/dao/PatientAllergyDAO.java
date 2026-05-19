package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.PatientAllergy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the patientAllergies collection
public class PatientAllergyDAO {

    private final Firestore db;

    public PatientAllergyDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(PatientAllergy patientAllergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patientAllergies").document();
        patientAllergy.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(patientAllergy);
        result.get();
    }

    public PatientAllergy getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("patientAllergies").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        PatientAllergy patientAllergy = document.toObject(PatientAllergy.class);
        if (patientAllergy != null && (patientAllergy.getId() == null || patientAllergy.getId().isEmpty())) {
            patientAllergy.setId(document.getId());
        }
        return patientAllergy;
    }

    public List<PatientAllergy> getAll() throws ExecutionException, InterruptedException {
        List<PatientAllergy> patientAllergies = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("patientAllergies").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            PatientAllergy patientAllergy = document.toObject(PatientAllergy.class);
            if (patientAllergy != null && (patientAllergy.getId() == null || patientAllergy.getId().isEmpty())) {
                patientAllergy.setId(document.getId());
            }
            patientAllergies.add(patientAllergy);
        }
        return patientAllergies;
    }

    public List<PatientAllergy> getByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<PatientAllergy> patientAllergies = new ArrayList<>();
        Query query = db.collection("patientAllergies").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            PatientAllergy patientAllergy = document.toObject(PatientAllergy.class);
            if (patientAllergy != null && (patientAllergy.getId() == null || patientAllergy.getId().isEmpty())) {
                patientAllergy.setId(document.getId());
            }
            patientAllergies.add(patientAllergy);
        }
        return patientAllergies;
    }

    public void update(PatientAllergy patientAllergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patientAllergies").document(patientAllergy.getId());
        ApiFuture<WriteResult> result = docRef.set(patientAllergy);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patientAllergies").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
