package vista;

import controlador.ControladorUsuario;
import modelo.Vendedor;

import javax.swing.*;
import java.awt.*;

public class DialogCrearVendedor extends JDialog {
    public DialogCrearVendedor(Window owner, ControladorUsuario ctrl) {
        super(owner, "Crear vendedor", ModalityType.APPLICATION_MODAL);
        setSize(380, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(8,2,6,6));
        JTextField tfCodigo = new JTextField();
        JTextField tfNombre = new JTextField();
        JComboBox<String> cbGenero = new JComboBox<>(new String[]{"M","F"});
        JPasswordField pf = new JPasswordField();
        form.add(new JLabel("Codigo:")); form.add(tfCodigo);
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Genero:")); form.add(cbGenero);
        form.add(new JLabel("Contraseña:")); form.add(pf);
        add(form, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnCrear = new JButton("Crear"); bot.add(btnCrear); add(bot, BorderLayout.SOUTH);
        btnCrear.addActionListener(e -> {
            String c = tfCodigo.getText().trim(); String n = tfNombre.getText().trim(); String g = (String)cbGenero.getSelectedItem(); String pw = new String(pf.getPassword());
            if (c.isEmpty()||n.isEmpty()||pw.isEmpty()) { JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios"); return; }
            Vendedor v = new Vendedor(c,n,g,pw);
            boolean ok = ctrl.agregarUsuario(v);
            if (!ok) JOptionPane.showMessageDialog(this, "Código ya existe"); else dispose();
        });
    }
}
