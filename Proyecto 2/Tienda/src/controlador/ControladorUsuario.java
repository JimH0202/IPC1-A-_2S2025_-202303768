package controlador;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import modelo.Administrador;
import modelo.Usuario;
import modelo.Vendedor;

/**
 * Controlador básico de usuarios usando arrays (vectores) y serialización simple.
 * Archivo persistente: usuarios.ser
 * Formato CSV para carga masiva: código,nombre,genero,contrasena (vendedores/ clientes según contexto)
 */
public class ControladorUsuario {
    private final List<Usuario> usuarios;
    private final ControladorBitacora bitacora;
    private final File persistFile = new File("usuarios.ser");

    public ControladorUsuario(ControladorBitacora bitacora) {
        this.bitacora = bitacora;
        this.usuarios = new ArrayList<>();
        cargarPersistencia();
        asegurarAdminPorDefecto();
    }

    private void asegurarAdminPorDefecto() {
        // Si no existe administrador con código "admin" lo crea con contraseña IPC1A
        if (buscarPorCodigo("admin") == null) {
            Administrador admin = new Administrador("admin", "Administrador", "M", "IPC1A");
            agregarUsuario(admin);
            bitacora.registrar("ADMIN", "admin", "CREAR_USUARIO", "EXITOSA", "Admin por defecto creado");
        }
    }

    public Usuario autenticar(String codigo, String contrasena) {
        Usuario u = buscarPorCodigo(codigo);
        if (u == null) {
            bitacora.registrar("SISTEMA", codigo, "LOGIN", "FALLIDA", "Usuario no encontrado");
            return null;
        }
        if (u.verifyPassword(contrasena)) {
            bitacora.registrar(u.getRol(), u.getCodigo(), "LOGIN", "EXITOSA", "Inicio de sesión correcto");
            return u;
        } else {
            bitacora.registrar(u.getRol(), u.getCodigo(), "LOGIN", "FALLIDA", "Contraseña incorrecta");
            return null;
        }
    }

    public boolean agregarUsuario(Usuario u) {
        if (buscarPorCodigo(u.getCodigo()) != null) return false;
        usuarios.add(u);
        guardarPersistencia();
        bitacora.registrar(u.getRol(), u.getCodigo(), "CREAR_USUARIO", "EXITOSA", "Usuario creado");
        return true;
    }

    public boolean actualizarUsuario(String codigo, String nuevoNombre, String nuevaContrasena) {
        Usuario u = buscarPorCodigo(codigo);
        if (u == null) return false;
        u.setNombre(nuevoNombre);
        u.setPassword(nuevaContrasena);
        guardarPersistencia();
        bitacora.registrar(u.getRol(), u.getCodigo(), "ACTUALIZAR_USUARIO", "EXITOSA", "Usuario actualizado");
        return true;
    }

    public boolean eliminarUsuario(String codigo) {
        Usuario u = buscarPorCodigo(codigo);
        if (u == null) return false;
        usuarios.remove(u);
        guardarPersistencia();
        bitacora.registrar(u.getRol(), u.getCodigo(), "ELIMINAR_USUARIO", "EXITOSA", "Usuario eliminado");
        return true;
    }

    /**
     * Incrementa el contador de ventas confirmadas de un vendedor y persiste el cambio.
     */
    public boolean incrementarVentasConfirmadas(String codigoVendedor, int delta) {
        Usuario u = buscarPorCodigo(codigoVendedor);
        if (u == null || !(u instanceof Vendedor)) return false;
        Vendedor v = (Vendedor) u;
        v.incrementarVentasConfirmadas(delta);
        guardarPersistencia();
        bitacora.registrar(v.getRol(), v.getCodigo(), "INCREMENTAR_VENTAS", "EXITOSA", "+" + delta);
        return true;
    }

    public Usuario buscarPorCodigo(String codigo) {
        for (Usuario u : usuarios) if (u.getCodigo().equalsIgnoreCase(codigo)) return u;
        return null;
    }

    public Usuario[] listarUsuariosPorTipo(Class<?> tipo) {
        List<Usuario> out = new ArrayList<>();
        for (Usuario u : usuarios) if (tipo.isInstance(u)) out.add(u);
        return out.toArray(new Usuario[0]);
    }

