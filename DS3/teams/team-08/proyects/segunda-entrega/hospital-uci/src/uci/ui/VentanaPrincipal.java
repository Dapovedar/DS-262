package uci.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;
import uci.servicio.Aplicacion;

/** Ventana unica de la aplicacion; cambia entre login, funcionario y paciente. */
public class VentanaPrincipal extends JFrame implements Navegador {

    private final Aplicacion app;
    private final CardLayout cartas = new CardLayout();
    private final JPanel raiz = new JPanel(cartas);
    private final PanelLogin login;
    private JPanel panelActual;

    public VentanaPrincipal(Aplicacion app) {
        super(Estilo.HOSPITAL + " — UCI");
        this.app = app;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        login = new PanelLogin(this, app.sistema, app.autenticacion);
        raiz.add(login, "login");
        setContentPane(raiz);
    }

    @Override
    public void mostrarLogin() {
        quitarPanelActual();
        cartas.show(raiz, "login");
    }

    @Override
    public void mostrarFuncionario(Funcionario funcionario) {
        mostrar(new PanelFuncionario(this, app.sistema, funcionario));
    }

    @Override
    public void mostrarPaciente(Paciente paciente) {
        mostrar(new PanelPaciente(this, app.sistema, paciente));
    }

    private void mostrar(JPanel panel) {
        quitarPanelActual();
        panelActual = panel;
        raiz.add(panel, "sesion");
        cartas.show(raiz, "sesion");
        raiz.revalidate();
        raiz.repaint();
    }

    private void quitarPanelActual() {
        if (panelActual != null) {
            raiz.remove(panelActual);
            panelActual = null;
        }
    }
}
