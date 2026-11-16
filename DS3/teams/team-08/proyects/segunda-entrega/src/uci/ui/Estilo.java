package uci.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Paleta (azul claro y blanco) y fabricas de componentes con estilo uniforme. */
public final class Estilo {

    public static final Color AZUL = new Color(0x1565C0);
    public static final Color AZUL_OSCURO = new Color(0x0D47A1);
    public static final Color AZUL_ACENTO = new Color(0x42A5F5);
    public static final Color AZUL_CLARO = new Color(0xE3F2FD);
    public static final Color AZUL_MUY_CLARO = new Color(0xF4F9FE);
    public static final Color BORDE = new Color(0xBBDEFB);
    public static final Color TEXTO = new Color(0x0D2B4A);
    public static final Color TEXTO_SUAVE = new Color(0x5C7A99);
    public static final Color ERROR = new Color(0xC62828);
    public static final Color EXITO = new Color(0x2E7D32);
    public static final Color ALERTA = new Color(0xB26A00);

    public static final String HOSPITAL = "Hospital Universitario Nacional de Colombia";
    public static final String UNIDAD = "Unidad de Cuidados Intensivos";

    private Estilo() { }

    public static Font fuente(int estilo, int tamano) {
        return new Font("Segoe UI", estilo, tamano);
    }

    public static JLabel etiqueta(String texto, int estilo, int tamano, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(estilo, tamano));
        l.setForeground(color);
        return l;
    }

    public static JTextField campo(int columnas) {
        JTextField c = new JTextField(columnas);
        c.setFont(fuente(Font.PLAIN, 15));
        c.setForeground(TEXTO);
        c.setBorder(bordeCampo());
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 40));
        return c;
    }

    public static JPasswordField campoClave(int columnas) {
        JPasswordField c = new JPasswordField(columnas);
        c.setFont(fuente(Font.PLAIN, 15));
        c.setForeground(TEXTO);
        c.setBorder(bordeCampo());
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 40));
        return c;
    }

    private static CompoundBorder bordeCampo() {
        return new CompoundBorder(new LineBorder(AZUL_ACENTO, 1, true), new EmptyBorder(4, 10, 4, 10));
    }

    /** Panel blanco con borde azul claro y relleno interno. */
    public static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDE, 1, true), new EmptyBorder(18, 22, 18, 22)));
        return p;
    }

    public static void error(JLabel l, String texto) {
        l.setForeground(ERROR);
        l.setText("<html>" + texto + "</html>");
    }

    public static void exito(JLabel l, String texto) {
        l.setForeground(EXITO);
        l.setText("<html>" + texto + "</html>");
    }

    public static void neutro(JLabel l, String texto) {
        l.setForeground(TEXTO_SUAVE);
        l.setText("<html>" + texto + "</html>");
    }

    /** Modelo de tabla que no permite editar celdas. */
    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(fuente(Font.PLAIN, 14));
        t.setForeground(TEXTO);
        t.setSelectionBackground(AZUL_CLARO);
        t.setSelectionForeground(TEXTO);
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new RenderizadorCelda());

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 36));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foco, int f, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, f, c);
                l.setBackground(AZUL);
                l.setForeground(Color.WHITE);
                l.setFont(fuente(Font.BOLD, 13));
                l.setBorder(new EmptyBorder(0, 8, 0, 8));
                l.setOpaque(true);
                return l;
            }
        });
        return t;
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane s = new JScrollPane(t);
        s.setBorder(new LineBorder(BORDE, 1, true));
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    /** Celdas con filas alternadas; resalta las camas asignadas y la lista de espera. */
    private static class RenderizadorCelda extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, f, c);
            l.setBorder(new EmptyBorder(0, 8, 0, 8));
            l.setFont(fuente(Font.PLAIN, 14));
            l.setForeground(TEXTO);
            if (!sel) {
                l.setBackground(f % 2 == 0 ? Color.WHITE : AZUL_MUY_CLARO);
            }
            String s = (v == null) ? "" : v.toString();
            if (s.startsWith("UCI-")) {
                l.setForeground(EXITO);
                l.setFont(fuente(Font.BOLD, 14));
            } else if (s.equals("En espera")) {
                l.setForeground(ALERTA);
                l.setFont(fuente(Font.BOLD, 14));
            }
            return l;
        }
    }
}
