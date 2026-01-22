package org.pcr;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class DownloadDirResolver {

    public static Path getDirectoryFromUser() {
        Scanner sc = new Scanner(System.in);
        System.out.print("📂 Indica la ruta de la carpeta que quieres organizar: ");
        String ruta = sc.nextLine().trim();

        if (ruta.isEmpty()) {
            ruta = System.getProperty("user.dir");
            System.out.println("⚠ No ingresaste ruta. Se usará: " + ruta);
        }
        return Paths.get(ruta);
    }
}
