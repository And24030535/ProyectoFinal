package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Allergy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion allergies
public class AllergyDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public AllergyDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda una nueva alergia
    public void saveAllergy(Allergy allergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("allergies").document();
        allergy.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(allergy);
        result.get();
    }

    // Obtiene una alergia por su ID
    public Allergy getAllergyById(String allergyId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("allergies").document(allergyId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Allergy allergy = snapshot.toObject(Allergy.class);
            if (allergy != null) {
                allergy.setId(snapshot.getId());
            }
            return allergy;
        }
        return null;
    }

    // Obtiene todas las alergias registradas
    public List<Allergy> getAllAllergies() throws ExecutionException, InterruptedException {
        List<Allergy> allergies = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("allergies").get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Allergy allergy = snapshot.toObject(Allergy.class);
            if (allergy != null) {
                allergy.setId(snapshot.getId());
                allergies.add(allergy);
            }
        }
        return allergies;
    }

    // Actualiza una alergia existente
    public void updateAllergy(Allergy allergy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("allergies").document(allergy.getId());
        ApiFuture<WriteResult> result = docRef.set(allergy);
        result.get();
    }

    // Elimina una alergia por ID
    public void deleteAllergy(String allergyId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("allergies").document(allergyId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
