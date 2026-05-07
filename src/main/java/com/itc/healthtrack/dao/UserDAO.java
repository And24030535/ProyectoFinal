package com.itc.healthtrack.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.itc.healthtrack.config.FirebaseConnection;
import com.itc.healthtrack.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

//Gestiona las operaciones de lectura y escritura para la coleccion 'users'
public class UserDAO {

    // Instancia de Firestore desde la configuración de Firebase
    private final Firestore db;

    //Constructor que inicializa la conexión a Firestore
    public UserDAO() {
        this.db = FirebaseConnection.getInstance().getFirestore();
    }

    //Valida el acceso de un usuario verificando que el correo y la contraseña coincidan.
    public User authenticateUser(String email, String password) throws ExecutionException, InterruptedException {
        CollectionReference usersRef = db.collection("users");

        // Exige que ambos campos sean exactamente iguales a los de la base de datos
        Query query = usersRef.whereEqualTo("email", email)
                .whereEqualTo("password", password);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            DocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
            return document.toObject(User.class);
        }
        return null; // Retorna nulo si las credenciales son incorrectas
    }

    // Guardado y recuperación

    //Guarda un nuevo usuario en la base de datos
    public void saveUser(User user) throws ExecutionException, InterruptedException {
        // Crea una referencia a un nuevo documento con ID autogenerado
        DocumentReference docRef = db.collection("users").document();
        user.setUid(docRef.getId()); // Asigna el ID autogenerado al objeto

        // Guarda el objeto completo en la base de datos
        ApiFuture<WriteResult> result = docRef.set(user);
        result.get(); // Espera a que la operacion finalice
    }

    // Obtiene todos los usuarios registrados en el sistema, sin importar su rol
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        List<User> userList = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("users").get();

        // Convierte cada documento en un objeto User de Java
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User user = document.toObject(User.class);
            // Garantiza que el UID siempre tenga el ID real del documento
            if (user != null && (user.getUid() == null || user.getUid().isEmpty())) {
                user.setUid(document.getId());
            }
            userList.add(user);
        }
        return userList;
    }

    //Obtiene todos los usuarios que tienen un rol específico
    public List<User> getUsersByRole(String role) throws ExecutionException, InterruptedException {
        List<User> userList = new ArrayList<>();

        // Consulta: buscar donde el rol coincida
        Query query = db.collection("users").whereEqualTo("role", role);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        // Convierte cada documento en un objeto User de Java
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            User user = document.toObject(User.class);

            // Garantiza que el UID siempre tenga el ID real del documento
            if (user != null && (user.getUid() == null || user.getUid().isEmpty())) {
                user.setUid(document.getId());
            }
            userList.add(user);
        }
        return userList;
    }

    // Eliminación y actualización

    //Elimina permanentemente un usuario de la base de datos, solo debe ser usado por administradores
    public void deleteUser(String uid) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(uid);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get(); // Espera a que la operación finalice
    }

    //Actualiza el rol de un usuario existente
    public void updateUserRole(String uid, String newRole) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.update("role", newRole).get(); // Actualiza solo el campo 'role'
    }

    /*Elimina un médico y automáticamente reasigna todos sus pacientes
     Si hay otros médicos disponibles: distribuye los pacientes entre ellos
     Si no hay otros médicos: desasigna los pacientes (quedan sin médico)
     pOR ULTIMO elimina el médico de la base de datos*/
    public void deleteDoctorAndReassignPatients(String doctorId) throws ExecutionException, InterruptedException {
        PatientDAO patientDAO = new PatientDAO();

        // Obtiene todos los pacientes del doctor que será eliminado
        List<User> patientsList = patientDAO.getPatientsByDoctor(doctorId);

        // Obtiene todos los médicos disponibles (excepto el que será eliminado)
        List<User> availableDoctors = patientDAO.getAllDoctors();
        availableDoctors.removeIf(doctor -> doctor.getUid().equals(doctorId));

        // Reasigna los pacientes
        if (!availableDoctors.isEmpty()) {
            // Si hay otros médicos, distribuye los pacientes entre ellos de forma circular
            int doctorIndex = 0;
            for (User patient : patientsList) {
                User newDoctor = availableDoctors.get(doctorIndex % availableDoctors.size());
                patientDAO.assignDoctorToPatient(patient.getUid(), newDoctor.getUid());
                doctorIndex++;
            }
        } else {
            // Si no hay otros médicos, desasigna los pacientes (los deja sin médico)
            for (User patient : patientsList) {
                DocumentReference patientRef = db.collection("users").document(patient.getUid());
                patientRef.update("assignedDoctorId", FieldValue.delete()).get();
            }
        }

        // Finalmente, elimina el médico de la base de datos
        deleteUser(doctorId);
    }
}