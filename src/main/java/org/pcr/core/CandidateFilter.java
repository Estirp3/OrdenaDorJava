package org.pcr.core;

import java.io.IOException;
import java.nio.file.*;

public class CandidateFilter {
    public static boolean isHidden(Path p) { try { return Files.isHidden(p); } catch (IOException e) { return false; } }
    public static boolean isUnder(Path p, Path root) { return p.normalize().startsWith(root.normalize()); }

    public static boolean isFileCandidate(Path file, Path destino, Path runningPath, String runningJarName) {
        try {
            if (!Files.isRegularFile(file) || isHidden(file)) return false;
            if (isUnder(file, destino)) return false;
            if (runningPath != null) {
                if (Files.isRegularFile(runningPath) && Files.isSameFile(file, runningPath)) return false;
                if (runningJarName != null && file.getFileName().toString().equalsIgnoreCase(runningJarName)) return false;
            }
            return true;
        } catch (IOException e) { return false; }
    }
}