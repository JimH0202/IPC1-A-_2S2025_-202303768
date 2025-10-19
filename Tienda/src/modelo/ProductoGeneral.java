package modelo;

public class ProductoGeneral extends Producto {
    private static final long serialVersionUID = 1L;

    private String material;

    public ProductoGeneral(String codigo, String nombre, String material) {
        super(codigo, nombre, "Generales");
        this.material = material;
    }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    @Override
    public String getDetalle() {
        return "Material: " + (material == null ? "" : material);
    }
}