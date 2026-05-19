package com.itc.healthtrack.controllers;

import com.google.cloud.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.itc.healthtrack.dao.DoctorDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.dao.RoleDAO;
import com.itc.healthtrack.dao.SpecialtyDAO;
import com.itc.healthtrack.dao.UserProfileDAO;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.Role;
import com.itc.healthtrack.models.Specialty;
import com.itc.healthtrack.models.UserProfile;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.List;

/*Controlador que gestiona el registro de nuevos usuarios
  Valida los datos, crea el usuario en Firebase Auth y luego crea sus documentos en Firestore*/
public class RegisterController {

    // Elementos de formulario
    @FXML private TextField txtFirstName;            // Campo: Nombre
    @FXML private TextField txtLastName;             // Campo: Apellido
    @FXML private TextField txtEmail;                // Campo: Email
    @FXML private TextField txtHeight;               // Campo: Estatura
    @FXML private DatePicker dpBirthDate;            // Campo: Fecha de nacimiento
    @FXML private PasswordField txtPassword;         // Campo: Contraseña
    @FXML private PasswordField txtConfirmPassword;  // Campo: Confirmar contraseña
    @FXML private ComboBox<String> comboGender;      // ComboBox: Género
    @FXML private ComboBox<String> comboRole;        // ComboBox: Rol (Paciente/Doctor)
    @FXML private Label lblStatus;                   // Etiqueta de estado/errores
    @FXML private Button btnRegister;                // Botón de registro

    // Acceso a datos
    private final RoleDAO roleDAO = new RoleDAO();
    private final UserProfileDAO userProfileDAO = new UserProfileDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final SpecialtyDAO specialtyDAO = new SpecialtyDAO();

    // Inicializa los ComboBox con opciones de género y rol
    @FXML
    public void initialize() {
        comboGender.getItems().addAll("M", "F", "Otro");
        comboRole.getItems().addAll("Paciente", "Doctor");
        comboRole.getSelectionModel().selectFirst();
    }

    /*Procesa el registro de un nuevo usuario.
      Paso 1: Crear credenciales en Firebase Auth.
      Paso 2: Crear documentos UserProfile y Patient/Doctor en Firestore.*/
    @FXML
    protected void onRegister(ActionEvent event) {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String roleLabel = comboRole.getValue();

        // Mapear roles en español a ingles para consistencia
        final String roleName;
        if ("Paciente".equals(roleLabel)) {
            roleName = "patient";
        } else if ("Doctor".equals(roleLabel)) {
            roleName = "doctor";
        } else {
            roleName = "patient";
        }

        // Validaciones basicas de campos
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
                // Paso 1: Crear el usuario en Firebase Authentication
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(email)
                        .setPassword(password);
                UserRecord authUser = FirebaseAuth.getInstance().createUser(request);

                // Paso 2: Obtener o crear el rol en Firestore
                Role role = getOrCreateRole(roleName);

                // Paso 3: Crear el perfil basico del usuario en Firestore
                UserProfile userProfile = new UserProfile();
                userProfile.setAuthUid(authUser.getUid());
                userProfile.setRoleId(role.getId());
                userProfile.setEmail(email);
                userProfile.setRegisteredAt(Timestamp.now());
                userProfileDAO.saveUserProfile(userProfile);

                // Paso 4: Crear el documento especifico del rol (paciente o doctor)
                if ("patient".equals(roleName)) {
                    Patient patient = new Patient();
                    patient.setUserProfileId(userProfile.getId());
                    patient.setFirstName(firstName);
                    patient.setLastName(lastName);
                    patient.setGender(comboGender.getValue());
                    patient.setBirthDate(dpBirthDate.getValue() != null ? dpBirthDate.getValue().toString() : null);

                    // Validar y asignar altura si se ingreso
                    String heightText = txtHeight.getText().trim();
                    if (!heightText.isEmpty()) {
                        try {
                            patient.setHeight(Double.parseDouble(heightText));
                        } catch (NumberFormatException e) {
                            Platform.runLater(() -> {
                                showStatus("Altura inválida. Usa un número decimal (ej: 1.75).", false);
                                btnRegister.setDisable(false);
                            });
                            return;
                        }
                    }

                    // Asignar doctor aleatorio si existe alguno
                    List<Doctor> doctors = doctorDAO.getAllDoctors();
                    if (!doctors.isEmpty()) {
                        Doctor randomDoctor = doctors.get((int) (Math.random() * doctors.size()));
                        patient.setPrimaryDoctorId(randomDoctor.getId());
                    }

                    patientDAO.savePatient(patient);
                } else if ("doctor".equals(roleName)) {
                    // Asegurar una especialidad por defecto si no existe
                    Specialty specialty = getOrCreateSpecialty("General");

                    Doctor doctor = new Doctor();
                    doctor.setUserProfileId(userProfile.getId());
                    doctor.setSpecialtyId(specialty.getId());
                    doctor.setLicenseNumber("PENDING");
                    doctor.setFirstName(firstName);
                    doctor.setLastName(lastName);
                    doctorDAO.saveDoctor(doctor);
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

    // Obtiene un rol existente o crea uno nuevo si no existe
    private Role getOrCreateRole(String roleName) throws Exception {
        Role role = roleDAO.getRoleByName(roleName);
        if (role != null) {
            return role;
        }

        Role newRole = new Role();
        newRole.setName(roleName);
        newRole.setDescription("Rol generado automaticamente");
        roleDAO.saveRole(newRole);
        return newRole;
    }

    // Obtiene una especialidad existente o crea una nueva si no existe
    private Specialty getOrCreateSpecialty(String specialtyName) throws Exception {
        Specialty specialty = specialtyDAO.getSpecialtyByName(specialtyName);
        if (specialty != null) {
            return specialty;
        }

        Specialty newSpecialty = new Specialty();
        newSpecialty.setName(specialtyName);
        specialtyDAO.saveSpecialty(newSpecialty);
        return newSpecialty;
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
