package uci.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;
import uci.modelo.Validador;
import uci.servicio.Autenticacion;
import uci.servicio.SistemaUCI;

/** Pantalla de inicio de sesion: paciente (solo ID) o funcionario (ID y clave). */
public class PanelLogin extends JPanel {

    private final Navegador navegador;
    private final SistemaUCI sistema;
    private final Autenticacion autenticacion;

    private boolean modoFuncionario = false;
    private final Boton btnPaciente = new Boton("Soy paciente", Boton.Tipo.PRIMARIO);
    private final Boton btnFuncionario = new Boton("Soy funcionario", Boton.Tipo.SECUNDARIO);
    private final Boton btnIngresar = new Boton("Ingresar", Boton.Tipo.PRIMARIO);
    private final JTextField txtId = Estilo.campo(20);
    private final JPasswordField txtClave = Estilo.campoClave(20);
    private final JLabel lblClave = Estilo.etiqueta("Clave", Font.BOLD, 13, Estilo.TEXTO);
    private final JLabel lblAyuda = Estilo.etiqueta(" ", Font.PLAIN, 13, Estilo.TEXTO_SUAVE);
    private final JLabel lblMensaje = Estilo.etiqueta(" ", Font.PLAIN, 13, Estilo.ERROR);

    public PanelLogin(Navegador navegador, SistemaUCI sistema, Autenticacion autenticacion) {
        this.navegador = navegador;
        this.sistema = sistema;
        this.autenticacion = autenticacion;
        setLayout(new GridLayout(1, 2));
        add(crearLadoMarca());
        add(crearLadoFormulario());

        Filtros.soloNumeros(txtId, Validador.ID_MAX, Long.MAX_VALUE);
        Filtros.sinEspacios(txtClave, Validador.CLAVE_MAX);

        btnPaciente.addActionListener(e -> cambiarModo(false));
        btnFuncionario.addActionListener(e -> cambiarModo(true));
        btnIngresar.addActionListener(e -> ingresar());
        txtId.addActionListener(e -> ingresar());
        txtClave.addActionListener(e -> ingresar());
        cambiarModo(false);
    }

    private JPanel crearLadoMarca() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Estilo.AZUL_OSCURO, getWidth(), getHeight(), Estilo.AZUL_ACENTO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // circulos decorativos suaves
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(-80, getHeight() - 220, 320, 320);
                g2.fillOval(getWidth() - 160, -90, 260, 260);
                // cruz blanca
                int cx = getWidth() / 2;
                int cy = Math.max(110, getHeight() / 2 - 150);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cx - 14, cy - 44, 28, 88, 10, 10);
                g2.fillRoundRect(cx - 44, cy - 14, 88, 28, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(110, 30, 6, 30);
        JLabel hospital = Estilo.etiqueta("<html><div style='width:380px;text-align:center'>" + Estilo.HOSPITAL + "</div></html>",
                Font.BOLD, 28, Color.WHITE);
        hospital.setHorizontalAlignment(JLabel.CENTER);
        p.add(hospital, c);
        c.gridy = 1;
        c.insets = new Insets(4, 30, 4, 30);
        p.add(Estilo.etiqueta(Estilo.UNIDAD, Font.PLAIN, 18, new Color(0xE3F2FD)), c);
        c.gridy = 2;
        c.insets = new Insets(24, 40, 0, 40);
        JLabel desc = Estilo.etiqueta("<html><div style='width:340px;text-align:center'>Sistema de asignación prioritaria de camas.<br>"
                + "La atención se ordena por índice de gravedad: a menor índice, mayor urgencia.</div></html>",
                Font.PLAIN, 14, new Color(0xD6EAFB));
        desc.setHorizontalAlignment(JLabel.CENTER);
        p.add(desc, c);
        return p;
    }

    private JPanel crearLadoFormulario() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Color.WHITE);
        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        f.setPreferredSize(new Dimension(380, 460));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        f.add(Estilo.etiqueta("Iniciar sesión", Font.BOLD, 26, Estilo.TEXTO), c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 18, 0);
        f.add(Estilo.etiqueta("Seleccione cómo desea ingresar", Font.PLAIN, 14, Estilo.TEXTO_SUAVE), c);

        c.gridy = 2;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, 18, 6);
        f.add(btnPaciente, c);
        c.gridx = 1;
        c.insets = new Insets(0, 6, 18, 0);
        f.add(btnFuncionario, c);

        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 4, 0);
        f.add(Estilo.etiqueta("Número de identificación", Font.BOLD, 13, Estilo.TEXTO), c);
        c.gridy = 4;
        c.insets = new Insets(0, 0, 12, 0);
        f.add(txtId, c);
        c.gridy = 5;
        c.insets = new Insets(0, 0, 4, 0);
        f.add(lblClave, c);
        c.gridy = 6;
        c.insets = new Insets(0, 0, 8, 0);
        f.add(txtClave, c);
        c.gridy = 7;
        c.insets = new Insets(0, 0, 6, 0);
        f.add(lblAyuda, c);
        c.gridy = 8;
        c.insets = new Insets(0, 0, 14, 0);
        f.add(lblMensaje, c);
        c.gridy = 9;
        c.insets = new Insets(0, 0, 0, 0);
        f.add(btnIngresar, c);
        c.gridy = 10;
        c.insets = new Insets(26, 0, 0, 0);
        f.add(Estilo.etiqueta("<html>Proyecto académico — Estructuras de Datos<br>Universidad Nacional de Colombia, 2026-2</html>",
                Font.PLAIN, 12, Estilo.TEXTO_SUAVE), c);

        fondo.add(f);
        return fondo;
    }

    private void cambiarModo(boolean funcionario) {
        modoFuncionario = funcionario;
        btnPaciente.setTipo(funcionario ? Boton.Tipo.SECUNDARIO : Boton.Tipo.PRIMARIO);
        btnFuncionario.setTipo(funcionario ? Boton.Tipo.PRIMARIO : Boton.Tipo.SECUNDARIO);
        lblClave.setVisible(funcionario);
        txtClave.setVisible(funcionario);
        Estilo.neutro(lblAyuda, funcionario
                ? "Ingrese su ID de funcionario (solo números) y su clave."
                : "Ingrese su número de identificación (solo números).");
        lblMensaje.setText(" ");
        txtClave.setText("");
        txtId.requestFocusInWindow();
    }

    private void ingresar() {
        String id = txtId.getText().trim();
        String error = Validador.validarId(id);
        if (error != null) {
            Estilo.error(lblMensaje, error);
            return;
        }
        try {
            if (modoFuncionario) {
                String clave = new String(txtClave.getPassword());
                error = Validador.validarClave(clave);
                if (error != null) {
                    Estilo.error(lblMensaje, error);
                    return;
                }
                Funcionario f = autenticacion.ingresarFuncionario(id, clave);
                limpiar();
                navegador.mostrarFuncionario(f);
            } else {
                Paciente p = autenticacion.ingresarPaciente(sistema, id);
                limpiar();
                navegador.mostrarPaciente(p);
            }
        } catch (IllegalArgumentException ex) {
            Estilo.error(lblMensaje, ex.getMessage());
            txtClave.setText("");
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtClave.setText("");
        lblMensaje.setText(" ");
    }
}
