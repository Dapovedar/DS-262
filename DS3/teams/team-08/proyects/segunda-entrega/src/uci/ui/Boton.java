package uci.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.ButtonModel;
import javax.swing.JButton;

/** Boton plano con esquinas redondeadas y colores de la paleta azul/blanco. */
public class Boton extends JButton {

    public enum Tipo { PRIMARIO, SECUNDARIO, PELIGRO, NAV, NAV_ACTIVO }

    private Tipo tipo;

    public Boton(String texto, Tipo tipo) {
        super(texto);
        this.tipo = tipo;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(Estilo.fuente(java.awt.Font.BOLD, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 28, Math.max(d.height + 12, 40));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ButtonModel m = getModel();
        Color fondo;
        Color texto;
        Color borde = null;
        switch (tipo) {
            case PRIMARIO:
                fondo = m.isPressed() ? Estilo.AZUL_OSCURO : m.isRollover() ? new Color(0x1976D2) : Estilo.AZUL;
                texto = Color.WHITE;
                break;
            case SECUNDARIO:
                fondo = m.isRollover() ? Estilo.AZUL_CLARO : Color.WHITE;
                texto = Estilo.AZUL;
                borde = Estilo.AZUL_ACENTO;
                break;
            case PELIGRO:
                fondo = m.isPressed() ? new Color(0x8E0000) : m.isRollover() ? new Color(0xD32F2F) : Estilo.ERROR;
                texto = Color.WHITE;
                break;
            case NAV:
                fondo = m.isRollover() ? new Color(0xCFE5FA) : Estilo.AZUL_CLARO;
                texto = Estilo.AZUL_OSCURO;
                break;
            default: // NAV_ACTIVO
                fondo = Estilo.AZUL;
                texto = Color.WHITE;
        }
        if (!isEnabled()) {
            fondo = new Color(0xE1EAF3);
            texto = new Color(0x8AA0B5);
            borde = null;
        }
        int w = getWidth();
        int h = getHeight();
        g2.setColor(fondo);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        if (borde != null) {
            g2.setColor(borde);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
        }
        g2.setFont(getFont());
        g2.setColor(texto);
        FontMetrics fm = g2.getFontMetrics();
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        boolean izquierda = (tipo == Tipo.NAV || tipo == Tipo.NAV_ACTIVO);
        int x = izquierda ? 16 : (w - fm.stringWidth(getText())) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
