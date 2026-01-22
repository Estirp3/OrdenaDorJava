package org.pcr.gui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.pcr.clean.BrowserCacheCleaner;

import java.util.Map;

public class BrowserCleanerTab extends VBox {

    private Label chromeLabel;
    private Label firefoxLabel;
    private Label edgeLabel;
    private Label braveLabel;
    private Button scanButton;
    private Button cleanButton;
    private TextArea logArea;
    private Label statusLabel;

    public BrowserCleanerTab() {
        setSpacing(15);
        setPadding(new Insets(20));

        Label titleLabel = new Label("🌐 Limpiador de Navegadores");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label warningLabel = new Label("⚠️ Cierra todos los navegadores antes de limpiar");
        warningLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");

        VBox browsersBox = createBrowsersBox();
        HBox buttonBox = createButtonBox();

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(10);
        logArea.setPromptText("Los resultados aparecerán aquí...");

        getChildren().addAll(titleLabel, warningLabel, new Separator(), browsersBox, buttonBox, statusLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
    }

    private VBox createBrowsersBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 10;");

        Label label = new Label("Navegadores detectados:");
        label.setFont(Font.font("System", FontWeight.BOLD, 14));

        chromeLabel = new Label("🔍 Chrome: Escaneando...");
        firefoxLabel = new Label("🔍 Firefox: Escaneando...");
        edgeLabel = new Label("🔍 Edge: Escaneando...");
        braveLabel = new Label("🔍 Brave: Escaneando...");

        box.getChildren().addAll(label, chromeLabel, firefoxLabel, edgeLabel, braveLabel);
        return box;
    }

    private HBox createButtonBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        scanButton = new Button("🔍 Escanear Caché");
        scanButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold;");
        scanButton.setOnAction(e -> scanCaches());

        cleanButton = new Button("🧹 Limpiar Todo");
        cleanButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        cleanButton.setOnAction(e -> cleanCaches());
        cleanButton.setDisable(true);

        box.getChildren().addAll(scanButton, cleanButton);
        return box;
    }

    private void scanCaches() {
        scanButton.setDisable(true);
        statusLabel.setText("🔄 Escaneando...");

        new Thread(() -> {
            Map<String, Long> sizes = BrowserCacheCleaner.getCacheSizes();

            Platform.runLater(() -> {
                updateBrowserLabel(chromeLabel, "Chrome", sizes.get("Chrome"));
                updateBrowserLabel(firefoxLabel, "Firefox", sizes.get("Firefox"));
                updateBrowserLabel(edgeLabel, "Edge", sizes.get("Edge"));
                updateBrowserLabel(braveLabel, "Brave", sizes.get("Brave"));

                long total = sizes.values().stream().mapToLong(Long::longValue).sum();
                statusLabel.setText(String.format("✅ Escaneo completado - Total: %.2f MB", total / (1024.0 * 1024.0)));
                statusLabel.setStyle("-fx-text-fill: #28a745;");

                scanButton.setDisable(false);
                cleanButton.setDisable(total == 0);
            });
        }).start();
    }

    private void updateBrowserLabel(Label label, String browser, Long size) {
        if (size == null || size == 0) {
            label.setText("❌ " + browser + ": No detectado o vacío");
        } else {
            label.setText(String.format("✅ %s: %.2f MB", browser, size / (1024.0 * 1024.0)));
        }
    }

    private void cleanCaches() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar limpieza");
        confirm.setHeaderText("¿Limpiar caché de navegadores?");
        confirm.setContentText("Asegúrate de haber cerrado todos los navegadores antes de continuar.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        cleanButton.setDisable(true);
        scanButton.setDisable(true);
        logArea.clear();
        statusLabel.setText("🔄 Limpiando...");

        new Thread(() -> {
            try {
                BrowserCacheCleaner.BrowserCleanResult result = BrowserCacheCleaner.cleanAll();

                Platform.runLater(() -> {
                    StringBuilder log = new StringBuilder();
                    log.append("=== Limpieza de Navegadores Completada ===\n\n");

                    result.bytesFreedPerBrowser.forEach((browser, bytes) -> {
                        log.append(String.format("✅ %s: %.2f MB liberados\n", browser, bytes / (1024.0 * 1024.0)));
                    });

                    if (!result.errors.isEmpty()) {
                        log.append("\nErrores:\n");
                        result.errors.forEach((browser, error) -> {
                            log.append(String.format("❌ %s: %s\n", browser, error));
                        });
                    }

                    log.append(String.format("\nTotal liberado: %.2f MB\n",
                            result.totalBytesFreed / (1024.0 * 1024.0)));

                    logArea.setText(log.toString());
                    statusLabel.setText(String.format("✅ Completado - %.2f MB liberados",
                            result.totalBytesFreed / (1024.0 * 1024.0)));
                    statusLabel.setStyle("-fx-text-fill: #28a745;");

                    scanButton.setDisable(false);
                    cleanButton.setDisable(false);

                    scanCaches();
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    logArea.setText("❌ Error: " + ex.getMessage());
                    statusLabel.setText("❌ Error en la limpieza");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    scanButton.setDisable(false);
                    cleanButton.setDisable(false);
                });
            }
        }).start();
    }
}
