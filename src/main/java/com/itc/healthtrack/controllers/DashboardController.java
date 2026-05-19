package com.itc.healthtrack.controllers;

import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.Role;
import com.itc.healthtrack.models.UserProfile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

/*Controlador principal que gestiona el menú lateral y el área de contenido dinámico.
  Adapta los módulos disponibles según el rol del usuario logeado*/
public class DashboardController {

    // Elementos de interfaz
    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;          // Etiqueta con el rol del usuario
    @FXML private VBox contentArea;         // Área central donde se cargan los módulos
    @FXML private Button btnPatientsList;   // Botón para ir a la lista de pacientes
    @FXML private Button btnAdminPanel;     // Botón para ir al panel administrador

    // Datos del usuario logeado
    private UserProfile loggedInProfile;
    private Role loggedInRole;
    private Doctor loggedInDoctor;
    private Patient loggedInPatient;

    /*Inicializa el panel con la información del usuario logeado.
      Ajusta la interfaz según el rol del usuario */
    public void initData(UserProfile profile, Role role, Doctor doctor, Patient patient) {
        this.loggedInProfile = profile;
        this.loggedInRole = role;
        this.loggedInDoctor = doctor;
        this.loggedInPatient = patient;

        String roleName = role != null ? role.getName() : "patient";

        // Muestra el prefijo correcto según el rol
        if ("doctor".equals(roleName) && doctor != null) {
            userNameLabel.setText("Dr. " + doctor.getLastName());
            roleLabel.setText("Médico");
        } else if ("admin".equals(roleName)) {
            userNameLabel.setText(profile != null ? profile.getEmail() : "Admin");
            roleLabel.setText("Administrador");
        } else if (patient != null) {
            userNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
            roleLabel.setText("Paciente");
        } else {
            userNameLabel.setText(profile != null ? profile.getEmail() : "Usuario");
            roleLabel.setText("Paciente");
        }

        if ("patient".equals(roleName)) {
            btnPatientsList.setVisible(false);
            btnPatientsList.setManaged(false);
            btnAdminPanel.setVisible(false);
            btnAdminPanel.setManaged(false);
            onShowMetrics();
        } else if ("admin".equals(roleName)) {
            onShowAdmin();    // Admins van al panel de administración
        } else {
            btnAdminPanel.setVisible(false);
            btnAdminPanel.setManaged(false);
            onShowPatientsList();  // Médicos ven su lista de pacientes
        }
    }

    //Carga el módulo de lista de pacientes en el área central
    @FXML
    protected void onShowPatientsList() {
        changeModule("/com/itc/healthtrack/views/patients-view.fxml", "patients");
    }

    //Carga el módulo del panel de administración en el área central.
    @FXML
    protected void onShowAdmin() {
        changeModule("/com/itc/healthtrack/views/admin-view.fxml", "admin");
    }

    //Carga el módulo de métricas
    @FXML
    protected void onShowMetrics() {
        changeModule("/com/itc/healthtrack/views/metrics-view.fxml", "metrics");
    }

    //Carga el módulo de reportes
    @FXML
    protected void onShowReports() {
        changeModule("/com/itc/healthtrack/views/reports-view.fxml", "reports");
    }

    //Carga el módulo de recomendaciones
    @FXML
    protected void onShowRecommendations() {
        changeModule("/com/itc/healthtrack/views/recommendations-view.fxml", "recommendations");
    }

    /*Método genérico para cargar vistas FXML
      Instancia el controlador correspondiente y pasa los datos del usuario.*/
    private void changeModule(String fxmlPath, String moduleType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            // Se entrega el contexto del usuario al controlador correspondiente
            switch (moduleType) {
                case "admin":
                    AdminController adminController = loader.getController();
                    adminController.initData(loggedInProfile, loggedInRole, loggedInDoctor, loggedInPatient);
                    break;
                case "patients":
                    PatientsController patientsController = loader.getController();
                    patientsController.initData(loggedInProfile, loggedInRole, loggedInDoctor, loggedInPatient);
                    break;
                case "metrics":
                    MetricsController metricsController = loader.getController();
                    metricsController.initData(loggedInProfile, loggedInRole, loggedInDoctor, loggedInPatient);
                    break;
                case "reports":
                    ReportsController reportsController = loader.getController();
                    reportsController.initData(loggedInProfile, loggedInRole, loggedInDoctor, loggedInPatient);
                    break;
                case "recommendations":
                    RecommendationsController recommendationsController = loader.getController();
                    recommendationsController.initData(loggedInProfile, loggedInRole, loggedInDoctor, loggedInPatient);
                    break;
            }

            // Reemplaza el contenido anterior por el nuevo módulo
            contentArea.getChildren().clear();
            contentArea.getChildren().add(node);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el módulo: " + fxmlPath);
        }
    }

    /* Cierra la sesión del usuario y vuelve a la pantalla de inicio de sesión.
      Restaura los estilos CSS para que la pantalla de login sea visible*/
    @FXML
    protected void onLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/itc/healthtrack/views/login-view.fxml"));
            Scene loginScene = new Scene(fxmlLoader.load(), 800, 600);

            loginScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            String cssPath = getClass().getResource("/css/main.css").toExternalForm();
            loginScene.getStylesheets().add(cssPath);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
