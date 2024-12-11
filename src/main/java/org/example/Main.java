package org.example;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Obtener direccion Actual
        String currentDir = System.getProperty("user.dir");
        String relativDir = "." + currentDir.substring(currentDir.lastIndexOf(File.separator));
       // System.out.println("Directorio actual : " + relativDir);
        String carpetaDescarga;
        if(currentDir.contains("Downloads") || currentDir.contains("Descarga")){
            carpetaDescarga = currentDir;
        }else{
            carpetaDescarga = "./Downloads";
            System.out.println("No está en 'Downloads' o 'Descarga'. estas en : " + relativDir);

        }
        // Crear el nombre de la carpeta destino
        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String carpetaDestino = carpetaDescarga + "/orden-" + fecha;
        // Categorías de archivos
        Map<String, List<String>> categorias = new HashMap<>();
        categorias.put("ejecutables", Arrays.asList("deb", "exe", "sh", "bin"));
        categorias.put("imagenes", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg"));
        categorias.put("documentos", Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"));
        categorias.put("musica", Arrays.asList("mp3", "wav", "flac", "m4a"));
        categorias.put("videos", Arrays.asList("mp4", "mkv", "avi", "mov"));
        categorias.put("archivos", Arrays.asList("zip", "rar", "tar.gz", "7z"));
        categorias.put("otros", Collections.singletonList("*"));

        // Crear la carpeta de destino
        new File(carpetaDestino).mkdirs();

        try {
            organizarArchivos(carpetaDescarga, carpetaDestino, categorias);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Archivos organizados en la carpeta: " + carpetaDestino);
    }

    private static void organizarArchivos(String carpetaDescarga, String carpetaDestino, Map<String, List<String>> categorias) throws IOException {
        File[] archivos = new File(carpetaDescarga).listFiles();
        if (archivos == null) return;

        for (File archivo : archivos) {
            if (archivo.isFile()) {
                String extension = obtenerExtension(archivo.getName());
                String carpeta = determinarCategoria(extension, categorias);

                Path destino = Paths.get(carpetaDestino, carpeta);
                Files.createDirectories(destino);
                Files.move(archivo.toPath(), destino.resolve(archivo.getName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static String obtenerExtension(String nombreArchivo) {
        int index = nombreArchivo.lastIndexOf(".");
        return index != -1 ? nombreArchivo.substring(index + 1) : "";
    }

    private static String determinarCategoria(String extension, Map<String, List<String>> categorias) {
        for (Map.Entry<String, List<String>> categoria : categorias.entrySet()) {
            if (categoria.getValue().contains(extension)) {
                return categoria.getKey();
            }
        }
        return "otros";
    }


}