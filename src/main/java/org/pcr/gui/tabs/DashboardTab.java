// src/main/java/org/pcr/gui/tabs/DashboardTab.java
package org.pcr.gui.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;

public class DashboardTab extends VBox {

        private TabPane tabPane;

        public DashboardTab(TabPane tabPane) {
                this.tabPane = tabPane;
                setSpacing(20);
                setPadding(new Insets(20));
                setAlignment(Pos.TOP_CENTER);

                // Title
                Label titleLabel = new Label("🎯 Panel de Control");
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
                titleLabel.setStyle("-fx-text-fill: #007acc;");

                // System info card
                VBox systemInfoCard = createSystemInfoCard();

                // Quick actions
                GridPane quickActions = createQuickActions();

                getChildren().addAll(titleLabel, systemInfoCard, quickActions);
        }

        private VBox createSystemInfoCard() {
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 10;");
                card.setMaxWidth(600);

                Label cardTitle = new Label("ℹ️ Información del Sistema");
                cardTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

                // System info
                String os = System.getProperty("os.name");
                String javaVersion = System.getProperty("java.version");

                File[] roots = File.listRoots();
                long totalSpace = 0;
                long freeSpace = 0;
                for (File root : roots) {
                        totalSpace += root.getTotalSpace();
                        freeSpace += root.getFreeSpace();
                }

                Label osLabel = new Label("Sistema Operativo: " + os);
                Label javaLabel = new Label("Versión Java: " + javaVersion);
                Label spaceLabel = new Label(String.format("Espacio Libre: %.2f GB / %.2f GB",
                                freeSpace / (1024.0 * 1024 * 1024),
                                totalSpace / (1024.0 * 1024 * 1024)));

                card.getChildren().addAll(cardTitle, new Separator(), osLabel, javaLabel, spaceLabel);
                return card;
        }

        private GridPane createQuickActions() {
                GridPane grid = new GridPane();
                grid.setHgap(15);
                grid.setVgap(15);
                grid.setAlignment(Pos.CENTER);

                Button organizeBtn = createActionButton("📁 Organizar Archivos",
                                "Organiza archivos por categorías", 1);
                Button cleanBtn = createActionButton("🧹 Limpiar Sistema",
                                "Limpia archivos temporales", 2);
                Button browserBtn = createActionButton("🌐 Limpiar Navegadores",
                                "Limpia caché de navegadores", 3);
                Button duplicateBtn = createActionButton("🔍 Buscar Duplicados",
                                "Encuentra archivos duplicados", 4);

                grid.add(organizeBtn, 0, 0);
                grid.add(cleanBtn, 1, 0);
                grid.add(browserBtn, 0, 1);
                grid.add(duplicateBtn, 1, 1);

                return grid;
        }

        private Button createActionButton(String title, String description, int tabIndex) {
                VBox content = new VBox(5);
                content.setAlignment(Pos.CENTER);

                Label titleLabel = new Label(title);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                Label descLabel = new Label(description);
                descLabel.setFont(Font.font("System", 11));
                descLabel.setStyle("-fx-text-fill: #888;");

                content.getChildren().addAll(titleLabel, descLabel);

                Button button = new Button();
                button.setGraphic(content);
                button.setPrefSize(250, 80);
                button.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-background-radius: 10;");

                // Acción: cambiar a la pestaña correspondiente
                button.setOnAction(e -> {
                        if (tabPane != null && tabIndex < tabPane.getTabs().size()) {
                                tabPane.getSelectionModel().select(tabIndex);
                        }
                });

                button.setOnMouseEntered(e -> button
                                .setStyle("-fx-background-color: #005a9e; -fx-text-fill: white; -fx-background-radius: 10;"));
                button.setOnMouseExited(e -> button
                                .setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-background-radius: 10;"));

                return button;
        }
}
