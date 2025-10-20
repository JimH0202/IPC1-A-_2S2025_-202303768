package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.Pedido;
import modelo.Producto;
import modelo.ProductoAlimento;

/**
 * Utilidades para calcular estadísticas y reportes necesarios.
 */
public final class ReportesUtil {
    private ReportesUtil() {}

    public static class ProductoVenta {
        public String codigo;
        public String nombre;
        public String categoria;
        public int cantidad;
        public double ingresos;

        public ProductoVenta(String codigo, String nombre, String categoria) {
            this.codigo = codigo; this.nombre = nombre; this.categoria = categoria; this.cantidad = 0; this.ingresos = 0.0;
        }
    }

    public static ProductoVenta[] calcularVentasPorProducto(Pedido[] pedidos, Producto[] productos) {
        Map<String, ProductoVenta> mapa = new HashMap<>();
        if (productos != null) for (Producto p : productos) mapa.put(p.getCodigo(), new ProductoVenta(p.getCodigo(), p.getNombre(), p.getCategoria()));
        if (pedidos != null) {
            for (Pedido ped : pedidos) {
                for (Pedido.Linea l : ped.getLineas()) {
                    ProductoVenta pv = mapa.get(l.getCodigoProducto());
                    if (pv == null) pv = new ProductoVenta(l.getCodigoProducto(), l.getCodigoProducto(), "-");
                    pv.cantidad += l.getCantidad();
                    pv.ingresos += l.getCantidad() * l.getPrecioUnitario();
                    mapa.put(pv.codigo, pv);
                }
            }
        }
        List<ProductoVenta> list = new ArrayList<>(mapa.values());
        return list.toArray(new ProductoVenta[0]);
    }

    // Obtener top N por cantidad (selección simple para cumplir la regla de no usar Collections.sort())
    public static ProductoVenta[] topN(ProductoVenta[] arr, int n) {
        if (arr == null) return new ProductoVenta[0];
        ProductoVenta[] copy = new ProductoVenta[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);
        ProductoVenta[] out = new ProductoVenta[Math.min(n, copy.length)];
        for (int i = 0; i < out.length; i++) {
            int bestIdx = -1;
            for (int j = i; j < copy.length; j++) {
                if (copy[j] == null) continue;
                if (bestIdx == -1 || copy[j].cantidad > copy[bestIdx].cantidad) bestIdx = j;
            }
            if (bestIdx == -1) break;
            out[i] = copy[bestIdx];
            // marcar eliminado
            copy[bestIdx] = null;
        }
        return out;
    }

    public static ProductoVenta[] bottomN(ProductoVenta[] arr, int n) {
        if (arr == null) return new ProductoVenta[0];
        ProductoVenta[] copy = new ProductoVenta[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);
        ProductoVenta[] out = new ProductoVenta[Math.min(n, copy.length)];
        for (int i = 0; i < out.length; i++) {
            int bestIdx = -1;
            for (int j = 0; j < copy.length; j++) {
                if (copy[j] == null) continue;
                if (bestIdx == -1 || copy[j].cantidad < copy[bestIdx].cantidad) bestIdx = j;
            }
            if (bestIdx == -1) break;
            out[i] = copy[bestIdx];
            copy[bestIdx] = null;
        }
        return out;
    }

    public static class StockEstado {
        public String codigo;
        public String nombre;
        public String categoria;
        public int stock;
        public String estado; // Critico/Bajo/Ok
    }

    public static StockEstado[] inventarioCritico(String[] codigos, int[] stocks, Producto[] productos) {
        Map<String, Producto> mapP = new HashMap<>();
        if (productos != null) for (Producto p : productos) mapP.put(p.getCodigo(), p);
        List<StockEstado> out = new ArrayList<>();
        for (int i = 0; i < codigos.length; i++) {
            StockEstado s = new StockEstado();
            s.codigo = codigos[i];
            Producto p = mapP.get(codigos[i]);
            s.nombre = p == null ? codigos[i] : p.getNombre();
            s.categoria = p == null ? "-" : p.getCategoria();
            s.stock = stocks[i];
            if (s.stock < 10) s.estado = "CRITICO"; else if (s.stock <= 20) s.estado = "BAJO"; else s.estado = "OK";
            out.add(s);
        }
        return out.toArray(new StockEstado[0]);
    }

