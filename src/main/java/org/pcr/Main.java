// src/main/java/org/pcr/Main.java
package org.pcr;

import org.pcr.core.FileOrganizer;
import org.pcr.core.FileOrganizer.Result;
import org.pcr.clean.SystemCleaner;
import org.pcr.clean.SystemCleaner.CleanResult;
import org.pcr.report.HtmlReportWriter;

import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {

            Path origen = DownloadDirResolver.getDirectoryFromUser();
            Path destino = origen.resolve("_ordenado");
            Map<String, String[]> categorias = Config.getCategorias();

            int opcion = askMenu(sc);
            boolean quiereLimpiar = (opcion == 2 || opcion == 3);

            // resultados para el reporte
            Result orgRes = new Result();            // vacío por defecto
            CleanResult cleanRes = new CleanResult();// vacío por defecto

            // 1) Organizar (si corresponde)
            if (opcion == 1 || opcion == 3) {
                orgRes = FileOrganizer.organizar(origen, destino, categorias, AppLocation.getRunningPath());
            }

            // 2) Limpiar (si corresponde)
            if (quiereLimpiar) {
                boolean agresivo = askYesNo(sc, "¿Modo agresivo (puede requerir admin/root)? (s/n) ");
                SystemCleaner.Mode mode = agresivo ? SystemCleaner.Mode.AGGRESSIVE : SystemCleaner.Mode.SAFE;

                SystemCleaner cleaner = new SystemCleaner(origen, destino);
                cleanRes = cleaner.run(mode);

                System.out.println("🧹 Limpieza completada. Revisa clean.log en: " + destino);
                if (agresivo) {
                    if (System.getProperty("os.name","").toLowerCase().contains("win")) {
                        System.out.println("ℹ️ Si viste advertencias de permisos, ejecuta la consola como Administrador y repite.");
                    } else {
                        System.out.println("ℹ️ Si viste advertencias de permisos, usa: sudo java -jar <tu-jar>.jar");
                    }
                }
            }

            // 3) Reporte HTML
            HtmlReportWriter.write(
                    destino,
                    orgRes.moves,
                    orgRes.perCategory,
                    orgRes.filesMoved, orgRes.dirsMoved, orgRes.skipped, orgRes.errors,
                    cleanRes.events,
                    cleanRes.dnsCommandsTried,
                    cleanRes.dnsExitCodes,
                    cleanRes.totalBytesFreed
            );

            System.out.println("📄 Reporte HTML: " + destino.resolve("reporte.html"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- helpers ----------------

    private static int askMenu(Scanner sc) {
        System.out.println();
        System.out.println("Opciones");
        System.out.println("  1) Quiero ordenar");
        System.out.println("  2) Quiero limpiar los temporales");
        System.out.println("  3) Quiero hacer ambos");
        System.out.print("Elige [1-3]: ");

        String in = sc.nextLine().trim();
        int opt;
        try { opt = Integer.parseInt(in); } catch (Exception e) { opt = 3; } // por defecto ambos
        if (opt < 1 || opt > 3) opt = 3;
        return opt;
    }

    private static boolean askYesNo(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim().toLowerCase();
        return s.startsWith("s") || s.startsWith("y");
    }
}
