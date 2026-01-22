package org.pcr.gui.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.pcr.clean.DiskSpaceAnalyzer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class DiskAnalyzerTab extends VBox {

    private TextField directoryField;
    private Button analyzeButton;
    private Label statusLabel;
    private PieChart pieChart;
    private TableView<LargeFile> largeFilesTable;
    private ObservableList<LargeFile> masterData = FXCollections.observableArrayList();
    private FilteredList<LargeFile> filteredData;
    private Label summaryLabel;
    private Button resetButton; // Botón para quitar filtro
    private ProgressBar progressBar;
    private Label progressLabel;

    public DiskAnalyzerTab() {
        setSpacing(15);
        setPadding(new Insets(20));

        Label titleLabel = new Label("💾 Análisis de Espacio en Disco");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        HBox directoryBox = createDirectorySelector();
        HBox buttonBox = createButtonBox();

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        summaryLabel = new Label("");
        summaryLabel.setFont(Font.font("System", 12));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setVisible(false);

        progressLabel = new Label("");
        progressLabel.getStyleClass().add("muted-label");

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);

        pieChart = new PieChart();
        pieChart.setTitle("Distribución por Categoría (Click para filtrar)");
        pieChart.setLegendVisible(true);

        largeFilesTable = createLargeFilesTable();

        filteredData = new FilteredList<>(masterData, p -> true);
        largeFilesTable.setItems(filteredData);

        splitPane.getItems().addAll(pieChart, largeFilesTable);
        splitPane.setDividerPositions(0.4);

        HBox progressBox = new HBox(10, progressBar, progressLabel);
        progressBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(titleLabel, new Separator(), directoryBox, buttonBox,
                statusLabel, summaryLabel, progressBox, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
    }

    private HBox createDirectorySelector() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Carpeta a analizar:");
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

    private HBox createButtonBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        analyzeButton = new Button("📊 Analizar Disco");
        analyzeButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold;");
        analyzeButton.setOnAction(e -> analyzeDisk());

        Button clearButton = new Button("Limpiar Resultados");
        clearButton.setOnAction(e -> {
            pieChart.getData().clear();
            masterData.clear();
            summaryLabel.setText("");
        });

        resetButton = new Button("Mostrar Todo");
        resetButton.setVisible(false);
        resetButton.setOnAction(e -> {
            filteredData.setPredicate(p -> true);
            resetButton.setVisible(false);
            pieChart.getData().forEach(d -> d.getNode().setStyle("")); // Reset styles if any
        });

        box.getChildren().addAll(analyzeButton, clearButton, resetButton);
        return box;
    }

    private TableView<LargeFile> createLargeFilesTable() {
        TableView<LargeFile> table = new TableView<>();

        TableColumn<LargeFile, String> fileCol = new TableColumn<>("Archivo Grande");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileCol.setPrefWidth(250);

        TableColumn<LargeFile, String> pathCol = new TableColumn<>("Ruta");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathCol.setPrefWidth(300);

        TableColumn<LargeFile, String> sizeCol = new TableColumn<>("Tamaño");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));
        sizeCol.setComparator((s1, s2) -> 0);

        TableColumn<LargeFile, Long> sizeValCol = new TableColumn<>("Tamaño");
        sizeValCol.setCellValueFactory(new PropertyValueFactory<>("rawSize"));
        sizeValCol.setCellFactory(column -> new TableCell<LargeFile, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DiskSpaceAnalyzer.humanReadableSize(item));
                }
            }
        });
        sizeValCol.setPrefWidth(100);
        sizeValCol.setComparator(Comparator.naturalOrder()); // Orden numérico natural

        TableColumn<LargeFile, String> categoryCol = new TableColumn<>("Categoría");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<LargeFile, Void> actionCol = new TableColumn<>("Acciones");
        actionCol.setPrefWidth(180);

        Callback<TableColumn<LargeFile, Void>, TableCell<LargeFile, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<LargeFile, Void> call(final TableColumn<LargeFile, Void> param) {
                return new TableCell<>() {
                    private final Button deleteBtn = new Button("🗑");
                    private final Button openBtn = new Button("📂");
                    private final HBox pane = new HBox(5, openBtn, deleteBtn);

                    {
                        pane.setAlignment(Pos.CENTER);

                        deleteBtn.setTooltip(new Tooltip("Eliminar Archivo"));
                        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        deleteBtn.setOnAction((event) -> {
                            LargeFile file = getTableView().getItems().get(getIndex());
                            handleDelete(file);
                        });

                        openBtn.setTooltip(new Tooltip("Abrir Ubicación"));
                        openBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                        openBtn.setOnAction((event) -> {
                            LargeFile file = getTableView().getItems().get(getIndex());
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
                                LargeFile file = getTableView().getItems().get(getIndex());
                                if (file != null && isSystemFile(file.getPath())) {
                                    deleteBtn.setDisable(true);
                                    deleteBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
                                } else {
                                    deleteBtn.setDisable(false);
                                    deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
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

        table.getColumns().addAll(fileCol, pathCol, sizeValCol, categoryCol, actionCol);
        sizeValCol.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(sizeValCol);

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

    private void handleDelete(LargeFile file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este archivo?");
        alert.setContentText(file.getFileName() + "\n" + file.getFormattedSize());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Files.delete(Path.of(file.getPath()));
                    masterData.remove(file);
                    statusLabel.setText("✅ Archivo eliminado: " + file.getFileName());
                } catch (IOException e) {
                    showAlert("Error", "No se pudo eliminar el archivo: " + e.getMessage());
                }
            }
        });
    }

    private void analyzeDisk() {
        String directory = directoryField.getText();
        if (directory == null || directory.trim().isEmpty()) {
            showAlert("Error", "Por favor selecciona una carpeta");
            return;
        }

        analyzeButton.setDisable(true);
        pieChart.getData().clear();
        masterData.clear();
        summaryLabel.setText("");
        statusLabel.setText("🔄 Analizando...");
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        progressLabel.setText("Preparando...");
        resetButton.setVisible(false);

        new Thread(() -> {
            try {
                DiskSpaceAnalyzer analyzer = new DiskSpaceAnalyzer(100 * 1024 * 1024); // 100MB threshold
                analyzer.setProgressCallback(msg -> Platform.runLater(() -> statusLabel.setText("🔄 " + msg)));
                analyzer.setProgressValueCallback(val -> Platform.runLater(() -> updateProgress(val)));

                DiskSpaceAnalyzer.DiskAnalysisResult result = analyzer.analyze(Path.of(directory));

                Platform.runLater(() -> {
                    result.sizeByCategory.forEach((category, size) -> {
                        if (size > 0) {
                            PieChart.Data data = new PieChart.Data(
                                    category + " (" + DiskSpaceAnalyzer.humanReadableSize(size) + ")",
                                    size);
                            pieChart.getData().add(data);

                            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                                filteredData.setPredicate(file -> file.getCategory().equals(category));
                                statusLabel.setText("Filtro aplicado: " + category);
                                resetButton.setVisible(true);
                            });
                        }
                    });

                    int count = 0;
                    for (DiskSpaceAnalyzer.FileInfo fileInfo : result.largeFiles) {
                        masterData.add(new LargeFile(
                                fileInfo.path.getFileName().toString(),
                                fileInfo.path.toString(),
                                fileInfo.size,
                                fileInfo.category));
                    }

                    largeFilesTable.sort();

                    String summary = String.format("✅ Análisis completado - Tamaño total: %s - Archivos grandes: %d",
                            DiskSpaceAnalyzer.humanReadableSize(result.totalSize),
                            result.largeFiles.size());

                    summaryLabel.setText(summary);
                    statusLabel.setText("✅ Análisis completado");
                    statusLabel.setStyle("-fx-text-fill: #28a745;");
                    updateProgress(1d);
                    analyzeButton.setDisable(false);
                    progressLabel.setText("Listo");
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    analyzeButton.setDisable(false);
                    progressBar.setVisible(false);
                    progressLabel.setText("");
                });
            }
        }).start();
    }

    private void updateProgress(double value) {
        if (value < 0) {
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            progressLabel.setText("Calculando...");
        } else {
            progressBar.setProgress(value);
            int percent = (int) Math.min(100, Math.round(value * 100));
            progressLabel.setText(percent + "%");
            if (value >= 1d) {
                progressBar.setVisible(false);
            } else {
                progressBar.setVisible(true);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class LargeFile {
        private final String fileName;
        private final String path;
        private final long rawSize;
        private final String category;

        public LargeFile(String fileName, String path, long rawSize, String category) {
            this.fileName = fileName;
            this.path = path;
            this.rawSize = rawSize; // En bytes
            this.category = category;
        }

        public String getFileName() {
            return fileName;
        }

        public String getPath() {
            return path;
        }

        public long getRawSize() {
            return rawSize;
        }

        public String getFormattedSize() {
            return DiskSpaceAnalyzer.humanReadableSize(rawSize);
        }

        public String getCategory() {
            return category;
        }
    }
}
