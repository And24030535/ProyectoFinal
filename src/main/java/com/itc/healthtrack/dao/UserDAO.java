package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages CRUD operations for the users collection
public class UserDAO {

    private final Firestore db;

    public UserDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Validates login using email and password in the users collection
    public User authenticateUser(String email, String password) throws ExecutionException, InterruptedException {
        Query query = db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (!querySnapshot.get().getDocuments().isEmpty()) {
            DocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
            User user = document.toObject(User.class);
            if (user != null && (user.getId() == null || user.getId().isEmpty())) {
                user.setId(document.getId());
            }
            return user;
        }
        return null;
    }

    // Create
    public void save(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document();
        user.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(user);
        result.get();
    }

    // Read
    public User getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("users").document(id).get().get();
        if (!document.exists()) {
            return null;
        }
        User user = document.toObject(User.class);
        if (user != null && (user.getId() == null || user.getId().isEmpty())) {
            user.setId(document.getId());
        }
        return user;
    }

    public List<User> getAll() throws ExecutionException, InterruptedException {
        List<User> userList = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("users").get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User user = document.toObject(User.class);
            if (user != null && (user.getId() == null || user.getId().isEmpty())) {
                user.setId(document.getId());
            }
            userList.add(user);
        }
        return userList;
    }

    public List<User> getByRoleId(String roleId) throws ExecutionException, InterruptedException {
        List<User> userList = new ArrayList<>();
        Query query = db.collection("users").whereEqualTo("roleId", roleId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User user = document.toObject(User.class);
            if (user != null && (user.getId() == null || user.getId().isEmpty())) {
                user.setId(document.getId());
            }
            userList.add(user);
        }
        return userList;
    }

    // Update
    public void update(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(user.getId());
        ApiFuture<WriteResult> result = docRef.set(user);
        result.get();
    }

    public void updateUserRole(String id, String newRoleId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(id);
        docRef.update("roleId", newRoleId).get();
    }

    // Delete
    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(id);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
