import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
// ejecutar en vs code
// javac -cp ".;librerias\\itextpdf-5.5.12.jar" src\\*.java
// java -cp ".;librerias\\itextpdf-5.5.12.jar;src" Main

public class Reportes {

    private static final int MAX_ACCIONES = 200; // límite de registros en bitácora
    private static String[][] bitacoraAcciones = new String[MAX_ACCIONES][4];
    private static int contadorAcciones = 0;

    // MÉTODO PARA REGISTRAR ACCIONES
    public static void registrarAccion(String tipoAccion, String estado, String usuario) {
        if (contadorAcciones < MAX_ACCIONES) {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaHora = LocalDateTime.now().format(formato);

            bitacoraAcciones[contadorAcciones][0] = fechaHora;     // Fecha y hora
            bitacoraAcciones[contadorAcciones][1] = tipoAccion;    // Acción
            bitacoraAcciones[contadorAcciones][2] = estado;        // Estado
            bitacoraAcciones[contadorAcciones][3] = usuario;       // Usuario
            contadorAcciones++;
        }
    }

    // MOSTRAR BITÁCORA EN CONSOLA
    public static void bitacora() {
        if (contadorAcciones == 0) {
            System.out.println("No hay acciones registradas en la bitácora.");
            return;
        }

        System.out.println("****** BITÁCORA DE ACCIONES ******");
        System.out.printf("%-20s %-20s %-15s %-10s%n", "Fecha y Hora", "Acción", "Estado", "Usuario");
        System.out.println("-----------------------------------------------------------------------");

        for (int i = 0; i < contadorAcciones; i++) {
            System.out.printf("%-20s %-20s %-15s %-10s%n",
                    bitacoraAcciones[i][0],
                    bitacoraAcciones[i][1],
                    bitacoraAcciones[i][2],
                    bitacoraAcciones[i][3]);
        }

        System.out.println("----------------------------------------------------------------------");
    }

    // ================== MENÚ DE REPORTES ==================
    public static void Generarreportes(Scanner teclado, String usuario) {
        int opcion;
        do {
            System.out.println("**** MENÚ REPORTES ****");
            System.out.println("1. Reporte de Stock");
            System.out.println("2. Reporte de Ventas");
            System.out.println("3. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            opcion = teclado.nextInt();
            teclado.nextLine(); // limpiar buffer

            switch (opcion) {
                case 1 -> generarReporteStock(usuario);
                case 2 -> generarReporteVentas(usuario);
                case 3 -> System.out.println("Regresando al menú principal...");
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        } while (opcion != 3);
    }

    // ================== REPORTE DE STOCK ==================
    private static void generarReporteStock(String usuario) {
        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
            String nombreArchivo = formato.format(LocalDateTime.now()) + "_Stock.pdf";

            Document documento = new Document();
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            documento.add(new Paragraph("Reporte de Stock", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            documento.add(new Paragraph("Fecha de generación: " + LocalDateTime.now()));
            documento.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(5); // 5 columnas
            tabla.setWidthPercentage(100);
            tabla.addCell("Código");
            tabla.addCell("Nombre");
            tabla.addCell("Categoría");
            tabla.addCell("Precio (Q)");
            tabla.addCell("Cantidad");

            for (int i = 0; i < Producto.getContador(); i++) {
                Producto p = Producto.getInventario()[i];
                tabla.addCell(p.getCodigo());
                tabla.addCell(p.getNombre());
                tabla.addCell(p.getCategoria());
                tabla.addCell(String.valueOf(p.getPrecio()));
                tabla.addCell(String.valueOf(p.getCantidad()));
            }

            documento.add(tabla);
            documento.close();

            System.out.println("Reporte de stock generado: " + nombreArchivo);
            registrarAccion("Generar Reporte Stock", "Correcta", usuario);
        } catch (FileNotFoundException | DocumentException e) {
            System.out.println("Error al generar reporte de stock: " + e.getMessage());
            registrarAccion("Generar Reporte Stock", "Errónea", usuario);
        }
    }

    // ================== REPORTE DE VENTAS ==================
    private static void generarReporteVentas(String usuario) {
        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
            String nombreArchivo = formato.format(LocalDateTime.now()) + "_Venta.pdf";

            Document documento = new Document();
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            documento.add(new Paragraph("Reporte de Ventas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            documento.add(new Paragraph("Fecha de generación: " + LocalDateTime.now()));
            documento.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(4); // 4 columnas
            tabla.setWidthPercentage(100);
            tabla.addCell("Código");
            tabla.addCell("Cantidad");
            tabla.addCell("Total (Q)");
            tabla.addCell("Fecha y Hora");

            if (Venta.getHistorialVentas().isEmpty()) {
                documento.add(new Paragraph("No hay ventas registradas."));
            } else {
                for (Venta v : Venta.getHistorialVentas()) {
                    tabla.addCell(v.getCodigoProducto());
                    tabla.addCell(String.valueOf(v.getCantidadVendida()));
                    tabla.addCell(String.format("%.2f", v.getTotal()));
                    tabla.addCell(v.getFechaHora());
                }
                documento.add(tabla);
            }

            documento.close();

            System.out.println("Reporte de ventas generado: " + nombreArchivo);
            registrarAccion("Generar Reporte Ventas", "Correcta", usuario);
        } catch (IOException | DocumentException e) {
            System.out.println("Error al generar reporte de ventas: " + e.getMessage());
            registrarAccion("Generar Reporte Ventas", "Errónea", usuario);
        }
    }
}
