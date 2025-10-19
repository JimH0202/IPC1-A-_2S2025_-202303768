package vista;

import controlador.ControladorUsuario;
import modelo.Cliente;
import modelo.Vendedor;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DialogCrearUsuario extends JDialog {
    public DialogCrearUsuario(Window owner, ControladorUsuario controlador, String tipo) {
        super(owner, "Crear " + tipo, ModalityType.APPLICATION_MODAL);
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(6,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JTextField tfCodigo = new JTextField();
        JTextField tfNombre = new JTextField();
        JTextField tfGenero = new JTextField();
        JTextField tfExtra = new JTextField(); // password or birthday

        form.add(new JLabel("Código:")); form.add(tfCodigo);
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Género:")); form.add(tfGenero);
        if (tipo.equalsIgnoreCase("Vendedor")) form.add(new JLabel("Contraseña:")); else form.add(new JLabel("Cumpleaños (dd/MM/yyyy):"));
        form.add(tfExtra);

        add(form, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCrear = new JButton("Crear");
        JButton btnCerrar = new JButton("Cancelar");
        bot.add(btnCrear); bot.add(btnCerrar);
        add(bot, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> dispose());
        btnCrear.addActionListener(e -> {
            String codigo = tfCodigo.getText().trim();
            String nombre = tfNombre.getText().trim();
            String genero = tfGenero.getText().trim();
            if (codigo.isEmpty() || nombre.isEmpty()) { JOptionPane.showMessageDialog(this, "Código y nombre obligatorios"); return; }
            try {
                if (tipo.equalsIgnoreCase("Vendedor")) {
                    String pwd = tfExtra.getText().trim();
                    Vendedor v = new Vendedor(codigo, nombre, genero, pwd);
                    boolean ok = controlador.agregarUsuario(v);
                    if (!ok) JOptionPane.showMessageDialog(this, "Código ya existe"); else dispose();
                } else {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate fecha = LocalDate.now();
                    try { fecha = LocalDate.parse(tfExtra.getText().trim(), f); } catch (Exception ignored) {}
                    Cliente c = new Cliente(codigo, nombre, genero, fecha, "1234");
                    boolean ok = controlador.agregarUsuario(c);
                    if (!ok) JOptionPane.showMessageDialog(this, "Código ya existe"); else dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
