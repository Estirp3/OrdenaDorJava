package org.pcr.gui.tabs;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StartupManagerTab extends VBox {

    private TableView<StartupItem> startupTable;
    private Label statusLabel;
    private ToggleButton fastBootToggle;
    private Label fastBootStateBadge;

    public StartupManagerTab() {
        setSpacing(15);
        setPadding(new Insets(20));

        Label titleLabel = new Label("🚀 Optimización del Inicio de Windows");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Fast Boot Section
        HBox fastBootBox = createFastBootSection();

        // Startup Apps Section
        Label appsLabel = new Label("Programas de Inicio Automático (Usuario Actual):");
        appsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        startupTable = createStartupTable();

        HBox actionBox = createActionBox();

        statusLabel = new Label("");

        getChildren().addAll(titleLabel, new Separator(),
                fastBootBox, new Separator(),
                appsLabel, startupTable, actionBox, statusLabel);

        VBox.setVgrow(startupTable, Priority.ALWAYS);

        refreshStartupItems();
        checkFastBootStatus();
    }

    private HBox createFastBootSection() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("card-pane");

        Label label = new Label("Inicio Rápido (Fast Boot):");
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        fastBootToggle = new ToggleButton("Analizando...");
        fastBootToggle.setOnAction(e -> toggleFastBoot());

        fastBootStateBadge = new Label("Leyendo...");
        fastBootStateBadge.getStyleClass().addAll("pill", "pill-muted");

        Label infoLabel = new Label("Desactivar para evitar caché persistente y asegurar apagado completo.");
        infoLabel.getStyleClass().add("muted-label");
        infoLabel.setWrapText(true);

        Label restartLabel = new Label("Tip: Windows puede requerir reinicio para aplicar por completo el cambio de Inicio Rápido.");
        restartLabel.getStyleClass().add("muted-label");
        restartLabel.setWrapText(true);

        Button openPowerSettings = new Button("Abrir configuración de energía");
        openPowerSettings.getStyleClass().add("ghost-button");
        openPowerSettings.setOnAction(e -> openPowerOptions());

        VBox textBox = new VBox(6, label, infoLabel, restartLabel);
        VBox toggleBox = new VBox(6, fastBootToggle, fastBootStateBadge);
        VBox actionsBox = new VBox(8, openPowerSettings);

        box.getChildren().addAll(textBox, toggleBox, actionsBox);
        return box;
    }

    private HBox createActionBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Button refreshButton = new Button("🔄 Actualizar Lista");
        refreshButton.setOnAction(e -> manualRefresh());

        Button deleteButton = new Button("🛑 Desactivar / Eliminar");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteSelected());

        box.getChildren().addAll(refreshButton, deleteButton);
        return box;
    }

    private TableView<StartupItem> createStartupTable() {
        TableView<StartupItem> table = new TableView<>();
        table.setPlaceholder(new Label("No se encontraron programas de inicio o no es Windows."));

        TableColumn<StartupItem, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name));
        nameCol.setPrefWidth(200);

        TableColumn<StartupItem, String> commandCol = new TableColumn<>("Comando / Ruta");
        commandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().command));
        commandCol.setPrefWidth(400);

        table.getColumns().addAll(nameCol, commandCol);
        return table;
    }

    // --- Logic ---

    private void refreshStartupItems() {
        if (!isWindows()) {
            statusLabel.setText("⚠️ Esta función solo está disponible en Windows.");
            return;
        }

        new Thread(() -> {
            List<StartupItem> items = new ArrayList<>();
            try {
                // Query HKCU Run
                Process process = Runtime.getRuntime()
                        .exec("reg query HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("HKEY"))
                        continue;
                    // Format: Name REG_SZ Command
                    String[] parts = line.trim().split("    REG_SZ    ");
                    if (parts.length >= 2) {
                        items.add(new StartupItem(parts[0], parts[1]));
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error leyendo registro: " + e.getMessage()));
            }

            Platform.runLater(() -> startupTable.getItems().setAll(items));
        }).start();
    }

    private void deleteSelected() {
        StartupItem selected = startupTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección", "Por favor selecciona un programa de la lista.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Quitar '" + selected.name + "' del inicio?");
        confirm.setContentText("El programa dejará de iniciarse automáticamente con Windows.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    // reg delete HKCU\...\Run /v Name /f
                    String cmd = "reg delete \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" /v \""
                            + selected.name + "\" /f";
                    Process p = Runtime.getRuntime().exec(cmd);
                    int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        refreshStartupItems();
                        statusLabel.setText("✅ Eliminado correctamente.");
                    } else {
                        showAlert("Error", "No se pudo eliminar. Código de error: " + exitCode);
                    }
                } catch (Exception e) {
                    showAlert("Error", "Excepción: " + e.getMessage());
                }
            }
        });
    }

    private void checkFastBootStatus() {
        if (!isWindows())
            return;
        new Thread(() -> {
            try {
                String cmd = "reg query \"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Power\" /v HiberbootEnabled /reg:64";
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                String foundValue = "No encontrado";
                boolean isEnabled = false;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("HiberbootEnabled")) {
                        // Buscamos 0x1 o 0x0
                        if (line.contains("0x1")) {
                            isEnabled = true;
                            foundValue = "Activado (0x1)";
                        } else if (line.contains("0x0")) {
                            isEnabled = false;
                            foundValue = "Desactivado (0x0)";
                        } else {
                            // Extraer el valor hexadecimal si es otro
                            foundValue = line.trim();
                        }
                    }
                }

                boolean finalEnabled = isEnabled;
                String debugVal = foundValue;

                Platform.runLater(() -> {
                    updateFastBootUI(finalEnabled);
                    // Solo mostramos tooltip o log, no popup invasivo, salvo si el usuario lo pide
                    // Pero aca actualizamos el texto del toggle para ser explicito
                    if (finalEnabled) {
                        fastBootToggle.setText("ACTIVADO (Click para desactivar)");
                    } else {
                        fastBootToggle.setText("DESACTIVADO (Sistema optimizado)");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> fastBootToggle.setText("Error al leer estado"));
            }
        }).start();
    }

    // Método auxiliar para el botón "Actualizar Lista" que SÍ muestra popup
    private void manualRefresh() {
        refreshStartupItems();

        // Check fastboot con popup
        new Thread(() -> {
            try {
                String cmd = "reg query \"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Power\" /v HiberbootEnabled /reg:64";
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() > 0)
                        output.append(line).append("\n");
                }

                Platform.runLater(() -> {
                    checkFastBootStatus(); // Update UI standard
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Diagnóstico de Registro");
                    alert.setHeaderText("Valor real en el registro:");
                    alert.setContentText(
                            output.toString().isEmpty() ? "No se pudo leer la clave (¿Permisos?)" : output.toString());
                    alert.showAndWait();
                });
            } catch (Exception e) {
            }
        }).start();
    }

    private void updateFastBootUI(boolean enabled) {
        if (enabled) {
            fastBootToggle.setSelected(true);
            fastBootToggle.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            if (fastBootStateBadge != null) {
                fastBootStateBadge.setText("Activado (registro 64-bit)");
                fastBootStateBadge.getStyleClass().setAll("pill", "pill-success");
            }
        } else {
            fastBootToggle.setSelected(false);
            fastBootToggle.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
            if (fastBootStateBadge != null) {
                fastBootStateBadge.setText("Desactivado (registro 64-bit)");
                fastBootStateBadge.getStyleClass().setAll("pill", "pill-muted");
            }
        }
    }

    private void toggleFastBoot() {
        // El ToggleButton ya cambió su estado antes de disparar el evento, así que
        // isSelected refleja el estado deseado (tras el click), no el previo.
        boolean targetEnabled = fastBootToggle.isSelected();
        String newVal = targetEnabled ? "1" : "0"; // 0 para desactivar, 1 para activar

        Platform.runLater(() -> {
            fastBootToggle.setDisable(true);
            statusLabel.setText("⏳ Lanzando script CMD... Acepta los permisos.");
        });

        new Thread(() -> {
            try {
                // 1. Crear archivo temporal .bat
                // Los BAT son más amigables con permisos que los PS1 en algunas configuraciones
                java.nio.file.Path scriptPath = java.nio.file.Files.createTempFile("ordenador_fastboot_fix", ".bat");
                String scriptContent = "@echo off\r\n" +
                        "title OrdenaDor - Cambiando Configuracion\r\n" +
                        "echo Solicitando acceso al registro...\r\n" +
                        "\r\n" +
                        "rem Intentamos cambiar la clave HiberbootEnabled\r\n" +
                        "reg add \"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Power\" /v HiberbootEnabled /t REG_DWORD /d "
                        + newVal + " /f\r\n" +
                        "if %errorlevel% neq 0 (\r\n" +
                        "    reg add \"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Power\" /v HiberbootEnabled /t REG_DWORD /d "
                        + newVal + " /f /reg:64\r\n" +
                        ")\r\n" +
                        "\r\n" +
                        "if %errorlevel% neq 0 (\r\n" +
                        "    color 4f\r\n" +
                        "    echo [ERROR CRITICO] No se pudo escribir en el registro.\r\n" +
                        "    echo Asegurate de haber dado click en SI en la ventana de control de cuentas de usuario.\r\n"
                        +
                        "    pause\r\n" +
                        "    exit /b 1\r\n" +
                        ")\r\n" +
                        "\r\n" +
                        "color 2f\r\n" +
                        "echo [EXITO] Configuracion cambiada correctamente.\r\n" +
                        "timeout /t 2 >nul\r\n" +
                        "exit /b 0\r\n";

                java.nio.file.Files.writeString(scriptPath, scriptContent);

                // 2. Ejecutar ese .bat como Administrador usando PowerShell Start-Process solo
                // como lanzador
                // Esto evita las políticas de ejecución de scripts de PS, ya que ejecutamos un
                // binario (cmd/bat)
                String cmd = "powershell -Command \"Start-Process -FilePath '" + scriptPath.toAbsolutePath().toString()
                        + "' -Verb RunAs -Wait\"";

                System.out.println("Lanzando BAT: " + cmd);

                Process p = Runtime.getRuntime().exec(cmd);
                int code = p.waitFor();

                // Borrar script después de un momento
                try {
                    Thread.sleep(1000);
                    java.nio.file.Files.deleteIfExists(scriptPath);
                } catch (Exception ignored) {
                }

                Platform.runLater(() -> {
                    // 3. Verificar
                    checkFastBootStatus();
                    fastBootToggle.setDisable(false);

                    if (code == 0) {
                        statusLabel.setText("✅ Comando finalizado. Verifica el botón.");
                    } else {
                        statusLabel.setText("❌ Error o cancelado (Código " + code + ").");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error Java: " + e.getMessage());
                    fastBootToggle.setDisable(false);
                });
            }
        }).start();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private void openPowerOptions() {
        if (!isWindows()) {
            showAlert("Solo Windows", "Esta acción abre el panel de energía de Windows.");
            return;
        }
        new Thread(() -> {
            try {
                Runtime.getRuntime().exec("control.exe /name Microsoft.PowerOptions");
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("No se pudo abrir", "Intenta abrir Configuración > Energía manualmente.\n" + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for table
    public static class StartupItem {
        String name;
        String command;

        public StartupItem(String name, String command) {
            this.name = name;
            this.command = command;
        }
    }
}
