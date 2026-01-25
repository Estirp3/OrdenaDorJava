package org.pcr.gui.tabs;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class OptimizationTab extends VBox {

    private TableView<DriverInfo> driverTable;
    private ObservableList<DriverInfo> masterData = FXCollections.observableArrayList();
    private FilteredList<DriverInfo> filteredData;
    private Label driverStatusLabel;
    private Label profileStatusLabel;

    // Performance Visualization fields
    private ProgressBar cpuBar;
    private ProgressBar gpuBar;
    private ProgressBar batteryBar;

    public OptimizationTab() {
        setSpacing(15);
        setPadding(new Insets(20));

        Label titleLabel = new Label("⚡ Centro de Optimización y Drivers");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // --- SECTION 1: PERFORMANCE PROFILES ---
        VBox profilesBox = createProfilesSection();

        // --- SECTION 2: DRIVER MANAGER ---
        VBox driversBox = createDriversSection();

        // Layout: SplitPane vertical
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(profilesBox, driversBox);
        splitPane.setDividerPositions(0.3);

        getChildren().addAll(titleLabel, new Separator(), splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
    }

    // ==========================================
    // PERFORMANCE PROFILES
    // ==========================================

    private VBox createProfilesSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        Label lbl = new Label("🚀 Perfiles de Rendimiento");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        profileStatusLabel = new Label("Selecciona un perfil para aplicar optimizaciones.");
        profileStatusLabel.setStyle("-fx-text-fill: #aaa;");

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        // Gaming Button
        Button btnGaming = createProfileButton("🎮 Modo Gaming / 3D",
                "-fx-background-color: #e83e8c; -fx-text-fill: white;",
                () -> applyProfile("GAMING"));

        // Dev Button
        Button btnDev = createProfileButton("💻 Modo Desarrollo",
                "-fx-background-color: #6610f2; -fx-text-fill: white;",
                () -> applyProfile("DEV"));

        // Normal Button
        Button btnNormal = createProfileButton("🔋 Modo Equilibrado",
                "-fx-background-color: #28a745; -fx-text-fill: white;",
                () -> applyProfile("NORMAL"));

        btnBox.getChildren().addAll(btnGaming, btnDev, btnNormal);

        // --- Metrics Visualization ---
        HBox metricsBox = createMetricsPanel();

        box.getChildren().addAll(lbl, btnBox, metricsBox, profileStatusLabel);
        return box;
    }

    private HBox createMetricsPanel() {
        HBox panel = new HBox(20);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10, 0, 0, 0));

        cpuBar = createMetric("CPU / Proceso", "-fx-accent: #dc3545;"); // Red for power
        gpuBar = createMetric("Gráficos (3D)", "-fx-accent: #6610f2;"); // Purple for gaming
        batteryBar = createMetric("Ahorro Energía", "-fx-accent: #28a745;"); // Green for eco

        panel.getChildren().addAll(
                new VBox(5, new Label("CPU Speed"), cpuBar),
                new VBox(5, new Label("GPU Prio"), gpuBar),
                new VBox(5, new Label("Batería"), batteryBar));
        return panel;
    }

    private ProgressBar createMetric(String title, String style) {
        ProgressBar pb = new ProgressBar(0.5);
        pb.setStyle(style + " -fx-control-inner-background: #444;");
        pb.setPrefWidth(150);
        return pb;
    }

    private void updateMetrics(double cpu, double gpu, double bat) {
        // Simple animation simulation via thread
        new Thread(() -> {
            try {
                for (int i = 0; i <= 10; i++) {
                    double p = i / 10.0;
                    double c = cpu * p;
                    double g = gpu * p;
                    double b = bat * p;
                    // Ease-out effect logic would be overkill, linear is fine
                    Platform.runLater(() -> {
                        cpuBar.setProgress(c);
                        gpuBar.setProgress(g);
                        batteryBar.setProgress(b);
                    });
                    Thread.sleep(20);
                }
            } catch (Exception e) {
            }
        }).start();
    }

    private Button createProfileButton(String text, String style, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle(style + " -fx-font-weight: bold; -fx-padding: 10 20;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void applyProfile(String type) {
        profileStatusLabel.setText("⏳ Aplicando perfil " + type + "...");

        new Thread(() -> {
            try {
                String powerSchemeGuid = "";
                String msg = "";

                // Update visuals instantly based on profile
                double tCpu = 0.5, tGpu = 0.5, tBat = 0.5;

                // Nota: Los GUIDs pueden variar, usaremos alias comunes o busqueda
                // High Performance: 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c
                // Balanced: 381b4222-f694-41f0-9685-ff5bb260df2e
                // Power Saver: a1841308-3541-4fab-bc81-f71556f20b4a

                // Ultimate Performance (Win 10 Pro): e9a42b02-d5df-448d-aa00-03f14749eb61

                switch (type) {
                    case "GAMING":
                        // Intentar High Performance
                        powerSchemeGuid = "8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c";
                        msg = "Modo Gaming Activado (Alto Rendimiento + Optimizaciones)";
                        // Tweaks extra para gaming (ej. GameBar)
                        executeRegCmd("HKCU\\Software\\Microsoft\\GameBar", "AllowAutoGameMode", "1");
                        tCpu = 1.0;
                        tGpu = 1.0;
                        tBat = 0.1;
                        break;
                    case "DEV":
                        // High Performance tambien
                        powerSchemeGuid = "8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c";
                        msg = "Modo Desarrollo Activado (Alto Rendimiento)";
                        tCpu = 0.9;
                        tGpu = 0.4;
                        tBat = 0.3;
                        break;
                    case "NORMAL":
                    default:
                        // Balanced
                        powerSchemeGuid = "381b4222-f694-41f0-9685-ff5bb260df2e";
                        msg = "Modo Equilibrado Activado";
                        executeRegCmd("HKCU\\Software\\Microsoft\\GameBar", "AllowAutoGameMode", "0");
                        tCpu = 0.6;
                        tGpu = 0.5;
                        tBat = 0.8;
                        break;
                }

                updateMetrics(tCpu, tGpu, tBat);

                // Ejecutar cambio de plan
                Process p = Runtime.getRuntime().exec("powercfg /setactive " + powerSchemeGuid);
                p.waitFor();

                // Check si fallo (a veces powercfg devuelve 0 aunque falle si el GUID no
                // existe)
                // Pero asumimos exito o damos feedback general

                String finalMsg = msg;
                Platform.runLater(() -> {
                    profileStatusLabel.setText("✅ " + finalMsg);
                    checkCurrentProfile(); // Verificar cambio real
                });

            } catch (Exception e) {
                Platform.runLater(() -> profileStatusLabel.setText("❌ Error: " + e.getMessage()));
            }
        }).start();
    }

    private void checkCurrentProfile() {
        new Thread(() -> {
            try {
                // powercfg /getactivescheme
                Process p = Runtime.getRuntime().exec("powercfg /getactivescheme");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
                String line = reader.readLine(); // Solo interesa la primera linea

                // Ejemplo salida: "GUID del plan de energía:
                // 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c (Alto rendimiento)"
                if (line != null && !line.isEmpty()) {
                    String cleanLine = line.trim();
                    Platform.runLater(() -> {
                        // Añadir info de verificación al label
                        String currentText = profileStatusLabel.getText();
                        if (!currentText.contains("Plan Activo:")) {
                            profileStatusLabel.setText(currentText + "\n🔍 Plan Activo en Windows: " + cleanLine);
                        } else {
                            // Reemplazar la linea anterior de verificacion si ya existia
                            profileStatusLabel
                                    .setText(currentText.split("\n")[0] + "\n🔍 Plan Activo en Windows: " + cleanLine);
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void executeRegCmd(String key, String valName, String valData) {
        try {
            // reg add Key /v ValName /t REG_DWORD /d ValData /f
            String cmd = "reg add \"" + key + "\" /v " + valName + " /t REG_DWORD /d " + valData + " /f";
            Runtime.getRuntime().exec(cmd);
        } catch (Exception ignored) {
        }
    }

    // ==========================================
    // DRIVER MANAGER
    // ==========================================

    private VBox createDriversSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        VBox.setVgrow(box, Priority.ALWAYS);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("🛠 Gestor de Drivers (Controladores)");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        Button btnRefresh = new Button("🔄 Escanear Drivers");
        btnRefresh.setOnAction(e -> loadDrivers());

        Button btnDevMgmt = new Button("⚙ Abrir Admin. Dispositivos");
        btnDevMgmt.setOnAction(e -> openDeviceManager());

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Buscar driver...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(driver -> {
                if (newVal == null || newVal.isEmpty())
                    return true;
                String lower = newVal.toLowerCase();
                return driver.displayName.toLowerCase().contains(lower) ||
                        driver.moduleName.toLowerCase().contains(lower) ||
                        driver.description.toLowerCase().contains(lower);
            });
        });

        header.getChildren().addAll(lbl, btnRefresh, btnDevMgmt, new Separator(javafx.geometry.Orientation.VERTICAL),
                searchField);

        driverTable = new TableView<>();

        TableColumn<DriverInfo, String> colName = new TableColumn<>("Nombre Módulo");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().moduleName));
        colName.setPrefWidth(150);

        TableColumn<DriverInfo, String> colDisplay = new TableColumn<>("Nombre Mostrado");
        colDisplay.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().displayName));
        colDisplay.setPrefWidth(250);

        TableColumn<DriverInfo, String> colType = new TableColumn<>("Tipo");
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().driverType));
        colType.setPrefWidth(120);

        TableColumn<DriverInfo, String> colDate = new TableColumn<>("Fecha Enlace");
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().linkDate));
        colDate.setPrefWidth(150);

        driverTable.getColumns().addAll(colName, colDisplay, colType, colDate);

        filteredData = new FilteredList<>(masterData, p -> true);
        driverTable.setItems(filteredData);
        VBox.setVgrow(driverTable, Priority.ALWAYS);

        driverStatusLabel = new Label("Presiona 'Escanear' para listar drivers.");

        box.getChildren().addAll(header, driverTable, driverStatusLabel);
        return box;
    }

    private void openDeviceManager() {
        try {
            // devmgmt.msc
            Runtime.getRuntime().exec("cmd /c start devmgmt.msc");
        } catch (Exception e) {
            driverStatusLabel.setText("❌ Error abriendo Admin. Dispositivos");
        }
    }

    private void loadDrivers() {
        driverStatusLabel.setText("⏳ Escaneando (driverquery)...");
        masterData.clear();

        new Thread(() -> {
            List<DriverInfo> list = new ArrayList<>();
            try {
                // driverquery /v /fo csv
                Process p = Runtime.getRuntime().exec("driverquery /v /fo csv");

                // Intentar leer con encoding correcto (CP850 o similar en Windows español, o
                // default)
                // Usaremos default del sistema
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));

                String line;
                boolean headerSkipped = false;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty())
                        continue;
                    if (!headerSkipped) {
                        headerSkipped = true; // Skip CSV header
                        // "Module Name","Display Name",...
                        continue;
                    }

                    // Simple CSV Parse: split by comma ONLY if outside quotes
                    // Regex: ,(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)
                    // Esto separa por comas que tengan un numero par de comillas a su derecha
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                    // Cleanup quotes
                    for (int i = 0; i < parts.length; i++) {
                        parts[i] = parts[i].replace("\"", "");
                    }

                    if (parts.length >= 4) {
                        // Indices tipicos: 0=Module, 1=Display, 2=Desc, 3=Type, ..., 12=LinkDate
                        // Depende de la version de Windows
                        // Module, Display, Desc, Type are usually first 4
                        String mod = parts[0];
                        String disp = parts.length > 1 ? parts[1] : "";
                        String desc = parts.length > 2 ? parts[2] : "";
                        String type = parts.length > 3 ? parts[3] : "";
                        String date = parts.length > 12 ? parts[12] : "N/A";

                        list.add(new DriverInfo(mod, disp, desc, type, date));
                    }
                }

                Platform.runLater(() -> {
                    masterData.setAll(list);
                    driverStatusLabel.setText("✅ Encontrados " + list.size() + " drivers.");
                });

            } catch (Exception e) {
                Platform.runLater(() -> driverStatusLabel.setText("❌ Error ejecutando driverquery: " + e.getMessage()));
            }
        }).start();
    }

    // Inner class
    public static class DriverInfo {
        String moduleName;
        String displayName;
        String description;
        String driverType;
        String linkDate;

        public DriverInfo(String moduleName, String displayName, String description, String driverType,
                String linkDate) {
            this.moduleName = moduleName;
            this.displayName = displayName;
            this.description = description;
            this.driverType = driverType;
            this.linkDate = linkDate;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getDisplayName() {
            return displayName;
        }
        // Getters for PropertyValueFactory (if used reflection)
        // But we used lambda in column setup, so these strictly aren't needed but good
        // practice
    }
}
