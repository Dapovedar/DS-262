package uci.ui;

import java.util.function.Predicate;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import uci.modelo.Validador;

/**
 * Restringe lo que se puede ESCRIBIR en un campo: si el texto resultante no es valido,
 * el cambio se rechaza (no aparece el caracter). Tambien protege contra pegar texto invalido.
 */
public final class Filtros {

    private Filtros() { }

    /** Solo digitos, con un maximo de digitos y un valor numerico maximo. */
    public static void soloNumeros(JTextComponent campo, int maxDigitos, long valorMaximo) {
        instalar(campo, s -> s.length() <= maxDigitos
                && Validador.soloDigitos(s)
                && (s.isEmpty() || Long.parseLong(s) <= valorMaximo));
    }

    /** Solo letras (con tildes y enie) y espacios simples. */
    public static void soloLetras(JTextComponent campo, int maxLargo) {
        instalar(campo, s -> s.length() <= maxLargo && Validador.soloLetrasYEspacios(s));
    }

    /** Cualquier caracter visible, sin espacios (claves). */
    public static void sinEspacios(JTextComponent campo, int maxLargo) {
        instalar(campo, s -> s.length() <= maxLargo && s.indexOf(' ') < 0);
    }

    private static void instalar(JTextComponent campo, Predicate<String> valido) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int pos, String texto, AttributeSet a) throws BadLocationException {
                replace(fb, pos, 0, texto, a);
            }

            @Override
            public void replace(FilterBypass fb, int pos, int largo, String texto, AttributeSet a)
                    throws BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = new StringBuilder(actual).replace(pos, pos + largo, texto == null ? "" : texto).toString();
                if (valido.test(nuevo)) {
                    super.replace(fb, pos, largo, texto, a);
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(campo);
                }
            }
        });
    }
}
