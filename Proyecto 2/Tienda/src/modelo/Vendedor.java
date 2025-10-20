package modelo;

public class Vendedor extends Usuario {
    private static final long serialVersionUID = 1L;

    private int ventasConfirmadas;

    public Vendedor(String codigo, String nombre, String genero, String contrasena) {
        super(codigo, nombre, genero, contrasena);
        this.ventasConfirmadas = 0;
    }

    public int getVentasConfirmadas() { return ventasConfirmadas; }
    public void incrementarVentasConfirmadas(int delta) { this.ventasConfirmadas += delta; }
    public void setVentasConfirmadas(int ventasConfirmadas) { this.ventasConfirmadas = ventasConfirmadas; }

    @Override
    public String getRol() {
        return "VENDEDOR";
    }

    @Override
    public String toString() {
        return String.format("%s - Confirmadas:%d", super.toString(), ventasConfirmadas);
    }
}