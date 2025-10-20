package vista;

import controlador.Controladores;
import java.awt.*;
import javax.swing.*;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Usuario;
import modelo.Vendedor;

/**
 * Ventana principal que redirige según rol.
 * Requiere una clase contenedora Controladores con referencias a los controladores específicos.
 */
public class VentanaPrincipal extends JFrame {
    private final Usuario usuario;
    private final Controladores controladores;

    public VentanaPrincipal(Usuario usuario, Controladores controladores) {
        this.usuario = usuario;
        this.controladores = controladores;
        setTitle("Sancarlista Shop - " + usuario.getNombre() + " (" + usuario.getCodigo() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lbWelcome = new JLabel("Bienvenido: " + usuario.getNombre() + " - Rol: " + usuario.getRol());
        lbWelcome.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(lbWelcome, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        add(content, BorderLayout.CENTER);

        if (usuario instanceof Administrador) {
            MenuAdministrador menu = new MenuAdministrador(controladores);
            content.add(menu, BorderLayout.CENTER);
        } else if (usuario instanceof Vendedor) {
            MenuVendedor menu = new MenuVendedor(controladores, usuario);
            content.add(menu, BorderLayout.CENTER);
        } else if (usuario instanceof Cliente) {
            MenuCliente menu = new MenuCliente(controladores, usuario);
            content.add(menu, BorderLayout.CENTER);
        } else {
            content.add(new JLabel("Rol no soportado"), BorderLayout.CENTER);
        }

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Cerrar sesión");
        btnLogout.addActionListener(e -> {
            // registrar cierre de sesión en la bitácora
            try {
                if (controladores != null && controladores.getBitacoraController() != null) {
                    String desc = String.format("Usuario %s (%s) cerró sesión", usuario.getNombre(), usuario.getCodigo());
                    controladores.getBitacoraController().registrar(usuario.getRol(), usuario.getCodigo(), "LOGOUT", "EXITOSA", desc);
                }
            } catch (Exception ex) {
                // no interrumpir el cierre por un error de bitácora
            }

            dispose();
            // limpiar appender UI del dispatcher para que no intente escribir en esta ventana cerrada
            try { util.GlobalMonitorDispatcher.getInstance().setUIAppender(null); } catch (Exception ignored) {}
            Login login = new Login(controladores);
            login.setVisible(true);
        });
        right.add(btnLogout);
        bottom.add(right, BorderLayout.EAST);

        // Área de estado para mensajes del monitor
        JTextArea estadoArea = new JTextArea(4, 60);
        estadoArea.setEditable(false);
        JScrollPane sp = new JScrollPane(estadoArea);
        bottom.add(sp, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Exponer método para que el dispatcher global publique en esta área
        MonitorAppender app = new MonitorAppender() {
            @Override
            public void append(String text) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    estadoArea.append(text + "\n");
                    estadoArea.setCaretPosition(estadoArea.getDocument().getLength());
                });
            }
        };
        this.setMonitorAppender(app);
        // Registrar en dispatcher global
        util.GlobalMonitorDispatcher.getInstance().setUIAppender(s -> app.append(s));

        // Los monitores se inician explícitamente por startMonitors() para mayor control
    }

    // Los monitores son gestionados globalmente por Main (GlobalMonitorDispatcher).
    // Esta ventana sólo registra/unregistra su appender UI; no controla la vida de los hilos.

    private MonitorAppender monitorAppender;

    public interface MonitorAppender { void append(String texto); }

    public void setMonitorAppender(MonitorAppender appender) { this.monitorAppender = appender; }

    public MonitorAppender getMonitorAppender() { return this.monitorAppender; }
}