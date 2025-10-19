package controlador;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;
import modelo.Pedido;

/**
 * Controlador sencillo de pedidos.
 * - Mantiene cola de pedidos pendientes en memoria y persistencia en pedidos.ser
 * - Permite crear pedido desde un DefaultTableModel (carrito)
 */
public class ControladorPedido {
    private Pedido[] pedidos;
    private int cantidad;
    private final File persistFile = new File("pedidos.ser");
    private final ControladorProducto productoController;
    private final ControladorStock stockController;
    private final ControladorUsuario usuarioController;
    private final ControladorBitacora bitacora;

    public ControladorPedido(ControladorProducto productoController,
                             ControladorStock stockController,
                             ControladorUsuario usuarioController,
                             ControladorBitacora bitacora) {
        this.productoController = productoController;
        this.stockController = stockController;
        this.usuarioController = usuarioController;
        this.bitacora = bitacora;
        this.pedidos = new Pedido[200];
        this.cantidad = 0;
        cargarPersistencia();
    }

    public Pedido crearPedidoDesdeCarrito(String codigoCliente, DefaultTableModel modeloCarrito) {
        // Generar código simple
        String codigo = generarCodigoPedido();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaHora = LocalDateTime.now().format(f);
        Pedido pedido = new Pedido(codigo, fechaHora, codigoCliente);
        double total = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            String codigoProducto = (String) modeloCarrito.getValueAt(i, 0);
            int cantidad = (int) modeloCarrito.getValueAt(i, 2);
            double precio = (double) modeloCarrito.getValueAt(i, 3);
            pedido.agregarLinea(codigoProducto, cantidad, precio);
            total += cantidad * precio;
        }
        pedido.setTotal(total);
        if (cantidad >= pedidos.length) pedidos = redimensionar(pedidos);
        pedidos[cantidad++] = pedido;
        guardarPersistencia();
        bitacora.registrar("CLIENTE", codigoCliente, "REALIZAR_PEDIDO", "EXITOSA", codigo);
        return pedido;
    }

    public Pedido[] listarPedidosPendientes() {
        // devuelve todos los pedidos pendientes (no confirmados)
        Pedido[] temp = new Pedido[cantidad];
        int k = 0;
        for (int i = 0; i < cantidad; i++) {
            if (!pedidos[i].isConfirmado()) temp[k++] = pedidos[i];
        }
        Pedido[] res = new Pedido[k];
        System.arraycopy(temp, 0, res, 0, k);
        return res;
    }

    public Pedido[] listarTodos() {
        Pedido[] res = new Pedido[cantidad];
        System.arraycopy(pedidos, 0, res, 0, cantidad);
        return res;
    }

    public boolean confirmarPedido(String codigoPedido, String codigoVendedor) {
        for (int i = 0; i < cantidad; i++) {
            if (pedidos[i].getCodigo().equalsIgnoreCase(codigoPedido)) {
                Pedido p = pedidos[i];
                // verificar stock para cada línea
                for (Pedido.Linea l : p.getLineas()) {
                    int stock = stockController.getStock(l.getCodigoProducto());
                    if (stock < l.getCantidad()) {
                        bitacora.registrar("VENDEDOR", codigoVendedor, "CONFIRMAR_PEDIDO", "FALLIDA", "Stock insuficiente " + l.getCodigoProducto());
                        return false;
                    }
                }
                // descontar stock
                for (Pedido.Linea l : p.getLineas()) {
                    stockController.agregarStock(l.getCodigoProducto(), -l.getCantidad(), codigoVendedor);
                }
                p.setConfirmado(true);
                p.setVendedorConfirmador(codigoVendedor);
                guardarPersistencia();
                bitacora.registrar("VENDEDOR", codigoVendedor, "CONFIRMAR_PEDIDO", "EXITOSA", codigoPedido);
                return true;
            }
        }
        return false;
    }

    private String generarCodigoPedido() {
        int seq = cantidad + 1;
        return String.format("PE-%03d", seq);
    }

    /* Persistencia */
    private void guardarPersistencia() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(persistFile))) {
            Pedido[] copia = new Pedido[cantidad];
            System.arraycopy(pedidos, 0, copia, 0, cantidad);
            oos.writeObject(copia);
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "PERSISTENCIA_PEDIDOS", "FALLIDA", e.getMessage());
        }
    }

    private void cargarPersistencia() {
        if (!persistFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(persistFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Pedido[]) {
                Pedido[] arr = (Pedido[]) obj;
                this.pedidos = new Pedido[Math.max(200, arr.length)];
                System.arraycopy(arr, 0, this.pedidos, 0, arr.length);
                this.cantidad = arr.length;
            }
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "CARGA_PERSISTENCIA_PEDIDOS", "FALLIDA", e.getMessage());
        }
    }

    private Pedido[] redimensionar(Pedido[] arr) {
        Pedido[] n = new Pedido[arr.length * 2];
        System.arraycopy(arr, 0, n, 0, arr.length);
        return n;
    }
}