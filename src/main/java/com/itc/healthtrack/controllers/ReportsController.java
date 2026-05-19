package com.itc.healthtrack.controllers;

import com.itc.healthtrack.dao.HealthMetricDAO;
import com.itc.healthtrack.dao.PatientDAO;
import com.itc.healthtrack.models.Doctor;
import com.itc.healthtrack.models.HealthMetric;
import com.itc.healthtrack.models.Patient;
import com.itc.healthtrack.models.Role;
import com.itc.healthtrack.models.UserProfile;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

//Controlador encargado de exportar el historial clinico a formatos de reporte (PDF)
public class ReportsController {

    // Elementos de interfaz
    @FXML private ComboBox<Patient> comboPatients;  // ComboBox para seleccionar el paciente
    @FXML private Label lblStatus;                  // Etiqueta para mensajes de estado/progreso

    // Acceso a datos
    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthMetricDAO healthMetricDAO = new HealthMetricDAO();

    private UserProfile loggedInProfile;
    private Role loggedInRole;
    private Doctor loggedInDoctor;
    private Patient loggedInPatient;

    /*Inicializa el controlador con los datos del usuario logeado
     Si es un paciente, muestra solo sus propios datos
     Si es médico/admin, carga la lista de pacientes*/
    public void initData(UserProfile profile, Role role, Doctor doctor, Patient patient) {
        this.loggedInProfile = profile;
        this.loggedInRole = role;
        this.loggedInDoctor = doctor;
        this.loggedInPatient = patient;

        if (role != null && "patient".equals(role.getName()) && patient != null) {
            comboPatients.getItems().add(patient);
            comboPatients.getSelectionModel().selectFirst();
            comboPatients.setDisable(true);
        } else {
            loadPatients();
        }
    }

