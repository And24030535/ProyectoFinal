package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the appointments collection
public class AppointmentDAO {

    private final Firestore db;

    public AppointmentDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Appointment appointment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("appointments").document();
        appointment.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(appointment);
        result.get();
    }

    public Appointment getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("appointments").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Appointment appointment = document.toObject(Appointment.class);
        if (appointment != null && (appointment.getId() == null || appointment.getId().isEmpty())) {
            appointment.setId(document.getId());
        }
        return appointment;
    }

    public List<Appointment> getAll() throws ExecutionException, InterruptedException {
        List<Appointment> appointments = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("appointments").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Appointment appointment = document.toObject(Appointment.class);
            if (appointment != null && (appointment.getId() == null || appointment.getId().isEmpty())) {
                appointment.setId(document.getId());
            }
            appointments.add(appointment);
        }
        return appointments;
    }

    public List<Appointment> getByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<Appointment> appointments = new ArrayList<>();
        Query query = db.collection("appointments").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Appointment appointment = document.toObject(Appointment.class);
            if (appointment != null && (appointment.getId() == null || appointment.getId().isEmpty())) {
                appointment.setId(document.getId());
            }
            appointments.add(appointment);
        }
        return appointments;
    }

    public List<Appointment> getByDoctorId(String doctorId) throws ExecutionException, InterruptedException {
        List<Appointment> appointments = new ArrayList<>();
        Query query = db.collection("appointments").whereEqualTo("doctorId", doctorId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Appointment appointment = document.toObject(Appointment.class);
            if (appointment != null && (appointment.getId() == null || appointment.getId().isEmpty())) {
                appointment.setId(document.getId());
            }
            appointments.add(appointment);
        }
        return appointments;
    }

    public void update(Appointment appointment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("appointments").document(appointment.getId());
        ApiFuture<WriteResult> result = docRef.set(appointment);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("appointments").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
