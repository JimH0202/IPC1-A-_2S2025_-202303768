package modelo;

public class Administrador extends Usuario {
    private static final long serialVersionUID = 1L;

    public Administrador(String codigo, String nombre, String genero, String contrasena) {
        super(codigo, nombre, genero, contrasena);
    }

    @Override
    public String getRol() {
        return "ADMINISTRADOR";
    }
}