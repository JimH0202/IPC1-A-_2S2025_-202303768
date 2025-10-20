package vista;

import controlador.Controladores;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.Pedido;
import modelo.Producto;
import modelo.Usuario;

/**
 * Panel para Cliente: catálogo, carrito y realizar pedidos.
 */
public class MenuCliente extends JPanel {
    private final Controladores controladores;
    private final Usuario cliente;
    private final DefaultTableModel modeloCatalogo;
    private final JTable tablaCatalogo;
    private final DefaultTableModel modeloCarrito;
    private final JTable tablaCarrito;

    public MenuCliente(Controladores controladores, Usuario cliente) {
        this.controladores = controladores;
        this.cliente = cliente;
        setLayout(new BorderLayout(8,8));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        add(split, BorderLayout.CENTER);

        // Catalogo
        JPanel panelCatalogo = new JPanel(new BorderLayout(6,6));
        modeloCatalogo = new DefaultTableModel(new Object[]{"Código","Nombre","Categoría","Stock","Precio"},0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tablaCatalogo = new JTable(modeloCatalogo);
        panelCatalogo.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);
        JPanel accionesCat = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar al carrito");
        JButton btnRefrescar = new JButton("Refrescar");
        accionesCat.add(btnAgregar);
        accionesCat.add(btnRefrescar);
        panelCatalogo.add(accionesCat, BorderLayout.NORTH);

        btnAgregar.addActionListener(e -> agregarAlCarrito());
        btnRefrescar.addActionListener(e -> cargarCatalogo());

        // Carrito
        JPanel panelCarrito = new JPanel(new BorderLayout(6,6));
        modeloCarrito = new DefaultTableModel(new Object[]{"Código","Nombre","Cantidad","Precio","Total"},0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        panelCarrito.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        JPanel accionesCar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEliminar = new JButton("Eliminar seleccionado");
        JButton btnActualizar = new JButton("Actualizar cantidad");
        JButton btnPedir = new JButton("Realizar pedido");
        accionesCar.add(btnEliminar);
        accionesCar.add(btnActualizar);
        accionesCar.add(btnPedir);
        panelCarrito.add(accionesCar, BorderLayout.NORTH);

        btnEliminar.addActionListener(e -> eliminarSeleccion());
        btnActualizar.addActionListener(e -> actualizarCantidad());
        btnPedir.addActionListener(e -> realizarPedido());

        split.setLeftComponent(panelCatalogo);
        split.setRightComponent(panelCarrito);
        split.setDividerLocation(520);

        cargarCatalogo();
    }

    private void cargarCatalogo() {
        modeloCatalogo.setRowCount(0);
        Producto[] productos = controladores.getProductoController().listarProductosDisponibles();
        if (productos == null) return;
        for (Producto p : productos) {
            int stock = controladores.getStockController().getStock(p.getCodigo());
            double precio = 0.0;
            modelo.Producto prod = controladores.getProductoController().buscarProducto(p.getCodigo());
            if (prod != null) precio = prod.getPrecio(); else precio = controladores.getStockController().getPrecio(p.getCodigo());
            modeloCatalogo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), stock, precio});
        }
    }

    // Exportar pedidos del cliente a PDF
    private void exportarMisPedidosPDF() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar reporte de mis pedidos");
    chooser.setSelectedFile(new java.io.File(util.PdfUtil.nombreReporte("MIS_PEDIDOS")));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File destino = chooser.getSelectedFile();
            try {
                Pedido[] todos = controladores.getPedidoController().listarTodos();
                java.util.List<Pedido> mis = new java.util.ArrayList<>();
                for (Pedido p : todos) {
                    if (p.getCodigoCliente().equalsIgnoreCase(cliente.getCodigo())) mis.add(p);
                }
                Pedido[] arr = mis.toArray(new Pedido[0]);
                util.PdfUtil.generarReportePedidos(destino, arr);
                JOptionPane.showMessageDialog(this, "Reporte generado: " + destino.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generando PDF: " + ex.getMessage());
            }
        }
    }

    private void agregarAlCarrito() {
        int row = tablaCatalogo.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }
        String codigo = (String) modeloCatalogo.getValueAt(row, 0);
        String s = JOptionPane.showInputDialog(this, "Cantidad:");
        if (s == null) return;
        try {
            int cantidad = Integer.parseInt(s);
            if (cantidad <= 0) throw new NumberFormatException();
            double precio = (double) modeloCatalogo.getValueAt(row, 4);
            String nombre = (String) modeloCatalogo.getValueAt(row,1);
            modeloCarrito.addRow(new Object[]{codigo, nombre, cantidad, precio, cantidad * precio});
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida");
        }
    }

    private void eliminarSeleccion() {
        int row = tablaCarrito.getSelectedRow();
        if (row >= 0) modeloCarrito.removeRow(row);
    }

    private void actualizarCantidad() {
        int row = tablaCarrito.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem del carrito");
            return;
        }
        String s = JOptionPane.showInputDialog(this, "Nueva cantidad:");
        if (s == null) return;
        try {
            int cantidad = Integer.parseInt(s);
            if (cantidad <= 0) throw new NumberFormatException();
            double precio = (double) modeloCarrito.getValueAt(row, 3);
            modeloCarrito.setValueAt(cantidad, row, 2);
            modeloCarrito.setValueAt(cantidad * precio, row, 4);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida");
        }
    }

    private void realizarPedido() {
        if (modeloCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Carrito vacío");
            return;
        }
        // Construir pedido simple y enviar a cola
        Pedido pedido = controladores.getPedidoController().crearPedidoDesdeCarrito(cliente.getCodigo(), modeloCarrito);
        if (pedido != null) {
            JOptionPane.showMessageDialog(this, "Pedido creado: " + pedido.getCodigo());
            modeloCarrito.setRowCount(0);
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear el pedido");
        }
    }
}