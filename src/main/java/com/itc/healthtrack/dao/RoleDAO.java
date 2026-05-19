package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Gestiona las operaciones CRUD de la coleccion roles
public class RoleDAO {

    // Instancia de Firestore obtenida desde la configuracion central
    private final Firestore db;

    // Constructor que inicializa la conexion a Firestore
    public RoleDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    // Guarda un nuevo rol en Firestore
    public void saveRole(Role role) throws ExecutionException, InterruptedException {
        // Se crea un documento vacio para obtener un ID autogenerado
        DocumentReference docRef = db.collection("roles").document();
        role.setId(docRef.getId()); // Se asigna el ID al objeto en memoria

        // Se guarda el objeto completo en la base de datos
        ApiFuture<WriteResult> result = docRef.set(role);
        result.get(); // Se espera a que la operacion finalice
    }

    // Obtiene un rol por su ID
    public Role getRoleById(String roleId) throws ExecutionException, InterruptedException {
        // Se apunta al documento por su ID
        DocumentReference docRef = db.collection("roles").document(roleId);
        ApiFuture<DocumentSnapshot> result = docRef.get();
        DocumentSnapshot snapshot = result.get();

        // Se convierte el documento en objeto Role si existe
        if (snapshot.exists()) {
            Role role = snapshot.toObject(Role.class);
            if (role != null) {
                role.setId(snapshot.getId());
            }
            return role;
        }
        return null;
    }

    // Obtiene un rol por su nombre
    public Role getRoleByName(String name) throws ExecutionException, InterruptedException {
        // Se crea una consulta filtrando por el campo name
        Query query = db.collection("roles").whereEqualTo("name", name);
        ApiFuture<QuerySnapshot> result = query.get();

        // Si hay resultados, se toma el primero
        if (!result.get().getDocuments().isEmpty()) {
            DocumentSnapshot snapshot = result.get().getDocuments().get(0);
            Role role = snapshot.toObject(Role.class);
            if (role != null) {
                role.setId(snapshot.getId());
            }
            return role;
        }
        return null;
    }

    // Obtiene todos los roles disponibles
    public List<Role> getAllRoles() throws ExecutionException, InterruptedException {
        List<Role> roles = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("roles").get();

        // Se convierte cada documento en un objeto Role
        for (DocumentSnapshot snapshot : result.get().getDocuments()) {
            Role role = snapshot.toObject(Role.class);
            if (role != null) {
                role.setId(snapshot.getId());
                roles.add(role);
            }
        }
        return roles;
    }

    // Actualiza un rol existente
    public void updateRole(Role role) throws ExecutionException, InterruptedException {
        // Se apunta al documento usando el ID del rol
        DocumentReference docRef = db.collection("roles").document(role.getId());
        ApiFuture<WriteResult> result = docRef.set(role);
        result.get(); // Se espera la confirmacion
    }

    // Elimina un rol por su ID
    public void deleteRole(String roleId) throws ExecutionException, InterruptedException {
        // Se apunta al documento que se desea eliminar
        DocumentReference docRef = db.collection("roles").document(roleId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get(); // Se espera la eliminacion
    }
}
