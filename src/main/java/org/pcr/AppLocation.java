package org.pcr;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppLocation {
    /**
     * Devuelve la ruta desde donde corre la app.
     * - Si es JAR: devuelve la ruta del .jar (archivo)
     * - Si es IDE/target: devuelve .../target/classes (carpeta)
     */
    public static Path getRunningPath() {
        try {
            return Paths.get(AppLocation.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).normalize();
        } catch (URISyntaxException e) {
            return Paths.get(System.getProperty("user.dir")).normalize();
        }
    }
}
