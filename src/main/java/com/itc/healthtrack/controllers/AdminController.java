package com.itc.healthtrack.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.itc.healthtrack.dao.DoctorDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.dao.RoleDAO;
import com.itc.healthtrack.dao.UserProfileDAO;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.Role;
import com.itc.healthtrack.models.UserProfile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del Panel de Administración
 * Gestiona la visualización de estadísticas de usuarios, búsqueda, eliminación y cambio de roles.
 */
public class AdminController {

    // Elementos de la interfaz
    @FXML private Label lblTotalUsers;      // Etiqueta para mostrar total de usuarios
    @FXML private Label lblTotalDoctors;    // Etiqueta para mostrar total de médicos
    @FXML private Label lblTotalPatients;   // Etiqueta para mostrar total de pacientes
    @FXML private Label lblStatus;          // Etiqueta para mensajes de estado

    // Tabla de columnas
    @FXML private TableView<UserProfile> tableUsers;                  // Tabla principal de usuarios
    @FXML private TableColumn<UserProfile, String> colFirstName;      // Columna: Nombre
    @FXML private TableColumn<UserProfile, String> colLastName;       // Columna: Apellido
    @FXML private TableColumn<UserProfile, String> colEmail;          // Columna: Correo
    @FXML private TableColumn<UserProfile, String> colRole;           // Columna: Rol
    @FXML private TableColumn<UserProfile, String> colAssignedDoctor; // Columna: Doctor Asignado
    @FXML private TextField txtSearch;                                // Campo de búsqueda
    @FXML private javafx.scene.control.ComboBox<String> cbRoleFilter; // Filtro por rol

    // Datos y controladores
    private final UserProfileDAO userProfileDAO = new UserProfileDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final ObservableList<UserProfile> usersObservableList = FXCollections.observableArrayList();
    private FilteredList<UserProfile> filteredList;

    // Mapas de apoyo para nombres y relaciones
    private final Map<String, String> roleNameById = new HashMap<>();
    private final Map<String, Patient> patientByProfileId = new HashMap<>();
    private final Map<String, Doctor> doctorByProfileId = new HashMap<>();
    private final Map<String, String> doctorNameByDoctorId = new HashMap<>();

    private UserProfile loggedInProfile;
    private UserProfile selectedUser = null;

    /*Inicializa el controlador con los datos del administrador logeado
      Configura la interfaz y carga la lista de usuarios  */
    public void initData(UserProfile profile, Role role, Doctor doctor, Patient patient) {
        this.loggedInProfile = profile;
        setupSearchControls();
        setupTable();
        loadAllUsers();
    }

    /*Configura el filtro de roles (Todos, Doctor, Paciente, Admin)
      Esto permite filtrar usuarios por su rol de forma rápida*/
    private void setupSearchControls() {
        cbRoleFilter.setItems(FXCollections.observableArrayList("Todos", "patient", "doctor", "admin"));
        cbRoleFilter.setValue("Todos");
    }

