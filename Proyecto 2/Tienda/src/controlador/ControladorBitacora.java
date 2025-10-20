package controlador;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de bitácora que escribe eventos en archivo bitacora.txt y permite registro simple en memoria si se desea.
 */
public class ControladorBitacora {
    private final String archivo = "bitacora.txt";
    private final DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void registrar(String tipoUsuario, String codigoUsuario, String operacion, String estado, String descripcion) {
        String timestamp = LocalDateTime.now().format(f);
        String linea = String.format("[%s] | %s | %s | %s | %s | %s", timestamp, tipoUsuario, codigoUsuario, operacion, estado, descripcion);
        escribirArchivo(linea);
    }

    private synchronized void escribirArchivo(String linea) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(archivo, true)))) {
            pw.println(linea);
        } catch (Exception e) {
            // si falla, no interrumpir la ejecución
            System.err.println("Error escribiendo bitácora: " + e.getMessage());
        }
    }
}   