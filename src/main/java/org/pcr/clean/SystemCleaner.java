// src/main/java/org/pcr/clean/SystemCleaner.java
package org.pcr.clean;

import org.pcr.report.CleanEvent;   // <-- usa la clase top-level
// 👇 elimina este import si lo tenías:
// import org.pcr.report.HtmlReportWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SystemCleaner {

    public enum Mode { SAFE, AGGRESSIVE }

    private final Path baseDir;
    private final Path logFile;
    private final boolean isWindows;
    private final boolean isMac;
    private final boolean isLinux;
    private final boolean isAdmin;

    public SystemCleaner(Path carpetaOrigen, Path carpetaDestino) throws IOException {
        this.baseDir = carpetaOrigen;
        this.logFile = carpetaDestino.resolve("clean.log");
        String os = System.getProperty("os.name","").toLowerCase(Locale.ROOT);
        this.isWindows = os.contains("win");
        this.isMac     = os.contains("mac");
        this.isLinux   = os.contains("nux") || os.contains("nix");
        this.isAdmin   = detectAdmin(this.isWindows);
        Files.createDirectories(carpetaDestino);
    }

    /** Resultado para el reporte HTML */
    public static class CleanResult {
        public final List<CleanEvent> events = new ArrayList<>();          // <-- aquí
        public final List<String> dnsCommandsTried = new ArrayList<>();
        public final Map<String,Integer> dnsExitCodes = new HashMap<>();
        public long totalBytesFreed = 0L;
    }

    public CleanResult run(Mode mode) throws IOException {
        CleanResult r = new CleanResult();

        try (BufferedWriter log = Files.newBufferedWriter(
                logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            log.write("=== SystemCleaner start ("+ mode +") ==="); log.newLine();
            log.write("OS: " + System.getProperty("os.name") + " | Admin: " + isAdmin); log.newLine();

            if (isWindows) cleanWindowsUserCaches(log, r);
            if (isMac)     cleanMacUserCaches(log, r);
            if (isLinux)   cleanLinuxUserCaches(log, r);

            if (mode == Mode.AGGRESSIVE) {
                if (isWindows) aggressiveWindows(log, r);
                if (isMac)     aggressiveMac(log, r);
                if (isLinux)   aggressiveLinux(log, r);
            }

            log.write("Total liberado: " + humanSize(r.totalBytesFreed)); log.newLine();
            log.write("=== SystemCleaner end ==="); log.newLine();
        }

        return r;
    }

    // ---------------- SAFE CLEANING ----------------

    private void cleanWindowsUserCaches(BufferedWriter log, CleanResult r) throws IOException {
        Path temp = Paths.get(System.getProperty("java.io.tmpdir"));
        Path localAppData = getEnvPath("LOCALAPPDATA");

        List<Path> targets = new ArrayList<>();
        if (temp != null) targets.add(temp);
        if (localAppData != null) {
            targets.add(localAppData.resolve("Temp"));
            targets.add(localAppData.resolve("Microsoft\\Windows\\Explorer"));
        }
        sweepDirs("Windows user caches", targets, log, r);
    }

    private void cleanMacUserCaches(BufferedWriter log, CleanResult r) throws IOException {
        Path caches = Paths.get(System.getProperty("user.home"), "Library", "Caches");
        sweepDirs("macOS ~/Library/Caches", Collections.singletonList(caches), log, r);
    }

    private void cleanLinuxUserCaches(BufferedWriter log, CleanResult r) throws IOException {
        Path xdgCache = getEnvPath("XDG_CACHE_HOME");
        if (xdgCache == null) xdgCache = Paths.get(System.getProperty("user.home"), ".cache");
        sweepDirs("Linux $XDG_CACHE_HOME", Collections.singletonList(xdgCache), log, r);
    }

    // ---------------- AGGRESSIVE ----------------

    private void aggressiveWindows(BufferedWriter log, CleanResult r) throws IOException {
        execDns(log, r, new String[]{"cmd","/c","ipconfig","/flushdns"}, "DNS flush");
        execDns(log, r, new String[]{"cmd","/c","netsh","winsock","reset"}, "Winsock reset");
        execDns(log, r, new String[]{"cmd","/c","netsh","int","ip","reset"}, "IP stack reset");
    }

    private void aggressiveMac(BufferedWriter log, CleanResult r) throws IOException {
        execDns(log, r, new String[]{"bash","-lc","sudo dscacheutil -flushcache"}, "DNS flush (dscacheutil)");
        execDns(log, r, new String[]{"bash","-lc","sudo killall -HUP mDNSResponder"}, "mDNSResponder HUP");
    }

    private void aggressiveLinux(BufferedWriter log, CleanResult r) throws IOException {
        execDns(log, r, new String[]{"bash","-lc","resolvectl flush-caches || systemd-resolve --flush-caches"}, "DNS flush (systemd)");
        execLogged(log, new String[]{"bash","-lc","command -v nmcli >/dev/null 2>&1 && nmcli general reload || true"}, "NM reload", null);
    }

    // ---------------- Carpeta helpers ----------------

    private void sweepDirs(String label, List<Path> targets, BufferedWriter log, CleanResult r) throws IOException {
        for (Path p : targets) {
            if (p == null) continue;
            try {
                if (Files.exists(p)) {
                    long before = dirSizeBytes(p);
                    deleteChildren(p, log);
                    long after = dirSizeBytes(p);
                    long freed = Math.max(0, before - after);

                    r.totalBytesFreed += freed;
                    r.events.add(new CleanEvent(label + ": " + p, true, "ok", freed));   // <-- usa CleanEvent

                    log.write("Limpio: " + p + " | " + humanSize(freed) + " liberados"); log.newLine();
                }
            } catch (Exception e) {
                r.events.add(new CleanEvent(label + ": " + p, false, e.getMessage(), -1)); // <--
                log.write("No limpiado: " + p + " -> " + e.getMessage()); log.newLine();
            }
        }
    }

    private long dirSizeBytes(Path dir) throws IOException {
        final long[] size = {0L};
        if (!Files.exists(dir)) return 0L;
        Files.walk(dir).filter(Files::isRegularFile).forEach(f -> {
            try { size[0] += Files.size(f); } catch (IOException ignored) {}
        });
        return size[0];
    }

    private void deleteChildren(Path dir, BufferedWriter log) throws IOException {
        if (!Files.exists(dir)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path child : ds) {
                try {
                    if (Files.isDirectory(child)) {
                        deleteTree(child);
                    } else {
                        Files.deleteIfExists(child);
                    }
                } catch (Exception e) {
                    log.write("No eliminado: " + child + " -> " + e.getMessage()); log.newLine();
                }
            }
        }
    }

    private void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file); return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir); return FileVisitResult.CONTINUE;
            }
        });
    }

    // ---------------- Comandos ----------------

    private void execDns(BufferedWriter log, CleanResult r, String[] cmd, String label) throws IOException {
        int code = execLogged(log, cmd, label, r);
        String joined = String.join(" ", cmd);
        r.dnsCommandsTried.add(joined);
        r.dnsExitCodes.put(joined, code);
        boolean ok = (code == 0);
        r.events.add(new CleanEvent(label, ok, ok ? "ok" : "exit " + code, -1));   // <--
    }

    private int execLogged(BufferedWriter log, String[] cmd, String label, CleanResult r) throws IOException {
        Process p = null;
        try {
            p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String out = readAll(p.getInputStream());
            int code = p.waitFor();

            log.write((label != null ? "["+label+"] " : "") + "$ " + String.join(" ", cmd)); log.newLine();
            log.write("exit=" + code); log.newLine();
            if (!out.isEmpty()) { log.write(out); log.newLine(); }
            if (code != 0) log.write("⚠ Puede requerir privilegios. Relanza como administrador/root."); log.newLine();
            return code;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.write("Interrumpido: " + String.join(" ", cmd)); log.newLine();
            return -1;
        } catch (Exception e) {
            log.write("Error ejecutando: " + String.join(" ", cmd) + " -> " + e.getMessage()); log.newLine();
            return -1;
        } finally {
            if (p != null) p.destroy();
        }
    }

    private String readAll(InputStream is) throws IOException { return new String(is.readAllBytes()); }

    private Path getEnvPath(String name) {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) return null;
        return Paths.get(v);
    }

    private boolean detectAdmin(boolean windows) {
        try {
            if (windows) {
                Process p = new ProcessBuilder("cmd","/c","net","session").start();
                int code = p.waitFor();
                return code == 0;
            } else {
                Process p = new ProcessBuilder("id","-u").start();
                int code = p.waitFor();
                if (code == 0) {
                    String out = new String(p.getInputStream().readAllBytes()).trim();
                    return "0".equals(out);
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String humanSize(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] u = {"B","KB","MB","GB","TB"};
        int i = (int)Math.floor(Math.log(bytes)/Math.log(1024));
        return String.format(Locale.US,"%.1f %s", bytes / Math.pow(1024, i), u[i]);
    }
}
