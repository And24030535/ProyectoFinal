package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.TreatmentDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion treatmentDetails
public class TreatmentDetailDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public TreatmentDetailDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo detalle de tratamiento
    public void saveTreatmentDetail(TreatmentDetail detail) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatmentDetails").document();
        detail.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(detail);
        result.get();
    }

    // Obtiene un detalle por su ID
    public TreatmentDetail getTreatmentDetailById(String detailId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatmentDetails").document(detailId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            TreatmentDetail detail = snapshot.toObject(TreatmentDetail.class);
            if (detail != null) {
                detail.setId(snapshot.getId());
            }
            return detail;
        }
        return null;
    }

    // Obtiene detalles por ID de tratamiento
    public List<TreatmentDetail> getTreatmentDetailsByTreatmentId(String treatmentId) throws ExecutionException, InterruptedException {
        List<TreatmentDetail> details = new ArrayList<>();
        Query query = db.collection("treatmentDetails").whereEqualTo("treatmentId", treatmentId);
        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            TreatmentDetail detail = snapshot.toObject(TreatmentDetail.class);
            if (detail != null) {
                detail.setId(snapshot.getId());
                details.add(detail);
            }
        }
        return details;
    }

    // Actualiza un detalle existente
    public void updateTreatmentDetail(TreatmentDetail detail) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatmentDetails").document(detail.getId());
        ApiFuture<WriteResult> result = docRef.set(detail);
        result.get();
    }

    // Elimina un detalle por ID
    public void deleteTreatmentDetail(String detailId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatmentDetails").document(detailId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