    // Carga la lista de pacientes en el menu desplegable
    private void loadPatients() {
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
                Platform.runLater(() -> comboPatients.setItems(FXCollections.observableArrayList(patients)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    protected void onExportPDF() {
        Patient selectedPatient = comboPatients.getValue();

        if (selectedPatient == null) {
            lblStatus.setText("Por favor, selecciona un paciente primero.");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte Clínico");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        fileChooser.setInitialFileName("Historial_" + selectedPatient.getFirstName() + ".pdf");

        Stage stage = (Stage) comboPatients.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            lblStatus.setText("Descargando métricas...");
            lblStatus.setTextFill(javafx.scene.paint.Color.WHITE);

            new Thread(() -> {
                try {
                    List<HealthMetric> history = healthMetricDAO.getHealthMetricsByPatient(selectedPatient.getId());

                    Platform.runLater(() -> {
                        try {
                            lblStatus.setText("Generando gráficos...");
                            List<byte[]> chartImages = buildChartImages(history);

                            new Thread(() -> {
                                try {
                                    generatePDF(file.getAbsolutePath(), selectedPatient, history, chartImages);
                                    Platform.runLater(() -> {
                                        lblStatus.setText("¡PDF guardado exitosamente en tu computadora!");
                                        lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
                                    });
                                } catch (Exception e) {
                                    Platform.runLater(() -> {
                                        lblStatus.setText("Error crítico al generar el PDF.");
                                        lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                                    });
                                    e.printStackTrace();
                                }
                            }).start();

                        } catch (Exception e) {
                            lblStatus.setText("Error al generar los gráficos.");
                            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblStatus.setText("Error crítico al generar el PDF.");
                        lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /*Utiliza la libreria iText para dibujar los elementos dentro del PDF
     Incluye una tabla con el historial y, si estan disponibles, graficos*/
    private void generatePDF(String destPath, Patient patient, List<HealthMetric> history,
                             List<byte[]> chartImages) throws Exception {
        PdfWriter writer = new PdfWriter(destPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Reporte Clínico - HealthTrack Community").setBold().setFontSize(18));
        document.add(new Paragraph("Paciente: " + patient.getFirstName() + " " + patient.getLastName()));
        if (loggedInDoctor != null) {
            document.add(new Paragraph("Médico a cargo: " + loggedInDoctor.getFirstName() + " " + loggedInDoctor.getLastName()));
        }
        document.add(new Paragraph(" "));

        float[] columnWidths = {130f, 100f, 60f, 80f, 80f};
        Table table = new Table(columnWidths);

        table.addHeaderCell("Fecha y Hora");
        table.addHeaderCell("Presión (Sis/Dia)");
        table.addHeaderCell("Pulso");
        table.addHeaderCell("Glucosa");
        table.addHeaderCell("Peso (kg)");

        for (HealthMetric metric : history) {
            String date = metric.getTimestamp() != null ? metric.getTimestamp().toDate().toString() : "N/A";
            String bp = (metric.getSystolic() != null && metric.getDiastolic() != null) ? metric.getSystolic() + "/" + metric.getDiastolic() : "-";
            String pulse = metric.getHeartRate() != null ? String.valueOf(metric.getHeartRate()) : "-";
            String glucose = metric.getGlucoseLevel() != null ? String.valueOf(metric.getGlucoseLevel()) : "-";
            String weight = metric.getWeight() != null ? String.valueOf(metric.getWeight()) : "-";

            table.addCell(date);
            table.addCell(bp);
            table.addCell(pulse);
            table.addCell(glucose);
            table.addCell(weight);
        }

        document.add(table);

        if (chartImages != null && !chartImages.isEmpty()) {
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Gráficos del Historial Clínico").setBold().setFontSize(14));
            for (byte[] imageData : chartImages) {
                ImageData imgData = ImageDataFactory.create(imageData);
                Image pdfImage = new Image(imgData);
                pdfImage.setAutoScale(true);
                document.add(pdfImage);
                document.add(new Paragraph(" "));
            }
        }

        document.close();
    }

    /*Crea un gráfico de línea para presión arterial y un gráfico de barras para promedios.
     Convierte los gráficos a imágenes PNG para incrustarlos en el PDF*/
    private List<byte[]> buildChartImages(List<HealthMetric> history) {
        List<byte[]> images = new ArrayList<>();

        try {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Evolución de Presión Arterial");
            lineChart.setAnimated(false);
            lineChart.setPrefSize(620, 280);

            XYChart.Series<String, Number> systolicSeries = new XYChart.Series<>();
            systolicSeries.setName("Sistólica");
            XYChart.Series<String, Number> diastolicSeries = new XYChart.Series<>();
            diastolicSeries.setName("Diastólica");

            for (int i = history.size() - 1; i >= 0; i--) {
                HealthMetric metric = history.get(i);
                if (metric.getSystolic() != null && metric.getDiastolic() != null && metric.getTimestamp() != null) {
                    String label = metric.getTimestamp().toDate().toString().substring(4, 10);
                    systolicSeries.getData().add(new XYChart.Data<>(label, metric.getSystolic()));
                    diastolicSeries.getData().add(new XYChart.Data<>(label, metric.getDiastolic()));
                }
            }

            lineChart.getData().addAll(systolicSeries, diastolicSeries);
            byte[] lineBytes = snapshotNodeToBytes(lineChart, 620, 280);
            if (lineBytes != null) {
                images.add(lineBytes);
            }
        } catch (Exception e) {
            System.err.println("Error generando gráfico de línea: " + e.getMessage());
        }

        try {
            CategoryAxis xAxis2 = new CategoryAxis();
            NumberAxis yAxis2 = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis2, yAxis2);
            barChart.setTitle("Promedios del Historial");
            barChart.setAnimated(false);
            barChart.setPrefSize(620, 280);

            XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
            avgSeries.setName("Promedio");

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
            if (hrCount > 0) { avgSeries.getData().add(new XYChart.Data<>("F.Cardíaca", hrTotal / (double) hrCount)); }
            if (glCount > 0) { avgSeries.getData().add(new XYChart.Data<>("Glucosa", glTotal / (double) glCount)); }
            if (weightCount > 0) { avgSeries.getData().add(new XYChart.Data<>("Peso (kg)", weightTotal / (double) weightCount)); }

            barChart.getData().add(avgSeries);
            byte[] barBytes = snapshotNodeToBytes(barChart, 620, 280);
            if (barBytes != null) {
                images.add(barBytes);
            }
        } catch (Exception e) {
            System.err.println("Error generando gráfico de barras: " + e.getMessage());
        }

        return images;
    }

    /*
     Renderiza un nodo de JavaFX en una escena temporal para aplicar CSS,
     toma una captura de pantalla y la retorna como un array de bytes PNG
     Debe ser llamado desde el hilo de aplicación de JavaFX*/
    private byte[] snapshotNodeToBytes(javafx.scene.Node node, double width, double height) {
        try {
            StackPane wrapper = new StackPane(node);
            javafx.scene.Scene tempScene = new javafx.scene.Scene(wrapper, width, height);
            node.applyCss();
            wrapper.layout();

            SnapshotParameters params = new SnapshotParameters();
            WritableImage writableImage = node.snapshot(params, null);

            BufferedImage bufferedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            System.err.println("Error capturando snapshot del gráfico: " + e.getMessage());
            return null;
        }
    }

    //Exporta el historial clínico a un archivo Excel (.xlsx) con formato
    @FXML
    protected void onExportExcel() {
        Patient selectedPatient = comboPatients.getValue();

        if (selectedPatient == null) {
            lblStatus.setText("Por favor, selecciona un paciente primero.");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte Clínico en Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"));
        fileChooser.setInitialFileName("Historial_" + selectedPatient.getFirstName() + ".xlsx");

        Stage stage = (Stage) comboPatients.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            lblStatus.setText("Generando archivo Excel...");
            lblStatus.setTextFill(javafx.scene.paint.Color.WHITE);

            new Thread(() -> {
                try {
                    List<HealthMetric> history = healthMetricDAO.getHealthMetricsByPatient(selectedPatient.getId());
                    generateExcel(file.getAbsolutePath(), selectedPatient, history);

                    Platform.runLater(() -> {
                        lblStatus.setText("¡Excel guardado exitosamente en tu computadora!");
                        lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblStatus.setText("Error crítico al generar el Excel.");
                        lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /*Utiliza la librería Apache POI para construir y exportar un archivo Excel*/
    private void generateExcel(String destPath, Patient patient, List<HealthMetric> history) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Historial Clínico");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Reporte Clínico - HealthTrack Community");

        Row patientRow = sheet.createRow(1);
        patientRow.createCell(0).setCellValue("Paciente: " + patient.getFirstName() + " " + patient.getLastName());

        if (loggedInDoctor != null) {
            Row doctorRow = sheet.createRow(2);
            doctorRow.createCell(0).setCellValue("Médico a cargo: " + loggedInDoctor.getFirstName() + " " + loggedInDoctor.getLastName());
        }

        Row headerRow = sheet.createRow(4);
        String[] columns = {"Fecha y Hora", "Presión (Sis/Dia)", "Pulso", "Glucosa", "Peso (kg)"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 5;
        for (HealthMetric metric : history) {
            Row row = sheet.createRow(rowNum++);

            String date = metric.getTimestamp() != null ? metric.getTimestamp().toDate().toString() : "N/A";
            String bp = (metric.getSystolic() != null && metric.getDiastolic() != null) ? metric.getSystolic() + "/" + metric.getDiastolic() : "-";
            String pulse = metric.getHeartRate() != null ? String.valueOf(metric.getHeartRate()) : "-";
            String glucose = metric.getGlucoseLevel() != null ? String.valueOf(metric.getGlucoseLevel()) : "-";
            String weight = metric.getWeight() != null ? String.valueOf(metric.getWeight()) : "-";

            row.createCell(0).setCellValue(date);
            row.createCell(1).setCellValue(bp);
            row.createCell(2).setCellValue(pulse);
            row.createCell(3).setCellValue(glucose);
            row.createCell(4).setCellValue(weight);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(destPath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }
}
