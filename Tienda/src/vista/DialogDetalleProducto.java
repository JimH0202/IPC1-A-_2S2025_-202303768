package vista;

import modelo.Producto;
import javax.swing.*;
import java.awt.*;

/** Diálogo que muestra el detalle específico de un producto según su categoría. */
public class DialogDetalleProducto extends JDialog {
    public DialogDetalleProducto(Window owner, Producto p) {
        super(owner, "Detalle del Producto", ModalityType.APPLICATION_MODAL);
        setSize(420, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JTextArea ta = new JTextArea(); ta.setEditable(false);
        ta.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        StringBuilder sb = new StringBuilder();
        sb.append("Código: ").append(p.getCodigo()).append('\n');
        sb.append("Nombre: ").append(p.getNombre()).append('\n');
        sb.append("Categoría: ").append(p.getCategoria()).append('\n');
    sb.append("Precio: ").append(String.format("%.2f", p.getPrecio())).append('\n');
        sb.append("\n");
        sb.append(p.getDetalle());
        ta.setText(sb.toString());
        add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("Cerrar"); btn.addActionListener(ev -> dispose()); bot.add(btn);
        add(bot, BorderLayout.SOUTH);
    }
}
