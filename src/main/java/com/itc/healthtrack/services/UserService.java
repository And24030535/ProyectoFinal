package com.itc.healthtrack.services;

import com.itc.healthtrack.dao.GenericDAO;
import com.itc.healthtrack.models.User;

import java.util.ArrayList;
import java.util.List;

// centraliza la logica para obtener la lista de pacientes visibles para cada usuario
public class UserService {

    private final GenericDAO<User> userDao = new GenericDAO<>(User.class, "users");

    // devuelve los pacientes que el usuario logeado puede ver
    // si es admin ve todos y si es medico solo ve los suyos
    public List<User> getPatientsForUser(User viewer) throws Exception {
        if (viewer == null) {
            return new ArrayList<>();
        }

        if ("admin".equals(viewer.getRole())) {
            return userDao.getByField("role", "patient");
        }

        if (viewer.getUid() == null || viewer.getUid().isBlank()) {
            return new ArrayList<>();
        }

        // para medicos consultamos solo sus asignados y despues filtramos pacientes en memoria
        // evita recorrer toda la coleccion de pacientes cuando el usuario no es admin
        List<User> assignedUsers = userDao.getByField("assignedDoctorId", viewer.getUid());
        List<User> visiblePatients = new ArrayList<>();
        for (User user : assignedUsers) {
            if ("patient".equals(user.getRole())) {
                visiblePatients.add(user);
            }
        }
        return visiblePatients;
    }
}
