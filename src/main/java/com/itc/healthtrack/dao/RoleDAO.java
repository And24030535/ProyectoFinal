package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the roles collection
public class RoleDAO {

    private final Firestore db;

    public RoleDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    public void save(Role role) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("roles").document();
        role.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(role);
        result.get();
    }

    public Role getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("roles").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        Role role = document.toObject(Role.class);
        if (role != null && (role.getId() == null || role.getId().isEmpty())) {
            role.setId(document.getId());
        }
        return role;
    }

    public List<Role> getAll() throws ExecutionException, InterruptedException {
        List<Role> roles = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("roles").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Role role = document.toObject(Role.class);
            if (role != null && (role.getId() == null || role.getId().isEmpty())) {
                role.setId(document.getId());
            }
            roles.add(role);
        }
        return roles;
    }

    public void update(Role role) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("roles").document(role.getId());
        ApiFuture<WriteResult> result = docRef.set(role);
        result.get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("roles").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
