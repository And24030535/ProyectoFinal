package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the patients collection
public class PatientDAO {

    private final Firestore db;

    public PatientDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Create
    public void save(Patient patient) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document();
        patient.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(patient);
        result.get();
    }

    // Read
    public Patient getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("patients").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Patient patient = document.toObject(Patient.class);
        if (patient != null && (patient.getId() == null || patient.getId().isEmpty())) {
            patient.setId(document.getId());
        }
        return patient;
    }

    public Patient getByUserId(String userId) throws ExecutionException, InterruptedException {
        Query query = db.collection("patients").whereEqualTo("userId", userId).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (querySnapshot.get().getDocuments().isEmpty()) {
            return null;
        }
        DocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
        Patient patient = document.toObject(Patient.class);
        if (patient != null && (patient.getId() == null || patient.getId().isEmpty())) {
            patient.setId(document.getId());
        }
        return patient;
    }

    public List<Patient> getAll() throws ExecutionException, InterruptedException {
        List<Patient> patients = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("patients").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Patient patient = document.toObject(Patient.class);
            if (patient != null && (patient.getId() == null || patient.getId().isEmpty())) {
                patient.setId(document.getId());
            }
            patients.add(patient);
        }
        return patients;
    }

    public List<Patient> getPatientsByDoctor(String doctorId) throws ExecutionException, InterruptedException {
        List<Patient> patients = new ArrayList<>();
        Query query = db.collection("patients").whereEqualTo("primaryDoctorId", doctorId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Patient patient = document.toObject(Patient.class);
            if (patient != null && (patient.getId() == null || patient.getId().isEmpty())) {
                patient.setId(document.getId());
            }
            patients.add(patient);
        }
        return patients;
    }

    public List<Patient> getUnassignedPatients() throws ExecutionException, InterruptedException {
        List<Patient> patients = new ArrayList<>();
        Query query = db.collection("patients").whereEqualTo("primaryDoctorId", null);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Patient patient = document.toObject(Patient.class);
            if (patient != null && (patient.getId() == null || patient.getId().isEmpty())) {
                patient.setId(document.getId());
            }
            patients.add(patient);
        }
        return patients;
    }

    // Update
    public void update(Patient patient) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document(patient.getId());
        ApiFuture<WriteResult> result = docRef.set(patient);
        result.get();
    }

    public void assignPrimaryDoctor(String patientId, String doctorId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document(patientId);
        ApiFuture<WriteResult> result = docRef.update("primaryDoctorId", doctorId);
        result.get();
    }

    public void reassignPatients(String oldDoctorId, String newDoctorId) throws ExecutionException, InterruptedException {
        List<Patient> patientsToReassign = getPatientsByDoctor(oldDoctorId);
        for (Patient patient : patientsToReassign) {
            DocumentReference docRef = db.collection("patients").document(patient.getId());
            if (newDoctorId != null) {
                docRef.update("primaryDoctorId", newDoctorId).get();
            } else {
                docRef.update("primaryDoctorId", FieldValue.delete()).get();
            }
        }
    }

    // Delete
    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("patients").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
