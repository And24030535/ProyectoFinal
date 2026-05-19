package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion patients
public class PatientDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public PatientDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo paciente en Firestore
    public void savePatient(Patient patient) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document();
        patient.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(patient);
        result.get();
    }

    // Obtiene un paciente por su ID
    public Patient getPatientById(String patientId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document(patientId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Patient patient = snapshot.toObject(Patient.class);
            if (patient != null) {
                patient.setId(snapshot.getId());
            }
            return patient;
        }
        return null;
    }

    // Obtiene un paciente usando el ID del perfil de usuario
    public Patient getPatientByUserProfileId(String userProfileId) throws ExecutionException, InterruptedException {
        Query query = db.collection("patients").whereEqualTo("userProfileId", userProfileId);
        ApiFuture<QuerySnapshot> result = query.get();

        if (!result.get().getDocuments().isEmpty()) {
            DocumentSnapshot snapshot = result.get().getDocuments().get(0);
            Patient patient = snapshot.toObject(Patient.class);
            if (patient != null) {
                patient.setId(snapshot.getId());
            }
            return patient;
        }
        return null;
    }

    // Obtiene todos los pacientes registrados
    public List<Patient> getAllPatients() throws ExecutionException, InterruptedException {
        List<Patient> patients = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("patients").get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Patient patient = snapshot.toObject(Patient.class);
            if (patient != null) {
                patient.setId(snapshot.getId());
                patients.add(patient);
            }
        }
        return patients;
    }

    // Obtiene pacientes asignados a un doctor especifico
    public List<Patient> getPatientsByDoctorId(String doctorId) throws ExecutionException, InterruptedException {
        List<Patient> patients = new ArrayList<>();
        Query query = db.collection("patients").whereEqualTo("primaryDoctorId", doctorId);
        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Patient patient = snapshot.toObject(Patient.class);
            if (patient != null) {
                patient.setId(snapshot.getId());
                patients.add(patient);
            }
        }
        return patients;
    }

    // Actualiza un paciente existente
    public void updatePatient(Patient patient) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document(patient.getId());
        ApiFuture<WriteResult> result = docRef.set(patient);
        result.get();
    }

    // Elimina un paciente por su ID
    public void deletePatient(String patientId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document(patientId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }

    // Asigna un doctor a un paciente especifico
    public void assignDoctorToPatient(String patientId, String doctorId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document(patientId);
        ApiFuture<WriteResult> result = docRef.update("primaryDoctorId", doctorId);
        result.get();
    }

    // Reasigna pacientes de un doctor a otro
    public void reassignPatients(String oldDoctorId, String newDoctorId) throws ExecutionException, InterruptedException {
        List<Patient> patientsToReassign = getPatientsByDoctorId(oldDoctorId);

        for (Patient patient : patientsToReassign) {
            DocumentReference docRef = db.collection("patients").document(patient.getId());
            if (newDoctorId != null) {
                docRef.update("primaryDoctorId", newDoctorId).get();
            } else {
                docRef.update("primaryDoctorId", null).get();
            }
        }
    }
}
