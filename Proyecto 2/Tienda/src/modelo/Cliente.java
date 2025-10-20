package modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Cliente extends Usuario {
    private static final long serialVersionUID = 1L;

    private LocalDate cumpleanos;

    public Cliente(String codigo, String nombre, String genero, LocalDate cumpleanos, String contrasena) {
        super(codigo, nombre, genero, contrasena);
        this.cumpleanos = cumpleanos;
    }

    public LocalDate getCumpleanos() { return cumpleanos; }
    public void setCumpleanos(LocalDate cumpleanos) { this.cumpleanos = cumpleanos; }

    public void setGenero(String genero) { this.genero = genero; }

    @Override
    public String getRol() {
        return "CLIENTE";
    }

    public String getCumpleanosFormato() {
        if (cumpleanos == null) return "";
        return cumpleanos.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public String toString() {
        return String.format("%s - Nac:%s", super.toString(), getCumpleanosFormato());
    }
}