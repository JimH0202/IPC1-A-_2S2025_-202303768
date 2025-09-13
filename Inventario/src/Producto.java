import java.util.Scanner;

public class Producto {
    private static final int MAX_PRODUCTOS = 100; //maximo de productos a agregar
    private static Producto[] inventario = new Producto[MAX_PRODUCTOS];
    private static int contador = 0;

    public static void setInventario(Producto[] inventario) {
        Producto.inventario = inventario;
    }

    private String codigo;
    private String nombre;
    private String categoria;
    private double precio;
    private int cantidad;

    public Producto(String codigo, String nombre, String categoria, double precio, int cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    // === Métodos del sistema ===
    public static void Agregarproducto(Scanner teclado) {
        if (contador >= MAX_PRODUCTOS) {
            System.out.println("Inventario lleno. No se pueden agregar más productos.");
            return;
        }

        System.out.print("Ingrese código del producto: ");
        String codigo = teclado.nextLine();

        // Validar código único
        for (int i = 0; i < contador; i++) {
            if (inventario[i].codigo.equals(codigo)) {
                System.out.println("Error: Ya existe un producto con ese código.");
                return;
            }
        }

        System.out.print("Ingrese nombre del producto: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese categoría: ");
        String categoria = teclado.nextLine();

        System.out.print("Ingrese precio: ");
        double precio = teclado.nextDouble();

        System.out.print("Ingrese cantidad: ");
        int cantidad = teclado.nextInt();
        teclado.nextLine(); // limpiar buffer

        if (precio <= 0 || cantidad < 0) {
            System.out.println("Error: El precio y cantidad deben ser positivos.");
            return;
        }

        inventario[contador] = new Producto(codigo, nombre, categoria, precio, cantidad);
        contador++;

        System.out.println("Producto agregado correctamente.");
    }

    public static void Buscarproducto(Scanner teclado) {
        System.out.println("Buscar por: 1.Código  2.Nombre  3.Categoría");
        int opcion = teclado.nextInt();
        teclado.nextLine();

        System.out.print("Ingrese búsqueda: ");
        String criterio = teclado.nextLine();

        boolean encontrado = false;
        for (int i = 0; i < contador; i++) {
            Producto p = inventario[i];
            if ((opcion == 1 && p.codigo.equalsIgnoreCase(criterio)) ||
                (opcion == 2 && p.nombre.equalsIgnoreCase(criterio)) ||
                (opcion == 3 && p.categoria.equalsIgnoreCase(criterio))) {
                System.out.println("Código: " + p.codigo + " | Nombre: " + p.nombre +
                        " | Categoría: " + p.categoria + " | Precio: " + p.precio +
                        " | Cantidad: " + p.cantidad);
                encontrado = true;
            }
        }
        if (!encontrado) {
            System.out.println("No se encontraron productos.");
        }
    }

    public static void Eliminarproducto(Scanner teclado) {
        System.out.print("Ingrese código del producto a eliminar: ");
        String codigo = teclado.nextLine();

        for (int i = 0; i < contador; i++) {
            if (inventario[i].codigo.equals(codigo)) {
                for (int j = i; j < contador - 1; j++) {
                    inventario[j] = inventario[j + 1];
                }
                inventario[contador - 1] = null;
                contador--;
                System.out.println("Producto eliminado correctamente.");
                return;
            }
        }
        System.out.println("No se encontró el producto.");
    }

    // ==== Getters y Setters ====
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public static Producto[] getInventario() { return inventario; }
    public static int getContador() { return contador; }
    public void setCodigo(String codigo) {this.codigo = codigo;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setCategoria(String categoria) {this.categoria = categoria;}
    public void setPrecio(double precio) {this.precio = precio;}
}
