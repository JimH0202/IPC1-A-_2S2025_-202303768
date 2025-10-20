package util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Dispatcher global para monitores: imprime en consola, mantiene un buffer de líneas
 * y puede delegar a un appender UI. Cuando se registra un appender UI nuevo, se
 * repite el historial en el appender para que la UI muestre las líneas previas.
 */
public class GlobalMonitorDispatcher implements HiloMonitor.MonitorListener {
    private static final GlobalMonitorDispatcher INSTANCE = new GlobalMonitorDispatcher();
    private final AtomicReference<Consumer<String>> uiAppender = new AtomicReference<>(null);
    // buffer circular con capacidad configurable
    private final Deque<String> buffer = new ArrayDeque<>();
    private final int capacity = 2000; // almacenar hasta 2000 líneas por defecto

    private GlobalMonitorDispatcher() {}

    public static GlobalMonitorDispatcher getInstance() { return INSTANCE; }

    /**
     * Registrar o limpiar el appender UI. Si se registra uno nuevo, reproducir
     * el buffer histórico al appender inmediatamente.
     */
    public void setUIAppender(Consumer<String> appender) {
        uiAppender.set(appender);
        if (appender != null) {
            // reproducir buffer (copia para evitar concurrencia)
            synchronized (buffer) {
                for (String s : buffer) {
                    try { appender.accept(s); } catch (Exception ignored) {}
                }
            }
        }
    }

    @Override
    public void onOutput(String texto) {
        // Siempre imprimir en consola
        System.out.println(texto);
        // almacenar en buffer
        synchronized (buffer) {
            buffer.addLast(texto);
            if (buffer.size() > capacity) buffer.removeFirst();
        }
        // Enviar a UI si existe
        Consumer<String> c = uiAppender.get();
        if (c != null) {
            try { c.accept(texto); } catch (Exception ignored) {}
        }
    }

    /**
     * Recuperar snapshot del buffer actual (para depuración o uso externo).
     */
    public String[] getBufferSnapshot() {
        synchronized (buffer) {
            return buffer.toArray(new String[0]);
        }
    }
}
