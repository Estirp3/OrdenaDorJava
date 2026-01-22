// src/main/java/org/pcr/gui/MainApp.java
package org.pcr.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.pcr.gui.tabs.*;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("OrdenaDor - Organizador y Limpiador de PC");

        // Create menu bar
        MenuBar menuBar = createMenuBar(primaryStage);

        // Create tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Add tabs (Dashboard needs tabPane reference for navigation)
        Tab dashboardTab = new Tab("📊 Dashboard", new DashboardTab(tabPane));
        Tab organizerTab = new Tab("📁 Organizador", new FileOrganizerTab());
        Tab cleanerTab = new Tab("🧹 Limpiador Sistema", new SystemCleanerTab());
        Tab startupTab = new Tab("🚀 Optimización Inicio", new StartupManagerTab());
        Tab browserTab = new Tab("🌐 Limpiador Navegadores", new BrowserCleanerTab());
        Tab duplicateTab = new Tab("🔍 Archivos Duplicados", new DuplicateFinderTab());
        Tab diskTab = new Tab("💾 Análisis de Disco", new DiskAnalyzerTab());

        tabPane.getTabs().addAll(dashboardTab, organizerTab, cleanerTab, startupTab, browserTab, duplicateTab, diskTab);

        // Main layout
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 800);

        // Load CSS
        try {
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("No se pudo cargar styles.css: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("Archivo");
        MenuItem exitItem = new MenuItem("Salir");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().add(exitItem);

        // Tools menu
        Menu toolsMenu = new Menu("Herramientas");
        MenuItem settingsItem = new MenuItem("Configuración");
        settingsItem.setOnAction(e -> showSettings());
        toolsMenu.getItems().add(settingsItem);

        // Help menu
        Menu helpMenu = new Menu("Ayuda");
        MenuItem aboutItem = new MenuItem("Acerca de");
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, toolsMenu, helpMenu);
        return menuBar;
    }

    private void showSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración");
        alert.setHeaderText("Configuración");
        alert.setContentText("Próximamente: Configuración de categorías y exclusiones.");
        alert.showAndWait();
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("OrdenaDor v1.0");
        alert.setContentText("Organizador y Limpiador de PC\n\n" +
                "Funcionalidades:\n" +
                "• Organización de archivos por categorías\n" +
                "• Limpieza de archivos temporales del sistema\n" +
                "• Limpieza de caché de navegadores\n" +
                "• Detección de archivos duplicados\n" +
                "• Análisis de espacio en disco\n\n" +
                "Desarrollado con JavaFX 21");
        alert.showAndWait();
    }

    public static void launch() {
        Application.launch(MainApp.class);
    }
}
