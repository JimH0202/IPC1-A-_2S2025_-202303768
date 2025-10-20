package controlador;

public class Controladores {
    private final ControladorUsuario usuarioController;
    private final ControladorProducto productoController;
    private final ControladorStock stockController;
    private final ControladorPedido pedidoController;
    private final ControladorBitacora bitacoraController;

    public Controladores() {
        this.bitacoraController = new ControladorBitacora();
        this.usuarioController = new ControladorUsuario(bitacoraController);
        this.productoController = new ControladorProducto(bitacoraController);
        this.stockController = new ControladorStock(productoController, bitacoraController);
        this.pedidoController = new ControladorPedido(productoController, stockController, usuarioController, bitacoraController);
    }

    public ControladorUsuario getUsuarioController() { return usuarioController; }
    public ControladorProducto getProductoController() { return productoController; }
    public ControladorStock getStockController() { return stockController; }
    public ControladorPedido getPedidoController() { return pedidoController; }
    public ControladorBitacora getBitacoraController() { return bitacoraController; }

    /**
     * Devuelve un objeto con referencias a controladores para pasar a la vista principal.
     */
    public Controladores getControladores() { return this; }
}