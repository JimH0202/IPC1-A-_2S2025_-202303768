package app;

import controlador.Controladores;
import javax.swing.*;
import vista.Login;

/**
 * Punto de entrada de la aplicación.
 * Crea los controladores centrales, configura look and feel y abre la ventana de login.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Look and feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) { }

        // Inicializar controladores compartidos
        final Controladores controladores = new Controladores();

        // Crear y mostrar login en el hilo de EDT
        SwingUtilities.invokeLater(() -> {
            // Pasar el contenedor de controladores para que la vista Login pueda
            // crear la ventana principal (VentanaPrincipal) con acceso a todos los controladores.
            Login login = new Login(controladores);
            login.setVisible(true);
        });
    }
}