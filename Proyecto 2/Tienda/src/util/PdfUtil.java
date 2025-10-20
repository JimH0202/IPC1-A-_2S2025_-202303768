package util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import modelo.Pedido;
import modelo.Producto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Util para generar reportes PDF usando iText 5.5.12 (ya incluido en el proyecto).
 * Provee métodos para generar reporte de productos y reporte de pedidos.
 */
public final class PdfUtil {
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private PdfUtil() {}

    private static void addReportHeader(Document doc, String titulo) throws DocumentException {
        Paragraph title = new Paragraph(titulo, TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        Paragraph fecha = new Paragraph("Generado: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), NORMAL);
        fecha.setAlignment(Element.ALIGN_CENTER);
        doc.add(fecha);
        doc.add(new Paragraph(" ", NORMAL));
    }

    public static void generarReporteProductos(java.io.File destino, Producto[] productos) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
    addReportHeader(doc, "Reporte de Productos");

        PdfPTable table = new PdfPTable(new float[]{2, 5, 3, 6});
        table.setWidthPercentage(100);
        addHeaderCell(table, "Código");
        addHeaderCell(table, "Nombre");
        addHeaderCell(table, "Categoría");
        addHeaderCell(table, "Detalle");

        if (productos != null) {
            for (Producto p : productos) {
                addCell(table, p.getCodigo());
                addCell(table, p.getNombre());
                addCell(table, p.getCategoria());
                addCell(table, p.getDetalle());
            }
        }

        doc.add(table);
        doc.close();
    }

    // Nombre de archivo: DD_MM_YYYY_HH_mm_ss_TipoReporte.pdf
    public static String nombreReporte(String tipo) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
        String ts = java.time.LocalDateTime.now().format(f);
        return ts + "_" + tipo + ".pdf";
    }

