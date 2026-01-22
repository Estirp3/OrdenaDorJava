// src/main/java/org/pcr/gui/tabs/SystemCleanerTab.java
package org.pcr.gui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.pcr.clean.SystemCleaner;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SystemCleanerTab extends VBox {

    private CheckBox tempFilesCheck;
    private CheckBox dnsCheck;
    private RadioButton safeMode;
    private RadioButton aggressiveMode;
    private TextArea logArea;
    private Button cleanButton;
    private Label statusLabel;

    public SystemCleanerTab() {
        setSpacing(15);
        setPadding(new Insets(20));

        Label titleLabel = new Label("🧹 Limpiador del Sistema");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        VBox optionsBox = createOptionsBox();
        VBox modeBox = createModeSelector();
        HBox buttonBox = createButtonBox();

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(15);
        logArea.setPromptText("Los resultados de la limpieza aparecerán aquí...");

        getChildren().addAll(titleLabel, new Separator(), optionsBox, modeBox, buttonBox, statusLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
    }

    private VBox createOptionsBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 10;");

        Label label = new Label("Selecciona qué limpiar:");
        label.setFont(Font.font("System", FontWeight.BOLD, 14));

        tempFilesCheck = new CheckBox("Archivos temporales del sistema");
        tempFilesCheck.setSelected(true);

        dnsCheck = new CheckBox("Caché DNS y configuración de red (requiere permisos)");

        box.getChildren().addAll(label, tempFilesCheck, dnsCheck);
        return box;
    }

    private VBox createModeSelector() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 10;");

        Label label = new Label("Modo de limpieza:");
        label.setFont(Font.font("System", FontWeight.BOLD, 14));

        ToggleGroup modeGroup = new ToggleGroup();

        safeMode = new RadioButton("Seguro (recomendado)");
        safeMode.setToggleGroup(modeGroup);
        safeMode.setSelected(true);

        aggressiveMode = new RadioButton("Agresivo (puede requerir permisos de administrador)");
        aggressiveMode.setToggleGroup(modeGroup);

        box.getChildren().addAll(label, safeMode, aggressiveMode);
        return box;
    }

    private HBox createButtonBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        cleanButton = new Button("🚀 Iniciar Limpieza");
        cleanButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        cleanButton.setOnAction(e -> startCleaning());

        Button clearLogButton = new Button("Limpiar Log");
        clearLogButton.setOnAction(e -> logArea.clear());

        box.getChildren().addAll(cleanButton, clearLogButton);
        return box;
    }

    private void startCleaning() {
        if (!tempFilesCheck.isSelected() && !dnsCheck.isSelected()) {
            showAlert("Advertencia", "Por favor selecciona al menos una opción de limpieza");
            return;
        }

        cleanButton.setDisable(true);
        logArea.clear();
        statusLabel.setText("🔄 Limpiando...");

        SystemCleaner.Mode mode = aggressiveMode.isSelected() ? SystemCleaner.Mode.AGGRESSIVE : SystemCleaner.Mode.SAFE;

        new Thread(() -> {
            try {
                Path tempDir = Paths.get(System.getProperty("user.home"));
                Path destDir = tempDir.resolve("_ordenado");

                SystemCleaner cleaner = new SystemCleaner(tempDir, destDir);
                SystemCleaner.CleanResult result = cleaner.run(mode);

                Platform.runLater(() -> {
                    StringBuilder log = new StringBuilder();
                    log.append("=== Limpieza Completada ===\n\n");
                    log.append(String.format("Total liberado: %.2f MB\n\n",
                            result.totalBytesFreed / (1024.0 * 1024.0)));

                    log.append("Eventos:\n");
                    result.events.forEach(event -> {
                        String icon = event.success ? "✅" : "❌";
                        log.append(String.format("%s %s - %s\n", icon, event.name, event.detail));
                    });

                    if (!result.dnsCommandsTried.isEmpty()) {
                        log.append("\nComandos DNS ejecutados:\n");
                        result.dnsCommandsTried.forEach(cmd -> {
                            Integer exitCode = result.dnsExitCodes.get(cmd);
                            String status = (exitCode != null && exitCode == 0) ? "✅" : "❌";
                            log.append(String.format("%s %s (exit: %d)\n", status, cmd, exitCode));
                        });
                    }

                    logArea.setText(log.toString());
                    statusLabel.setText(String.format("✅ Completado - %.2f MB liberados",
                            result.totalBytesFreed / (1024.0 * 1024.0)));
                    statusLabel.setStyle("-fx-text-fill: #28a745;");
                    cleanButton.setDisable(false);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    logArea.setText("❌ Error: " + ex.getMessage());
                    statusLabel.setText("❌ Error en la limpieza");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    cleanButton.setDisable(false);
                });
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
