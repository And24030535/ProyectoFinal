package com.itc.healthtrack.controllers;

import com.google.cloud.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
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
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestion de pacientes. Permite registrar, editar,
 * visualizar y eliminar los pacientes asignados a un medico.
 */
public class PatientsController {

    // Componentes del formulario
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtHeight;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> comboGender;
    @FXML private DatePicker dpBirthDate;

    // Componentes de la tabla
    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient, String> colFirstName;
    @FXML private TableColumn<Patient, String> colLastName;
    @FXML private TableColumn<Patient, String> colEmail;
    @FXML private TableColumn<Patient, String> colGender;

    // Acceso a datos
    private final PatientDAO patientDAO = new PatientDAO();
    private final UserProfileDAO userProfileDAO = new UserProfileDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private ObservableList<Patient> patientsObservableList = FXCollections.observableArrayList();
    private Map<String, UserProfile> userProfileById = new HashMap<>();

    private UserProfile loggedInProfile;
    private Role loggedInRole;
    private Doctor loggedInDoctor;
    private Patient selectedPatient = null;

    // Metodo de inicializacion que recibe el medico actualmente logueado
    public void initData(UserProfile profile, Role role, Doctor doctor, Patient patient) {
        this.loggedInProfile = profile;
        this.loggedInRole = role;
        this.loggedInDoctor = doctor;

        setupTable();
        comboGender.setItems(FXCollections.observableArrayList("M", "F", "Other"));
        loadPatients();
    }

