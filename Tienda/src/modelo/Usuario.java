package modelo;

import java.io.Serializable;

public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String codigo;
    protected String nombre;
    protected String genero;
    protected String passwordHash; // formato salt:hash (Base64)

    public Usuario(String codigo, String nombre, String genero, String contrasena) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.genero = genero;
        setPassword(contrasena);
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getGenero() { return genero; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public void setPassword(String contrasena) {
        if (contrasena == null) this.passwordHash = null; else this.passwordHash = util.PasswordUtil.hashPassword(contrasena);
    }

    public boolean verifyPassword(String contrasena) {
        if (this.passwordHash == null) return false;
        return util.PasswordUtil.verifyPassword(contrasena, this.passwordHash);
    }

    public abstract String getRol();

    @Override
    public String toString() {
        return String.format("%s[%s] - %s", getRol(), codigo, nombre);
    }
}