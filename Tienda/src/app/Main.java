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

        // Crear monitores globales y arrancarlos inmediatamente
        util.GlobalMonitorDispatcher dispatcher = util.GlobalMonitorDispatcher.getInstance();

        util.HiloMonitor sesionesMonitor = new util.HiloMonitor("SesionesMonitor", 10_000, () -> {
            int n = controladores.getUsuarioController().listarTodosUsuarios().length;
            String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            return String.format("Usuarios Activos: %d - Última actividad: %s", n, ts);
        });
        sesionesMonitor.setListener(dispatcher);

        util.HiloMonitor pedidosMonitor = new util.HiloMonitor("PedidosPendientes", 8_000, () -> {
            int k = controladores.getPedidoController().listarPedidosPendientes().length;
            String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            return String.format("Pedidos Pendientes: %d - Procesando... %s", k, ts);
        });
        pedidosMonitor.setListener(dispatcher);

        util.HiloMonitor statsMonitor = new util.HiloMonitor("EstadisticasVivas", 15_000, () -> {
            int ventas = 0;
            for (modelo.Pedido p : controladores.getPedidoController().listarTodos()) if (p.isConfirmado()) ventas++;
            int productos = controladores.getProductoController().listarProductos().length;
            String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            return String.format("Ventas del día: %d | Productos registrados: %d | %s", ventas, productos, ts);
        });
        statsMonitor.setListener(dispatcher);

        // arrancar hilos
        sesionesMonitor.start(); pedidosMonitor.start(); statsMonitor.start();

        // asegurar que los monitores se detengan al terminar la JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { sesionesMonitor.requestStop(); } catch (Exception ignored) {}
            try { pedidosMonitor.requestStop(); } catch (Exception ignored) {}
            try { statsMonitor.requestStop(); } catch (Exception ignored) {}
        }));

        // Crear y mostrar login en el hilo de EDT
        SwingUtilities.invokeLater(() -> {
            // Pasar el contenedor de controladores para que la vista Login pueda
            // crear la ventana principal (VentanaPrincipal) con acceso a todos los controladores.
            Login login = new Login(controladores);
            login.setVisible(true);
        });
    }
}