package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            // Preguntar carpeta a organizar
            Path carpetaOrigen = DownloadDirResolver.getDirectoryFromUser();

            // Crear carpeta destino con fecha
            String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Path carpetaDestino = carpetaOrigen.resolve("orden-" + fecha);

            //Obtener categorías
            Map<String, String[]> categorias = Config.getCategorias();

            // Organizar (con ruta del propio JAR/clases para evitar auto-movida)
            FileOrganizer.organizar(
                    carpetaOrigen,
                    carpetaDestino,
                    categorias,
                    AppLocation.getRunningPath()
            );

            System.out.println("✅ Archivos organizados en: " + carpetaDestino);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
