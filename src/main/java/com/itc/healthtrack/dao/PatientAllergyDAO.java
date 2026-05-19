package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.PatientAllergy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion patientAllergies
public class PatientAllergyDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public PatientAllergyDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo registro de alergia por paciente
    public void savePatientAllergy(PatientAllergy patientAllergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patientAllergies").document();
        patientAllergy.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(patientAllergy);
        result.get();
    }

    // Obtiene un registro por su ID
    public PatientAllergy getPatientAllergyById(String recordId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patientAllergies").document(recordId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            PatientAllergy patientAllergy = snapshot.toObject(PatientAllergy.class);
            if (patientAllergy != null) {
                patientAllergy.setId(snapshot.getId());
            }
            return patientAllergy;
        }
        return null;
    }

    // Obtiene registros por ID de paciente
    public List<PatientAllergy> getPatientAllergiesByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<PatientAllergy> records = new ArrayList<>();
        Query query = db.collection("patientAllergies").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            PatientAllergy patientAllergy = snapshot.toObject(PatientAllergy.class);
            if (patientAllergy != null) {
                patientAllergy.setId(snapshot.getId());
                records.add(patientAllergy);
            }
        }
        return records;
    }

    // Actualiza un registro existente
    public void updatePatientAllergy(PatientAllergy patientAllergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patientAllergies").document(patientAllergy.getId());
        ApiFuture<WriteResult> result = docRef.set(patientAllergy);
        result.get();
    }

    // Elimina un registro por ID
    public void deletePatientAllergy(String recordId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patientAllergies").document(recordId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
