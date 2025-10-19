package modelo;

import java.io.Serializable;

public abstract class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String codigo;
    protected String nombre;
    protected String categoria;

    public Producto(String codigo, String nombre, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public abstract String getDetalle();

    @Override
    public String toString() {
        return String.format("%s - %s [%s]", codigo, nombre, categoria);
    }
}