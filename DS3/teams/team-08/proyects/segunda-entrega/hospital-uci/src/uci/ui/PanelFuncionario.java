package uci.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import uci.modelo.Cama;
import uci.modelo.Funcionario;
import uci.modelo.Paciente;
import uci.modelo.Validador;
import uci.servicio.ResultadoAsignacion;
import uci.servicio.SistemaUCI;

/** Pantalla del funcionario: acceso completo a todas las operaciones del sistema. */
public class PanelFuncionario extends JPanel {

    private final Navegador navegador;
    private final SistemaUCI sistema;
    private final Funcionario funcionario;

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Map<String, Boton> navegacion = new LinkedHashMap<>();

    // --- Inicio
    private final JLabel lblTotal = Estilo.etiqueta("0", Font.BOLD, 30, Estilo.AZUL);
    private final JLabel lblCamas = Estilo.etiqueta("0", Font.BOLD, 30, Estilo.AZUL);
    private final JLabel lblConCama = Estilo.etiqueta("0", Font.BOLD, 30, Estilo.EXITO);
    private final JLabel lblEspera = Estilo.etiqueta("0", Font.BOLD, 30, Estilo.ALERTA);
    private final JLabel lblBanner = Estilo.etiqueta(" ", Font.BOLD, 13, Estilo.ALERTA);
    private final DefaultTableModel modeloLista =
            Estilo.modelo("Pos.", "ID", "Nombre", "Diagnóstico", "Índice", "Estado", "Ubicación");
    private final JComboBox<String> filtro =
            new JComboBox<>(new String[] {"Todos los pacientes", "Con cama asignada", "En lista de espera"});

    // --- Registro
    private final JTextField regId = Estilo.campo(20);
    private final JTextField regNombre = Estilo.campo(20);
    private final JTextField regDiagnostico = Estilo.campo(20);
    private final JTextField regIndice = Estilo.campo(20);
    private final JLabel regMensaje = Estilo.etiqueta(" ", Font.PLAIN, 13, Estilo.ERROR);

    // --- Gestion por ID
    private final JTextField gesId = Estilo.campo(20);
    private final JTextField gesIndice = Estilo.campo(8);
    private final JLabel gesEstado = Estilo.etiqueta(" ", Font.BOLD, 13, Estilo.TEXTO_SUAVE);
    private final JLabel gesMensaje = Estilo.etiqueta(" ", Font.PLAIN, 13, Estilo.ERROR);
    private final JLabel detNombre = valorDetalle();
    private final JLabel detDiagnostico = valorDetalle();
    private final JLabel detIndice = valorDetalle();
    private final JLabel detEstado = valorDetalle();
    private final JLabel detPosicion = valorDetalle();
    private final Boton btnActualizar = new Boton("Actualizar índice", Boton.Tipo.PRIMARIO);
    private final Boton btnAlta = new Boton("Dar de alta", Boton.Tipo.PELIGRO);
    private Paciente pacienteActual;

    // --- Camas
    private final JTextField camasCampo = Estilo.campo(6);
    private final JLabel camasMensaje = Estilo.etiqueta(" ", Font.PLAIN, 13, Estilo.ERROR);
    private final JLabel camasUltima = Estilo.etiqueta(" ", Font.PLAIN, 13, Estilo.TEXTO_SUAVE);
    private final DefaultTableModel modeloConCama = Estilo.modelo("Pos.", "Nombre", "Índice", "Cama", "Ubicación");
    private final DefaultTableModel modeloEspera = Estilo.modelo("Pos.", "Nombre", "Índice");

    public PanelFuncionario(Navegador navegador, SistemaUCI sistema, Funcionario funcionario) {
        this.navegador = navegador;
        this.sistema = sistema;
        this.funcionario = funcionario;
        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearLateral(), BorderLayout.WEST);

