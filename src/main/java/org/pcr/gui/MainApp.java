package org.pcr.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.pcr.gui.tabs.*;
import javafx.scene.image.Image;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("OrdenaDor - Organizador y Limpiador de PC");
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            System.err.println("Icon load fail: " + e.getMessage());
        }

        MenuBar menuBar = createMenuBar(primaryStage);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab dashboardTab = new Tab("📊 Dashboard", new DashboardTab(tabPane));
        Tab organizerTab = new Tab("📁 Organizador", new FileOrganizerTab());
        Tab cleanerTab = new Tab("🧹 Limpiador Sistema", new SystemCleanerTab());
        Tab optimizationTab = new Tab("⚡ Optimización Sistema", new OptimizationTab());
        Tab browserTab = new Tab("🌐 Limpiador Navegadores", new BrowserCleanerTab());
        Tab duplicateTab = new Tab("🔍 Archivos Duplicados", new DuplicateFinderTab());
        Tab diskTab = new Tab("💾 Análisis de Disco", new DiskAnalyzerTab());

        tabPane.getTabs().addAll(dashboardTab, organizerTab, cleanerTab, optimizationTab);
        if (isWindows()) {
            Tab startupTab = new Tab("🚀 Optimización Inicio", new StartupManagerTab());
            tabPane.getTabs().add(startupTab);
        }
        tabPane.getTabs().addAll(browserTab, duplicateTab, diskTab);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 800);

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

        Menu fileMenu = new Menu("Archivo");
        MenuItem exitItem = new MenuItem("Salir");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().add(exitItem);

        Menu toolsMenu = new Menu("Herramientas");
        MenuItem settingsItem = new MenuItem("Configuración");
        settingsItem.setOnAction(e -> showSettings());
        toolsMenu.getItems().add(settingsItem);

        Menu helpMenu = new Menu("Ayuda");
        MenuItem aboutItem = new MenuItem("Acerca de");
        aboutItem.setOnAction(e -> showAbout());
        MenuItem depsItem = new MenuItem("Dependencias / JavaFX");
        depsItem.setOnAction(e -> showDependencies());
        MenuItem envItem = new MenuItem("Variables de entorno");
        envItem.setOnAction(e -> showEnv());
        helpMenu.getItems().add(aboutItem);
        helpMenu.getItems().add(depsItem);
        helpMenu.getItems().add(envItem);

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

    private void showDependencies() {
        String os = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        String javaHome = System.getenv("JAVA_HOME");
        StringBuilder sb = new StringBuilder();
        sb.append("Sistema: ").append(os).append("\n");
        sb.append("Java detectado: ").append(javaVersion).append("\n");
        sb.append("JAVA_HOME: ").append(javaHome == null ? "(no definido)" : javaHome).append("\n\n");
        sb.append("Instrucciones:\n");
        if (isWindows()) {
            sb.append(
                    "- Windows: el JAR sombreado ya incluye dependencias. Si algo falla, instala Java 17+ y JavaFX:\n");
            sb.append("  winget install EclipseAdoptium.Temurin.17.JDK\n");
            sb.append("  winget install OpenJFX\n");
        } else if (isMac()) {
            sb.append("- macOS: instala Java 17 y JavaFX nativo:\n");
            sb.append("  brew install openjdk@17\n");
            sb.append("  brew install openjfx\n");
            sb.append("  Ejecuta con:\n");
            sb.append(
                    "  java --module-path /opt/homebrew/opt/openjfx/lib --add-modules javafx.controls,javafx.fxml -jar target/OrdenDeArchivos-1.0-jar-with-dependencies.jar\n");
        } else if (isLinux()) {
            sb.append("- Linux: instala Java 17 y JavaFX del repo o SDKMAN. Ejemplo Ubuntu/Debian:\n");
            sb.append("  sudo apt-get install openjdk-17-jdk openjfx\n");
            sb.append("  Ejecuta con:\n");
            sb.append(
                    "  java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -jar target/OrdenDeArchivos-1.0-jar-with-dependencies.jar\n");
        } else {
            sb.append("- Plataforma no reconocida: asegúrate de tener Java 17+ y JavaFX nativo.\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dependencias y JavaFX");
        alert.setHeaderText("Requisitos según sistema operativo");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    private boolean isLinux() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("nux") || osName.contains("nix");
    }

    private void showEnv() {
        String javaHome = System.getenv("JAVA_HOME");
        String path = System.getenv("PATH");
        String javaRuntime = System.getProperty("java.home");
        String javaVersion = System.getProperty("java.version");
        boolean hasJavaHome = javaHome != null && !javaHome.isBlank();
        String nvmHome = System.getenv("NVM_HOME");
        String nvmDir = System.getenv("NVM_DIR");

        StringBuilder sb = new StringBuilder();
        sb.append("Java runtime: ").append(javaRuntime).append("\n");
        sb.append("Java version: ").append(javaVersion).append("\n");
        sb.append("JAVA_HOME: ").append(hasJavaHome ? javaHome : "(no definido)").append("\n\n");
        sb.append("PATH (primeros 8 elementos):\n");
        if (path != null) {
            String[] parts = path.split(isWindows() ? ";" : ":");
            for (int i = 0; i < parts.length && i < 8; i++) {
                sb.append("  ").append(i + 1).append(". ").append(parts[i]).append("\n");
            }
            if (parts.length > 8) {
                sb.append("  ... (").append(parts.length - 8).append(" más)\n");
            }
        } else {
            sb.append("(no disponible)\n");
        }
        sb.append("\nTip:\n");
        if (isWindows()) {
            sb.append("- Instalar/actualizar JDK 17+ (winget):\n");
            sb.append("  winget install EclipseAdoptium.Temurin.17.JDK\n");
            sb.append("- Definir JAVA_HOME:\n");
            sb.append("  setx JAVA_HOME \"C:\\\\Program Files\\\\Java\\\\jdk-17\"\n");
            sb.append("- Añadir bin a PATH si falta:\n");
            sb.append("  setx PATH \"%PATH%;%JAVA_HOME%\\\\bin\"\n");
            sb.append("- Cierra y abre la terminal para aplicar cambios.\n");
            sb.append("- Node/NVM (opcional):\n");
            sb.append("  winget install CoreyButler.NVMforWindows\n");
            sb.append("  nvm install 20 && nvm use 20\n");
            sb.append("  (NPX viene con npm, que viene con Node)\n");
        } else {
            sb.append("- Instalar/actualizar JDK 17+ y JavaFX:\n");
            if (isMac()) {
                sb.append("  brew install openjdk@17 openjfx\n");
                sb.append("  export JAVA_HOME=$(/usr/libexec/java_home -v 17)\n");
            } else if (isLinux()) {
                sb.append("  sudo apt-get install openjdk-17-jdk openjfx\n");
                sb.append("  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64\n");
            } else {
                sb.append("  Instala JDK 17 y ajusta JAVA_HOME según tu distro.\n");
            }
            sb.append("- Añade a PATH en tu shell (~/.zshrc o ~/.bashrc):\n");
            sb.append("  export PATH=\"$JAVA_HOME/bin:$PATH\"\n");
            sb.append("- Node/NVM (opcional):\n");
            if (isMac()) {
                sb.append("  brew install nvm\n");
                sb.append("  export NVM_DIR=\"$HOME/.nvm\" && source $(brew --prefix nvm)/nvm.sh\n");
            } else if (isLinux()) {
                sb.append("  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash\n");
                sb.append("  source \"$HOME/.nvm/nvm.sh\"\n");
            } else {
                sb.append("  Instala nvm desde https://github.com/nvm-sh/nvm\n");
            }
            sb.append("  nvm install 20 && nvm use 20\n");
            sb.append("  (NPX viene con npm, que viene con Node)\n");
        }

        sb.append("\nDetectado NVM_HOME/NVM_DIR: ")
                .append(nvmHome != null ? nvmHome : (nvmDir != null ? nvmDir : "(no definido)"));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Variables de entorno clave");
        alert.setHeaderText("JAVA_HOME y PATH");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    public static void launch() {
        Application.launch(MainApp.class);
    }
}
