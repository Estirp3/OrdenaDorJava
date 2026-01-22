// src/main/java/org/pcr/gui/tabs/FileOrganizerTab.java
package org.pcr.gui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.pcr.AppLocation;
import org.pcr.Config;
import org.pcr.core.FileOrganizer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class FileOrganizerTab extends VBox {

    private TextField directoryField;
    private TextField targetNameField; // Nuevo campo para nombre de carpeta
    private ProgressBar progressBar;
    private Label statusLabel;
    private TableView<FileResult> resultsTable;
    private Button organizeButton;

    public FileOrganizerTab() {
        setSpacing(15);
        setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("📁 Organizador de Archivos");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Directory selection
        HBox directoryBox = createDirectorySelector();

        // Target folder name selection
        HBox targetBox = createTargetNameSelector();

        // Progress section
        VBox progressBox = createProgressSection();

        // Results table
        resultsTable = createResultsTable();

        // Action buttons
        HBox buttonBox = createActionButtons();

        getChildren().addAll(titleLabel, new Separator(), directoryBox, targetBox, buttonBox, progressBox,
                resultsTable);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
    }

    private HBox createDirectorySelector() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Carpeta a organizar:");
        directoryField = new TextField();
        directoryField.setPromptText("Selecciona una carpeta...");
        directoryField.setPrefWidth(400);

        Button browseButton = new Button("Examinar...");
        browseButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Seleccionar carpeta");
            File selected = chooser.showDialog(getScene().getWindow());
            if (selected != null) {
                directoryField.setText(selected.getAbsolutePath());
            }
        });

        box.getChildren().addAll(label, directoryField, browseButton);
        return box;
    }

    private HBox createTargetNameSelector() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Nombre carpeta destino:");
        targetNameField = new TextField("_ordenado");
        targetNameField.setPromptText("Ej: Ordenado, Archivos, Backup...");
        targetNameField.setPrefWidth(200);
        Label hintLabel = new Label("(Se creará dentro de la carpeta seleccionada)");
        hintLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");

        box.getChildren().addAll(label, targetNameField, hintLabel);
        return box;
    }

    private VBox createProgressSection() {
        VBox box = new VBox(5);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);

        statusLabel = new Label("");
        statusLabel.setVisible(false);

        box.getChildren().addAll(progressBar, statusLabel);
        return box;
    }

    private TableView<FileResult> createResultsTable() {
        TableView<FileResult> table = new TableView<>();

        TableColumn<FileResult, String> fileCol = new TableColumn<>("Archivo");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileCol.setPrefWidth(300);

        TableColumn<FileResult, String> categoryCol = new TableColumn<>("Categoría");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);

        TableColumn<FileResult, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);

        TableColumn<FileResult, Void> actionCol = new TableColumn<>("Acción");
        actionCol.setPrefWidth(150);

        Callback<TableColumn<FileResult, Void>, TableCell<FileResult, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<FileResult, Void> call(final TableColumn<FileResult, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("📂 Abrir");

                    {
                        btn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 11px;");
                        btn.setOnAction((event) -> {
                            FileResult result = getTableView().getItems().get(getIndex());
                            openLocation(result.getDestPath());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            FileResult result = getTableView().getItems().get(getIndex());
                            if (result.getDestPath() != null) {
                                setGraphic(btn);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
        actionCol.setCellFactory(cellFactory);

        table.getColumns().addAll(fileCol, categoryCol, statusCol, actionCol);
        return table;
    }

    private void openLocation(Path path) {
        if (path == null)
            return;
        new Thread(() -> {
            try {
                // Try to open parent directory if it's a file, or directory itself if it is a
                // directory
                File file = path.toFile();
                if (file.exists()) {
                    if (file.isFile()) {
                        Desktop.getDesktop().open(file.getParentFile());
                    } else {
                        Desktop.getDesktop().open(file);
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Error", "No se pudo abrir la ubicación: " + e.getMessage()));
            }
        }).start();
    }

    private HBox createActionButtons() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        organizeButton = new Button("🚀 Organizar Archivos");
        organizeButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold;");
        organizeButton.setOnAction(e -> organizeFiles());

        Button clearButton = new Button("Limpiar Resultados");
        clearButton.setOnAction(e -> resultsTable.getItems().clear());

        box.getChildren().addAll(organizeButton, clearButton);
        return box;
    }

    private void organizeFiles() {
        String directory = directoryField.getText();
        if (directory == null || directory.trim().isEmpty()) {
            showAlert("Error", "Por favor selecciona una carpeta");
            return;
        }

        String targetName = targetNameField.getText();
        if (targetName == null || targetName.trim().isEmpty()) {
            targetName = "_ordenado"; // Default fallback
        }

        Path origen = Path.of(directory);
        Path destino = origen.resolve(targetName);
        Map<String, String[]> categorias = Config.getCategorias();

        organizeButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate
        statusLabel.setVisible(true);
        statusLabel.setText("Organizando archivos...");
        resultsTable.getItems().clear();

        final Path finalDest = destino;

        new Thread(() -> {
            try {
                FileOrganizer.Result result = FileOrganizer.organizar(origen, finalDest, categorias,
                        AppLocation.getRunningPath());

                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    statusLabel.setText(String.format("✅ Completado: %d archivos movidos, %d omitidos",
                            result.filesMoved, result.skipped));

                    // Add results to table
                    result.moves.forEach(move -> resultsTable.getItems().add(new FileResult(
                            move.src.getFileName().toString(),
                            move.category,
                            "Movido",
                            move.dst))); // Guardamos ruta destino
                    organizeButton.setDisable(false);
                    showAlert("Éxito", "Archivos organizados correctamente en: " + finalDest);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                    organizeButton.setDisable(false);
                    showAlert("Error", "Error al organizar archivos: " + ex.getMessage());
                });
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

    public static class FileResult {
        private final String fileName;
        private final String category;
        private final String status;
        private final Path destPath; // Nuevo campo

        public FileResult(String fileName, String category, String status, Path destPath) {
            this.fileName = fileName;
            this.category = category;
            this.status = status;
            this.destPath = destPath;
        }

        public String getFileName() {
            return fileName;
        }

        public String getCategory() {
            return category;
        }

        public String getStatus() {
            return status;
        }

        public Path getDestPath() {
            return destPath;
        }
    }
}
