package org.pcr.core;


import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static org.pcr.core.SafeMover.*;

public class DirectoryMerger {
    public static void moveDirectoryMerge(Path srcDir, Path dstDir) throws IOException {
        if (!Files.exists(dstDir)) {
            try { Files.move(srcDir, dstDir, ATOMIC_MOVE); return; }
            catch (Exception ignore) { /* merge */ }
        } else if (!Files.isDirectory(dstDir)) {
            throw new IOException("Destino existe y no es directorio: " + dstDir);
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcDir)) {
            for (Path child : ds) {
                Path target = dstDir.resolve(child.getFileName());
                if (Files.isDirectory(child)) {
                    Path targetDir = resolveDirConflict(dstDir, child.getFileName().toString());
                    moveDirectoryMerge(child, targetDir);
                } else {
                    Path safeTarget = resolveFileConflict(dstDir, child.getFileName().toString());
                    moveAtomicOrNormal(child, safeTarget);
                }
            }
        }
        if (isEmpty(srcDir)) Files.deleteIfExists(srcDir);
    }

    public static boolean isEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            Iterator<Path> it = ds.iterator(); return !it.hasNext();
        }
    }
}
