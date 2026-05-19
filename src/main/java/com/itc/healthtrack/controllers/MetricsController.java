package com.itc.healthtrack.controllers;

import com.google.cloud.Timestamp;
import com.itc.healthtrack.dao.HealthMetricDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.dao.UserProfileDAO;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.HealthMetric;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.Role;
import com.itc.healthtrack.models.UserProfile;
import com.itc.healthtrack.services.NotificationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsController {

    // Componentes de la interfaz de usuario
    @FXML private ComboBox<Patient> comboPatients;
    @FXML private TextField txtSystolic;
    @FXML private TextField txtDiastolic;
    @FXML private TextField txtHeartRate;
    @FXML private TextField txtGlucose;
    @FXML private TextField txtWeight;
    @FXML private javafx.scene.control.Button btnSave;
    @FXML private Label lblStatus;

    // Filtros y promedios
    @FXML private ComboBox<String> comboTimeFilter;
    @FXML private Label lblAvgBP;
    @FXML private Label lblAvgGlucose;
    @FXML private Label lblAvgWeight;

    // Tabla y gráficos
    @FXML private TableView<HealthMetric> tableMetrics;
    @FXML private TableColumn<HealthMetric, String> colDate;
    @FXML private TableColumn<HealthMetric, String> colSysDia;
    @FXML private TableColumn<HealthMetric, String> colHeartRate;
    @FXML private TableColumn<HealthMetric, String> colGlucose;
    @FXML private TableColumn<HealthMetric, String> colWeight;
    @FXML private LineChart<String, Number> evolutionChart;
    @FXML private BarChart<String, Number> averagesChart;

    // Acceso a la base de datos y variables de estado
    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthMetricDAO healthMetricDAO = new HealthMetricDAO();
    private final UserProfileDAO userProfileDAO = new UserProfileDAO();
    private final NotificationService notificationService = new NotificationService();
    private final ObservableList<HealthMetric> metricsObservableList = FXCollections.observableArrayList();

    // Estado del controlador
    private UserProfile loggedInProfile;
    private Role loggedInRole;
    private Doctor loggedInDoctor;
    private Patient loggedInPatient;
    private HealthMetric selectedMetric = null;
    private List<HealthMetric> currentPatientHistory = new ArrayList<>();
    private Map<String, UserProfile> userProfileByPatientId = new HashMap<>();

    /*
     Inicializa el controlador con los datos del usuario logeado
     Configura la tabla y carga los pacientes según el rol
     - Pacientes solo ven sus propias métricas
     - Médicos ven sus pacientes asignados
     - Admins ven todos los pacientes
     */
    public void initData(UserProfile profile, Role role, Doctor doctor, Patient patient) {
        this.loggedInProfile = profile;
        this.loggedInRole = role;
        this.loggedInDoctor = doctor;
        this.loggedInPatient = patient;

        setupTable();
        setupFilters();

        if (role != null && "patient".equals(role.getName()) && patient != null) {
            comboPatients.getItems().add(patient);
            comboPatients.getSelectionModel().selectFirst();
            comboPatients.setDisable(true);
            loadMetricsForPatient(patient.getId());
        } else {
            loadPatientsIntoCombo();
            comboPatients.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    onClearForm();
                    loadMetricsForPatient(newVal.getId());
                }
            });
        }
    }

    private void setupFilters() {
        comboTimeFilter.setItems(FXCollections.observableArrayList("Historial Completos", "Últimos 7 Días", "Últimos 30 Días"));
        comboTimeFilter.getSelectionModel().selectFirst();
        comboTimeFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyTimeFilter());
    }

    // Configura las columnas de la tabla y establece escuchadores de evento
    private void setupTable() {
        colDate.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getTimestamp();
            return new SimpleStringProperty(ts != null ? ts.toDate().toString().substring(0, 19) : "N/A");
        });

        colSysDia.setCellValueFactory(cellData -> {
            Integer sys = cellData.getValue().getSystolic();
            Integer dia = cellData.getValue().getDiastolic();
            return new SimpleStringProperty((sys != null && dia != null) ? sys + "/" + dia : "-");
        });

        colHeartRate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getHeartRate() != null ? String.valueOf(cellData.getValue().getHeartRate()) : "-"));
        colGlucose.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGlucoseLevel() != null ? String.valueOf(cellData.getValue().getGlucoseLevel()) : "-"));
        colWeight.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getWeight() != null ? String.valueOf(cellData.getValue().getWeight()) : "-"));

        tableMetrics.setItems(metricsObservableList);

        tableMetrics.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMetric = newSelection;
                txtSystolic.setText(newSelection.getSystolic() != null ? String.valueOf(newSelection.getSystolic()) : "");
                txtDiastolic.setText(newSelection.getDiastolic() != null ? String.valueOf(newSelection.getDiastolic()) : "");
                txtHeartRate.setText(newSelection.getHeartRate() != null ? String.valueOf(newSelection.getHeartRate()) : "");
                txtGlucose.setText(newSelection.getGlucoseLevel() != null ? String.valueOf(newSelection.getGlucoseLevel()) : "");
                txtWeight.setText(newSelection.getWeight() != null ? String.valueOf(newSelection.getWeight()) : "");
                btnSave.setText("Actualizar");
            }
        });
    }

    /*Carga la lista de pacientes en el ComboBox
     Si es admin, carga todos los pacientes; si es médico, solo sus pacientes */
    private void loadPatientsIntoCombo() {
        new Thread(() -> {
            try {
                List<Patient> patients;
                if (loggedInRole != null && "admin".equals(loggedInRole.getName())) {
                    patients = patientDAO.getAllPatients();
                } else if (loggedInDoctor != null) {
                    patients = patientDAO.getPatientsByDoctorId(loggedInDoctor.getId());
                } else {
                    patients = new ArrayList<>();
                }

                Map<String, UserProfile> profileMap = new HashMap<>();
                for (Patient patient : patients) {
                    UserProfile profile = userProfileDAO.getUserProfileById(patient.getUserProfileId());
                    if (profile != null) {
                        profileMap.put(patient.getId(), profile);
                    }
                }

                Platform.runLater(() -> {
                    userProfileByPatientId = profileMap;
                    comboPatients.setItems(FXCollections.observableArrayList(patients));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /*Carga el historial de métricas de un paciente desde Firestore
     Se ejecuta en un hilo secundario para no bloquear la interfaz*/
    private void loadMetricsForPatient(String patientId) {
        new Thread(() -> {
            try {
                List<HealthMetric> history = healthMetricDAO.getHealthMetricsByPatient(patientId);
                Platform.runLater(() -> {
                    currentPatientHistory = history;
                    applyTimeFilter();
                    lblStatus.setText("Historial cargado con éxito");
                    lblStatus.setTextFill(Color.web("#4caf50"));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Error al cargar el historial");
                    lblStatus.setTextFill(Color.web("#ff5252"));
                });
            }
        }).start();
    }

    /*Aplica el filtro de período al historial completo de métricas
     Actualiza la tabla, gráficos y calcula promedios según el filtro seleccionado*/
    private void applyTimeFilter() {
        if (currentPatientHistory.isEmpty()) {
            metricsObservableList.clear();
            evolutionChart.getData().clear();
            averagesChart.getData().clear();
            calculateAverages(new ArrayList<>());
            return;
        }

        String filter = comboTimeFilter.getValue();
        long nowSeconds = System.currentTimeMillis() / 1000;
        long limitSeconds = 0;

        if ("Últimos 7 Días".equals(filter)) {
            limitSeconds = 7L * 24 * 3600;
        } else if ("Últimos 30 Días".equals(filter)) {
            limitSeconds = 30L * 24 * 3600;
        }

        List<HealthMetric> filteredList = new ArrayList<>();
        for (HealthMetric metric : currentPatientHistory) {
            long metricTime = metric.getTimestamp().getSeconds();
            if (limitSeconds == 0 || (nowSeconds - metricTime) <= limitSeconds) {
                filteredList.add(metric);
            }
        }

        metricsObservableList.clear();
        metricsObservableList.addAll(filteredList);
        updateChart(filteredList);
        updateBarChart(filteredList);
        calculateAverages(filteredList);
    }

    /*Calcula y muestra los promedios de presión arterial, glucosa y peso*/
    private void calculateAverages(List<HealthMetric> data) {
        if (data.isEmpty()) {
            lblAvgBP.setText("PA: --/-- mmHg");
            lblAvgGlucose.setText("Glucosa: -- mg/dL");
            lblAvgWeight.setText("Peso: -- kg");
            return;
        }

        int sysTotal = 0;
        int diaTotal = 0;
        int bpCount = 0;
        double glTotal = 0;
        double weightTotal = 0;
        int glCount = 0;
        int weightCount = 0;

        for (HealthMetric metric : data) {
            if (metric.getSystolic() != null && metric.getDiastolic() != null) {
                sysTotal += metric.getSystolic();
                diaTotal += metric.getDiastolic();
                bpCount++;
            }
            if (metric.getGlucoseLevel() != null) {
                glTotal += metric.getGlucoseLevel();
                glCount++;
            }
            if (metric.getWeight() != null) {
                weightTotal += metric.getWeight();
                weightCount++;
            }
        }

        lblAvgBP.setText(bpCount > 0 ? "PA: " + (sysTotal / bpCount) + "/" + (diaTotal / bpCount) + " mmHg" : "PA: --/-- mmHg");
        lblAvgGlucose.setText(glCount > 0 ? String.format("Glucosa: %.1f mg/dL", (glTotal / glCount)) : "Glucosa: -- mg/dL");
        lblAvgWeight.setText(weightCount > 0 ? String.format("Peso: %.1f kg", (weightTotal / weightCount)) : "Peso: -- kg");
    }

    /*Actualiza el gráfico de línea con la evolución de presión arterial.*/
    private void updateChart(List<HealthMetric> history) {
        evolutionChart.getData().clear();

        XYChart.Series<String, Number> systolicSeries = new XYChart.Series<>();
        systolicSeries.setName("Sistólica");

        XYChart.Series<String, Number> diastolicSeries = new XYChart.Series<>();
        diastolicSeries.setName("Diastólica");

        for (int i = history.size() - 1; i >= 0; i--) {
            HealthMetric metric = history.get(i);
            if (metric.getSystolic() != null && metric.getDiastolic() != null) {
                String label = metric.getTimestamp().toDate().toString().substring(4, 10);
                systolicSeries.getData().add(new XYChart.Data<>(label, metric.getSystolic()));
                diastolicSeries.getData().add(new XYChart.Data<>(label, metric.getDiastolic()));
            }
        }

        evolutionChart.getData().addAll(systolicSeries, diastolicSeries);
    }

    /*Actualiza el gráfico de barras con los promedios de todas las métricas.*/
    private void updateBarChart(List<HealthMetric> history) {
        averagesChart.getData().clear();
        if (history.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Promedio del período");

        int sysTotal = 0;
        int diaTotal = 0;
        int hrTotal = 0;
        double glTotal = 0;
        double weightTotal = 0;
        int sysCount = 0;
        int diaCount = 0;
        int hrCount = 0;
        int glCount = 0;
        int weightCount = 0;

        for (HealthMetric metric : history) {
            if (metric.getSystolic() != null) { sysTotal += metric.getSystolic(); sysCount++; }
            if (metric.getDiastolic() != null) { diaTotal += metric.getDiastolic(); diaCount++; }
            if (metric.getHeartRate() != null) { hrTotal += metric.getHeartRate(); hrCount++; }
            if (metric.getGlucoseLevel() != null) { glTotal += metric.getGlucoseLevel(); glCount++; }
            if (metric.getWeight() != null) { weightTotal += metric.getWeight(); weightCount++; }
        }

        if (sysCount > 0) { avgSeries.getData().add(new XYChart.Data<>("Sistólica", sysTotal / (double) sysCount)); }
        if (diaCount > 0) { avgSeries.getData().add(new XYChart.Data<>("Diastólica", diaTotal / (double) diaCount)); }
        if (hrCount > 0) { avgSeries.getData().add(new XYChart.Data<>("F. Cardíaca", hrTotal / (double) hrCount)); }
        if (glCount > 0) { avgSeries.getData().add(new XYChart.Data<>("Glucosa", glTotal / (double) glCount)); }
        if (weightCount > 0) { avgSeries.getData().add(new XYChart.Data<>("Peso (kg)", weightTotal / (double) weightCount)); }

        averagesChart.getData().add(avgSeries);
    }

    /*Guarda una nueva métrica o actualiza una existente*/
    @FXML
    protected void onSaveMetric() {
        Patient selectedPatient = comboPatients.getValue();
        if (selectedPatient == null) {
            lblStatus.setText("Error: Selecciona un paciente primero");
            lblStatus.setTextFill(Color.web("#ff5252"));
            return;
        }

        try {
            boolean isNewRecord = (selectedMetric == null);
            HealthMetric metricToProcess = isNewRecord ? new HealthMetric() : selectedMetric;

            if (isNewRecord) {
                metricToProcess.setPatientId(selectedPatient.getId());
                metricToProcess.setTimestamp(Timestamp.now());
            }

            metricToProcess.setSystolic(txtSystolic.getText().isEmpty() ? null : Integer.parseInt(txtSystolic.getText()));
            metricToProcess.setDiastolic(txtDiastolic.getText().isEmpty() ? null : Integer.parseInt(txtDiastolic.getText()));
            metricToProcess.setHeartRate(txtHeartRate.getText().isEmpty() ? null : Integer.parseInt(txtHeartRate.getText()));
            metricToProcess.setGlucoseLevel(txtGlucose.getText().isEmpty() ? null : Double.parseDouble(txtGlucose.getText()));
            metricToProcess.setWeight(txtWeight.getText().isEmpty() ? null : Double.parseDouble(txtWeight.getText()));

            if (metricToProcess.getWeight() != null && selectedPatient.getHeight() != null && selectedPatient.getHeight() > 0) {
                double heightM = selectedPatient.getHeight();
                double bmi = metricToProcess.getWeight() / (heightM * heightM);
                metricToProcess.setBmi(Math.round(bmi * 10.0) / 10.0);
            }

            lblStatus.setText(isNewRecord ? "Guardando..." : "Actualizando...");
            lblStatus.setTextFill(Color.web("#ffffff"));

            String alert = evaluateClinicalThresholds(metricToProcess);
            if (alert != null) {
                showClinicalAlert(alert, selectedPatient);
            }

            new Thread(() -> {
                try {
                    if (isNewRecord) {
                        healthMetricDAO.saveHealthMetric(metricToProcess);
                    } else {
                        healthMetricDAO.updateHealthMetric(metricToProcess);
                    }

                    Platform.runLater(() -> {
                        onClearForm();
                        loadMetricsForPatient(selectedPatient.getId());
                        lblStatus.setText(isNewRecord ? "Métrica guardada" : "Métrica actualizada");
                        lblStatus.setTextFill(Color.web("#4caf50"));
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblStatus.setText("Error al guardar en la base de datos");
                        lblStatus.setTextFill(Color.web("#ff5252"));
                    });
                    e.printStackTrace();
                }
            }).start();

        } catch (NumberFormatException e) {
            lblStatus.setText("Error: Usa valores numéricos válidos");
            lblStatus.setTextFill(Color.web("#ff5252"));
        }
    }

    /*Evalúa datos y retorna un mensaje de alerta si se detectan valores críticos*/
    private String evaluateClinicalThresholds(HealthMetric metric) {
        StringBuilder alert = new StringBuilder();

        if (metric.getSystolic() != null && metric.getDiastolic() != null) {
            int sys = metric.getSystolic();
            int dia = metric.getDiastolic();
            if (sys >= 180 || dia >= 120) {
                alert.append("ALERTA CRÍTICA: Hipertensión en crisis (").append(sys).append("/").append(dia).append(" mmHg)\nConsulta médica urgente\n\n");
            } else if (sys >= 140 || dia >= 90) {
                alert.append("ALERTA: Hipertensión arterial detectada (").append(sys).append("/").append(dia).append(" mmHg)\n\n");
            }
        }

        if (metric.getGlucoseLevel() != null) {
            double gluc = metric.getGlucoseLevel();
            if (gluc > 300) {
                alert.append("ALERTA CRÍTICA: Glucosa muy elevada (").append(gluc).append(" mg/dL)\n Riesgo de cetoacidosis\n\n");
            } else if (gluc > 125) {
                alert.append("ALERTA: Glucosa elevada (").append(gluc).append(" mg/dL)\n Posible diabetes\n\n");
            } else if (gluc < 70) {
                alert.append("ALERTA: Hipoglucemia detectada (").append(gluc).append(" mg/dL)\n\n");
            }
        }

        if (metric.getHeartRate() != null) {
            int hr = metric.getHeartRate();
            if (hr > 120) {
                alert.append("ALERTA: Frecuencia cardíaca elevada (").append(hr).append(" lpm)\n Taquicardia\n\n");
            } else if (hr < 50) {
                alert.append("ALERTA: Frecuencia cardíaca baja (").append(hr).append(" lpm)\nBradicardia\n\n");
            }
        }

        if (metric.getBmi() != null) {
            double bmi = metric.getBmi();
            if (bmi >= 40) {
                alert.append("ALERTA: Obesidad mórbida (IMC ").append(bmi).append(")\n Riesgo cardiovascular alto\n\n");
            } else if (bmi >= 30) {
                alert.append("Obesidad detectada (IMC ").append(bmi).append(")\n Se recomienda plan nutricional\n\n");
            }
        }

        return alert.length() > 0 ? alert.toString().trim() : null;
    }

    /* Muestra un diálogo de alerta clínica en la interfaz.
     También notifica al paciente y al médico si es aplicable. */
    private void showClinicalAlert(String message, Patient patient) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Alerta Clínica — HealthTrack");
            alert.setHeaderText("Se detectaron valores fuera del rango clínico normal");
            alert.setContentText(message);
            alert.getDialogPane().setStyle("-fx-background-color: #ffffff; -fx-font-size: 13px;");
            alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #000000;");
            alert.showAndWait();

            lblStatus.setText("Valores críticos detectados, revisa la alerta");
            lblStatus.setTextFill(Color.web("#ff9800"));

            // Notificar al paciente
            String patientEmail = getPatientEmail(patient);
            String patientName = patient.getFirstName() + " " + patient.getLastName();
            notificationService.notifyPatient(patientEmail, patientName,
                    "Valores clínicos críticos detectados en tu última medición, consulta a tu médico");

            // Notificar al doctor si aplica
            if (loggedInRole != null && ("doctor".equals(loggedInRole.getName()) || "admin".equals(loggedInRole.getName()))
                    && loggedInDoctor != null && loggedInProfile != null) {
                String doctorName = "Dr. " + loggedInDoctor.getFirstName() + " " + loggedInDoctor.getLastName();
                notificationService.notifyDoctor(loggedInProfile.getEmail(), doctorName,
                        "ALERTA: El paciente " + patientName + " tiene valores críticos registrados");
            }
        });
    }

    //Elimina la métrica seleccionada de la base de datos
    @FXML
    protected void onDeleteMetric() {
        if (selectedMetric == null) {
            lblStatus.setText("Selecciona una fila de la tabla para eliminar.");
            lblStatus.setTextFill(Color.web("#ff5252"));
            return;
        }

        lblStatus.setText("Eliminando...");
        lblStatus.setTextFill(Color.web("#ffffff"));
        String metricId = selectedMetric.getId();
        String patientId = selectedMetric.getPatientId();

        new Thread(() -> {
            try {
                healthMetricDAO.deleteHealthMetric(metricId);
                Platform.runLater(() -> {
                    onClearForm();
                    loadMetricsForPatient(patientId);
                    lblStatus.setText("Métrica eliminada.");
                    lblStatus.setTextFill(Color.web("#4caf50"));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Limpia todos los campos del formulario y deselecciona la métrica actual
    @FXML
    protected void onClearForm() {
        txtSystolic.clear();
        txtDiastolic.clear();
        txtHeartRate.clear();
        txtGlucose.clear();
        txtWeight.clear();

        selectedMetric = null;
        tableMetrics.getSelectionModel().clearSelection();
        btnSave.setText("Guardar");
    }

    // Obtiene el correo del paciente usando el mapa local
    private String getPatientEmail(Patient patient) {
        if (patient == null) {
            return "";
        }
        UserProfile profile = userProfileByPatientId.get(patient.getId());
        if (profile != null && profile.getEmail() != null) {
            return profile.getEmail();
        }
        if (loggedInProfile != null && loggedInPatient != null && loggedInPatient.getId().equals(patient.getId())) {
            return loggedInProfile.getEmail();
        }
        return "";
    }
}
