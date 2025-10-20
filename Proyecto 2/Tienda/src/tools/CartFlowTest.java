package tools;

import controlador.*;
import modelo.*;

public class CartFlowTest {
    public static void main(String[] args) {
        // Setup minimal controllers
        ControladorBitacora b = new ControladorBitacora();
        ControladorProducto pc = new ControladorProducto(b);
        ControladorStock sc = new ControladorStock(pc, b);
        ControladorUsuario uc = new ControladorUsuario(b);
        ControladorPedido pp = new ControladorPedido(pc, sc, uc, b);

    // Create product and stock
    Producto p = new ProductoGeneral("T100","Prueba", "General");
        pc.crearProducto(p);
        sc.agregarStock("T100", 10, "test");

        // Simulate cart as DefaultTableModel
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(new Object[]{"Código","Nombre","Cantidad","Precio","Total","Opciones"},0);
        modelo.addRow(new Object[]{"T100","Prueba",2, 5.0, 10.0, "Opciones"});

        Pedido pedido = pp.crearPedidoDesdeCarrito("C_TEST", modelo);
        System.out.println("Pedido creado: " + (pedido != null ? pedido.getCodigo() : "NULL"));
    }
}
