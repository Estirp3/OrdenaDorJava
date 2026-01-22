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
import org.pcr.clean.DuplicateFileFinder;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DuplicateFinderTab extends VBox {

    private TextField directoryField;
    private TextField minSizeField;
    private Button scanButton;
    private Label statusLabel;
    private TableView<DuplicateGroup> resultsTable;
    private Label summaryLabel;

    public DuplicateFinderTab() {
        setSpacing(15);
        setPadding(new Insets(20));

        Label titleLabel = new Label("🔍 Detector de Archivos Duplicados");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        HBox directoryBox = createDirectorySelector();
        HBox optionsBox = createOptionsBox();
        HBox buttonBox = createButtonBox();

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        summaryLabel = new Label("");
        summaryLabel.setFont(Font.font("System", 12));

        resultsTable = createResultsTable();

        getChildren().addAll(titleLabel, new Separator(), directoryBox, optionsBox, buttonBox,
                statusLabel, summaryLabel, resultsTable);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
    }

    private HBox createDirectorySelector() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Carpeta a escanear:");
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

    private HBox createOptionsBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Tamaño mínimo (MB):");
        minSizeField = new TextField("1");
        minSizeField.setPrefWidth(80);

        box.getChildren().addAll(label, minSizeField);
        return box;
    }

    private HBox createButtonBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        scanButton = new Button("🔍 Buscar Duplicados");
        scanButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold;");
        scanButton.setOnAction(e -> scanForDuplicates());

        Button clearButton = new Button("Limpiar Resultados");
        clearButton.setOnAction(e -> {
            resultsTable.getItems().clear();
            summaryLabel.setText("");
        });

        box.getChildren().addAll(scanButton, clearButton);
        return box;
    }

    private TableView<DuplicateGroup> createResultsTable() {
        TableView<DuplicateGroup> table = new TableView<>();

        TableColumn<DuplicateGroup, String> fileCol = new TableColumn<>("Archivo");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileCol.setPrefWidth(300);

        TableColumn<DuplicateGroup, String> pathCol = new TableColumn<>("Ruta");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathCol.setPrefWidth(400);

        TableColumn<DuplicateGroup, String> sizeCol = new TableColumn<>("Tamaño");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(100);

        TableColumn<DuplicateGroup, Void> actionCol = new TableColumn<>("Acciones");
        actionCol.setPrefWidth(200);

        Callback<TableColumn<DuplicateGroup, Void>, TableCell<DuplicateGroup, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<DuplicateGroup, Void> call(final TableColumn<DuplicateGroup, Void> param) {
                return new TableCell<>() {
                    private final Button deleteBtn = new Button("🗑 Eliminar");
                    private final Button openBtn = new Button("📂 Abrir");
                    private final HBox pane = new HBox(5, openBtn, deleteBtn);

                    {
                        pane.setAlignment(Pos.CENTER);

                        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px;");
                        deleteBtn.setOnAction((event) -> {
                            DuplicateGroup file = getTableView().getItems().get(getIndex());
                            handleDelete(file);
                        });

                        openBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 11px;");
                        openBtn.setOnAction((event) -> {
                            DuplicateGroup file = getTableView().getItems().get(getIndex());
                            openLocation(file.getPath());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                                DuplicateGroup file = getTableView().getItems().get(getIndex());
                                if (file != null && isSystemFile(file.getPath())) {
                                    deleteBtn.setDisable(true);
                                    deleteBtn.setTooltip(new Tooltip("No se pueden eliminar archivos del sistema"));
                                    deleteBtn.setStyle(
                                            "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 11px;");
                                } else {
                                    deleteBtn.setDisable(false);
                                    deleteBtn.setTooltip(null);
                                    deleteBtn.setStyle(
                                            "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px;");
                                }
                                setGraphic(pane);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };

        actionCol.setCellFactory(cellFactory);

        table.getColumns().addAll(fileCol, pathCol, sizeCol, actionCol);
        return table;
    }

    private void openLocation(String pathStr) {
        if (pathStr == null)
            return;
        new Thread(() -> {
            try {
                File file = new File(pathStr);
                if (file.exists()) {
                    Desktop.getDesktop().open(file.getParentFile());
                }
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Error", "No se pudo abrir la ubicación: " + e.getMessage()));
            }
        }).start();
    }

    private boolean isSystemFile(String pathStr) {
        if (pathStr == null)
            return false;
        String lowerPath = pathStr.toLowerCase();
        return lowerPath.contains("windows") ||
                lowerPath.contains("program files") ||
                lowerPath.contains("programdata") ||
                lowerPath.contains("system32") ||
                lowerPath.contains("appdata");
    }

    private void handleDelete(DuplicateGroup file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este archivo duplicado?");
        alert.setContentText(file.getFileName() + "\n" + file.getSize());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Files.delete(Path.of(file.getPath()));
                    resultsTable.getItems().remove(file);
                    statusLabel.setText("✅ Archivo eliminado: " + file.getFileName());
                } catch (IOException e) {
                    showAlert("Error", "No se pudo eliminar el archivo: " + e.getMessage());
                }
            }
        });
    }

    private void scanForDuplicates() {
        String directory = directoryField.getText();
        if (directory == null || directory.trim().isEmpty()) {
            showAlert("Error", "Por favor selecciona una carpeta");
            return;
        }

        long minSize;
        try {
            minSize = (long) (Double.parseDouble(minSizeField.getText()) * 1024 * 1024);
        } catch (NumberFormatException e) {
            showAlert("Error", "Tamaño mínimo inválido");
            return;
        }

        scanButton.setDisable(true);
        resultsTable.getItems().clear();
        summaryLabel.setText("");
        statusLabel.setText("🔄 Escaneando...");

        new Thread(() -> {
            try {
                DuplicateFileFinder finder = new DuplicateFileFinder(minSize);
                finder.setProgressCallback(msg -> Platform.runLater(() -> statusLabel.setText("🔄 " + msg)));

                DuplicateFileFinder.DuplicateResult result = finder.findDuplicates(Path.of(directory));

                Platform.runLater(() -> {
                    result.duplicateGroups.forEach((hash, files) -> {
                        for (Path file : files) {
                            try {
                                long size = Files.size(file);
                                resultsTable.getItems().add(new DuplicateGroup(
                                        file.getFileName().toString(),
                                        file.toString(),
                                        formatSize(size)));
                            } catch (Exception ignored) {
                            }
                        }
                    });

                    String summary = String.format(
                            "✅ Encontrados %d archivos duplicados en %d grupos - Espacio recuperable: %.2f MB",
                            result.totalDuplicateFiles,
                            result.duplicateGroups.size(),
                            result.totalDuplicateSize / (1024.0 * 1024.0));

                    summaryLabel.setText(summary);
                    statusLabel.setText("✅ Escaneo completado");
                    statusLabel.setStyle("-fx-text-fill: #28a745;");
                    scanButton.setDisable(false);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    scanButton.setDisable(false);
                });
            }
        }).start();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class DuplicateGroup {
        private final String fileName;
        private final String path;
        private final String size;

        public DuplicateGroup(String fileName, String path, String size) {
            this.fileName = fileName;
            this.path = path;
            this.size = size;
        }

        public String getFileName() {
            return fileName;
        }

        public String getPath() {
            return path;
        }

        public String getSize() {
            return size;
        }
    }
}
