package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Treatment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the treatments collection
public class TreatmentDAO {

    private final Firestore db;

    public TreatmentDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Treatment treatment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatments").document();
        treatment.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(treatment);
        result.get();
    }

    public Treatment getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("treatments").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Treatment treatment = document.toObject(Treatment.class);
        if (treatment != null && (treatment.getId() == null || treatment.getId().isEmpty())) {
            treatment.setId(document.getId());
        }
        return treatment;
    }

    public List<Treatment> getAll() throws ExecutionException, InterruptedException {
        List<Treatment> treatments = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("treatments").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Treatment treatment = document.toObject(Treatment.class);
            if (treatment != null && (treatment.getId() == null || treatment.getId().isEmpty())) {
                treatment.setId(document.getId());
            }
            treatments.add(treatment);
        }
        return treatments;
    }

    public List<Treatment> getByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<Treatment> treatments = new ArrayList<>();
        Query query = db.collection("treatments").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Treatment treatment = document.toObject(Treatment.class);
            if (treatment != null && (treatment.getId() == null || treatment.getId().isEmpty())) {
                treatment.setId(document.getId());
            }
            treatments.add(treatment);
        }
        return treatments;
    }

    public List<Treatment> getByDoctorId(String doctorId) throws ExecutionException, InterruptedException {
        List<Treatment> treatments = new ArrayList<>();
        Query query = db.collection("treatments").whereEqualTo("doctorId", doctorId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Treatment treatment = document.toObject(Treatment.class);
            if (treatment != null && (treatment.getId() == null || treatment.getId().isEmpty())) {
                treatment.setId(document.getId());
            }
            treatments.add(treatment);
        }
        return treatments;
    }

    public void update(Treatment treatment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatments").document(treatment.getId());
        ApiFuture<WriteResult> result = docRef.set(treatment);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatments").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