    public util.CargaCSVResult cargarVendedoresDesdeCSV(File file) {
        util.CargaCSVResult res = new util.CargaCSVResult();
        // Formato: Código, Nombre, Género, Contraseña, ventasConfirmadas (opcional)
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line; int lineaNum = 0;
            while ((line = br.readLine()) != null) {
                lineaNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                res.procesadas++;
                String[] parts = line.split(",");
                if (parts.length < 4) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": formato invalido"); continue; }
                String codigo = parts[0].trim();
                String nombre = parts[1].trim();
                String genero = parts[2].trim();
                String contrasena = parts[3].trim();
                if (codigo.isEmpty() || nombre.isEmpty() || contrasena.isEmpty()) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": campos vacios"); continue; }
                if (buscarPorCodigo(codigo) != null) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": codigo ya existe: " + codigo); continue; }
                Vendedor v = new Vendedor(codigo, nombre, genero, contrasena);
                // si existe una quinta columna, interpretarla como ventas confirmadas
                if (parts.length >= 5) {
                    String s = parts[4].trim();
                    if (!s.isEmpty()) {
                        try {
                            int vc = Integer.parseInt(s);
                            v.setVentasConfirmadas(vc);
                        } catch (NumberFormatException ex) {
                            res.errores.add("Linea " + lineaNum + ": ventasConfirmadas invalido, se uso 0");
                        }
                    }
                }
                agregarUsuario(v);
                res.aceptadas++;
            }
            bitacora.registrar("ADMIN", "admin", "CARGA_CSV_VENDEDORES", "EXITOSA", file.getName() + " -> " + res.resumen());
        } catch (Exception e) {
            res.rechazadas++;
            res.errores.add("Error lectura: " + e.getMessage());
            bitacora.registrar("ADMIN", "admin", "CARGA_CSV_VENDEDORES", "FALLIDA", file.getName() + " : " + e.getMessage());
        }
        return res;
    }

    public util.CargaCSVResult cargarClientesDesdeCSV(File file) {
        util.CargaCSVResult res = new util.CargaCSVResult();
        // Formato: Código, Nombre, Género, Cumpleaños(dd/MM/yyyy), Contraseña
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line; int lineaNum = 0;
            while ((line = br.readLine()) != null) {
                lineaNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                res.procesadas++;
                String[] parts = line.split(",");
                if (parts.length < 5) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": formato invalido"); continue; }
                String codigo = parts[0].trim();
                String nombre = parts[1].trim();
                String genero = parts[2].trim();
                java.time.LocalDate fecha = java.time.LocalDate.now();
                try { fecha = java.time.LocalDate.parse(parts[3].trim(), f); } catch (Exception ignored) {}
                String contrasena = parts[4].trim();
                if (codigo.isEmpty() || nombre.isEmpty()) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": codigo/nombre vacio"); continue; }
                if (buscarPorCodigo(codigo) != null) { res.rechazadas++; res.errores.add("Linea " + lineaNum + ": codigo ya existe: " + codigo); continue; }
                modelo.Cliente c = new modelo.Cliente(codigo, nombre, genero, fecha, contrasena);
                agregarUsuario(c);
                res.aceptadas++;
            }
            bitacora.registrar("ADMIN", "admin", "CARGA_CSV_CLIENTES", "EXITOSA", file.getName() + " -> " + res.resumen());
        } catch (Exception e) {
            res.rechazadas++;
            res.errores.add("Error lectura: " + e.getMessage());
            bitacora.registrar("ADMIN", "admin", "CARGA_CSV_CLIENTES", "FALLIDA", file.getName() + " : " + e.getMessage());
        }
        return res;
    }

    public Usuario[] listarTodosUsuarios() {
        return usuarios.toArray(new Usuario[0]);
    }

    /* Persistencia simple por serialización */
    private void guardarPersistencia() {
        try {
            util.Serializador.guardar(new ArrayList<>(usuarios), persistFile);
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "PERSISTENCIA_USUARIOS", "FALLIDA", e.getMessage());
        }
    }

    private void cargarPersistencia() {
        if (!persistFile.exists()) return;
        try {
            Object obj = util.Serializador.cargar(persistFile);
            if (obj instanceof List) {
                List<?> l = (List<?>) obj;
                for (Object o : l) if (o instanceof Usuario) usuarios.add((Usuario) o);
            }
        } catch (Exception e) {
            bitacora.registrar("SISTEMA", "admin", "CARGA_PERSISTENCIA_USUARIOS", "FALLIDA", e.getMessage());
        }
    }
    
}