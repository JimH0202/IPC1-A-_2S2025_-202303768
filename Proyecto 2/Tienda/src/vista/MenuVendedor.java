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
        modeloStock = new DefaultTableModel(new Object[]{"Código","Nombre","Categoría","Stock","Precio","Acciones"},0){
            @Override public boolean isCellEditable(int row,int col){ return col == 5; }
        };
        tablaStock = new JTable(modeloStock);
        // renderer/editor para botón en la columna Acciones
        tablaStock.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        ButtonEditor stockEditor = new ButtonEditor(new JCheckBox());
        stockEditor.setClickCountToStart(1);
        tablaStock.getColumnModel().getColumn(5).setCellEditor(stockEditor);
        panelStock.add(new JScrollPane(tablaStock), BorderLayout.CENTER);

        JPanel accionesStock = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregarStock = new JButton("Agregar stock");
        JButton btnCargarStockCSV = new JButton("Cargar stock CSV");
        JButton btnVerHistorial = new JButton("Ver historial general");
        accionesStock.add(btnAgregarStock);
        accionesStock.add(btnCargarStockCSV);
        accionesStock.add(btnVerHistorial);
        panelStock.add(accionesStock, BorderLayout.NORTH);

    btnAgregarStock.addActionListener(e -> dialogAgregarStockForm());
        btnCargarStockCSV.addActionListener(e -> cargarStockCSV());
    btnVerHistorial.addActionListener(e -> mostrarHistorial());

    // Pedidos tab
        JPanel panelPedidos = new JPanel(new BorderLayout(6,6));
        modeloPedidos = new DefaultTableModel(new Object[]{"Código","Fecha","Cliente","Nombre Cliente","Total","Opciones"},0){
            @Override public boolean isCellEditable(int row,int col){ return col == 5; }
        };
        tablaPedidos = new JTable(modeloPedidos);
        // convertir la columna Opciones en botón por fila para confirmar
        tablaPedidos.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        ButtonEditor pedidosEditor = new ButtonEditor(new JCheckBox());
        pedidosEditor.setClickCountToStart(1);
        tablaPedidos.getColumnModel().getColumn(5).setCellEditor(pedidosEditor);
        panelPedidos.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
    JPanel accionesPedidos = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton btnConfirmar = new JButton("Confirmar todos los pedidos");
        JButton btnRefrescar = new JButton("Refrescar");
        accionesPedidos.add(btnConfirmar);
        accionesPedidos.add(btnRefrescar);
        panelPedidos.add(accionesPedidos, BorderLayout.NORTH);

    btnConfirmar.addActionListener(e -> confirmarTodosPedidos());
        btnRefrescar.addActionListener(e -> cargarPedidos());

    // Añadir pestañas (Stock ya añadido más abajo)
        // Clientes tab
        JPanel panelClientes = new JPanel(new BorderLayout(6,6));
        DefaultTableModel modeloClientes = new DefaultTableModel(new Object[]{"Código","Nombre","Género","Cumpleaños"},0) {
            @Override public boolean isCellEditable(int row,int col){ return false; }
        };
        JTable tablaClientes = new JTable(modeloClientes);
        panelClientes.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
        JPanel accionesClientes = new JPanel(new GridLayout(4,1,6,6));
        JButton btnCrearCliente = new JButton("Crear");
        JButton btnCargarClientes = new JButton("Cargar");
        JButton btnActualizarCliente = new JButton("Actualizar");
        JButton btnEliminarCliente = new JButton("Eliminar");
        accionesClientes.add(btnCrearCliente); accionesClientes.add(btnCargarClientes); accionesClientes.add(btnActualizarCliente); accionesClientes.add(btnEliminarCliente);
        panelClientes.add(accionesClientes, BorderLayout.EAST);

        btnCrearCliente.addActionListener(e -> {
            DialogCrearUsuario d = new DialogCrearUsuario(SwingUtilities.getWindowAncestor(MenuVendedor.this), controladores.getUsuarioController(), "Cliente");
            d.setVisible(true);
            cargarClientesEnTabla(modeloClientes);
        });
        btnCargarClientes.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            if (ch.showOpenDialog(MenuVendedor.this) == JFileChooser.APPROVE_OPTION) {
                    // registrar inicio de carga CSV clientes (UI)
                    try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CARGA_CSV_CLIENTES_UI", "INICIO", ch.getSelectedFile().getName()); } catch (Exception ignored) {}
                    util.CargaCSVResult r = controladores.getUsuarioController().cargarClientesDesdeCSV(ch.getSelectedFile());
                    if (r != null) {
                        String msg = r.resumen();
                        if (!r.errores.isEmpty()) msg += "\nErrores:\n" + String.join("\n", r.errores);
                        JOptionPane.showMessageDialog(MenuVendedor.this, msg, "Resultado carga clientes", JOptionPane.INFORMATION_MESSAGE);
                    }
                cargarClientesEnTabla(modeloClientes);
            }
        });
        btnActualizarCliente.addActionListener(e -> {
            // validar que existan clientes
            Object[] clients = controladores.getUsuarioController().listarUsuariosPorTipo(modelo.Cliente.class);
            if (clients == null || clients.length == 0) { JOptionPane.showMessageDialog(MenuVendedor.this, "No hay clientes creados"); return; }
            // Abrir diálogo de actualización (el diálogo tiene su propio buscador)
            DialogActualizarCliente dlg = new DialogActualizarCliente(SwingUtilities.getWindowAncestor(MenuVendedor.this), controladores.getUsuarioController());
            dlg.setVisible(true);
            cargarClientesEnTabla(modeloClientes);
        });
        btnEliminarCliente.addActionListener(e -> {
            Object[] clients = controladores.getUsuarioController().listarUsuariosPorTipo(modelo.Cliente.class);
            if (clients == null || clients.length == 0) { JOptionPane.showMessageDialog(MenuVendedor.this, "No hay clientes creados"); return; }
            int r = tablaClientes.getSelectedRow();
            String codigo = null;
            if (r >= 0) codigo = (String) modeloClientes.getValueAt(r, 0);
            else codigo = JOptionPane.showInputDialog(MenuVendedor.this, "Código del cliente a eliminar:");
            if (codigo != null && !codigo.trim().isEmpty()) {
                modelo.Usuario u = controladores.getUsuarioController().buscarPorCodigo(codigo.trim());
                if (u == null) { JOptionPane.showMessageDialog(MenuVendedor.this, "Código no encontrado"); return; }
                int conf = JOptionPane.showConfirmDialog(MenuVendedor.this, "Confirmar eliminación de " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    boolean ok = controladores.getUsuarioController().eliminarUsuario(codigo.trim());
                    if (!ok) JOptionPane.showMessageDialog(MenuVendedor.this, "No se pudo eliminar");
                    cargarClientesEnTabla(modeloClientes);
                }
            }
        });

    tabs.addTab("Productos", panelStock);
        tabs.addTab("Clientes", panelClientes);
        tabs.addTab("Pedidos", panelPedidos);

        add(tabs, BorderLayout.CENTER);

        cargarProductos();
        cargarClientesEnTabla((DefaultTableModel) tablaClientes.getModel());
        cargarPedidos();
    }

    private void cargarClientesEnTabla(DefaultTableModel modeloClientes) {
        modeloClientes.setRowCount(0);
        // listar todos los usuarios tipo Cliente
        Object[] clients = controladores.getUsuarioController().listarUsuariosPorTipo(modelo.Cliente.class);
        if (clients == null) return;
        for (Object o : clients) {
            if (o instanceof modelo.Cliente) {
                modelo.Cliente c = (modelo.Cliente) o;
                modeloClientes.addRow(new Object[]{c.getCodigo(), c.getNombre(), c.getGenero(), c.getCumpleanosFormato()});
            }
        }
    }

    private void cargarProductos() {
        modeloStock.setRowCount(0);
        Producto[] productos = controladores.getProductoController().listarProductos();
        if (productos == null) return;
        for (Producto p : productos) {
            int stock = controladores.getStockController().getStock(p.getCodigo());
            double precio = controladores.getProductoController().buscarProducto(p.getCodigo()) != null ? controladores.getProductoController().buscarProducto(p.getCodigo()).getPrecio() : controladores.getStockController().getPrecio(p.getCodigo());
            modeloStock.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), stock, String.format("%.2f", precio), "Ver historial"});
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
            boolean ok = controladores.getStockController().agregarStock(codigo, cantidad, vendedor.getCodigo());
            if (ok) {
                try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "AGREGAR_STOCK_UI", "EXITOSA", String.format("Vendedor %s agregó stock Producto %s Cant=%d", vendedor.getCodigo(), codigo, cantidad)); } catch (Exception ignored) {}
            } else {
                try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "AGREGAR_STOCK_UI", "FALLIDA", String.format("Vendedor %s intentó agregar Producto %s Cant=%d", vendedor.getCodigo(), codigo, cantidad)); } catch (Exception ignored) {}
            }
            cargarProductos();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida");
        }
    }

    private void dialogAgregarStockForm() {
        JPanel p = new JPanel(new GridLayout(3,2,6,6));
        JTextField tfCodigo = new JTextField();
        JTextField tfCantidad = new JTextField();
        p.add(new JLabel("Código producto:")); p.add(tfCodigo);
        p.add(new JLabel("Cantidad a agregar (use negativo para descontar):")); p.add(tfCantidad);
        int res = JOptionPane.showConfirmDialog(this, p, "Agregar stock", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        String codigo = tfCodigo.getText().trim();
        int cantidad = 0;
        try { cantidad = Integer.parseInt(tfCantidad.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Cantidad inválida"); return; }
        if (controladores.getProductoController().buscarProducto(codigo) == null) { JOptionPane.showMessageDialog(this, "Código de producto no existe"); return; }
        boolean ok = controladores.getStockController().agregarStock(codigo, cantidad, vendedor.getCodigo());
        if (!ok) {
            JOptionPane.showMessageDialog(this, "No se pudo agregar stock");
            try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "AGREGAR_STOCK_UI", "FALLIDA", String.format("Agregar stock formulario: %s Cant=%d", codigo, cantidad)); } catch (Exception ignored) {}
        } else {
            try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "AGREGAR_STOCK_UI", "EXITOSA", String.format("Agregar stock formulario: %s Cant=%d", codigo, cantidad)); } catch (Exception ignored) {}
            cargarProductos();
        }
    }

    private void cargarStockCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // registrar inicio de carga CSV stock (UI)
            try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CARGA_CSV_STOCK_UI", "INICIO", chooser.getSelectedFile().getName()); } catch (Exception ignored) {}
            util.CargaCSVResult r = controladores.getStockController().cargarDesdeCSV(chooser.getSelectedFile(), vendedor.getCodigo());
            if (r != null) {
                String msg = r.resumen();
                if (!r.errores.isEmpty()) msg += "\nErrores:\n" + String.join("\n", r.errores);
                JOptionPane.showMessageDialog(MenuVendedor.this, msg, "Resultado carga stock", JOptionPane.INFORMATION_MESSAGE);
            }
            cargarProductos();
        }
    }

    private void mostrarHistorial() {
        DialogHistorialIngresos dlg = new DialogHistorialIngresos(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
    }

    private void cargarPedidos() {
        modeloPedidos.setRowCount(0);
        Pedido[] pedidos = controladores.getPedidoController().listarPedidosPendientes();
        if (pedidos == null) return;
        for (Pedido p : pedidos) {
            String nombreCliente = p.getCodigoCliente();
            modelo.Usuario u = controladores.getUsuarioController().buscarPorCodigo(p.getCodigoCliente());
            if (u != null) nombreCliente = u.getNombre();
            modeloPedidos.addRow(new Object[]{p.getCodigo(), p.getFechaHora(), p.getCodigoCliente(), nombreCliente, p.getTotal(), "Confirmar"});
        }
    }

    private void confirmarSeleccion() {
        int row = tablaPedidos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido");
            return;
        }
        String codigo = (String) modeloPedidos.getValueAt(row, 0);
        try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CONFIRMAR_PEDIDO_UI", "INTENTO", codigo); } catch (Exception ignored) {}
        boolean ok = controladores.getPedidoController().confirmarPedido(codigo, vendedor.getCodigo());
        if (ok) {
            try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CONFIRMAR_PEDIDO_UI", "EXITOSA", codigo); } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this, "Pedido confirmado");
            cargarPedidos();
            cargarProductos();
        } else {
            try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CONFIRMAR_PEDIDO_UI", "FALLIDA", codigo); } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this, "No se pudo confirmar el pedido (stock insuficiente?)");
        }
    }

    private void confirmarTodosPedidos() {
        Pedido[] pedidos = controladores.getPedidoController().listarPedidosPendientes();
        if (pedidos == null || pedidos.length == 0) {
            JOptionPane.showMessageDialog(this, "No hay pedidos pendientes");
            return;
        }
        int exitos = 0; int fallidos = 0;
        for (Pedido p : pedidos) {
            try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CONFIRMAR_PEDIDO_UI", "INTENTO", p.getCodigo()); } catch (Exception ignored) {}
            boolean ok = controladores.getPedidoController().confirmarPedido(p.getCodigo(), vendedor.getCodigo());
            if (ok) { exitos++; try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CONFIRMAR_PEDIDO_UI", "EXITOSA", p.getCodigo()); } catch (Exception ignored) {} } else { fallidos++; try { controladores.getBitacoraController().registrar(vendedor.getRol(), vendedor.getCodigo(), "CONFIRMAR_PEDIDO_UI", "FALLIDA", p.getCodigo()); } catch (Exception ignored) {} }
        }
        String msg = String.format("Confirmados: %d. Fallidos: %d.", exitos, fallidos);
        JOptionPane.showMessageDialog(this, msg);
        cargarPedidos();
        cargarProductos();
    }

    // Componentes para botones en tablas
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private JTable currentTable;
        private int currentRow;

        public ButtonEditor(final JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JTable table = currentTable;
                    int row = currentRow;
                    if (table != null && row >= 0) {
                        // si es la tabla de productos -> abrir historial del producto
                        if (table == tablaStock) {
                            // pasar el CÓDIGO del producto (columna 0) para que el historial filtre por codigo
                            String codigoProducto = (String) tablaStock.getValueAt(row, 0);
                            DialogHistorialIngresos dlg = new DialogHistorialIngresos(SwingUtilities.getWindowAncestor(MenuVendedor.this), codigoProducto);
                            dlg.setVisible(true);
                        } else if (table == tablaPedidos) {
                            String codigoPedido = (String) tablaPedidos.getValueAt(row, 0);
                            boolean ok = controladores.getPedidoController().confirmarPedido(codigoPedido, vendedor.getCodigo());
                            if (ok) { JOptionPane.showMessageDialog(MenuVendedor.this, "Pedido confirmado"); cargarPedidos(); cargarProductos(); }
                            else JOptionPane.showMessageDialog(MenuVendedor.this, "No se pudo confirmar (stock?)");
                        }
                    }
                    fireEditingStopped();
                }
            });
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentTable = table;
            currentRow = row;
            // store reference for safety
            button.putClientProperty("table", table);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}