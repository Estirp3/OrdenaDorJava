package org.pcr.core;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import static org.pcr.core.CandidateFilter.*;
import static org.pcr.core.ExclusionMatcher.isExcluded;

public class CandidateScanner {
    public static int countFiles(Path raiz, Path destino, Path runningPath, String runningJarName) throws IOException {
        final AtomicInteger count = new AtomicInteger();
        Files.walkFileTree(raiz, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes a) {
                if (dir.equals(destino) || isUnder(dir, destino)) return FileVisitResult.SKIP_SUBTREE;
                if (runningPath != null && isUnder(dir, runningPath)) return FileVisitResult.SKIP_SUBTREE;
                if (isHidden(dir) || isExcluded(dir)) return FileVisitResult.SKIP_SUBTREE;
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                if (isFileCandidate(file, destino, runningPath, runningJarName)) count.incrementAndGet();
                return FileVisitResult.CONTINUE;
            }

            // Si no hay permisos o está bloqueado, seguimos sin cortar el árbol
            @Override public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        return count.get();
    }
}