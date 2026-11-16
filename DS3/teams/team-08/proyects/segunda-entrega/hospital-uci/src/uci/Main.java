package uci;

import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import uci.servicio.Aplicacion;
import uci.ui.ConsolaUCI;
import uci.ui.VentanaPrincipal;

/**
 * Hospital Universitario Nacional de Colombia - Unidad de Cuidados Intensivos.
 * Sin argumentos abre la interfaz grafica; con --consola abre la version de consola.
 */
public class Main {

    public static void main(String[] args) {
        boolean consola = args.length > 0 && args[0].equals("--consola");
        Aplicacion app;
        try {
            app = Aplicacion.cargar(Path.of("data"));
        } catch (Exception e) {
            String mensaje = "No se pudo cargar la base de datos:\n" + e.getMessage();
            if (consola) {
                System.err.println(mensaje);
            } else {
                JOptionPane.showMessageDialog(null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        if (consola) {
            new ConsolaUCI(app).ejecutar();
        } else {
            SwingUtilities.invokeLater(() -> new VentanaPrincipal(app).setVisible(true));
        }
    }
}
