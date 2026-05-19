package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion appointments
public class AppointmentDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public AppointmentDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda una nueva cita
    public void saveAppointment(Appointment appointment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("appointments").document();
        appointment.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(appointment);
        result.get();
    }

    // Obtiene una cita por su ID
    public Appointment getAppointmentById(String appointmentId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("appointments").document(appointmentId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Appointment appointment = snapshot.toObject(Appointment.class);
            if (appointment != null) {
                appointment.setId(snapshot.getId());
            }
            return appointment;
        }
        return null;
    }

    // Obtiene citas por ID de paciente
    public List<Appointment> getAppointmentsByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<Appointment> appointments = new ArrayList<>();
        Query query = db.collection("appointments").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Appointment appointment = snapshot.toObject(Appointment.class);
            if (appointment != null) {
                appointment.setId(snapshot.getId());
                appointments.add(appointment);
            }
        }
        return appointments;
    }

    // Actualiza una cita existente
    public void updateAppointment(Appointment appointment) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("appointments").document(appointment.getId());
        ApiFuture<WriteResult> result = docRef.set(appointment);
        result.get();
    }

    // Elimina una cita por ID
    public void deleteAppointment(String appointmentId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("appointments").document(appointmentId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
