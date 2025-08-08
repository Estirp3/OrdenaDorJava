package org.example;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static Map<String, String[]> getCategorias() {
        Map<String, String[]> categorias = new HashMap<>();
        categorias.put("ejecutables", new String[]{"deb", "exe", "sh", "bin"});
        categorias.put("imagenes", new String[]{"jpg", "jpeg", "png", "gif", "bmp", "svg"});
        categorias.put("documentos", new String[]{"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"});
        categorias.put("musica", new String[]{"mp3", "wav", "flac", "m4a"});
        categorias.put("videos", new String[]{"mp4", "mkv", "avi", "mov"});
        categorias.put("archivos", new String[]{"zip", "rar", "tar.gz", "7z"});
        categorias.put("otros", new String[]{"*"});
        return categorias;
    }
}
