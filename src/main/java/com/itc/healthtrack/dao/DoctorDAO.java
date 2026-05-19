package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion doctors
public class DoctorDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public DoctorDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo doctor en Firestore
    public void saveDoctor(Doctor doctor) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("doctors").document();
        doctor.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(doctor);
        result.get();
    }

    // Obtiene un doctor por su ID
    public Doctor getDoctorById(String doctorId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("doctors").document(doctorId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Doctor doctor = snapshot.toObject(Doctor.class);
            if (doctor != null) {
                doctor.setId(snapshot.getId());
            }
            return doctor;
        }
        return null;
    }

    // Obtiene un doctor usando el ID del perfil de usuario
    public Doctor getDoctorByUserProfileId(String userProfileId) throws ExecutionException, InterruptedException {
        Query query = db.collection("doctors").whereEqualTo("userProfileId", userProfileId);
        ApiFuture<QuerySnapshot> result = query.get();
        if (!result.get().getDocuments().isEmpty()) {
            DocumentSnapshot snapshot = result.get().getDocuments().get(0);
            Doctor doctor = snapshot.toObject(Doctor.class);
            if (doctor != null) {
                doctor.setId(snapshot.getId());
            }
            return doctor;
        }
        return null;
    }

    // Obtiene todos los doctores registrados
    public List<Doctor> getAllDoctors() throws ExecutionException, InterruptedException {
        List<Doctor> doctors = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("doctors").get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Doctor doctor = snapshot.toObject(Doctor.class);
            if (doctor != null) {
                doctor.setId(snapshot.getId());
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    // Actualiza un doctor existente
    public void updateDoctor(Doctor doctor) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("doctors").document(doctor.getId());
        ApiFuture<WriteResult> result = docRef.set(doctor);
        result.get();
    }

    // Elimina un doctor por su ID
    public void deleteDoctor(String doctorId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("doctors").document(doctorId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
