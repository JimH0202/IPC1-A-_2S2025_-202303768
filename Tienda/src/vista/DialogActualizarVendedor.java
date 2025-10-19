package vista;

import controlador.ControladorUsuario;
import java.awt.*;
import javax.swing.*;
import modelo.Vendedor;

public class DialogActualizarVendedor extends JDialog {
    public DialogActualizarVendedor(Window owner, ControladorUsuario ctrl) {
        super(owner, "Actualizar vendedor", ModalityType.APPLICATION_MODAL);
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfCodigo = new JTextField(10); JButton btnBuscar = new JButton("Buscar");
        top.add(new JLabel("Codigo:")); top.add(tfCodigo); top.add(btnBuscar);
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4,2,6,6));
        JTextField tfNombre = new JTextField(); JPasswordField pf = new JPasswordField();
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Contraseña:")); form.add(pf);
        add(form, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER)); JButton btnAct = new JButton("Actualizar"); bot.add(btnAct); add(bot, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> {
            Vendedor v = (Vendedor) ctrl.buscarPorCodigo(tfCodigo.getText().trim());
            if (v == null) { JOptionPane.showMessageDialog(this, "No encontrado"); return; }
            tfNombre.setText(v.getNombre());
        });

        btnAct.addActionListener(e -> {
            String codigo = tfCodigo.getText().trim(); String nombre = tfNombre.getText().trim(); String pass = new String(pf.getPassword());
            if (codigo.isEmpty()||nombre.isEmpty()) { JOptionPane.showMessageDialog(this, "Codigo y nombre son obligatorios"); return; }
            boolean ok = ctrl.actualizarUsuario(codigo, nombre, pass);
            if (!ok) JOptionPane.showMessageDialog(this, "No se actualizo"); else dispose();
        });
    }
}
