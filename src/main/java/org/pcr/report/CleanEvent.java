package org.pcr.report;

public class CleanEvent {
    public String name;
    public boolean success;
    public String detail;
    public long bytesFreed;

    public CleanEvent(String name, boolean success, String detail, long bytesFreed) {
        this.name = name; this.success = success; this.detail = detail; this.bytesFreed = bytesFreed;
    }
}
