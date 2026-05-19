package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.TreatmentDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the treatmentDetails collection
public class TreatmentDetailDAO {

    private final Firestore db;

    public TreatmentDetailDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(TreatmentDetail detail) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatmentDetails").document();
        detail.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(detail);
        result.get();
    }

    public TreatmentDetail getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("treatmentDetails").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        TreatmentDetail detail = document.toObject(TreatmentDetail.class);
        if (detail != null && (detail.getId() == null || detail.getId().isEmpty())) {
            detail.setId(document.getId());
        }
        return detail;
    }

    public List<TreatmentDetail> getAll() throws ExecutionException, InterruptedException {
        List<TreatmentDetail> details = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("treatmentDetails").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            TreatmentDetail detail = document.toObject(TreatmentDetail.class);
            if (detail != null && (detail.getId() == null || detail.getId().isEmpty())) {
                detail.setId(document.getId());
            }
            details.add(detail);
        }
        return details;
    }

    public List<TreatmentDetail> getByTreatmentId(String treatmentId) throws ExecutionException, InterruptedException {
        List<TreatmentDetail> details = new ArrayList<>();
        Query query = db.collection("treatmentDetails").whereEqualTo("treatmentId", treatmentId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            TreatmentDetail detail = document.toObject(TreatmentDetail.class);
            if (detail != null && (detail.getId() == null || detail.getId().isEmpty())) {
                detail.setId(document.getId());
            }
            details.add(detail);
        }
        return details;
    }

    public void update(TreatmentDetail detail) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatmentDetails").document(detail.getId());
        ApiFuture<WriteResult> result = docRef.set(detail);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatmentDetails").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
