package vista;

import controlador.Controladores;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.Producto;
import modelo.Vendedor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import vista.DialogBitacoraViewer;

/**
 * Panel principal para el Administrador con pestañas: Vendedores, Productos, Reportes
 */
public class MenuAdministrador extends JPanel {
    private final Controladores controladores;

    public MenuAdministrador(Controladores controladores) {
        this.controladores = controladores;
        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Módulo Administrador", SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vendedores", crearPanelVendedores());
        tabs.addTab("Productos", crearPanelProductos());
        tabs.addTab("Reportes", crearPanelReportes());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel crearPanelVendedores() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Codigo", "Nombre", "Genero", "Ventas confirmadas"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JButton btnCrear = new JButton("Crear");
        JButton btnCargar = new JButton("Cargar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        // reducir la altura de los botones para dar más espacio a la gráfica
        Dimension btnSize = new Dimension(220, 44);
        for (JButton b : new JButton[]{btnCrear, btnCargar, btnActualizar, btnEliminar}) {
            b.setMaximumSize(btnSize);
            b.setPreferredSize(new Dimension(btnSize.width, btnSize.height));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            right.add(Box.createVerticalStrut(8));
            right.add(b);
        }

    final BarChartPanel graf = new BarChartPanel();
    graf.setBorder(BorderFactory.createTitledBorder("Top 3 - Vendedores con más ventas confirmadas"));
    // aumentar la altura para que las barras verticales se vean mejor
    graf.setPreferredSize(new Dimension(300, 420));
    graf.setAlignmentX(Component.CENTER_ALIGNMENT);
    right.add(Box.createVerticalStrut(8));
    right.add(graf);

        panel.add(right, BorderLayout.EAST);

    cargarVendedoresEnTabla(modelo);
    // inicializar gráfica
    actualizarGraficaVendedores(graf);

        btnCrear.addActionListener(e -> {
            DialogCrearVendedor d = new DialogCrearVendedor(SwingUtilities.getWindowAncestor(MenuAdministrador.this), controladores.getUsuarioController());
            d.setVisible(true);
            cargarVendedoresEnTabla(modelo);
            actualizarGraficaVendedores(graf);
        });

        btnCargar.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            if (ch.showOpenDialog(MenuAdministrador.this) == JFileChooser.APPROVE_OPTION) {
                util.CargaCSVResult r = controladores.getUsuarioController().cargarVendedoresDesdeCSV(ch.getSelectedFile());
                if (r != null) {
                    String msg = r.resumen();
                    if (!r.errores.isEmpty()) msg += "\nErrores:\n" + String.join("\n", r.errores);
                    JOptionPane.showMessageDialog(MenuAdministrador.this, msg, "Resultado carga vendedores", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            cargarVendedoresEnTabla(modelo);
            // actualizar grafica inmediatamente despues de cargar nuevos vendedores
            actualizarGraficaVendedores(graf);
        });
        // actualizar grafica despues de cargar desde CSV
        btnCargar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {}
        });

        btnActualizar.addActionListener(e -> {
            DialogActualizarVendedor d = new DialogActualizarVendedor(SwingUtilities.getWindowAncestor(MenuAdministrador.this), controladores.getUsuarioController());
            d.setVisible(true);
            cargarVendedoresEnTabla(modelo);
            actualizarGraficaVendedores(graf);
        });

        btnEliminar.addActionListener(e -> {
            DialogEliminarVendedor d = new DialogEliminarVendedor(SwingUtilities.getWindowAncestor(MenuAdministrador.this), controladores.getUsuarioController());
            d.setVisible(true);
            cargarVendedoresEnTabla(modelo);
            actualizarGraficaVendedores(graf);
        });

