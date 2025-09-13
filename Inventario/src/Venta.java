import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Venta {
    private String codigoProducto;
    private int cantidadVendida;
    private double total;
    public void setTotal(double total) {
        this.total = total;
    }

    private String fechaHora;

    // Constructor
    public Venta(String codigoProducto, int cantidadVendida, double total, String fechaHora) {
        this.codigoProducto = codigoProducto;
        this.cantidadVendida = cantidadVendida;
        this.total = total;
        this.fechaHora = fechaHora;
    }

    // Método para registrar una venta
    public static void Registrarventa(Scanner teclado) {
        System.out.print("Ingrese código del producto: ");
        String codigo = teclado.nextLine();

        // Buscar el producto en el inventario
        Producto productoEncontrado = null;
        for (int i = 0; i < Producto.getContador(); i++) {
            Producto p = Producto.getInventario()[i];
            if (p.getCodigo().equals(codigo)) {
                productoEncontrado = p;
                break;
            }
        }

        if (productoEncontrado == null) {
            System.out.println("Error: Producto no encontrado.");
            return;
        }

        System.out.print("Ingrese cantidad a vender: ");
        int cantidad = teclado.nextInt();
        teclado.nextLine(); // limpiar buffer

        if (cantidad <= 0) {
            System.out.println("Error: La cantidad debe ser mayor que 0.");
            return;
        }

        if (cantidad > productoEncontrado.getCantidad()) {
            System.out.println("Error: Stock insuficiente. Disponible: " + productoEncontrado.getCantidad());
            return;
        }

        // Calcular total y actualizar stock
        double total = cantidad * productoEncontrado.getPrecio();
        productoEncontrado.setCantidad(productoEncontrado.getCantidad() - cantidad);

        // Obtener fecha y hora actual
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaHora = LocalDateTime.now().format(formato);

        // Guardar en archivo ventas.txt
        try {
            File archivo = new File("ventas.txt");
            boolean nuevoArchivo = !archivo.exists() || archivo.length() == 0;

            try (FileWriter writer = new FileWriter(archivo, true)) {
                if (nuevoArchivo) {
                    writer.write(String.format("%-12s %-15s %-15s %-20s%n",
                            "Código", "Cantidad", "Total(Q)", "Fecha y Hora"));
                    writer.write("----------------------------------------------------------------\n");
                }

                writer.write(String.format("%-12s %-15d %-15.2f %-20s%n",
                        codigo, cantidad, total, fechaHora));
            }

            System.out.println("Venta registrada exitosamente. Total: Q" + total);

        } catch (IOException e) {
            System.out.println("Error al guardar la venta en el archivo.");
        }
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public int getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public double getTotal() {
        return total;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
