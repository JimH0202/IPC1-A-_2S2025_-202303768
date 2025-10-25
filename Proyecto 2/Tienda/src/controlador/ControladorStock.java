package controlador;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador simple para stock y precios usando vectores paralelos:
 * - códigos de producto
 * - stock int
 * - precio double
 *
 * Persistencia: stock.ser
 */
public class ControladorStock {
    private String[] codigos;
    private int[] stocks;
    private double[] precios;
    private int cantidad;
    private final File persistFile = new File("stock.ser");
    private final ControladorProducto productoController;
    private final ControladorBitacora bitacora;

    public ControladorStock(ControladorProducto productoController, ControladorBitacora bitacora) {
        this.productoController = productoController;
        this.bitacora = bitacora;
        this.codigos = new String[100];
        this.stocks = new int[100];
        this.precios = new double[100];
        this.cantidad = 0;
        cargarPersistencia();
    }

    public int getStock(String codigo) {
        int idx = indice(codigo);
        if (idx < 0) return 0;
        return stocks[idx];
    }

    /**
     * Devuelve copia de los códigos actuales (hasta cantidad).
     */
    public String[] listarCodigos() {
        String[] out = new String[cantidad];
        System.arraycopy(codigos, 0, out, 0, cantidad);
        return out;
    }

    /**
     * Devuelve copia de los stocks actuales (alineado con listarCodigos).
     */
    public int[] listarStocks() {
        int[] out = new int[cantidad];
        System.arraycopy(stocks, 0, out, 0, cantidad);
        return out;
    }

    /**
     * Devuelve copia de los precios actuales (alineado con listarCodigos).
     */
    public double[] listarPrecios() {
        double[] out = new double[cantidad];
        System.arraycopy(precios, 0, out, 0, cantidad);
        return out;
    }

    public double getPrecio(String codigo) {
        int idx = indice(codigo);
        if (idx < 0) return 0.0;
        return precios[idx];
    }

    public boolean setPrecio(String codigo, double precio) {
        int idx = indice(codigo);
        if (idx < 0) {
            agregarRegistro(codigo, 0, precio);
            return true;
        }
        precios[idx] = precio;
        guardarPersistencia();
        return true;
    }

    public boolean agregarStock(String codigo, int cantidadAgregar, String codigoUsuario) {
        if (cantidadAgregar == 0) return false; // no hay cambio
        int idx = indice(codigo);
        int antes = 0;
        if (idx < 0) {
            agregarRegistro(codigo, cantidadAgregar, 0.0);
            antes = 0;
            idx = indice(codigo);
        } else {
            antes = stocks[idx];
            stocks[idx] += cantidadAgregar;
        }
        int despues = (idx >= 0) ? stocks[idx] : cantidadAgregar;
        // registrar movimiento en CSV de historial
        registrarHistorial(codigo, cantidadAgregar, codigoUsuario);
        guardarPersistencia();
        String desc = String.format("Stock actualizado: Producto %s, Usuario %s, Antes=%d, Cambio=%+d, Ahora=%d", codigo, codigoUsuario, antes, cantidadAgregar, despues);
        // intentar inferir rol a partir del código de usuario para registros más descriptivos
        String tipo = "USUARIO";
        if (codigoUsuario != null) {
            String lc = codigoUsuario.toLowerCase();
            if (lc.startsWith("v")) tipo = "VENDEDOR";
            else if (lc.startsWith("c")) tipo = "CLIENTE";
            else if (lc.equals("admin") || lc.startsWith("a")) tipo = "ADMIN";
        }
        bitacora.registrar(tipo, codigoUsuario == null ? "-" : codigoUsuario, "AGREGAR_STOCK", "EXITOSA", desc);
        return true;
    }

