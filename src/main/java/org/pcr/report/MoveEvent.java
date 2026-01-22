package org.pcr.report;

import java.nio.file.Path;

public class MoveEvent {
    public String type;
    public String category;
    public Path src;
    public Path dst;
    public String reason;
    public long sizeBytes;

    public MoveEvent(String type, String category, Path src, Path dst, String reason, long sizeBytes) {
        this.type = type; this.category = category; this.src = src; this.dst = dst; this.reason = reason; this.sizeBytes = sizeBytes;
    }
}
