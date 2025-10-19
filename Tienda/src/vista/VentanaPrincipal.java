package vista;

import controlador.Controladores;
import java.awt.*;
import javax.swing.*;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Usuario;
import modelo.Vendedor;
import util.HiloMonitor;

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
            dispose();
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

        // Exponer método para que HiloMonitor pueda publicar en esta área
        this.setMonitorAppender(new MonitorAppender() {
            @Override
            public void append(String text) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    estadoArea.append(text + "\n");
                    estadoArea.setCaretPosition(estadoArea.getDocument().getLength());
                });
            }
        });

        // Los monitores se inician explícitamente por startMonitors() para mayor control
    }

    private HiloMonitor sesionesMonitor;
    private HiloMonitor pedidosMonitor;

    public void startMonitors(Controladores controladores) {
        if (sesionesMonitor != null) return;
        sesionesMonitor = new HiloMonitor("Sesiones", 5000, () -> {
            int n = controladores.getUsuarioController().listarTodosUsuarios().length;
            return "Usuarios registrados: " + n;
        });
        sesionesMonitor.setListener(text -> { if (this.monitorAppender != null) this.monitorAppender.append(text);
            // Si quieres que los monitores impriman en consola, descomenta la siguiente línea:
            // System.out.println(text);
        });
        sesionesMonitor.start();

        pedidosMonitor = new HiloMonitor("PedidosPendientes", 7000, () -> {
            int k = controladores.getPedidoController().listarPedidosPendientes().length;
            return "Pedidos pendientes: " + k;
        });
        pedidosMonitor.setListener(text -> { if (this.monitorAppender != null) this.monitorAppender.append(text);
            // Si quieres que los monitores impriman en consola, descomenta la siguiente línea:
            // System.out.println(text);
        });
        pedidosMonitor.start();

        // Detener monitores al cerrar la ventana
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                stopMonitors();
            }
        });
    }

    public void stopMonitors() {
        if (sesionesMonitor != null) { sesionesMonitor.requestStop(); sesionesMonitor = null; }
        if (pedidosMonitor != null) { pedidosMonitor.requestStop(); pedidosMonitor = null; }
    }

    private MonitorAppender monitorAppender;

    public interface MonitorAppender { void append(String texto); }

    public void setMonitorAppender(MonitorAppender appender) { this.monitorAppender = appender; }

    public MonitorAppender getMonitorAppender() { return this.monitorAppender; }
}