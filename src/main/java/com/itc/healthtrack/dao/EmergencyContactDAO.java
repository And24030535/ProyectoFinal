package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.EmergencyContact;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion emergencyContacts
public class EmergencyContactDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public EmergencyContactDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo contacto de emergencia
    public void saveEmergencyContact(EmergencyContact contact) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("emergencyContacts").document();
        contact.setId(docRef.getId());

        ApiFuture<WriteResult> result = docRef.set(contact);
        result.get();
    }

    // Obtiene un contacto por su ID
    public EmergencyContact getEmergencyContactById(String contactId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("emergencyContacts").document(contactId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            EmergencyContact contact = snapshot.toObject(EmergencyContact.class);
            if (contact != null) {
                contact.setId(snapshot.getId());
            }
            return contact;
        }
        return null;
    }

    // Obtiene contactos por ID de paciente
    public List<EmergencyContact> getEmergencyContactsByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<EmergencyContact> contacts = new ArrayList<>();
        Query query = db.collection("emergencyContacts").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> result = query.get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            EmergencyContact contact = snapshot.toObject(EmergencyContact.class);
            if (contact != null) {
                contact.setId(snapshot.getId());
                contacts.add(contact);
            }
        }
        return contacts;
    }

    // Actualiza un contacto de emergencia
    public void updateEmergencyContact(EmergencyContact contact) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("emergencyContacts").document(contact.getId());
        ApiFuture<WriteResult> result = docRef.set(contact);
        result.get();
    }

    // Elimina un contacto de emergencia por ID
    public void deleteEmergencyContact(String contactId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("emergencyContacts").document(contactId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
