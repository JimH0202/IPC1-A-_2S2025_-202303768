package vista;

import controlador.ControladorUsuario;
import controlador.Controladores;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import modelo.Usuario;

/**
 * Ventana de login.
 * Ahora recibe el contenedor `Controladores` para poder instanciar la VentanaPrincipal
 * con acceso a todos los controladores.
 */
public class Login extends JFrame {
    private final Controladores controladores;
    private final JTextField tfCodigo;
    private final JPasswordField pfContrasena;
    private final JButton btnLogin;
    private final JLabel lbStatus;
    public Login(Controladores controladores) {
        this.controladores = controladores;
        setTitle("Sancarlista Shop - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 220);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        JPanel panelCenter = new JPanel(new GridLayout(4, 1, 6, 6));
        panelCenter.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tfCodigo = new JTextField();
        pfContrasena = new JPasswordField();

        panelCenter.add(new JLabel("Código:"));
        panelCenter.add(tfCodigo);
        panelCenter.add(new JLabel("Contraseña:"));
        panelCenter.add(pfContrasena);

        add(panelCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new BorderLayout(6, 6));
        btnLogin = new JButton("Iniciar sesión");
        lbStatus = new JLabel(" ");
        lbStatus.setForeground(Color.RED);
        panelBottom.add(lbStatus, BorderLayout.CENTER);
        panelBottom.add(btnLogin, BorderLayout.EAST);
        panelBottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        add(panelBottom, BorderLayout.SOUTH);

        btnLogin.addActionListener(new LoginAction());

        getRootPane().setDefaultButton(btnLogin);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String codigo = tfCodigo.getText().trim();
            String contrasena = new String(pfContrasena.getPassword()).trim();
            if (codigo.isEmpty() || contrasena.isEmpty()) {
                lbStatus.setText("Ingrese código y contraseña");
                return;
            }
            try {
                    // usar el controlador de usuarios desde el contenedor
                    ControladorUsuario controlador = controladores.getUsuarioController();
                    Usuario user = controlador.autenticar(codigo, contrasena);
                if (user == null) {
                    lbStatus.setText("Credenciales incorrectas");
                    return;
                }
                lbStatus.setText("Inicio correcto");
                dispose();
                VentanaPrincipal vp = new VentanaPrincipal(user, controladores);
                vp.setVisible(true);
                // arrancar monitores cuando la ventana ya esté visible
                vp.startMonitors(controladores);
            } catch (Exception ex) {
                lbStatus.setText("Error al autenticar");
                ex.printStackTrace();
            }
        }
    }
}