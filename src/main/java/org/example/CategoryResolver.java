package org.example;

import java.util.Map;

public class CategoryResolver {
    public static String getCategoria(String extension, Map<String, String[]> categorias) {
        extension = extension.toLowerCase();
        for (Map.Entry<String, String[]> entry : categorias.entrySet()) {
            for (String ext : entry.getValue()) {
                if (ext.equalsIgnoreCase(extension)) {
                    return entry.getKey();
                }
            }
        }
        return "otros";
    }

    public static String getExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        return index != -1 ? fileName.substring(index + 1) : "";
    }
}
