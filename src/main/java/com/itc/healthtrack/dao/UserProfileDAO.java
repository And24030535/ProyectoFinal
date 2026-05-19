package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion userProfiles
public class UserProfileDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public UserProfileDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo perfil de usuario en Firestore
    public void saveUserProfile(UserProfile profile) throws ExecutionException, InterruptedException {
        // Se crea un documento vacio para obtener un ID autogenerado
        DocumentReference docRef = db.collection("userProfiles").document();
        profile.setId(docRef.getId()); // Se asigna el ID al objeto en memoria

        // Se guarda el objeto completo en la base de datos
        ApiFuture<WriteResult> result = docRef.set(profile);
        result.get(); // Se espera a que la operacion termine
    }

    // Obtiene un perfil por su ID
    public UserProfile getUserProfileById(String profileId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("userProfiles").document(profileId);
        ApiFuture<DocumentSnapshot> result = docRef.get();
        DocumentSnapshot snapshot = result.get();

        if (snapshot.exists()) {
            UserProfile profile = snapshot.toObject(UserProfile.class);
            if (profile != null) {
                profile.setId(snapshot.getId());
            }
            return profile;
        }
        return null;
    }

    // Obtiene un perfil usando el UID de Firebase Auth
    public UserProfile getUserProfileByAuthUid(String authUid) throws ExecutionException, InterruptedException {
        Query query = db.collection("userProfiles").whereEqualTo("authUid", authUid);
        ApiFuture<QuerySnapshot> result = query.get();

        if (!result.get().getDocuments().isEmpty()) {
            DocumentSnapshot snapshot = result.get().getDocuments().get(0);
            UserProfile profile = snapshot.toObject(UserProfile.class);
            if (profile != null) {
                profile.setId(snapshot.getId());
            }
            return profile;
        }
        return null;
    }

    // Obtiene todos los perfiles registrados
    public List<UserProfile> getAllUserProfiles() throws ExecutionException, InterruptedException {
        List<UserProfile> profiles = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("userProfiles").get();

        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            UserProfile profile = snapshot.toObject(UserProfile.class);
            if (profile != null) {
                profile.setId(snapshot.getId());
                profiles.add(profile);
            }
        }
        return profiles;
    }

    // Actualiza un perfil completo
    public void updateUserProfile(UserProfile profile) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("userProfiles").document(profile.getId());
        ApiFuture<WriteResult> result = docRef.set(profile);
        result.get();
    }

    // Actualiza solamente el rol de un perfil
    public void updateUserRole(String profileId, String roleId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("userProfiles").document(profileId);
        ApiFuture<WriteResult> result = docRef.update("roleId", roleId);
        result.get();
    }

    // Elimina un perfil por su ID
    public void deleteUserProfile(String profileId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("userProfiles").document(profileId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
    }
}
