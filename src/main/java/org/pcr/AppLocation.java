package org.pcr;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppLocation {

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
