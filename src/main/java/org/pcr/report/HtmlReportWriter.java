
package org.pcr.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HtmlReportWriter {

    public static void write(Path destinoDir,
                             List<MoveEvent> moves,
                             Map<String,Integer> perCategory,
                             int filesMoved, int dirsMoved, int skipped, int errors,
                             List<CleanEvent> cleanEvents,
                             List<String> dnsCommandsTried,
                             Map<String,Integer> dnsExitCodes,
                             long totalBytesFreed) throws IOException {

        Files.createDirectories(destinoDir);
        Path out = destinoDir.resolve("reporte.html");

        int totalMovs = Math.max(0, filesMoved + dirsMoved + skipped + errors);
        int maxCat = 0;
        if (perCategory != null) {
            for (Integer v : perCategory.values()) if (v != null) maxCat = Math.max(maxCat, v);
        }
        if (maxCat == 0) maxCat = 1;

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        try (BufferedWriter w = Files.newBufferedWriter(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("<!doctype html><html lang='es'><head><meta charset='utf-8'>");
            w.write("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            w.write("<title>Reporte de Organización y Limpieza</title>");
            w.write("<style>");
            w.write("html,body{background:#0b1220;color:#e6e8ee;font-family:ui-sans-serif,system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:0}");
            w.write(".wrap{padding:28px;max-width:1200px;margin:0 auto}");
            w.write(".h1{font-size:26px;font-weight:800;margin:0 0 6px}");
            w.write(".muted{opacity:.75}");
            w.write(".grid{display:grid;gap:16px}");
            w.write(".cards{grid-template-columns:repeat(auto-fit,minmax(220px,1fr))}");
            w.write(".card{background:#111a2b;border:1px solid #24324a;border-radius:14px;padding:16px;box-shadow:0 8px 30px rgba(0,0,0,.35)}");
            w.write(".section-title{font-size:18px;font-weight:700;margin:6px 0 12px}");
            w.write(".kpi{font-size:28px;font-weight:800;margin:2px 0 0}");
            w.write(".kpl{font-size:12px;opacity:.8;margin:0}");
            w.write(".table-wrap{overflow:auto;border-radius:12px;border:1px solid #23324b}");
            w.write(".table{min-width:920px;width:100%;border-collapse:collapse;font-size:14px;background:#0f1729}");
            w.write(".table th,.table td{border-bottom:1px solid #23324b;padding:10px 8px;vertical-align:top}");
            w.write(".table th{color:#aab3c5;text-align:left;font-weight:600;background:#0c1628;position:sticky;top:0}");
            w.write(".mono{font-family:ui-monospace,SFMono-Regular,Consolas,Monaco,monospace;font-size:12px;word-break:break-all}");
            w.write(".badge{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;border:1px solid #334567;background:#0f1a2a}");
            w.write(".ok{color:#9FE870;border-color:#2d6a39;background:rgba(45,106,57,.15)}");
            w.write(".warn{color:#ffd166;border-color:#7a5c21;background:rgba(122,92,33,.15)}");
            w.write(".err{color:#ff6b6b;border-color:#723232;background:rgba(114,50,50,.15)}");
            w.write(".cat{color:#6ab0ff;border-color:#2d4e7a;background:rgba(45,78,122,.15)}");
            w.write(".chart{background:#0f1729;border:1px solid #23324b;border-radius:12px;padding:12px}");
            w.write(".legend{display:flex;gap:10px;flex-wrap:wrap;margin-top:8px}");
            w.write(".legend span{display:inline-flex;align-items:center;gap:6px;font-size:12px;opacity:.9}");
            w.write(".dot{width:10px;height:10px;border-radius:2px;display:inline-block}");
            w.write(".dot-cat{background:#59A5FF}.dot-sum1{background:#9FE870}.dot-sum2{background:#6AB0FF}.dot-sum3{background:#FFD166}.dot-sum4{background:#FF6B6B}");
            w.write(".center{display:flex;justify-content:center}");
            w.write(".footer{margin-top:24px;opacity:.7;font-size:12px;text-align:center}");
            w.write("a{color:#8fcaff;text-decoration:none}a:hover{text-decoration:underline}");
            w.write("</style></head><body><div class='wrap'>");

            w.write("<div class='h1'>Reporte de Organización y Limpieza</div>");
            w.write("<div class='muted' style='margin-bottom:14px'>Generado: " + esc(fecha) + "</div>");

            w.write("<div class='grid cards' style='margin-top:6px'>");
            w.write(cardKpi("Archivos movidos", String.valueOf(filesMoved), "✅ OK"));
            w.write(cardKpi("Carpetas movidas", String.valueOf(dirsMoved), "📦 OK"));
            w.write(cardKpi("Saltados", String.valueOf(skipped), "⚠️ Revisar"));
            w.write(cardKpi("Errores", String.valueOf(errors), "❌ Atención"));
            w.write(cardKpi("Espacio liberado", human(totalBytesFreed), "🧹 Limpieza"));
            w.write("</div>");

            w.write("<div class='grid' style='grid-template-columns:repeat(auto-fit,minmax(320px,1fr));margin-top:16px'>");

            w.write("<div class='card'><div class='section-title'>Distribución por categoría</div>");
            w.write("<div class='chart'>");
            w.write(renderCategoryChart(perCategory, maxCat));
            w.write("<div class='legend'><span><i class='dot dot-cat'></i> Archivos por categoría</span></div>");
            w.write("</div></div>");

            w.write("<div class='card'><div class='section-title'>Resumen de acciones</div>");
            w.write("<div class='chart'>");
            w.write(renderSummaryChart(filesMoved, dirsMoved, skipped, errors));
            w.write("<div class='legend'>");
            w.write("<span><i class='dot dot-sum1'></i> Archivos</span>");
            w.write("<span><i class='dot dot-sum2'></i> Carpetas</span>");
            w.write("<span><i class='dot dot-sum3'></i> Saltados</span>");
            w.write("<span><i class='dot dot-sum4'></i> Errores</span>");
            w.write("</div></div></div>");

            w.write("</div>");

            w.write("<div class='card' style='margin-top:16px'>");
            w.write("<div class='section-title'>Detalle de movimientos</div>");
            w.write("<div class='table-wrap'><table class='table'><thead><tr>");
            w.write("<th style='min-width:90px'>Tipo</th><th style='min-width:120px'>Categoría</th>");
            w.write("<th style='min-width:380px'>Origen</th><th style='min-width:380px'>Destino</th>");
            w.write("<th style='min-width:160px'>Motivo</th><th style='min-width:110px'>Tamaño</th>");
            w.write("</tr></thead><tbody>");

            if (moves != null && !moves.isEmpty()) {
                for (MoveEvent ev : moves) {
                    String tipoBadge;
                    if ("file".equals(ev.type))      tipoBadge = "<span class='badge ok'>archivo</span>";
                    else if ("dir".equals(ev.type))  tipoBadge = "<span class='badge ok'>carpeta</span>";
                    else if ("skip".equals(ev.type)) tipoBadge = "<span class='badge warn'>saltado</span>";
                    else if ("error".equals(ev.type))tipoBadge = "<span class='badge err'>error</span>";
                    else                              tipoBadge = "<span class='badge'>" + esc(ev.type) + "</span>";

                    w.write("<tr>");
                    w.write("<td>" + tipoBadge + "</td>");
                    w.write("<td>" + (ev.category == null ? "-" : "<span class='badge cat'>" + esc(ev.category) + "</span>") + "</td>");
                    w.write("<td class='mono'>" + (ev.src == null ? "-" : esc(ev.src.toString())) + "</td>");
                    w.write("<td class='mono'>" + (ev.dst == null ? "-" : esc(ev.dst.toString())) + "</td>");
                    w.write("<td>" + (ev.reason == null ? "-" : esc(ev.reason)) + "</td>");
                    w.write("<td class='mono'>" + (ev.sizeBytes >= 0 ? esc(human(ev.sizeBytes)) : "-") + "</td>");
                    w.write("</tr>");
                }
            } else {
                w.write("<tr><td colspan='6' class='muted'>No hay movimientos registrados.</td></tr>");
            }
            w.write("</tbody></table></div></div>");

            w.write("<div class='center' style='margin-top:18px'><div class='card' style='max-width:1120px;flex:1'>");
            w.write("<div class='section-title'>Limpieza del sistema</div>");

            w.write("<div class='grid' style='grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:12px'>");
            w.write("<div class='chart'>");
            w.write(renderCleanBytesChart(cleanEvents));
            w.write("</div>");
            w.write("<div class='chart'>");
            w.write(renderCleanDonut(cleanEvents));
            w.write("</div>");
            w.write("</div>");

            w.write("<div class='grid' style='grid-template-columns:repeat(auto-fit,minmax(420px,1fr));gap:12px;margin-top:12px'>");

            w.write("<div>");
            w.write("<div class='muted' style='margin-bottom:8px'>Acciones realizadas</div>");
            w.write("<div class='table-wrap'><table class='table'><thead><tr>");
            w.write("<th>Acción</th><th>Estado</th><th>Detalle</th><th>Bytes</th>");
            w.write("</tr></thead><tbody>");
            if (cleanEvents != null && !cleanEvents.isEmpty()) {
                for (CleanEvent ce : cleanEvents) {
                    String st = ce.success ? "<span class='badge ok'>ok</span>" : "<span class='badge warn'>parcial</span>";
                    w.write("<tr>");
                    w.write("<td>" + esc(nullDash(ce.name)) + "</td>");
                    w.write("<td>" + st + "</td>");
                    w.write("<td class='mono'>" + esc(nullDash(ce.detail)) + "</td>");
                    w.write("<td class='mono'>" + (ce.bytesFreed >= 0 ? esc(human(ce.bytesFreed)) : "-") + "</td>");
                    w.write("</tr>");
                }
            } else {
                w.write("<tr><td colspan='4' class='muted'>No se registraron acciones de limpieza.</td></tr>");
            }
            w.write("</tbody></table></div>");
            w.write("</div>");

            w.write("<div>");
            w.write("<div class='muted' style='margin-bottom:8px'>DNS (comandos ejecutados)</div>");
            w.write("<div class='table-wrap'><table class='table'><thead><tr><th>Comando</th><th>Exit</th></tr></thead><tbody>");
            if (dnsCommandsTried != null && !dnsCommandsTried.isEmpty()) {
                for (String cmd : dnsCommandsTried) {
                    Integer code = dnsExitCodes == null ? null : dnsExitCodes.get(cmd);
                    String badge = code == null ? "<span class='badge'>-</span>"
                            : (code == 0 ? "<span class='badge ok'>0</span>" : "<span class='badge warn'>" + code + "</span>");
                    w.write("<tr><td class='mono'>" + esc(cmd) + "</td><td>" + badge + "</td></tr>");
                }
            } else {
                w.write("<tr><td colspan='2' class='muted'>No se intentó limpiar la caché DNS.</td></tr>");
            }
            w.write("</tbody></table></div>");
            w.write("</div>");

            w.write("</div>");
            w.write("</div></div>");

            w.write("<div class='footer'>Creado por <strong>Patricio Calderón</strong> y <strong>OpenIA</strong>.</div>");

            w.write("</div></body></html>");
        }
    }

    private static String renderCategoryChart(Map<String,Integer> perCategory, int maxCat) {
        int barH = 20, gap = 10, padding = 10, width = 520;
        List<Map.Entry<String,Integer>> entries = new ArrayList<>();
        if (perCategory != null) entries.addAll(perCategory.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));

        int height = padding * 2 + (barH + gap) * Math.max(1, entries.size());
        StringBuilder sb = new StringBuilder();
        sb.append("<svg width='100%' viewBox='0 0 ").append(width).append(" ").append(height).append("'>");
        int y = padding + 5;
        if (entries.isEmpty()) {
            sb.append(text(10, y + 10, "Sin datos de categorías", "#aab3c5", 12));
        } else {
            for (Map.Entry<String,Integer> e : entries) {
                String cat = e.getKey();
                int val = e.getValue() == null ? 0 : e.getValue();
                double pct = Math.min(1.0, (double) val / (double) maxCat);
                int w = (int) Math.round(pct * (width - 170));
                sb.append(rect(150, y - 12, Math.max(2, w), barH, "#59A5FF"));
                sb.append(text(10, y + 4, truncate(cat, 18), "#c9d2e3", 12));
                sb.append(text(160 + Math.max(2, w) + 8, y + 4, String.valueOf(val), "#9FE870", 12));
                y += (barH + gap);
            }
        }
        sb.append("</svg>");
        return sb.toString();
    }

    private static String renderSummaryChart(int files, int dirs, int skipped, int errors) {
        int width = 520, height = 160, baseY = 130, barW = 40, gap = 35, startX = 40;
        int max = Math.max(1, Math.max(Math.max(files, dirs), Math.max(skipped, errors)));
        int hFiles = scale(files, max, baseY - 20);
        int hDirs  = scale(dirs,  max, baseY - 20);
        int hSkip  = scale(skipped, max, baseY - 20);
        int hErr   = scale(errors, max, baseY - 20);

        StringBuilder sb = new StringBuilder();
        sb.append("<svg width='100%' viewBox='0 0 ").append(width).append(" ").append(height).append("'>");
        sb.append(line(30, baseY, width - 20, baseY, "#23324b"));

        int x = startX;
        sb.append(rect(x, baseY - hFiles, barW, hFiles, "#9FE870"));
        sb.append(text(x - 2, baseY + 14, "Arch", "#aab3c5", 11));
        sb.append(text(x + 6, baseY - hFiles - 6, String.valueOf(files), "#cfead0", 11));

        x += (barW + gap);
        sb.append(rect(x, baseY - hDirs, barW, hDirs, "#6AB0FF"));
        sb.append(text(x + 2, baseY + 14, "Dir", "#aab3c5", 11));
        sb.append(text(x + 6, baseY - hDirs - 6, String.valueOf(dirs), "#cfead0", 11));

        x += (barW + gap);
        sb.append(rect(x, baseY - hSkip, barW, hSkip, "#FFD166"));
        sb.append(text(x - 6, baseY + 14, "Skip", "#aab3c5", 11));
        sb.append(text(x + 6, baseY - hSkip - 6, String.valueOf(skipped), "#f7e3b0", 11));

        x += (barW + gap);
        sb.append(rect(x, baseY - hErr, barW, hErr, "#FF6B6B"));
        sb.append(text(x - 2, baseY + 14, "Err", "#aab3c5", 11));
        sb.append(text(x + 6, baseY - hErr - 6, String.valueOf(errors), "#f1cccc", 11));

        sb.append("</svg>");
        return sb.toString();
    }

    private static String renderCleanBytesChart(List<CleanEvent> cleanEvents) {
        List<CleanEvent> items = new ArrayList<>();
        long max = 0;
        if (cleanEvents != null) {
            for (CleanEvent ce : cleanEvents) {
                if (ce != null && ce.bytesFreed > 0) {
                    items.add(ce);
                    max = Math.max(max, ce.bytesFreed);
                }
            }
        }
        if (items.isEmpty()) {
            return "<div class='muted'>Sin datos de bytes liberados.</div>";
        }
        int barH = 18, gap = 8, padding = 10, width = 520;
        int height = padding * 2 + (barH + gap) * items.size();
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>Espacio liberado por acción</div>");
        sb.append("<svg width='100%' viewBox='0 0 ").append(width).append(" ").append(height).append("'>");
        int y = padding + 5;
        for (CleanEvent ce : items) {
            double pct = Math.min(1.0, (double) ce.bytesFreed / (double) Math.max(1, max));
            int w = (int) Math.round(pct * (width - 170));
            sb.append(rect(150, y - 12, Math.max(2, w), barH, "#6AB0FF"));
            sb.append(text(10, y + 4, truncate(ce.name, 28), "#c9d2e3", 12));
            sb.append(text(160 + Math.max(2, w) + 8, y + 4, human(ce.bytesFreed), "#9FE870", 12));
            y += (barH + gap);
        }
        sb.append("</svg>");
        return sb.toString();
    }

    private static String renderCleanDonut(List<CleanEvent> cleanEvents) {
        int ok = 0, parcial = 0;
        if (cleanEvents != null) {
            for (CleanEvent ce : cleanEvents) {
                if (ce != null) {
                    if (ce.success) ok++;
                    else            parcial++;
                }
            }
        }
        int total = Math.max(1, ok + parcial);
        double pctOk = ok / (double) total;

        int size = 160, stroke = 18, r = (size/2) - stroke, cx = size/2, cy = size/2;
        double circ = 2 * Math.PI * r;
        double okLen = circ * pctOk;
        double parLen = circ - okLen;

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>Estado de limpieza</div>");
        sb.append("<svg width='100%' viewBox='0 0 ").append(size).append(" ").append(size).append("'>");
        sb.append("<circle cx='"+cx+"' cy='"+cy+"' r='"+r+"' fill='none' stroke='#23324b' stroke-width='"+stroke+"'></circle>");
        sb.append("<circle cx='"+cx+"' cy='"+cy+"' r='"+r+"' fill='none' stroke='#9FE870' stroke-width='"+stroke+"' ");
        sb.append("stroke-dasharray='"+okLen+" "+(circ-okLen)+"' stroke-linecap='round' transform='rotate(-90 "+cx+" "+cy+")'></circle>");
        sb.append("<circle cx='"+cx+"' cy='"+cy+"' r='"+r+"' fill='none' stroke='#FFD166' stroke-width='"+stroke+"' ");
        sb.append("stroke-dasharray='"+parLen+" "+(circ-parLen)+"' stroke-dashoffset='"+okLen+"' stroke-linecap='round' transform='rotate(-90 "+cx+" "+cy+")'></circle>");
        sb.append(text(cx-12, cy+4, (int)Math.round(pctOk*100) + "%", "#e6e8ee", 16));
        sb.append("</svg>");
        sb.append("<div class='legend'><span><i class='dot dot-sum1'></i>OK ("+ok+")</span><span><i class='dot dot-sum3'></i>Parcial ("+parcial+")</span></div>");
        return sb.toString();
    }

    private static String rect(int x,int y,int w,int h,String color){
        return "<rect x='"+x+"' y='"+y+"' width='"+w+"' height='"+h+"' rx='4' fill='"+color+"' />";
    }
    private static String line(int x1,int y1,int x2,int y2,String color){
        return "<line x1='"+x1+"' y='"+y1+"' x2='"+x2+"' y='"+y2+"' stroke='"+color+"' stroke-width='1' />";
    }
    private static String text(int x,int y,String t,String color,int size){
        return "<text x='"+x+"' y='"+y+"' fill='"+color+"' font-size='"+size+"px' dominant-baseline='middle'>"+esc(t)+"</text>";
    }
    private static int scale(int v,int max,int maxPx){
        return (int)Math.round((v/(double)Math.max(1,max))*maxPx);
    }

    private static String human(long bytes){
        if (bytes < 0) return "-";
        if (bytes == 0) return "0 B";
        final String[] u = {"B","KB","MB","GB","TB"};
        int i = (int)Math.floor(Math.log(bytes)/Math.log(1024));
        i = Math.max(0, Math.min(i, u.length-1));
        return String.format(Locale.US,"%.1f %s", bytes/Math.pow(1024,i), u[i]);
    }
    private static String esc(String s){
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
    private static String truncate(String s,int n){
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, Math.max(0, n-1)) + "…";
    }
    private static String nullDash(String s){ return (s == null || s.isBlank()) ? "-" : s; }

    private static String cardKpi(String label, String value, String hint){
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card'>");
        sb.append("<div class='kpl'>").append(esc(label)).append("</div>");
        sb.append("<div class='kpi'>").append(esc(value)).append("</div>");
        sb.append("<div class='muted small'>").append(esc(hint)).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
