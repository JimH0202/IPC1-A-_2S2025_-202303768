package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entrada de bitácora mínima para mantener registro en memoria o para serializar si hace falta.
 * Las operaciones de escritura en archivo quedan a cargo de ControladorBitacora.
 */
public class Bitacora implements Serializable {
    private static final long serialVersionUID = 1L;

    private String timestamp;
    private String tipoUsuario;
    private String codigoUsuario;
    private String operacion;
    private String estado;
    private String descripcion;

    public Bitacora(String tipoUsuario, String codigoUsuario, String operacion, String estado, String descripcion) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        this.tipoUsuario = tipoUsuario;
        this.codigoUsuario = codigoUsuario;
        this.operacion = operacion;
        this.estado = estado;
        this.descripcion = descripcion;
    }

    public String getTimestamp() { return timestamp; }
    public String getTipoUsuario() { return tipoUsuario; }
    public String getCodigoUsuario() { return codigoUsuario; }
    public String getOperacion() { return operacion; }
    public String getEstado() { return estado; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() {
        return String.format("[%s] | %s | %s | %s | %s | %s",
                timestamp, tipoUsuario, codigoUsuario, operacion, estado, descripcion);
    }
}