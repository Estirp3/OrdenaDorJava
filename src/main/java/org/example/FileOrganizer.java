package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class FileOrganizer {

    public static void organizar(Path carpetaOrigen,
                                 Path carpetaDestino,
                                 Map<String, String[]> categorias,
                                 Path runningPath) throws IOException {

        Files.createDirectories(carpetaDestino);

        //Archivo de log
        Path logFile = carpetaDestino.resolve("log.txt");
        try (BufferedWriter log = Files.newBufferedWriter(logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            // Nombre del ejecutable si corre desde JAR
            String runningJarName = null;
            if (runningPath != null && Files.isRegularFile(runningPath)) {
                runningJarName = runningPath.getFileName().toString().toLowerCase();
            }

            File[] archivos = carpetaOrigen.toFile().listFiles();
            if (archivos == null) return;

            for (File archivo : archivos) {
                Path p = archivo.toPath();

                //No tocar directorios ni archivos ocultos
                if (Files.isDirectory(p) || archivo.isHidden()) {
                    log.write("Ignorado (directorio/oculto): " + archivo.getName());
                    log.newLine();
                    continue;
                }

                //No tocar la carpeta destino ni su contenido
                if (p.equals(carpetaDestino) || p.startsWith(carpetaDestino)) {
                    log.write("Ignorado (carpeta destino): " + archivo.getName());
                    log.newLine();
                    continue;
                }

                //No mover el propio JAR o binario/clases
                boolean esPropio = false;
                if (runningPath != null) {
                    try {
                        if (Files.exists(runningPath) && Files.isSameFile(p, runningPath)) {
                            esPropio = true;
                        }
                    } catch (IOException ignored) { /* best effort */ }

                    if (!esPropio && runningJarName != null &&
                            archivo.getName().equalsIgnoreCase(runningJarName)) {
                        esPropio = true;
                    }
                }
                if (esPropio) {
                    log.write("Ignorado (propio JAR): " + archivo.getName());
                    log.newLine();
                    continue;
                }

                //Clasificar y mover
                String extension = CategoryResolver.getExtension(archivo.getName());
                String categoria = CategoryResolver.getCategoria(extension, categorias);

                Path destinoCategoria = carpetaDestino.resolve(categoria);
                Files.createDirectories(destinoCategoria);

                try {
                    Files.move(p,
                            destinoCategoria.resolve(archivo.getName()),
                            StandardCopyOption.REPLACE_EXISTING);
                    log.write("Movido [" + categoria + "]: " + archivo.getName());
                    log.newLine();
                } catch (FileSystemException e) {
                    log.write("No movido (en uso/bloqueado): " + archivo.getName());
                    log.newLine();
                    System.out.println("⚠ No se pudo mover (en uso/bloqueado): " + archivo.getName());
                }
            }
        }
    }
}
