package org.pcr.core;

import java.nio.file.Path;
import java.util.Set;

public class ExclusionMatcher {
    // Agrega lo que quieras excluir por nombre exacto de carpeta/archivo
    private static final Set<String> EXCLUDE_NAMES = Set.of(
            "node_modules", ".pnpm", ".git", ".idea", ".vscode", "target", "build"
    );

    public static boolean isExcluded(Path p) {
        Path name = p.getFileName();
        return name != null && EXCLUDE_NAMES.contains(name.toString());
    }
}
