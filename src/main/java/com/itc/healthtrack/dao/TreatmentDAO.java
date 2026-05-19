package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Treatment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion treatments
public class TreatmentDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public TreatmentDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo tratamiento
    public void saveTreatment(Treatment treatment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatments").document();
        treatment.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(treatment);
        result.get();
    }

    // Obtiene un tratamiento por su ID
    public Treatment getTreatmentById(String treatmentId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatments").document(treatmentId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Treatment treatment = snapshot.toObject(Treatment.class);
            if (treatment != null) {
                treatment.setId(snapshot.getId());
            }
            return treatment;
        }
        return null;
    }

    // Obtiene tratamientos por ID de paciente
    public List<Treatment> getTreatmentsByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<Treatment> treatments = new ArrayList<>();
        Query query = db.collection("treatments").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Treatment treatment = snapshot.toObject(Treatment.class);
            if (treatment != null) {
                treatment.setId(snapshot.getId());
                treatments.add(treatment);
            }
        }
        return treatments;
    }

    // Actualiza un tratamiento existente
    public void updateTreatment(Treatment treatment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatments").document(treatment.getId());
        ApiFuture<WriteResult> result = docRef.set(treatment);
        result.get();
    }

    // Elimina un tratamiento por ID
    public void deleteTreatment(String treatmentId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("treatments").document(treatmentId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
