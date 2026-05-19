package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.EmergencyContact;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the emergencyContacts collection
public class EmergencyContactDAO {

    private final Firestore db;

    public EmergencyContactDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(EmergencyContact contact) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("emergencyContacts").document();
        contact.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(contact);
        result.get();
    }

    public EmergencyContact getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("emergencyContacts").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        EmergencyContact contact = document.toObject(EmergencyContact.class);
        if (contact != null && (contact.getId() == null || contact.getId().isEmpty())) {
            contact.setId(document.getId());
        }
        return contact;
    }

    public List<EmergencyContact> getAll() throws ExecutionException, InterruptedException {
        List<EmergencyContact> contacts = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("emergencyContacts").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            EmergencyContact contact = document.toObject(EmergencyContact.class);
            if (contact != null && (contact.getId() == null || contact.getId().isEmpty())) {
                contact.setId(document.getId());
            }
            contacts.add(contact);
        }
        return contacts;
    }

    public List<EmergencyContact> getByPatientId(String patientId) throws ExecutionException, InterruptedException {
        List<EmergencyContact> contacts = new ArrayList<>();
        Query query = db.collection("emergencyContacts").whereEqualTo("patientId", patientId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            EmergencyContact contact = document.toObject(EmergencyContact.class);
            if (contact != null && (contact.getId() == null || contact.getId().isEmpty())) {
                contact.setId(document.getId());
            }
            contacts.add(contact);
        }
        return contacts;
    }

    public void update(EmergencyContact contact) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("emergencyContacts").document(contact.getId());
        ApiFuture<WriteResult> result = docRef.set(contact);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("emergencyContacts").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