    public static void generarReporteTopVendidos(java.io.File destino, java.util.List<util.ReportesUtil.ProductoVenta> top) throws Exception {
        Document doc = new Document(PageSize.A4, 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
    addReportHeader(doc, "Top Productos Más Vendidos");
        PdfPTable table = new PdfPTable(new float[]{6,3,3});
        table.setWidthPercentage(100);
        addHeaderCell(table, "Nombre");
        addHeaderCell(table, "Cantidad Vendida");
        addHeaderCell(table, "Ingresos");
        for (util.ReportesUtil.ProductoVenta pv : top) {
            addCell(table, pv.nombre);
            addCell(table, String.valueOf(pv.cantidad));
            addCell(table, DF.format(pv.ingresos));
        }
        doc.add(table);
        doc.close();
    }

    public static void generarReportePedidos(java.io.File destino, Pedido[] pedidos) throws Exception {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();

    addReportHeader(doc, "Reporte de Pedidos");

        if (pedidos != null) {
            for (Pedido p : pedidos) {
                Paragraph pHeader = new Paragraph(String.format("Pedido: %s   Fecha: %s   Cliente: %s   Total: %s", p.getCodigo(), p.getFechaHora(), p.getCodigoCliente(), DF.format(p.getTotal())), NORMAL);
                doc.add(pHeader);
                PdfPTable table = new PdfPTable(new float[]{3, 2, 2});
                table.setWidthPercentage(100);
                addHeaderCell(table, "Producto");
                addHeaderCell(table, "Cantidad");
                addHeaderCell(table, "Precio Unit.");
                for (Pedido.Linea l : p.getLineas()) {
                    addCell(table, l.getCodigoProducto());
                    addCell(table, String.valueOf(l.getCantidad()));
                    addCell(table, DF.format(l.getPrecioUnitario()));
                }
                doc.add(table);
                doc.add(new Paragraph(" ", NORMAL));
            }
        }

        doc.close();
    }

    /**
     * Versión que recibe controladores para resolver nombres (cliente, producto, vendedor)
     */
    public static void generarReportePedidos(java.io.File destino, controlador.Controladores controladores, Pedido[] pedidos) throws Exception {
        // reutiliza la anterior pero traduce códigos a nombres
        if (pedidos == null) { generarReportePedidos(destino, pedidos); return; }
        // crear tabla con más detalle: pedido (código, fecha, cliente nombre, vendedor), luego lineas con nombre producto, cantidad, precio unit.
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
        addReportHeader(doc, "Reporte de Pedidos (detallado)");
        for (Pedido p : pedidos) {
            String nombreCliente = p.getCodigoCliente();
            modelo.Usuario u = controladores.getUsuarioController().buscarPorCodigo(p.getCodigoCliente());
            if (u != null) nombreCliente = u.getNombre();
            String vendedor = p.getVendedorConfirmador() == null ? "-" : p.getVendedorConfirmador();
            modelo.Usuario uv = controladores.getUsuarioController().buscarPorCodigo(p.getVendedorConfirmador());
            if (uv != null) vendedor = uv.getNombre();
            Paragraph pHeader = new Paragraph(String.format("Pedido: %s   Fecha: %s   Cliente: %s   Vendedor: %s   Total: %s", p.getCodigo(), p.getFechaHora(), nombreCliente, vendedor, DF.format(p.getTotal())), NORMAL);
            doc.add(pHeader);
            PdfPTable table = new PdfPTable(new float[]{4, 2, 2, 2});
            table.setWidthPercentage(100);
            addHeaderCell(table, "Producto");
            addHeaderCell(table, "Nombre Producto");
            addHeaderCell(table, "Cantidad");
            addHeaderCell(table, "Precio Unit.");
            for (Pedido.Linea l : p.getLineas()) {
                addCell(table, l.getCodigoProducto());
                String prodName = l.getCodigoProducto();
                modelo.Producto prod = controladores.getProductoController().buscarProducto(l.getCodigoProducto());
                if (prod != null) prodName = prod.getNombre();
                addCell(table, prodName);
                addCell(table, String.valueOf(l.getCantidad()));
                addCell(table, DF.format(l.getPrecioUnitario()));
            }
            doc.add(table);
            doc.add(new Paragraph(" ", NORMAL));
        }
        doc.close();
    }

    public static void generarReporteVentasPorVendedor(java.io.File destino, util.ReportesUtil.VendedorVenta[] ventas) throws Exception {
        Document doc = new Document(PageSize.A4, 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
    addReportHeader(doc, "Ventas por Vendedor");
        PdfPTable table = new PdfPTable(new float[]{6,3,3});
        table.setWidthPercentage(100);
        addHeaderCell(table, "Vendedor"); addHeaderCell(table, "Pedidos"); addHeaderCell(table, "Ingresos");
        if (ventas != null) for (util.ReportesUtil.VendedorVenta v : ventas) { addCell(table, v.codigoVendedor); addCell(table, String.valueOf(v.cantidadPedidos)); addCell(table, DF.format(v.totalVentas)); }
        doc.add(table);
        doc.close();
    }

    public static void generarReporteClientesActivos(java.io.File destino, util.ReportesUtil.ClienteAct[] clientes) throws Exception {
        Document doc = new Document(PageSize.A4, 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
    addReportHeader(doc, "Clientes Activos");
        PdfPTable table = new PdfPTable(new float[]{6,3}); table.setWidthPercentage(100);
        addHeaderCell(table, "Cliente"); addHeaderCell(table, "Pedidos Confirmados");
        if (clientes != null) for (util.ReportesUtil.ClienteAct c : clientes) { addCell(table, c.codigoCliente); addCell(table, String.valueOf(c.pedidosConfirmados)); }
        doc.add(table); doc.close();
    }

    public static void generarReporteResumenFinanciero(java.io.File destino, util.ReportesUtil.ResumenFinanciero r) throws Exception {
        Document doc = new Document(PageSize.A4, 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
    addReportHeader(doc, "Resumen Financiero");
        doc.add(new Paragraph("Total ingresos: " + DF.format(r.totalIngresos), NORMAL));
        doc.add(new Paragraph("Pedidos confirmados: " + r.pedidosConfirmados, NORMAL));
        doc.add(new Paragraph("Promedio por pedido: " + DF.format(r.promedioPorPedido), NORMAL));
        doc.close();
    }

    public static void generarReporteProductosPorCaducar(java.io.File destino, util.ReportesUtil.ProductoPorCaducar[] arr) throws Exception {
        Document doc = new Document(PageSize.A4, 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
    addReportHeader(doc, "Productos por caducar");
        PdfPTable table = new PdfPTable(new float[]{3,6,3}); table.setWidthPercentage(100);
        addHeaderCell(table, "Codigo"); addHeaderCell(table, "Nombre"); addHeaderCell(table, "Días restantes");
        if (arr != null) for (util.ReportesUtil.ProductoPorCaducar p : arr) { addCell(table, p.codigo); addCell(table, p.nombre); addCell(table, String.valueOf(p.diasRestantes)); }
        doc.add(table); doc.close();
    }

    public static void generarReporteInventarioCritico(java.io.File destino, util.ReportesUtil.StockEstado[] arr) throws Exception {
        Document doc = new Document(PageSize.A4, 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
    addReportHeader(doc, "STOCK Inventario");
        PdfPTable table = new PdfPTable(new float[]{3,6,3,3}); table.setWidthPercentage(100);
        addHeaderCell(table, "Codigo"); addHeaderCell(table, "Nombre"); addHeaderCell(table, "Stock"); addHeaderCell(table, "Estado");
        if (arr != null) for (util.ReportesUtil.StockEstado s : arr) { addCell(table, s.codigo); addCell(table, s.nombre); addCell(table, String.valueOf(s.stock)); addCell(table, s.estado); }
        doc.add(table); doc.close();
    }

    /**
     * Genera un PDF simple con las líneas de la bitácora.
     */
    public static void generarReporteBitacora(java.io.File destino, java.util.List<String> lineas) throws Exception {
        Document doc = new Document(PageSize.A4, 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
        addReportHeader(doc, "Bitácora del Sistema");
        if (lineas != null) {
            for (String l : lineas) {
                Paragraph p = new Paragraph(l == null ? "" : l, NORMAL);
                doc.add(p);
            }
        }
        doc.close();
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(6);
        table.addCell(c);
    }

    private static void addCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, NORMAL));
        c.setPadding(4);
        table.addCell(c);
    }

    /**
     * Generador genérico de tablas para reportes: recibe encabezados y filas (cada fila es String[])
     */
    public static void generarReporteTabla(java.io.File destino, String titulo, String[] headers, java.util.List<String[]> filas) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 36,36,36,36);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
        addReportHeader(doc, titulo);
        if (headers == null) headers = new String[0];
        PdfPTable table = new PdfPTable(headers.length > 0 ? headers.length : 1);
        table.setWidthPercentage(100);
        for (String h : headers) addHeaderCell(table, h);
        if (filas != null) {
            for (String[] row : filas) {
                if (row == null) continue;
                for (int i = 0; i < headers.length; i++) {
                    String v = i < row.length ? row[i] : "";
                    addCell(table, v);
                }
            }
        }
        doc.add(table);
        doc.close();
    }
}
