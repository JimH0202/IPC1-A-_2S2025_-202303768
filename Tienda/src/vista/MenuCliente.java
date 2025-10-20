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
    private JLabel lblTotal;

    public MenuCliente(Controladores controladores, Usuario cliente) {
        this.controladores = controladores;
        this.cliente = cliente;
        setLayout(new BorderLayout(8,8));

    // Usaremos pestañas principales para separar Productos / Carrito / Historial
    JTabbedPane mainTabs = new JTabbedPane();
    add(mainTabs, BorderLayout.CENTER);

        // Catalogo
        JPanel panelCatalogo = new JPanel(new BorderLayout(6,6));
        modeloCatalogo = new DefaultTableModel(new Object[]{"Código","Nombre","Categoría","Stock","Precio","Acciones"},0){
            @Override public boolean isCellEditable(int r,int c){ return c == 5; }
        };
        tablaCatalogo = new JTable(modeloCatalogo);
    panelCatalogo.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);
    // renderer/editor para la columna Acciones en catálogo
    tablaCatalogo.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
    ButtonEditor catalogoEditor = new ButtonEditor(new JCheckBox());
    catalogoEditor.setClickCountToStart(1);
    tablaCatalogo.getColumnModel().getColumn(5).setCellEditor(catalogoEditor);
        JPanel accionesCat = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton btnRefrescar = new JButton("Refrescar");
        accionesCat.add(btnRefrescar);
        panelCatalogo.add(accionesCat, BorderLayout.NORTH);
        btnRefrescar.addActionListener(e -> cargarCatalogo());

        // Carrito
        JPanel panelCarrito = new JPanel(new BorderLayout(6,6));
        modeloCarrito = new DefaultTableModel(new Object[]{"Código","Nombre","Cantidad","Precio","Total","Opciones"},0){
            @Override public boolean isCellEditable(int r,int c){ return c == 5; }
        };
    tablaCarrito = new JTable(modeloCarrito);
    // renderer/editor para la columna Opciones (Actualizar/Eliminar por fila) -> botones separados
    tablaCarrito.getColumnModel().getColumn(5).setCellRenderer(new MultiButtonRenderer());
    MultiButtonEditor carritoEditor = new MultiButtonEditor(new JCheckBox());
    tablaCarrito.getColumnModel().getColumn(5).setCellEditor(carritoEditor);
    // Renderer para mostrar 2 decimales en la columna Total
    tablaCarrito.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
        @Override public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Number) setText(String.format("%.2f", ((Number) value).doubleValue()));
            return this;
        }
    });
        panelCarrito.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        JPanel accionesCar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnPedir = new JButton("Realizar pedido");
        accionesCar.add(btnPedir);
        panelCarrito.add(accionesCar, BorderLayout.NORTH);
        btnPedir.addActionListener(e -> realizarPedido());

    // total label (campo de instancia)
    JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    this.lblTotal = new JLabel("Total: 0.00");
    totalPanel.add(this.lblTotal);
    panelCarrito.add(totalPanel, BorderLayout.SOUTH);

        // Historial tab panel
    JPanel panelHistorial = new JPanel(new BorderLayout(6,6));
    DefaultTableModel modeloHist = new DefaultTableModel(new Object[]{"Código","Fecha de confirmación","Total"},0){ @Override public boolean isCellEditable(int r,int c){ return false;} };
    JTable tablaHist = new JTable(modeloHist);
    JPanel accionesHist = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton btnRefrescarAhora = new JButton("Refrescar ahora");
    accionesHist.add(btnRefrescarAhora);
    panelHistorial.add(accionesHist, BorderLayout.NORTH);
    panelHistorial.add(new JScrollPane(tablaHist), BorderLayout.CENTER);

        mainTabs.addTab("Productos", panelCatalogo);
        mainTabs.addTab("Carrito Compra", panelCarrito);
        mainTabs.addTab("Historial Compras", panelHistorial);

        cargarCatalogo();
        cargarHistorial(modeloHist);

        btnRefrescarAhora.addActionListener(ev -> {
            cargarCatalogo();
            cargarHistorial(modeloHist);
        });

        // Timer para refresco automático (5s)
        javax.swing.Timer refresco = new javax.swing.Timer(5000, ev -> {
            cargarCatalogo();
            cargarHistorial(modeloHist);
        });
        refresco.setRepeats(true);
        refresco.start();
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
            modeloCatalogo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), stock, precio, "Agregar"});
        }
    }

    // calcular total general del carrito y actualizar label
    private void actualizarTotalCarrito(JLabel lblTotal) {
        double total = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            Object v = modeloCarrito.getValueAt(i, 4);
            if (v instanceof Number) total += ((Number) v).doubleValue();
        }
        lblTotal.setText(String.format("Total: %.2f", total));
    }

    // Renderer/editor simplificados (reutilizan patrón de MenuVendedor)
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
                        // Si es tabla de catálogo -> agregar al carrito preguntando cantidad
                        if (table == tablaCatalogo) {
                            String codigo = (String) modeloCatalogo.getValueAt(row, 0);
                            String s = JOptionPane.showInputDialog(MenuCliente.this, "Cantidad:");
                            if (s == null) return;
                            try {
                                int cantidad = Integer.parseInt(s);
                                if (cantidad <= 0) throw new NumberFormatException();
                                int stock = controladores.getStockController().getStock(codigo);
                                if (stock < cantidad) { JOptionPane.showMessageDialog(MenuCliente.this, "Stock insuficiente"); return; }
                                boolean ok = controladores.getStockController().agregarStock(codigo, -cantidad, cliente.getCodigo());
                                if (!ok) { JOptionPane.showMessageDialog(MenuCliente.this, "No se pudo reservar stock"); return; }
                                double precio = Double.parseDouble(modeloCatalogo.getValueAt(row, 4).toString());
                                String nombre = (String) modeloCatalogo.getValueAt(row,1);
                                // stop editing then perform model update asynchronously to avoid editor/model conflict
                                fireEditingStopped();
                                SwingUtilities.invokeLater(() -> {
                                    // consolidar: si ya existe una fila con mismo código sumar cantidades
                                    int found = -1;
                                    for (int r = 0; r < modeloCarrito.getRowCount(); r++) {
                                        if (((String) modeloCarrito.getValueAt(r,0)).equalsIgnoreCase(codigo)) { found = r; break; }
                                    }
                                    if (found >= 0) {
                                        int old = Integer.parseInt(modeloCarrito.getValueAt(found, 2).toString());
                                        int nueva = old + cantidad;
                                        modeloCarrito.setValueAt(nueva, found, 2);
                                        modeloCarrito.setValueAt(nueva * precio, found, 4);
                                    } else {
                                        modeloCarrito.addRow(new Object[]{codigo, nombre, cantidad, precio, cantidad * precio, "Opciones"});
                                    }
                                    try { actualizarTotalCarrito(lblTotal); } catch (Exception ignored) {}
                                    // refrescar catálogo para actualizar stocks y liberar cualquier estado de edición
                                    try { cargarCatalogo(); tablaCatalogo.repaint(); } catch (Exception ignored) {}
                                });
                            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(MenuCliente.this, "Cantidad inválida"); }
                        } else if (table == tablaCarrito) {
                            // si es carrito: mostrar menu para Actualizar o Eliminar
                            Object[] opts = new Object[]{"Actualizar","Eliminar","Cancelar"};
                            int sel = JOptionPane.showOptionDialog(MenuCliente.this, "Seleccione acción:", "Opciones", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
                            if (sel == 0) {
                                // actualizar cantidad
                                String s = JOptionPane.showInputDialog(MenuCliente.this, "Nueva cantidad:");
                                if (s == null) return;
                                try {
                                    int cantidad = Integer.parseInt(s);
                                    if (cantidad <= 0) throw new NumberFormatException();
                                    String codigo = (String) modeloCarrito.getValueAt(row, 0);
                                    int old = (int) modeloCarrito.getValueAt(row, 2);
                                    int diff = cantidad - old;
                                    if (diff > 0) {
                                        int stock = controladores.getStockController().getStock(codigo);
                                        if (stock < diff) { JOptionPane.showMessageDialog(MenuCliente.this, "Stock insuficiente"); return; }
                                        controladores.getStockController().agregarStock(codigo, -diff, cliente.getCodigo());
                                    } else if (diff < 0) {
                                        controladores.getStockController().agregarStock(codigo, -diff, cliente.getCodigo());
                                    }
                                    double precio = Double.parseDouble(modeloCarrito.getValueAt(row, 3).toString());
                                    modeloCarrito.setValueAt(cantidad, row, 2);
                                    modeloCarrito.setValueAt(cantidad * precio, row, 4);
                                } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(MenuCliente.this, "Cantidad inválida"); }
                            } else if (sel == 1) {
                                // eliminar: liberar stock
                                String codigo = (String) modeloCarrito.getValueAt(row, 0);
                                int cantidad = Integer.parseInt(modeloCarrito.getValueAt(row, 2).toString());
                                controladores.getStockController().agregarStock(codigo, cantidad, cliente.getCodigo());
                                modeloCarrito.removeRow(row);
                            }
                        }
                    }
                    // actualizar total visual directamente
                    try { actualizarTotalCarrito(lblTotal); } catch (Exception ignored) {}
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
            button.putClientProperty("table", table);
            return button;
        }

        @Override
        public Object getCellEditorValue() { isPushed = false; return label; }

        @Override
        public boolean stopCellEditing() { isPushed = false; return super.stopCellEditing(); }
    }

    // Renderer/editor con dos botones (Actualizar / Eliminar) dentro de la celda
    class MultiButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btn1 = new JButton("Actualizar");
        private final JButton btn2 = new JButton("Eliminar");
        public MultiButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
            add(btn1); add(btn2);
        }
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class MultiButtonEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,4,0));
        private final JButton btnActualizar = new JButton("Actualizar");
        private final JButton btnEliminar = new JButton("Eliminar");
        private JTable table;
        private int row;

        public MultiButtonEditor(JCheckBox chk) {
            panel.add(btnActualizar); panel.add(btnEliminar);
            btnActualizar.addActionListener(ae -> {
                if (table == null || row < 0) return;
                String s = JOptionPane.showInputDialog(MenuCliente.this, "Nueva cantidad:");
                if (s == null) return;
                try {
                    int cantidad = Integer.parseInt(s);
                    if (cantidad <= 0) throw new NumberFormatException();
                    String codigo = (String) modeloCarrito.getValueAt(row, 0);
                    int old = Integer.parseInt(modeloCarrito.getValueAt(row, 2).toString());
                    int diff = cantidad - old;
                    if (diff > 0) {
                        int stock = controladores.getStockController().getStock(codigo);
                        if (stock < diff) { JOptionPane.showMessageDialog(MenuCliente.this, "Stock insuficiente"); return; }
                        controladores.getStockController().agregarStock(codigo, -diff, cliente.getCodigo());
                    } else if (diff < 0) {
                        controladores.getStockController().agregarStock(codigo, -diff, cliente.getCodigo());
                    }
                    double precio = Double.parseDouble(modeloCarrito.getValueAt(row, 3).toString());
                    // stop editing and then update model on EDT to avoid concurrent modification
                    fireEditingStopped();
                    SwingUtilities.invokeLater(() -> {
                        modeloCarrito.setValueAt(cantidad, row, 2);
                        modeloCarrito.setValueAt(cantidad * precio, row, 4);
                        actualizarTotalCarrito(lblTotal);
                    });
                } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(MenuCliente.this, "Cantidad inválida"); }
            });
            btnEliminar.addActionListener(ae -> {
                if (table == null || row < 0) return;
                // stop editing and then remove row on EDT
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> {
                    String codigo = (String) modeloCarrito.getValueAt(row, 0);
                    int cantidad = Integer.parseInt(modeloCarrito.getValueAt(row, 2).toString());
                    controladores.getStockController().agregarStock(codigo, cantidad, cliente.getCodigo());
                    if (row >= 0 && row < modeloCarrito.getRowCount()) modeloCarrito.removeRow(row);
                    actualizarTotalCarrito(lblTotal);
                    tablaCarrito.repaint(); tablaCarrito.revalidate();
                });
            });
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table; this.row = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return null; }
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
            int stock = controladores.getStockController().getStock(codigo);
            if (stock < cantidad) { JOptionPane.showMessageDialog(this, "Stock insuficiente"); return; }
            // reservar stock temporalmente
            boolean ok = controladores.getStockController().agregarStock(codigo, -cantidad, cliente.getCodigo());
            if (!ok) { JOptionPane.showMessageDialog(this, "No se pudo reservar stock"); return; }
            double precio = (double) modeloCatalogo.getValueAt(row, 4);
            String nombre = (String) modeloCatalogo.getValueAt(row,1);
            modeloCarrito.addRow(new Object[]{codigo, nombre, cantidad, precio, cantidad * precio});
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida");
        }
    }

    private void eliminarSeleccion() {
        int row = tablaCarrito.getSelectedRow();
        if (row >= 0) {
            // liberar stock reservado
            String codigo = (String) modeloCarrito.getValueAt(row, 0);
            int cantidad = (int) modeloCarrito.getValueAt(row, 2);
            controladores.getStockController().agregarStock(codigo, cantidad, cliente.getCodigo());
            modeloCarrito.removeRow(row);
        }
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
            String codigo = (String) modeloCarrito.getValueAt(row, 0);
            int old = (int) modeloCarrito.getValueAt(row, 2);
            int diff = cantidad - old;
            if (diff > 0) {
                int stock = controladores.getStockController().getStock(codigo);
                if (stock < diff) { JOptionPane.showMessageDialog(this, "Stock insuficiente"); return; }
                controladores.getStockController().agregarStock(codigo, -diff, cliente.getCodigo());
            } else if (diff < 0) {
                controladores.getStockController().agregarStock(codigo, -diff, cliente.getCodigo());
            }
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
            // Después de crear el pedido, el historial puede actualizarse por si hay pedidos confirmados
            // (la recarga real del modelo de historial la realiza cargarHistorial cuando se necesite)
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear el pedido");
        }
    }

    private void cargarHistorial(DefaultTableModel modeloHist) {
        modeloHist.setRowCount(0);
        Pedido[] todos = controladores.getPedidoController().listarTodos();
        if (todos == null) return;
        for (Pedido p : todos) {
            if (!p.isConfirmado()) continue;
            if (!p.getCodigoCliente().equalsIgnoreCase(cliente.getCodigo())) continue;
            modeloHist.addRow(new Object[]{p.getCodigo(), p.getFechaHora(), String.format("%.2f", p.getTotal())});
        }
    }
}