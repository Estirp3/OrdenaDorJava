package org.pcr;

import java.util.Map;

public class CategoryResolver {


    public static String getCategoria(String fileName, Map<String, String[]> categorias) {
        String nombre = fileName.toLowerCase();

        for (var e : categorias.entrySet()) {
            for (String patron : e.getValue()) {
                String p = patron.toLowerCase();

                if ("*".equals(p)) return e.getKey();

                if (p.contains(".")) {
                    if (nombre.endsWith("." + p) || nombre.endsWith(p)) {
                        return e.getKey();
                    }
                } else {
                    if (getExtension(nombre).equals(p)) {
                        return e.getKey();
                    }
                }
            }
        }

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
