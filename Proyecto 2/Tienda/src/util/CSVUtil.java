package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitario simple para leer y escribir CSV sin dependencias externas.
 * Lee líneas y devuelve arrays de columnas (trim a cada columna).
 * No intenta parsear comillas ni separadores complejos, suficiente para el formato del proyecto.
 */
public final class CSVUtil {
    private CSVUtil() { }

    public static String[] parseLine(String line) {
        if (line == null) return new String[0];
        String[] parts = line.split(",");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    public static List<String[]> readAll(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                rows.add(parseLine(line));
            }
        }
        return rows;
    }

    public static void writeAppend(File file, String line) throws IOException {
        try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();
            bw.flush();
        }
    }

    public static void writeAll(File file, List<String> lines) throws IOException {
        try (FileWriter fw = new FileWriter(file); BufferedWriter bw = new BufferedWriter(fw)) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
            bw.flush();
        }
    }
}