package vista;

import controlador.ControladorBitacora;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;

/**
 * Visor simple de la bitácora leyendo el archivo `bitacora.txt` y permitiendo exportar a CSV.
 */
public class DialogBitacoraViewer extends JDialog {
    private JTextArea ta;
    private final ControladorBitacora bit;
    private JComboBox<String> cbTipo;
    private JComboBox<String> cbOp;
    private JComboBox<String> cbCodigo;

    public DialogBitacoraViewer(Window owner, ControladorBitacora bit) {
        super(owner, "Bitácora del Sistema", ModalityType.APPLICATION_MODAL);
        this.bit = bit;
        setSize(800, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

    ta = new JTextArea(); ta.setEditable(false); ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    add(new JScrollPane(ta), BorderLayout.CENTER);

        // Panel de filtros
    JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filtros.add(new JLabel("Tipo usuario:"));
    cbTipo = new JComboBox<>();
    filtros.add(cbTipo);
    filtros.add(new JLabel("Operacion:"));
    cbOp = new JComboBox<>();
    filtros.add(cbOp);
    filtros.add(new JLabel("Codigo usuario:"));
    cbCodigo = new JComboBox<>(); cbCodigo.setEditable(true);
    filtros.add(cbCodigo);
    JButton btnAplicarFiltro = new JButton("Aplicar filtro"); filtros.add(btnAplicarFiltro);
    add(filtros, BorderLayout.NORTH);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRef = new JButton("Refrescar");
        JButton btnExport = new JButton("Exportar CSV");
        JButton btnClose = new JButton("Cerrar");
        bot.add(btnRef); bot.add(btnExport); bot.add(btnClose);
        add(bot, BorderLayout.SOUTH);

    btnRef.addActionListener((ActionEvent e) -> cargarArchivo());
    btnExport.addActionListener((ActionEvent e) -> exportarCSV());
        btnClose.addActionListener((ActionEvent e) -> dispose());

        btnAplicarFiltro.addActionListener((ActionEvent e) -> {
            String tipo = cbTipo.getSelectedItem() == null ? "" : cbTipo.getSelectedItem().toString();
            String op = cbOp.getSelectedItem() == null ? "" : cbOp.getSelectedItem().toString();
            String codigo = cbCodigo.getEditor() != null ? cbCodigo.getEditor().getItem().toString().trim() : "";
            aplicarFiltro(tipo, op, codigo);
        });

        // Exportar a PDF de lo mostrado
        JButton btnExportPDF = new JButton("Exportar PDF");
        bot.add(btnExportPDF);
        btnExportPDF.addActionListener((ActionEvent e) -> {
            JFileChooser ch = new JFileChooser(); ch.setSelectedFile(new File(util.PdfUtil.nombreReporte("BITACORA")));
            if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File out = ch.getSelectedFile();
            try {
                java.util.List<String> lineas = java.util.Arrays.asList(ta.getText().split("\n"));
                util.PdfUtil.generarReporteBitacora(out, lineas);
                JOptionPane.showMessageDialog(this, "PDF generado: " + out.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exportando PDF: " + ex.getMessage());
            }
        });

        cargarArchivo();
    }

    private void aplicarFiltro(String tipo, String operacion, String codigoUsuario) {
        File f = new File("bitacora.txt");
        if (!f.exists()) { ta.setText("No existe bitacora.txt"); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                String l = line;
                boolean ok = true;
                if (tipo != null && !tipo.isEmpty() && !l.contains("| " + tipo + " |")) ok = false;
                if (operacion != null && !operacion.isEmpty() && !l.contains("| " + operacion + " |")) ok = false;
                if (codigoUsuario != null && !codigoUsuario.isEmpty() && !l.contains("| " + codigoUsuario + " |")) ok = false;
                if (ok) { sb.append(line).append('\n'); }
            }
            ta.setText(sb.toString());
        } catch (Exception ex) {
            ta.setText("Error aplicando filtro: " + ex.getMessage());
        }
    }

    private void cargarArchivo() {
        File f = new File("bitacora.txt");
        if (!f.exists()) { ta.setText("No existe bitacora.txt"); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            StringBuilder sb = new StringBuilder();
            String line;
            java.util.Set<String> tipos = new java.util.TreeSet<>();
            java.util.Set<String> ops = new java.util.TreeSet<>();
            java.util.Set<String> cods = new java.util.TreeSet<>();
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
                // parsear formato exacto: [DD/MM/YYYY HH:mm:ss] | TIPO | CODIGO | OPER | EST | DES
                String[] parts = line.split("\\s*\\|\\s*");
                if (parts.length >= 2) {
                    tipos.add(parts[1].trim());
                }
                if (parts.length >= 4) {
                    ops.add(parts[3].trim());
                }
                if (parts.length >= 3) {
                    cods.add(parts[2].trim());
                }
            }
            ta.setText(sb.toString());
            // poblar combos (si existen)
            // poblar combos existentes
            cbTipo.removeAllItems(); cbTipo.addItem(""); for (String t : tipos) cbTipo.addItem(t);
            cbOp.removeAllItems(); cbOp.addItem(""); for (String o : ops) cbOp.addItem(o);
            // poblar codigo usuario sin duplicados
            cbCodigo.removeAllItems(); cbCodigo.addItem(""); for (String c : cods) cbCodigo.addItem(c);
        } catch (Exception ex) {
            ta.setText("Error leyendo bitácora: " + ex.getMessage());
        }
    }

    private void exportarCSV() {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File("bitacora_export.csv"));
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File out = ch.getSelectedFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            String[] lines = ta.getText().split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String csv = line.replace(" | ", ",");
                bw.write(csv); bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Exportado: " + out.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exportando: " + ex.getMessage());
        }
    }
}
