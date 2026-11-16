package uci.modelo;

/**
 * Reglas de validacion de datos de entrada. Cada metodo validarXxx devuelve
 * null si el dato es correcto, o un mensaje de error en caso contrario.
 * Se usa tanto en la interfaz grafica como en la consola y en el modelo.
 */
public final class Validador {

    public static final int ID_MIN = 5;
    public static final int ID_MAX = 12;
    public static final int TEXTO_MIN = 3;
    public static final int TEXTO_MAX = 60;
    public static final int INDICE_MIN = 0;
    public static final int INDICE_MAX = 100;
    public static final int CLAVE_MIN = 4;
    public static final int CLAVE_MAX = 20;

    private Validador() { }

    /** true si todos los caracteres son digitos 0-9 (la cadena vacia tambien cumple). */
    public static boolean soloDigitos(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /** true si solo hay letras (con tildes y enie) y espacios simples, sin espacio inicial. */
    public static boolean soloLetrasYEspacios(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ' ') {
                if (i == 0 || s.charAt(i - 1) == ' ') {
                    return false;
                }
            } else if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    public static String validarId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "El ID es obligatorio.";
        }
        id = id.trim();
        if (!soloDigitos(id)) {
            return "El ID solo puede contener números (0-9).";
        }
        if (id.length() < ID_MIN || id.length() > ID_MAX) {
            return "El ID debe tener entre " + ID_MIN + " y " + ID_MAX + " dígitos.";
        }
        return null;
    }

    public static String validarNombre(String nombre) {
        return validarTexto(nombre, "El nombre");
    }

    public static String validarDiagnostico(String diagnostico) {
        return validarTexto(diagnostico, "El diagnóstico");
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            return campo + " es obligatorio.";
        }
        texto = texto.trim();
        if (!soloLetrasYEspacios(texto)) {
            return campo + " solo puede contener letras y espacios.";
        }
        if (texto.length() < TEXTO_MIN || texto.length() > TEXTO_MAX) {
            return campo + " debe tener entre " + TEXTO_MIN + " y " + TEXTO_MAX + " letras.";
        }
        return null;
    }

    public static String validarIndice(int indice) {
        if (indice < INDICE_MIN || indice > INDICE_MAX) {
            return "El índice de gravedad debe estar entre " + INDICE_MIN + " y " + INDICE_MAX + ".";
        }
        return null;
    }

    /** Valida el indice escrito como texto (solo digitos, rango permitido). */
    public static String validarIndiceTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "El índice de gravedad es obligatorio.";
        }
        texto = texto.trim();
        if (!soloDigitos(texto) || texto.length() > 3) {
            return "El índice de gravedad solo puede ser un número entero entre "
                    + INDICE_MIN + " y " + INDICE_MAX + ".";
        }
        return validarIndice(Integer.parseInt(texto));
    }

    public static String validarCamas(int camas) {
        if (camas < 1 || camas > Cama.MAX) {
            return "La cantidad de camas debe estar entre 1 y " + Cama.MAX + ".";
        }
        return null;
    }

    public static String validarClave(String clave) {
        if (clave == null || clave.isEmpty()) {
            return "La clave es obligatoria.";
        }
        if (clave.length() < CLAVE_MIN || clave.length() > CLAVE_MAX) {
            return "La clave debe tener entre " + CLAVE_MIN + " y " + CLAVE_MAX + " caracteres.";
        }
        return null;
    }
}
