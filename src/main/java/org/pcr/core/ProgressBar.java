package org.pcr.core;

public class ProgressBar {
    private final int total, width;
    private int done = 0;
    public ProgressBar(int total) { this(total, 40); }
    public ProgressBar(int total, int width) { this.total = Math.max(total,0); this.width = width; }
    public synchronized void step() { done++; print(); }
    public synchronized void print() {
        if (total == 0) return;
        double ratio = Math.min(1.0, (double) done / total);
        int filled = (int)Math.floor(ratio * width);
        String bar = "[" + "=".repeat(Math.max(0, filled)) +
                (filled < width ? ">" : "") +
                " ".repeat(Math.max(0, width - filled - (filled < width ? 1 : 0))) + "]";
        int percent = (int)(ratio * 100);
        System.out.print("\r" + bar + " " + percent + "% (" + Math.min(done,total) + "/" + total + ")");
        if (done >= total) System.out.println();
    }
}
