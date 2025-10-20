package vista;

import util.CSVUtil;
import util.PdfUtil;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Diálogo simple para ver el historial de ingresos (CSV) generado por el sistema.
 */
public class DialogHistorialIngresos extends JDialog {
    private final DefaultTableModel modelo;
    private final JTable tabla;

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
        // aplicar filtro tras cargar
        if (filtroCodigoProducto != null && !filtroCodigoProducto.trim().isEmpty()) {
            // filtrar filas actuales
            for (int r = modelo.getRowCount()-1; r >= 0; r--) {
                Object nombre = modelo.getValueAt(r, 3); // columna nombre producto
                Object codigoCol = modelo.getValueAt(r, 1); // si hubiera codigo en ese campo
                String n = nombre == null ? "" : nombre.toString();
                String c = codigoCol == null ? "" : codigoCol.toString();
                if (!n.equalsIgnoreCase(filtroCodigoProducto) && !c.equalsIgnoreCase(filtroCodigoProducto)) modelo.removeRow(r);
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
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = ch.getSelectedFile();
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
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = ch.getSelectedFile();
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
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exportando PDF: " + ex.getMessage());
            }
        }
    }
}
