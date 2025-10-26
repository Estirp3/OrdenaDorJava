package org.pcr;

import java.util.Map;

public class CategoryResolver {

    /**
     * Devuelve la categoría de un archivo según su nombre y el mapa de categorías.
     * Soporta extensiones simples (.jpg, .pdf) y compuestas (.tar.gz, .7z.001).
     */
    public static String getCategoria(String fileName, Map<String, String[]> categorias) {
        String nombre = fileName.toLowerCase();

        for (var e : categorias.entrySet()) {
            for (String patron : e.getValue()) {
                String p = patron.toLowerCase();

                // Patrón comodín
                if ("*".equals(p)) return e.getKey();

                // Extensiones compuestas (ej: tar.gz)
                if (p.contains(".")) {
                    if (nombre.endsWith("." + p) || nombre.endsWith(p)) {
                        return e.getKey();
                    }
                } else {
                    // Extensión simple
                    if (getExtension(nombre).equals(p)) {
                        return e.getKey();
                    }
                }
            }
        }

        // Si no calza con nada → "otros"
        return "otros";
    }

    /**
     * Obtiene la extensión "simple" de un archivo (después del último punto).
     */
    public static String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return (i > 0 && i < fileName.length() - 1)
                ? fileName.substring(i + 1).toLowerCase()
                : "";
    }
}
