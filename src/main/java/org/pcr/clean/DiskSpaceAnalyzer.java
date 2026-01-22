// src/main/java/org/pcr/clean/DiskSpaceAnalyzer.java
package org.pcr.clean;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;

public class DiskSpaceAnalyzer {

    public static class DiskAnalysisResult {
        public final Map<String, Long> sizeByCategory = new HashMap<>();
        public final List<FileInfo> largeFiles = new ArrayList<>();
        public final Map<Path, Long> folderSizes = new HashMap<>();
        public long totalSize = 0L;
    }

    public static class FileInfo implements Comparable<FileInfo> {
        public final Path path;
        public final long size;
        public final String category;

        public FileInfo(Path path, long size, String category) {
            this.path = path;
            this.size = size;
            this.category = category;
        }

        @Override
        public int compareTo(FileInfo other) {
            return Long.compare(other.size, this.size); // Descending
        }
    }

    private Consumer<String> progressCallback;
    private final long largeFileThreshold;

    public DiskSpaceAnalyzer(long largeFileThreshold) {
        this.largeFileThreshold = largeFileThreshold;
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    public DiskAnalysisResult analyze(Path directory) throws IOException {
        DiskAnalysisResult result = new DiskAnalysisResult();

        updateProgress("Analizando espacio en disco...");

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    if (attrs.isRegularFile()) {
                        long size = attrs.size();
                        result.totalSize += size;

                        // Categorize by extension
                        String category = categorizeFile(file);
                        result.sizeByCategory.merge(category, size, Long::sum);

                        // Track large files
                        if (size >= largeFileThreshold) {
                            result.largeFiles.add(new FileInfo(file, size, category));
                        }

                        updateProgress("Procesando: " + file.getFileName());
                    }
                } catch (Exception ignored) {
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                try {
                    long dirSize = calculateDirectorySize(dir);
                    result.folderSizes.put(dir, dirSize);
                } catch (Exception ignored) {
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        // Sort large files
        Collections.sort(result.largeFiles);

        updateProgress("Análisis completado!");
        return result;
    }

    private String categorizeFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1)
            return "Otros";

        String ext = fileName.substring(dotIndex + 1);

        // Images
        if (ext.matches("jpg|jpeg|png|gif|bmp|svg|webp|ico"))
            return "Imágenes";

        // Videos
        if (ext.matches("mp4|mkv|avi|mov|wmv|flv|webm"))
            return "Videos";

        // Audio
        if (ext.matches("mp3|wav|flac|aac|ogg|m4a|wma"))
            return "Música";

        // Documents
        if (ext.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|odt|ods"))
            return "Documentos";

        // Archives
        if (ext.matches("zip|rar|7z|tar|gz|bz2"))
            return "Archivos Comprimidos";

        // Executables
        if (ext.matches("exe|msi|bat|sh|cmd|app|deb|rpm"))
            return "Ejecutables";

        // Code
        if (ext.matches("java|py|js|html|css|cpp|c|h|cs|php|rb|go"))
            return "Código";

        return "Otros";
    }

    private long calculateDirectorySize(Path dir) throws IOException {
        final long[] size = { 0L };

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                try {
                    if (Files.isRegularFile(child)) {
                        size[0] += Files.size(child);
                    }
                } catch (IOException ignored) {
                }
            }
        }

        return size[0];
    }

    private void updateProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
    }

    public static String humanReadableSize(long bytes) {
        if (bytes <= 0)
            return "0 B";
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, unitIndex), units[unitIndex]);
    }
}
