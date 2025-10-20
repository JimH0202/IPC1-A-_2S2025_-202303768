package tools;

import controlador.ControladorBitacora;
import controlador.ControladorPedido;
import controlador.ControladorProducto;
import controlador.ControladorStock;
import controlador.ControladorUsuario;
import java.io.File;
import java.time.LocalDate;
import modelo.Cliente;
import modelo.Pedido;
import modelo.ProductoGeneral;
import modelo.Vendedor;
import util.CSVUtil;

public class E2ETestRunner {
    public static void main(String[] args) throws Exception {
        ControladorBitacora bit = new ControladorBitacora();
        ControladorProducto cp = new ControladorProducto(bit);
        ControladorStock cs = new ControladorStock(cp, bit);
        ControladorUsuario cu = new ControladorUsuario(bit);
        ControladorPedido pedidoCtrl = new ControladorPedido(cp, cs, cu, bit);

        System.out.println("--- E2E TEST START ---");

        // 1. Crear producto
        ProductoGeneral p = new ProductoGeneral("TP1", "TestProd", "Plastico");
        cp.crearProducto(p);
        cp.setPrecioProducto("TP1", 12.5);
        System.out.println("Producto creado: " + p.getCodigo());

        // 2. Crear vendedor y cliente
    Vendedor v = new Vendedor("V001", "Vendedor1", "M", "pass");
    cu.agregarUsuario(v);
    Cliente c = new Cliente("C001", "Cliente1", "F", LocalDate.now().minusYears(30), "pwd123");
    cu.agregarUsuario(c);
        System.out.println("Vendedor y cliente creados");

        // 3. Agregar stock
        cs.agregarStock("TP1", 100, "V001");
        System.out.println("Stock inicial: " + cs.getStock("TP1"));

        // 4. Crear pedido (simulado)
        javax.swing.table.DefaultTableModel carrito = new javax.swing.table.DefaultTableModel(new Object[][]{}, new Object[]{"Codigo","Nombre","Cantidad","Precio"});
        carrito.addRow(new Object[]{"TP1", "TestProd", 2, 12.5});
        Pedido pedido = pedidoCtrl.crearPedidoDesdeCarrito("C001", carrito);
        System.out.println("Pedido creado: " + pedido.getCodigo());

        // 5. Confirmar pedido
        boolean ok = pedidoCtrl.confirmarPedido(pedido.getCodigo(), "V001");
        System.out.println("Confirmar pedido result: " + ok);
        System.out.println("Stock despues: " + cs.getStock("TP1"));

        // 6. Mostrar historial file
        File f = new File("historial_ingresos.csv");
        System.out.println("Historial existe: " + f.exists());
        if (f.exists()) {
            java.util.List<String[]> rows = CSVUtil.readAll(f);
            System.out.println("Historial filas: " + rows.size());
            for (String[] r : rows) System.out.println(String.join(" | ", r));
        }

        System.out.println("--- E2E TEST END ---");
    }
}
