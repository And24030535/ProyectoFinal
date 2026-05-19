package com.itc.healthtrack.controllers;

import com.google.cloud.Timestamp;
import com.itc.healthtrack.dao.DoctorDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.dao.UserDAO;
import com.itc.healthtrack.dto.UserSession;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la gestion de pacientes. Permite registrar, editar,
 * visualizar y eliminar los pacientes asignados a un medico.
 */
public class PatientsController {

    // Componentes del formulario
    @FXML private TextField txtFirstName, txtLastName, txtEmail, txtHeight;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> comboGender;
    @FXML private DatePicker dpBirthDate;

    // Componentes de la tabla
    @FXML private TableView<UserSession> tablePatients;
    @FXML private TableColumn<UserSession, String> colFirstName, colLastName, colEmail, colGender;

    // Variables de logica de negocio
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final UserDAO userDAO = new UserDAO();
    private ObservableList<UserSession> patientsObservableList = FXCollections.observableArrayList();

    private UserSession loggedInDoctor;
    private UserSession selectedPatient = null;

    // Metodo de inicializacion que recibe el medico actualmente logueado
    public void initData(UserSession doctor) {
        this.loggedInDoctor = doctor;
        setupTable();

        // Inicializar opciones del combo box
        comboGender.setItems(FXCollections.observableArrayList("M", "F", "Other"));

        loadPatients();
    }

    //Carga los pacientes desde Firestore y llena el formulario al seleccionar uno
    private void setupTable() {
        // Vincula cada columna con su propiedad correspondiente del modelo UserSession
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        tablePatients.setItems(patientsObservableList);

        // Cuando se selecciona un paciente de la tabla, llena el formulario
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
                List<Patient> patients = "admin".equals(loggedInDoctor.getRoleId())
                        ? patientDAO.getAll()
                        : patientDAO.getPatientsByDoctor(loggedInDoctor.getDoctorId());
                List<UserSession> sessions = buildPatientSessions(patients);
                Platform.runLater(() -> {
                    patientsObservableList.clear();
                    patientsObservableList.addAll(sessions);
                });
            } catch (Exception e) {
                System.out.println("Error al cargar pacientes.");
            }
        }).start();
    }

    private List<UserSession> buildPatientSessions(List<Patient> patients) throws Exception {
        List<UserSession> sessions = new ArrayList<>();
        for (Patient patient : patients) {
            User user = userDAO.getById(patient.getUserId());
            if (user != null) {
                sessions.add(new UserSession(user, patient, null));
            }
        }
        return sessions;
    }

    @FXML
    protected void onSavePatient() {
        try {
            if (selectedPatient == null) {
                // Si no hay ninguno seleccionado, creamos uno nuevo
                User newUser = new User();
                newUser.setEmail(txtEmail.getText());
                if (!txtPassword.getText().isEmpty()) {
                    newUser.setPassword(txtPassword.getText());
                }
                newUser.setRoleId("patient");
                newUser.setRegisteredAt(Timestamp.now());

                Patient newPatient = new Patient();
                newPatient.setFirstName(txtFirstName.getText());
                newPatient.setLastName(txtLastName.getText());
                newPatient.setBirthDate(dpBirthDate.getValue() != null ? dpBirthDate.getValue().toString() : null);
                newPatient.setGender(comboGender.getValue());
                newPatient.setHeight(Double.parseDouble(txtHeight.getText()));

                // Guardamos en un hilo nuevo
                new Thread(() -> {
                    try {
                        String doctorId = null;
                        if ("doctor".equals(loggedInDoctor.getRoleId())) {
                            doctorId = loggedInDoctor.getDoctorId();
                        } else {
                            List<Doctor> doctors = doctorDAO.getAll();
                            if (!doctors.isEmpty()) {
                                Doctor randomDoctor = doctors.get((int) (Math.random() * doctors.size()));
                                doctorId = randomDoctor.getId();
                            }
                        }
                        newPatient.setPrimaryDoctorId(doctorId);

                        userDAO.save(newUser);
                        newPatient.setUserId(newUser.getId());
                        patientDAO.save(newPatient);
                        Platform.runLater(() -> {
                            onClearForm();
                            loadPatients();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                // Actualizar paciente existente
                Patient patient = selectedPatient.getPatient();
                User user = selectedPatient.getUser();
                if (patient == null || user == null) {
                    return;
                }

                patient.setFirstName(txtFirstName.getText());
                patient.setLastName(txtLastName.getText());
                patient.setBirthDate(dpBirthDate.getValue() != null ? dpBirthDate.getValue().toString() : null);
                patient.setGender(comboGender.getValue());
                patient.setHeight(Double.parseDouble(txtHeight.getText()));

                user.setEmail(txtEmail.getText());
                if (!txtPassword.getText().isEmpty()) {
                    user.setPassword(txtPassword.getText());
                }

                new Thread(() -> {
                    try {
                        userDAO.update(user);
                        patientDAO.update(patient);
                        Platform.runLater(() -> {
                            onClearForm();
                            loadPatients();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Asegurate de que la altura sea un numero.");
        }
    }

    //Elimina el paciente seleccionado de la base de datos
    @FXML
    protected void onDeletePatient() {
        if (selectedPatient != null) {
            new Thread(() -> {
                try {
                    if (selectedPatient.getPatientId() != null) {
                        patientDAO.delete(selectedPatient.getPatientId());
                    }
                    if (selectedPatient.getUserId() != null) {
                        userDAO.delete(selectedPatient.getUserId());
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
    private void fillForm(UserSession p) {
        txtFirstName.setText(p.getFirstName());
        txtLastName.setText(p.getLastName());
        txtEmail.setText(p.getEmail());
        dpBirthDate.setValue(p.getBirthDate() != null ? LocalDate.parse(p.getBirthDate()) : null);
        comboGender.setValue(p.getGender());
        txtHeight.setText(p.getHeight() != null ? String.valueOf(p.getHeight()) : "");
        txtPassword.clear();
    }
}