    public util.CargaCSVResult cargarDesdeCSV(File file, String usuarioCodigo) {
        util.CargaCSVResult res = new util.CargaCSVResult();
        // Formato: codigo,cantidad
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line; int lineaNum = 0;
            while((line = br.readLine()) != null) {
                lineaNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                res.procesadas++;
                String[] parts = line.split(",");
                if (parts.length < 2) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": formato invalido"); continue; }
                String codigo = parts[0].trim();
                int cantidad = 0;
                try { cantidad = Integer.parseInt(parts[1].trim()); } catch (NumberFormatException ignored) { res.errores.add("Linea " + lineaNum + ": cantidad invalida, se uso 0"); }
                if (codigo.isEmpty()) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": codigo vacio"); continue; }
                agregarStock(codigo, cantidad, usuarioCodigo);
                res.aceptadas++;
            }
            // inferir rol del usuario que hizo la carga
            String tipo = "USUARIO";
            if (usuarioCodigo != null) {
                String lc = usuarioCodigo.toLowerCase();
                if (lc.startsWith("v")) tipo = "VENDEDOR";
                else if (lc.startsWith("c")) tipo = "CLIENTE";
                else if (lc.equals("admin") || lc.startsWith("a")) tipo = "ADMIN";
            }
            bitacora.registrar(tipo, usuarioCodigo == null ? "-" : usuarioCodigo, "CARGA_CSV_STOCK", "EXITOSA", file.getName() + " -> " + res.resumen());
        } catch (Exception e) {
            res.rechazadas++;
            res.errores.add("Error lectura: " + e.getMessage());
            String tipo2 = "USUARIO";
            if (usuarioCodigo != null) {
                String lc2 = usuarioCodigo.toLowerCase();
                if (lc2.startsWith("v")) tipo2 = "VENDEDOR";
                else if (lc2.startsWith("c")) tipo2 = "CLIENTE";
                else if (lc2.equals("admin") || lc2.startsWith("a")) tipo2 = "ADMIN";
            }
            bitacora.registrar(tipo2, usuarioCodigo == null ? "-" : usuarioCodigo, "CARGA_CSV_STOCK", "FALLIDA", file.getName() + " : " + e.getMessage());
        }
        return res;
    }

    private void registrarHistorial(String codigo, int cantidadAgregada, String usuario) {
        String nombreArchivo = "historial_ingresos.csv";
        DateTimeFormatter fDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fTime = DateTimeFormatter.ofPattern("HH:mm:ss");
        String fecha = LocalDateTime.now().format(fDate);
        String hora = LocalDateTime.now().format(fTime);
        // registrar el codigo del producto (no el nombre) para consistencia y trazabilidad
        String linea = fecha + "," + hora + "," + usuario + "," + codigo + "," + cantidadAgregada;
        try (FileWriter fw = new FileWriter(nombreArchivo, true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(linea);
            bw.newLine();
        } catch (Exception e) {
            // no interrumpir
        }
    }

    private void agregarRegistro(String codigo, int stockInicial, double precioInicial) {
        if (cantidad >= codigos.length) redimensionar();
        codigos[cantidad] = codigo;
        stocks[cantidad] = stockInicial;
        precios[cantidad] = precioInicial;
        cantidad++;
    }

    private int indice(String codigo) {
        for (int i = 0; i < cantidad; i++) if (codigos[i].equalsIgnoreCase(codigo)) return i;
        return -1;
    }

    /* Persistencia */
    private void guardarPersistencia() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(persistFile))) {
            oos.writeInt(cantidad);
            for (int i = 0; i < cantidad; i++) {
                oos.writeObject(codigos[i]);
                oos.writeInt(stocks[i]);
                oos.writeDouble(precios[i]);
            }
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "PERSISTENCIA_STOCK", "FALLIDA", e.getMessage());
        }
    }

    private void cargarPersistencia() {
        if (!persistFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(persistFile))) {
            int n = ois.readInt();
            for (int i = 0; i < n; i++) {
                codigos[i] = (String) ois.readObject();
                stocks[i] = ois.readInt();
                precios[i] = ois.readDouble();
            }
            cantidad = n;
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "CARGA_PERSISTENCIA_STOCK", "FALLIDA", e.getMessage());
        }
    }

    private void redimensionar() {
        String[] nc = new String[codigos.length * 2];
        int[] ns = new int[stocks.length * 2];
        double[] np = new double[precios.length * 2];
        System.arraycopy(codigos, 0, nc, 0, codigos.length);
        System.arraycopy(stocks, 0, ns, 0, stocks.length);
        System.arraycopy(precios, 0, np, 0, precios.length);
        codigos = nc; stocks = ns; precios = np;
    }
}