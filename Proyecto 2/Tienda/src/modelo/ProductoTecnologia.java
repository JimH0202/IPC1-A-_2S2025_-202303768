package modelo;

public class ProductoTecnologia extends Producto {
    private static final long serialVersionUID = 1L;

    private int mesesGarantia;

    public ProductoTecnologia(String codigo, String nombre, int mesesGarantia) {
        super(codigo, nombre, "Tecnologia");
        this.mesesGarantia = mesesGarantia;
    }

    public int getMesesGarantia() { return mesesGarantia; }
    public void setMesesGarantia(int mesesGarantia) { this.mesesGarantia = mesesGarantia; }

    @Override
    public String getDetalle() {
        return "Meses de garantía: " + mesesGarantia;
    }
}