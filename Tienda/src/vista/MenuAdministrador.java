package vista;

import controlador.Controladores;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.Producto;
import modelo.Vendedor;

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

        JPanel right = new JPanel(new GridLayout(5, 1, 8, 8));
        JButton btnCrear = new JButton("Crear");
        JButton btnCargar = new JButton("Cargar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        right.add(btnCrear);
        right.add(btnCargar);
        right.add(btnActualizar);
        right.add(btnEliminar);

        JPanel graf = new JPanel();
        graf.setBorder(BorderFactory.createTitledBorder("Top 3 - Vendedores con más ventas confirmadas"));
        graf.setPreferredSize(new Dimension(200, 140));
        right.add(graf);

        panel.add(right, BorderLayout.EAST);

        cargarVendedoresEnTabla(modelo);

        btnCrear.addActionListener(e -> {
            DialogCrearVendedor d = new DialogCrearVendedor(SwingUtilities.getWindowAncestor(this), controladores.getUsuarioController());
            d.setVisible(true);
            cargarVendedoresEnTabla(modelo);
        });

        btnCargar.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                controladores.getUsuarioController().cargarVendedoresDesdeCSV(ch.getSelectedFile());
            }
            cargarVendedoresEnTabla(modelo);
        });

        btnActualizar.addActionListener(e -> {
            DialogActualizarVendedor d = new DialogActualizarVendedor(SwingUtilities.getWindowAncestor(this), controladores.getUsuarioController());
            d.setVisible(true);
            cargarVendedoresEnTabla(modelo);
        });

        btnEliminar.addActionListener(e -> {
            DialogEliminarVendedor d = new DialogEliminarVendedor(SwingUtilities.getWindowAncestor(this), controladores.getUsuarioController());
            d.setVisible(true);
            cargarVendedoresEnTabla(modelo);
        });

        return panel;
    }

    private void cargarVendedoresEnTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        // listarUsuariosPorTipo devuelve Usuario[] (controlador devuelve Usuario[]), por eso usamos Usuario[] y casteamos
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
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Codigo", "Nombre", "Categoria", "Acciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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
                controladores.getProductoController().cargarDesdeCSV(ch.getSelectedFile());
            }
            cargarProductosEnTabla(modelo);
        });

        btnActualizar.addActionListener(e -> {
            DialogActualizarProducto d = new DialogActualizarProducto(SwingUtilities.getWindowAncestor(this), controladores.getProductoController(), "");
            d.setVisible(true);
            cargarProductosEnTabla(modelo);
        });

        btnEliminar.addActionListener(e -> {
            String codigo = JOptionPane.showInputDialog(this, "Codigo a eliminar:");
            if (codigo != null) {
                controladores.getProductoController().eliminarProducto(codigo);
                cargarProductosEnTabla(modelo);
            }
        });

        cargarProductosEnTabla(modelo);
        return panel;
    }

    private void cargarProductosEnTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        Producto[] productos = controladores.getProductoController().listarProductos();
        if (productos == null) return;
        for (Producto p : productos) {
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), "Ver detalle"});
        }
    }

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 12));
        JPanel left = new JPanel(new GridLayout(0, 1, 8, 8));
        left.add(new JButton(new AbstractAction("Top 5 - Más vendidos") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("TOP_VENDIDOS");
            }
        }));
        left.add(new JButton(new AbstractAction("Top 5 - Menos vendidos") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("MENOS_VENDIDOS");
            }
        }));
        left.add(new JButton(new AbstractAction("Inventario crítico") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("INVENTARIO_CRITICO");
            }
        }));
        left.add(new JButton(new AbstractAction("Ventas por vendedor") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("VENTAS_VENDEDOR");
            }
        }));
        left.add(new JButton(new AbstractAction("Clientes activos") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("CLIENTES_ACTIVOS");
            }
        }));
        left.add(new JButton(new AbstractAction("Reporte financiero") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("FINANCIERO");
            }
        }));
        left.add(new JButton(new AbstractAction("Productos por caducar") {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                generarReporte("POR_CADUCAR");
            }
        }));

        panel.add(left);
        panel.add(new JPanel());
        return panel;
    }

    private void generarReporte(String tipo) {
        JFileChooser ch = new JFileChooser();
        // sugerir nombre por defecto
        ch.setSelectedFile(new File(util.PdfUtil.nombreReporte(tipo)));
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = ch.getSelectedFile();
        try {
            switch (tipo) {
                case "TOP_VENDIDOS": {
                    util.ReportesUtil.ProductoVenta[] pv = util.ReportesUtil.topN(util.ReportesUtil.calcularVentasPorProducto(controladores.getPedidoController().listarTodos(), controladores.getProductoController().listarProductos()), 5);
                    util.PdfUtil.generarReporteTopVendidos(f, java.util.Arrays.asList(pv));
                    break;
                }
                case "MENOS_VENDIDOS": {
                    util.ReportesUtil.ProductoVenta[] pv = util.ReportesUtil.bottomN(util.ReportesUtil.calcularVentasPorProducto(controladores.getPedidoController().listarTodos(), controladores.getProductoController().listarProductos()), 5);
                    util.PdfUtil.generarReporteTopVendidos(f, java.util.Arrays.asList(pv));
                    break;
                }
                case "VENTAS_VENDEDOR": {
                    util.ReportesUtil.VendedorVenta[] vv = util.ReportesUtil.ventasPorVendedor(controladores.getPedidoController().listarTodos());
                    util.PdfUtil.generarReporteVentasPorVendedor(f, vv);
                    break;
                }
                case "CLIENTES_ACTIVOS": {
                    util.ReportesUtil.ClienteAct[] ca = util.ReportesUtil.clientesActivos(controladores.getPedidoController().listarTodos());
                    util.PdfUtil.generarReporteClientesActivos(f, ca);
                    break;
                }
                case "FINANCIERO": {
                    util.ReportesUtil.ResumenFinanciero r = util.ReportesUtil.resumenFinanciero(controladores.getPedidoController().listarTodos());
                    util.PdfUtil.generarReporteResumenFinanciero(f, r);
                    break;
                }
                case "POR_CADUCAR": {
                    util.ReportesUtil.ProductoPorCaducar[] pc = util.ReportesUtil.productosPorCaducar(controladores.getProductoController().listarProductos());
                    util.PdfUtil.generarReporteProductosPorCaducar(f, pc);
                    break;
                }
                case "INVENTARIO_CRITICO": {
                    String[] cods = controladores.getStockController().listarCodigos();
                    int[] stocks = controladores.getStockController().listarStocks();
                    util.ReportesUtil.StockEstado[] se = util.ReportesUtil.inventarioCritico(cods, stocks, controladores.getProductoController().listarProductos());
                    util.PdfUtil.generarReporteInventarioCritico(f, se);
                    break;
                }
                default:
                    JOptionPane.showMessageDialog(this, "Reporte generado (simple)");
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generando reporte: " + ex.getMessage());
        }
    }
}