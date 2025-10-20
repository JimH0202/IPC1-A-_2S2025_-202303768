package vista;

import controlador.ControladorProducto;
import java.awt.*;
import javax.swing.*;
import modelo.Producto;

public class DialogActualizarProducto extends JDialog {
    public DialogActualizarProducto(Window owner, ControladorProducto controlador, String codigo) {
        super(owner, "Actualizar producto", ModalityType.APPLICATION_MODAL);
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setSize(420, 260);
        setLayout(new BorderLayout(8,8));

        Producto p = controlador.buscarProducto(codigo);
        if (p == null) {
            JOptionPane.showMessageDialog(owner, "Producto no encontrado");
            dispose();
            return;
        }

    JPanel form = new JPanel(new GridLayout(6,2,6,6));
    form.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    JTextField tfNombre = new JTextField(p.getNombre());
    JTextField tfAtributo = new JTextField();
    JTextField tfPrecio = new JTextField(String.valueOf(p.getPrecio()));
    form.add(new JLabel("Código:")); form.add(new JLabel(p.getCodigo()));
    form.add(new JLabel("Nombre:")); form.add(tfNombre);
    form.add(new JLabel("Atributo (según categoría):")); form.add(tfAtributo);
    form.add(new JLabel("Precio:")); form.add(tfPrecio);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnCancelar = new JButton("Cancelar");
        bottom.add(btnActualizar); bottom.add(btnCancelar);
        add(bottom, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnActualizar.addActionListener(e -> {
            String nuevoNombre = tfNombre.getText().trim();
            String nuevoAtributo = tfAtributo.getText().trim();
            if (nuevoNombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío");
                return;
            }
            boolean ok = controlador.actualizarProducto(p.getCodigo(), nuevoNombre, nuevoAtributo);
            try { double pr = Double.parseDouble(tfPrecio.getText().trim()); controlador.setPrecioProducto(p.getCodigo(), pr); } catch (Exception ignored) {}
            if (!ok) JOptionPane.showMessageDialog(this, "No fue posible actualizar"); else dispose();
        });
    }
}
