package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Specialty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion specialties
public class SpecialtyDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public SpecialtyDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda una nueva especialidad en Firestore
    public void saveSpecialty(Specialty specialty) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("specialties").document();
        specialty.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(specialty);
        result.get();
    }

    // Obtiene una especialidad por su ID
    public Specialty getSpecialtyById(String specialtyId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("specialties").document(specialtyId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Specialty specialty = snapshot.toObject(Specialty.class);
            if (specialty != null) {
                specialty.setId(snapshot.getId());
            }
            return specialty;
        }
        return null;
    }

    // Obtiene una especialidad por su nombre
    public Specialty getSpecialtyByName(String name) throws ExecutionException, InterruptedException {
        Query query = db.collection("specialties").whereEqualTo("name", name);
        ApiFuture<QuerySnapshot> result = query.get();

        if (!result.get().getDocuments().isEmpty()) {
            DocumentSnapshot snapshot = result.get().getDocuments().get(0);
            Specialty specialty = snapshot.toObject(Specialty.class);
            if (specialty != null) {
                specialty.setId(snapshot.getId());
            }
            return specialty;
        }
        return null;
    }

    // Obtiene todas las especialidades
    public List<Specialty> getAllSpecialties() throws ExecutionException, InterruptedException {
        List<Specialty> specialties = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("specialties").get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Specialty specialty = snapshot.toObject(Specialty.class);
            if (specialty != null) {
                specialty.setId(snapshot.getId());
                specialties.add(specialty);
            }
        }
        return specialties;
    }

    // Actualiza una especialidad existente
    public void updateSpecialty(Specialty specialty) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("specialties").document(specialty.getId());
        ApiFuture<WriteResult> result = docRef.set(specialty);
        result.get();
    }

    // Elimina una especialidad por su ID
    public void deleteSpecialty(String specialtyId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("specialties").document(specialtyId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
