package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Medication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion medications
public class MedicationDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public MedicationDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo medicamento
    public void saveMedication(Medication medication) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("medications").document();
        medication.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(medication);
        result.get();
    }

    // Obtiene un medicamento por su ID
    public Medication getMedicationById(String medicationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("medications").document(medicationId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Medication medication = snapshot.toObject(Medication.class);
            if (medication != null) {
                medication.setId(snapshot.getId());
            }
            return medication;
        }
        return null;
    }

    // Obtiene todos los medicamentos registrados
    public List<Medication> getAllMedications() throws ExecutionException, InterruptedException {
        List<Medication> medications = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("medications").get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Medication medication = snapshot.toObject(Medication.class);
            if (medication != null) {
                medication.setId(snapshot.getId());
                medications.add(medication);
            }
        }
        return medications;
    }

    // Actualiza un medicamento existente
    public void updateMedication(Medication medication) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("medications").document(medication.getId());
        ApiFuture<WriteResult> result = docRef.set(medication);
        result.get();
    }

    // Elimina un medicamento por ID
    public void deleteMedication(String medicationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("medications").document(medicationId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