    /**
     * Ventas totales por vendedor: recorre pedidos confirmados y suma totales por vendedor
     */
    public static class VendedorVenta {
        public String codigoVendedor;
        public double totalVentas;
        public int cantidadPedidos;

        public VendedorVenta(String codigo) { this.codigoVendedor = codigo; this.totalVentas = 0.0; this.cantidadPedidos = 0; }
    }

    public static VendedorVenta[] ventasPorVendedor(Pedido[] pedidos) {
        java.util.Map<String, VendedorVenta> map = new java.util.HashMap<>();
        if (pedidos == null) return new VendedorVenta[0];
        for (Pedido p : pedidos) {
            if (!p.isConfirmado()) continue;
            String ven = p.getVendedorConfirmador();
            if (ven == null) ven = "-";
            VendedorVenta vv = map.get(ven);
            if (vv == null) { vv = new VendedorVenta(ven); map.put(ven, vv); }
            vv.totalVentas += p.getTotal();
            vv.cantidadPedidos += 1;
        }
        java.util.List<VendedorVenta> list = new java.util.ArrayList<>(map.values());
        return list.toArray(new VendedorVenta[0]);
    }

    /**
     * Clientes activos (por número de pedidos confirmados)
     */
    public static class ClienteAct {
        public String codigoCliente;
        public int pedidosConfirmados;
        public ClienteAct(String codigo) { this.codigoCliente = codigo; this.pedidosConfirmados = 0; }
    }

    public static ClienteAct[] clientesActivos(Pedido[] pedidos) {
        java.util.Map<String, ClienteAct> map = new java.util.HashMap<>();
        if (pedidos == null) return new ClienteAct[0];
        for (Pedido p : pedidos) {
            if (!p.isConfirmado()) continue;
            String c = p.getCodigoCliente();
            ClienteAct ca = map.get(c);
            if (ca == null) { ca = new ClienteAct(c); map.put(c, ca); }
            ca.pedidosConfirmados += 1;
        }
        return map.values().toArray(new ClienteAct[0]);
    }

    /**
     * Resumen financiero: total ingresos y promedio por pedido confirmado
     */
    public static class ResumenFinanciero {
        public double totalIngresos;
        public int pedidosConfirmados;
        public double promedioPorPedido;
    }

    public static ResumenFinanciero resumenFinanciero(Pedido[] pedidos) {
        ResumenFinanciero r = new ResumenFinanciero();
        if (pedidos == null) return r;
        for (Pedido p : pedidos) {
            if (!p.isConfirmado()) continue;
            r.totalIngresos += p.getTotal();
            r.pedidosConfirmados += 1;
        }
        if (r.pedidosConfirmados > 0) r.promedioPorPedido = r.totalIngresos / r.pedidosConfirmados;
        return r;
    }

    /**
     * Productos por caducar: filtra productos de tipo Alimento y devuelve días restantes
     */
    public static class ProductoPorCaducar {
        public String codigo;
        public String nombre;
        public long diasRestantes;
    }

    public static ProductoPorCaducar[] productosPorCaducar(Producto[] productos) {
        java.util.List<ProductoPorCaducar> out = new java.util.ArrayList<>();
        if (productos == null) return new ProductoPorCaducar[0];
        for (Producto p : productos) {
            if (p instanceof ProductoAlimento) {
                ProductoAlimento pa = (ProductoAlimento) p;
                ProductoPorCaducar pc = new ProductoPorCaducar();
                pc.codigo = pa.getCodigo(); pc.nombre = pa.getNombre();
                java.time.LocalDate hoy = java.time.LocalDate.now();
                java.time.LocalDate cad = pa.getFechaCaducidad();
                pc.diasRestantes = cad == null ? Long.MAX_VALUE : java.time.temporal.ChronoUnit.DAYS.between(hoy, cad);
                out.add(pc);
            }
        }
        return out.toArray(new ProductoPorCaducar[0]);
    }

}
