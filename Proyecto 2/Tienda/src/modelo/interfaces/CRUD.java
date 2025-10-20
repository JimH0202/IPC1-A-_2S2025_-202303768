package modelo.interfaces;

/**
 * Interfaz genérica que define operaciones básicas CRUD.
 * Implementaciones pueden adaptar firmas según necesiten tipos concretos.
 */
public interface CRUD {
    boolean crear(Object obj);
    boolean actualizar(String codigo, Object... datos);
    boolean eliminar(String codigo);
    Object buscar(String codigo);
    Object[] listar();
}