package tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import util.CSVUtil;

/**
 * Herramienta simple para normalizar el archivo historial_ingresos.csv.
 * Crea un backup historial_ingresos.csv.bak y reescribe el archivo con líneas normalizadas
 * en formato: fecha,hora,usuario,nombreProducto,cantidad
 */
public class HistorialNormalizer {
    public static void main(String[] args) throws Exception {
        File f = new File("historial_ingresos.csv");
        if (!f.exists()) { System.out.println("No existe historial_ingresos.csv"); return; }
        File bak = new File("historial_ingresos.csv.bak");
        if (!bak.exists()) f.renameTo(bak);
        List<String[]> rows = new ArrayList<>();
        try {
            List<String[]> raw = CSVUtil.readAll(bak.exists() ? bak : f);
            for (String[] r : raw) {
                if (r == null) continue;
                // caso esperado: [fecha, hora, usuario, producto, cantidad]
                if (r.length >= 5) {
                    // ya correcto
                    rows.add(new String[]{r[0], r[1], r[2], r[3], r[4]});
                    continue;
                }
                // casos comunes de legacy: todo en una columna o en 4 columnas
                // intentamos heurística: si la primera columna contiene '|' intentar split por '|'
                if (r.length == 1) {
                    String s = r[0];
                    if (s.contains("|")) {
                        String[] parts = s.split("\\|");
                        for (int i=0;i<parts.length;i++) parts[i] = parts[i].trim();
                        if (parts.length >= 5) rows.add(new String[]{parts[0], parts[1], parts[2], parts[3], parts[4]});
                        else {
                            // fallback: marcar como desconocido
                            rows.add(new String[]{"-","-","-", s, "0"});
                        }
                    } else {
                        rows.add(new String[]{"-","-","-", r[0], "0"});
                    }
                } else if (r.length == 4) {
                    // suponer fecha/hora juntos en r[0]
                    String fh = r[0]; String usuario = r[1]; String producto = r[2]; String cantidad = r[3];
                    // intentar separar fecha y hora por espacio
                    String fecha = fh; String hora = "-";
                    if (fh.contains(" ")) { int p = fh.indexOf(' '); fecha = fh.substring(0,p); hora = fh.substring(p+1); }
                    rows.add(new String[]{fecha, hora, usuario, producto, cantidad});
                } else {
                    // otros longitudes
                    String prod = r.length > 0 ? r[0] : "-";
                    rows.add(new String[]{"-","-","-", prod, "0"});
                }
            }
        } catch (IOException ex) {
            System.out.println("Error leyendo historial: " + ex.getMessage());
            return;
        }
        // escribir normalizado
        List<String> out = new ArrayList<>();
        for (String[] r : rows) {
            out.add(String.join(",", r));
        }
        try {
            CSVUtil.writeAll(f, out);
            System.out.println("Historial normalizado y backup creado (si no existía): " + bak.getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Error escribiendo historial: " + ex.getMessage());
        }
    }
}
