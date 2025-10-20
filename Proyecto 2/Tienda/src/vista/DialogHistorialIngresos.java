package vista;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import util.CSVUtil;
import util.PdfUtil;

/**
 * Diálogo simple para ver el historial de ingresos (CSV) generado por el sistema.
 */
public class DialogHistorialIngresos extends JDialog {
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private String filtroProducto = null;

    public DialogHistorialIngresos(Window owner) {
        super(owner, "Historial de Ingresos", ModalityType.APPLICATION_MODAL);
        setSize(700,400);
        setLocationRelativeTo(owner);

        modelo = new DefaultTableModel(new Object[]{"Fecha","Hora","Usuario","Producto","Cantidad"},0) {
            @Override public boolean isCellEditable(int row,int col){ return false; }
        };
        tabla = new JTable(modelo);

        JButton btnExportCSV = new JButton("Exportar CSV");
        JButton btnExportPDF = new JButton("Exportar PDF");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnExportCSV);
        botones.add(btnExportPDF);

        btnExportCSV.addActionListener(e -> exportarCSV());
        btnExportPDF.addActionListener(e -> exportarPDF());

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        cargarArchivo();
    }

    public DialogHistorialIngresos(Window owner, String filtroCodigoProducto) {
        this(owner);
        this.filtroProducto = (filtroCodigoProducto == null || filtroCodigoProducto.trim().isEmpty()) ? null : filtroCodigoProducto.trim();
        // aplicar filtro tras cargar
        if (this.filtroProducto != null) {
            // filtrar filas actuales
            for (int r = modelo.getRowCount()-1; r >= 0; r--) {
                Object nombre = modelo.getValueAt(r, 3); // columna nombre producto
                Object codigoCol = modelo.getValueAt(r, 1); // si hubiera codigo en ese campo
                String n = nombre == null ? "" : nombre.toString();
                String c = codigoCol == null ? "" : codigoCol.toString();
                if (!n.equalsIgnoreCase(this.filtroProducto) && !c.equalsIgnoreCase(this.filtroProducto)) modelo.removeRow(r);
            }
        }
    }

    private File obtenerArchivoHistorial() {
        // archivo relativo al directorio del proyecto
        return new File("historial_ingresos.csv");
    }

    private void cargarArchivo() {
        modelo.setRowCount(0);
        File f = obtenerArchivoHistorial();
        if (!f.exists()) return;
        try {
            java.util.List<String[]> rows = CSVUtil.readAll(f);
            for (String[] r : rows) {
                // Esperamos formato: fecha,hora,usuario,producto,cantidad
                if (r.length >= 5) modelo.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4]});
                else if (r.length > 0) {
                    // rellenar con lo disponible
                    List<String> cols = new ArrayList<>();
                    for (int i = 0; i < 5; i++) cols.add(i < r.length ? r[i] : "");
                    modelo.addRow(cols.toArray());
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error leyendo historial: " + ex.getMessage());
        }
    }

    private void exportarCSV() {
        JFileChooser ch = new JFileChooser();
        // sugerir nombre por defecto
        String sugerencia = "Historial_ingresos_" + (filtroProducto == null ? "general" : filtroProducto) + ".csv";
        ch.setSelectedFile(new File(sugerencia));
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = ch.getSelectedFile();
            // asegurar extensión .csv
            if (!out.getName().toLowerCase().endsWith(".csv")) out = new File(out.getAbsolutePath() + ".csv");
            List<String> lines = new ArrayList<>();
            for (int r = 0; r < modelo.getRowCount(); r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    if (c > 0) sb.append(",");
                    Object val = modelo.getValueAt(r, c);
                    sb.append(val == null ? "" : val.toString());
                }
                lines.add(sb.toString());
            }
            try {
                util.CSVUtil.writeAll(out, lines);
                JOptionPane.showMessageDialog(this, "CSV exportado: " + out.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exportando CSV: " + ex.getMessage());
            }
        }
    }

    private void exportarPDF() {
        JFileChooser ch = new JFileChooser();
        // sugerir nombre por defecto
        String sugerencia = "Historial_ingresos_" + (filtroProducto == null ? "general" : filtroProducto) + ".pdf";
        ch.setSelectedFile(new File(sugerencia));
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = ch.getSelectedFile();
            if (!out.getName().toLowerCase().endsWith(".pdf")) out = new File(out.getAbsolutePath() + ".pdf");
            // construir filas para PdfUtil
            List<String[]> filas = new ArrayList<>();
            for (int r = 0; r < modelo.getRowCount(); r++) {
                String[] row = new String[modelo.getColumnCount()];
                for (int c = 0; c < modelo.getColumnCount(); c++) row[c] = String.valueOf(modelo.getValueAt(r, c));
                filas.add(row);
            }
            try {
                String[] headers = new String[modelo.getColumnCount()];
                for (int i = 0; i < headers.length; i++) headers[i] = modelo.getColumnName(i);
                PdfUtil.generarReporteTabla(out, "Historial de ingresos", headers, filas);
                JOptionPane.showMessageDialog(this, "PDF exportado: " + out.getAbsolutePath());
            } catch (Throwable t) {
                // Atrapar Throwable para incluir NoClassDefFoundError u otros errores de la librería iText
                t.printStackTrace();
                // buscar causa que indique falta de clase de iText
                boolean missingIText = false;
                Throwable x = t;
                while (x != null) {
                    if (x instanceof NoClassDefFoundError || x.getMessage() != null && x.getMessage().contains("com.itextpdf")) { missingIText = true; break; }
                    x = x.getCause();
                }
                if (missingIText) {
                    JOptionPane.showMessageDialog(this, "Error exportando PDF: falta la librería iText en el classpath. Ejecute la aplicación con 'librerias\\itextpdf-5.5.12.jar' en el classpath. (ver consola para más detalles)");
                } else {
                    String msg = t.getMessage() == null ? t.toString() : t.getMessage();
                    JOptionPane.showMessageDialog(this, "Error exportando PDF: " + msg + " (ver consola para más detalles)");
                }
            }
        }
    }
}
