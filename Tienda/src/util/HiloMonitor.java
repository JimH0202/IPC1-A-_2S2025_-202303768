package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hilo de monitor simple que ejecuta un Runnable periódico y notifica a un listener.
 * Se usa para procesos visuales demostrables (monitor de sesiones, pedidos pendientes, estadísticas).
 *
 * Uso:
 *   HiloMonitor m = new HiloMonitor("MonitorSesiones", 10_000, () -> { return "Usuarios Activos: 3 - " + timestamp; });
 *   m.setListener(s -> SwingUtilities.invokeLater(() -> textPane.append(s + "\n")));
 *   m.start();
 *   m.requestStop();
 */
public class HiloMonitor extends Thread {
    private volatile boolean running = true;
    private final long periodoMs;
    private final MonitorCallback callback;
    private MonitorListener listener;
    private final String nombre;
    private final DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public HiloMonitor(String nombre, long periodoMs, MonitorCallback callback) {
        super(nombre);
        this.nombre = nombre;
        this.periodoMs = periodoMs;
        this.callback = callback;
        setDaemon(true);
    }

    public void setListener(MonitorListener listener) {
        this.listener = listener;
    }

    public void requestStop() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                String resultado = callback.onTick();
                String linea = String.format("[%s] %s | %s", LocalDateTime.now().format(f), nombre, resultado);
                if (listener != null) listener.onOutput(linea);
                Thread.sleep(periodoMs);
            } catch (InterruptedException ex) {
                // permitir terminar al recibir interrupt
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                if (listener != null) listener.onOutput("ERROR HiloMonitor " + nombre + ": " + ex.getMessage());
            }
        }
        if (listener != null) listener.onOutput("HiloMonitor " + nombre + " detenido");
    }

    public interface MonitorCallback {
        String onTick();
    }

    public interface MonitorListener {
        void onOutput(String texto);
    }
}