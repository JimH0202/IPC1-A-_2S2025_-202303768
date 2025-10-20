package modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ProductoAlimento extends Producto {
    private static final long serialVersionUID = 1L;

    private LocalDate fechaCaducidad;

    public ProductoAlimento(String codigo, String nombre, LocalDate fechaCaducidad) {
        super(codigo, nombre, "Alimento");
        this.fechaCaducidad = fechaCaducidad;
    }

    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    @Override
    public String getDetalle() {
        if (fechaCaducidad == null) return "Fecha de caducidad desconocida";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaCaducidad);
        return "Fecha de caducidad: " + fechaCaducidad.format(f) + " | Días restantes: " + dias;
    }
}