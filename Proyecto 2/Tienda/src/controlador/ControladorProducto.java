package controlador;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import modelo.Producto;
import modelo.ProductoAlimento;
import modelo.ProductoGeneral;
import modelo.ProductoTecnologia;

/**
 * Controlador para gestión de productos y stock/ precio asociados (stock y precios se mantienen en ControladorStock).
 * Persistencia en productos.ser
 * CSV formato: codigo,nombre,categoria,atributo_unico
 */
public class ControladorProducto {
    private Producto[] productos;
    private int cantidad;
    private final File persistFile = new File("productos.ser");
    private final ControladorBitacora bitacora;

    public ControladorProducto(ControladorBitacora bitacora) {
        this.bitacora = bitacora;
        this.productos = new Producto[100];
        this.cantidad = 0;
        cargarPersistencia();
    }

    public boolean crearProducto(Producto p) {
        if (buscarProducto(p.getCodigo()) != null) return false;
        if (cantidad >= productos.length) productos = redimensionar(productos);
        productos[cantidad++] = p;
        guardarPersistencia();
        bitacora.registrar("ADMIN", "admin", "CREAR_PRODUCTO", "EXITOSA", p.getCodigo());
        return true;
    }

    public boolean actualizarProducto(String codigo, String nuevoNombre, String nuevoAtributo) {
        Producto p = buscarProducto(codigo);
        if (p == null) return false;
        p.setNombre(nuevoNombre);
        // actualizar atributo según tipo
        if (p instanceof ProductoTecnologia) {
            try {
                int meses = Integer.parseInt(nuevoAtributo);
                ((ProductoTecnologia) p).setMesesGarantia(meses);
            } catch (NumberFormatException ignored) { }
        } else if (p instanceof ProductoAlimento) {
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fecha = LocalDate.parse(nuevoAtributo, f);
                ((ProductoAlimento) p).setFechaCaducidad(fecha);
            } catch (Exception ignored) { }
        } else if (p instanceof ProductoGeneral) {
            ((ProductoGeneral) p).setMaterial(nuevoAtributo);
        }
        guardarPersistencia();
        bitacora.registrar("ADMIN", "admin", "ACTUALIZAR_PRODUCTO", "EXITOSA", codigo);
        return true;
    }

    /**
     * Permite establecer el precio asociado a un producto (delegable al ControladorStock si se necesita).
     */
    public boolean setPrecioProducto(String codigo, double precio) {
        Producto p = buscarProducto(codigo);
        if (p == null) return false;
        p.setPrecio(precio);
        guardarPersistencia();
        bitacora.registrar("ADMIN", "admin", "ACTUALIZAR_PRECIO", "EXITOSA", codigo + "->" + precio);
        return true;
    }

    public boolean eliminarProducto(String codigo) {
        int idx = indicePorCodigo(codigo);
        if (idx < 0) return false;
        Producto p = productos[idx];
        for (int i = idx; i < cantidad - 1; i++) productos[i] = productos[i + 1];
        productos[--cantidad] = null;
        guardarPersistencia();
        bitacora.registrar("ADMIN", "admin", "ELIMINAR_PRODUCTO", "EXITOSA", codigo);
        return true;
    }

    public Producto buscarProducto(String codigo) {
        for (int i = 0; i < cantidad; i++) if (productos[i].getCodigo().equalsIgnoreCase(codigo)) return productos[i];
        return null;
    }

    public Producto[] listarProductos() {
        Producto[] res = new Producto[cantidad];
        System.arraycopy(productos, 0, res, 0, cantidad);
        return res;
    }

    public Producto[] listarProductosDisponibles() {
        // devuelve mismos productos; stock manejado por StockController
        return listarProductos();
    }

    public util.CargaCSVResult cargarDesdeCSV(File file) {
        util.CargaCSVResult res = new util.CargaCSVResult();
        // Formato: codigo,nombre,categoria,atributo_unico
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line; int lineaNum = 0;
            while ((line = br.readLine()) != null) {
                lineaNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                res.procesadas++;
                String[] parts = line.split(",");
                if (parts.length < 4) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": formato invalido"); continue; }
                String codigo = parts[0].trim();
                String nombre = parts[1].trim();
                String categoria = parts[2].trim();
                String atributo = parts[3].trim();
                if (codigo.isEmpty() || nombre.isEmpty()) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": codigo/nombre vacio"); continue; }
                if (buscarProducto(codigo) != null) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": codigo ya existe: " + codigo); continue; }
                Producto p = null;
                if (categoria.equalsIgnoreCase("Tecnologia")) {
                    int meses = 0;
                    try { meses = Integer.parseInt(atributo); } catch (NumberFormatException ignored) { res.errores.add("Linea " + lineaNum + ": atributo meses invalido, se puso 0"); }
                    p = new ProductoTecnologia(codigo, nombre, meses);
                } else if (categoria.equalsIgnoreCase("Alimento")) {
                    try { p = new ProductoAlimento(codigo, nombre, LocalDate.parse(atributo, f)); } catch (Exception ex) { p = new ProductoAlimento(codigo, nombre, LocalDate.now()); res.errores.add("Linea " + lineaNum + ": fecha invalida, se uso hoy"); }
                } else {
                    p = new ProductoGeneral(codigo, nombre, atributo);
                }
                crearProducto(p);
                res.aceptadas++;
            }
            String resumen = String.format("%s -> %s", file.getName(), res.resumen());
            bitacora.registrar("ADMIN", "admin", "CARGA_CSV_PRODUCTOS", "EXITOSA", resumen);
        } catch (Exception e) {
            res.rechazadas++;
            res.errores.add("Error lectura: " + e.getMessage());
            bitacora.registrar("ADMIN", "admin", "CARGA_CSV_PRODUCTOS", "FALLIDA", file.getName() + " : " + e.getMessage());
        }
        return res;
    }

    /* Persistencia */
    private void guardarPersistencia() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(persistFile))) {
            Producto[] copia = new Producto[cantidad];
            System.arraycopy(productos, 0, copia, 0, cantidad);
            oos.writeObject(copia);
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "PERSISTENCIA_PRODUCTOS", "FALLIDA", e.getMessage());
        }
    }

    private void cargarPersistencia() {
        if (!persistFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(persistFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Producto[]) {
                Producto[] arr = (Producto[]) obj;
                this.productos = new Producto[Math.max(100, arr.length)];
                System.arraycopy(arr, 0, this.productos, 0, arr.length);
                this.cantidad = arr.length;
            }
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "CARGA_PERSISTENCIA_PRODUCTOS", "FALLIDA", e.getMessage());
        }
    }

    private Producto[] redimensionar(Producto[] arr) {
        Producto[] n = new Producto[arr.length * 2];
        System.arraycopy(arr, 0, n, 0, arr.length);
        return n;
    }

    private int indicePorCodigo(String codigo) {
        for (int i = 0; i < cantidad; i++) if (productos[i].getCodigo().equalsIgnoreCase(codigo)) return i;
        return -1;
    }
}   