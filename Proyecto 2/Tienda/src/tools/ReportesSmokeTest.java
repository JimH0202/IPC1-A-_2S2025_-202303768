package tools;

import controlador.Controladores;
import modelo.ProductoAlimento;
import modelo.ProductoGeneral;
import modelo.ProductoTecnologia;
import modelo.Pedido;
import modelo.Pedido.Linea;
import modelo.Vendedor;
import util.PdfUtil;
import util.ReportesUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportesSmokeTest {
    public static void main(String[] args) throws Exception {
        Controladores c = new Controladores();
        // crear algunos productos
        c.getProductoController().crearProducto(new ProductoTecnologia("T001","Teléfono",24));
        c.getProductoController().crearProducto(new ProductoGeneral("G001","Lapicero","Plástico"));
        c.getProductoController().crearProducto(new ProductoAlimento("A001","Leche", LocalDate.now().plusDays(10)));

        // setear stock y precios
        c.getStockController().agregarStock("T001", 10, "admin"); c.getStockController().setPrecio("T001", 250.0);
        c.getStockController().agregarStock("G001", 50, "admin"); c.getStockController().setPrecio("G001", 1.5);
        c.getStockController().agregarStock("A001", 20, "admin"); c.getStockController().setPrecio("A001", 0.95);

        // crear vendedores y usuarios
        c.getUsuarioController().agregarUsuario(new Vendedor("V001","Juan","M","pass"));

        // crear pedido
        Pedido p = new Pedido("PE-001","19/10/2025 12:00:00","C001");
        p.agregarLinea("T001",1,250.0); p.agregarLinea("A001",2,0.95);
        p.setTotal(251.9); p.setConfirmado(true); p.setVendedorConfirmador("V001");

        // generar reportes
        File outDir = new File("out_reports"); outDir.mkdirs();
        PdfUtil.generarReporteProductos(new File(outDir, "productos_test.pdf"), c.getProductoController().listarProductos());
        PdfUtil.generarReportePedidos(new File(outDir, "pedidos_test.pdf"), new Pedido[]{p});
        ReportesUtil.ProductoVenta[] ventas = ReportesUtil.calcularVentasPorProducto(new Pedido[]{p}, c.getProductoController().listarProductos());
        PdfUtil.generarReporteTopVendidos(new File(outDir, "top_test.pdf"), java.util.Arrays.asList(ReportesUtil.topN(ventas,5)));
        PdfUtil.generarReporteVentasPorVendedor(new File(outDir, "ventas_vendedor_test.pdf"), ReportesUtil.ventasPorVendedor(new Pedido[]{p}));
        PdfUtil.generarReporteClientesActivos(new File(outDir, "clientes_test.pdf"), ReportesUtil.clientesActivos(new Pedido[]{p}));
        PdfUtil.generarReporteResumenFinanciero(new File(outDir, "financiero_test.pdf"), ReportesUtil.resumenFinanciero(new Pedido[]{p}));
        PdfUtil.generarReporteProductosPorCaducar(new File(outDir, "por_caducar_test.pdf"), ReportesUtil.productosPorCaducar(c.getProductoController().listarProductos()));
        PdfUtil.generarReporteInventarioCritico(new File(outDir, "inventario_test.pdf"), ReportesUtil.inventarioCritico(c.getStockController().listarCodigos(), c.getStockController().listarStocks(), c.getProductoController().listarProductos()));

        System.out.println("Reportes generados en: " + outDir.getAbsolutePath());
    }
}