    /*Configura las columnas de la tabla para mostrar datos del perfil
      También configura el escuchador para detectar cuando se selecciona un usuario*/
    private void setupTable() {
        colFirstName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                getFirstName(cellData.getValue())));
        colLastName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                getLastName(cellData.getValue())));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                getRoleName(cellData.getValue())));
        colAssignedDoctor.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                getAssignedDoctorName(cellData.getValue())));

        filteredList = new FilteredList<>(usersObservableList, u -> true);
        tableUsers.setItems(filteredList);

        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                lblStatus.setText("Usuario seleccionado: " + getFullName(newVal));
                lblStatus.setTextFill(Color.web("#aaaaaa"));
            }
        });
    }

    /*Carga todos los usuarios desde la base de datos (Firestore) en un hilo de fondo.
      Luego actualiza la tabla y las estadísticas en el hilo principal de la interfaz.*/
    private void loadAllUsers() {
        new Thread(() -> {
            try {
                List<UserProfile> profiles = userProfileDAO.getAllUserProfiles();
                List<Role> roles = roleDAO.getAllRoles();
                List<Patient> patients = patientDAO.getAllPatients();
                List<Doctor> doctors = doctorDAO.getAllDoctors();

                roleNameById.clear();
                patientByProfileId.clear();
                doctorByProfileId.clear();
                doctorNameByDoctorId.clear();

                for (Role role : roles) {
                    roleNameById.put(role.getId(), role.getName());
                }

                for (Patient patient : patients) {
                    patientByProfileId.put(patient.getUserProfileId(), patient);
                }

                for (Doctor doctor : doctors) {
                    doctorByProfileId.put(doctor.getUserProfileId(), doctor);
                    doctorNameByDoctorId.put(doctor.getId(), doctor.getFirstName() + " " + doctor.getLastName());
                }

                int doctorCount = 0;
                int patientCount = 0;
                for (UserProfile profile : profiles) {
                    String roleName = roleNameById.get(profile.getRoleId());
                    if ("doctor".equals(roleName)) {
                        doctorCount++;
                    } else if ("patient".equals(roleName)) {
                        patientCount++;
                    }
                }

                int finalDoctorCount = doctorCount;
                int finalPatientCount = patientCount;

                Platform.runLater(() -> {
                    usersObservableList.clear();
                    usersObservableList.addAll(profiles);
                    lblTotalUsers.setText(String.valueOf(profiles.size()));
                    lblTotalDoctors.setText(String.valueOf(finalDoctorCount));
                    lblTotalPatients.setText(String.valueOf(finalPatientCount));
                    applyFilter();
                    lblStatus.setText("Lista actualizada\n" + profiles.size() + " usuario(s) encontrado(s)");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Error al cargar la lista de usuarios");
                    lblStatus.setTextFill(Color.web("#ff5252"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    /* Se ejecuta cuando el usuario hace clic en el botón "Buscar"
      Aplica los filtros de búsqueda y rol a la tabla*/
    @FXML
    protected void onSearch() {
        applyFilter();
    }

    // Aplica filtros a la tabla
    private void applyFilter() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String roleFilter = cbRoleFilter.getValue();

        filteredList.setPredicate(profile -> {
            String roleName = getRoleName(profile);
            boolean roleMatch = "Todos".equals(roleFilter) || roleFilter.equals(roleName);

            String fullName = (getFirstName(profile) + " " + getLastName(profile)).toLowerCase();
            boolean textMatch = keyword.isEmpty()
                    || fullName.contains(keyword)
                    || (profile.getEmail() != null && profile.getEmail().toLowerCase().contains(keyword));

            return roleMatch && textMatch;
        });

        lblStatus.setText("Mostrando " + filteredList.size() + " de " + usersObservableList.size() + " usuario(s).");
        lblStatus.setTextFill(Color.web("#aaaaaa"));
    }

    /* Se ejecuta cuando el usuario hace clic en el botón "Limpiar"
      Limpia el campo de búsqueda y resetea el filtro a "Todos"*/
    @FXML
    protected void onClearSearch() {
        txtSearch.clear();
        cbRoleFilter.setValue("Todos");
        applyFilter();
    }

    /* Abre un diálogo que muestra todos los detalles del usuario seleccionado.
      Muestra información diferente según el rol (paciente, médico, admin)  */
    @FXML
    protected void onViewDetails() {
        if (selectedUser == null) {
            lblStatus.setText("Selecciona un usuario de la tabla para ver sus detalles.");
            lblStatus.setTextFill(Color.web("#ffffff"));
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle del Usuario");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPrefWidth(440);
        grid.setStyle("-fx-background-color: #ffffff; -fx-padding: 20;");

        Label lblHeader = new Label(getFullName(selectedUser));
        lblHeader.setStyle("-fx-text-fill: #000000; -fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane.setColumnSpan(lblHeader, 2);
        grid.add(lblHeader, 0, 0);

        int row = 1;
        row = addDetailRow(grid, row, "Perfil ID", selectedUser.getId());
        row = addDetailRow(grid, row, "Correo", selectedUser.getEmail());
        row = addDetailRow(grid, row, "Rol", getRoleName(selectedUser));

        Patient patient = patientByProfileId.get(selectedUser.getId());
        Doctor doctor = doctorByProfileId.get(selectedUser.getId());

        if (patient != null) {
            row = addDetailRow(grid, row, "Nacimiento", patient.getBirthDate());
            row = addDetailRow(grid, row, "Género", patient.getGender());
            row = addDetailRow(grid, row, "Estatura", patient.getHeight() != null ? patient.getHeight() + " m" : "—");
            row = addDetailRow(grid, row, "Doctor ID", patient.getPrimaryDoctorId());
        }

        if (doctor != null) {
            row = addDetailRow(grid, row, "Licencia", doctor.getLicenseNumber());
            row = addDetailRow(grid, row, "Especialidad ID", doctor.getSpecialtyId());
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(300);
        scrollPane.setStyle("-fx-background: #ffffff; -fx-background-color: #ffffff; -fx-border-color: rgb(255 255 255 / 0);");

        DialogPane dp = dialog.getDialogPane();
        dp.setContent(scrollPane);
        dp.setHeader(null);
        dp.setGraphic(null);
        dp.getButtonTypes().add(ButtonType.CLOSE);
        dp.setStyle("-fx-background-color: #ffffff;");

        ((Button) dp.lookupButton(ButtonType.CLOSE))
                .setStyle("-fx-background-color: #6a6a6a; -fx-text-fill: #020000; -fx-cursor: hand; -fx-padding: 6 20;");

        dialog.showAndWait();
    }

    /*Metodo auxiliar que agrega una fila de detalle al GridPane.
     Cada fila tiene una etiqueta (label) y un valor (value).*/
    private int addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-weight: bold;");

        Label val = new Label(value != null && !value.isBlank() ? value : "—");
        val.setStyle("-fx-text-fill: #000000;");
        val.setWrapText(true);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
        return row + 1;
    }

    /*Elimina un usuario y sus registros relacionados.
      También elimina el usuario en Firebase Authentication.*/
    @FXML
    protected void onDeleteUser() {
        if (selectedUser == null) {
            lblStatus.setText("Selecciona un usuario de la tabla para eliminar");
            lblStatus.setTextFill(Color.web("#ff5252"));
            return;
        }

        if (loggedInProfile != null && selectedUser.getId().equals(loggedInProfile.getId())) {
            lblStatus.setText("No puedes eliminar tu propia cuenta de administrador");
            lblStatus.setTextFill(Color.web("#ff9800"));
            return;
        }

        String profileId = selectedUser.getId();
        lblStatus.setText("Eliminando usuario: " + getFullName(selectedUser) + "...");
        lblStatus.setTextFill(Color.web("#ffffff"));

        new Thread(() -> {
            try {
                String roleName = getRoleName(selectedUser);
                Patient patient = patientByProfileId.get(profileId);
                Doctor doctor = doctorByProfileId.get(profileId);

                if ("doctor".equals(roleName) && doctor != null) {
                    reassignPatientsFromDoctor(doctor.getId());
                    doctorDAO.deleteDoctor(doctor.getId());
                }

                if ("patient".equals(roleName) && patient != null) {
                    patientDAO.deletePatient(patient.getId());
                }

                userProfileDAO.deleteUserProfile(profileId);

                if (selectedUser.getAuthUid() != null && !selectedUser.getAuthUid().isBlank()) {
                    FirebaseAuth.getInstance().deleteUser(selectedUser.getAuthUid());
                }

                Platform.runLater(() -> {
                    selectedUser = null;
                    tableUsers.getSelectionModel().clearSelection();
                    loadAllUsers();
                    lblStatus.setText("Usuario eliminado correctamente");
                    lblStatus.setTextFill(Color.web("#4caf50"));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Error al eliminar el usuario, intenta de nuevo");
                    lblStatus.setTextFill(Color.web("#ff5252"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    // Cambiar rol de un usuario
    @FXML
    protected void onChangeRole() {
        if (selectedUser == null) {
            lblStatus.setText("Selecciona un usuario primero");
            lblStatus.setTextFill(Color.web("#ff5252"));
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(getRoleName(selectedUser),
                "patient", "doctor", "admin");
        dialog.setTitle("Cambiar Rol");
        dialog.setHeaderText("Usuario: " + getFullName(selectedUser));
        dialog.setContentText("Selecciona el nuevo rol:");
        dialog.getDialogPane().setStyle("-fx-background-color: #1e1e1e; -fx-font-size: 13px;");

        dialog.showAndWait().ifPresent(newRoleName -> {
            if (newRoleName.equals(getRoleName(selectedUser))) {
                return;
            }

            new Thread(() -> {
                try {
                    Role role = roleDAO.getRoleByName(newRoleName);
                    if (role == null) {
                        Role newRole = new Role();
                        newRole.setName(newRoleName);
                        newRole.setDescription("Rol generado automaticamente");
                        roleDAO.saveRole(newRole);
                        role = newRole;
                    }

                    userProfileDAO.updateUserRole(selectedUser.getId(), role.getId());

                    if ("patient".equals(newRoleName)) {
                        Patient patient = patientDAO.getPatientByUserProfileId(selectedUser.getId());
                        if (patient == null) {
                            Patient newPatient = new Patient();
                            newPatient.setUserProfileId(selectedUser.getId());
                            newPatient.setFirstName(getFirstName(selectedUser));
                            newPatient.setLastName(getLastName(selectedUser));
                            patientDAO.savePatient(newPatient);
                        }
                    } else if ("doctor".equals(newRoleName)) {
                        Doctor doctor = doctorDAO.getDoctorByUserProfileId(selectedUser.getId());
                        if (doctor == null) {
                            Doctor newDoctor = new Doctor();
                            newDoctor.setUserProfileId(selectedUser.getId());
                            newDoctor.setFirstName(getFirstName(selectedUser));
                            newDoctor.setLastName(getLastName(selectedUser));
                            newDoctor.setLicenseNumber("PENDING");
                            doctorDAO.saveDoctor(newDoctor);
                        }
                    }

                    Platform.runLater(() -> {
                        loadAllUsers();
                        lblStatus.setText("Rol actualizado a: " + newRoleName);
                        lblStatus.setTextFill(Color.web("#4caf50"));
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblStatus.setText("Error al actualizar el rol");
                        lblStatus.setTextFill(Color.web("#ff5252"));
                    });
                    e.printStackTrace();
                }
            }).start();
        });
    }

    // Reasigna pacientes cuando se elimina un doctor
    private void reassignPatientsFromDoctor(String doctorId) throws Exception {
        List<Patient> patients = patientDAO.getPatientsByDoctorId(doctorId);
        List<Doctor> doctors = doctorDAO.getAllDoctors();
        List<Doctor> availableDoctors = new ArrayList<>();

        // Se crea una lista sin el doctor que será eliminado
        for (Doctor doctor : doctors) {
            if (!doctor.getId().equals(doctorId)) {
                availableDoctors.add(doctor);
            }
        }

        if (!availableDoctors.isEmpty()) {
            int doctorIndex = 0;
            for (Patient patient : patients) {
                Doctor newDoctor = availableDoctors.get(doctorIndex % availableDoctors.size());
                patientDAO.assignDoctorToPatient(patient.getId(), newDoctor.getId());
                doctorIndex++;
            }
        } else {
            for (Patient patient : patients) {
                patientDAO.assignDoctorToPatient(patient.getId(), null);
            }
        }
    }

    // Obtiene el nombre del rol usando el mapa de roles
    private String getRoleName(UserProfile profile) {
        String roleName = roleNameById.get(profile.getRoleId());
        return roleName != null ? roleName : "—";
    }

    // Obtiene el nombre completo para mostrarlo en tabla o dialogos
    private String getFullName(UserProfile profile) {
        return getFirstName(profile) + " " + getLastName(profile);
    }

    // Obtiene el nombre usando los registros de paciente o doctor
    private String getFirstName(UserProfile profile) {
        Patient patient = patientByProfileId.get(profile.getId());
        if (patient != null && patient.getFirstName() != null) {
            return patient.getFirstName();
        }

        Doctor doctor = doctorByProfileId.get(profile.getId());
        if (doctor != null && doctor.getFirstName() != null) {
            return doctor.getFirstName();
        }
        return "—";
    }

    // Obtiene el apellido usando los registros de paciente o doctor
    private String getLastName(UserProfile profile) {
        Patient patient = patientByProfileId.get(profile.getId());
        if (patient != null && patient.getLastName() != null) {
            return patient.getLastName();
        }

        Doctor doctor = doctorByProfileId.get(profile.getId());
        if (doctor != null && doctor.getLastName() != null) {
            return doctor.getLastName();
        }
        return "—";
    }

    // Obtiene el nombre del doctor asignado para un paciente
    private String getAssignedDoctorName(UserProfile profile) {
        Patient patient = patientByProfileId.get(profile.getId());
        if (patient != null && patient.getPrimaryDoctorId() != null) {
            String doctorName = doctorNameByDoctorId.get(patient.getPrimaryDoctorId());
            return doctorName != null ? doctorName : "—";
        }
        return "—";
    }
}
