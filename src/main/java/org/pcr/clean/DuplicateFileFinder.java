package org.pcr.clean;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;

public class DuplicateFileFinder {

    public static class DuplicateResult {
        public final Map<String, List<Path>> duplicateGroups = new HashMap<>();
        public long totalDuplicateSize = 0L;
        public int totalDuplicateFiles = 0;
    }

    private final long minFileSize;
    private Consumer<String> progressCallback;

    public DuplicateFileFinder(long minFileSize) {
        this.minFileSize = minFileSize;
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    public DuplicateResult findDuplicates(Path directory) throws IOException {
        DuplicateResult result = new DuplicateResult();
        Map<String, List<Path>> hashMap = new HashMap<>();

        updateProgress("Escaneando archivos...");

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    if (attrs.isRegularFile() && attrs.size() >= minFileSize) {
                        updateProgress("Procesando: " + file.getFileName());
                        String hash = calculateHash(file);
                        hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
                    }
                } catch (Exception e) {
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        updateProgress("Identificando duplicados...");

        for (Map.Entry<String, List<Path>> entry : hashMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.duplicateGroups.put(entry.getKey(), entry.getValue());

                try {
                    long fileSize = Files.size(entry.getValue().get(0));
                    result.totalDuplicateSize += fileSize * (entry.getValue().size() - 1);
                    result.totalDuplicateFiles += entry.getValue().size() - 1;
                } catch (IOException ignored) {
                }
            }
        }

        updateProgress("Completado!");
        return result;
    }

    private String calculateHash(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return DigestUtils.sha256Hex(is);
        }
    }

    private void updateProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
    }
}