    //Carga los pacientes desde Firestore y llena el formulario al seleccionar uno
    private void setupTable() {
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colEmail.setCellValueFactory(cellData -> {
            UserProfile profile = userProfileById.get(cellData.getValue().getUserProfileId());
            String email = profile != null ? profile.getEmail() : "";
            return new javafx.beans.property.SimpleStringProperty(email);
        });

        tablePatients.setItems(patientsObservableList);

        tablePatients.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPatient = newSelection;
                fillForm(newSelection);
            }
        });
    }

    //Carga los pacientes del medico desde Firestore en un hilo secundario
    private void loadPatients() {
        new Thread(() -> {
            try {
                List<Patient> list;
                if (loggedInRole != null && "admin".equals(loggedInRole.getName())) {
                    list = patientDAO.getAllPatients();
                } else if (loggedInDoctor != null) {
                    list = patientDAO.getPatientsByDoctorId(loggedInDoctor.getId());
                } else {
                    list = new ArrayList<>();
                }

                Map<String, UserProfile> profileMap = new HashMap<>();
                for (Patient patient : list) {
                    UserProfile profile = userProfileDAO.getUserProfileById(patient.getUserProfileId());
                    if (profile != null) {
                        profileMap.put(patient.getUserProfileId(), profile);
                    }
                }

                Platform.runLater(() -> {
                    userProfileById = profileMap;
                    patientsObservableList.clear();
                    patientsObservableList.addAll(list);
                });
            } catch (Exception e) {
                System.out.println("Error al cargar pacientes.");
            }
        }).start();
    }

    @FXML
    protected void onSavePatient() {
        try {
            if (selectedPatient == null) {
                // Si no hay ninguno seleccionado, creamos uno nuevo
                createNewPatient();
            } else {
                // Actualizar paciente existente
                updateSelectedPatient();
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Asegurate de que la altura sea un numero.");
        }
    }

    // Crea un paciente nuevo junto con su perfil y su cuenta de Auth
    private void createNewPatient() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Correo y contraseña son obligatorios para crear un paciente.");
            return;
        }

        new Thread(() -> {
            try {
                // Crear el usuario en Firebase Authentication
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(email)
                        .setPassword(password);
                UserRecord authUser = FirebaseAuth.getInstance().createUser(request);

                // Obtener o crear el rol de paciente
                Role patientRole = roleDAO.getRoleByName("patient");
                if (patientRole == null) {
                    Role newRole = new Role();
                    newRole.setName("patient");
                    newRole.setDescription("Rol generado automaticamente");
                    roleDAO.saveRole(newRole);
                    patientRole = newRole;
                }

                // Crear el perfil basico del usuario
                UserProfile profile = new UserProfile();
                profile.setAuthUid(authUser.getUid());
                profile.setRoleId(patientRole.getId());
                profile.setEmail(email);
                profile.setRegisteredAt(Timestamp.now());
                userProfileDAO.saveUserProfile(profile);

                // Crear el documento del paciente
                Patient newPatient = new Patient();
                newPatient.setUserProfileId(profile.getId());
                newPatient.setFirstName(txtFirstName.getText());
                newPatient.setLastName(txtLastName.getText());
                newPatient.setBirthDate(dpBirthDate.getValue() != null ? dpBirthDate.getValue().toString() : null);
                newPatient.setGender(comboGender.getValue());
                newPatient.setHeight(parseHeight(txtHeight.getText()));

                if (loggedInDoctor != null) {
                    newPatient.setPrimaryDoctorId(loggedInDoctor.getId());
                } else if (loggedInRole != null && "admin".equals(loggedInRole.getName())) {
                    List<Doctor> doctors = doctorDAO.getAllDoctors();
                    if (!doctors.isEmpty()) {
                        Doctor randomDoctor = doctors.get((int) (Math.random() * doctors.size()));
                        newPatient.setPrimaryDoctorId(randomDoctor.getId());
                    }
                }

                patientDAO.savePatient(newPatient);

                Platform.runLater(() -> {
                    onClearForm();
                    loadPatients();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Actualiza un paciente existente y su perfil de usuario
    private void updateSelectedPatient() {
        selectedPatient.setFirstName(txtFirstName.getText());
        selectedPatient.setLastName(txtLastName.getText());
        selectedPatient.setBirthDate(dpBirthDate.getValue() != null ? dpBirthDate.getValue().toString() : null);
        selectedPatient.setGender(comboGender.getValue());
        selectedPatient.setHeight(parseHeight(txtHeight.getText()));

        new Thread(() -> {
            try {
                patientDAO.updatePatient(selectedPatient);

                UserProfile profile = userProfileById.get(selectedPatient.getUserProfileId());
                if (profile != null) {
                    String newEmail = txtEmail.getText().trim();
                    if (!newEmail.isEmpty() && !newEmail.equals(profile.getEmail())) {
                        profile.setEmail(newEmail);
                        userProfileDAO.updateUserProfile(profile);

                        if (profile.getAuthUid() != null) {
                            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(profile.getAuthUid())
                                    .setEmail(newEmail);
                            FirebaseAuth.getInstance().updateUser(updateRequest);
                        }
                    }
                }

                Platform.runLater(() -> {
                    onClearForm();
                    loadPatients();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Elimina el paciente seleccionado de la base de datos
    @FXML
    protected void onDeletePatient() {
        if (selectedPatient != null) {
            new Thread(() -> {
                try {
                    UserProfile profile = userProfileById.get(selectedPatient.getUserProfileId());
                    patientDAO.deletePatient(selectedPatient.getId());

                    if (profile != null) {
                        userProfileDAO.deleteUserProfile(profile.getId());
                        if (profile.getAuthUid() != null) {
                            FirebaseAuth.getInstance().deleteUser(profile.getAuthUid());
                        }
                    }

                    Platform.runLater(() -> {
                        onClearForm();
                        loadPatients();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    //Limpia todos los campos del formulario y deselecciona el paciente
    @FXML
    protected void onClearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        dpBirthDate.setValue(null);
        comboGender.setValue(null);
        txtHeight.clear();
        txtPassword.clear();

        selectedPatient = null;
        tablePatients.getSelectionModel().clearSelection();
    }

    /*Llena los campos del formulario con los datos del paciente seleccionado
     Se usa para editar un paciente existente*/
    private void fillForm(Patient patient) {
        txtFirstName.setText(patient.getFirstName());
        txtLastName.setText(patient.getLastName());
        dpBirthDate.setValue(patient.getBirthDate() != null ? LocalDate.parse(patient.getBirthDate()) : null);
        comboGender.setValue(patient.getGender());
        txtHeight.setText(patient.getHeight() != null ? String.valueOf(patient.getHeight()) : "");

        UserProfile profile = userProfileById.get(patient.getUserProfileId());
        txtEmail.setText(profile != null ? profile.getEmail() : "");
        txtPassword.clear();
    }

    // Convierte el texto de altura a Double
    private Double parseHeight(String heightText) {
        String trimmed = heightText == null ? "" : heightText.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return Double.parseDouble(trimmed);
    }
}
