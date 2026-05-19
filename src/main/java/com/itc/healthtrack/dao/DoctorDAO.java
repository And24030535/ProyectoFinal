package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the doctors collection
public class DoctorDAO {

    private final Firestore db;

    public DoctorDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Doctor doctor) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("doctors").document();
        doctor.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(doctor);
        result.get();
    }

    public Doctor getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("doctors").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Doctor doctor = document.toObject(Doctor.class);
        if (doctor != null && (doctor.getId() == null || doctor.getId().isEmpty())) {
            doctor.setId(document.getId());
        }
        return doctor;
    }

    public Doctor getByUserId(String userId) throws ExecutionException, InterruptedException {
        Query query = db.collection("doctors").whereEqualTo("userId", userId).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (querySnapshot.get().getDocuments().isEmpty()) {
            return null;
        }
        DocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
        Doctor doctor = document.toObject(Doctor.class);
        if (doctor != null && (doctor.getId() == null || doctor.getId().isEmpty())) {
            doctor.setId(document.getId());
        }
        return doctor;
    }

    public List<Doctor> getAll() throws ExecutionException, InterruptedException {
        List<Doctor> doctors = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("doctors").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Doctor doctor = document.toObject(Doctor.class);
            if (doctor != null && (doctor.getId() == null || doctor.getId().isEmpty())) {
                doctor.setId(document.getId());
            }
            doctors.add(doctor);
        }
        return doctors;
    }

    public List<Doctor> getBySpecialtyId(String specialtyId) throws ExecutionException, InterruptedException {
        List<Doctor> doctors = new ArrayList<>();
        Query query = db.collection("doctors").whereEqualTo("specialtyId", specialtyId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Doctor doctor = document.toObject(Doctor.class);
            if (doctor != null && (doctor.getId() == null || doctor.getId().isEmpty())) {
                doctor.setId(document.getId());
            }
            doctors.add(doctor);
        }
        return doctors;
    }

    public void update(Doctor doctor) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("doctors").document(doctor.getId());
        ApiFuture<WriteResult> result = docRef.set(doctor);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("doctors").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