        contenido.setBackground(Estilo.AZUL_MUY_CLARO);
        contenido.add(crearInicio(), "inicio");
        contenido.add(crearRegistro(), "registro");
        contenido.add(crearGestion(), "gestion");
        contenido.add(crearCamas(), "camas");
        add(contenido, BorderLayout.CENTER);

        instalarFiltros();
        filtro.addActionListener(e -> refrescar());
        refrescar();
        mostrar("inicio");
    }

    // =========================================================== estructura general

    private JPanel crearEncabezado() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Estilo.AZUL);
        h.setBorder(new EmptyBorder(14, 24, 14, 24));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Estilo.etiqueta(Estilo.HOSPITAL, Font.BOLD, 20, Color.WHITE));
        textos.add(Estilo.etiqueta(Estilo.UNIDAD + " — Panel del funcionario", Font.PLAIN, 13, new Color(0xD6EAFB)));
        h.add(textos, BorderLayout.WEST);
        return h;
    }

    private JPanel crearLateral() {
        JPanel l = new JPanel();
        l.setBackground(Estilo.AZUL_CLARO);
        l.setPreferredSize(new Dimension(240, 100));
        l.setBorder(new EmptyBorder(20, 14, 20, 14));
        l.setLayout(new BoxLayout(l, BoxLayout.Y_AXIS));

        l.add(Estilo.etiqueta("Sesión de", Font.PLAIN, 12, Estilo.TEXTO_SUAVE));
        l.add(Estilo.etiqueta("<html>" + funcionario.getNombre() + "</html>", Font.BOLD, 14, Estilo.TEXTO));
        l.add(Estilo.etiqueta(funcionario.getCargo(), Font.PLAIN, 12, Estilo.AZUL));
        l.add(Box.createVerticalStrut(22));

        agregarNav(l, "inicio", "Panel general");
        agregarNav(l, "registro", "Registrar paciente");
        agregarNav(l, "gestion", "Gestionar paciente");
        agregarNav(l, "camas", "Camas y asignación");
        l.add(Box.createVerticalGlue());

        Boton salir = new Boton("Cerrar sesión", Boton.Tipo.SECUNDARIO);
        salir.setAlignmentX(0f);
        salir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        salir.addActionListener(e -> navegador.mostrarLogin());
        l.add(salir);
        return l;
    }

    private void agregarNav(JPanel lateral, String clave, String texto) {
        Boton b = new Boton(texto, Boton.Tipo.NAV);
        b.setAlignmentX(0f);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.addActionListener(e -> mostrar(clave));
        navegacion.put(clave, b);
        lateral.add(b);
        lateral.add(Box.createVerticalStrut(8));
    }

    private void mostrar(String clave) {
        for (Map.Entry<String, Boton> e : navegacion.entrySet()) {
            e.getValue().setTipo(e.getKey().equals(clave) ? Boton.Tipo.NAV_ACTIVO : Boton.Tipo.NAV);
        }
        cartas.show(contenido, clave);
        refrescar();
    }

    /** Seccion con titulo, subtitulo y un cuerpo. */
    private JPanel crearSeccion(String titulo, String subtitulo, JComponent cuerpo) {
        JPanel s = new JPanel(new BorderLayout(0, 14));
        s.setBackground(Estilo.AZUL_MUY_CLARO);
        s.setBorder(new EmptyBorder(22, 28, 22, 28));
        JPanel enc = new JPanel();
        enc.setOpaque(false);
        enc.setLayout(new BoxLayout(enc, BoxLayout.Y_AXIS));
        enc.add(Estilo.etiqueta(titulo, Font.BOLD, 24, Estilo.TEXTO));
        enc.add(Estilo.etiqueta(subtitulo, Font.PLAIN, 14, Estilo.TEXTO_SUAVE));
        s.add(enc, BorderLayout.NORTH);
        s.add(cuerpo, BorderLayout.CENTER);
        return s;
    }

    private static JLabel valorDetalle() {
        return Estilo.etiqueta("—", Font.PLAIN, 14, Estilo.TEXTO);
    }

    // =========================================================== 1. Inicio

    private JPanel crearInicio() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 14, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjetaDato("Pacientes registrados", lblTotal));
        tarjetas.add(tarjetaDato("Camas disponibles", lblCamas));
        tarjetas.add(tarjetaDato("Con cama asignada", lblConCama));
        tarjetas.add(tarjetaDato("En lista de espera", lblEspera));
        arriba.add(lblBanner, BorderLayout.NORTH);
        arriba.add(tarjetas, BorderLayout.CENTER);
        cuerpo.add(arriba, BorderLayout.NORTH);

        JPanel tarjetaTabla = Estilo.tarjeta();
        tarjetaTabla.setLayout(new BorderLayout(0, 10));
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Lista de prioridad (menor índice = mayor urgencia)", Font.BOLD, 15, Estilo.TEXTO),
                BorderLayout.WEST);
        filtro.setFont(Estilo.fuente(Font.PLAIN, 13));
        filtro.setBackground(Color.WHITE);
        barra.add(filtro, BorderLayout.EAST);
        tarjetaTabla.add(barra, BorderLayout.NORTH);
        javax.swing.JTable t = Estilo.tabla(modeloLista);
        anchos(t, 50, 120, -235, -225, 70, 85, 125);
        tarjetaTabla.add(Estilo.scroll(t), BorderLayout.CENTER);
        cuerpo.add(tarjetaTabla, BorderLayout.CENTER);

        return crearSeccion("Panel general", "Resumen de la unidad y lista de prioridad en tiempo real.", cuerpo);
    }

    private JPanel tarjetaDato(String titulo, JLabel valor) {
        JPanel t = Estilo.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Estilo.etiqueta(titulo, Font.PLAIN, 13, Estilo.TEXTO_SUAVE));
        t.add(Box.createVerticalStrut(4));
        t.add(valor);
        return t;
    }

    /** Anchos de columna: un valor negativo marca una columna flexible (puede encogerse); las demas no. */
    private static void anchos(javax.swing.JTable t, int... a) {
        for (int i = 0; i < a.length && i < t.getColumnCount(); i++) {
            javax.swing.table.TableColumn col = t.getColumnModel().getColumn(i);
            int ancho = Math.abs(a[i]);
            col.setPreferredWidth(ancho);
            col.setMinWidth(a[i] < 0 ? 70 : ancho);
        }
    }

    // =========================================================== 2. Registro

    private JPanel crearRegistro() {
        JPanel tarjeta = Estilo.tarjeta();
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setPreferredSize(new Dimension(560, 520));
        agregarCampo(tarjeta, 0, "ID del paciente", regId, "Solo números, de 5 a 12 dígitos.");
        agregarCampo(tarjeta, 1, "Nombre completo", regNombre, "Solo letras y espacios.");
        agregarCampo(tarjeta, 2, "Diagnóstico", regDiagnostico, "Solo letras y espacios.");
        agregarCampo(tarjeta, 3, "Índice de gravedad (0 a 100)", regIndice, "Solo números. Menor índice = mayor urgencia.");

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 12;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 12, 0);
        tarjeta.add(regMensaje, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botones.setOpaque(false);
        Boton registrar = new Boton("Registrar paciente", Boton.Tipo.PRIMARIO);
        Boton limpiar = new Boton("Limpiar", Boton.Tipo.SECUNDARIO);
        registrar.addActionListener(e -> registrarPaciente());
        limpiar.addActionListener(e -> limpiarRegistro(true));
        botones.add(registrar);
        botones.add(Box.createHorizontalStrut(10));
        botones.add(limpiar);
        c.gridy = 13;
        c.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(botones, c);

        return crearSeccion("Registrar paciente", "El ID debe ser único: se verifica contra la base de datos.",
                envolverArriba(tarjeta));
    }

    private void agregarCampo(JPanel panel, int n, String etiqueta, JComponent campo, String ayuda) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = n * 3;
        c.insets = new Insets(0, 0, 4, 0);
        panel.add(Estilo.etiqueta(etiqueta, Font.BOLD, 13, Estilo.TEXTO), c);
        c.gridy = n * 3 + 1;
        c.insets = new Insets(0, 0, 2, 0);
        panel.add(campo, c);
        c.gridy = n * 3 + 2;
        c.insets = new Insets(0, 0, 12, 0);
        panel.add(Estilo.etiqueta(ayuda, Font.PLAIN, 12, Estilo.TEXTO_SUAVE), c);
    }

    private JPanel envolverArriba(JComponent c) {
        JPanel w = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        w.setOpaque(false);
        w.add(c);
        return w;
    }

    private void registrarPaciente() {
        String id = regId.getText().trim();
        String nombre = regNombre.getText().trim();
        String diagnostico = regDiagnostico.getText().trim();
        String error = Validador.validarId(id);
        if (error == null) error = Validador.validarNombre(nombre);
        if (error == null) error = Validador.validarDiagnostico(diagnostico);
        if (error == null) error = Validador.validarIndiceTexto(regIndice.getText());
        if (error != null) {
            Estilo.error(regMensaje, error);
            return;
        }
        try {
            Paciente p = sistema.registrar(id, nombre, diagnostico, Integer.parseInt(regIndice.getText().trim()));
            Estilo.exito(regMensaje, "Paciente " + p.getNombre() + " registrado. El ID " + p.getId()
                    + " quedó guardado en la base de datos.");
            limpiarRegistro(false);
            refrescar();
        } catch (IllegalArgumentException | UncheckedIOException ex) {
            Estilo.error(regMensaje, ex.getMessage());
        }
    }

    private void limpiarRegistro(boolean borrarMensaje) {
        regId.setText("");
        regNombre.setText("");
        regDiagnostico.setText("");
        regIndice.setText("");
        if (borrarMensaje) {
            regMensaje.setText(" ");
        }
    }

    // =========================================================== 3. Gestion por ID

    private JPanel crearGestion() {
        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));

        // tarjeta de busqueda
        JPanel busqueda = Estilo.tarjeta();
        busqueda.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 4, 0);
        busqueda.add(Estilo.etiqueta("ID del paciente", Font.BOLD, 13, Estilo.TEXTO), c);
        c.gridy = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 8, 10);
        busqueda.add(gesId, c);
        c.gridx = 1;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 8, 0);
        Boton buscar = new Boton("Buscar en la base de datos", Boton.Tipo.PRIMARIO);
        busqueda.add(buscar, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 0, 0);
        busqueda.add(gesEstado, c);
        busqueda.setMaximumSize(new Dimension(700, 150));
        busqueda.setAlignmentX(0f);

        // tarjeta de datos y acciones
        JPanel datos = Estilo.tarjeta();
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        JPanel rejilla = new JPanel(new GridLayout(0, 2, 10, 8));
        rejilla.setOpaque(false);
        rejilla.add(Estilo.etiqueta("Nombre", Font.BOLD, 13, Estilo.TEXTO_SUAVE));
        rejilla.add(detNombre);
        rejilla.add(Estilo.etiqueta("Diagnóstico", Font.BOLD, 13, Estilo.TEXTO_SUAVE));
        rejilla.add(detDiagnostico);
        rejilla.add(Estilo.etiqueta("Índice de gravedad", Font.BOLD, 13, Estilo.TEXTO_SUAVE));
        rejilla.add(detIndice);
        rejilla.add(Estilo.etiqueta("Estado de cama", Font.BOLD, 13, Estilo.TEXTO_SUAVE));
        rejilla.add(detEstado);
        rejilla.add(Estilo.etiqueta("Posición en la lista", Font.BOLD, 13, Estilo.TEXTO_SUAVE));
        rejilla.add(detPosicion);
        rejilla.setAlignmentX(0f);
        datos.add(rejilla);
        datos.add(Box.createVerticalStrut(18));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acciones.setOpaque(false);
        acciones.setAlignmentX(0f);
        acciones.add(Estilo.etiqueta("Nuevo índice (0-100)  ", Font.BOLD, 13, Estilo.TEXTO));
        gesIndice.setPreferredSize(new Dimension(80, 40));
        acciones.add(gesIndice);
        acciones.add(Box.createHorizontalStrut(10));
        acciones.add(btnActualizar);
        acciones.add(Box.createHorizontalStrut(24));
        acciones.add(btnAlta);
        datos.add(acciones);
        datos.add(Box.createVerticalStrut(10));
        gesMensaje.setAlignmentX(0f);
        datos.add(gesMensaje);
        datos.setMaximumSize(new Dimension(700, 400));
        datos.setAlignmentX(0f);

        cuerpo.add(busqueda);
        cuerpo.add(Box.createVerticalStrut(14));
        cuerpo.add(datos);

        buscar.addActionListener(e -> buscarPaciente());
        gesId.addActionListener(e -> buscarPaciente());
        btnActualizar.addActionListener(e -> actualizarIndice());
        btnAlta.addActionListener(e -> darAlta());
        habilitarAcciones(false);

        return crearSeccion("Gestionar paciente",
                "Consulte un paciente por su ID, actualice su índice (empeoramiento o mejoría) o regístrele el alta.",
                cuerpo);
    }

    private void habilitarAcciones(boolean activo) {
        btnActualizar.setEnabled(activo);
        btnAlta.setEnabled(activo);
        gesIndice.setEnabled(activo);
    }

    private void buscarPaciente() {
        gesMensaje.setText(" ");
        String id = gesId.getText().trim();
        String error = Validador.validarId(id);
        if (error != null) {
            pacienteActual = null;
            limpiarDetalle();
            habilitarAcciones(false);
            Estilo.error(gesEstado, error);
            return;
        }
        Paciente p = sistema.buscar(id);
        if (p == null) {
            pacienteActual = null;
            limpiarDetalle();
            habilitarAcciones(false);
            Estilo.error(gesEstado, "El ID " + id + " NO existe en la base de datos de pacientes.");
        } else {
            pacienteActual = p;
            Estilo.exito(gesEstado, "El ID " + id + " existe en la base de datos de pacientes.");
            mostrarDetalle();
            habilitarAcciones(true);
        }
    }

    private void mostrarDetalle() {
        Paciente p = pacienteActual;
        detNombre.setText(p.getNombre());
        detDiagnostico.setText(p.getDiagnostico());
        detIndice.setText(p.getIndiceGravedad() + "  (" + p.nivelUrgencia() + ")");
        detEstado.setText(p.tieneCama() ? Cama.descripcion(p.getCama()) : "En lista de espera");
        detPosicion.setText("#" + sistema.posicion(p.getId()) + " de " + sistema.cantidad());
    }

    private void limpiarDetalle() {
        detNombre.setText("—");
        detDiagnostico.setText("—");
        detIndice.setText("—");
        detEstado.setText("—");
        detPosicion.setText("—");
        gesIndice.setText("");
    }

    private void actualizarIndice() {
        if (pacienteActual == null) {
            return;
        }
        String error = Validador.validarIndiceTexto(gesIndice.getText());
        if (error != null) {
            Estilo.error(gesMensaje, error);
            return;
        }
        try {
            int nuevo = Integer.parseInt(gesIndice.getText().trim());
            int viejo = pacienteActual.getIndiceGravedad();
            int antes = sistema.posicion(pacienteActual.getId());
            sistema.modificarIndice(pacienteActual.getId(), nuevo);
            int despues = sistema.posicion(pacienteActual.getId());
            mostrarDetalle();
            gesIndice.setText("");
            Estilo.exito(gesMensaje, "Índice actualizado de " + viejo + " a " + nuevo + ". Posición en la lista: #"
                    + antes + " → #" + despues + ". Ejecute la asignación para reflejarlo en las camas.");
            refrescar();
        } catch (IllegalArgumentException | UncheckedIOException ex) {
            Estilo.error(gesMensaje, ex.getMessage());
        }
    }

    private void darAlta() {
        if (pacienteActual == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Confirma el alta médica de " + pacienteActual.getNombre() + " (ID " + pacienteActual.getId() + ")?\n"
                        + "El paciente se eliminará de la base de datos.",
                "Confirmar alta médica", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Paciente p = sistema.darAlta(pacienteActual.getId());
            pacienteActual = null;
            limpiarDetalle();
            habilitarAcciones(false);
            gesId.setText("");
            gesEstado.setText(" ");
            Estilo.exito(gesMensaje, "Alta registrada: " + p.getNombre() + " fue eliminado de la base de datos."
                    + (p.tieneCama() ? " La " + Cama.codigo(p.getCama()) + " quedó libre al ejecutar la asignación." : ""));
            refrescar();
        } catch (IllegalArgumentException | UncheckedIOException ex) {
            Estilo.error(gesMensaje, ex.getMessage());
        }
    }

    // =========================================================== 4. Camas y asignacion

    private JPanel crearCamas() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);

        JPanel config = Estilo.tarjeta();
        config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(0f);
        fila.add(Estilo.etiqueta("Camas disponibles (1 a " + Cama.MAX + ")  ", Font.BOLD, 13, Estilo.TEXTO));
        camasCampo.setPreferredSize(new Dimension(80, 40));
        fila.add(camasCampo);
        fila.add(Box.createHorizontalStrut(10));
        Boton guardar = new Boton("Guardar cantidad", Boton.Tipo.SECUNDARIO);
        Boton asignar = new Boton("Ejecutar asignación de camas", Boton.Tipo.PRIMARIO);
        fila.add(guardar);
        fila.add(Box.createHorizontalStrut(24));
        fila.add(asignar);
        config.add(fila);
        config.add(Box.createVerticalStrut(8));
        camasUltima.setAlignmentX(0f);
        camasMensaje.setAlignmentX(0f);
        config.add(camasUltima);
        config.add(camasMensaje);
        cuerpo.add(config, BorderLayout.NORTH);

        JPanel tablas = new JPanel(new GridBagLayout());
        tablas.setOpaque(false);
        javax.swing.JTable t1 = Estilo.tabla(modeloConCama);
        anchos(t1, 50, -200, 70, 85, 125);
        javax.swing.JTable t2 = Estilo.tabla(modeloEspera);
        anchos(t2, 50, -200, 70);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.gridy = 0;
        g.gridx = 0;
        g.weightx = 0.6;
        g.insets = new Insets(0, 0, 0, 7);
        tablas.add(bloqueTabla("Pacientes con cama", t1), g);
        g.gridx = 1;
        g.weightx = 0.4;
        g.insets = new Insets(0, 7, 0, 0);
        tablas.add(bloqueTabla("Lista de espera", t2), g);
        cuerpo.add(tablas, BorderLayout.CENTER);

        guardar.addActionListener(e -> guardarCamas());
        camasCampo.addActionListener(e -> guardarCamas());
        asignar.addActionListener(e -> ejecutarAsignacion());
        return crearSeccion("Camas y asignación",
                "Defina cuántas camas hay y asigne a los pacientes de menor índice (mayor urgencia).", cuerpo);
    }

    private JPanel bloqueTabla(String titulo, javax.swing.JTable tabla) {
        JPanel b = Estilo.tarjeta();
        b.setLayout(new BorderLayout(0, 8));
        b.add(Estilo.etiqueta(titulo, Font.BOLD, 15, Estilo.TEXTO), BorderLayout.NORTH);
        b.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        return b;
    }

    private void guardarCamas() {
        String txt = camasCampo.getText().trim();
        if (txt.isEmpty()) {
            Estilo.error(camasMensaje, "Ingrese la cantidad de camas.");
            return;
        }
        try {
            sistema.definirCamas(Integer.parseInt(txt));
            Estilo.exito(camasMensaje, "Camas disponibles: " + sistema.getCamas()
                    + ". Ejecute la asignación para aplicar el cambio.");
            refrescar();
        } catch (IllegalArgumentException | UncheckedIOException ex) {
            Estilo.error(camasMensaje, ex.getMessage());
        }
    }

    private void ejecutarAsignacion() {
        try {
            ResultadoAsignacion r = sistema.asignar();
            Estilo.exito(camasMensaje, "Asignación realizada: " + r.conCama().size() + " paciente(s) con cama y "
                    + r.enEspera().size() + " en lista de espera.");
            refrescar();
        } catch (UncheckedIOException ex) {
            Estilo.error(camasMensaje, ex.getMessage());
        }
    }

    // =========================================================== refresco

    /** Vuelve a leer el sistema y actualiza tarjetas, banner y tablas. */
    public void refrescar() {
        List<Paciente> todos = sistema.listarOrdenado();
        int conCama = 0;
        for (Paciente p : todos) {
            if (p.tieneCama()) conCama++;
        }
        lblTotal.setText(String.valueOf(todos.size()));
        lblCamas.setText(String.valueOf(sistema.getCamas()));
        lblConCama.setText(String.valueOf(conCama));
        lblEspera.setText(String.valueOf(todos.size() - conCama));
        if (sistema.isCambiosPendientes()) {
            lblBanner.setText("Hay cambios sin asignar: ejecute la asignación en «Camas y asignación» para actualizar las camas.");
        } else {
            lblBanner.setText(" ");
        }
        camasCampo.setText(String.valueOf(sistema.getCamas()));
        Estilo.neutro(camasUltima, "Última asignación: " + sistema.getUltimaAsignacion());

        modeloLista.setRowCount(0);
        modeloConCama.setRowCount(0);
        modeloEspera.setRowCount(0);
        int sel = filtro.getSelectedIndex();
        for (int i = 0; i < todos.size(); i++) {
            Paciente p = todos.get(i);
            int pos = i + 1;
            String estado = p.tieneCama() ? Cama.codigo(p.getCama()) : "En espera";
            String ubic = p.tieneCama() ? Cama.ubicacion(p.getCama()) : "—";
            if (sel == 0 || (sel == 1 && p.tieneCama()) || (sel == 2 && !p.tieneCama())) {
                modeloLista.addRow(new Object[] {pos, p.getId(), p.getNombre(), p.getDiagnostico(),
                        p.getIndiceGravedad(), estado, ubic});
            }
            if (p.tieneCama()) {
                modeloConCama.addRow(new Object[] {pos, p.getNombre(), p.getIndiceGravedad(), estado, ubic});
            } else {
                modeloEspera.addRow(new Object[] {pos, p.getNombre(), p.getIndiceGravedad()});
            }
        }
        if (pacienteActual != null && sistema.existe(pacienteActual.getId())) {
            mostrarDetalle();
        }
    }

    private void instalarFiltros() {
        Filtros.soloNumeros(regId, Validador.ID_MAX, Long.MAX_VALUE);
        Filtros.soloLetras(regNombre, Validador.TEXTO_MAX);
        Filtros.soloLetras(regDiagnostico, Validador.TEXTO_MAX);
        Filtros.soloNumeros(regIndice, 3, Validador.INDICE_MAX);
        Filtros.soloNumeros(gesId, Validador.ID_MAX, Long.MAX_VALUE);
        Filtros.soloNumeros(gesIndice, 3, Validador.INDICE_MAX);
        Filtros.soloNumeros(camasCampo, 2, Cama.MAX);
    }
}
