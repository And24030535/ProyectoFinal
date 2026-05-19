package com.itc.healthtrack.controllers;

import com.google.cloud.Timestamp;
import com.itc.healthtrack.dao.DoctorDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.dao.UserDAO;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.List;

/*Controlador que gestiona el registro de nuevos usuarios
 Valida los datos, guarda en Firestore y asigna un doctor automáticamente para pacientes*/
public class RegisterController {

    // Elementos de formulario
    @FXML private TextField txtFirstName;           // Campo: Nombre
    @FXML private TextField txtLastName;            // Campo: Apellido
    @FXML private TextField txtEmail;               // Campo: Email
    @FXML private TextField txtHeight;              // Campo: Estatura
    @FXML private DatePicker dpBirthDate;           // Campo: Fecha de nacimiento
    @FXML private PasswordField txtPassword;         // Campo: Contraseña
    @FXML private PasswordField txtConfirmPassword;  // Campo: Confirmar contraseña
    @FXML private ComboBox<String> comboGender;     // ComboBox: Género
    @FXML private ComboBox<String> comboRole;       // ComboBox: Rol (Paciente/Doctor)
    @FXML private Label lblStatus;                  // Etiqueta de estado/errores
    @FXML private Button btnRegister;               // Botón de registro

    // Acceso a datos
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final UserDAO userDAO = new UserDAO();

    // Inicializa los ComboBox con opciones de género y rol
    @FXML
    public void initialize() {
        comboGender.getItems().addAll("M", "F", "Otro");
        comboRole.getItems().addAll("Paciente", "Doctor");
        comboRole.getSelectionModel().selectFirst();
    }

    /*Procesa el registro de un nuevo usuario.
     Valida todos los campos, guarda el usuario en Firestore y asigna un doctor automáticamente si es paciente*/
    @FXML
    protected void onRegister(ActionEvent event) {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String role = comboRole.getValue();

        // Mapear roles en español a inglés para consistencia con el sistema
        final String mappedRole;
        if ("Paciente".equals(role)) {
            mappedRole = "patient";
        } else if ("Doctor".equals(role)) {
            mappedRole = "doctor";
        } else if ("Admin".equals(role)) {
            mappedRole = "admin";
        } else {
            mappedRole = "patient"; // por defecto: paciente
        }

        // Validaciones básicas
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("Por favor, completa todos los campos obligatorios", false);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showStatus("Las contraseñas no coinciden", false);
            return;
        }

        if (password.length() < 6) {
            showStatus("La contraseña debe tener al menos 6 caracteres", false);
            return;
        }

        btnRegister.setDisable(true);

        new Thread(() -> {
            try {
                // Crear nuevo usuario
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setRoleId(mappedRole);
                newUser.setRegisteredAt(Timestamp.now());

                userDAO.save(newUser);

                // Crear perfil según el rol
                if ("patient".equals(mappedRole)) {
                    Patient patient = new Patient();
                    patient.setUserId(newUser.getId());
                    patient.setFirstName(firstName);
                    patient.setLastName(lastName);
                    patient.setGender(comboGender.getValue());
                    if (dpBirthDate.getValue() != null) {
                        patient.setBirthDate(dpBirthDate.getValue().toString());
                    }

                    String heightText = txtHeight.getText().trim();
                    if (!heightText.isEmpty()) {
                        try {
                            patient.setHeight(Double.parseDouble(heightText));
                        } catch (NumberFormatException e) {
                            Platform.runLater(() -> {
                                showStatus("Altura inválida.\n Verifica que sea un número decimal (ej: 1.75).", false);
                                btnRegister.setDisable(false);
                            });
                            return;
                        }
                    }

                    List<Doctor> doctors = doctorDAO.getAll();
                    if (!doctors.isEmpty()) {
                        Doctor randomDoctor = doctors.get((int) (Math.random() * doctors.size()));
                        patient.setPrimaryDoctorId(randomDoctor.getId());
                        System.out.println("Paciente registrado y asignado al doctor: "
                                + randomDoctor.getFirstName() + " " + randomDoctor.getLastName());
                    } else {
                        System.out.println("Paciente registrado pero no hay doctores disponibles para asignar");
                    }

                    patientDAO.save(patient);
                } else if ("doctor".equals(mappedRole)) {
                    Doctor doctor = new Doctor();
                    doctor.setUserId(newUser.getId());
                    doctor.setFirstName(firstName);
                    doctor.setLastName(lastName);
                    doctor.setSpecialtyId(null);
                    doctor.setLicenseNumber(null);
                    doctorDAO.save(doctor);
                }

                Platform.runLater(() -> {
                    showStatus("¡Cuenta creada exitosamente! \nRedirigiendo al login...", true);
                    btnRegister.setDisable(false);
                    new Thread(() -> {
                        try { Thread.sleep(1500); } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        Platform.runLater(() -> goToLogin(event));
                    }).start();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("Error al crear la cuenta, intenta de nuevo", false);
                    btnRegister.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /*Navega a la pantalla de inicio de sesión
     Se llama cuando el usuario hace clic en "Ir a Login" */
    @FXML
    protected void onGoToLogin(ActionEvent event) {
        goToLogin(event);
    }

    //Carga la pantalla de login aplicando los estilos CSS
    private void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/itc/healthtrack/views/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            String cssPath = getClass().getResource("/css/main.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Muestra un mensaje de estado en la interfaz.
     Cambia el color según si es un éxito (verde) o un error (rojo)*/
    private void showStatus(String message, boolean isSuccess) {
        lblStatus.setText(message);
        lblStatus.setTextFill(isSuccess ? Color.web("#4caf50") : Color.web("#ff5252"));
        lblStatus.setVisible(true);
    }
}
