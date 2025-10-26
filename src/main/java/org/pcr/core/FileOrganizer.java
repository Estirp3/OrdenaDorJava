// src/main/java/org/pcr/core/FileOrganizer.java
package org.pcr.core;

import org.pcr.CategoryResolver;
import org.pcr.report.MoveEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class FileOrganizer {

    // Excluir por nombre en el nivel raíz
    private static final Set<String> EXCLUDE_NAMES = new HashSet<>(Arrays.asList(
            "_orden", "_ordenado", "node_modules", ".pnpm", ".git", ".idea", ".vscode", "target", "build"
    ));

    /** Resultado de la organización (para el reporte) */
    public static class Result {
        public final List<MoveEvent> moves = new ArrayList<>();
        public final Map<String, Integer> perCategory = new HashMap<>();
        public int filesMoved, dirsMoved, skipped, errors;
    }

    /** Alias opcional */
    public static Result run(Path carpetaOrigen,
                             Path carpetaDestino,
                             Map<String, String[]> categorias,
                             Path runningPath) throws IOException {
        return organizar(carpetaOrigen, carpetaDestino, categorias, runningPath);
    }

    /**
     * Organiza SOLO el NIVEL RAÍZ de carpetaOrigen.
     * - Archivos por tipo en destino/<categoria>/
     * - Carpetas como unidad en destino/_carpetas/<Nombre>
     * No abre subcarpetas. No fuerza movimientos.
     */
    public static Result organizar(Path carpetaOrigen,
                                   Path carpetaDestino,
                                   Map<String, String[]> categorias,
                                   Path runningPath) throws IOException {

        Result r = new Result();

        Files.createDirectories(carpetaDestino);
        final String runningJarName =
                (runningPath != null && Files.isRegularFile(runningPath))
                        ? runningPath.getFileName().toString().toLowerCase(Locale.ROOT)
                        : null;

        // Candidatos: SOLO nivel raíz
        List<Path> candidatos = listarNivelRaiz(carpetaOrigen, carpetaDestino, runningPath, runningJarName);

        Path logFile = carpetaDestino.resolve("log.txt");
        try (BufferedWriter log = Files.newBufferedWriter(logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            final int total = candidatos.size();
            printProgress(0, total);

            for (int i = 0; i < candidatos.size(); i++) {
                Path p = candidatos.get(i);
                try {
                    if (Files.isDirectory(p)) {
                        // mover carpeta como unidad
                        Path destDir = carpetaDestino.resolve("_carpetas");
                        Files.createDirectories(destDir);

                        Path dst = resolveDirConflict(destDir, p.getFileName().toString());
                        try {
                            Files.move(p, dst, ATOMIC_MOVE);
                            writeln(log, "Movido [dir]: " + p + " => " + dst);
                            r.moves.add(new MoveEvent("dir", null, p, dst, null, -1));
                            r.dirsMoved++;
                        } catch (Exception ex) {
                            String reason = cleanReason(ex);
                            writeln(log, "No movido dir: " + p + " -> " + reason);
                            r.moves.add(new MoveEvent("skip", null, p, null, reason, -1));
                            r.skipped++;
                        }

                    } else if (Files.isRegularFile(p)) {
                        // archivo suelto → por categoría
                        if (!Files.isReadable(p)) {
                            writeln(log, "No movido (sin permisos lectura): " + p);
                            r.moves.add(new MoveEvent("skip", null, p, null, "sin permisos de lectura", -1));
                            r.skipped++;
                        } else {
                            String categoria = CategoryResolver.getCategoria(p.getFileName().toString(), categorias);
                            if (categoria == null || categoria.isBlank()) categoria = "otros";

                            Path destDir = carpetaDestino.resolve(categoria);
                            Files.createDirectories(destDir);

                            Path dst = resolveFileConflict(destDir, p.getFileName().toString());
                            boolean moved = false;
                            try {
                                Files.move(p, dst, ATOMIC_MOVE);
                                moved = true;
                            } catch (Exception ex) {
                                try {
                                    Files.move(p, dst); // sin REPLACE_EXISTING
                                    moved = true;
                                } catch (Exception ex2) {
                                    String reason = cleanReason(ex2);
                                    writeln(log, "No movido (archivo): " + p + " -> " + reason);
                                    r.moves.add(new MoveEvent("skip", categoria, p, null, reason, -1));
                                    r.skipped++;
                                }
                            }

                            if (moved) {
                                long size = -1;
                                try { size = Files.size(dst); } catch (Exception ignore) {}
                                writeln(log, "Movido [arch:" + categoria + "]: " + p + " => " + dst);
                                r.moves.add(new MoveEvent("file", categoria, p, dst, null, size));
                                r.filesMoved++;
                                r.perCategory.merge(categoria, 1, Integer::sum);
                            }
                        }

                    } else {
                        // ni archivo ni dir regular
                        r.skipped++;
                    }
                } catch (Exception e) {
                    r.errors++;
                    writeln(log, "ERROR: " + p + " -> " + e);
                    r.moves.add(new MoveEvent("error", null, p, null, e.toString(), -1));
                } finally {
                    printProgress(i + 1, total);
                }
            }

            writeln(log, "==== RESUMEN (no recursivo) ====");
            writeln(log, "Archivos movidos: " + r.filesMoved);
            writeln(log, "Carpetas movidas: " + r.dirsMoved);
            writeln(log, "Saltados: " + r.skipped);
            writeln(log, "Errores: " + r.errors);
        }

        return r;
    }

    // ==================== Helpers privados ====================

    /** Lista solo los elementos del nivel raíz a procesar. */
    private static List<Path> listarNivelRaiz(Path origen, Path destino, Path runningPath, String runningJarName) throws IOException {
        List<Path> out = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(origen)) {
            for (Path p : ds) {
                if (isExcluded(p)) continue;                 // por nombre
                if (isUnder(p, destino)) continue;           // no tocar destino
                if (runningPath != null) {
                    try {
                        if (Files.isRegularFile(runningPath) && Files.isSameFile(p, runningPath)) continue; // el propio jar
                    } catch (IOException ignore) {}
                    if (runningJarName != null && p.getFileName() != null &&
                            p.getFileName().toString().equalsIgnoreCase(runningJarName)) continue;
                }
                if (Files.isRegularFile(p) || Files.isDirectory(p)) out.add(p);
            }
        }
        return out;
    }

    private static boolean isUnder(Path p, Path root) {
        return p != null && root != null && p.normalize().startsWith(root.normalize());
    }

    private static boolean isExcluded(Path p) {
        Path name = (p == null) ? null : p.getFileName();
        return name != null && EXCLUDE_NAMES.contains(name.toString());
    }

    /** Conflictos: archivo (no sobrescribe). */
    private static Path resolveFileConflict(Path dir, String originalName) throws IOException {
        Path c = dir.resolve(originalName);
        if (!Files.exists(c)) return c;

        String base, ext;
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) { base = originalName.substring(0, dot); ext = originalName.substring(dot); }
        else { base = originalName; ext = ""; }

        int n = 2;
        while (true) {
            Path attempt = dir.resolve(base + " (" + n + ")" + ext);
            if (!Files.exists(attempt)) return attempt;
            n++;
        }
    }

    /** Conflictos: carpeta (no sobrescribe). */
    private static Path resolveDirConflict(Path dir, String folderName) throws IOException {
        Path c = dir.resolve(folderName);
        if (!Files.exists(c)) return c;

        int n = 2;
        while (true) {
            Path attempt = dir.resolve(folderName + " (" + n + ")");
            if (!Files.exists(attempt)) return attempt;
            n++;
        }
    }

    private static void printProgress(int done, int total) {
        int width = 40;
        double ratio = (total == 0) ? 1.0 : Math.min(1.0, (double) done / total);
        int filled = (int) Math.floor(ratio * width);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < filled; i++) bar.append('=');
        if (filled < width) bar.append('>');
        for (int i = 0; i < width - filled - (filled < width ? 1 : 0); i++) bar.append(' ');
        bar.append(']');

        int percent = (int) Math.floor(ratio * 100.0);
        System.out.print("\r" + bar + " " + percent + "% (" + done + "/" + total + ")");
        if (done >= total) System.out.println();
    }

    private static void writeln(BufferedWriter log, String s) throws IOException {
        log.write(s);
        log.newLine();
    }

    private static String cleanReason(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) return ex.getClass().getSimpleName();
        String m = msg.toLowerCase(Locale.ROOT);
        if (m.contains("being used") || m.contains("siendo utilizado")) return "archivo en uso por otro proceso";
        if (m.contains("access") || m.contains("acceso") || m.contains("denied")) return "acceso denegado";
        return msg;
    }
}
