package com.itc.healthtrack.controllers;

import com.itc.healthtrack.dao.DoctorDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.dao.UserDAO;
import com.itc.healthtrack.dto.UserSession;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.util.List;

/**
 * Controlador encargado de la autenticacion de usuarios.
 */
public class LoginController {

    // Elementos de interfaz
    @FXML private TextField emailField;         // Campo de email
    @FXML private PasswordField passwordField;   // Campo de contraseña
    @FXML private Label errorLabel;              // Etiqueta para mostrar errores
    @FXML private Button loginButton;            // Botón de inicio de sesión

    //Acceso a datos
    private final UserDAO userDAO = new UserDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    //Navega a la pantalla de crear nueva cuenta
    @FXML
    protected void onGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/itc/healthtrack/views/register-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            String cssPath = getClass().getResource("/css/main.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la pantalla de registro");
        }
    }

    // Procesa el intento de inicio de sesion
    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, ingresa correo y contraseña.");
            return;
        }

        // Bloqueo visual de seguridad mientras se consulta la nube
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        // Ejecucion en hilo secundario para no congelar la UI
        new Thread(() -> {
            try {
                User authUser = userDAO.authenticateUser(email, password);
                if (authUser == null) {
                    Platform.runLater(() -> {
                        showError("Credenciales inválidas");
                        loginButton.setDisable(false);
                    });
                    return;
                }

                UserSession session = buildSession(authUser);
                if (session == null) {
                    Platform.runLater(() -> {
                        showError("Perfil de usuario no encontrado");
                        loginButton.setDisable(false);
                    });
                    return;
                }

                Platform.runLater(() -> loadDashboard(event, session));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error al conectar con la base de datos");
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }

    private UserSession buildSession(User authUser) throws Exception {
        String roleId = authUser.getRoleId();
        if ("patient".equals(roleId)) {
            Patient patient = patientDAO.getByUserId(authUser.getId());
            if (patient == null) {
                return null;
            }
            if (patient.getPrimaryDoctorId() == null || patient.getPrimaryDoctorId().isEmpty()) {
                assignRandomDoctor(patient);
            }
            return new UserSession(authUser, patient, null);
        }
        if ("doctor".equals(roleId)) {
            Doctor doctor = doctorDAO.getByUserId(authUser.getId());
            if (doctor == null) {
                return null;
            }
            return new UserSession(authUser, null, doctor);
        }
        return new UserSession(authUser, null, null);
    }

    private void assignRandomDoctor(Patient patient) throws Exception {
        List<Doctor> doctors = doctorDAO.getAll();
        if (!doctors.isEmpty()) {
            Doctor randomDoctor = doctors.get((int) (Math.random() * doctors.size()));
            patientDAO.assignPrimaryDoctor(patient.getId(), randomDoctor.getId());
            patient.setPrimaryDoctorId(randomDoctor.getId());
            System.out.println("Paciente " + patient.getFirstName() + " asignado al doctor: "
                    + randomDoctor.getFirstName() + " " + randomDoctor.getLastName());
        }
    }

    //Muestra un mensaje de error en la interfaz.
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    //Configura y muestra la escena del Dashboard inyectando los estilos CSS
    private void loadDashboard(ActionEvent event, UserSession session) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/itc/healthtrack/views/dashboard-view.fxml"));
            Scene dashboardScene = new Scene(fxmlLoader.load(), 1000, 700);

            // Inyeccion de estilos para garantizar visibilidad en modo oscuro
            dashboardScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            String cssPath = getClass().getResource("/css/main.css").toExternalForm();
            dashboardScene.getStylesheets().add(cssPath);

            DashboardController controller = fxmlLoader.getController();
            controller.initData(session);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error crítico al cargar la interfaz principal.");
        }
    }
}