        return panel;
    }

    private void cargarVendedoresEnTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        // listarUsuariosPorTipo devuelve Usuario[] (controlador devuelve Usuario[]), casteamos a Vendedor[]
        modelo.setRowCount(0);
        modelo.setRowCount(0);
        modelo.setRowCount(0);
        modelo.setRowCount(0);
        modelo.setRowCount(0);
        modelo.setRowCount(0);
        Object[] arr = controladores.getUsuarioController().listarUsuariosPorTipo(Vendedor.class);
        if (arr == null) return;
        for (Object o : arr) {
            if (o instanceof Vendedor) {
                Vendedor ven = (Vendedor) o;
                modelo.addRow(new Object[]{ven.getCodigo(), ven.getNombre(), ven.getGenero(), ven.getVentasConfirmadas()});
            }
        }
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Codigo", "Nombre", "Categoria", "Precio", "Acciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // solo la columna 'Acciones' (índice 3) es editable para poder usar el editor con botón
                return column == 4;
            }
        };
    JTable tabla = new JTable(modelo);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel right = new JPanel(new GridLayout(4, 1, 8, 8));
        JButton btnCrear = new JButton("Crear");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnCargar = new JButton("Cargar");
        right.add(btnCrear);
        right.add(btnActualizar);
        right.add(btnEliminar);
        right.add(btnCargar);
        panel.add(right, BorderLayout.EAST);

        btnCrear.addActionListener(e -> {
            DialogCrearProducto d = new DialogCrearProducto(SwingUtilities.getWindowAncestor(this), controladores.getProductoController());
            d.setVisible(true);
            cargarProductosEnTabla(modelo);
        });

        btnCargar.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                util.CargaCSVResult r = controladores.getProductoController().cargarDesdeCSV(ch.getSelectedFile());
                if (r != null) {
                    String msg = r.resumen();
                    if (!r.errores.isEmpty()) msg += "\nErrores:\n" + String.join("\n", r.errores);
                    JOptionPane.showMessageDialog(MenuAdministrador.this, msg, "Resultado carga productos", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            cargarProductosEnTabla(modelo);
        });

        btnActualizar.addActionListener(e -> {
            int sel = tabla.getSelectedRow();
            String codigo = null;
            if (sel >= 0) codigo = (String) tabla.getValueAt(sel, 0);
            else codigo = JOptionPane.showInputDialog(MenuAdministrador.this, "Código del producto a actualizar:");
            if (codigo != null && !codigo.trim().isEmpty()) {
                DialogActualizarProducto d = new DialogActualizarProducto(SwingUtilities.getWindowAncestor(MenuAdministrador.this), controladores.getProductoController(), codigo.trim());
                d.setVisible(true);
                cargarProductosEnTabla(modelo);
            }
        });

        btnEliminar.addActionListener(e -> {
            int sel = tabla.getSelectedRow();
            String codigo = null;
            if (sel >= 0) codigo = (String) tabla.getValueAt(sel, 0);
            else codigo = JOptionPane.showInputDialog(MenuAdministrador.this, "Codigo a eliminar:");
            if (codigo != null && !codigo.trim().isEmpty()) {
                int conf = JOptionPane.showConfirmDialog(MenuAdministrador.this, "¿Confirmar eliminación de " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    boolean ok = controladores.getProductoController().eliminarProducto(codigo.trim());
                    if (!ok) JOptionPane.showMessageDialog(MenuAdministrador.this, "No se pudo eliminar (no existe)");
                    cargarProductosEnTabla(modelo);
                }
            }
        });

        cargarProductosEnTabla(modelo);

        // Convertir la columna "Acciones" en botones "Ver detalle" por fila
        tabla.getColumn("Acciones").setCellRenderer(new ButtonRenderer());
        tabla.getColumn("Acciones").setCellEditor(new ButtonEditor(new JCheckBox()));
        return panel;
    }

    private void cargarProductosEnTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        Producto[] productos = controladores.getProductoController().listarProductos();
        if (productos == null) return;
        for (Producto p : productos) {
            double precio = p.getPrecio();
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), String.format("%.2f", precio), "Ver detalle"});
        }
    }

    // Renderer para mostrar botón en la celda
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Editor que maneja el click en el botón y abre el diálogo de detalle
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    // obtener código desde la tabla
                    JTable table = (JTable) button.getClientProperty("table");
                    if (table == null) return;
                    Object codigoObj = table.getValueAt(currentRow, 0);
                    if (codigoObj == null) return;
                    String codigo = codigoObj.toString();
                    Producto p = controladores.getProductoController().buscarProducto(codigo);
                    if (p != null) {
                        // abrir diálogo con detalle específico por categoría
                        DialogDetalleProducto d = new DialogDetalleProducto(SwingUtilities.getWindowAncestor(MenuAdministrador.this), p);
                        d.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(MenuAdministrador.this, "Producto no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            // almacenar referencia a la tabla y fila
            button.putClientProperty("table", table);
            currentRow = row;
            isPushed = true;
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

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 12));
        JPanel left = new JPanel(new GridLayout(0, 1, 8, 8));
        left.add(new JButton(new AbstractAction("Top 5 - Más vendidos") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("TOP_VENDIDOS");
            }
        }));
        left.add(new JButton(new AbstractAction("Top 5 - Menos vendidos") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("MENOS_VENDIDOS");
            }
        }));
        left.add(new JButton(new AbstractAction("STOCK Inventario") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("STOCK_INVENTARIO");
            }
        }));
        left.add(new JButton(new AbstractAction("Ventas por vendedor") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("VENTAS_VENDEDOR");
            }
        }));
        left.add(new JButton(new AbstractAction("Clientes activos") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("CLIENTES_ACTIVOS");
            }
        }));
        left.add(new JButton(new AbstractAction("Reporte financiero") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("FINANCIERO");
            }
        }));
        left.add(new JButton(new AbstractAction("Productos por caducar") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("POR_CADUCAR");
            }
        }));

        // Botón para abrir la bitácora del sistema
        left.add(new JButton(new AbstractAction("Bitácora del Sistema") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                DialogBitacoraViewer d = new DialogBitacoraViewer(SwingUtilities.getWindowAncestor(MenuAdministrador.this), controladores.getBitacoraController());
                d.setVisible(true);
            }
        }));

        // Botón para ver datos del estudiante
        left.add(new JButton(new AbstractAction("Ver datos estudiante") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                DialogDatosEstudiante d = new DialogDatosEstudiante(SwingUtilities.getWindowAncestor(MenuAdministrador.this));
                d.setVisible(true);
            }
        }));

        panel.add(left);
        panel.add(new JPanel());
        return panel;
    }
      //Reportes
    private void generarReporte(String tipo) {
        JFileChooser ch = new JFileChooser();
        // sugerir nombre por defecto
        ch.setSelectedFile(new File(util.PdfUtil.nombreReporte(tipo)));
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = ch.getSelectedFile();
        try {
            java.util.List<String[]> filas = new java.util.ArrayList<>();
            String titulo = "Reporte";
            switch (tipo) {
                case "TOP_VENDIDOS": {
                    titulo = "Top Productos - Más vendidos";
                    util.ReportesUtil.ProductoVenta[] pv = util.ReportesUtil.topN(util.ReportesUtil.calcularVentasPorProducto(controladores.getPedidoController().listarTodos(), controladores.getProductoController().listarProductos()), 5);
                    // columnas: nombre, cantidad vendida, categoria, precio unitario, ingresos generados
                    for (util.ReportesUtil.ProductoVenta p : pv) {
                        if (p == null) continue;
                        // obtener precio unitario preferido (modelo producto o stock)
                        double precioUnit = 0.0;
                        modelo.Producto prod = controladores.getProductoController().buscarProducto(p.codigo);
                        if (prod != null) precioUnit = prod.getPrecio(); else precioUnit = controladores.getStockController().getPrecio(p.codigo);
                        filas.add(new String[]{p.nombre, String.valueOf(p.cantidad), p.categoria, String.format("%.2f", precioUnit), String.format("%.2f", p.ingresos)});
                    }
                    util.PdfUtil.generarReporteTabla(f, titulo, new String[]{"Nombre","Cantidad Vendida","Categoria","Precio Unit.","Ingresos Generados"}, filas);
                    break;
                }
                case "MENOS_VENDIDOS": {
                    titulo = "Top Productos - Menos vendidos";
                    util.ReportesUtil.ProductoVenta[] pv = util.ReportesUtil.bottomN(util.ReportesUtil.calcularVentasPorProducto(controladores.getPedidoController().listarTodos(), controladores.getProductoController().listarProductos()), 5);
                    // columnas: nombre, cantidad vendida, stock actual, recomendaciones de promocion
                    for (util.ReportesUtil.ProductoVenta p : pv) {
                        if (p == null) continue;
                        int stock = controladores.getStockController().getStock(p.codigo);
                        String rec = stock <= 5 ? "Promoción: Descuento 20%" : "Promoción: Combo/Regalo/Bundle";
                        filas.add(new String[]{p.nombre, String.valueOf(p.cantidad), String.valueOf(stock), rec});
                    }
                    util.PdfUtil.generarReporteTabla(f, titulo, new String[]{"Nombre","Cantidad Vendida","Stock Actual","Recomendaciones"}, filas);
                    break;
                }
                case "STOCK_INVENTARIO": {
                    titulo = "Reporte de Inventario";
                    String[] cods = controladores.getStockController().listarCodigos();
                    int[] stocks = controladores.getStockController().listarStocks();
                    util.ReportesUtil.StockEstado[] se = util.ReportesUtil.inventarioCritico(cods, stocks, controladores.getProductoController().listarProductos());
                    // columnas: codigo, nombre, categoria, stock actual, estado, fecha actualización stock, sugerencias
                    java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String now = java.time.LocalDateTime.now().format(df);
                    for (util.ReportesUtil.StockEstado s : se) {
                        String suger = s.estado.equalsIgnoreCase("CRITICO") ? "Reabastecer URGENTE" : (s.estado.equalsIgnoreCase("BAJO") ? "Reordenar / Considerar abastecer" : "OK");
                        // preferir precio desde el modelo Producto, si existe, sino desde stock
                        modelo.Producto prod = controladores.getProductoController().buscarProducto(s.codigo);
                        double precio = prod != null ? prod.getPrecio() : controladores.getStockController().getPrecio(s.codigo);
                        filas.add(new String[]{s.codigo, s.nombre, s.categoria, String.format("%.2f", precio), String.valueOf(s.stock), s.estado, now, suger});
                    }
                    util.PdfUtil.generarReporteTabla(f, titulo, new String[]{"Codigo","Nombre","Categoria","Precio","Stock Actual","Estado","Fecha Actualizacion","Sugerencias"}, filas);
                    break;
                }
                case "VENTAS_VENDEDOR": {
                    titulo = "Ventas por Vendedor";
                    util.ReportesUtil.VendedorVenta[] vv = util.ReportesUtil.ventasPorVendedor(controladores.getPedidoController().listarTodos());
                    // columnas: codigo, nombre (buscar en usuarios), cantidad pedidos, monto total, producto más vendido
                    for (util.ReportesUtil.VendedorVenta v : vv) {
                        String nombre = v.codigoVendedor;
                        modelo.Usuario u = controladores.getUsuarioController().buscarPorCodigo(v.codigoVendedor);
                        if (u != null) nombre = u.getNombre();
                        // producto más vendido por vendedor y monto total real
                        java.util.Map<String,Integer> cont = new java.util.HashMap<>();
                        double montoTotal = 0.0;
                        for (modelo.Pedido p : controladores.getPedidoController().listarTodos()) {
                            if (!p.isConfirmado()) continue;
                            if (v.codigoVendedor.equals(p.getVendedorConfirmador())) {
                                for (modelo.Pedido.Linea l : p.getLineas()) {
                                    cont.put(l.getCodigoProducto(), cont.getOrDefault(l.getCodigoProducto(), 0) + l.getCantidad());
                                    montoTotal += l.getCantidad() * l.getPrecioUnitario();
                                }
                            }
                        }
                        String topProd = "-";
                        int best = -1;
                        for (java.util.Map.Entry<String,Integer> e : cont.entrySet()) {
                            if (e.getValue() > best) { best = e.getValue(); topProd = e.getKey(); }
                        }
                        // mapear topProd a nombre si existe
                        String topProdNombre = topProd;
                        modelo.Producto prodTop = controladores.getProductoController().buscarProducto(topProd);
                        if (prodTop != null) topProdNombre = prodTop.getNombre();
                        filas.add(new String[]{v.codigoVendedor, nombre, String.valueOf(v.cantidadPedidos), String.format("%.2f", montoTotal), topProdNombre});
                    }
                    util.PdfUtil.generarReporteTabla(f, titulo, new String[]{"Codigo Vendedor","Nombre Vendedor","Pedidos Confirmados","Monto Total","Producto Mas Vendido"}, filas);
                    break;
                }
                case "CLIENTES_ACTIVOS": {
                    titulo = "Clientes Activos";
                    util.ReportesUtil.ClienteAct[] ca = util.ReportesUtil.clientesActivos(controladores.getPedidoController().listarTodos());
                    // columnas: codigo, nombre cliente, fecha de ultima compra, producto favorito (primeros 2)
                    java.time.format.DateTimeFormatter fdt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    for (util.ReportesUtil.ClienteAct c : ca) {
                        String nombre = c.codigoCliente;
                        modelo.Usuario u = controladores.getUsuarioController().buscarPorCodigo(c.codigoCliente);
                        if (u != null) nombre = u.getNombre();
                        // fecha ultima compra y productos favoritos (top 2)
                        java.time.LocalDateTime ultima = null;
                        java.util.Map<String,Integer> cont = new java.util.HashMap<>();
                        for (modelo.Pedido p : controladores.getPedidoController().listarTodos()) {
                            if (!p.isConfirmado()) continue;
                            if (c.codigoCliente.equals(p.getCodigoCliente())) {
                                // parsear fecha para determinar la ultima
                                try {
                                    java.time.LocalDateTime dt = java.time.LocalDateTime.parse(p.getFechaHora(), fdt);
                                    if (ultima == null || dt.isAfter(ultima)) ultima = dt;
                                } catch (Exception ex) {
                                    // si no se puede parsear, fallback a considerar la cadena
                                }
                                for (modelo.Pedido.Linea l : p.getLineas()) cont.put(l.getCodigoProducto(), cont.getOrDefault(l.getCodigoProducto(), 0) + l.getCantidad());
                            }
                        }
                        String fechaUlt = ultima == null ? "-" : ultima.format(fdt);
                        // obtener top2 ordenados
                        java.util.List<java.util.Map.Entry<String,Integer>> list = new java.util.ArrayList<>(cont.entrySet());
                        list.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));
                        java.util.List<String> favNames = new java.util.ArrayList<>();
                        for (int i = 0; i < Math.min(2, list.size()); i++) {
                            String code = list.get(i).getKey();
                            modelo.Producto prod = controladores.getProductoController().buscarProducto(code);
                            favNames.add(prod == null ? code : prod.getNombre());
                        }
                        String favs = String.join(";", favNames);
                        filas.add(new String[]{c.codigoCliente, nombre, fechaUlt, favs});
                    }
                    util.PdfUtil.generarReporteTabla(f, titulo, new String[]{"Codigo","Nombre","Fecha Ultima Compra","Productos Favoritos"}, filas);
                    break;
                }
                case "FINANCIERO": {
                    titulo = "Reporte Financiero por Categoria";
                    // categoria, cantidad vendido por categoria, ingresos totales por categoria, porcentaje, promedio precio
                    java.util.Map<String, Integer> cantidadPorCat = new java.util.HashMap<>();
                    java.util.Map<String, Double> ingresosPorCat = new java.util.HashMap<>();
                    java.util.Map<String, Integer> contPrecios = new java.util.HashMap<>();
                    int totalCant = 0; double totalIngresos = 0.0;
                    for (modelo.Pedido p : controladores.getPedidoController().listarTodos()) {
                        if (!p.isConfirmado()) continue;
                        for (modelo.Pedido.Linea l : p.getLineas()) {
                            modelo.Producto prod = controladores.getProductoController().buscarProducto(l.getCodigoProducto());
                            String cat = prod == null ? "-" : prod.getCategoria();
                            cantidadPorCat.put(cat, cantidadPorCat.getOrDefault(cat,0) + l.getCantidad());
                            ingresosPorCat.put(cat, ingresosPorCat.getOrDefault(cat,0.0) + l.getCantidad() * l.getPrecioUnitario());
                            contPrecios.put(cat, contPrecios.getOrDefault(cat,0) + l.getCantidad());
                            totalCant += l.getCantidad();
                            totalIngresos += l.getCantidad() * l.getPrecioUnitario();
                        }
                    }
                    for (String cat : cantidadPorCat.keySet()) {
                        int cant = cantidadPorCat.get(cat);
                        double inc = ingresosPorCat.getOrDefault(cat, 0.0);
                        // porcentaje por unidades (cantidad) y por ingresos (monetario)
                        double pctUnidades = totalCant == 0 ? 0.0 : (100.0 * cant / totalCant);
                        double pctIngresos = totalIngresos == 0.0 ? 0.0 : (100.0 * inc / totalIngresos);
                        double avg = contPrecios.getOrDefault(cat,0) == 0 ? 0.0 : inc / contPrecios.get(cat);
                        filas.add(new String[]{cat, String.valueOf(cant), String.format("%.2f", inc), String.format("%.2f%%", pctUnidades), String.format("%.2f%%", pctIngresos), String.format("%.2f", avg)});
                    }
                    util.PdfUtil.generarReporteTabla(f, titulo, new String[]{"Categoria","Cantidad Vendida","Ingresos","% Participación Ventas Unidades","% Participación Ingresos","Precio de Venta Promedio"}, filas);
                    break;
                }
                case "POR_CADUCAR": {
                    titulo = "Productos por Caducar";
                    util.ReportesUtil.ProductoPorCaducar[] pc = util.ReportesUtil.productosPorCaducar(controladores.getProductoController().listarProductos());
                    // codigo, nombre, fecha, dias restantes, stock actual, valor monetario en riesgo, estado prioridad
                    for (util.ReportesUtil.ProductoPorCaducar p : pc) {
                        int stock = controladores.getStockController().getStock(p.codigo);
                        // preferir precio almacenado en el modelo Producto, si existe
                        double precio = 0.0;
                        modelo.Producto productoExistente = controladores.getProductoController().buscarProducto(p.codigo);
                        if (productoExistente != null) precio = productoExistente.getPrecio(); else precio = controladores.getStockController().getPrecio(p.codigo);
                        double valor = precio * stock;
                        String prioridad = p.diasRestantes <= 3 ? "CRITICO" : (p.diasRestantes <= 7 ? "URGENTE" : "NORMAL");
                        String fecha = "-";
                        modelo.Producto prod = controladores.getProductoController().buscarProducto(p.codigo);
                        if (prod instanceof modelo.ProductoAlimento) {
                            java.time.LocalDate cad = ((modelo.ProductoAlimento) prod).getFechaCaducidad();
                            fecha = cad == null ? "-" : cad.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        }
                        filas.add(new String[]{p.codigo, p.nombre, fecha, String.valueOf(p.diasRestantes), String.valueOf(stock), String.format("%.2f", precio), String.format("%.2f", valor), prioridad});
                    }
                    util.PdfUtil.generarReporteTabla(f, titulo, new String[]{"Codigo","Nombre","Fecha","Dias Restantes","Stock","Precio","Valor en Riesgo","Prioridad"}, filas);
                    break;
                }
                default: {
                    JOptionPane.showMessageDialog(this, "Tipo de reporte no soportado");
                    return;
                }
            }
            // registrar en bitacora la generación de reporte
            controladores.getBitacoraController().registrar("ADMIN", "admin", "GENERAR_REPORTE", "EXITOSA", f.getName());
        } catch (Exception ex) {
            controladores.getBitacoraController().registrar("ADMIN", "admin", "GENERAR_REPORTE", "FALLIDA", ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error generando reporte: " + ex.getMessage());
        }
    }

    // Actualiza la gráfica de barras para mostrar top 3 vendedores
    private void actualizarGraficaVendedores(BarChartPanel panel) {
        // Construir metricas por vendedor usando ventasConfirmadas en el modelo + pedidos confirmados
        Object[] vObjs = controladores.getUsuarioController().listarUsuariosPorTipo(modelo.Vendedor.class);
        modelo.Vendedor[] vendedores = null;
        if (vObjs != null) {
            java.util.List<modelo.Vendedor> tmp = new java.util.ArrayList<>();
            for (Object o : vObjs) if (o instanceof modelo.Vendedor) tmp.add((modelo.Vendedor)o);
            vendedores = tmp.toArray(new modelo.Vendedor[0]);
        }
        if (vendedores == null || vendedores.length == 0) {
            panel.setData(new String[0], new double[0]);
            return;
        }

        // sumar ventas desde pedidos confirmados por vendedor
        modelo.Pedido[] pedidos = controladores.getPedidoController().listarTodos();
        java.util.Map<String, Integer> ventasPorVendedor = new java.util.HashMap<>();
        java.util.Map<String, Integer> ventasConfirmadasMap = new java.util.HashMap<>();
        for (modelo.Vendedor v : vendedores) {
            ventasPorVendedor.put(v.getCodigo(), v.getVentasConfirmadas());
            ventasConfirmadasMap.put(v.getCodigo(), v.getVentasConfirmadas());
        }

        // contar pedidos confirmados por vendedor (cada pedido = 1 venta)
        java.util.Map<String, Integer> pedidosPorVendedor = new java.util.HashMap<>();
        if (pedidos != null) {
            for (modelo.Pedido p : pedidos) {
                if (!p.isConfirmado()) continue;
                String vend = p.getVendedorConfirmador();
                if (vend == null) continue;
                pedidosPorVendedor.put(vend, pedidosPorVendedor.getOrDefault(vend, 0) + 1);
                ventasPorVendedor.put(vend, ventasPorVendedor.getOrDefault(vend, 0) + 1);
            }
        }

        // no imprimir diagnóstico en consola (limpieza) - solo usar datos internamente

        // ordenar y tomar top 3
        java.util.List<java.util.Map.Entry<String,Integer>> list = new java.util.ArrayList<>(ventasPorVendedor.entrySet());
        list.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));
        int n = Math.min(3, list.size());
        String[] names = new String[n];
        double[] totals = new double[n];
        for (int i = 0; i < n; i++) {
            String code = list.get(i).getKey();
            int val = list.get(i).getValue();
            modelo.Usuario u = controladores.getUsuarioController().buscarPorCodigo(code);
            String nombre = (u != null) ? u.getNombre() : code;
            names[i] = nombre + " (" + val + ")";
            totals[i] = val;
        }
        panel.setData(names, totals);
    }

    // Panel simple para dibujar una gráfica de barras sin librerías externas
    private static class BarChartPanel extends JPanel {
        private String[] labels = new String[0];
        private double[] values = new double[0];

        public void setData(String[] labels, double[] values) {
            this.labels = labels == null ? new String[0] : labels;
            this.values = values == null ? new double[0] : values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                int w = getWidth();
                int h = getHeight();
                g2.setColor(getBackground());
                g2.fillRect(0,0,w,h);
                if (labels.length == 0 || values.length == 0) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString("No hay datos para mostrar", 10, 20);
                    return;
                }
                double max = 0.0;
                for (double v : values) if (v > max) max = v;
                if (max <= 0) max = 1.0;
                    int marginSides = 10;
                    int marginTop = 34; // espacio superior extra para no pegar las barras al título
                    int labelHeight = 40;
                    int chartH = h - labelHeight - marginTop - marginSides;
                    int chartW = w - marginSides*2;
                int n = values.length;
                int barWidth = Math.max(20, chartW / (n*2));
                int gap = (chartW - n*barWidth) / (n+1);
                    int x = marginSides + gap;
                for (int i = 0; i < n; i++) {
                    int barH = (int) Math.round((values[i]/max) * chartH);
                        int y = marginTop + (chartH - barH);
                    // barra
                    g2.setColor(new Color(79,129,189));
                    g2.fillRect(x, y, barWidth, barH);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, barWidth, barH);
                    // valor encima
                    String valStr = String.format("%.2f", values[i]);
                    int vsw = g2.getFontMetrics().stringWidth(valStr);
                    g2.drawString(valStr, x + Math.max(0, (barWidth - vsw)/2), Math.max(12, y - 4));
                    // label abajo
                    String lbl = labels.length > i ? labels[i] : Integer.toString(i+1);
                    // envolver label si es largo
                    int lblY = marginTop + chartH + 15;
                    drawWrappedString(g2, lbl, x, lblY, barWidth);
                    x += barWidth + gap;
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawWrappedString(Graphics2D g2, String text, int x, int y, int width) {
            java.util.List<String> parts = new java.util.ArrayList<>();
            String[] toks = text.split("\\s+");
            String cur = "";
            for (String t : toks) {
                String cand = cur.isEmpty() ? t : cur + " " + t;
                if (g2.getFontMetrics().stringWidth(cand) > width && !cur.isEmpty()) { parts.add(cur); cur = t; }
                else cur = cand;
            }
            if (!cur.isEmpty()) parts.add(cur);
            int line = 0;
            for (String p : parts) {
                g2.drawString(p, x, y + line * 12);
                line++;
            }
        }
    }
}