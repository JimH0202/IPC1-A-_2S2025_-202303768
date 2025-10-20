package vista;

import controlador.ControladorProducto;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import modelo.Producto;
import modelo.ProductoAlimento;
import modelo.ProductoGeneral;
import modelo.ProductoTecnologia;

public class DialogCrearProducto extends JDialog {
    private final ControladorProducto controlador;

    public DialogCrearProducto(Window owner, ControladorProducto controlador) {
        super(owner, "Crear producto", ModalityType.APPLICATION_MODAL);
        this.controlador = controlador;
        setSize(480, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(6,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JTextField tfCodigo = new JTextField();
        JTextField tfNombre = new JTextField();
        JComboBox<String> cbCategoria = new JComboBox<>(new String[]{"Tecnologia","Alimento","Generales"});
        JTextField tfAtributo = new JTextField();
    JTextField tfPrecio = new JTextField();

        form.add(new JLabel("Código:")); form.add(tfCodigo);
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Categoría:")); form.add(cbCategoria);
        form.add(new JLabel("Atributo (según categoría):")); form.add(tfAtributo);
    form.add(new JLabel("Precio:")); form.add(tfPrecio);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCrear = new JButton("Crear");
        JButton btnCerrar = new JButton("Cancelar");
        bottom.add(btnCrear); bottom.add(btnCerrar);
        add(bottom, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> dispose());

        btnCrear.addActionListener(e -> {
            String codigo = tfCodigo.getText().trim();
            String nombre = tfNombre.getText().trim();
            String categoria = (String) cbCategoria.getSelectedItem();
            String atributo = tfAtributo.getText().trim();
            if (codigo.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Código y nombre son obligatorios");
                return;
            }
            Producto p = null;
            try {
                if (categoria.equalsIgnoreCase("Tecnologia")) {
                    int meses = Integer.parseInt(atributo);
                    p = new ProductoTecnologia(codigo, nombre, meses);
                } else if (categoria.equalsIgnoreCase("Alimento")) {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate fecha = LocalDate.parse(atributo, f);
                    p = new ProductoAlimento(codigo, nombre, fecha);
                } else {
                    p = new ProductoGeneral(codigo, nombre, atributo);
                }
                boolean ok = controlador.crearProducto(p);
                if (ok) {
                    try {
                        double pr = Double.parseDouble(tfPrecio.getText().trim());
                        controlador.setPrecioProducto(p.getCodigo(), pr);
                    } catch (Exception ignored) {}
                    dispose();
                } else JOptionPane.showMessageDialog(this, "Código ya existe");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
