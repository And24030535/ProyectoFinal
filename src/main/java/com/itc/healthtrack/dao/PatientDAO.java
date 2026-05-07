package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*Gestiona las operaciones CRUD exclusivas para los pacientes en la clinica.
 Los pacientes se almacenan en la coleccion 'users' pero con el rol 'patient'*/
public class PatientDAO {

    private final Firestore db;

    public PatientDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    //Obtiene la lista de pacientes asignados a un medico en especifico
    public List<User> getPatientsByDoctor(String doctorId) throws ExecutionException, InterruptedException {
        List<User> patientsList = new ArrayList<>();

        //Buscar usuarios que sean pacientes y esten asignados a este medico
        Query query = db.collection("users")
                .whereEqualTo("role", "patient")
                .whereEqualTo("assignedDoctorId", doctorId);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User patient = document.toObject(User.class);
            patientsList.add(patient);
        }

        return patientsList;
    }

    //Actualiza la informacion de un paciente existente
    public void updatePatient(User patient) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(patient.getUid());
        ApiFuture<WriteResult> result = docRef.set(patient);
        result.get();
    }

    //Elimina el registro de un paciente de la base de datos
    public void deletePatient(String patientId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(patientId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }

    //Obtener todos los pacientes
    public List<User> getAllPatients() throws ExecutionException, InterruptedException {
        List<User> patientsList = new ArrayList<>();
        Query query = db.collection("users").whereEqualTo("role", "patient");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User patient = document.toObject(User.class);
            if (patient != null && (patient.getUid() == null || patient.getUid().isEmpty())) {
                patient.setUid(document.getId());
            }
            patientsList.add(patient);
        }
        return patientsList;
    }

    //Asigna un doctor a un paciente específico
    public void assignDoctorToPatient(String patientId, String doctorId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(patientId);
        ApiFuture<WriteResult> result = docRef.update("assignedDoctorId", doctorId);
        result.get();
    }

    //Obtiene una lista de pacientes que no tienen doctor asignado
    public List<User> getUnassignedPatients() throws ExecutionException, InterruptedException {
        List<User> unassignedPatients = new ArrayList<>();
        Query query = db.collection("users")
                .whereEqualTo("role", "patient")
                .whereEqualTo("assignedDoctorId", null);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User patient = document.toObject(User.class);
            if (patient != null && (patient.getUid() == null || patient.getUid().isEmpty())) {
                patient.setUid(document.getId());
            }
            unassignedPatients.add(patient);
        }
        return unassignedPatients;
    }

    //Reasigna todos los pacientes de un doctor a otro doctor, sii newDoctorId es null, desasigna los pacientes (los deja sin doctor)*/
    public void reassignPatients(String oldDoctorId, String newDoctorId) throws ExecutionException, InterruptedException {
        List<User> patientsToReassign = getPatientsByDoctor(oldDoctorId);

        for (User patient : patientsToReassign) {
            DocumentReference docRef = db.collection("users").document(patient.getUid());
            if (newDoctorId != null) {
                docRef.update("assignedDoctorId", newDoctorId).get();
            } else {
                // Si no hay nuevo doctor, se elimina la asignación
                docRef.update("assignedDoctorId", FieldValue.delete()).get();
            }
        }
    }

    //Obtiene todos los doctores disponibles del sistema
    public List<User> getAllDoctors() throws ExecutionException, InterruptedException {
        List<User> doctorsList = new ArrayList<>();
        Query query = db.collection("users").whereEqualTo("role", "doctor");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User doctor = document.toObject(User.class);
            if (doctor != null && (doctor.getUid() == null || doctor.getUid().isEmpty())) {
                doctor.setUid(document.getId());
            }
            doctorsList.add(doctor);
        }
        return doctorsList;
    }

    //Verifica si existen doctores disponibles en el sistema
    public boolean hasDoctorsAvailable() throws ExecutionException, InterruptedException {
        return !getAllDoctors().isEmpty();
    }
}