package org.pcr.report;

import java.nio.file.Path;

public class MoveEvent {
    public String type;      // "file" | "dir" | "skip" | "error"
    public String category;  // ej: "imagenes"
    public Path src;         // origen
    public Path dst;         // destino
    public String reason;    // motivo en skip/error
    public long sizeBytes;   // -1 si no aplica

    public MoveEvent(String type, String category, Path src, Path dst, String reason, long sizeBytes) {
        this.type = type; this.category = category; this.src = src; this.dst = dst; this.reason = reason; this.sizeBytes = sizeBytes;
    }
}
