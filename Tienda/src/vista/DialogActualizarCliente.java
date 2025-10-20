package vista;

import controlador.ControladorUsuario;
import java.awt.*;
import javax.swing.*;

/**
 * Diálogo para actualizar datos básicos de un Cliente (nombre y contraseña).
 */
public class DialogActualizarCliente extends JDialog {
    public DialogActualizarCliente(Window owner, ControladorUsuario ctrl) {
        super(owner, "Actualizar cliente", ModalityType.APPLICATION_MODAL);
        setSize(420, 260);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfCodigo = new JTextField(10); JButton btnBuscar = new JButton("Buscar");
        top.add(new JLabel("Codigo:")); top.add(tfCodigo); top.add(btnBuscar);
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6,2,6,6));
        JTextField tfNombre = new JTextField(); JComboBox<String> cbGenero = new JComboBox<>(new String[]{"M","F"});
        JTextField tfCumple = new JTextField(); JPasswordField pf = new JPasswordField();
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Genero:")); form.add(cbGenero);
        form.add(new JLabel("Cumpleaños (dd/MM/yyyy):")); form.add(tfCumple);
        form.add(new JLabel("Contraseña:")); form.add(pf);
        add(form, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER)); JButton btnAct = new JButton("Actualizar"); bot.add(btnAct); add(bot, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> {
            String cod = tfCodigo.getText().trim();
            modelo.Usuario u = ctrl.buscarPorCodigo(cod);
            if (u == null) { JOptionPane.showMessageDialog(this, "No encontrado"); return; }
            tfNombre.setText(u.getNombre());
            cbGenero.setSelectedItem(u.getGenero());
            if (u instanceof modelo.Cliente) {
                modelo.Cliente c = (modelo.Cliente) u;
                tfCumple.setText(c.getCumpleanosFormato());
            }
        });

        btnAct.addActionListener(e -> {
            String codigo = tfCodigo.getText().trim(); String nombre = tfNombre.getText().trim(); String pass = new String(pf.getPassword());
            String genero = (String) cbGenero.getSelectedItem();
            if (codigo.isEmpty()||nombre.isEmpty()) { JOptionPane.showMessageDialog(this, "Codigo y nombre son obligatorios"); return; }
            boolean ok = ctrl.actualizarUsuario(codigo, nombre, pass);
            modelo.Usuario u = ctrl.buscarPorCodigo(codigo);
            if (u != null) u.setGenero(genero);
            if (u instanceof modelo.Cliente) {
                modelo.Cliente c = (modelo.Cliente) u;
                try { java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"); c.setCumpleanos(java.time.LocalDate.parse(tfCumple.getText().trim(), f)); } catch (Exception ignored) {}
            }
            if (!ok) JOptionPane.showMessageDialog(this, "No se actualizo"); else dispose();
        });
    }
}
