package org.pcr.core;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class SafeMover {
    public static void moveAtomicOrNormal(Path src, Path dst) throws IOException {
        try { Files.move(src, dst, ATOMIC_MOVE); }
        catch (Exception ex) { Files.move(src, dst); } // sin REPLACE_EXISTING
    }

    public static Path resolveFileConflict(Path dir, String originalName) throws IOException {
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

    public static Path resolveDirConflict(Path dir, String folderName) throws IOException {
        Path c = dir.resolve(folderName);
        if (!Files.exists(c)) return c;
        int n = 2;
        while (true) {
            Path attempt = dir.resolve(folderName + " (" + n + ")");
            if (!Files.exists(attempt)) return attempt;
            n++;
        }
    }
}
