package vista;

import java.awt.*;
import javax.swing.*;

/** Diálogo para mostrar datos del estudiante que desarrolló el proyecto. */
public class DialogDatosEstudiante extends JDialog {
    public DialogDatosEstudiante(Window owner) {
        super(owner, "Datos del Estudiante", ModalityType.APPLICATION_MODAL);
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JTextArea ta = new JTextArea(); ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        StringBuilder sb = new StringBuilder();
        sb.append("Laboratorio: Introducción a la Programación y Computación 1\n");
        sb.append("Sección:[A] 2S2025\n");
        sb.append("Nombre: [Jimmy Brian Hurtarte López]\n");
        sb.append("Carnet: [202303768]\n");
        sb.append("GitHub: [JimH0202]\n");
        sb.append("Proyecto 2: Tienda - SANCARLISTA SHOP\n");
        ta.setText(sb.toString());
        add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("Cerrar"); btn.addActionListener(e -> dispose()); bot.add(btn);
        add(bot, BorderLayout.SOUTH);
    }
}
