package vista;

import controlador.ControladorUsuario;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import modelo.Cliente;
import modelo.Vendedor;

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
    JComboBox<String> cbGenero = new JComboBox<>(new String[]{"Masculino","Femenino"});
    JTextField tfFecha = new JTextField(); // cumpleaños
    JPasswordField pfPassword = new JPasswordField();
    JButton btnVerificar = new JButton("Verificar código");

        JPanel codigoRow = new JPanel(new BorderLayout()); codigoRow.add(tfCodigo, BorderLayout.CENTER); codigoRow.add(btnVerificar, BorderLayout.EAST);
        form.add(new JLabel("Código:")); form.add(codigoRow);
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Género:")); form.add(cbGenero);
        if (tipo.equalsIgnoreCase("Vendedor")) {
            form.add(new JLabel("Contraseña:")); form.add(pfPassword);
        } else {
            form.add(new JLabel("Cumpleaños (dd/MM/yyyy):")); form.add(tfFecha);
            form.add(new JLabel("Contraseña:")); form.add(pfPassword);
        }

        add(form, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCrear = new JButton("Crear");
        JButton btnCerrar = new JButton("Cancelar");
        bot.add(btnCrear); bot.add(btnCerrar);
        add(bot, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> dispose());
        btnVerificar.addActionListener(e -> {
            String codigo = tfCodigo.getText().trim();
            if (codigo.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese código"); return; }
            if (controlador.buscarPorCodigo(codigo) != null) JOptionPane.showMessageDialog(this, "Código ya existe"); else JOptionPane.showMessageDialog(this, "Código disponible");
        });
        btnCrear.addActionListener(e -> {
            String codigo = tfCodigo.getText().trim();
            String nombre = tfNombre.getText().trim();
            String genero = (String) cbGenero.getSelectedItem();
            String pwd = new String(pfPassword.getPassword()).trim();
            if (codigo.isEmpty() || nombre.isEmpty() || pwd.isEmpty()) { JOptionPane.showMessageDialog(this, "Código, nombre y contraseña obligatorios"); return; }
            try {
                if (tipo.equalsIgnoreCase("Vendedor")) {
                    Vendedor v = new Vendedor(codigo, nombre, genero, pwd);
                    boolean ok = controlador.agregarUsuario(v);
                    if (!ok) JOptionPane.showMessageDialog(this, "Código ya existe"); else dispose();
                } else {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate fecha = LocalDate.now();
                    try { fecha = LocalDate.parse(tfFecha.getText().trim(), f); } catch (Exception ignored) {}
                    Cliente c = new Cliente(codigo, nombre, genero, fecha, pwd);
                    boolean ok = controlador.agregarUsuario(c);
                    if (!ok) JOptionPane.showMessageDialog(this, "Código ya existe"); else dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
