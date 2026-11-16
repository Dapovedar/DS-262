package uci.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import uci.modelo.Cama;
import uci.modelo.Paciente;
import uci.servicio.SistemaUCI;

/** Pantalla del paciente: solo consulta su propio estado y si tiene cama UCI asignada. */
public class PanelPaciente extends JPanel {

    private final Navegador navegador;
    private final SistemaUCI sistema;
    private final String idPaciente;

    private final JLabel lblBienvenida = Estilo.etiqueta(" ", Font.BOLD, 26, Estilo.TEXTO);
    private final JLabel lblBadge = Estilo.etiqueta(" ", Font.BOLD, 18, Color.WHITE);
    private final JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 14));
    private final JLabel lblCama = Estilo.etiqueta(" ", Font.BOLD, 22, Estilo.TEXTO);
    private final JLabel lblDetalleCama = Estilo.etiqueta(" ", Font.PLAIN, 15, Estilo.TEXTO_SUAVE);
    private final JLabel vId = valor();
    private final JLabel vDiagnostico = valor();
    private final JLabel vIndice = valor();
    private final JLabel vPosicion = valor();
    private final JLabel vCamas = valor();
    private final JLabel vActualizacion = valor();

    public PanelPaciente(Navegador navegador, SistemaUCI sistema, Paciente paciente) {
        this.navegador = navegador;
        this.sistema = sistema;
        this.idPaciente = paciente.getId();
        setLayout(new BorderLayout());
        setBackground(Estilo.AZUL_MUY_CLARO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        refrescar();
    }

    private static JLabel valor() {
        return Estilo.etiqueta("—", Font.PLAIN, 15, Estilo.TEXTO);
    }

    private JPanel crearEncabezado() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Estilo.AZUL);
        h.setBorder(new EmptyBorder(14, 24, 14, 24));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Estilo.etiqueta(Estilo.HOSPITAL, Font.BOLD, 20, Color.WHITE));
        textos.add(Estilo.etiqueta(Estilo.UNIDAD + " — Consulta del paciente", Font.PLAIN, 13, new Color(0xD6EAFB)));
        h.add(textos, BorderLayout.WEST);
        return h;
    }

    private JPanel crearCentro() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setOpaque(false);

        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setPreferredSize(new Dimension(720, 600));

        lblBienvenida.setAlignmentX(0f);
        columna.add(lblBienvenida);
        columna.add(Box.createVerticalStrut(4));
        JLabel sub = Estilo.etiqueta("Aquí puede consultar su estado actual en la unidad.", Font.PLAIN, 14, Estilo.TEXTO_SUAVE);
        sub.setAlignmentX(0f);
        columna.add(sub);
        columna.add(Box.createVerticalStrut(18));

        // tarjeta de estado de cama
        JPanel estado = Estilo.tarjeta();
        estado.setLayout(new BoxLayout(estado, BoxLayout.Y_AXIS));
        estado.setAlignmentX(0f);
        estado.setMaximumSize(new Dimension(720, 210));
        badge.setBorder(null);
        badge.add(lblBadge);
        badge.setAlignmentX(0f);
        badge.setMaximumSize(new Dimension(420, 56));
        estado.add(badge);
        estado.add(Box.createVerticalStrut(16));
        lblCama.setAlignmentX(0f);
        lblDetalleCama.setAlignmentX(0f);
        estado.add(lblCama);
        estado.add(Box.createVerticalStrut(4));
        estado.add(lblDetalleCama);
        columna.add(estado);
        columna.add(Box.createVerticalStrut(14));

        // tarjeta de datos
        JPanel datos = Estilo.tarjeta();
        datos.setLayout(new GridLayout(0, 2, 12, 10));
        datos.setAlignmentX(0f);
        datos.setMaximumSize(new Dimension(720, 260));
        agregar(datos, "Número de identificación", vId);
        agregar(datos, "Diagnóstico", vDiagnostico);
        agregar(datos, "Índice de gravedad", vIndice);
        agregar(datos, "Posición en la lista de prioridad", vPosicion);
        agregar(datos, "Camas disponibles en la UCI", vCamas);
        agregar(datos, "Última actualización de camas", vActualizacion);
        columna.add(datos);
        columna.add(Box.createVerticalStrut(18));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(0f);
        Boton actualizar = new Boton("Actualizar información", Boton.Tipo.PRIMARIO);
        Boton salir = new Boton("Cerrar sesión", Boton.Tipo.SECUNDARIO);
        actualizar.addActionListener(e -> refrescar());
        salir.addActionListener(e -> navegador.mostrarLogin());
        botones.add(actualizar);
        botones.add(Box.createHorizontalStrut(10));
        botones.add(salir);
        columna.add(botones);

        fondo.add(columna);
        return fondo;
    }

    private void agregar(JPanel panel, String etiqueta, JLabel valor) {
        panel.add(Estilo.etiqueta(etiqueta, Font.BOLD, 13, Estilo.TEXTO_SUAVE));
        panel.add(valor);
    }

    /** Vuelve a leer el estado del paciente en la base de datos. */
    public void refrescar() {
        Paciente p = sistema.buscar(idPaciente);
        if (p == null) { // el paciente recibio el alta mientras tenia la sesion abierta
            lblBienvenida.setText("Sesión finalizada");
            badge.setBackground(Estilo.AZUL);
            lblBadge.setText("ALTA MÉDICA REGISTRADA");
            lblCama.setText("Su registro ya no está en la lista de la UCI.");
            lblDetalleCama.setText("Si tiene dudas, consulte con el personal del hospital.");
            return;
        }
        lblBienvenida.setText("Hola, " + p.getNombre());
        if (p.tieneCama()) {
            badge.setBackground(Estilo.EXITO);
            lblBadge.setText("CAMA UCI ASIGNADA");
            lblCama.setText("Cama " + Cama.codigo(p.getCama()));
            lblDetalleCama.setText("Ubicación: " + Cama.ubicacion(p.getCama()));
        } else {
            badge.setBackground(Estilo.ALERTA);
            lblBadge.setText("EN LISTA DE ESPERA");
            lblCama.setText("Aún no tiene cama UCI asignada");
            lblDetalleCama.setText("Será asignado cuando se libere una cama, según su prioridad clínica.");
        }
        badge.setOpaque(true);
        vId.setText(p.getId());
        vDiagnostico.setText(p.getDiagnostico());
        vIndice.setText(p.getIndiceGravedad() + "  (" + p.nivelUrgencia() + ")");
        vPosicion.setText("#" + sistema.posicion(p.getId()) + " de " + sistema.cantidad());
        vCamas.setText(String.valueOf(sistema.getCamas()));
        vActualizacion.setText(sistema.getUltimaAsignacion());
    }
}
