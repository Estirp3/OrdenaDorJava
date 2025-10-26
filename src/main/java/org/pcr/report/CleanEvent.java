package org.pcr.report;

public class CleanEvent {
    public String name;        // "TEMP Windows", "DNS flush"
    public boolean success;    // true si exit 0
    public String detail;      // salida/nota
    public long bytesFreed;    // -1 si no aplica

    public CleanEvent(String name, boolean success, String detail, long bytesFreed) {
        this.name = name; this.success = success; this.detail = detail; this.bytesFreed = bytesFreed;
    }
}
