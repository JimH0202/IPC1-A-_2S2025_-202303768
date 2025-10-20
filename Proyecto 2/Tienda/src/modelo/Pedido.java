package modelo;

import java.io.Serializable;

public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String fechaHora;
    private String codigoCliente;
    private double total;
    private boolean confirmado;
    private String vendedorConfirmador;
    private Linea[] lineas; // array dinámico gestionado manualmente
    private int cantidadLineas;

    public Pedido(String codigo, String fechaHora, String codigoCliente) {
        this.codigo = codigo;
        this.fechaHora = fechaHora;
        this.codigoCliente = codigoCliente;
        this.total = 0.0;
        this.confirmado = false;
        this.lineas = new Linea[10];
        this.cantidadLineas = 0;
    }

    public String getCodigo() { return codigo; }
    public String getFechaHora() { return fechaHora; }
    public String getCodigoCliente() { return codigoCliente; }
    public double getTotal() { return total; }
    public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }
    public String getVendedorConfirmador() { return vendedorConfirmador; }
    public void setVendedorConfirmador(String vendedorConfirmador) { this.vendedorConfirmador = vendedorConfirmador; }
    public void setTotal(double total) { this.total = total; }

    public boolean agregarLinea(String codigoProducto, int cantidad, double precioUnitario) {
        if (cantidad <= 0 || precioUnitario < 0) return false;
        if (cantidadLineas >= lineas.length) {
            Linea[] n = new Linea[lineas.length * 2];
            System.arraycopy(lineas, 0, n, 0, lineas.length);
            lineas = n;
        }
        lineas[cantidadLineas++] = new Linea(codigoProducto, cantidad, precioUnitario);
        return true;
    }

    public Linea[] getLineas() {
        Linea[] res = new Linea[cantidadLineas];
        System.arraycopy(lineas, 0, res, 0, cantidadLineas);
        return res;
    }

    public static class Linea implements Serializable {
        private static final long serialVersionUID = 1L;
        private String codigoProducto;
        private int cantidad;
        private double precioUnitario;

        public Linea(String codigoProducto, int cantidad, double precioUnitario) {
            this.codigoProducto = codigoProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public String getCodigoProducto() { return codigoProducto; }
        public int getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }

        @Override
        public String toString() {
            return String.format("%s x%d @ %.2f", codigoProducto, cantidad, precioUnitario);
        }
    }

    @Override
    public String toString() {
        return String.format("%s - Cliente:%s - Total:%.2f - Confirmado:%s", codigo, codigoCliente, total, confirmado);
    }
}