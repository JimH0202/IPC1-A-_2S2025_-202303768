package vista;

import controlador.ControladorUsuario;
import java.awt.*;
import javax.swing.*;

public class DialogEliminarVendedor extends JDialog {
    public DialogEliminarVendedor(Window owner, ControladorUsuario ctrl) {
        super(owner, "Eliminar vendedor", ModalityType.APPLICATION_MODAL);
        setSize(360, 200);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField tf = new JTextField(12); form.add(new JLabel("Codigo:")); form.add(tf);
        add(form, BorderLayout.CENTER);
        JPanel bot = new JPanel(); JButton btn = new JButton("Eliminar"); bot.add(btn); add(bot, BorderLayout.SOUTH);
        btn.addActionListener(e -> {
            String codigo = tf.getText().trim(); if (codigo.isEmpty()) { JOptionPane.showMessageDialog(this, "Codigo requerido"); return; }
            boolean ok = ctrl.eliminarUsuario(codigo);
            if (!ok) JOptionPane.showMessageDialog(this, "No encontrado o no eliminado"); else dispose();
        });
    }
}
