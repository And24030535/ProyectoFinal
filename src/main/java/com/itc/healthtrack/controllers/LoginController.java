package com.itc.healthtrack.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itc.healthtrack.dao.DoctorDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.dao.RoleDAO;
import com.itc.healthtrack.dao.UserProfileDAO;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.Role;
import com.itc.healthtrack.models.UserProfile;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Controlador encargado de la autenticacion de usuarios.
 */
public class LoginController {

    // Elementos de interfaz
    @FXML private TextField emailField;         // Campo de email
    @FXML private PasswordField passwordField;  // Campo de contraseña
    @FXML private Label errorLabel;             // Etiqueta para mostrar errores
    @FXML private Button loginButton;           // Botón de inicio de sesión

    // Acceso a datos
    private final UserProfileDAO userProfileDAO = new UserProfileDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    // Cliente HTTP para comunicarse con Firebase Auth REST
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Navega a la pantalla de crear nueva cuenta
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
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validacion basica de campos vacios
        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, ingresa correo y contraseña.");
            return;
        }

        // Bloqueo visual de seguridad mientras se consulta la nube
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        // Ejecucion en hilo secundario para no congelar la interfaz
        new Thread(() -> {
            try {
                // Paso 1: Validar credenciales con Firebase Authentication (REST)
                String authUid = signInWithFirebase(email, password);
                if (authUid == null) {
                    Platform.runLater(() -> {
                        showError("Credenciales inválidas");
                        loginButton.setDisable(false);
                    });
                    return;
                }

                // Paso 2: Obtener el usuario autenticado desde Firebase Admin
                UserRecord authUser = FirebaseAuth.getInstance().getUser(authUid);

                // Paso 3: Buscar el perfil local en Firestore con el authUid
                UserProfile userProfile = userProfileDAO.getUserProfileByAuthUid(authUser.getUid());
                if (userProfile == null) {
                    Platform.runLater(() -> {
                        showError("Perfil no encontrado en la base de datos");
                        loginButton.setDisable(false);
                    });
                    return;
                }

                // Paso 4: Obtener el rol del usuario usando el roleId
                Role role = roleDAO.getRoleById(userProfile.getRoleId());
                if (role == null) {
                    Platform.runLater(() -> {
                        showError("Rol no encontrado en la base de datos");
                        loginButton.setDisable(false);
                    });
                    return;
                }

                // Paso 5: Cargar informacion adicional segun el rol
                String roleName = role.getName();
                Patient patient = null;
                Doctor doctor = null;

                if ("patient".equals(roleName)) {
                    // Si es paciente, buscar su registro clinico
                    patient = patientDAO.getPatientByUserProfileId(userProfile.getId());
                    if (patient == null) {
                        Platform.runLater(() -> {
                            showError("Paciente no encontrado en la base de datos");
                            loginButton.setDisable(false);
                        });
                        return;
                    }

                    // Si el paciente no tiene doctor, se asigna automaticamente
                    if (patient.getPrimaryDoctorId() == null || patient.getPrimaryDoctorId().isEmpty()) {
                        List<Doctor> doctors = doctorDAO.getAllDoctors();
                        if (!doctors.isEmpty()) {
                            Doctor randomDoctor = doctors.get((int) (Math.random() * doctors.size()));
                            patientDAO.assignDoctorToPatient(patient.getId(), randomDoctor.getId());
                            patient.setPrimaryDoctorId(randomDoctor.getId());
                        }
                    }
                } else if ("doctor".equals(roleName)) {
                    // Si es doctor, buscar su registro medico
                    doctor = doctorDAO.getDoctorByUserProfileId(userProfile.getId());
                    if (doctor == null) {
                        Platform.runLater(() -> {
                            showError("Doctor no encontrado en la base de datos");
                            loginButton.setDisable(false);
                        });
                        return;
                    }
                }

                // Paso 6: Cargar el dashboard con toda la informacion del usuario
                UserProfile finalUserProfile = userProfile;
                Role finalRole = role;
                Patient finalPatient = patient;
                Doctor finalDoctor = doctor;
                Platform.runLater(() -> loadDashboard(event, finalUserProfile, finalRole, finalDoctor, finalPatient));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Error al conectar con la base de datos");
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }

    // Autentica al usuario con Firebase Authentication usando el API REST
    private String signInWithFirebase(String email, String password) throws Exception {
        // La clave web se obtiene desde una variable de entorno para no guardarla en el codigo
        String apiKey = System.getenv("FIREBASE_WEB_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta la clave FIREBASE_WEB_API_KEY");
        }

        // Se construye el cuerpo JSON requerido por Firebase Auth
        String requestBody = "{"
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\","
                + "\"returnSecureToken\":true"
                + "}";

        // Se construye la solicitud HTTP POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Se envia la solicitud y se obtiene la respuesta JSON
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Si la respuesta no es 200, se considera que la autenticacion fallo
        if (response.statusCode() != 200) {
            return null;
        }

        // Se parsea la respuesta para obtener el UID (localId)
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.get("localId").getAsString();
    }

    // Muestra un mensaje de error en la interfaz
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    // Configura y muestra la escena del Dashboard inyectando los estilos CSS
    private void loadDashboard(ActionEvent event, UserProfile profile, Role role, Doctor doctor, Patient patient) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/itc/healthtrack/views/dashboard-view.fxml"));
            Scene dashboardScene = new Scene(fxmlLoader.load(), 1000, 700);

            // Inyeccion de estilos para garantizar visibilidad en modo oscuro
            dashboardScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            String cssPath = getClass().getResource("/css/main.css").toExternalForm();
            dashboardScene.getStylesheets().add(cssPath);

            DashboardController controller = fxmlLoader.getController();
            controller.initData(profile, role, doctor, patient);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error crítico al cargar la interfaz principal.");
        }
    }
}
