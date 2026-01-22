// src/main/java/org/pcr/clean/BrowserCacheCleaner.java
package org.pcr.clean;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class BrowserCacheCleaner {

    public static class BrowserCleanResult {
        public final Map<String, Long> bytesFreedPerBrowser = new HashMap<>();
        public final Map<String, String> errors = new HashMap<>();
        public long totalBytesFreed = 0L;
    }

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    public static BrowserCleanResult cleanAll() throws IOException {
        BrowserCleanResult result = new BrowserCleanResult();

        cleanChrome(result);
        cleanFirefox(result);
        cleanEdge(result);
        cleanBrave(result);

        return result;
    }

    private static void cleanChrome(BrowserCleanResult result) {
        try {
            Path cachePath = getChromeCache();
            if (cachePath != null && Files.exists(cachePath)) {
                long freed = cleanDirectory(cachePath);
                result.bytesFreedPerBrowser.put("Chrome", freed);
                result.totalBytesFreed += freed;
            }
        } catch (Exception e) {
            result.errors.put("Chrome", e.getMessage());
        }
    }

    private static void cleanFirefox(BrowserCleanResult result) {
        try {
            List<Path> profilePaths = getFirefoxProfiles();
            long totalFreed = 0L;
            for (Path profile : profilePaths) {
                Path cache = profile.resolve("cache2");
                if (Files.exists(cache)) {
                    totalFreed += cleanDirectory(cache);
                }
            }
            if (totalFreed > 0) {
                result.bytesFreedPerBrowser.put("Firefox", totalFreed);
                result.totalBytesFreed += totalFreed;
            }
        } catch (Exception e) {
            result.errors.put("Firefox", e.getMessage());
        }
    }

    private static void cleanEdge(BrowserCleanResult result) {
        try {
            Path cachePath = getEdgeCache();
            if (cachePath != null && Files.exists(cachePath)) {
                long freed = cleanDirectory(cachePath);
                result.bytesFreedPerBrowser.put("Edge", freed);
                result.totalBytesFreed += freed;
            }
        } catch (Exception e) {
            result.errors.put("Edge", e.getMessage());
        }
    }

    private static void cleanBrave(BrowserCleanResult result) {
        try {
            Path cachePath = getBraveCache();
            if (cachePath != null && Files.exists(cachePath)) {
                long freed = cleanDirectory(cachePath);
                result.bytesFreedPerBrowser.put("Brave", freed);
                result.totalBytesFreed += freed;
            }
        } catch (Exception e) {
            result.errors.put("Brave", e.getMessage());
        }
    }

    private static Path getChromeCache() {
        if (IS_WINDOWS) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null) {
                return Paths.get(localAppData, "Google", "Chrome", "User Data", "Default", "Cache");
            }
        }
        return null;
    }

    private static Path getEdgeCache() {
        if (IS_WINDOWS) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null) {
                return Paths.get(localAppData, "Microsoft", "Edge", "User Data", "Default", "Cache");
            }
        }
        return null;
    }

    private static Path getBraveCache() {
        if (IS_WINDOWS) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null) {
                return Paths.get(localAppData, "BraveSoftware", "Brave-Browser", "User Data", "Default", "Cache");
            }
        }
        return null;
    }

    private static List<Path> getFirefoxProfiles() throws IOException {
        List<Path> profiles = new ArrayList<>();
        if (IS_WINDOWS) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                Path firefoxPath = Paths.get(appData, "Mozilla", "Firefox", "Profiles");
                if (Files.exists(firefoxPath)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(firefoxPath, "*.default*")) {
                        for (Path profile : stream) {
                            if (Files.isDirectory(profile)) {
                                profiles.add(profile);
                            }
                        }
                    }
                }
            }
        }
        return profiles;
    }

    private static long cleanDirectory(Path dir) throws IOException {
        if (!Files.exists(dir))
            return 0L;

        long sizeBefore = calculateSize(dir);
        deleteChildren(dir);
        long sizeAfter = calculateSize(dir);

        return Math.max(0, sizeBefore - sizeAfter);
    }

    private static long calculateSize(Path dir) throws IOException {
        final long[] size = { 0L };
        if (!Files.exists(dir))
            return 0L;

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                size[0] += attrs.size();
                return FileVisitResult.CONTINUE;
            }
        });
        return size[0];
    }

    private static void deleteChildren(Path dir) throws IOException {
        if (!Files.exists(dir))
            return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                try {
                    if (Files.isDirectory(child)) {
                        deleteTree(child);
                    } else {
                        Files.deleteIfExists(child);
                    }
                } catch (Exception ignored) {
                    // Some files may be locked
                }
            }
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root))
            return;

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Map<String, Long> getCacheSizes() {
        Map<String, Long> sizes = new HashMap<>();

        try {
            Path chrome = getChromeCache();
            if (chrome != null && Files.exists(chrome)) {
                sizes.put("Chrome", calculateSize(chrome));
            }
        } catch (Exception ignored) {
        }

        try {
            List<Path> firefoxProfiles = getFirefoxProfiles();
            long totalSize = 0L;
            for (Path profile : firefoxProfiles) {
                Path cache = profile.resolve("cache2");
                if (Files.exists(cache)) {
                    totalSize += calculateSize(cache);
                }
            }
            if (totalSize > 0) {
                sizes.put("Firefox", totalSize);
            }
        } catch (Exception ignored) {
        }

        try {
            Path edge = getEdgeCache();
            if (edge != null && Files.exists(edge)) {
                sizes.put("Edge", calculateSize(edge));
            }
        } catch (Exception ignored) {
        }

        try {
            Path brave = getBraveCache();
            if (brave != null && Files.exists(brave)) {
                sizes.put("Brave", calculateSize(brave));
            }
        } catch (Exception ignored) {
        }

        return sizes;
    }
}
