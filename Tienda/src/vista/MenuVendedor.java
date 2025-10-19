package vista;

import controlador.Controladores;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.Pedido;
import modelo.Producto;
import modelo.Usuario;

/**
 * Panel para Vendedor: gestión de stock, clientes y pedidos.
 * Usuario corresponde al vendedor logueado.
 */
public class MenuVendedor extends JPanel {
    private final Controladores controladores;
    private final Usuario vendedor;
    private final DefaultTableModel modeloStock;
    private final JTable tablaStock;
    private final DefaultTableModel modeloPedidos;
    private final JTable tablaPedidos;

    public MenuVendedor(Controladores controladores, Usuario vendedor) {
        this.controladores = controladores;
        this.vendedor = vendedor;
        setLayout(new BorderLayout(8,8));

        JTabbedPane tabs = new JTabbedPane();

        // Stock tab
        JPanel panelStock = new JPanel(new BorderLayout(6,6));
        modeloStock = new DefaultTableModel(new Object[]{"Código","Nombre","Categoría","Stock"},0){
            @Override public boolean isCellEditable(int row,int col){ return false; }
        };
        tablaStock = new JTable(modeloStock);
        panelStock.add(new JScrollPane(tablaStock), BorderLayout.CENTER);

        JPanel accionesStock = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregarStock = new JButton("Agregar stock");
        JButton btnCargarStockCSV = new JButton("Cargar stock CSV");
        JButton btnVerHistorial = new JButton("Ver historial");
        accionesStock.add(btnAgregarStock);
        accionesStock.add(btnCargarStockCSV);
        accionesStock.add(btnVerHistorial);
        panelStock.add(accionesStock, BorderLayout.NORTH);

        btnAgregarStock.addActionListener(e -> dialogAgregarStock());
        btnCargarStockCSV.addActionListener(e -> cargarStockCSV());
        btnVerHistorial.addActionListener(e -> mostrarHistorial());

        // Pedidos tab
        JPanel panelPedidos = new JPanel(new BorderLayout(6,6));
        modeloPedidos = new DefaultTableModel(new Object[]{"Código","Fecha","Cliente","Total","Opciones"},0){
            @Override public boolean isCellEditable(int row,int col){ return false; }
        };
        tablaPedidos = new JTable(modeloPedidos);
        panelPedidos.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        JPanel accionesPedidos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnConfirmar = new JButton("Confirmar pedido");
        JButton btnRefrescar = new JButton("Refrescar");
        accionesPedidos.add(btnConfirmar);
        accionesPedidos.add(btnRefrescar);
        panelPedidos.add(accionesPedidos, BorderLayout.NORTH);

        btnConfirmar.addActionListener(e -> confirmarSeleccion());
        btnRefrescar.addActionListener(e -> cargarPedidos());

        tabs.addTab("Stock", panelStock);
        tabs.addTab("Pedidos", panelPedidos);

        add(tabs, BorderLayout.CENTER);

        cargarProductos();
        cargarPedidos();
    }

    private void cargarProductos() {
        modeloStock.setRowCount(0);
        Producto[] productos = controladores.getProductoController().listarProductos();
        if (productos == null) return;
        for (Producto p : productos) {
            int stock = controladores.getStockController().getStock(p.getCodigo());
            modeloStock.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), stock});
        }
    }

    private void dialogAgregarStock() {
        int row = tablaStock.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }
        String codigo = (String) modeloStock.getValueAt(row, 0);
        String s = JOptionPane.showInputDialog(this, "Cantidad a agregar:");
        if (s == null) return;
        try {
            int cantidad = Integer.parseInt(s);
            controladores.getStockController().agregarStock(codigo, cantidad, vendedor.getCodigo());
            cargarProductos();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida");
        }
    }

    private void cargarStockCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            controladores.getStockController().cargarDesdeCSV(chooser.getSelectedFile(), vendedor.getCodigo());
            cargarProductos();
        }
    }

    private void mostrarHistorial() {
        JOptionPane.showMessageDialog(this, "La funcionalidad de historial abre un archivo CSV generado por el sistema.");
    }

    private void cargarPedidos() {
        modeloPedidos.setRowCount(0);
        Pedido[] pedidos = controladores.getPedidoController().listarPedidosPendientes();
        if (pedidos == null) return;
        for (Pedido p : pedidos) {
            modeloPedidos.addRow(new Object[]{p.getCodigo(), p.getFechaHora(), p.getCodigoCliente(), p.getTotal(), "Seleccionar"});
        }
    }

    private void confirmarSeleccion() {
        int row = tablaPedidos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido");
            return;
        }
        String codigo = (String) modeloPedidos.getValueAt(row, 0);
        boolean ok = controladores.getPedidoController().confirmarPedido(codigo, vendedor.getCodigo());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Pedido confirmado");
            cargarPedidos();
            cargarProductos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo confirmar el pedido (stock insuficiente?)");
        }
    }
}