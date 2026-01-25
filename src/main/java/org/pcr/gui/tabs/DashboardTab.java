package org.pcr.gui.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DashboardTab extends VBox {

        private TabPane tabPane;
        private ListView<String> activityLog;
        private Label kpiOsValue;
        private Label kpiJavaValue;
        private Label kpiStorageValue;
        private FlowPane kpiRow;
        private FlowPane quickActionsPane;

        public DashboardTab(TabPane tabPane) {
                this.tabPane = tabPane;
                setSpacing(18);
                setPadding(new Insets(20));
                setAlignment(Pos.TOP_CENTER);

                Label titleLabel = new Label("🎯 Panel de Control");
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
                titleLabel.setStyle("-fx-text-fill: linear-gradient(to right, #18c29c, #3b82f6);");

                // --- LOGO INTEGRATION ---
                ImageView logoView = null;
                try {
                        // Cargar logo desde resources
                        Image logoImg = new Image(
                                        getClass().getResourceAsStream("/images/logo.png"));
                        logoView = new ImageView(logoImg);
                        logoView.setPreserveRatio(true);
                        logoView.setFitHeight(120); // Ajustar altura
                        logoView.setFitWidth(500); // Max width
                        // Truco para quitar fondo blanco si existe: MULTIPLY
                        logoView.setBlendMode(javafx.scene.effect.BlendMode.MULTIPLY);
                } catch (Exception e) {
                        System.out.println("Logo no encontrado: " + e.getMessage());
                }

                kpiRow = createKpiRow();

                Label tip = new Label("Tip: usa el lanzador rápido para abrir módulos o ejecutar acciones guiadas.");
                tip.getStyleClass().add("muted-label");

                quickActionsPane = createQuickActions();
                VBox logCard = createLogCard();

                if (logoView != null) {
                        getChildren().add(logoView);
                }
                getChildren().addAll(titleLabel, kpiRow, tip, quickActionsPane, logCard);

                widthProperty().addListener((obs, oldV, newV) -> {
                        double wrap = Math.max(400, newV.doubleValue() - 80);
                        kpiRow.setPrefWrapLength(wrap);
                        quickActionsPane.setPrefWrapLength(wrap);
                });

                refreshSystemInfo();
        }

        private FlowPane createQuickActions() {
                FlowPane pane = new FlowPane();
                pane.setHgap(15);
                pane.setVgap(15);
                pane.setAlignment(Pos.CENTER);
                pane.setPrefWrapLength(600);

                Button organizeBtn = createActionButton("📁 Organizar Archivos",
                                "Organiza archivos por categorías", 1,
                                "¿Quieres abrir el Organizador y mover archivos por categoría?");
                Button cleanBtn = createActionButton("🧹 Limpiar Sistema",
                                "Limpia archivos temporales", 2,
                                "¿Limpiamos caché y temporales ahora? Puedes elegir Modo Seguro dentro del módulo.");
                Button browserBtn = createActionButton("🌐 Limpiar Navegadores",
                                "Limpia caché de navegadores", 3,
                                "Abrir limpiador de navegadores para vaciar cachés.");
                Button duplicateBtn = createActionButton("🔍 Buscar Duplicados",
                                "Encuentra archivos duplicados", 4,
                                "Analizar duplicados y liberar espacio.");

                pane.getChildren().addAll(organizeBtn, cleanBtn, browserBtn, duplicateBtn);

                return pane;
        }

        private Button createActionButton(String title, String description, int tabIndex, String confirmationText) {
                VBox content = new VBox(5);
                content.setAlignment(Pos.CENTER);

                Label titleLabel = new Label(title);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                Label descLabel = new Label(description);
                descLabel.setFont(Font.font("System", 11));
                descLabel.getStyleClass().add("muted-label");

                content.getChildren().addAll(titleLabel, descLabel);

                Button button = new Button();
                button.setGraphic(content);
                button.setPrefSize(250, 80);
                button.setMinWidth(220);
                button.setMaxWidth(320);
                button.getStyleClass().add("primary-button");

                button.setOnAction(e -> showLaunchDialog(title, confirmationText, tabIndex));
                return button;
        }

        private FlowPane createKpiRow() {
                FlowPane row = new FlowPane();
                row.setHgap(12);
                row.setVgap(12);
                row.setAlignment(Pos.CENTER);
                row.setPrefWrapLength(600);

                kpiOsValue = new Label("...");
                VBox kpi1 = buildKpiCard("Sistema", kpiOsValue);

                kpiJavaValue = new Label("...");
                VBox kpi2 = buildKpiCard("Java", kpiJavaValue);

                kpiStorageValue = new Label("...");
                VBox kpi3 = buildKpiCard("Almacenamiento", kpiStorageValue);

                row.getChildren().addAll(kpi1, kpi2, kpi3);
                return row;
        }

        private VBox buildKpiCard(String title, Label valueLabel) {
                Label t = new Label(title);
                t.getStyleClass().add("muted-label");
                valueLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

                VBox box = new VBox(4, t, valueLabel);
                box.getStyleClass().add("card-pane");
                box.setPadding(new Insets(12));
                box.setPrefWidth(200);
                return box;
        }

        private VBox createLogCard() {
                activityLog = new ListView<>();
                activityLog.setPrefHeight(170);
                activityLog.setPlaceholder(new Label("Aún no hay actividad en esta sesión."));

                VBox logCard = new VBox(8, new Label("🗒️ Registro rápido"), activityLog);
                logCard.getStyleClass().add("card-pane");
                logCard.setPadding(new Insets(12));
                logCard.setMaxWidth(700);
                return logCard;
        }

        private void showLaunchDialog(String title, String message, int tabIndex) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(title);
                alert.setHeaderText(title);
                alert.setContentText(message);

                ButtonType openTab = new ButtonType("Abrir módulo", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(openTab, cancel);

                alert.showAndWait().ifPresent(resp -> {
                        if (resp == openTab) {
                                if (tabPane != null && tabIndex < tabPane.getTabs().size()) {
                                        tabPane.getSelectionModel().select(tabIndex);
                                        logActivity("Acción lanzada: " + title);
                                }
                        }
                });
        }

        private void logActivity(String text) {
                if (activityLog == null)
                        return;
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                activityLog.getItems().add(0, "[" + timestamp + "] " + text);
                if (activityLog.getItems().size() > 50) {
                        activityLog.getItems().remove(50, activityLog.getItems().size());
                }
        }

        private void refreshSystemInfo() {
                String os = System.getProperty("os.name");
                String javaVersion = System.getProperty("java.version");

                File[] roots = File.listRoots();
                long totalSpace = 0;
                long freeSpace = 0;
                for (File root : roots) {
                        totalSpace += root.getTotalSpace();
                        freeSpace += root.getFreeSpace();
                }

                kpiOsValue.setText(os);
                kpiJavaValue.setText(javaVersion);
                kpiStorageValue.setText(String.format("%.2f GB libres / %.2f GB",
                                freeSpace / (1024.0 * 1024 * 1024),
                                totalSpace / (1024.0 * 1024 * 1024)));
        }
}
